/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

/**
 *
 * @author Gerrit
 */
public interface DriverModel {
    void init(DriverType driver, CarType car, double initial_max_velocity);
    void setMaxVelocity(double max_velocity);
    double update(double velocity_leader, double distance_to_leader);
    double getMinimumDistanceToLeader();
}
