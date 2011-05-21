/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.ArrayDeque;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Lane {
    private Node source_node, destination_node;
    private LinkedList<Car> cars;
    private double length;
    private double max_velocity;
    private double position_coefficient;

    private double end_position, start_position;

    private static double DISTANCE_THRESHOLD = 0.2;

    private TrafficQueue queue;

    public Lane(Node source_node, Node destination_node, double length, double max_velocity, double position_coefficient) {
        this.source_node = source_node;
        this.destination_node = destination_node;
        this.length = length;
        this.cars = new LinkedList<Car>();
        this.max_velocity = max_velocity;
        this.position_coefficient = position_coefficient;


        if (position_coefficient > 0){
            end_position = length;
            start_position = 0.0;
        }else{
            end_position = 0.0;
            start_position = length;
        }

        queue = new TrafficQueue(end_position);
    }

    public Car getFirstCar() {
        if (!cars.isEmpty())
            return cars.get(0);
        else
            return null;
    }

    public boolean acceptsCar(Car car) {
        if (
                !cars.isEmpty() &&
                (
                    (position_coefficient * (cars.getLast().getBack() - start_position) < car.getDriverModel().getMinimumDistanceToLeader() + car.getLength())
                )
            )
            return false;
        else
            return true;
    }

    public void addCar(Car car) {
        if (acceptsCar(car)) {
            cars.add(cars.size(),car);
            car.setLane(this);
        } else
            System.err.println("Rejected");
    }

    public void insertCar(Car car) {

        //TODO: THIS DOES NOT WORK PROPERLY YET DUE TO THE NEW LANE IMPLEMENTATION!
        if (car.getPosition() == 0.0) {
            cars.add(cars.size(),car);
            return;
        }

        for (int i = 0; i < cars.size(); i++) {
            if (cars.get(i).getPosition() < car.getPosition()) {
                cars.add(i,car);
                return;
            }
        }
    }

    public List<Car> getCars() {
        return cars;
    }

    public double getPositionCoefficient() {
        return position_coefficient;
    }

    public double getMaximumVelocity() {
        return max_velocity;
    }

    public double getLength() {
        return length;
    }

    public Node getSourceNode() {
        return source_node;
    }

    public Node getDestinationNode() {
        return destination_node;
    }

    public class TrafficQueue {

        private double default_queue_end;
        private LinkedList<Car> queue;
        private Car last_car = null;

        public TrafficQueue(double default_queue_end) {
            this.default_queue_end = default_queue_end;
            this.queue = new LinkedList<Car>();
        }

        public void addCar(Car c) {
            c.putInQueue(true);
            queue.addLast(c);
            last_car = c;
        }

        public LinkedList<Car> getQueue() {
            return queue;
        }

        public double getQueueEnd() {
            if (last_car == null)
                return default_queue_end;
            else
                return last_car.getBack();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    public TrafficQueue getQueue() {
        return queue;
    }


    private final static double FICTIONAL_DISTANCE = 100000.0;
    
    public void update(double timestep) {
        boolean first_car = true;
        Car previous = null;
        Car remove = null;

        for (Car car : cars) {

            if (previous == null)
                first_car = true;

            if (first_car) {
                if (car.isInQueue() || car.getDistanceToLaneEnd() < DISTANCE_THRESHOLD + car.getDriverType().getMinimumDistanceToLeader()) {
                    if (destination_node.drivethrough(car)) { //the car may pass, let it do so
                        remove = car;
                        destination_node.acceptCar(car);
                    } else if (!car.isInQueue()) { //if the car has just arrived here, it needs to added to the queue. otherwise, nothing happens
                        queue.addCar(car);
                    }
                } else if (destination_node.drivethrough(car)) { //if the car is not close enough, but is allowed to drive on.
                    car.update(timestep, max_velocity, FICTIONAL_DISTANCE);
                    /*car.update(timestep, max_velocity, car.getDistanceToLaneEnd() +
                            Math.max(2.0 * car.getVelocity() * car.getDriverType().getDesiredTimeHeadway(),car.getDriverType().getMinimumDistanceToLeader()));*/
                } else { //the car is not close enough, and not allowed to drive on.
                    car.update(timestep, 0.0, car.getDistanceToLaneEnd());
                }
                
                previous = car;

                first_car = false;
                
                continue;
            }

            if (car.isInQueue()) {
                previous = car;
                continue;
            }

            if (previous.isInQueue() && car.getDistanceToQueueEnd() < DISTANCE_THRESHOLD + car.getDriverType().getMinimumDistanceToLeader()) {
                queue.addCar(car);
            } else {
                car.update(timestep, previous.getVelocity(), position_coefficient * (previous.getBack() - car.getPosition()));
            }

            previous = car;

        }

        if (remove != null)
            cars.remove(remove);
    }

}
