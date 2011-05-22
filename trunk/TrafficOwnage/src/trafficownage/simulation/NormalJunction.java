/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import trafficownage.util.Pair;
import trafficownage.util.Triplet;

/**
 *
 * @author Stefan
 */
public class NormalJunction extends Node {

    private static final double INTERNAL_LANE_LENGTH = 5.0; //5 metres seems reasonable as the diameter of a small intesection. TODO: Investigate this.
    private static final double INTERNAL_LANE_SPEED = 8.4;
    private static final double INTERNAL_TRAVEL_ESTIMATE = INTERNAL_LANE_LENGTH / INTERNAL_LANE_SPEED;

    private HashMap<Lane,Boolean> lane_passthrough;

    public NormalJunction(Point2D.Double location){
        super(location);
    }

    @Override
    public boolean drivethrough(Car incoming) {
        // TODO: first check if the junction is clear
        // if so return false, else continue

        Lane incomingLane = incoming.getLane();

        if (lane_passthrough.get(incomingLane))
            return true;
        else
            return false;
    }

    @Override
    void acceptCar(Car incoming) {
        Lane incomingLane = incoming.getLane();

        if (lane_passthrough.get(incomingLane)) {
            //TODO: instead of immediate passthrough, a timer has to be built-in.
            //TODO: advance node should be in the lane class, probably.
            Node n = incoming.getNextNode();
            List<Lane> lanes = getRoad(n).getLanes(n);
            lanes.get(0).addCar(incoming);
            incoming.advanceNode();
        }
    }

    private class ArrivalTime {
        private double arrival_time;
        private double leave_time;

        public ArrivalTime(double arrival_time) {
            this.arrival_time = arrival_time;
            this.leave_time = arrival_time + INTERNAL_TRAVEL_ESTIMATE;
        }

        public boolean intersects(ArrivalTime at) {

            if (leave_time < at.arrival_time || arrival_time > at.leave_time)
                return false;
            else
                return true;

        }
    }

    @Override
    public void init() {
        super.init();

        lane_passthrough = new HashMap<Lane,Boolean>();
    }

    @Override
    void update(double timestep) {
        Car c;

        HashMap<Lane,ArrivalTime> arrival_times = new HashMap<Lane,ArrivalTime>();

        List<Lane> lanes = getIncomingLanes();

        for (Lane l : lanes) {
            //TODO: here we have to determine who will be first
            lane_passthrough.put(l, true);

            c = l.getFirstCar();

            if (c != null) {

                if (c.isInQueue())
                    arrival_times.put(l, new ArrivalTime(0.0));
                else
                    arrival_times.put(l, new ArrivalTime(c.getDistanceToLaneEnd() / c.getVelocity()));
            }
        }
        
        Lane l1,l2;
        int i,j;

        Triplet<Double,Lane,Lane> overlap = null;
        ArrivalTime a1,a2;


        for (i = 0; i < lanes.size() - 1; i++) {

            l1 = getIncomingLanes().get(i);

            if (!arrival_times.containsKey(l1))
                continue;

            a1 = arrival_times.get(l1);

            for (j = i + 1; j < lanes.size(); j++) {

                l2 = getIncomingLanes().get(j);

                if (!arrival_times.containsKey(l2))
                    continue;

                a2 = arrival_times.get(l1);

                if (arrival_times.get(l1).intersects(arrival_times.get(l2))) {
                    //we have an overlap
                    double arrival_time = Math.min(a1.arrival_time,a2.arrival_time);

                    if (overlap == null || overlap.getObject1() > arrival_time) {

                        if (j == i - 1) {
                            //if l2 is right of l1
                            if (true) {
                                //TODO: Change this into the intersection check!!!
                                lane_passthrough.put(l1, false);
                            }
                        } else if (i == j - 1) {
                            //if l1 is right of l2
                            if (true) {
                                //TODO: Change this into the intersection check!!!
                                lane_passthrough.put(l2, false);

                            }
                        }

                    }

                }
            }
        }


    }

}
