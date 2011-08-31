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
        setGreen(lanes, MIN_GREEN_TIME);
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

        if (incoming.getNextLane() == null || !incoming.getNextLane().acceptsCarAdd(incoming))
        {
            System.err.println("Car did not check correctly if it could join a lane.");
        }

        incoming.getNextLane().addCar(incoming);
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

    private static final double GREEN_TIME_PER_CAR = 5.0;
    private static final double IGNORE_TRAFFIC_TIME = 10.0;
    private static final double MIN_GREEN_TIME = 10.0;
    private static final double MAX_GREEN_TIME = 120.0;

    public double getDesiredGreenTime(List<Lane> lanes)
    {
        int count = 0;

        double arrivalTime;

        double greenTime = 0.0;
        double maxGreenTime = 0.0;

        for (Lane l : lanes)
        {
            if (!l.hasCars())
            {
                continue;
            }

            if (l.getFirstCar().isInQueue())
            {
                arrivalTime = 0.0;
            }
            else
            {
                arrivalTime = l.getFirstCar().getDistanceToLaneEnd() / l.getFirstCar().getVelocity();
            }

            if (arrivalTime > IGNORE_TRAFFIC_TIME)
            {
                continue;
            }

            greenTime = arrivalTime + (l.getCarCount() * GREEN_TIME_PER_CAR);
            maxGreenTime = Math.max(maxGreenTime, greenTime);
        }

        return maxGreenTime;
    }

    private boolean isCarOnTime(Car car)
    {
        return (car.getDistanceToLaneEnd() / car.getVelocity()) < (greenTime - timePassed);
    }


    private Comparator<List<Lane>> laneSetComparator = new Comparator<List<Lane>>() {

        public int compare(List<Lane> o1, List<Lane> o2) {
            return (int)Math.signum(getDesiredGreenTime(o2) - getDesiredGreenTime(o1));
        }

    };

    private void checkForNewTraffic(boolean mustChange)
    {
        double desiredGreenTime;

        Collections.sort(laneSets, laneSetComparator);

        List<Lane> greenLanes = laneSets.getFirst();

        desiredGreenTime = getDesiredGreenTime(greenLanes);

        //TODO: MAGIC NUMBER!!!!!1111
        //if there is no noticable change and we do not HAVE to change, return
        if (desiredGreenTime < 0.5 && !mustChange)
            return;

        laneSets.poll();

        desiredGreenTime = Math.min(MAX_GREEN_TIME, Math.max(MIN_GREEN_TIME, desiredGreenTime));

        setGreen(greenLanes, desiredGreenTime);

        if (laneSets.isEmpty())
            laneSets.addAll(getLaneSets());
    }

    private boolean checkForNextRoad;

    @Override
    public void update(double timestep)
    {
        super.update(timestep);

        //no lights, no update!
        if (!needsLights)
            return;
       

        //update our local time var
        timePassed += timestep;

        //if there is a green wave, we never check for a next road.
        //if the green wave is off, we might check.
        if (!greenWaveActive)
            checkForNextRoad = true;
        else
            checkForNextRoad = false;


        if (checkForNextRoad && timePassed < greenTime)
        {
            for (Lane l : greenLanes)
            {
                if (l.hasCars() && isCarOnTime(l.getFirstCar()))
                {
                    //current green road is still in use, therefore do not check
                    checkForNextRoad = false;
                    break;
                }
            }
        }

        if (checkForNextRoad)
        {
            // Ok the Wave is over, time for normal work to resume
            greenWaveActive = false;

            //if we are past due, we have to force the change of traffic light
            checkForNewTraffic(timePassed > greenTime);
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
