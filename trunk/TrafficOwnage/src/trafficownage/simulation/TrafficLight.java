/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import trafficownage.util.Pair;
import trafficownage.util.Triplet;

/**
 *
 * @author Gerrit
 */
public class TrafficLight extends Node implements TrafficLightInterface
{

    private HashMap<Road, List<Lane>> laneMap;
    private List<Road> roads;
    private double greenTime;
    private double timePassed;
    private int greenRoadIndex;
    private List<Lane> greenLanes;
    private boolean needsLights;
    // Determines if the GreenwaveScheduler can override the lights
    private boolean greenWaveActive;

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
        laneMap = new HashMap<Road, List<Lane>>();
        roads = new ArrayList<Road>();

        greenRoadIndex = 0;

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

        RoadSegment rs1, rs2;

        List<Lane> currentLanes;

        //for all sourcenodes
        for (Node n1 : getSourceNodes())
        {
            currentLanes = new ArrayList<Lane>();

            //get the road segment connected to that source node
            rs1 = getRoadSegment(n1);

            //add all lanes that have this node as destination to the list
            currentLanes.addAll(rs1.getDestinationLanes(this));

            //for all nodes that this node connects to
            for (Node n2 : getDestinationNodes())
            {
                //get the roadsegment to those nodes
                rs2 = getRoadSegment(n2);

                //if the roadsegments aren't the from same node, but they are on the same road,
                //add the lanes to the 
                if (n2 != n1 && rs2.getRoad() == rs1.getRoad())
                {
                    currentLanes.addAll(rs2.getDestinationLanes(this));
                    break;
                }
            }

            if (!roads.contains(rs1.getRoad()))
            {
                roads.add(rs1.getRoad());
                laneMap.put(rs1.getRoad(), currentLanes);
            }
        }

        setNodeType(Node.TRAFFICLIGHT_NODE);

        setGreen(greenRoadIndex, MAX_GREEN_TIME);
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

    private void setGreen(int roadIndex, double greenTime)
    {
        //TODO: it should be possible to let cars from the same road drive
        timePassed = 0.0;
        greenRoadIndex = roadIndex;
        Road road = roads.get(roadIndex);
        greenLanes = laneMap.get(road);
        this.greenTime = greenTime;
    }

    /**
     * Special setGreen for greenwaves! Doesn't do anyting if not part of greenwave!
     * Sets lights greeeeen
     * @param greenLanes Lanes which need to be given green
     * @param greenTime How long the lanes get da green
     */
    public void setGreen(List<Lane> greenLanes, double greenTime)
    {
        if (greenWaveActive)
        {
            this.greenLanes = greenLanes;
            this.greenTime = greenTime;
        }
        else
        {
            System.err.print("Oh noes, you are using special greenWaveFunctions without activating them");
        }
    }
    private static final double GREEN_TIME_PER_CAR = 5.0;
    private static final double IGNORE_TRAFFIC_TIME = 10.0;
    private static final double MAX_GREEN_TIME = 120.0;

    private double getDesiredGreenTime(Road r)
    {
        int count = 0;

        double arrivalTime;

        double greenTime = 0.0;
        double maxGreenTime = 0.0;

        for (Lane l : laneMap.get(r))
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

    private void checkForNewTraffic()
    {
        int checkIndex = (greenRoadIndex + 1) % roads.size();

        Road checkRoad = roads.get(checkIndex);

        double desiredGreenTime;

        while (checkIndex != greenRoadIndex)
        {
            desiredGreenTime = getDesiredGreenTime(checkRoad);

            if (desiredGreenTime > 0.0)
            {
                setGreen(checkIndex, Math.min(MAX_GREEN_TIME, desiredGreenTime));
                return;
            }

            checkIndex = (checkIndex + 1) % roads.size();
            checkRoad = roads.get(checkIndex);
        }
    }
    private boolean checkForNextRoad;

    @Override
    public void update(double timestep)
    {
        super.update(timestep);

        if (!needsLights)
        {
            return;
        }

        timePassed += timestep;

        if (!greenWaveActive)
        {
            checkForNextRoad = true;
        }
        else
        {
            // Greenwave is active so lets not ruin everything
            checkForNextRoad = false;
        }

        if (timePassed < greenTime)
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

        if (checkForNextRoad || timePassed > greenTime)
        {
            // Ok the Wave is over, time for normal work to resume
            greenWaveActive = false;
            
            checkForNewTraffic();
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
