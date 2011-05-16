/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Gerrit
 */
public class Car {
    private CarType car_type;
    private DriverType driver_type;

    private Route route;

    private boolean in_queue = false;

    private DriverModel driver_model;

    private Lane current_lane;
    private double position_coefficient;

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

            in_queue = false;
        }

        private double desired_distance;
        public double update(double velocity_leader, double distance_to_leader) {

            desired_distance = s0 + (velocity * T) + ((velocity * Math.abs(velocity_leader - velocity)) / (2 * Math.sqrt(a*b)));
            
            return a * (1 - Math.pow((velocity/v0),4.0) - (desired_distance / distance_to_leader));

        }

        public void setMaxVelocity(double max_velocity) {
            v0 = max_velocity;
        }

        public double getMinimumDistanceToLeader() {
            return s0;
        }

    }

    /**
     * Initializes the car. Since we will use object pools, this just initializes the object
     * @param carType
     * @param driverType
     */
    public Car() {
        driver_model = new IDM();
    }


    /**
     * Real initialization. Sets the driver and car type, and the lane the car
     * will start driving on.
     * @param max_velocity
     */
    public void init(CarType carType, DriverType driverType) {
        this.car_type = carType;
        this.driver_type = driverType;
        driver_model.init(driverType, carType, max_velocity);
        route = new Route();

    }

    public void switchLane(Lane lane) {
        this.current_lane = lane;
    }

    public void setLane(Lane lane) {
        this.current_lane = lane;
        this.max_velocity = Math.min(Math.min((double) car_type.getMaxV(), (double) driver_type.getMaxVelocity()), lane.getMaximumVelocity());
        this.position_coefficient = lane.getPositionCoefficient();

        if (position_coefficient < 0)
            this.position = lane.getLength();
        else
            this.position = 0.0;

        driver_model.setMaxVelocity(max_velocity);
    }

    public double getDistanceToLaneEnd() {
        if (position_coefficient > 0)
            return current_lane.getLength() - position;
        else
            return position;
    }


    public double getDistanceToQueueEnd() {
        if (position_coefficient > 0)
            return current_lane.getQueue().getQueueEnd() - position;
        else
            return position - current_lane.getQueue().getQueueEnd();
    }

    /**
     * Sets the maximum allowed velocity.
     * @param max_velocity
     */
    public void setMaxVelocity(double max_velocity) {
        this.max_velocity = max_velocity;

        driver_model.setMaxVelocity(max_velocity);
    }

    public DriverModel getDriverModel() {
        return driver_model;
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

    public double getFront() {
        return position;
    }

    public double getBack() {
        return position - (position_coefficient * car_type.getLength());
    }

    public double getLength() {
        return car_type.getLength();
    }

    public Lane getLane(){
        return current_lane;
    }

    public void putInQueue(boolean in_queue) {
        this.in_queue = in_queue;
    }

    public boolean isInQueue() {
        return in_queue;
    }

    private class Route {

        private Node current_node;
        private Node next_node = null;

        private Random randy;

        public Route() {
            randy = new Random();
        }

        private void setCurrentNode() {
            current_node = current_lane.getDestinationNode();
        }

        public Node getCurrent() {
            if (current_node == null)
                setCurrentNode();

            return current_node;
        }

        public Node getNext() {
            if (current_node == null)
                setCurrentNode();

            if (next_node == null)
                next_node = current_node.getDestinationNodes().get(randy.nextInt(current_node.getDestinationNodes().size()));

            return next_node;
        }

        public boolean advance() {
            current_node = next_node;
            next_node = null;
            return true;
        }
    }

    /*private class Route {

        private Node[] route;
        private int current_node_index;
        private Node current_node;

        public Route(Node[] route) {
            this.route = route;
            this.current_node_index = 0;
            current_node = route[current_node_index];
        }

        public Node getCurrent() {
            return current_node;
        }

        public Node getNext() {
            if (current_node_index < route.length - 1)
                return route[current_node_index + 1];
            else
                return null;
        }

        public boolean advance() {
            if (current_node_index < route.length - 1) {
                current_node_index++;
                current_node = route[current_node_index];
                return true;
            } else {
                return false;
            }
        }
    }*/

    /**
     * Updates the car position and velocity
     * @param dT
     * @param velocity_leader
     * @param distance_to_leader
     */
    public void update(double timestep, double velocity_leader, double distance_to_leader) {
        if (in_queue) {
            acceleration = 0.0;
            velocity = 0.0;
        } else {
            acceleration = driver_model.update(velocity_leader, distance_to_leader);
            velocity += acceleration * timestep;
            position += position_coefficient * velocity * timestep;
        }
    }
}
