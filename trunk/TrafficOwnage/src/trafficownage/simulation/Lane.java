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

    private double start_position;

    private static double DISTANCE_THRESHOLD = 0.2;

    private TrafficQueue queue;

    public Lane(Node source_node, Node destination_node, double length, double max_velocity, double position_coefficient) {
        this.source_node = source_node;
        this.destination_node = destination_node;
        this.length = length;
        this.cars = new LinkedList<Car>();
        this.max_velocity = max_velocity;
        this.position_coefficient = position_coefficient;


        if (position_coefficient < 0)
            start_position = 0.0;
        else
            start_position = length;

        queue = new TrafficQueue(start_position);
    }

    public boolean acceptsCar(Car car) {
        if (!cars.isEmpty() && Math.abs(cars.getLast().getBack() - start_position) < car.getDriverModel().getMinimumDistanceToLeader())
            return false;
        else
            return true;
    }

    public void addCar(Car car) {
        if (acceptsCar(car)) {
            cars.add(cars.size(),car);
            car.setLane(this);
        }
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


    private double distance_to_leader;

    public void update(double timestep) {
        boolean leader = true;
        
        Car previous = null;

        for (Car car : cars) {

            if (car.getInQueue())
                continue;

            //the car is not in queue, check if it is the leader
            if (leader) {

                //if the car is able to join the queue, it is not the leader
                if (car.getDistanceToQueueEnd() < (DISTANCE_THRESHOLD + car.getDriverModel().getMinimumDistanceToLeader())) {

                    //add the car to the queue
                    queue.addCar(car);
                    car.setInQueue(true);

                //normally it is the leader
                } else {
                    car.update(timestep, 0.0, car.getDistanceToQueueEnd());
                    
                    leader = false;
                }

                previous = car;
            } else {
                car.update(timestep, previous.getVelocity(), Math.abs(previous.getBack() - car.getPosition()));

                previous = car;
            }

        }
    }

    @Override
    public String toString() {
        String out = "";

        int resolution = 100;

        Car car;

        List<Integer> car_positions = new ArrayList<Integer>();
        int p;

        for (Car c : cars)
        {
            p = (int)Math.floor((c.getPosition() / length) * (double)resolution);
            car_positions.add(p);
        }

        int lowest;
        int current;

        lowest = Integer.MAX_VALUE;
        for (int j = 0; j < car_positions.size(); j++) {
            current = car_positions.get(j);
            if (current < lowest) {
                lowest = current;
            }
        }

        for (int i = 0; i < resolution; i++) {
            if (i == lowest) {
                out += "*";
                lowest = Integer.MAX_VALUE;
                for (int j = 0; j < car_positions.size(); j++) {
                    current = car_positions.get(j);
                    if (current > i && current < lowest) {
                        lowest = current;
                    }
                }
            } else {
                out += "=";
            }
        }

        return out;
    }

}
