/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class NormalJunction extends Node {


    public NormalJunction(Point2D.Double location){
        super(location);
    }

    @Override
    public boolean drivethrough(Car incoming) { 
        
        double arrivalTime;
        
        //this car is destined for this node, no need to check further
        if (incoming.getNextLane() == null)
            return false;
        
        //determine its arrival time
        //if the car is in queue, its velocity is 0.0, leading to a divByZeroException.
        if (!incoming.isInQueue())
            arrivalTime = incoming.getDistanceToLaneEnd() / incoming.getVelocity();
        else
            arrivalTime = 0.0;
        
        
        
        double otherCarArrivalTime;
        double arrivalStart;
        double arrivalEnd;
        
        double overlap;
        
        Road priorityRoad = getPriorityRoad();
        
        //am I currently on a priority road?
        boolean mePriority = 
                (
                    priorityRoad != null 
                && 
                    priorityRoad == incoming.getLane().getRoadSegment().getRoad() 
                );

        boolean otherCarPriority;
        
        boolean laneIntersect;
        
        //loop through all other cars
        for (Car car : cars) {
            
            //if its the same car, don't check
            if (car == incoming)
                continue;
            
            //is the other car currently on the priority road?
            otherCarPriority = 
                    (
                        priorityRoad != null 
                    &&
                        priorityRoad == car.getLane().getRoadSegment().getRoad()
                    );
            
            //lets see when the other car will arrive
            otherCarArrivalTime = car.getDistanceToLaneEnd() / car.getVelocity();

            //determine the time overlap we are looking at
            if (!car.isInQueue())
                overlap = car.getVelocity() * OVERLAP_CONSTANT;
            else
                overlap = 0.0;
            
            overlap = Math.max(MIN_OVERLAP, overlap);

            //determine the start time and end time of the overlap timespan
            arrivalStart = otherCarArrivalTime - overlap;
            arrivalEnd = otherCarArrivalTime + overlap;

            //look if there is an overlap
            if (arrivalTime >= arrivalStart && arrivalTime <= arrivalEnd) {
                //there is an overlap!!!111ONE
                
                //theres no use in checking when the other car isnt going anywhere
                if (car.getNextLane() == null)
                    continue;
                
                //check if there is an intersect
                laneIntersect = intersects(incoming.getLane(), incoming.getNextLane(), car.getLane(), car.getNextLane());
                
                if (laneIntersect) {
                
                    //If i am not on a priority road and the other car isnt, he can drive and I have to stop!
                    if (!mePriority && otherCarPriority)
                        return false;
                    
                    if (mePriority == otherCarPriority) {
                        //check if one of the two cars comes from the right for the other
                        boolean otherCarFromRight = (rightDistance(incoming.getLane(), car.getLane()) == 1);
                        boolean meFromRight = (rightDistance(car.getLane(), incoming.getLane()) == 1);

                        //the other car comes from the right, or the other car does not come from the right but it arrives earlier
                        if (otherCarFromRight || (!meFromRight && arrivalTime > arrivalStart))
                            return false;
                    }
                }
            }
            
        }
        
        //if we found no overlapping car that gets priority, the only check that is left
        //before we can drive through is whether the new lane has space for us.
        return incoming.getNextLane().acceptsCarAdd(incoming);
        
    }

    @Override
    void acceptCar(Car incoming) {
        if (incoming.getNextLane() == null || !incoming.getNextLane().acceptsCarAdd(incoming))
            System.err.println("Car did not check correctly if it could join a lane.");

        incoming.getNextLane().addCar(incoming);
    }

    @Override
    public void init(NodeListener listener) {
        super.init(listener);
        
        cars = new ArrayList<Car>();
    }
    
    //this constant is important!!! it is multiplied with the speed of the other cars
    //from a waiting cars perspective. The outcome is the number of seconds the car will
    //take into account as overlap in arrival times.    
    private static final double OVERLAP_CONSTANT = 0.1;
    private static final double MIN_OVERLAP = 1.0;
    private static final double VELOCITY_THRESHOLD = 2.0;

    
    private List<Car> cars;
    @Override
    public void update(double timestep) {
        cars.clear();
        
        for (Lane l : getIncomingLanes()) {
            Car c = l.getFirstCar();
            
            if (c != null)
                cars.add(c);
        }
    }
}
