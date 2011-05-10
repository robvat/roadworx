/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

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

    public Lane(double length, double max_velocity, double position_coefficient) {
        this.length = length;
        this.cars = new ArrayList<Car>();
        this.max_velocity = max_velocity;
        this.position_coefficient = position_coefficient;
    }

    public void addCar(Car car) {
        cars.add(cars.size(),car);
        car.setLane(this);
    }

    public void insertCar(Car car) {

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

    public void update(double timestep) {
        boolean leader = true;
        
        Car previous = null;

        for (Car car : cars) {
            if (leader) {

                if (position_coefficient < 0)
                    car.update(timestep, 0.0, car.getPosition());
                else
                    car.update(timestep, 0.0, length - car.getPosition());

                previous = car;
                leader = false;
            } else {
                car.update(timestep, previous.getVelocity(), Math.abs(previous.getBack() - car.getPosition()));
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
