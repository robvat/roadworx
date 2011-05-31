/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Gerrit Drpst <gerritdrost@gmail.com>
 */
public abstract class Node
{
    private Point2D.Double location;
    private HashMap<Node,RoadSegment> destinationRoads;
    private List<RoadSegment> roads;
    private List<Node> destinationNodes;
    private List<Lane> incomingLanes;
    private HashMap<Lane,Lane> laneMap;


    public double f,g,h; //pathfinding variables
    public Node parent;

    public Node(Point2D.Double location) {
        this.location = location;
        destinationRoads = new HashMap<Node,RoadSegment>();
        destinationNodes = new ArrayList<Node>();
        incomingLanes = new ArrayList<Lane>();
        roads = new ArrayList<RoadSegment>();

        laneMap = new HashMap<Lane, Lane>();
    }

    public void init() {
        sortNodes();

        determineIncomingLanes();

        determineLaneMapping();
    }

    private void determineLaneMapping() {
        
        RoadSegment segment1;
        RoadSegment segment2;

        for (Node n1 : getDestinationNodes()) {

            segment1 = getRoadSegment(n1);

            for (Node n2 : getDestinationNodes()) {

                if (n1 == n2)
                    continue;

                segment2 = getRoadSegment(n2);

                if (segment1.getNextSegment() == segment2 || segment1.getPreviousSegment() == segment2) {
                    mapLanes(segment1,segment2);
                }
            }
            
        }

        System.out.println(laneMap.size());
    }

    private void mapLanes(RoadSegment rs1, RoadSegment rs2) {

        List<Lane> lanes1,lanes2;

        lanes1 = rs1.getDestinationLanes(this);
        lanes2 = rs2.getSourceLanes(this);

        for (Lane l1 : lanes1)
            for (Lane l2 : lanes2)
                if (l1.getLaneId() == l2.getLaneId())
                    laneMap.put(l1, l2);

        lanes1 = rs2.getDestinationLanes(this);
        lanes2 = rs1.getSourceLanes(this);
        for (Lane l1 : lanes1)
            for (Lane l2 : lanes2)
                if (l1.getLaneId() == l2.getLaneId())
                    laneMap.put(l1, l2);
    }

    private void determineIncomingLanes() {
        RoadSegment r;
        
        for (Node n : getDestinationNodes()) {
            r = this.getRoadSegment(n);

            for (Lane l : r.getDestinationLanes(this)) {
                incomingLanes.add(l);
            }
        }
    }

    public void mapLane(Lane incoming, Lane outgoing) {
        laneMap.put(incoming, outgoing);
    }

    public Lane getLaneMapping(Lane incoming) {
        if (laneMap.containsKey(incoming))
            return laneMap.get(incoming);
        else
            return null;
    }

    public List<Lane> getIncomingLanes() {
        return incomingLanes;
    }

    public double distanceTo(Node destination) {
        return Math.sqrt(
                Math.pow(location.x - destination.location.x, 2) +
                Math.pow(location.y - destination.location.y, 2)
                );
    }

    public void addDestination(Node n, RoadSegment r) {
        roads.add(r);
        destinationNodes.add(n);
        destinationRoads.put(n,r);
    }

    public RoadSegment getRoadSegment(Node destination) {
        return destinationRoads.get(destination);
    }

    public Point2D.Double getLocation() {
        return location;
    }

    public List<Node> getDestinationNodes() {
        return destinationNodes;
    }
    

    private void sortNodes() {
        double
                x = this.getLocation().x,
                y = this.getLocation().y,
                max_angle,
                angle;

        Node n, max_node = null;
        int i, sorted = 0;

        while (sorted < destinationNodes.size()) {
            max_angle = -Double.MAX_VALUE;
            max_node = null;

            for (i = sorted; i < destinationNodes.size(); i++) {

                n = destinationNodes.get(i);

                angle = Math.atan2(
                            (y - n.getLocation().y),
                            (x - n.getLocation().x)
                        );

                if (angle > max_angle) {
                    max_angle = angle;
                    max_node = n;
                }
            }

            if (max_node != null) {
                //move the selected node to position 0.
                destinationNodes.remove(max_node);
                destinationNodes.add(0,max_node);
            }
            sorted++;
        }

    }

    /* incoming cars need to know wether to brake or continue driving */
    abstract boolean drivethrough(Car incoming);
    /* TODO: crossroads without lights need drivers to check so drivers
     need a function for this that will have to be called by that node */

    
    /* once a car is at the border it has to be accepted
     by the new node and leave the old road */
    abstract void acceptCar(Car incoming);

    /* Cars can be on a node for a longer time so nodes need to be
     updated aswell (if a car has to be 40 sec on a node then the node needs to
     know the time */
    abstract void update(double timestep);

    @Override
    public String toString() {
        return location.toString();
    }
}
