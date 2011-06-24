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
    private RoadSegment roadSegment;
    private Lane rightNeighbour;
    private Lane leftNeighbour;
    private Node startNode;
    private Node endNode;
    private double maxSpeed;
    private List<Node> allowedDirections;

    private double queueLength;
    private int queueCount;

    private LinkedList<Car> cars;

    public Lane(int laneId, RoadSegment roadSegment, Node startNode, Node endNode, List<Node> allowedDirections, double maxSpeed) {
        this.laneId = laneId;
        this.roadSegment = roadSegment;
        this.startNode = startNode;
        this.endNode = endNode;
        this.maxSpeed = maxSpeed;
        this.allowedDirections = allowedDirections;

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

        if (car.getCurrentLane() != null)
            car.getCurrentLane().removeCar(car);
        
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

        if (car.getCurrentLane() != null)
            car.getCurrentLane().removeCar(car);

        int index = 0;

        if (carF != null && carB != null) {
            index = cars.indexOf(carF) + 1;
            cars.add(index,car);
            car.setCarBehind(carB);
            car.setCarInFront(carF);
            carF.setCarBehind(car);
            carB.setCarInFront(car);
        } else if (carF == null && carB == null) {
            cars.addLast(car);
            firstCar = car;
            lastCar = car;
            car.setCarInFront(null);
            car.setCarBehind(null);
        } else { // thus carF == null and carB != null because carB can never be null without carF being null;
            cars.addLast(car);
            lastCar.setCarBehind(car);
            car.setCarInFront(lastCar);
            lastCar = car;
        }
        
        car.switchLane(this);
    }

    public double getQueueLength() {
        return queueLength;
    }

    public int getQueueCount() {
        return queueCount;
    }

    public boolean hasCars() {
        return !cars.isEmpty();
    }

    public List<Car> getCars() {
        return cars;
    }

    public void update(double timestep) {

        //Car car = firstCar;

        Car car = null;

        int size = cars.size();

        boolean queue = true;

        for (int i = 0; i < size; i++) {
            
            car = cars.get(i);
            car.update(timestep);

            if (queue && car.isInQueue()) {
                queueLength = getLength() - car.getBack();
                queueCount = i;
            } else {
                queue = false;
            }
            
            if (size > cars.size()) {
                size--;
                i--;
            }
        }
        
        /*while (car != null) {
            car.update(timestep);
            car = car.getCarBehind();
        }*/

    }
}
