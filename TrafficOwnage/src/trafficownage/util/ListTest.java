/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.util;

import trafficownage.simulation.Car;

/**
 *
 * @author Gerrit
 */
public class ListTest {
    public static void main(String args[]) {
        Car[] cars = new Car[] {
            new Car(),
            new Car(),
            new Car(),
            new Car(),
            new Car()
        };


        Car extra = new Car();

        CarList carList = new CarList();

        carList.add(cars[0]);
        carList.add(cars[1]);
        carList.add(cars[4]);

        carList.insertAfter(cars[1],cars[3]);
        carList.insertBefore(cars[3],cars[2]);

        carList.remove(extra);

        Car car = carList.getFirst();
        
        int i = 0;
        while (car != null) {
            if (!(car == cars[i])) {
                System.out.println("NOPE!");
                return;
            }
            car = car.getCarBehind();
            i++;
        }

        System.out.println("YES!");
    }
}
