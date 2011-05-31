/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.util;

import trafficownage.simulation.RoadSegment;
import trafficownage.simulation.Road;
import trafficownage.simulation.DrivethroughNode;
import trafficownage.simulation.Node;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class MapGenerator {

    Random rand;

    public MapGenerator() {
    }

    public void generate(double diameter, int max_nodes) {
        rand = new Random();
        generate(rand.nextLong(), diameter, max_nodes);
    }

    public void generate(long seed, double diameter, int max_nodes) {
        rand = new Random(seed);
        generateNodes(diameter, max_nodes);
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

    private void generateNodes(double diameter, int max_nodes) {
        nodes = new ArrayList<Node>();

        Node node = null;
        double radius = diameter / 2.0;
        double x, y;

        while (nodes.size() < max_nodes) {
            x = (rand.nextDouble() * diameter) - radius;
            y = (rand.nextDouble() * diameter) - radius;

            if (!isOccupied(x, y, 10.0) && distance(0, 0, x, y) < radius) {
                node = new DrivethroughNode(new Point2D.Double(x, y));
                nodes.add(node);
            }

        }
    }

    private void addSegment(Road r, Node n1, Node n2, double max_velocity, int lanes_per_side) {

        RoadSegment s = new RoadSegment(n1,n2);

        for (int i = 0; i < lanes_per_side; i++) {
            s.addRightStartLane(i, max_velocity, false);
            s.addRightEndLane(i, max_velocity, false);
        }

        r.addLast(s);
    }

    private void generateRoads() {

        roads = new ArrayList<Road>();

        generateRingRoad();

        //generateMainRoads(ring_road, 5);


    }

    private List<Node> generateRingRoad() {

        ArrayList<Node> hull = (new QuickHull()).quickHull(nodes);

        Node prev = null;
        Node first = null;

        Road ringroad = new Road("DE SNELWEG!");

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

        roads.add(ringroad);

        return hull;
    }

    private static double distance(Node n1, Node n2) {
        return distance(n1.getLocation().x, n1.getLocation().y, n2.getLocation().x, n2.getLocation().y);
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
    }

    private boolean isOccupied(double x, double y, double distance_threshold) {

        for (Node n : nodes) {
            if (distance(n.getLocation().x, n.getLocation().y, x, y) <= distance_threshold) {
                return true;
            }
        }


        return false;
    }
}
