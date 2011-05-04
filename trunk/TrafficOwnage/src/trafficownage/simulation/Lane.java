/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Gerrit
 */
public class Lane {
    private List<Car> cars;
    private double length;
    private double max_velocity;
    private int turnDirections;                 //indicates if you can turn from this road left, right, both or neither.
    public static final int BOTH_NOT = 0;
    public static final int RIGHT = 1;
    public static final int LEFT = 2;
    public static final int BOTH = 3;

    public Lane(double length, double max_velocity, int turnDirections) {
        this.length = length;
        this.cars = new ArrayList<Car>();
        this.max_velocity = max_velocity;
        this.turnDirections = turnDirections;
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

    public double getMaximumVelocity() {
        return max_velocity;
    }

    public double getLength() {
        return length;
    }

    public int getTurnDirections(){
        return turnDirections;
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
                car.update(timestep, previous.getVelocity(), previous.getPosition() - previous.getLength() - car.getPosition());
            }

        }
    }

    @Override
    public String toString() {
        String out = "";

        int resolution = 100;

        int c = 0;

        Car car;

        for (int i = cars.size() - 1; i >= 0; i--) {
            car = cars.get(i);
            
            int p = (int)Math.round((car.getPosition() / length) * (double)resolution);
                    
            if (c == p)
                continue;

            for (int j = c; j < p; j++)
                out += "=";

            out += "*";

            c = p;
        }

        for (int i = c; i < resolution; i++)
                out += "=";

        return out;
    }

}
