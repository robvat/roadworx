package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import trafficownage.util.Pair;
import trafficownage.util.Triplet;
/**
 *
 * @author frans
 */
public class Junction extends Node
{

    private static final double INTERNAL_LANE_LENGTH = 5.0; //5 metres seems reasonable as the diameter of a small intesection. TODO: Investigate this.
    private static final double INTERNAL_LANE_SPEED = 8.4;
    private static final double INTERNAL_TRAVEL_ESTIMATE = INTERNAL_LANE_LENGTH / INTERNAL_LANE_SPEED;

    private boolean priority;

    public Junction(Point2D.Double location)
    {
        super(location);
        priority = false;
    }

    @Override
    public void init(NodeListener listener) {
        super.init(listener);

        // If there is no priority road we can treat this junction as a normal junction
        if(super.getPriorityRoad()!=null)
        {
            priority = true;
        }
    }

    @Override
    boolean drivethrough(Car incoming)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void acceptCar(Car incoming)
    {
        /*
         * We simply put the car on the next road here, if this code is called
         * the Junction must accept the car and get it to the next road
         */
        if (incoming.getNextLane() == null || !incoming.getNextLane().acceptsCarAdd(incoming))
        {
            System.err.println("Car did not check correctly if it could join a lane.");
        }

        incoming.getNextLane().addCar(incoming);
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
}
