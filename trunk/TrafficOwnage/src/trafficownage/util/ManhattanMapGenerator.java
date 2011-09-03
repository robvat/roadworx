/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.util;

import java.awt.Rectangle;
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
    
    private List<Road> highways;

    private List<Integer> verticalMainRoads;
    private List<Integer> horizontalMainRoads;
    private List<Integer> verticalHighways;
    private List<Integer> horizontalHighways;

    private double[] highwayMaxVelocity;
    private double[] mainRoadMaxVelocity;
    private double[] smallRoadMaxVelocity;
    
    private HashMap<Integer,List<Node>> areas = new HashMap<Integer, List<Node>>();

    public static final int ALL_NODES = -1;
    public static final int LOCAL_NODES = 0;
    public static final int SPAWN_NODES = 1;

    private Random rand;

    public ManhattanMapGenerator() {
    }

    public void generate(int width, int height, double blockSize, Integer[] verticalMainRoads, Integer[] horizontalMainRoads, Integer[] verticalHighways, Integer[] horizontalHighways, double[] highwayMaxVelocity, double[] mainRoadMaxVelocity, double[] smallRoadMaxVelocity) {
        rand = new Random();
        generate(rand.nextLong(),width,height,blockSize,verticalMainRoads,horizontalMainRoads,verticalHighways,horizontalHighways, highwayMaxVelocity, mainRoadMaxVelocity, smallRoadMaxVelocity);
    }

    public void generate(long seed, int width, int height, double blockSize, Integer[] verticalMainRoads, Integer[] horizontalMainRoads, Integer[] verticalHighways, Integer[] horizontalHighways, double[] highwayMaxVelocity, double[] mainRoadMaxVelocity, double[] smallRoadMaxVelocity) {
        this.seed = seed;
        this.width = width;
        this.height = height;
        this.blockSize = blockSize;
        
        this.verticalMainRoads = Arrays.asList(verticalMainRoads);
        this.horizontalMainRoads = Arrays.asList(horizontalMainRoads);

        this.verticalHighways = Arrays.asList(verticalHighways);
        this.horizontalHighways = Arrays.asList(horizontalHighways);

        this.highwayMaxVelocity = highwayMaxVelocity;
        this.mainRoadMaxVelocity = mainRoadMaxVelocity;
        this.smallRoadMaxVelocity = smallRoadMaxVelocity;

        rand = new Random(seed);

        areas = new HashMap<Integer,List<Node>>();
        nodes = new ArrayList<Node>();
        localNodes = new ArrayList<Node>();
        spawnNodes = new ArrayList<Node>();
        highways = new ArrayList<Road>();
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

    
    
    public int requestArea(Rectangle[] rectangles) {

        List<Node> nodes = new ArrayList<Node>();
        
        int x,y;
        for (Rectangle r : rectangles)
            for (x = r.x; x <= r.x + r.width; x++)
                for (y = r.y; y <= r.y + r.height; y++)
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

                if ((horizontalHighways.contains(y) && !verticalHighways.contains(x) && !verticalMainRoads.contains(x)) || (!horizontalHighways.contains(y) && !horizontalMainRoads.contains(y) && verticalHighways.contains(x)))
                    n = new DrivethroughNode(new Point2D.Double(x_loc,y_loc));
                else
                    n = new TrafficLight(new Point2D.Double(x_loc,y_loc));
                
                grid[x][y] = n;
                nodes.add(n);

                if (!horizontalHighways.contains(x) && !horizontalMainRoads.contains(x) && !verticalHighways.contains(x) && !verticalMainRoads.contains(y) )
                    localNodes.add(n);

                y_loc += blockSize;
            }
            
            x_loc += blockSize;
        }


    }

    
    public List<Road> getHighways() {
        return highways;
    }


    private static final int 
            ROADTYPE_SMALLROAD = 0,
            ROADTYPE_MAINROAD = 1,
            ROADTYPE_HIGHWAY = 2;
    
    
    private static final int
            NO_MAINROAD_INTERSECTION = 0,
            APPROACHING_MAINROAD_VERTICAL = 1,
            APPROACHING_MAINROAD_HORIZONTAL = 2,
            LEAVING_MAINROAD_VERTICAL = 3,
            LEAVING_MAINROAD_HORIZONTAL = 4;
    
    private RoadSegment createRoadSegment(Road r, int lanesPerSide, double[] velocity, Node n1, Node n2) {
        RoadSegment rs = new RoadSegment(r, velocity, n1, n2);
        
        for (int i = 0; i < lanesPerSide; i++) {
            rs.addLeftStartLane(i, false);
            rs.addLeftEndLane(lanesPerSide + i, false);
        }  

        return rs;
    }
    
    private RoadSegment createRoadSegment(Road r, int lanesPerSide, double[] velocity, int x1, int y1, int x2, int y2, int mainRoadIntersectionType) {
        Node n1 = grid[x1][y1];
        Node n2 = grid[x2][y2];
        
        RoadSegment rs = new RoadSegment(r, velocity, n1, n2);
        
        if (mainRoadIntersectionType == APPROACHING_MAINROAD_VERTICAL) {            
            
            for (int i = 0; i < lanesPerSide; i++) {
                
                if (i == 0) {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x1][y2+1]);
                    nodes.add(grid[x1-1][y2]);
                    rs.addLeftStartLane(i, nodes, false);
                } else if (i == lanesPerSide - 1) {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x1][y2+1]);
                    nodes.add(grid[x1+1][y2]);
                    rs.addLeftStartLane(i, nodes, false);
                } else {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x1][y2+1]);
                    rs.addLeftStartLane(i, nodes, false);
                }
                
                rs.addLeftEndLane(lanesPerSide + i, false);
            }
            
        } else if (mainRoadIntersectionType == APPROACHING_MAINROAD_HORIZONTAL) {            
            
            for (int i = 0; i < lanesPerSide; i++) {
                
                if (i == 0) {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x2+1][y1]);
                    nodes.add(grid[x2][y1+1]);
                    rs.addLeftStartLane(i, nodes, false);
                } else if (i == lanesPerSide - 1) {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x2+1][y1]);
                    nodes.add(grid[x2][y1-1]);
                    rs.addLeftStartLane(i, nodes, false);
                } else {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x2+1][y1]);
                    rs.addLeftStartLane(i, nodes, false);
                }
                
                rs.addLeftEndLane(lanesPerSide + i, false);
            }
            
        } else if (mainRoadIntersectionType == LEAVING_MAINROAD_VERTICAL) {
            
            for (int i = 0; i < lanesPerSide; i++) {
                
                if (i == 0) {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x1][y1-1]);
                    nodes.add(grid[x1+1][y1]);
                    rs.addLeftEndLane(lanesPerSide + i, nodes, false);
                } else if (i == lanesPerSide - 1) {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x1][y1-1]);
                    nodes.add(grid[x1-1][y1]);
                    rs.addLeftEndLane(lanesPerSide + i, nodes, false);
                } else {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x1][y1-1]);
                    rs.addLeftEndLane(lanesPerSide + i, nodes, false);
                }
                
                rs.addLeftStartLane(i, false);
