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
    public HashMap<Node,List<Lane>> laneMap;

    public Node node1, node2;
    
    public List<Lane> lanes, lanes_from_node1, lanes_from_node2;
    private boolean priority;


    public Road(Node node1, Node node2, double length, double max_velocity, int lanes_per_side, boolean oneway) {
        lanes_from_node1 = new ArrayList<Lane>();
        lanes_from_node2 = new ArrayList<Lane>();

        this.node1 = node1;
        this.node2 = node2;

        for (int i = 0; i < lanes_per_side; i++) {
            lanes_from_node1.add(new Lane(length,max_velocity,1));
            lanes_from_node2.add(new Lane(length,max_velocity,-1));
        }

        lanes = new ArrayList<Lane>();
        lanes.addAll(lanes_from_node1);
        lanes.addAll(lanes_from_node2);

        laneMap = new HashMap<Node,List<Lane>>();

        laneMap.put(node1, lanes_from_node1);
        laneMap.put(node2, lanes_from_node2);
    }
    
    public List<Lane> getLanes(Node destination) {
        return laneMap.get(destination);
    }

    public List<Lane> getAllLanes() {
        return lanes;
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

    public String toString() {
        String out = "";
        for (Lane l : lanes)
            out += l.toString() + "\n";

        return out;
    }
}
