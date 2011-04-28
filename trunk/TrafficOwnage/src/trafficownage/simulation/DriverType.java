/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

/**
 *
 * @author Gerrit Drost
 */
public enum DriverType implements Updatable {

    NORMAL(2.78f, 2.5f, 44.0f, 1.0f, 2.0f),
    AGRESSIVE(4.0f, 3.5f, 65.0f, 0.2f, 0.5f),
    GRANDPA(1.8f, 1.0f, 25f, 3.0f, 3.0f);

    private float max_acceleration,
            max_comfortable_deceleration,
            max_velocity,
            minimum_distance_to_leader,
            desired_time_headway;

    DriverType(
            float max_acceleration,
            float max_comfortable_deceleration,
            float max_velocity,
            float minimum_distance_to_leader,
            float desired_time_headway) {

            this.max_acceleration = max_acceleration;
            this.max_comfortable_deceleration = max_comfortable_deceleration;
            this.max_velocity = max_velocity;
            this.minimum_distance_to_leader = minimum_distance_to_leader;
            this.desired_time_headway = desired_time_headway;
    }

    @Override
    public void update(int dT) {
    
    }

    /**
     * @return the max_acceleration
     */
    public float getMaxAcceleration() {
        return max_acceleration;
    }

    /**
     * @return the max_comfortable_deceleration
     */
    public float getMaxComfortableDeceleration() {
        return max_comfortable_deceleration;
    }

    /**
     * @return the max_velocity
     */
    public float getMaxVelocity() {
        return max_velocity;
    }

    /**
     * @return the minimum_distance_to_leader
     */
    public float getMinimumDistanceToLeader() {
        return minimum_distance_to_leader;
    }

    /**
     * @return the desired_time_headway
     */
    public float getDesiredTimeHeadway() {
        return desired_time_headway;
    }
    
}
