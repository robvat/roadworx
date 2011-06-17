/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Lane {
    private int laneId;
    private boolean ending;
    private RoadSegment roadSegment;
    private Lane rightNeighbour;
    private Lane leftNeighbour;
    private Node startNode;
    private Node endNode;
    private double maxSpeed;
    private List<Node> allowedDirections;

    private LinkedList<Car> cars;

    public Lane(int laneId, RoadSegment roadSegment, Node startNode, Node endNode, List<Node> allowedDirections, double maxSpeed, boolean ending) {
        this.laneId = laneId;
        this.roadSegment = roadSegment;
        this.startNode = startNode;
        this.endNode = endNode;
        this.maxSpeed = maxSpeed;
        this.allowedDirections = allowedDirections;
        this.ending = ending;

        this.cars = new LinkedList<Car>();
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
     * @return the ending
     */
    public boolean isEnding() {
        return ending;
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

    private Car firstCar, lastCar;


    public Car getFirstCar() {
        return firstCar;
    }

    public Car getLastCar() {
        return lastCar;
    }

    public boolean acceptsCar(Car car) {
        if (lastCar != null && lastCar.getBack() < car.getLength())
            return false;
        else
            return true;
    }

    /**
     *
     * @param car The car that is added.
     */
    public void addCar(Car car) {
        if (!acceptsCar(car)) {
            return;
        }

        cars.addLast(car);

        if (firstCar == null) {
            firstCar = car;
            lastCar = car;
            car.setCarInFront(null);
            car.setCarBehind(null);
        } else {
            lastCar.setCarBehind(car);
            car.setCarInFront(lastCar);
            lastCar = car;
        }

        car.setLane(this);
    }

    public void removeCar(Car car) {

        cars.remove(car);

        if (car == firstCar) {
            if (car.getCarBehind() != null) {
                car.getCarBehind().setCarInFront(null);
                firstCar = car.getCarBehind();
            } else {
                firstCar = null;
                lastCar = null;
            }
        } else if (car.getCarBehind() != null) {
            car.getCarBehind().setCarInFront(car.getCarInFront());
            car.getCarInFront().setCarBehind(car.getCarBehind());
        }

        car.setCarBehind(null);
        car.setCarInFront(null);
        
    }
    
    public void insertCar(Car car, Car carF, Car carB){
        int index = 0;
        
        //we assume carF has index +1
        if(carF == null){
            firstCar = car;
            index = 0;
        } else
            index = cars.indexOf(carF) +1;

        if(carB == null){
            lastCar = car;
        }

        cars.add(index, car);
        car.setCarBehind(carB);
        car.setCarInFront(carF);
    }

    public List<Car> getCars() {
        return cars;
    }

    public void update(double timestep) {
        Car car = firstCar;

        while (car != null) {
            car.update(timestep);
            car = car.getCarBehind();
        }

    }
}
