/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * A traffic light which assigns the green-lanes at random
 * @author frans
 */
public class RandomTrafficLight extends Node implements TrafficLightInterface
{

    /** A static final for setting the interval to random */
    public static final int RANDOMINTERVAL = 1764;
    /** If the trafficlight is set to random interval, 
     * this is the max it will go */
    public double maxTime;
    private boolean randomInterval;
    
    private double trafficLightInterval;
    private HashMap<RoadSegment, Boolean> trafficLights;
    private List<RoadSegment> roadSegments;
    private int currentLight = 0;
    private double timePassed;
    private Random randomizer;

    /**
     * Creates a randomly changing trafficlight
     * @param location position of this trafficlight
     * @param trafficLightInterval interval at which this trafficlight switches, can be set to random aswell!-> RandomTrafficLight.RANDOMINTERVAL
     */
    public RandomTrafficLight(Point2D.Double location, double trafficLightInterval)
    {
        super(location);
        this.trafficLightInterval = trafficLightInterval;
        randomInterval = false;
    }

    @Override
    public void init(NodeListener listener)
    {
        super.init(listener);

        trafficLights = new HashMap<RoadSegment, Boolean>();
        roadSegments = new ArrayList<RoadSegment>();
        randomizer = new Random();
        currentLight = 0;
        timePassed = 0.0;
        maxTime = 8.0;

       if(trafficLightInterval == RandomTrafficLight.RANDOMINTERVAL)
       {
           randomInterval = true;
           trafficLightInterval = (randomizer.nextDouble() * maxTime);
       }

        RoadSegment rs;

        for (Node n : getSourceNodes())
        {
            rs = getRoadSegment(n);
            roadSegments.add(rs);
            trafficLights.put(rs, false);
        }

        setNodeType(Node.TRAFFICLIGHT_NODE);
    }

    /**
     * Check if the car needs to stop at the node or if it can go through
     * (at this moment!)
     * @return true = go, false = stop
     */
    @Override
    boolean drivethrough(Car incoming)
    {
        Lane l = incoming.getNextLane();

        if (l == null)
        {
            return false;
        }

        RoadSegment rs = incoming.getLane().getRoadSegment();

        return trafficLights.get(rs) && l.acceptsCarAdd(incoming);
    }

    /**
     * Will put the car on the next lane if it is possible,
     * will throw warning if it cant (should b checked by drivethrough)
     */
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
     * Gets all the lanes turned on green at that moment
     */
    public List<Lane> getGreenLanes()
    {
        return roadSegments.get(currentLight).getDestinationLanes(this);
    }

    @Override
    public void update(double timestep)
    {
        super.update(timestep);

        timePassed += timestep;

        if (timePassed > trafficLightInterval) //it's that time again!
        {
            currentLight = randomizer.nextInt(roadSegments.size());

            for (int i = 0; i < roadSegments.size(); i++)
            {
                trafficLights.put(roadSegments.get(i), i == currentLight);
            }


            if(randomInterval)
                trafficLightInterval = (randomizer.nextDouble() * maxTime);

            timePassed = 0.0;
        }
    }
}
