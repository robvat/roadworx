/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.HashMap;

/**
 *
 * @author Stefan
 */
public class NormalJunction extends Node {

    private static final double INTERNAL_LANE_LENGTH = 5.0; //5 metres seems reasonable as the diameter of a small intesection. TODO: Investigate this.
    private static final double INTERNAL_LANE_SPEED = 8.4;
    private static final double INTERNAL_TRAVEL_ESTIMATE = INTERNAL_LANE_LENGTH / INTERNAL_LANE_SPEED;

    public NormalJunction(Point2D.Double location){
        super(location);
    }

    @Override
    public boolean drivethrough(Car incoming) {
        // TODO: first check if the junction is clear
        // if so return false, else continue

        Lane incomingLane = incoming.getLane();


        

        return true; // just to get rid of the missing return statement error
    }

    @Override
    void acceptCar(Car incoming) {

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
    void update(double timestep) {
        Car c;

        HashMap<Lane,Double> arrival_times = new HashMap<Lane,Double>();

        for (Lane l : getIncomingLanes()) {
            //TODO: here we have to determine who will be first
            c = l.getFirstCar();

            if (c != null) {

                if (c.isInQueue())
                    arrival_times.put(l, 0.0);
                else
                    arrival_times.put(l, c.getDistanceToLaneEnd() / c.getVelocity());
            }
        }
    }

}
