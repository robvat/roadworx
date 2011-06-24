/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.List;
import java.util.Random;
import trafficownage.util.Container;
import trafficownage.util.Pair;
import trafficownage.util.Triplet;

/**
 *
 * @author Gerrit
 */
public class Car
{

    private CarType car_type;
    private DriverType driver_type;
    private Route route;
    private boolean in_queue = false;
    private DriverModel driver_model;
    private Node previousNode;
    private Lane currentLane;
    private Node currentNode;
    private int position_coefficient;
    private double max_velocity;
    private double velocity;
    private double acceleration;
    private double position;
    private double position_threshold;
    private boolean updated = false; //if the car already changed lane, this is true
    private boolean courtesy = false; //In courtesy mode => braking
    private Car courtesyCar; // The car that send the request needs to be checked

    private Container container;

    private class IDM implements DriverModel
    {

        private double a, b, v0, s0, T;

        public void init(DriverType driver, CarType car, double initial_max_velocity)
        {
            a = Math.min(driver.getMaxAcceleration(), car.getMaxAcceleration());
            b = driver.getMaxComfortableDeceleration();
            v0 = initial_max_velocity;
            s0 = driver.getMinimumDistanceToLeader();
            T = driver.getDesiredTimeHeadway();

            in_queue = false;
        }
        private double desired_distance;

        public double update(double velocity_leader, double distance_to_leader)
        {

            if (courtesy)
            {
                return (-b); // Brakes as hard as Comfortable :)
            }
            desired_distance = s0 + (velocity * T) + ((velocity * Math.abs(velocity_leader - velocity)) / (2 * Math.sqrt(a * b)));

            return a * (1 - Math.pow((velocity / v0), 4.0) - Math.pow(desired_distance / distance_to_leader, 2.0));

        }

        public void setMaxVelocity(double max_velocity)
        {
            v0 = max_velocity;
        }

        public double getMinimumDistanceToLeader()
        {
            return s0;
        }
    }

    /**
     * Initializes the car. Since we will use object pools, this just initializes the object
     * @param carType
     * @param driverType
     */
    public Car()
    {
        driver_model = new IDM();
    }

    /**
     * Real initialization. Sets the driver and car type, and the lane the car
     * will start driving on.
     * @param max_velocity
     */
    public void init(CarType carType, DriverType driverType)
    {
        this.car_type = carType;
        this.driver_type = driverType;
        driver_model.init(driverType, carType, max_velocity);
        route = new Route();

    }

