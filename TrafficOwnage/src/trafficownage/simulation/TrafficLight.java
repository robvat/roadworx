/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Gerrit
 */
public class TrafficLight extends Node implements TrafficLightInterface
{
    private static int LANESCOREMETHOD_DISTANCE_FRACTION = 0;
    private static int LANESCOREMETHOD_ARRIVAL_TIME = 1;
    
    public static int LANESCOREMETHOD = LANESCOREMETHOD_DISTANCE_FRACTION;
    
    public static final double IGNORE_TRAFFIC_TIME = 10.0;
    public static final double GREEN_TIME = 60.0; //20,40,60,80,100,120
    public static final double MIN_GREEN_TIME = 5.0;

    public static final double MAX_TRAFFICLIGHT_RECEIVE_DISTANCE = 100.0;
    public static final double MAX_CAR_RECEIVE_DISTANCE = 100.0;
    
    private double greenTime;
    private double timePassed;
    private List<Lane> greenLanes;
    private boolean needsLights;
    
    // Determines if the GreenwaveScheduler can override the lights
    private boolean greenWaveActive;

    private LinkedList<List<Lane>> laneSets;

    public TrafficLight(Point2D.Double location)
    {
        super(location);
        greenWaveActive = false;
    }

    @Override
    public void init(NodeListener listener)
    {
        super.init(listener);

        greenLanes = new ArrayList<Lane>();

        timePassed = 0.0;

        if (getNeighbourNodes().size() <= 2)
        {
            needsLights = false;
            return;
        }
        else
        {
            needsLights = true;
        }

        setNodeType(Node.TRAFFICLIGHT_NODE);

        laneSets = (LinkedList<List<Lane>>)getLaneSets().clone();

        List<Lane> lanes = laneSets.pop();
        setGreen(lanes, GREEN_TIME);
    }

    public List<Lane> getGreenLanes()
    {
        return greenLanes;
    }

    @Override
    boolean drivethrough(Car incoming)
    {
        Lane l = incoming.getNextLane();

        if (l == null)
        {
            return false;
        }
        

        return (!needsLights || greenLanes.contains(incoming.getLane())) && l.acceptsCarAdd(incoming);

    }

    @Override
    void acceptCar(Car incoming)
    {
        Lane l = incoming.getNextLane();

        if (l == null || !l.acceptsCarAdd(incoming))
        {
            System.err.println("Car did not check correctly if it could join a lane.");
        }
        
        if (!incoming.getCurrentLane().getAllowedDirections().contains(l.getEndNode()))
            System.err.println("Car is turning from the wrong lane. Should not happen.");
        
        l.addCar(incoming);
    }

    /**
     * Special setGreen for greenwaves! Doesn't do anyting if not part of greenwave!
     * Sets lights greeeeen
     * @param greenLanes Lanes which need to be given green
     * @param greenTime How long the lanes get da green
     */
    public void setGreen(List<Lane> greenLanes, double greenTime)
    {
        this.greenLanes = greenLanes;
        this.greenTime = greenTime;

        timePassed = 0.0;
    }

    
    
    public double getLaneSetScore(List<Lane> lanes)
    {        
        double laneSetScore = 0.0;

        for (Lane l : lanes)
        {
            if (!l.hasCars())
                continue;

            double score = 0.0;
            
            if (LANESCOREMETHOD == LANESCOREMETHOD_DISTANCE_FRACTION) {
                
                if (l.getFirstCar().getDistanceToLaneEnd() < MAX_TRAFFICLIGHT_RECEIVE_DISTANCE) {
                    Car car = l.getFirstCar();

                    score = 1.0 / car.getDistanceToLaneEnd();

                    while (car.getCarBehind() != null && car.getBack() - car.getCarBehind().getFront() < MAX_CAR_RECEIVE_DISTANCE) {
                        car = car.getCarBehind();

                        score += (1.0 / car.getDistanceToLaneEnd());
                    }
                }
                
            } else if (LANESCOREMETHOD == LANESCOREMETHOD_ARRIVAL_TIME) {
                
                if (l.getFirstCar().getDistanceToLaneEnd() < MAX_TRAFFICLIGHT_RECEIVE_DISTANCE) {                    
                    score = 1.0 / determineArrivalTime(l.getFirstCar());
                }
                
            }
            
            laneSetScore += score;
        }

        return laneSetScore;
    }

