/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.ArrayList;
import java.util.List;
import trafficownage.simulation.TrafficManager.CarStatistics;
import trafficownage.util.Container;
import trafficownage.util.Triplet;

/**
 *
 * @author Gerrit
 */
public class Car
{

    private CarType carType;
    private CarStatistics carStatistics;
    private DriverType driverType;
    private Route route;
    private boolean inQueue = false;
    private DriverModel driverModel;
    private Node previousNode;
    private Lane currentLane;
    private Node currentNode;
    private int position_coefficient;
    private double maxCarVelocity;
    private double maxLaneVelocity;
    private double currentMaxVelocity;
    private double velocity;
    private double acceleration;
    private double position;
    private double position_threshold;
    private boolean updated = false; //if the car already changed lane, this is true
    private boolean courtesy = false; // In courtesy mode => braking
    private Car courtesyCar; // The car that send the request needs to be checked
    private List<CarListener> listeners;

    private Container container;

    private class DriverModel
    {

        private double a, b, v0, s0, T;

        public void init(DriverType driver, CarType car)
        {
            a = Math.min(driver.getMaxAcceleration(), car.getMaxAcceleration());
            b = driver.getMaxComfortableDeceleration();
            s0 = driver.getMinimumDistanceToLeader();
            T = driver.getDesiredTimeHeadway();

            inQueue = false;
        }
        private double desired_distance;

        public double update(double velocity_leader, double distance_to_leader)
        {
            desired_distance = s0 + (velocity * T) + ((velocity * Math.abs(velocity_leader - velocity)) / (2 * Math.sqrt(a * b)));

            return a * (1 - Math.pow((velocity / v0), 4.0) - Math.pow(desired_distance / distance_to_leader, 2.0));

        }

        public void setMaxVelocity(double maxVelocity)
        {
            v0 = maxVelocity;
        }

        public double getMinimumDistanceToLeader()
        {
            return s0;
        }

        public double getMaxVelocity()
        {
            return v0;
        }
    }

    /**
     * Initializes the car. Since we will use object pools, this just initializes the object
     * @param carType
     * @param driverType
     */
    public Car()
    {
        driverModel = new DriverModel();
    }

    public void addCarStatisticsListener(CarStatistics carStatistics) {
        this.carStatistics = carStatistics;
        listeners.add(carStatistics);
    }

    public CarStatistics getCarStatisticsListener() {
        return carStatistics;
    }

    public void addListener(CarListener listener) {
        listeners.add(listener);
    }

    /**
     * Real initialization. Sets the driver and car type, and the lane the car
     * will start driving on.
     * @param max_velocity
     */
    public void init(CarType carType, DriverType driverType)
    {
        listeners = new ArrayList<CarListener>();

        maxCarVelocity = Math.min(carType.getMaxVelocity(), driverType.getMaxVelocity());

        this.carType = carType;
        this.driverType = driverType;

        driverModel.init(driverType, carType);

        queueTime = 0.0;

    }
    
    public void setRoute(Route route) {
        this.route = route;
    }