    public DriverType getDriverType()
    {
        return driver_type;
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

    public void setLane(Lane lane)
    {
        this.currentLane = lane;
        this.previousNode = lane.getStartNode();
        this.currentNode = lane.getEndNode();

        route.determineNext();

        setMaxVelocity(Math.min(Math.min((double) car_type.getMaxV(), (double) driver_type.getMaxVelocity()), lane.getMaxSpeed()));

        this.position = 0.0;//car_type.getLength();
        this.position_threshold = lane.getLength() - DISTANCE_THRESHOLD;// + getLength();
        
        nextLane = null;
    }

    public double getDistanceToLaneEnd()
    {
        return currentLane.getLength() - position;
    }

    /**
     * Sets the maximum allowed velocity.
     * @param max_velocity
     */
    public void setMaxVelocity(double max_velocity)
    {
        this.max_velocity = max_velocity;

        driver_model.setMaxVelocity(max_velocity);
    }

    public DriverModel getDriverModel()
    {
        return driver_model;
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
        return position - car_type.getLength();
    }

    public double getLength()
    {
        return car_type.getLength();
    }

    public Lane getLane()
    {
        return currentLane;
    }

    public void putInQueue(boolean in_queue)
    {
        this.in_queue = in_queue;
    }

    public boolean isInQueue()
    {
        return in_queue;
    }

    public Node getPreviousNode()
    {
        return previousNode;
    }

    public Lane getCurrentLane()
    {
        return currentLane;
    }

    public Node getFirstNode() {
        return route.getFirstNode();
    }

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

    private class Route
    {

        private Node nextNode = null;
        private Node firstNode = null;
        private boolean endOfRoute;
        private Random randy;

        public Route()
        {
            randy = new Random();
            endOfRoute = false;
        }

        public Node getFirstNode() {
            return firstNode;
        }

        public void determineNext() {

            List<Node> destinationNodes = currentNode.getDestinationNodes();

            nextNode = null;

            if (destinationNodes.isEmpty() || (destinationNodes.size() == 1 && destinationNodes.get(0) == previousNode))
            {
                endOfRoute = true;

            } else {
                while (nextNode == null || (previousNode != null && nextNode == previousNode) || nextNode instanceof SpawnNode) {
                    nextNode = destinationNodes.get(randy.nextInt(destinationNodes.size()));
                }
            }
        }

        public boolean isEndOfRoute()
        {
            return endOfRoute;
        }

        public Node getNextNode()
        {
            //determineNext();

            return nextNode;
        }

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
    private void determineNextLane() {
        nextLane = null;

        List<Lane> lanes = currentNode.getRoadSegment(route.getNextNode()).getSourceLanes(currentNode);

        Lane mapped = currentNode.getLaneMapping(currentLane);

        if (mapped != null && lanes.contains(mapped) && mapped.acceptsCarAdd(this)) {
            nextLane = mapped;
            return;
        }
        
        for (Lane l : lanes) {
            if (l.acceptsCarAdd(this)) {
                nextLane = l;
                return;
            }
        }        
    }
    /**
     * Updates the car position and velocity
     * @param dT
     * @param velocity_leader
     * @param distance_to_leader
     */
    public void update(double timestep)
    {

        in_queue = false; //every time we look if this is still the case. Normally, this is turned off.

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

            if (distanceToNextCar < 0.0) {
                distanceToNextCar = 0.0;
                System.err.println("SAY WHAT?!?!?");
            }

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
                follow(timestep, currentLane.getMaxSpeed(), VERY_LONG_DISTANCE);
                method = 1;
            } else
            {
                follow(timestep, 0.0, distance);
                method = 2;
            }

            if (((getCarInFront() != null && getCarInFront().isInQueue()) || getCarInFront() == null) && velocity < VELOCITY_THRESHOLD)
            {
                in_queue = true;
            }
        }

        if (position > position_threshold)
        {
            if (getCarInFront() != null)
                System.err.println("Car is over the line but not first. What is wrong?");
            
            if (route.isEndOfRoute())
            {
                currentLane.removeCar(this);
                System.out.println(this.toString() + " arrived.");
            } else
            {
                currentNode.acceptCar(this);
            }

        } else if (nextCar != null && position > position_threshold) {
            System.err.println("The car in front of this car should not be there.");
        }

        updated = true;
    }

    private final static double DISTANCE_THRESHOLD = 0.0;
    private final static double VELOCITY_THRESHOLD = 0.1;

    private void follow(double timestep, double leaderVelocity, double distanceToLeader)
    {
        acceleration = driver_model.update(leaderVelocity, distanceToLeader);
        velocity = Math.max(0.0, velocity + (acceleration * timestep));
        position += velocity * timestep;
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
        //we assume that findNextCar is the car in front of this car, and that the double is the distance between the cars

        // We need to be far off the next Node
//        if((currentLane.getLength() - position) > 300)
//        {
//            // We need to be close to the next car
//            if(nextCar != null && distanceToNextCar < 200)
//            {
//                // Both aren't accelerating or nothing
//                if((Math.abs(this.getCarInFront().getAcceleration()) < 0.1) && (Math.abs(acceleration) < 0.1))
//                {
//                    // You still have some headroom left in the speed department
//                    if(velocity < max_velocity)
//                    {
//                        // time to overtake!
//                        importance[1] = DESIRABLE;
//                        done = false;
//                    }
//                }
//            }
//        }

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
            //System.err.println("The lane it wants to change to doesn't exist");
            return false;
        }