//                rs.addLeftEndLane(lanesPerSide + i, nodes, false);
            }
            
        } else if (mainRoadIntersectionType == LEAVING_MAINROAD_HORIZONTAL) {         
            
            for (int i = 0; i < lanesPerSide; i++) {
                
                if (i == 0) {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x1-1][y1]);
                    nodes.add(grid[x1][y1-1]);
                    rs.addLeftEndLane(lanesPerSide + i, nodes, false);
                } else if (i == lanesPerSide - 1) {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x1-1][y1]);
                    nodes.add(grid[x1][y1+1]);
                    rs.addLeftEndLane(lanesPerSide + i, nodes, false);
                } else {
                    List<Node> nodes = new ArrayList<Node>();                    
                    nodes.add(grid[x1-1][y1]);
                    rs.addLeftEndLane(lanesPerSide + i, nodes, false);
                }
                
                rs.addLeftStartLane(i, false);
            }            
        } else {
            for (int i = 0; i < lanesPerSide; i++) {
                rs.addLeftStartLane(i, false);
                rs.addLeftEndLane(lanesPerSide + i, false);
            }   
        }

        return rs;
    }

    private static final int HIGHWAY_LANES = 2;
    //private static final double[] HIGHWAY_VELOCITY = new double[] {80 / 3.6, 60 / 3.6};

    private static final int MAINROAD_LANES = 2;
    //private static final double[] MAINROAD_VELOCITY = new double[] {50 / 3.6, 40 / 3.6};

    private static final int SMALLROAD_LANES = 1;
    //private static final double[] SMALLROAD_VELOCITY = new double[] {30 / 3.6};

    private Node generateVerticalSpawnNode(int x, int y) {
        double 
                x_loc = 0.0,
                y_loc = 0.0;
        
        Node n;

        if (y == 0) {
            x_loc = grid[x][y].getLocation().getX();
            y_loc = grid[x][y].getLocation().getY() - (blockSize * 8.0);
        } else {//if (y == height) {
            x_loc = grid[x][y].getLocation().getX();
            y_loc = grid[x][y].getLocation().getY() + (blockSize * 8.0);
        }

        n = new SpawnNode(new Point2D.Double(x_loc,y_loc));
        nodes.add(n);
        spawnNodes.add(n);

        return n;
    }

    private Node generateHorizontalSpawnNode(int x, int y) {
        double
                x_loc = 0.0,
                y_loc = 0.0;

        Node n;

        if (x == 0) {
            x_loc = grid[x][y].getLocation().getX() - (blockSize * 8.0);
            y_loc = grid[x][y].getLocation().getY();
        } else {//if (y == height) {
            x_loc = grid[x][y].getLocation().getX() + (blockSize * 8.0);
            y_loc = grid[x][y].getLocation().getY();
        }

        n = new SpawnNode(new Point2D.Double(x_loc,y_loc));
        nodes.add(n);
        spawnNodes.add(n);

        return n;
    }

    private int getVerticalNodeType(int x) {
            if (verticalMainRoads.contains(x))
                return ROADTYPE_MAINROAD;
            else if (verticalHighways.contains(x))
                return ROADTYPE_HIGHWAY;
            else
                return ROADTYPE_SMALLROAD;
    }

    private int getHorizontalNodeType(int y) {
            if (horizontalMainRoads.contains(y))
                return ROADTYPE_MAINROAD;
            else if (horizontalHighways.contains(y))
                return ROADTYPE_HIGHWAY;
            else
                return ROADTYPE_SMALLROAD;
    }
    
    private boolean isMainRoadIntersection(int x, int y) {
        int xType = getVerticalNodeType(x);
        int yType = getHorizontalNodeType(y);
        
        boolean xMainRoad = (xType == ROADTYPE_HIGHWAY || xType == ROADTYPE_MAINROAD);
        boolean yMainRoad = (yType == ROADTYPE_HIGHWAY || yType == ROADTYPE_MAINROAD);
        
        return (xMainRoad && yMainRoad);        
    }

    private void generateRoads() {
        RoadSegment rs = null;

        int x,y;
        int myNodeType;
        
        boolean approachingMainRoadIntersection;
        boolean leavingMainRoadIntersection;
        
        int intersectionType;

        Node n1,n2;

        int i;

        Road r;

        for (x = 0; x <= width; x++) {

            i = 1;

            myNodeType = getVerticalNodeType(x);

            r = new Road("Vertical " + Integer.toString(x + 1));

            if (myNodeType == ROADTYPE_HIGHWAY)
                r.addFirst(createRoadSegment(r,HIGHWAY_LANES,highwayMaxVelocity,generateVerticalSpawnNode(x,0),grid[x][0]));
                        
            for (y = 0; y < height; y++) {

                n1 = grid[x][y];
                n2 = grid[x][y+1];
                
                approachingMainRoadIntersection = isMainRoadIntersection(x,y+1);
                leavingMainRoadIntersection = isMainRoadIntersection(x,y);

                if (approachingMainRoadIntersection)
                    intersectionType = APPROACHING_MAINROAD_VERTICAL;
                if (leavingMainRoadIntersection)
                    intersectionType = LEAVING_MAINROAD_VERTICAL;
                else
                    intersectionType = NO_MAINROAD_INTERSECTION;

                rs = null;
                
                if (myNodeType == ROADTYPE_HIGHWAY) 
                    rs = createRoadSegment(r,HIGHWAY_LANES,highwayMaxVelocity, x, y, x, y+1, intersectionType);  
                else if (myNodeType == ROADTYPE_MAINROAD)
                    rs = createRoadSegment(r,MAINROAD_LANES,mainRoadMaxVelocity, x, y, x, y+1, intersectionType);
                else if (myNodeType == ROADTYPE_SMALLROAD) {

                    if (getHorizontalNodeType(y) == ROADTYPE_HIGHWAY || getHorizontalNodeType(y+1) == ROADTYPE_HIGHWAY) {
                        i++;
                        if (r.getFirstSegment() != null)
                            roads.add(r);

                        r = new Road("Vertical " + Integer.toString(x + 1) + " part " + i);
                    } else {
                        rs = createRoadSegment(r,SMALLROAD_LANES,smallRoadMaxVelocity, n1, n2);
                    }
                }
                
                if (rs != null)
                    r.addLast(rs);
            }

            if (myNodeType == ROADTYPE_HIGHWAY) {
                r.addLast(createRoadSegment(r, HIGHWAY_LANES, highwayMaxVelocity, grid[x][height], generateVerticalSpawnNode(x,height)));
                highways.add(r);
            }
            
            if (r.getFirstSegment() != null)
                roads.add(r);
            
        }
        
        for (y = 0; y <= height; y++) {

            i = 1;

            myNodeType = getHorizontalNodeType(y);

            r = new Road("Horizontal " + Integer.toString(y + 1));

            if (myNodeType == ROADTYPE_HIGHWAY)
                r.addFirst(createRoadSegment(r, HIGHWAY_LANES, highwayMaxVelocity, generateHorizontalSpawnNode(0,y), grid[0][y]));

            for (x = 0; x < width; x++) {

                n1 = grid[x][y];
                n2 = grid[x+1][y];
                
                approachingMainRoadIntersection = isMainRoadIntersection(x,y+1);
                leavingMainRoadIntersection = isMainRoadIntersection(x,y);
                
                if (approachingMainRoadIntersection)
                    intersectionType = APPROACHING_MAINROAD_HORIZONTAL;
                if (leavingMainRoadIntersection)
                    intersectionType = LEAVING_MAINROAD_HORIZONTAL;
                else
                    intersectionType = NO_MAINROAD_INTERSECTION;

                rs = null;

                if (myNodeType == ROADTYPE_HIGHWAY) 
                    rs = createRoadSegment(r,HIGHWAY_LANES,highwayMaxVelocity, x, y, x+1, y, intersectionType);
                else if (myNodeType == ROADTYPE_MAINROAD)
                    rs = createRoadSegment(r,MAINROAD_LANES,mainRoadMaxVelocity, x, y, x+1, y, intersectionType);
                else if (myNodeType == ROADTYPE_SMALLROAD) {
                    if (getVerticalNodeType(x) == ROADTYPE_HIGHWAY || getVerticalNodeType(x+1) == ROADTYPE_HIGHWAY) {
                        i++;

                        if (r.getFirstSegment() != null)
                            roads.add(r);

                        r = new Road("Horizontal " + Integer.toString(y + 1) + " part " + i);
                    } else {
                        rs = createRoadSegment(r,SMALLROAD_LANES,smallRoadMaxVelocity,n1,n2);
                    }
                }

                if (rs != null)
                    r.addLast(rs);
            }

            if (myNodeType == ROADTYPE_HIGHWAY) {
                r.addLast(createRoadSegment(r, HIGHWAY_LANES, highwayMaxVelocity, grid[width][y], generateHorizontalSpawnNode(width,y)));
                highways.add(r);
            }

            if (r.getFirstSegment() != null)
                roads.add(r);
        }
    }





}
