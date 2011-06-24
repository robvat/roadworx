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
public class Container {

    public Car car;
    public Container next;
    public Container previous;

    private int index;

    public Container(Car car) {
        this.car = car;
        
        if (car != null)
            car.setContainer(this);
    }

    public Car getCar() {
        return car;
    }

    public void setData(Car car) {
        this.car = car;
        car.setContainer(this);
    }

    public void setNext(Container next) {
        this.next = next;
    }
    
    public void setNext(Car next) {
        this.next = next.getContainer();
    }

    public void setPrevious(Car previous) {
        this.previous = previous.getContainer();
    }

    public void setPrevious(Container previous) {
        this.previous = previous;
    }


    public Container getNext() {
        return next;
    }

    public Container getPrevious() {
        return previous;
    }
}
