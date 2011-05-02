/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Main {
    public static void main(String[] args) {
        Lane l = new Lane(100000.0, 50.0);

        Car c = new Car();
        c.init(CarType.CAR, DriverType.NORMAL);
        l.addCar(c);

        double time_passed = 0.0;
        double TIMESTEP = 0.1;
        for (int i = 0; i < 10000; i++) {
            l.update(TIMESTEP);
            System.out.println(time_passed + ": " + l.toString());
            time_passed += TIMESTEP;
        }

    }
}
