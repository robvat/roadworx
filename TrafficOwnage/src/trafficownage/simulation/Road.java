/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Road {
    private HashMap<Node,List<Lane>> laneMap;

    private Node node1, node2;
    
    private List<Lane> lanes, lanes_from_node1, lanes_from_node2;

    private double length;

    private boolean priority;


    public Road(Node node1, Node node2, double length, double max_velocity, int lanes_per_side, boolean oneway) {
        lanes_from_node1 = new ArrayList<Lane>();
        lanes_from_node2 = new ArrayList<Lane>();

        this.length = length;

        this.node1 = node1;
        this.node2 = node2;

        for (int i = 0; i < lanes_per_side; i++) {
            lanes_from_node1.add(new Lane(node1, node2, length,max_velocity,1));
            lanes_from_node2.add(new Lane(node2, node1, length,max_velocity,-1));
        }

        lanes = new ArrayList<Lane>();
        lanes.addAll(lanes_from_node1);
        lanes.addAll(lanes_from_node2);

        laneMap = new HashMap<Node,List<Lane>>();

        laneMap.put(node1, lanes_from_node2);
        laneMap.put(node2, lanes_from_node1);

        node1.addDestination(node2, this);
        
        if (!oneway)
            node2.addDestination(node1, this);
        
    }

    public Node getStartNode() {
        return node1;
    }

    public Node getEndNode() {
        return node2;
    }

    public List<Lane> getLanes(Node destination) {
        return laneMap.get(destination);
    }

    public List<Lane> getAllLanes() {
        return lanes;
    }

    public double getLength() {
        return length;
    }

    public boolean hasPriority(){
        return priority;
    }

    public Lane getLeftNeighbour(Lane l) {
        int i = lanes.indexOf(l);

        if (i < 0 || i >= lanes.size() - 1)
            return null;
        else
            return lanes.get(i+1);
    }

    public void update(double timestep) {
        for (Lane l : lanes)
            l.update(timestep);
        
    }

    public void init() {
        
    }

    public String toString() {
        String out = "";
        for (Lane l : lanes)
            out += l.toString() + "\n";

        return out;
    }
}
