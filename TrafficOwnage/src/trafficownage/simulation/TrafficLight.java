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
public class TrafficLight extends Node implements TrafficLightInterface {

    private HashMap<RoadSegment,Boolean> trafficLights;
    private List<RoadSegment> roadSegments;
    private double trafficLightInterval;
    private int activeLight = 0;
    private double timePassed;

    public TrafficLight(Point2D.Double location) {
        super(location);
    }

    @Override
    public void init(NodeListener listener) {
        super.init(listener);

        trafficLights = new HashMap<RoadSegment,Boolean>();
        roadSegments = new ArrayList<RoadSegment>();
        activeLight = 0;
        timePassed = 0.0;

        RoadSegment rs;

        for (Node n : getSourceNodes()) {
            rs = getRoadSegment(n);
            roadSegments.add(rs);
            trafficLights.put(rs, false);
        }

        setNodeType(Node.TRAFFICLIGHT_NODE);
    }
    
    public List<Lane> getGreenLanes() {
        return roadSegments.get(activeLight).getDestinationLanes(this);
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

    private int getLongestQueueCount(RoadSegment rs) {
    	int count = 0;
    	for (Lane l : rs.getDestinationLanes(this)) {
    		count = Math.max(l.getCarCount(), count);
    	}
    	
    	return count;
    }
    
    @Override
	public
    void update(double timestep) {
        super.update(timestep);

        timePassed += timestep;           
        
        if (timePassed > trafficLightInterval) {
        	
        	//gets the lane with the longest Qcount in the next avtive road segment.
            int tmp,max = -1;
            
            int currentLight = activeLight;
            
            int i = (currentLight + 1) % roadSegments.size();
            
            while (i != currentLight) {
            	tmp = getLongestQueueCount(roadSegments.get(i));
            	if (tmp > max) {
            		max = tmp;
            		break;
            	}
            	
            	i = (i + 1) % roadSegments.size();
            }

            trafficLightInterval = 3 * max;//3 seconds for each car can change after with physics formulas..            

            timePassed = 0.0;
            activeLight = i;

            for (i = 0; i < roadSegments.size(); i++) {
                trafficLights.put(roadSegments.get(i), i == activeLight);
            }
        }
    }

}
