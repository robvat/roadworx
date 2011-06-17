/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.List;
import java.util.Random;
import trafficownage.util.Pair;

/**
 *
 * @author Gerrit
 */
public class Car {

    private CarType car_type;
    private DriverType driver_type;
    private Route route;
    private boolean in_queue = false;
    private DriverModel driver_model;
    private Lane currentLane;
    private Node currentNode;
    private int position_coefficient;
    private double max_velocity;
    private double velocity;
    private double acceleration;
    private double position;
    private boolean changed_lane = false; //if the car already changed lane, this is true

    private class IDM implements DriverModel {

        private double a, b, v0, s0, T;

        public void init(DriverType driver, CarType car, double initial_max_velocity) {
            a = Math.min(driver.getMaxAcceleration(), car.getMaxAcceleration());
            b = driver.getMaxComfortableDeceleration();
            v0 = initial_max_velocity;
            s0 = driver.getMinimumDistanceToLeader();
            T = driver.getDesiredTimeHeadway();

            in_queue = false;
        }
        private double desired_distance;

        public double update(double velocity_leader, double distance_to_leader) {

            desired_distance = s0 + (velocity * T) + ((velocity * Math.abs(velocity_leader - velocity)) / (2 * Math.sqrt(a * b)));

            return a * (1 - Math.pow((velocity / v0), 4.0) - Math.pow(desired_distance / distance_to_leader, 2.0));

        }

        public void setMaxVelocity(double max_velocity) {
            v0 = max_velocity;
        }

        public double getMinimumDistanceToLeader() {
            return s0;
        }
    }

    /**
     * Initializes the car. Since we will use object pools, this just initializes the object
     * @param carType
     * @param driverType
     */
    public Car() {
        driver_model = new IDM();
    }

    /**
     * Real initialization. Sets the driver and car type, and the lane the car
     * will start driving on.
     * @param max_velocity
     */
    public void init(CarType carType, DriverType driverType) {
        this.car_type = carType;
        this.driver_type = driverType;
        driver_model.init(driverType, carType, max_velocity);
        route = new Route();

    }

    public DriverType getDriverType() {
        return driver_type;
    }

    public void switchLane(Lane lane) {
        this.currentLane = lane;
    }

    public void setLane(Lane lane) {
        this.currentLane = lane;
        this.currentNode = lane.getEndNode();

        route.determineNext();

        this.max_velocity = Math.min(Math.min((double) car_type.getMaxV(), (double) driver_type.getMaxVelocity()), lane.getMaxSpeed());
        
        this.position = car_type.getLength();

        driver_model.setMaxVelocity(max_velocity);
    }

    public double getDistanceToLaneEnd() {
        return currentLane.getLength() - position;
    }

    /**
     * Sets the maximum allowed velocity.
     * @param max_velocity
     */
    public void setMaxVelocity(double max_velocity) {
        this.max_velocity = max_velocity;

        driver_model.setMaxVelocity(max_velocity);
    }

    public DriverModel getDriverModel() {
        return driver_model;
    }

    /**
     * @return the velocity
     */
    public double getVelocity() {
        return velocity;
    }

    /**
     * @return the acceleration
     */
    public double getAcceleration() {
        return acceleration;
    }

    /**
     * @return the position
     */
    public double getPosition() {
        return position;
    }

    public double getFront() {
        return position;
    }

    public double getBack() {
        return position - car_type.getLength();
    }

    public double getLength() {
        return car_type.getLength();
    }

    public Lane getLane() {
        return currentLane;
    }

    public void putInQueue(boolean in_queue) {
        this.in_queue = in_queue;
    }

    public boolean isInQueue() {
        return in_queue;
    }

    public Node getNextNode() {
        if (route != null) {
            return route.getNext();
        } else {
            return null;
        }
    }

    public Node getCurrentNode() {
        if (route != null) {
            return route.getCurrent();
        } else {
            return null;
        }
    }

    public void advanceNode() {
        if (route != null) {
            route.advance();
        }
    }

    public Node getPreviousNode() {
        if (route != null) {
            return route.previous_node;
        } else {
            return null;
        }
    }

    public double getPositionCoefficient() {
        return position_coefficient;
    }

    private class Route {
        private boolean lastNode;
        private Node previous_node;
        private Node current_node;
        private Node nextNode = null;
        private Random randy;

        public Route() {
            randy = new Random();
            lastNode = false;
        }

        private void setCurrentNode() {
            previous_node = currentLane.getStartNode();
            current_node = currentLane.getEndNode();
        }

        public Node getCurrent() {
            if (current_node == null) {
                setCurrentNode();
            }

            return current_node;
        }

        public boolean lastNode() {
            return lastNode;
        }

        public void determineNext() {
            if (current_node == null) {
                setCurrentNode();
            }

            if (nextNode == null) {
                if (current_node.getDestinationNodes().size() > 1) {
                    while (nextNode == null || nextNode == previous_node) {
                        nextNode = current_node.getDestinationNodes().get(randy.nextInt(current_node.getDestinationNodes().size()));
                    }
                } else {
                    nextNode = null;
                    lastNode = true;
                }
            }
        }

