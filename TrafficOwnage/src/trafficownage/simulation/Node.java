/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Gerrit Drpst <gerritdrost@gmail.com>
 */
public abstract class Node
{
    private Point2D.Double location;
    private HashMap<Node,Road> destination_roads;
    private List<Node> destination_nodes;
    private List<Lane> incoming_lanes;

    public Node(Point2D.Double location) {
        this.location = location;
        destination_roads = new HashMap<Node,Road>();
        destination_nodes = new ArrayList<Node>();
        incoming_lanes = new ArrayList<Lane>();
    }

    public void init() {

        sortNodes();

        determineIncomingLanes();
        
    }

    private void determineIncomingLanes() {
        Road r;
        
        for (Node n : getDestinationNodes()) {
            r = this.getRoad(n);

            for (Lane l : r.getLanes(this)) {
                incoming_lanes.add(l);
            }
        }
    }

    public List<Lane> getIncomingLanes() {
        return incoming_lanes;
    }

    public double distanceTo(Node destination) {
        return Math.sqrt(
                Math.pow(location.x - destination.location.x, 2) +
                Math.pow(location.y - destination.location.y, 2)
                );
    }

    public void addDestination(Node n, Road r) {
        destination_nodes.add(n);
        destination_roads.put(n,r);
    }

    public Road getRoad(Node destination) {
        return destination_roads.get(destination);
    }

    public Point2D.Double getLocation() {
        return location;
    }

    public List<Node> getDestinationNodes() {
        return destination_nodes;
    }

    private void sortNodes() {
        double
                x = this.getLocation().x,
                y = this.getLocation().y,
                max_angle,
                angle;

        Node n, max_node = null;
        int i, sorted = 0;

        while (sorted < destination_nodes.size()) {
            max_angle = -Double.MAX_VALUE;
            max_node = null;

            for (i = sorted; i < destination_nodes.size(); i++) {

                n = destination_nodes.get(i);

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
                destination_nodes.remove(max_node);
                destination_nodes.add(0,max_node);
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
