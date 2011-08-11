/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.List;
import trafficownage.util.Averager;
import trafficownage.util.CarList;
import trafficownage.util.Co2Calculator;
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
    private List<Node> allowedDirections;
    private double queueLength;
    private double carsLength;
    private double maxVelocity;


    private int queueCount;
    
    private CarList cars;

    public Lane(int laneId, RoadSegment roadSegment, Node startNode, Node endNode, List<Node> allowedDirections, double maxSpeed) {
        this.laneId = laneId;
        this.roadSegment = roadSegment;
        this.startNode = startNode;
        this.endNode = endNode;
        this.allowedDirections = allowedDirections;

        this.maxVelocity = roadSegment.getMaxVelocity();

        this.currentCO2Emission = 0.0;

        this.cars = new CarList();
    }

    public void setLeftNeighbour(Lane leftNeighbour) {
        this.leftNeighbour = leftNeighbour;
    }

    public void setRightNeighbour(Lane rightNeighbour) {
        this.rightNeighbour = rightNeighbour;
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
    public double getMaxVelocity() {
        return maxVelocity;
    }


    public void setMaxVelocity(double maxVelocity) {
        this.maxVelocity = maxVelocity;

        if (!cars.isEmpty()) {
            Car car = getFirstCar();

            Car nextCar;

            while (car != null) {
                nextCar = car.getCarBehind();

                car.setMaxLaneVelocity(maxVelocity);

                car = nextCar;
            }
        }
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
    /**
     * This method returns if the car is allowed to join this lane, only taking into account space available,
     * not taking into account velocities.
     * @param previousResult the results of previous time. These are checked first for performances sake (very often, nothing changes)
     * @param car the car that wants to change to this lane
     * @return a triplet containing 
     *          1) a boolean telling whether it is possible for the car to change to this lane
     *          2) the car in front of the space where the car would end up
     *          3) the car behind the space where the car would end up
     */
    public Triplet<Boolean, Car, Car> acceptsCarInsert(Triplet<Boolean, Car, Car> previousResult, Car car) {

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

        
        if (previousResult != null) {
            Car carInFront = previousResult.getObject2();
            Car carBehind = previousResult.getObject3();
            
            if ((carInFront == null || carInFront.getLane() == this) && (carBehind == null || carBehind.getLane() == this)) {
                if (
                        carInFront != null && 
                        carInFront.getBack() > car.getFront() && 
                        carInFront.hasCarBehind() && 
                        car.getBack() > carInFront.getCarBehind().getFront()) {
                    
                    return previousResult;
                }
            }
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

    public double getDensity() {
        return carsLength / getLength();
    }
    
    public double pollCo2Emission() {
        double tmp = currentCO2Emission;
        currentCO2Emission = 0.0;
        return tmp;
    }

    public boolean hasCars() {
        return !cars.isEmpty();
    }

    public int getCarCount() {
        return cars.size();
    }

    private double currentCO2Emission;

    public void update(double timestep) {

        Car car = getFirstCar();

        Car nextCar;

        boolean queue = true;

        queueLength = 0.0;
        carsLength = 0.0;
        queueCount = 0;

        while (car != null) {
            nextCar = car.getCarBehind();

            car.update(timestep);

            if (car.getCurrentLane() == this && queue && car.isInQueue()) {
                queueLength = getLength() - car.getBack();
                queueCount++;
            }

            carsLength += car.getLength();
            
            currentCO2Emission += Co2Calculator.calculate(car.getVelocity(), car.getAcceleration(), car.getCarType().getFuelType()) * timestep;

            car = nextCar;
        }
    }
}
