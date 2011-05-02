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
        double LANE_LENGTH = 10000.0;

        Lane l = new Lane(LANE_LENGTH, 50.0);

        Car c = new Car();
        c.init(CarType.CAR, DriverType.NORMAL);
        l.addCar(c);

        double time_passed = 0.0;
        double TIMESTEP = 0.1;

        //let them drive for 5 minutes
        while (time_passed < (60 * 5)) {
            l.update(TIMESTEP);
            System.out.println(l.toString());

            //when the last car is at position 100m, a new car is added
            if (c.getPosition() > 100.0) {
                c = new Car();
                c.init(CarType.CAR, DriverType.NORMAL);
                l.addCar(c);
            }


            time_passed += TIMESTEP;
        }

    }
}
