/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.Random;

/**
 *
 * @author Gerrit Drost
 */
public enum DriverType {

    NORMAL(
            (100.0/3.6)/12,
            2.5,
            130.0/3.6,
            1.0, 2.0
            ),

    AGRESSIVE(
            (100.0/3.6)/4,
            3.5, 400.0/3.6,
            0.2,
            0.5
            ),

    GRANDPA(
            (100.0/3.6)/20,
            1.0,
            80.0/3.6,
            3.0,
            3.0
            );

    private double max_acceleration,
            max_comfortable_deceleration,
            max_velocity,
            minimum_distance_to_leader,
            desired_time_headway;

    DriverType(
            double max_acceleration,
            double max_comfortable_deceleration,
            double max_velocity,
            double minimum_distance_to_leader,
            double desired_time_headway) {

            this.max_acceleration = max_acceleration;
            this.max_comfortable_deceleration = max_comfortable_deceleration;
            this.max_velocity = max_velocity;
            this.minimum_distance_to_leader = minimum_distance_to_leader;
            this.desired_time_headway = desired_time_headway;
    }

    private static Random rand = new Random();

    public static DriverType getRandomDriverType() {
        // normal drivers are 90%, the other two are 10%, divided: 6% aggressive, 4% grandpa.
        double rn = Math.random();
        if (rn >= 0 & rn < 0.9)
            return DriverType.NORMAL;
        else if (rn >=0.9 & rn < 0.96)
            return DriverType.AGRESSIVE;
        else
            return DriverType.GRANDPA;
    }

    /**
     * @return the max_acceleration
     */
    public double getMaxAcceleration() {
        return max_acceleration;
    }

    /**
     * @return the max_comfortable_deceleration
     */
    public double getMaxComfortableDeceleration() {
        return max_comfortable_deceleration;
    }

    /**
     * @return the max_velocity
     */
    public double getMaxVelocity() {
        return max_velocity;
    }

    /**
     * @return the minimum_distance_to_leader
     */
    public double getMinimumDistanceToLeader() {
        return minimum_distance_to_leader;
    }

    /**
     * @return the desired_time_headway
     */
    public double getDesiredTimeHeadway() {
        return desired_time_headway;
    }
    
}