    private double determineArrivalTime(Car car) {
        double s = car.getDistanceToLaneEnd();
        double a = car.getAcceleration();
        double v0 = car.getVelocity();
        return
                    s
                /
                        (
                            .5
                    *
                            (
                                v0
                            +
                                Math.sqrt(
                                    (2 * s * a) + (v0 * v0)
                                )
                            )
                        );            
    }
    
    private boolean isCarOnTime(Car car)
    {
//        if (car.getDistanceToLaneEnd() >= MAX_RECEIVE_DISTANCE)
//            return false;
//        else
//            return (car.getDistanceToLaneEnd() / car.getVelocity()) < (greenTime - timePassed);

        if (car.getDistanceToLaneEnd() >= MAX_TRAFFICLIGHT_RECEIVE_DISTANCE) {
            return false;
        } else {
            return determineArrivalTime(car) < (greenTime - timePassed);
        }
    }


    private Comparator<List<Lane>> laneSetComparator = new Comparator<List<Lane>>() {

        public int compare(List<Lane> o1, List<Lane> o2) {
            return (int)Math.signum(getLaneSetScore(o2) - getLaneSetScore(o1));
        }

    };

    private void checkForNewTraffic(boolean mustChange)
    {
        double laneSetScore;
        double highestLaneSetScore = 0;
        List<Lane> highestLaneSet = null;

        Collections.sort(laneSets, laneSetComparator);

        
        for (List<Lane> laneSet : laneSets) {
            laneSetScore = getLaneSetScore(laneSet);
            
            if (highestLaneSet == null || highestLaneSetScore < laneSetScore) {
                highestLaneSetScore = laneSetScore;
                highestLaneSet = laneSet;
            }
        }
        
        if (highestLaneSet == null)
            highestLaneSet = laneSets.poll();
        else
            laneSets.remove(highestLaneSet);

//        while (!laneSets.isEmpty() && highestLaneScore == 0) {
//            greenLanes = laneSets.poll();
//            highestLaneScore = getHighestLaneScore(greenLanes);
//        }
//
        if (highestLaneSet != null && (highestLaneSetScore > 0.0 || mustChange))
            setGreen(highestLaneSet, GREEN_TIME);
        

        if (laneSets.isEmpty())
            laneSets.addAll(getLaneSets());
    }

    //private boolean checkForNextRoad;

    @Override
    public void update(double timestep)
    {
        super.update(timestep);

        //no lights, no update!
        if (!needsLights)
            return;
       
        
        //update our local time var
        timePassed += timestep;
        
        //disable the green wave if necessary
        if (timePassed >= greenTime && greenWaveActive)
            greenWaveActive = false;

        //if there is a green wave, we never check for a next road.
        //if the green wave is off, we might check.
        
        
        boolean greenRoadsEmpty = true;
        //if there is no green wave active and the green time has not passed yet
        //check if all active roads are empty. If so, switch to the next light.
        if (!greenWaveActive && timePassed < greenTime)
        {
            for (Lane l : greenLanes)
            {
                if (l.hasCars() && isCarOnTime(l.getFirstCar()))
                {
                    //current green road is still in use, therefore do not check
                    greenRoadsEmpty = false;
                    break;
                }
            }
        }

        if (timePassed >= greenTime || (timePassed >= MIN_GREEN_TIME && !greenWaveActive && greenRoadsEmpty))
        {
            //if we are past due, we have to force the change of traffic light
            checkForNewTraffic(timePassed >= greenTime);
        }

    }

    /**
     * Actives a green-wave meaning that you must now use the special setGreen
     * method to start/use the green wave and then after the set time it is done
     * (you need to set the time & lanes directly after!)
     */
    public void activateGreenWave()
    {
        greenWaveActive = true;
    }

    /**
     * Returns wether this trafficlight is currently busy with a greenwave
     * thus he is not actively selecting new roads which can be green
     */
    public boolean isBusyWithGreenWave()
    {
        return greenWaveActive;
    }

    /**
     * Check how long the current green lanes have green light
     */
    public double getGreenTime()
    {

        return greenTime;
    }
}
