/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.util;

import trafficownage.simulation.RoadSegment;
import trafficownage.simulation.Road;
import trafficownage.simulation.Node;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import trafficownage.simulation.SpawnNode;
import trafficownage.simulation.StupidTrafficLight;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class BigMapGenerator {

    Random rand;

    private double diameter;
    private double radius;
    private int maxNodeCount;
    private int spawnNodeCount;
    private int maxMainRoads;
    private double spawnNodeInterval;

    private List<Node> ringroad;

    public BigMapGenerator() {
    }

    public void generate(double diameter, int maxNodeCount, int maxMainRoads, int spawnNodeCount, double spawnNodeInterval) {
        rand = new Random();
        generate(rand.nextLong(), diameter, maxNodeCount, maxMainRoads, spawnNodeCount, spawnNodeInterval);
    }

    public void generate(long seed, double diameter, int maxNodeCount, int maxMainRoads, int spawnNodeCount, double spawnNodeInterval) {
        rand = new Random(seed);

        this.diameter = diameter;
        this.radius = diameter / 2.0;
        this.maxNodeCount = maxNodeCount;
        this.maxMainRoads = maxMainRoads;
        this.spawnNodeCount = spawnNodeCount;
        this.spawnNodeInterval = spawnNodeInterval;

        generateNodes();
        generateRoads();
    }
    
    private ArrayList<Node> nodes;
    private ArrayList<Road> roads;

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Road> getRoads() {
        return roads;
    }

    private void generateNodes() {
        nodes = new ArrayList<Node>();

        Node node = null;
        double x, y;

        while (nodes.size() < (maxNodeCount - spawnNodeCount)) {
            x = (rand.nextDouble() * diameter) - radius;
            y = (rand.nextDouble() * diameter) - radius;

            if (getOccupant(x, y, 10.0) == null && distance(0, 0, x, y) < radius) {
                node = new StupidTrafficLight(new Point2D.Double(x, y),20.0);
                nodes.add(node);
            }

        }
    }

    private void generateMainRoad(int n, Node n1, Node n2) {
        int steps = 8;

        double dx = n2.getLocation().x - n1.getLocation().x;
        double dy = n2.getLocation().y - n1.getLocation().y;

        double xStep = dx / steps;
        double yStep = dy / steps;

        double x = n1.getLocation().x,
                y = n1.getLocation().y;

        double xDiff = dx / 10;
        double yDiff = dy / 10;

        Road r = new Road("Mainroad " + Integer.toString(n));

        RoadSegment rs;
        Node previous = n1;
        Node current;

        for (int i = 0; i < steps - 1; i++) {
            x += xStep;
            y += yStep;

            x += (rand.nextDouble() * xDiff) - (xDiff / 2);
            y += (rand.nextDouble() * yDiff) - (yDiff / 2);

            current = getOccupant(x,y, diameter / 25.0);

            if (current == null)
                current = new StupidTrafficLight(new Point2D.Double(x,y), 5.0);
            
            nodes.add(current);
            
            rs = new RoadSegment(r,50.0 / 3.6,previous,current);

            rs.addLeftStartLane(0, false);
            rs.addLeftEndLane(1, false);

            r.addLast(rs);

            previous = current;
        }

        rs = new RoadSegment(r, 50.0 / 3.6, previous, n2);

        rs.addLeftStartLane(0, false);
        rs.addLeftEndLane(1, false);

        r.addLast(rs);

        roads.add(r);
    }

    private void generateMainRoads() {
        int roads = 0;

        while (roads < maxMainRoads) {
            Node n1 = nodes.get(rand.nextInt(nodes.size()));
            Node n2 = nodes.get(rand.nextInt(nodes.size()));

            if (distance(n1,n2) > .5 * diameter && n1 instanceof StupidTrafficLight && n2 instanceof StupidTrafficLight) {
                roads++;
                generateMainRoad(roads, n1,n2);
            }

        }
    }

    private void addSegment(Road r, Node n1, Node n2, double max_velocity, int lanes_per_side) {

        RoadSegment s = new RoadSegment(r, max_velocity, n1, n2);

        for (int i = 0; i < lanes_per_side; i++) {
            s.addRightStartLane(i, false);
            s.addRightEndLane(i, false);
        }

        r.addLast(s);
    }

    private void generateRoads() {

        roads = new ArrayList<Road>();

        generateRingRoad();

        generateSpawnNodes();

        generateMainRoads();

        //generateMainRoads(ring_road, 5);


    }

    private double distanceToZero(Point2D.Double p) {
        return distance(p.x,p.y,0.0,0.0);
    }
    private double distanceToZero(double x, double y) {
        return distance(x,y,0.0,0.0);
    }

    private Node addSpawnRoad(int i, Point2D.Double spawnLocation, Node originalNode) {
        Node spawnNode = new SpawnNode(spawnLocation, spawnNodeInterval);
        nodes.add(spawnNode);

        Road r = new Road("Spawnroad " + Integer.toString(i));
        RoadSegment rs = new RoadSegment(r, 50.0 / 3.6, spawnNode,originalNode);
        rs.addLeftStartLane(0, false);
        r.addLast(rs);

        roads.add(r);

        return spawnNode;
    }

    private void generateSpawnNodes() {
        int max = Math.min(nodes.size(),spawnNodeCount);

        int i = 0;

        double x,y;

        List<Node> used = new ArrayList<Node>();
        List<Node> spawnNodes = new ArrayList<Node>();

        double range = diameter / 3.0;
        double diff = range / 2.0;

        Node n, spawnNode;

        while (i < max) {
            n = nodes.get(rand.nextInt(nodes.size()));

            if (!used.contains(n) && !spawnNodes.contains(n)) {
                x = n.getLocation().x + (rand.nextDouble() * range) - diff;
                y = n.getLocation().y + (rand.nextDouble() * range) - diff;

                if (distanceToZero(x, y) > distanceToZero(n.getLocation())) {
                    used.add(n);
                    spawnNode = addSpawnRoad(i,new Point2D.Double(x,y), n);
                    spawnNodes.add(spawnNode);
                    i++;
                }
            }
        }
    }

    private void generateRingRoad() {

        ArrayList<Node> hull = (new QuickHull()).quickHull(nodes);

        Node prev = null;
        Node first = null;

        Road ringroad = new Road("SURROUNDING HIGHWAY");

        for (Node n : hull) {
            if (first == null) {
                prev = n;
                first = n;
            } else {
                addSegment(ringroad, prev, n, 120.0 / 3.6, 2);
                prev = n;
            }
        }
        addSegment(ringroad, prev, first, 120.0 / 3.6, 2);

        nodes = hull;

        this.ringroad = nodes;

        roads.add(ringroad);
    }

    private static double distance(Node n1, Node n2) {
        return distance(n1.getLocation().x, n1.getLocation().y, n2.getLocation().x, n2.getLocation().y);
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
    }

    private Node getOccupant(double x, double y, double distance_threshold) {

        Node occupant = null;

        for (Node n : nodes) {
            if (distance(n.getLocation().x, n.getLocation().y, x, y) <= distance_threshold) {
                occupant = n;
            }
        }


        return occupant;
    }
}
