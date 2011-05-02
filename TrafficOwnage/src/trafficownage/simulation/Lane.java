/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.LinkedList;

/**
 *
 * @author Gerrit
 */
public class Lane {
    private LinkedList<Car> cars;
    private double length;
    private double max_velocity;

    public Lane(double length, double max_velocity) {
        this.length = length;
        this.cars = new LinkedList<Car>();
        this.max_velocity = max_velocity;
    }

    public void addCar(Car car) {
        cars.addLast(car);
        car.setLane(this);
    }

    public void insertCar(Car car) {

        if (car.getPosition() == 0.0) {
            cars.addLast(car);
            return;
        }

        for (int i = 0; i < cars.size(); i++) {
            if (cars.get(i).getPosition() < car.getPosition()) {
                cars.add(i,car);
                return;
            }
        }
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
                car.update(timestep, 0.0, length - car.getPosition());
                previous = car;
                leader = false;
            } else {
                car.update(timestep, previous.getVelocity(), previous.getPosition() - car.getPosition());
            }

        }
    }

    @Override
    public String toString() {
        String out = "";
        for (Car car : cars) {
            out += "[p=" + Double.toString(car.getPosition()) + ",v="+ Double.toString(car.getVelocity()) + "]";
        }
        return out;
    }

}
