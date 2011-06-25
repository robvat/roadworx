/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.List;
import trafficownage.util.CarList;
import trafficownage.util.Triplet;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Lane {

    private int laneId;
    private RoadSegment roadSegment;
    private Lane rightNeighbour;
    private Lane leftNeighbour;
    private Node startNode;
    private Node endNode;
    private double maxSpeed;
    private List<Node> allowedDirections;
    private double queueLength;
    private double carsLength;
    private int queueCount;
    private CarList cars;

    public Lane(int laneId, RoadSegment roadSegment, Node startNode, Node endNode, List<Node> allowedDirections, double maxSpeed) {
        this.laneId = laneId;
        this.roadSegment = roadSegment;
        this.startNode = startNode;
        this.endNode = endNode;
        this.maxSpeed = maxSpeed;
        this.allowedDirections = allowedDirections;

        this.cars = new CarList();
    }

    public void setLeftNeighbour(Lane leftNeighbour) {
        this.leftNeighbour = leftNeighbour;
    }

    public void setRightNeighbour(Lane leftNeighbour) {
        this.leftNeighbour = leftNeighbour;
    }

    /**
     * @return the laneId
     */
    public int getLaneId() {
        return laneId;
    }

    /**
     * @return the roadSegment
     */
    public RoadSegment getRoadSegment() {
        return roadSegment;
    }

    public double getLength() {
        return roadSegment.getLength();
    }

    /**
     * @return the rightNeighbour
     */
    public Lane getRightNeighbour() {
        return rightNeighbour;
    }

    /**
     * @return the leftNeighbour
     */
    public Lane getLeftNeighbour() {
        return leftNeighbour;
    }

    /**
     * @return the startNode
     */
    public Node getStartNode() {
        return startNode;
    }

    /**
     * @return the endNode
     */
    public Node getEndNode() {
        return endNode;
    }

    /**
     * @return the maxSpeed
     */
    public double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * @return the allowedDirections
     */
    public List<Node> getAllowedDirections() {
        return allowedDirections;
    }

    public void setAllowedDirections(List<Node> allowedDirections) {
        this.allowedDirections = allowedDirections;
    }

    public void clear() {
        cars.clear();
    }

    public Car getFirstCar() {
        return cars.getFirst();
    }

    public Car getLastCar() {
        return cars.getLast();
    }

    public boolean acceptsCarAdd(Car car) {

        Car lastCar = getLastCar();

        if (!hasCars() || lastCar.getBack() > 0.0) {
            return true;
        } else {
            return false;
        }
    }

    public Triplet<Boolean, Car, Car> acceptsCarInsert(Car car) {

        Car firstCar = getFirstCar();
        Car lastCar = getLastCar();

        if (lastCar == null && firstCar == null) {
            return new Triplet<Boolean, Car, Car>(true, null, null);
        }

        if (lastCar != null && lastCar.getBack() > car.getFront()) {
            return new Triplet<Boolean, Car, Car>(true, lastCar, null);
        }

        if (firstCar != null && car.getBack() > firstCar.getFront()) {
            return new Triplet<Boolean, Car, Car>(true, null, firstCar);
        }

        Car otherCar = firstCar;

        while (otherCar != null) {
            if (otherCar.getBack() > car.getFront() && otherCar.getCarBehind() != null && car.getBack() > otherCar.getCarBehind().getFront()) //we found our slot
            {
                return new Triplet<Boolean, Car, Car>(true, otherCar, otherCar.getCarBehind());
            }

            otherCar = otherCar.getCarBehind();
        }

        return new Triplet<Boolean, Car, Car>(false, null, null);
    }

    /**
     *
     * @param car The car that is added.
     */
    public void addCar(Car car) {

        carsLength += car.getLength();

        if (car.getCurrentLane() != null) {
            car.getCurrentLane().removeCar(car);
        }

        cars.addLast(car);

        car.setLane(this);
    }

    public void removeCar(Car car) {

        carsLength -= car.getLength();

        cars.remove(car);
    }

    public void insertCar(Car car) {

        carsLength += car.getLength();

        if (car.getCurrentLane() != null) {
            car.getCurrentLane().removeCar(car);
        }

        cars.addLast(car);

        car.switchLane(this);
    }

    public void insertCarAfter(Car carInFront, Car car) {

        carsLength += car.getLength();

        if (car.getCurrentLane() != null) {
            car.getCurrentLane().removeCar(car);
        }

        cars.insertAfter(carInFront, car);

        car.switchLane(this);
    }

    public void insertCarBefore(Car carBehind, Car car) {

        carsLength += car.getLength();

        if (car.getCurrentLane() != null) {
            car.getCurrentLane().removeCar(car);
        }

        cars.insertBefore(carBehind, car);

        car.switchLane(this);
    }

    public double getQueueLength() {
        return queueLength;
    }

    public int getQueueCount() {
        return queueCount;
    }

    public double getCombinedCarLength() {
        return carsLength;
    }

    public boolean hasCars() {
        return !cars.isEmpty();
    }

    public void update(double timestep) {


        Car car = getFirstCar();

        Car nextCar;

        boolean queue = true;


        while (car != null) {
            nextCar = car.getCarBehind();

            car.update(timestep);

            if (car.getCurrentLane() == this && queue && car.isInQueue()) {
                queueLength = getLength() - car.getBack();
                queueCount++;
            }

            car = nextCar;
        }

        //Car car = firstCar;

        //Car car = null;

//        int size = cars.size();
//
//
//        for (int i = 0; i < size; i++) {
//
//            car = cars.get(i);
//            car.update(timestep);
//
//            if (queue && car.isInQueue()) {
//                queueLength = getLength() - car.getBack();
//                queueCount = i;
//            } else {
//                queue = false;
//            }
//
//            if (size > cars.size()) {
//                size--;
//                i--;
//            }
//        }

        /*while (car != null) {
        car.update(timestep);
        car = car.getCarBehind();
        }*/

    }
}
