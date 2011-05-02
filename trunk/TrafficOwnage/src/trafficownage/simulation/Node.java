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
    private HashMap<Node,Road> destination_roads;

    public Node(Point2D.Double location) {
        this.location = location;
        destination_roads = new HashMap<Node,Road>();
    }

    public void addDestination(Node n, Road r) {
        destination_roads.put(n,r);
    }

    public Road getRoad(Node destination) {
        return destination_roads.get(destination);
    }

    public Point2D.Double getLocation() {
        return location;
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
}
