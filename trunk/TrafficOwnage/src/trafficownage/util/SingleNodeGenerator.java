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
import trafficownage.simulation.*;

/**
 *
 * @author Frans van den Heuvel, Gerrit Drost
 */
public class SingleNodeGenerator {

    public SingleNodeGenerator() {

    }

    /**
     * These static ints represent the type of node placed in the middle of the system
     */
    public static final int NODE_NORMAL_JUNCTION = 0;
    public static final int NODE_DYNAMIC_TRAFFICLIGHT = 1;
    public static final int NODE_RANDOM_TRAFFICLIGHT = 2;
    public static final int NODE_ROUNDABOUT = 3;

    private ArrayList<Node> nodes;
    private List<Road> roads;

    private HashMap<Integer,List<Node>> areas;

    /**
     * generates a network for the mainloop
     * @param nodeType the type of node to be placed in the middle. Insert one of the public static final int NODE_* vars here
     * @param roadLengths an array of doubles in metres. Each value represents a road. Thus, 5 entries will cause the network to have a junction with 5 roads connected.
     * @param roadSets an array consisting out of arrays of 2 ints. These 2 ints denote the
     * indices of roads (referring to the index in roadLengths) that belong together in one road.
     * @param maxSpeed the maximum allowed velocity in m/s
     * @param laneCount the number of lanes per side on all roads
     */
    public void generate(int nodeType, double[] roadLengths, int[][] roadSets, double maxSpeed, int laneCount) {

        //this node will be the middle node.
        //we do not add it to the node list yet, because the node list
        //indices play an important role in the algorithm. This node
        //will be added last.
        Node middleNode = null;

        //we use this number quite a lot so lets put it in a var
        int segmentCount = roadLengths.length;

        //define the middle node
        //change the node types here
        switch (nodeType)
        {
            case NODE_NORMAL_JUNCTION:
                //TODO: Warning, not fully functional!
                middleNode = new NormalJunction(new Point2D.Double(0.0,0.0));
                break;
            case NODE_DYNAMIC_TRAFFICLIGHT:
                middleNode = new TrafficLight(new Point2D.Double(0.0,0.0));
                break;
            case NODE_RANDOM_TRAFFICLIGHT:
                // Time between light switches also random!
                middleNode = new RandomTrafficLight(new Point2D.Double(0.0,0.0), RandomTrafficLight.RANDOMINTERVAL);
                break;
            case NODE_ROUNDABOUT:
                // Radius = 10 meters
                middleNode = new Roundabout(new Point2D.Double(0.0,0.0), 10.0);
                break;
        }

        //calculate the angle between the roads
        //eg when there are 5 roads, the angle between them is 360 degrees (2pi) / 5.
        double angle = (2 * Math.PI) / segmentCount;

        //we start at angle 0
        double currentAngle = 0.0;

        //make a point object to store points in
        Point2D.Double next = new Point2D.Double();

        //construct a new arraylist for the nodes
        nodes = new ArrayList<Node>();

        //the area hashmap
        areas = new HashMap<Integer,List<Node>>();


        //the current node
        Node node;

        //loop through all roadlengths
        for (int i = 0; i < segmentCount; i++) {
            //x = roadLength * sin(angle)
            next.x = roadLengths[i] * Math.sin(currentAngle);

            //y = roadLength * sin(angle)
            next.y = roadLengths[i] * Math.cos(currentAngle);

            //construct the node
            node = new SpawnNode((Point2D.Double)next.clone());

            //we found the x and y coordinates. construct the node and add it to the list
            nodes.add(node);

            //add the node to the areas map as a new "area"  consisting out of just one node: itself
            areas.put(i, new ArrayList<Node>(Arrays.asList(new Node[] {node})));

            //increment the angle for the next node
            currentAngle += angle;
        }

        //add all ring nodes as an area as well
        areas.put(nodes.size(), (ArrayList<Node>)nodes.clone());
        
        
        
        //we start constructing roads here, so we better construct the list for it
        roads = new ArrayList<Road>();
        
        //to keep track of which segments have been added to the map.
        boolean[] constructed = new boolean[segmentCount];

        int startRoadSegment, endRoadSegment;
        for (int[] pair : roadSets) {
            //make stuff more readable with named variables
            startRoadSegment = pair[0];
            endRoadSegment = pair[1];

            //set the constructed booleans to true
            constructed[startRoadSegment] = true;
            constructed[endRoadSegment] = true;

            //construct the road object
            Road r = new Road("Road " + Integer.toString(roads.size()));

            //construct the road segments
            RoadSegment rs1 = new RoadSegment(r,new double[] {maxSpeed}, nodes.get(startRoadSegment), middleNode);
            RoadSegment rs2 = new RoadSegment(r,new double[] {maxSpeed}, middleNode, nodes.get(endRoadSegment));

            //populate the road segments with lanes
            populateRoadSegment(rs1,laneCount);
            populateRoadSegment(rs2,laneCount);

            //add the segment to the road
            r.addLast(rs1);
            r.addLast(rs2);

            //add the road to the road list
            roads.add(r);
        }
        
        for (int i = 0; i < segmentCount; i++) {
            
            if (!constructed[i]) {
                //construct the road object
                Road r = new Road("Road " + Integer.toString(roads.size()));

                //construct the road segment
                RoadSegment rs = new RoadSegment(r,new double[] {maxSpeed}, nodes.get(i), middleNode);

                //populate the road segments with lanes
                populateRoadSegment(rs,laneCount);

                //add the segment to the road
                r.addLast(rs);

                //add the road to the road list
                roads.add(r);
            }            
        }
        
        //finally, add the middle node
        nodes.add(middleNode);
    }


    /**
     * Self explanatory
     * @return a hashmap of areas
     */
    public HashMap<Integer,List<Node>> getAreas() {
        return areas;
    }

    /**
     * Self explanatory
     * @return a list of nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Self explanatory
     * @return a list of roads
     */

    public List<Road> getRoads() {
        return roads;
    }

    /**
     * Fills the road segment with the desired number of lanes
     * @param roadSegment the road segment to fill
     * @param laneCount the number of lanes required per side
     */
    private void populateRoadSegment(RoadSegment roadSegment, int laneCount) {
        for (int i = 0; i < laneCount; i++) {
            roadSegment.addLeftStartLane(i, false);
            roadSegment.addLeftEndLane(laneCount + i, false);
        }
    }
}
