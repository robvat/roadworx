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
public class CarList {

    public Container header,tail;

    public int size;

    public CarList() {
        header = new Container(null);
        tail = new Container(null);
        header.setNext(tail);
        tail.setPrevious(header);
        size = 0;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    public int size() {
        return size;
    }

    public void add(Car car) {
        addLast(car);
    }

    public void addFirst(Car car) {
        Container newContainer = new Container(car);
        
        newContainer.setPrevious(header);
        newContainer.setNext(header.getNext());

        header.getNext().setPrevious(newContainer);

        header.setNext(newContainer);

        size++;
    }

    public void addLast(Car car) {
        Container newContainer = new Container(car);

        newContainer.setNext(tail);
        newContainer.setPrevious(tail.getPrevious());

        tail.getPrevious().setNext(newContainer);

        tail.setPrevious(newContainer);

        size++;
    }

    public void insertAfter(Car oldCar, Car newCar) {
        Container newContainer = new Container(newCar);

        Container oldContainer = oldCar.getContainer();

        newContainer.setPrevious(oldContainer);
        newContainer.setNext(oldContainer.getNext());

        oldContainer.getNext().setPrevious(newContainer);
        
        oldContainer.setNext(newContainer);

        size++;
    }

    public void insertBefore(Car oldCar, Car newCar) {
        Container newContainer = new Container(newCar);

        Container oldContainer = oldCar.getContainer();

        newContainer.setNext(oldContainer);
        newContainer.setPrevious(oldContainer.getPrevious());

        oldContainer.getPrevious().setNext(newContainer);
        
        oldContainer.setPrevious(newContainer);

        size++;
    }

    public void remove(Car car) {
        Container container = car.getContainer();

        container.getPrevious().setNext(container.getNext());
        container.getNext().setPrevious(container.getPrevious());
        
        container.setNext((Container)null);
        container.setPrevious((Container)null);

        size--;
    }

    public void clear() {
        while (size > 0)
            remove(getFirst());
        
    }

    public Car getFirst() {
        return header.getNext().getCar();
    }

    public Car getLast() {
        return tail.getPrevious().getCar();
    }

    @Override
    public String toString() {
        String output = "";

        Container cont = header.getNext();
        output = cont.getCar().toString();

        while (cont != tail) {
            cont = cont.getNext();
            output += "," + cont.getCar().toString();
        }

        return output;
    }
}
