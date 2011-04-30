/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

/**
 *
 * @author Gerrit
 */
public class Car {
    private CarType carType;
    private DriverType driverType;

    private DriverModel driverModel;

    private double max_velocity;

    private double velocity;
    private double acceleration;

    private double position;

    private class IDM implements DriverModel {
        private double a,b,v0,s0,T;

        public void init(DriverType driver, CarType car, double initial_max_velocity) {
            a = Math.min(driver.getMaxAcceleration(), car.getMaxAcceleration());
            b = driver.getMaxComfortableDeceleration();
            v0 = initial_max_velocity;
            s0 = driver.getMinimumDistanceToLeader();
            T = driver.getDesiredTimeHeadway();
        }

        private double desired_distance;
        public double update(double velocity_leader, double distance_to_leader) {
            desired_distance = s0 + (velocity * T) + ((velocity * velocity_leader) / (2 * Math.sqrt(a*b)));
            return a * (1 - Math.pow((velocity/v0),4.0) - (desired_distance / distance_to_leader));

        }

        public void setMaxVelocity(double max_velocity) {
            v0 = max_velocity;
        }

    }

    /**
     * Initializes the car. For this we need to know the driver and the car
     * @param carType
     * @param driverType
     */
    public Car(CarType carType, DriverType driverType) {
        this.carType = carType;
        this.driverType = driverType;

        driverModel = new IDM();
    }


    /**
     * Initializes the car. The car needs to know some stuff like the maximum
     * allowed velocity to start driving properly.
     * @param max_velocity
     */
    public void init(double max_velocity) {
        this.max_velocity = max_velocity;
    }


    /**
     * Sets the maximum allowed velocity.
     * @param max_velocity
     */
    public void setMaxVelocity(double max_velocity) {
        this.max_velocity = max_velocity;
    }

    /**
     * @return the velocity
     */
    public double getVelocity() {
        return velocity;
    }

    /**
     * @return the acceleration
     */
    public double getAcceleration() {
        return acceleration;
    }

    /**
     * @return the position
     */
    public double getPosition() {
        return position;
    }

    /**
     * Updates the car position and velocity
     * @param dT
     * @param velocity_leader
     * @param distance_to_leader
     */
    public void update(double timestep, double velocity_leader, double distance_to_leader) {
        position += velocity * timestep;
        velocity += acceleration * timestep;
        acceleration = driverModel.update(velocity_leader, distance_to_leader);

    }
}
