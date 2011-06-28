/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.util;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import trafficownage.simulation.*;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class ManhattanMapGenerator {
    private int width,height;
    private double blockSize;
    private long seed;

    private Node[][] grid;
    private List<Node> nodes;
    private List<Road> roads;

    private List<Node> localNodes;
    private List<Node> spawnNodes;

    private Road[] horizontalRoads;
    private Road[] verticalRoads;
    private List<Pair<Node,Node>>[] horizontalAvenues;
    private List<Pair<Node,Node>>[] verticalAvenues;

    private List<Integer> verticalMainRoads;
    private List<Integer> horizontalMainRoads;
    private List<Integer> verticalHighways;
    private List<Integer> horizontalHighways;
    
    private HashMap<Integer,List<Node>> areas = new HashMap<Integer, List<Node>>();

    public static final int ALL_NODES = -1;
    public static final int LOCAL_NODES = 0;
    public static final int SPAWN_NODES = 1;

    private Random rand;

    public ManhattanMapGenerator() {
    }

    public void generate(int width, int height, double blockSize, Integer[] verticalMainRoads, Integer[] horizontalMainRoads, Integer[] verticalHighways, Integer[] horizontalHighways) {
        rand = new Random();
        generate(rand.nextLong(),width,height,blockSize,verticalMainRoads,horizontalMainRoads,verticalHighways,horizontalHighways);
    }

    public void generate(long seed, int width, int height, double blockSize, Integer[] verticalMainRoads, Integer[] horizontalMainRoads, Integer[] verticalHighways, Integer[] horizontalHighways) {
        this.seed = seed;
        this.width = width;
        this.height = height;
        this.blockSize = blockSize;
        
        this.verticalMainRoads = Arrays.asList(verticalMainRoads);
        this.horizontalMainRoads = Arrays.asList(horizontalMainRoads);

        this.verticalHighways = Arrays.asList(verticalHighways);
        this.horizontalHighways = Arrays.asList(horizontalHighways);

        rand = new Random(seed);

        areas = new HashMap<Integer,List<Node>>();
        nodes = new ArrayList<Node>();
        localNodes = new ArrayList<Node>();
        spawnNodes = new ArrayList<Node>();
        roads = new ArrayList<Road>();

        generateNodes();

        generateRoads();
        
        areas.put(ALL_NODES, nodes);
        areas.put(LOCAL_NODES, localNodes);
        areas.put(SPAWN_NODES, spawnNodes);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Node> getLocalNodes() {
        return localNodes;
    }

    public List<Node> getSpawnNodes() {
        return spawnNodes;
    }

    
    
    public int requestArea(int x1, int y1, int x2, int y2) {
        List<Node> nodes = new ArrayList<Node>();
        int x,y;

        for (x = x1; x <= x2; x++)
            for (y = y1; y <= y2; y++)
                nodes.add(grid[x][y]);

        int i = areas.size();

        while (areas.containsKey(i))
            i++;

        areas.put(i,nodes);

        return i;
    }

    public HashMap<Integer,List<Node>> getAreas() {      


        return areas;

    }

    public List<Road> getRoads() {
        return roads;
    }
    
    private void generateNodes() {
        double x_start = -(((double)width / 2.0) * blockSize);
        double y_start = -(((double)height / 2.0) * blockSize);

        double x_loc = x_start;

        double y_loc;


        grid = new Node[width+1][height+1];

        Node n;

        for (int x = 0; x <= width; x++) {

            y_loc = y_start;

            for (int y = 0; y <= height; y++) {

                n = new TrafficLight(new Point2D.Double(x_loc,y_loc));
                
                grid[x][y] = n;
                nodes.add(n);

                if (!horizontalMainRoads.contains(x) && !verticalMainRoads.contains(y) )
                    localNodes.add(n);

                y_loc += blockSize;
            }
            
            x_loc += blockSize;
        }


    }

    private void addSpawnNodes() {
        Node n;
        double x_loc,y_loc;



        for (int x : horizontalHighways) {
            x_loc = grid[x][0].getLocation().getX();


            y_loc = grid[x][0].getLocation().getY() - (blockSize * 8.0);
            n = new SpawnNode(new Point2D.Double(x_loc,y_loc),5.0);
            nodes.add(n);
            spawnNodes.add(n);
            verticalAvenues[x].add(0,new Pair<Node,Node>(n,grid[x][0]));


            y_loc = grid[x][height].getLocation().getY() + (blockSize * 8.0);
            n = new SpawnNode(new Point2D.Double(x_loc,y_loc),5.0);
            nodes.add(n);
            spawnNodes.add(n);
            verticalAvenues[x].add(verticalAvenues[x].size(),new Pair<Node,Node>(grid[x][height],n));

        }


        for (int y : verticalHighways) {
            y_loc = grid[0][y].getLocation().getY();


            x_loc = grid[0][y].getLocation().getX() - (blockSize * 8.0);
            n = new SpawnNode(new Point2D.Double(x_loc,y_loc),5.0);
            nodes.add(n);
            spawnNodes.add(n);
            horizontalAvenues[y].add(0,new Pair<Node,Node>(n,grid[0][y]));


            x_loc = grid[width][y].getLocation().getX() + (blockSize * 8.0);
            n = new SpawnNode(new Point2D.Double(x_loc,y_loc),5.0);
            nodes.add(n);
            spawnNodes.add(n);
            horizontalAvenues[y].add(horizontalAvenues[y].size(),new Pair<Node,Node>(grid[width][y],n));

        }
    }


    private static final int 
            SMALL_ROAD = 0,
            MAIN_ROAD = 1,
            HIGHWAY = 2;
    
    private void generateRoads() {
        generateRoadPairs();

        addSpawnNodes();

        RoadSegment rs = null;

        int v = 0;
        int roadType = -1;

        for (List<Pair<Node,Node>> verticalAvenue : verticalAvenues) {
            
            if (verticalMainRoads.contains(v))
                roadType = MAIN_ROAD;
            else if (verticalHighways.contains(v))
                roadType = HIGHWAY;
            else
                roadType = SMALL_ROAD;
            
            Road r = new Road("Vertical " + Integer.toString(v + 1));

            for (Pair<Node,Node> pair : verticalAvenue) {

                if (roadType == HIGHWAY) {
                    rs = new RoadSegment(r, 70.0 / 3.6, pair.getObject1(), pair.getObject2());

                    rs.addLeftStartLane(0, false);
                    rs.addLeftStartLane(1, false);

                    rs.addLeftEndLane(10, false);
                    rs.addLeftEndLane(11, false);

                } else if (roadType == MAIN_ROAD) {
                    rs = new RoadSegment(r, 50.0 / 3.6, pair.getObject1(), pair.getObject2());

                    rs.addLeftStartLane(0, false);
                    rs.addLeftStartLane(1, false);

                    rs.addLeftEndLane(10, false);
                    rs.addLeftEndLane(11, false);

                } else if (roadType == SMALL_ROAD) {

                    rs = new RoadSegment(r, 30.0 / 3.6, pair.getObject1(), pair.getObject2());
                    rs.addLeftStartLane(0, false);
                    rs.addLeftEndLane(10, false);

                }
                
                r.addLast(rs);
            }
            roads.add(r);
            v++;
        }
        
        int h = 0;
        for (List<Pair<Node,Node>> horizontalAvenue : horizontalAvenues) {

            if (horizontalMainRoads.contains(h))
                roadType = MAIN_ROAD;
            else if (horizontalHighways.contains(h))
                roadType = HIGHWAY;
            else
                roadType = SMALL_ROAD;

            Road r = new Road("Horizontal " + Integer.toString(h + 1));

            for (Pair<Node,Node> pair : horizontalAvenue) {

                if (roadType == HIGHWAY) {
                    rs = new RoadSegment(r, 70.0 / 3.6, pair.getObject1(), pair.getObject2());
                    rs.addLeftStartLane(0, false);
                    rs.addLeftStartLane(1, false);

                    rs.addLeftEndLane(10, false);
                    rs.addLeftEndLane(11, false);

                } else if (roadType == MAIN_ROAD) {
                    rs = new RoadSegment(r, 50.0 / 3.6, pair.getObject1(), pair.getObject2());
                    rs.addLeftStartLane(0, false);
                    rs.addLeftStartLane(1, false);

                    rs.addLeftEndLane(10, false);
                    rs.addLeftEndLane(11, false);
                } else if (roadType == SMALL_ROAD) {
                    rs = new RoadSegment(r, 30.0 / 3.6, pair.getObject1(), pair.getObject2());
                    rs.addLeftStartLane(0, false);
                    rs.addLeftEndLane(10, false);
                }

                r.addLast(rs);
            }

            roads.add(r);
            h++;
        }
    }

    private void generateRoadPairs() {
        verticalAvenues = new List[width+1];
        horizontalAvenues = new List[height+1];

        List<Pair<Node,Node>> roadList;
        int x,y;

        for (x = 0; x <= width; x++) {

            roadList = new ArrayList<Pair<Node,Node>>();

            verticalAvenues[x] = roadList;

            for (y = 0; y < height; y++)
                roadList.add(new Pair<Node,Node>(grid[x][y],grid[x][y+1]));
            
        }

        for (y = 0; y <= height; y++) {

            roadList = new ArrayList<Pair<Node,Node>>();

            horizontalAvenues[y] = roadList;

            for (x = 0; x < width; x++)
                roadList.add(new Pair<Node,Node>(grid[x][y],grid[x+1][y]));

        }
    }




}
