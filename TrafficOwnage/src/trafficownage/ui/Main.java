/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.ui;

import java.awt.geom.Point2D;
import java.util.Iterator;
import trafficownage.simulation.Car;
import trafficownage.simulation.CarType;
import trafficownage.simulation.DriverType;
import trafficownage.simulation.DummyNode;
import trafficownage.simulation.Node;
import trafficownage.simulation.Road;

/**
 *
 * @author Gerrit
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Node n1 = new DummyNode(new Point2D.Double(0.0,100.0));
        Node n2 = new DummyNode(new Point2D.Double(0.0,300.0));
        Road r = new Road(n1,n2, n1.distanceTo(n2),13.9,2,false);

        r.getLanes(n2).get(0).addCar(createCar(DriverType.AGRESSIVE, CarType.CAR));
        r.getLanes(n2).get(1).addCar(createCar(DriverType.AGRESSIVE, CarType.CAR));
        r.getLanes(n1).get(0).addCar(createCar(DriverType.AGRESSIVE, CarType.CAR));
        r.getLanes(n1).get(1).addCar(createCar(DriverType.AGRESSIVE, CarType.CAR));

        for (int i = 0; i < 1000; i++) {
            r.update(0.05);
            System.out.println(r.toString());
        }


    }

    public static Car createCar(DriverType d, CarType c) {
        Car car = new Car();
        car.init(c, d);
        return car;

    }





}
