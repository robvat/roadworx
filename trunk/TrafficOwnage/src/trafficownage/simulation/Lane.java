/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.ArrayDeque;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Lane {
    private List<Car> cars;
    private double length;
    private double max_velocity;
    private double position_coefficient;

    private static double VELOCITY_THRESHOLD = 0.5;
    private static double DISTANCE_THRESHOLD = 0.1;

    private TrafficQueue queue;

    public Lane(double length, double max_velocity, double position_coefficient) {
        this.length = length;
        this.cars = new ArrayList<Car>();
        this.max_velocity = max_velocity;
        this.position_coefficient = position_coefficient;


        if (position_coefficient < 0)
            queue = new TrafficQueue(0.0);
        else
            queue = new TrafficQueue(length);
    }

    public void addCar(Car car) {
        cars.add(cars.size(),car);
        car.setLane(this);
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


    public class TrafficQueue {

        private double default_queue_end;
        private ArrayDeque<Car> queue;
        private Car last_car = null;

        public TrafficQueue(double default_queue_end) {
            this.default_queue_end = default_queue_end;
            this.queue = new ArrayDeque<Car>();
        }

        public void addCar(Car c) {
            queue.addLast(c);
            last_car = c;
        }

        public ArrayDeque<Car> getQueue() {
            return queue;
        }

        public double getQueueEnd() {
            if (last_car == null)
                return default_queue_end;
            else
                return last_car.getBack();
        }
    }


    private double distance_to_leader;

    public void update(double timestep) {
        boolean leader = true;
        
        Car previous = null;

        Car car;

        for (int i = 0; i < cars.size(); i++) {
            car = cars.get(i);

            //the car is not in queue, check if it is the leader
            if (leader) {
                distance_to_leader = Math.abs(queue.getQueueEnd() - car.getPosition());
                car.update(timestep, 0.0, distance_to_leader);

                if (distance_to_leader < (DISTANCE_THRESHOLD + car.getDriverModel().getMinimumDistanceToLeader())) {

                    cars.remove(i);
                    i--;

                    queue.addCar(car);
                    car.setInQueue(true);
                } else {
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
