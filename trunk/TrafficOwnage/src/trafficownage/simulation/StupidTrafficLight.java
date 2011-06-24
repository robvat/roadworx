/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Gerrit
 */
public class StupidTrafficLight extends Node {

    private HashMap<RoadSegment,Boolean> trafficLights;
    private List<RoadSegment> roadSegments;
    private double trafficLightInterval;
    private int currentLight = 0;
    private double timePassed;

    public StupidTrafficLight(Point2D.Double location, double trafficLightInterval) {
        super(location);
        this.trafficLightInterval = trafficLightInterval;
    }

    @Override
    public void init() {
        super.init();

        trafficLights = new HashMap<RoadSegment,Boolean>();
        roadSegments = new ArrayList<RoadSegment>();
        currentLight = 0;
        timePassed = 0.0;

        RoadSegment rs;

        for (Node n : getSourceNodes()) {
            rs = getRoadSegment(n);
            roadSegments.add(rs);
            trafficLights.put(rs, false);
        }
    }

    @Override
    boolean drivethrough(Car incoming) {
        Lane l = incoming.getNextLane();
        
        if (l == null)
            return false;
        
        RoadSegment rs = l.getRoadSegment();

        return trafficLights.get(rs) && l.acceptsCarAdd(incoming);
    }

    @Override
    void acceptCar(Car incoming) {
        if (incoming.getNextLane() == null || !incoming.getNextLane().acceptsCarAdd(incoming))
            System.err.println("Car did not check correctly if it could join a lane.");

        incoming.getNextLane().addCar(incoming);

    }



    @Override
    public void update(double timestep) {
        super.update(timestep);

        timePassed += timestep;

        if (timePassed > trafficLightInterval) {
            timePassed = 0.0;
            currentLight = (currentLight + 1) % roadSegments.size();

            for (int i = 0; i < roadSegments.size(); i++) {
                trafficLights.put(roadSegments.get(i), i == currentLight);
            }

        }
    }

}
