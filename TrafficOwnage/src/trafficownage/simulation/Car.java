/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.Random;
import trafficownage.util.Pair;

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
    private Lane currentLane;
    private Node currentNode;
    private int position_coefficient;
    private double max_velocity;
    private double velocity;
    private double acceleration;
    private double position;

    private class IDM implements DriverModel {

        private double a, b, v0, s0, T;

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

            desired_distance = s0 + (velocity * T) + ((velocity * Math.abs(velocity_leader - velocity)) / (2 * Math.sqrt(a * b)));

            return a * (1 - Math.pow((velocity / v0), 4.0) - Math.pow(desired_distance / distance_to_leader, 2.0));

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

    public DriverType getDriverType() {
        return driver_type;
    }

    public void switchLane(Lane lane) {
        this.currentLane = lane;
    }

    public void setLane(Lane lane) {
        this.currentLane = lane;
        this.currentNode = lane.getEndNode();

        route.determineNext();

        this.max_velocity = Math.min(Math.min((double) car_type.getMaxV(), (double) driver_type.getMaxVelocity()), lane.getMaxSpeed());
        
        this.position = car_type.getLength();

        driver_model.setMaxVelocity(max_velocity);
    }

    public double getDistanceToLaneEnd() {
        return currentLane.getLength() - position;
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
        return position - car_type.getLength();
    }

    public double getLength() {
        return car_type.getLength();
    }

    public Lane getLane() {
        return currentLane;
    }

    public void putInQueue(boolean in_queue) {
        this.in_queue = in_queue;
    }

    public boolean isInQueue() {
        return in_queue;
    }

    public Node getNextNode() {
        if (route != null) {
            return route.getNext();
        } else {
            return null;
        }
    }

    public Node getCurrentNode() {
        if (route != null) {
            return route.getCurrent();
        } else {
            return null;
        }
    }

    public void advanceNode() {
        if (route != null) {
            route.advance();
        }
    }

    public Node getPreviousNode() {
        if (route != null) {
            return route.previous_node;
        } else {
            return null;
        }
    }

    public double getPositionCoefficient() {
        return position_coefficient;
    }

    private class Route {

        private Node previous_node;
        private Node current_node;
        private Node next_node = null;
        private Random randy;

        public Route() {
            randy = new Random();
        }

        private void setCurrentNode() {
            previous_node = currentLane.getStartNode();
            current_node = currentLane.getEndNode();
        }

        public Node getCurrent() {
            if (current_node == null) {
                setCurrentNode();
            }

            return current_node;
        }

        public void determineNext() {
            if (current_node == null) {
                setCurrentNode();
            }

            if (next_node == null) {
                while (next_node == null || next_node == previous_node) {
                    next_node = current_node.getDestinationNodes().get(randy.nextInt(current_node.getDestinationNodes().size()));
                }
            }
        }

        public Node getNext() {
            determineNext();

            return next_node;
        }

        public boolean advance() {
            previous_node = current_node;
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
    public void setPosition(double position) {
        this.position = position;
    }

    /**
     * Updates the car position and velocity
     * @param dT
     * @param velocity_leader
     * @param distance_to_leader
     */
    public void update(double timestep) {

        boolean drivethrough = currentNode.drivethrough(this);

        Pair<Double, Car> nextCar = findNextCar();

        if (carInFront == null && getDistanceToLaneEnd() + getLength() < DISTANCE_THRESHOLD) {
            if (drivethrough) {
                in_queue = false;
                currentLane.removeCar(this);
                currentNode.acceptCar(this);
                route.advance();
            } else {
                in_queue = true;
                acceleration = 0.0;
                velocity = 0.0;
            }
        } else if (nextCar == null && drivethrough) {
            in_queue = false;
            follow(timestep, currentLane.getMaxSpeed(), Double.MAX_VALUE);
        } else if (nextCar == null && !drivethrough) {
            in_queue = false;
          follow(timestep,currentLane.getMaxSpeed(), getDistanceToLaneEnd());
        } else if (nextCar != null) {
            in_queue = false;
            follow(timestep, nextCar.getObject2().getVelocity(), nextCar.getObject1());
        }
    }
    private final static double DISTANCE_THRESHOLD = 2.0;

    private Pair<Double, Car> findNextCar() {

        if (carInFront != null)
            return new Pair<Double, Car>(carInFront.getBack() - this.getPosition(),carInFront);

        Lane lane = currentNode.getLaneMapping(currentLane);
        Car car = null;

        double distance = getDistanceToLaneEnd();

        while (lane != null && distance < 500) {
            car = lane.getLastCar();

            if (car != null && (distance + car.getBack()) < 500) {
                return new Pair<Double, Car>(distance + car.getBack(), car);
            }

            distance += lane.getLength();

            if (car == null)
                lane = lane.getEndNode().getLaneMapping(lane);
            else
                lane = null;
        }

        return null;
    }

    private void follow(double timestep, double leaderVelocity, double distanceToLeader) {
        acceleration = driver_model.update(leaderVelocity, distanceToLeader);
        velocity += acceleration * timestep;
        position += velocity * timestep;

    }
    private Car carInFront, carBehind;

    public Car getCarInFront() {
        return carInFront;
    }

    public Car getCarBehind() {
        return carBehind;
    }

    public void setCarInFront(Car nextCar) {
        this.carInFront = nextCar;
    }

    public void setCarBehind(Car previousCar) {
        this.carBehind = previousCar;
    }
}