    public DriverType getDriverType()
    {
        return driverType;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public Container getContainer() {
        return container;
    }

    public void switchLane(Lane lane)
    {
        this.currentLane = lane;
        nextLane = null;
    }

    public void initSpeed(double maxLaneVelocity)
    {
        setMaxLaneVelocity(maxLaneVelocity);
        velocity = currentMaxVelocity;
    }

    public void setLane(Lane lane)
    {
        this.currentLane = lane;
        this.previousNode = lane.getStartNode();
        this.currentNode = lane.getEndNode();

        route.determineNext(currentNode);

        setMaxLaneVelocity(lane.getMaxVelocity());

        this.position = 0.0;//car_type.getLength();
        this.position_threshold = lane.getLength() - DISTANCE_THRESHOLD;// + getLength();
        
        nextLane = null;
    }

    /**
     * Set the current Lane of the car to null
     * @return True if the switch succeeded, false if it was already null
     */
    public boolean setNoLane()
    {
        if(this.currentLane != null)
        {
            currentLane = null;
            return true;
        }
        else
            return false;
    }

    public double getDistanceToLaneEnd()
    {
        return currentLane.getLength() - position;
    }

    public double getMaxVelocity() {
        return maxCarVelocity;
    }

    /**
     * Sets the maximum allowed velocity.
     * @param max_velocity
     */
    public void setMaxLaneVelocity(double maxLaneVelocity)
    {
        this.maxLaneVelocity = maxLaneVelocity;
        this.currentMaxVelocity = Math.min(maxLaneVelocity,maxCarVelocity);

        driverModel.setMaxVelocity(currentMaxVelocity);
    }

    public DriverModel getDriverModel()
    {
        return driverModel;
    }

    /**
     * @return the velocity
     */
    public double getVelocity()
    {
        return velocity;
    }

    /**
     * @return the acceleration
     */
    public double getAcceleration()
    {
        return acceleration;
    }

    /**
     * @return the position
     */
    public double getPosition()
    {
        return position;
    }

    public double getFront()
    {
        return position;
    }

    public double getBack()
    {
        return position - carType.getLength();
    }

    public double getLength()
    {
        return carType.getLength();
    }

    public Lane getLane()
    {
        return currentLane;
    }

    private double queueTime;
    public void putInQueue(boolean inQueue)
    {
        if (!this.inQueue) {
            this.inQueue = inQueue;
        }
    }

    public double getQueueTime() {
        return queueTime;
    }

    public boolean isInQueue()
    {
        return inQueue;
    }

    public Node getPreviousNode()
    {
        return previousNode;
    }

    public Lane getCurrentLane()
    {
        return currentLane;
    }

//    public Node getFirstNode() {
//        return route.getFirstNode();
//    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public Node getNextNode()
    {
        if (route != null)
        {
            return route.getNextNode();
        } else
        {
            return null;
        }
    }

    public double getPositionCoefficient()
    {
        return position_coefficient;
    }

    


    public void setPosition(double position)
    {
        this.position = position;
    }
    private static final double VIEW_DISTANCE = 100.0;
    private static final double VERY_LONG_DISTANCE = 100000.0;
    //TODO: change the view distance into a driver property

    public Lane getNextLane() {
        return nextLane;
    }

    private Lane nextLane;
    public void determineNextLane() { // moet weer private worden -> debugging
        nextLane = null;

        List<Lane> lanes = currentNode.getRoadSegment(route.getNextNode()).getSourceLanes(currentNode);

        Lane mapped = currentNode.getLaneMapping(currentLane);

        if (mapped != null && lanes.contains(mapped) && mapped.acceptsCarAdd(this)) {
            nextLane = mapped;
            return;
        }
        
        for (Lane l : lanes) {
            if (!carType.doesOvertake() && l.getRightNeighbour() != null)
                continue;
            
            if (l.acceptsCarAdd(this)) {
                nextLane = l;
                return;
            }
        }        
    }

    public CarType getCarType() {
        return carType;
    }

    public boolean doesOvertake() {
        return carType.doesOvertake();
    }

    public Route getRoute() {
        return route;
    }

    /**
     * Updates the car position and velocity
     * @param dT
     * @param velocity_leader
     * @param distance_to_leader
     */
    public void update(double timestep)
    {
        if(courtesy)
            checkPassage();

        inQueue = false; //every time we look if this is still the case. Normally, this is turned off.

        //find out where, so which lane, we are intending to join
        if (!route.isEndOfRoute() && (nextLane == null || !nextLane.acceptsCarAdd(this)))
            determineNextLane();
        

        Car nextCar = getCarInFront();
        
        boolean drivethrough = currentNode.drivethrough(this);

        double distance = Math.max(0.0, getDistanceToLaneEnd());

        double distanceToNextCar = 0.0;


        if (nextCar != null)
        {
            distanceToNextCar = nextCar.getBack() - getFront();

        } else if (
                nextCar == null &&
                distance < VIEW_DISTANCE &&
                drivethrough &&
                !route.isEndOfRoute() &&
                nextLane != null &&
                nextLane.hasCars() &&
                (distance + nextLane.getLastCar().getBack()) < VIEW_DISTANCE
                )
        {
            nextCar = nextLane.getLastCar();
            distanceToNextCar = distance + nextCar.getBack();            
        }


        int method = -1;
        double p_before = position;
        
        if (!this.laneChanging(nextCar, distanceToNextCar)) {

            if (nextCar != null)
            {
                follow(timestep, nextCar.getVelocity(), distanceToNextCar);
                method = 0;
            } else if (drivethrough)
            {
                follow(timestep, currentLane.getMaxVelocity(), VERY_LONG_DISTANCE);
                method = 1;
            } else
            {
                follow(timestep, 0.0, distance);
                method = 2;
            }

            if (((getCarInFront() != null && getCarInFront().isInQueue()) || getCarInFront() == null) && velocity < VELOCITY_THRESHOLD)
            {
                if (route.isEndOfRoute()) {
                    //System.out.println("Car arrived at its destination.");

                    if (!listeners.isEmpty())
                        for (CarListener listener : listeners)
                            listener.reachedDestination(this, currentLane.getEndNode());

                    currentLane.removeCar(this);

                } else {
                    putInQueue(true);
                    queueTime += timestep;
                }
            }
        }

        if (position > position_threshold)
        {
            currentNode.acceptCar(this);
        } else if (nextCar != null && position > position_threshold) {
            System.err.println("The car in front of this car should not be there.");
        }

        updated = true;

        for (CarListener listener : listeners)
            listener.positionChanged(this);

    }

    private final static double DISTANCE_THRESHOLD = 0.0;
    private final static double VELOCITY_THRESHOLD = 0.1;

    private void follow(double timestep, double leaderVelocity, double distanceToLeader)
    {
        if(courtesy)
        {
            if(this.getVelocity() > 0.05)
                acceleration = (-driverType.getMaxComfortableDeceleration());
            else
                acceleration = 0;
        }
        else
        {
            acceleration = driverModel.update(leaderVelocity, distanceToLeader);
        }
        
        velocity = Math.max(0.0, velocity + (acceleration * timestep));
        position += velocity * timestep;
    }

    public boolean hasCarBehind()
    {
        return container.getNext() != null;
    }
    
    public Car getCarBehind()
    {
        return container.getNext().getCar();
    }

    public Car getCarInFront()
    {
        return container.getPrevious().getCar();
    }

    public static final int UNNECESSARY = 0, DESIRABLE = 1, ESSENTIAL = 2;

    private Triplet<Boolean,Car,Car> laneChangeParameters;
    private boolean laneChanging(Car nextCar, double distanceToNextCar)
    {
        Lane leftLane, rightLane;
        // Check if there are other lanes!
        leftLane = this.getLane().getLeftNeighbour();
        rightLane = this.getLane().getRightNeighbour();

        if (this.route.isEndOfRoute()) //you're heading to your final destination
        {
            return false;
        }

        if ((leftLane == null) && (rightLane == null))
        {
            return false; // quit! its useless, no lanes to change to
        }
        Lane desiredLane = null;
        boolean done = true;    //check for later on, if everything says lane changing is unnecessary then you're done
        //first check how important it is; essential, desirable or unnecessary
        // variable importance:
        // slot 1: turning movement/end-of-lane
        // slot 2: speed advantage
        // slot 3: queue advantage
        // slot 4: go back after overtake
        int[] importance = new int[4];
        for (int i : importance)
        {
            i = UNNECESSARY;
        }

        //turning movement and end-of-lane
        if (this.getLane().getAllowedDirections() == null || !this.getLane().getAllowedDirections().contains(this.getNextNode()))
        {
            //we assume that position is distance from previous node; so lane length - position = distance to next node
            //if the turn is less than 10s away
            if ((this.getLane().getLength() - this.getPosition()) / this.getVelocity() < 10.0)
            {
                importance[0] = ESSENTIAL;
                done = false;
            } else if ((this.getLane().getLength() - this.getPosition()) / this.getVelocity() < 50.0)
            {
                //if the turn is between 10s and 50s away -> desirable
                importance[0] = DESIRABLE;
                done = false;
            }
        }

        //incident
        //TODO: check if there is need for lane changing because of an incident

        //transit lanes (do we have them?)
        //TODO: check if there is need for lane changing because of transit lanes

        //speed advantage
//        we assume that findNextCar is the car in front of this car, and that the double is the distance between the cars

//         We need to be far off the next Node
        if((currentLane.getLength() - position) > 300)
        {
            // We need to be close to the next car
            if(nextCar != null && distanceToNextCar < 200)
            {
                // Both aren't accelerating or nothing
                if((Math.abs(this.getCarInFront().getAcceleration()) < 0.1) && (Math.abs(acceleration) < 0.1))
                {
                    // You still have some headroom left in the speed department
                    if((velocity + 10) < driverModel.getMaxVelocity())
                    {
                        // time to overtake!
                        importance[1] = DESIRABLE;
                        done = false;
                    }
                }
            }
        }

        //queue advantage
        Lane left = leftLane;

        if (!isInQueue() && getCarInFront() != null && getCarInFront().getVelocity() < 2.0 && (getCarInFront().getBack() - getFront()) < 6.0)
        {
//            System.out.println("ME!: " + this.toString() + " FRONT: " + getCarInFront().toString());
            // there is a queue
            // check if left go the right way
            if (left != null && !left.getAllowedDirections().contains(this.getNextNode()))
                left = null;

            //same for the lane left of you
            if (left != null && (left.getQueueLength() + getLength()) < currentLane.getQueueLength())
            {
                //System.out.println("Left: " + Double.toString(left.getQueueLength() + getLength() + 2) + " < " + "Current: " + Double.toString(currentLane.getQueueLength()));
                importance[2] = DESIRABLE;
                desiredLane = left;
                done = false;
            }
        }

        //going back
        if (rightLane != null)
        {
            if (rightLane.getAllowedDirections().contains(this.getNextNode()))
            {
                importance[3] = DESIRABLE;
                done = false;
            }
        }

        // if lane changing is unnecessary for everything: stop
        if (done)
        {
            return false;
        }

        if(!this.carType.doesOvertake() && importance[0] == UNNECESSARY){
            return false;
        }

        // now determine to which lane you will change

        //you are driving towards the current node
        List<Lane> allLanes = null;
        if (this.getLane().getRoadSegment().getStartNode().equals(this.getCurrentNode()))
        {
            allLanes = this.getLane().getRoadSegment().getEndLanes();
        } else
        {
            allLanes = this.getLane().getRoadSegment().getStartLanes();
        }

        //only first reason can be essential (speed/queue changing is never essential)
        if (importance[0] != UNNECESSARY)
        {
            int currentId = this.getLane().getLaneId();

            int newId, direction = 0;

            for (Lane l : currentNode.getIncomingLanes()) {
                if (l.getAllowedDirections().contains(this.getNextNode())) {
                    newId = l.getLaneId();
                    direction = Integer.signum(newId - currentId);
                }
            }
            if (direction > 0)
                desiredLane = getLane().getLeftNeighbour();
            else if (direction < 0)
                desiredLane = getLane().getRightNeighbour();

        } else if (importance[1] != UNNECESSARY)
        {
            desiredLane = leftLane;
        } else if (importance[3] != UNNECESSARY)
        {
            desiredLane = rightLane;
        }

        if (desiredLane == null)
        {
            //System.err.println("The lane it wants to change to doesn't exist.");
            return false;
        }
        
        laneChangeParameters = desiredLane.acceptsCarInsert(laneChangeParameters, this);

        Car carInFront = laneChangeParameters.getObject2();
        Car carBehind = laneChangeParameters.getObject3();

        if (!laneChangeParameters.getObject1())
            return false;

        if (carInFront == null && carBehind == null)
        {
            //change lane :D

            return changeLane(desiredLane, carInFront, carBehind);
        } else if (carBehind == null)
        {
            double timeUntilCrashWithCarF = (carInFront.getBack() - this.getFront() -2.0) / (this.getVelocity() - carInFront.getVelocity());    //2 meters for safety
            double decceleratedVelocity = timeUntilCrashWithCarF * this.getDriverType().getMaxComfortableDeceleration();

            if (timeUntilCrashWithCarF < this.getDriverType().getDesiredTimeHeadway())
            {
                return false;
            }
            if((carInFront.getBack() - this.getFront()) < this.getDriverType().getMinimumDistanceToLeader())
            {
                return false;
            }
            if (!laneChangeParameters.getObject1() && importance[0] != ESSENTIAL)
            {
                return false;
            } else if (!laneChangeParameters.getObject1() && importance[0] == ESSENTIAL) //TODO: slow down yourself
            {
                return false;
            } else if (!((this.getVelocity() - carInFront.getVelocity()) < decceleratedVelocity))
            {
                return false;
            } else
            {
                //change lane :D
                return changeLane(desiredLane, carInFront, carBehind);
            }
        } else if (carInFront == null)
        {
            double timeUntilCrashWithMe = (this.getBack() - carBehind.getFront() -2.0) / (carBehind.getVelocity() - this.getVelocity());    //2m for safety
            double decceleratedVelocity2 = timeUntilCrashWithMe * carBehind.getDriverType().getMaxComfortableDeceleration();

            if (timeUntilCrashWithMe < carBehind.getDriverType().getDesiredTimeHeadway())
            {
                return false;
            }
            if((this.getBack() - carBehind.getFront()) < carBehind.getDriverType().getMinimumDistanceToLeader())
            {
                return false;
            }
            if (carBehind.getFront() > this.getBack() && importance[0] != ESSENTIAL)
            {
                return false;
            } else if (carBehind.getFront() > this.getBack() && importance[0] == ESSENTIAL)
            {
                return this.sendCourtesyRequest(carBehind);
            } else if (!((carBehind.getVelocity() - this.getVelocity()) < decceleratedVelocity2))
            {
                return false;
            } else
            {
                //change lane :D
                return changeLane(desiredLane, carInFront, carBehind);
            }
        } else
        {
            double timeUntilCrashWithCarF = (carInFront.getBack() - this.getFront() -2.0) / (this.getVelocity() - carInFront.getVelocity());    //2m for safety
            double decceleratedVelocity = timeUntilCrashWithCarF * this.getDriverType().getMaxComfortableDeceleration();

            double timeUntilCrashWithMe = (this.getBack() - carBehind.getFront() -2.0) / (carBehind.getVelocity() - this.getVelocity());    //2m for safety
            double decceleratedVelocity2 = timeUntilCrashWithMe * carBehind.getDriverType().getMaxComfortableDeceleration();

            //check that they aren't overlapping you
            if (timeUntilCrashWithCarF < this.getDriverType().getDesiredTimeHeadway())
            {
                return false;
            }
            if (timeUntilCrashWithMe < carBehind.getDriverType().getDesiredTimeHeadway())
            {
                return false;
            }
            if((carInFront.getBack() - this.getFront()) < this.getDriverType().getMinimumDistanceToLeader())
            {
                return false;
            }
            if((this.getBack() - carBehind.getFront()) < carBehind.getDriverType().getMinimumDistanceToLeader())
            {
                return false;
            }
            if (carInFront.getBack() < this.getFront() && importance[0] != ESSENTIAL
                    || carBehind.getFront() > this.getBack() && importance[0] != ESSENTIAL)
            {
                return false;
            } else if (carInFront.getBack() < this.getFront() && importance[0] == ESSENTIAL
                    || carBehind.getFront() > this.getBack() && importance[0] == ESSENTIAL)
            {
                return sendCourtesyRequest(carBehind);
            } else if (!((this.getVelocity() - carInFront.getVelocity()) < decceleratedVelocity)
                    || !((carBehind.getVelocity() - this.getVelocity()) < decceleratedVelocity2))
            {
                //you will crash into carF or carB will crash into you
                return false;
            } else
            {
                //change lane :D
                return changeLane(desiredLane, carInFront, carBehind);
            }
        }
    }

    private boolean changeLane(Lane changeLane, Car carInFront, Car carBehind)
    {
        if (carInFront != null)
            changeLane.insertCarAfter(carInFront, this);
        else if (carBehind != null)
            changeLane.insertCarBefore(carBehind, this);
        else
            changeLane.insertCar(this);

        return true;
    }

    

    public boolean sendCourtesyRequest(Car car)
    {
        boolean already;
        already = car.startCourtesy(this);
        // now he MUST go that way or something will go wrong!!!
        System.out.println("Courtesy, gimme some room plox");
        if(already)
            System.out.println("HondeLULLEN! He was already givvin sum!");
        return true;
    }

    /**
     * Puts the car in Courtesy mode, braking for the car that needs to
     * enter the lane
     * @param car The Car requesting the courtesy
     * @return Wether the car already was in courtesy mode or not
     */
    public boolean startCourtesy(Car car)
    {
        if (!courtesy)
        {
            courtesy = true;
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * Stops the courtesy mode
     */
    public void endCourtesy()
    {
        courtesy = false;
    }

    /**
     * We need to check wether this car has accidentally drove passed the car
     * who send the Courtesy Request or other eventualities
     * @return True is it had to be changed
     */
    private boolean checkPassage()
    {
        if(courtesyCar == null)
            System.err.println("No car here to provide this courtesy for");
        if (courtesyCar.getPosition() < this.getPosition())
        {
            this.endCourtesy(); // You passed the requester
            return true;
        }
        if (this.getCarInFront().equals(courtesyCar))
        {
            this.endCourtesy(); // The Car is already in your lane / it WORKED!
            return true;
        }
        if ((this.getVelocity() == 0) && (this.getFront() < courtesyCar.getBack()))
        {
            this.endCourtesy(); // You stand still next to the car.
            return true;
        }
        return false;
    }
}