        public Node getNext() {
            determineNext();

            return nextNode;
        }

        public boolean advance() {
            previous_node = current_node;
            current_node = nextNode;
            nextNode = null;
            return true;
        }
    }

    /*private class Route {

    private Node[] route;
    private int current_node_index;
    private Node current_node;

    public Route(Node[] route) {
    this.route = route;
    this.current_node_index = 0;
    current_node = route[current_node_index];
    }

    public Node getCurrent() {
    return current_node;
    }

    public Node getNext() {
    if (current_node_index < route.length - 1)
    return route[current_node_index + 1];
    else
    return null;
    }

    public boolean advance() {
    if (current_node_index < route.length - 1) {
    current_node_index++;
    current_node = route[current_node_index];
    return true;
    } else {
    return false;
    }
    }
    }*/
    public void setPosition(double position) {
        this.position = position;
    }

    /**
     * Updates the car position and velocity
     * @param dT
     * @param velocity_leader
     * @param distance_to_leader
     */
    public void update(double timestep) {

        //if not yet changed lane, do it, if you already changed lane, reset the variable, so you can change again next time
        if(!changed_lane)
            changed_lane = this.laneChanging();
        else
            changed_lane = false;

        boolean drivethrough = currentNode.drivethrough(this);

        Pair<Double, Car> nextCar = findNextCar();

        if (carInFront == null && getDistanceToLaneEnd() + getLength() < DISTANCE_THRESHOLD) {
            if (route.lastNode()) {
                currentLane.removeCar(this);
                System.out.println(this.toString() + " arrived");
            } else if (drivethrough) {
                in_queue = false;
                currentLane.removeCar(this);
                currentNode.acceptCar(this);
                route.advance();
            } else {
                in_queue = true;
                acceleration = 0.0;
                velocity = 0.0;
            }
        } else if (nextCar == null && drivethrough) {
            in_queue = false;
            follow(timestep, currentLane.getMaxSpeed(), Double.MAX_VALUE);
        } else if (nextCar == null && !drivethrough) {
            in_queue = false;
            follow(timestep,currentLane.getMaxSpeed(), getDistanceToLaneEnd());
        } else if (nextCar != null) {
            in_queue = false;
            follow(timestep, nextCar.getObject2().getVelocity(), nextCar.getObject1());
        }
    }
    private final static double DISTANCE_THRESHOLD = 2.0;

    private Pair<Double, Car> findNextCar() {

        if (carInFront != null)
            return new Pair<Double, Car>(carInFront.getBack() - this.getPosition(),carInFront);

        Lane lane = currentNode.getLaneMapping(currentLane);
        Car car = null;

        double distance = getDistanceToLaneEnd();

        while (lane != null && distance < 500) {
            car = lane.getLastCar();

            if (car != null && (distance + car.getBack()) < 500) {
                return new Pair<Double, Car>(distance + car.getBack(), car);
            }

            distance += lane.getLength();

            if (car == null)
                lane = lane.getEndNode().getLaneMapping(lane);
            else
                lane = null;
        }

        return null;
    }

    private void follow(double timestep, double leaderVelocity, double distanceToLeader) {
        acceleration = driver_model.update(leaderVelocity, distanceToLeader);
        velocity += acceleration * timestep;
        position += velocity * timestep;

    }
    private Car carInFront, carBehind;

    public Car getCarInFront() {
        return carInFront;
    }

    public Car getCarBehind() {
        return carBehind;
    }

    public void setCarInFront(Car nextCar) {
        this.carInFront = nextCar;
    }

    public void setCarBehind(Car previousCar) {
        this.carBehind = previousCar;
    }