        Triplet<Boolean,Car,Car> laneChangeParameters = desiredLane.acceptsCarInsert(this);

        //check if the l ane change is physically possible
        //find the cars in front of you and behind you on the changedLane
//        Car carInFront = null;
//        double carInFrontDistance = Double.MAX_VALUE;
//        Car carBehind = null;
//        double carBehindDistance = Double.MAX_VALUE;
//
//        double tmp;
//
//        if (desiredLane.hasCars())
//        {
//            Car otherCar = desiredLane.getFirstCar();
//
//            while (otherCar != null) {
//                 tmp = otherCar.getBack() - getFront();
//
//                if (tmp > 0.0 && tmp < carInFrontDistance) {
//                    carInFront = otherCar;
//                    carInFrontDistance = tmp;
//                }
//
//                tmp = getBack() - otherCar.getFront();
//
//                if (tmp > 0.0 && tmp < carBehindDistance) {
//                    carBehind = otherCar;
//                    carBehindDistance = tmp;
//                }
//
//                otherCar = otherCar.getCarBehind();
//            }
//        }

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
            double timeUntilCrashWithCarF = (carInFront.getBack() - this.getFront()) / (this.getVelocity() - carInFront.getVelocity());
            double decceleratedVelocity = timeUntilCrashWithCarF * this.getDriverType().getMaxComfortableDeceleration();

            if (carInFront.getBack() < this.getFront() && importance[0] != ESSENTIAL)
            {
                return false;
            } else if (carInFront.getBack() < this.getFront() && importance[0] == ESSENTIAL) //TODO: slow down yourself
            {
                return false;
            } else if (!((carInFront.getVelocity() - this.getVelocity()) < decceleratedVelocity))
            {
                return false;
            } else
            {
                //change lane :D
                return changeLane(desiredLane, carInFront, carBehind);
            }
        } else if (carInFront == null)
        {
            double timeUntilCrashWithMe = (this.getBack() - carBehind.getFront()) / (carBehind.getVelocity() - this.getVelocity());
            double decceleratedVelocity2 = timeUntilCrashWithMe * carBehind.getDriverType().getMaxComfortableDeceleration();

            if (carBehind.getFront() > this.getBack() && importance[0] != ESSENTIAL)
            {
                return false;
            } else if (carBehind.getFront() > this.getBack() && importance[0] == ESSENTIAL)
            {
                return this.sendCourtesyRequest(carBehind);
            } else if (!((this.getVelocity() - carBehind.getVelocity()) < decceleratedVelocity2))
            {
                return false;
            } else
            {
                //change lane :D
                return changeLane(desiredLane, carInFront, carBehind);
            }
        } else
        {
            double timeUntilCrashWithCarF = (carInFront.getBack() - this.getFront()) / (this.getVelocity() - carInFront.getVelocity());
            double decceleratedVelocity = timeUntilCrashWithCarF * this.getDriverType().getMaxComfortableDeceleration();

            double timeUntilCrashWithMe = (this.getBack() - carBehind.getFront()) / (carBehind.getVelocity() - this.getVelocity());
            double decceleratedVelocity2 = timeUntilCrashWithMe * carBehind.getDriverType().getMaxComfortableDeceleration();

            //check that they aren't overlapping you
            if (carInFront.getBack() < this.getFront() && importance[0] != ESSENTIAL
                    || carBehind.getFront() > this.getBack() && importance[0] != ESSENTIAL)
            {
                return false;
            } else if (carInFront.getBack() < this.getFront() && importance[0] == ESSENTIAL
                    || carBehind.getFront() > this.getBack() && importance[0] == ESSENTIAL)
            {
                return sendCourtesyRequest(carBehind);
            } else if (!((carInFront.getVelocity() - this.getVelocity()) < decceleratedVelocity)
                    || !((this.getVelocity() - carBehind.getVelocity()) < decceleratedVelocity2))
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
