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
public class FixedTrafficLight extends Node implements TrafficLightInterface {

    private HashMap<RoadSegment,Boolean> trafficLights;
    private List<RoadSegment> roadSegments;
    private int currentLight = 0;
    private double timePassed;
    private boolean overlap;

    private static double GREEN_TIME = 50.0;
    private static final List<Lane> emptyList = new ArrayList<Lane>();

    public FixedTrafficLight(Point2D.Double location) {
        super(location);      
    }

    @Override
    public void init(NodeListener listener) {
        super.init(listener);

        trafficLights = new HashMap<RoadSegment,Boolean>();
        roadSegments = new ArrayList<RoadSegment>();
        currentLight = 0;
        timePassed = 0.0;
        overlap = false;

        RoadSegment rs;

        for (Node n : getSourceNodes()) {
            rs = getRoadSegment(n);
            roadSegments.add(rs);
            trafficLights.put(rs, false);
        }

        setNodeType(Node.TRAFFICLIGHT_NODE);
    }

    public List<Lane> getGreenLanes() {
        if (overlap)
            return emptyList;
        return roadSegments.get(currentLight).getDestinationLanes(this);
    }

    @Override
    boolean drivethrough(Car incoming) {
        Lane l = incoming.getNextLane();
        
        if (l == null)
            return false;
        
        RoadSegment rs = incoming.getLane().getRoadSegment();

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

        if (timePassed > GREEN_TIME && !overlap) {
            for (int i = 0; i < roadSegments.size(); i++) {
                trafficLights.put(roadSegments.get(i),false);
            }
            overlap = true;

        }
        else if (timePassed > GREEN_TIME + 3.0){
            timePassed = 0.0;
            currentLight = (currentLight + 1) % roadSegments.size();
            trafficLights.put(roadSegments.get(currentLight), true);
            overlap = false;
        }
    }

}
