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
    public List<Lane> lanes,lanes1,lanes2;
    private boolean priority;

    public Road(Node node1, Node node2, double length, double speed_limit, int lanes_per_side, boolean oneway, boolean priority) {
        this.node1 = node1;
        this.node2 = node2;
        this.priority = priority;

        laneMap = new HashMap<Node,List<Lane>>();

        Lane lane;

        if (oneway) {
            lanes = new ArrayList<Lane>();
            lanes1 = new ArrayList<Lane>();
            
            if(lanes_per_side == 1){
                lane = new Lane(length,speed_limit, Lane.BOTH);
                lanes1.add(lane);
                lanes.add(lane);
            }
            else{
                //first the most left lane
                lane = new Lane(length,speed_limit, Lane.LEFT);
                lanes1.add(lane);
                lanes.add(lane);

                for (int i = 0; i < lanes_per_side -2; i++) {
                    lane = new Lane(length,speed_limit, Lane.BOTH_NOT);
                    lanes1.add(lane);
                    lanes.add(lane);
                }

                //last the most right lane
                lane = new Lane(length,speed_limit, Lane.RIGHT);
                lanes1.add(lane);
                lanes.add(lane);
            }

            laneMap.put(node2, lanes1);
        } else {
            lanes = new ArrayList<Lane>();
            lanes1 = new ArrayList<Lane>();
            lanes2 = new ArrayList<Lane>();

                


            if(lanes_per_side == 1){
                lane = new Lane(length,speed_limit, Lane.BOTH);
                lanes1.add(lane);
                lanes.add(lane);

                lane = new Lane(length,speed_limit, Lane.BOTH);
                lanes2.add(lane);
                lanes.add(lane);
            }
            else{
                //first the most left lane
                lane = new Lane(length,speed_limit, Lane.LEFT);
                lanes1.add(lane);
                lanes.add(lane);

                lane = new Lane(length,speed_limit, Lane.LEFT);
                lanes2.add(lane);
                lanes.add(lane);

                for (int i = 0; i < lanes_per_side -2; i++) {
                    lane = new Lane(length,speed_limit, Lane.BOTH_NOT);
                    lanes1.add(lane);
                    lanes.add(lane);

                    lane = new Lane(length,speed_limit, Lane.BOTH_NOT);
                    lanes2.add(lane);
                    lanes.add(lane);
                }

                //last the most right lane
                lane = new Lane(length,speed_limit, Lane.RIGHT);
                lanes1.add(lane);
                lanes.add(lane);

                lane = new Lane(length,speed_limit, Lane.RIGHT);
                lanes2.add(lane);
                lanes.add(lane);
            }

            laneMap.put(node1, lanes1);
            laneMap.put(node2, lanes2);
        }
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
        //TODO: Small task: write this more efficiently.
        if (lanes1.contains(l) && lanes1.indexOf(l) < lanes1.size() - 1)
            return lanes1.get(lanes1.indexOf(l) + 1);

        if (lanes2.contains(l) && lanes2.indexOf(l) < lanes2.size() - 1)
            return lanes2.get(lanes2.indexOf(l) + 1);

        return null;
    }

    public void update(double timestep) {
        for (Lane l : lanes)
            l.update(timestep);
        
    }
}