    public static final int UNNECESSARY = 0, DESIRABLE = 1, ESSENTIAL = 2;
    private boolean laneChanging(){  
        Lane changedLane = null;
        //first check how important it is; essential, desirable or unnecessary
        // variable importance:
        // slot 1: turning movement/end-of-lane
        // slot 2: speed advantage
        // slot 3: queue advantage
        int[] importance = new int[3];
        for (int i: importance){
            i = UNNECESSARY;
        }

        //turning movement and end-of-lane
        if(!this.getLane().getAllowedDirections().contains(this.getNextNode())){
            //we assume that position is distance from previous node; so lane length - position = distance to next node
            //if the turn is less than 10s away
            if((this.getLane().getLength() - this.getPosition()) / this.getVelocity() < 10){
                importance[0] = ESSENTIAL;
            } else if ((this.getLane().getLength() - this.getPosition()) / this.getVelocity() < 50){
                //if the turn is between 10s and 50s away -> desirable
                importance[0] = DESIRABLE;
            }
        }

        //incident
        //TODO: check if there is need for lane changing because of an incident

        //transit lanes (do we have them?)
        //TODO: check if there is need for lane changing because of transit lanes

        //speed advantage
        //we assume that findNextCar is the car in front of this car, and that the double is the distance between the cars

        //first check if you want to go faster than the car in front of you is going
        if(Math.min(Math.min(this.getLane().getMaxSpeed(), this.getDriverType().getMaxVelocity()), this.car_type.getMaxV()) > this.findNextCar().getObject2().getVelocity()){
            //then check if you are close enough (less than 200 meters away
            if(this.findNextCar().getObject1() < 200){
                //TODO: overtaking on "straight/pass-through" nodes
                if((this.getLane().getLength() - this.getPosition()) / this.getVelocity() < 10){
                    importance[1] = DESIRABLE;
                }
            }
        }
        
        //queue advantage
        Lane right = this.getLane().getRightNeighbour();
        Lane left = this.getLane().getLeftNeighbour();
        
        if(this.findNextCar().getObject2().getVelocity() < 1){
            // there is a queue
            // check if right and left go the right way
            if(!right.getAllowedDirections().contains(this.getNextNode()))
                right = null;
            if(!left.getAllowedDirections().contains(this.getNextNode()))
                left = null;
            
            //for the lane right of you
            if(right != null){
                Car car = right.getFirstCar();
                while(car.getPosition() >= this.getPosition()){
                    car = car.getCarBehind();
                }

                if(!(car.getVelocity() < 1)){
                    if(car.getCarInFront().getPosition() - this.getCarInFront().getPosition() > 10){
                        importance[2] = DESIRABLE;
                        changedLane = right;
                    }  
                }
            }

            //same for the lane left of you
            if(left != null){
                Car car = left.getFirstCar();
                while(car.getPosition() >= this.getPosition()){
                    car = car.getCarBehind();
                }

                if(!(car.getVelocity() < 1)){
                    if(car.getCarInFront().getPosition() - this.getCarInFront().getPosition() > 10){
                        importance[2] = DESIRABLE;
                        changedLane = left;
                    }
                }
            }
        }

        // if lane changing is unnecessary for everything: stop
        boolean done = true;
        A: for(int i: importance){
            if(i != UNNECESSARY){
                done = false;
                break A;
            }
        }
        if(done)
            return false;

        // now determine to which lane you will change
        
        //you are driving towards the current node
        List<Lane> allLanes = null;
        if(this.getLane().getRoadSegment().getStartNode().equals(this.getCurrentNode())){
            allLanes = this.getLane().getRoadSegment().getEndLanes();
        } else {
            allLanes = this.getLane().getRoadSegment().getStartLanes();
        }
        
        //find index current lane
        int currentIndex = 0;
        for(int i = 1; i < allLanes.size(); i++){
                if(allLanes.get(i).equals(this.getLane())){
                    currentIndex = i;
                    break;
                }
            }

        //only first reason can be essential (speed/queue changing is never essential)
        if(importance[0] != UNNECESSARY){
            int targetIndex = 0;
            loop: for(int j = 0; j < allLanes.size(); j++){
                if(allLanes.get(j).getAllowedDirections().contains(this.getNextNode())){
                    targetIndex = j;
                    break loop;
                }
                
                //TODO: we assume that if targetIndex > you,  you go right
                if(targetIndex > currentIndex){
                    changedLane = this.getLane().getRightNeighbour();
                } else
                    changedLane = this.getLane().getLeftNeighbour();
            }
        } else if(importance[1] != UNNECESSARY){
            changedLane = this.getLane().getLeftNeighbour();
        }

        //check if the lane change is physically possible
        boolean feasible = false;
        //find the cars in front of you and behind you on the changedLane
        Car carF = changedLane.getFirstCar();
        while(carF.getPosition() >= this.getPosition()){
            carF = carF.getCarBehind();
        }
        Car carB = carF.getCarBehind();

        double timeUntilCrashWithCarF = (carF.getBack() - this.getFront()) / (carF.getVelocity() - this.getVelocity());
        double decceleratedVelocity = timeUntilCrashWithCarF * this.getDriverType().getMaxComfortableDeceleration();

        double timeUntilCrashWithMe = (this.getBack() - carB.getFront()) / (this.getVelocity() - carB.getVelocity());
        double decceleratedVelocity2 = timeUntilCrashWithMe * carB.getDriverType().getMaxComfortableDeceleration();
        
        //check that they aren't overlapping you
        if(carF.getBack() < this.getFront() && importance[0] != ESSENTIAL ||
                carB.getFront() > this.getBack() && importance[0] != ESSENTIAL){
            return false;
        } else if(carF.getBack() < this.getFront() && importance[0] == ESSENTIAL ||
                carB.getFront() > this.getBack() && importance[0] == ESSENTIAL){
            return sendCourtesyRequest(carB);
        } else if(!((carF.getVelocity() - this.getVelocity()) < decceleratedVelocity) ||
                !((this.getVelocity() - carB.getVelocity()) < decceleratedVelocity2)){
            //you will crash into carF or carB will crash into you
            return false;
        } else {
            //change lane :D
            this.getLane().removeCar(this);
            changedLane.insertCar(this, carF, carB);
            this.changed_lane = true;
            return true;
        }
        //TODO: carF and carB can't be null yet
    }
    
    
    public boolean sendCourtesyRequest(Car car){
        //TODO: car has to slow down/stop
        
        return false;
    }
}
