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
public class TrafficLight extends Node implements TrafficLightInterface {

    private List<RoadSegment> roadSegments;
    private List<Lane> activeLights;
    private double trafficLightInterval;
    private int activeLight = 0;

    private double timePassed;

    private HashMap<Lane,TrafficLightListener> sequences;

    public TrafficLight(Point2D.Double location) {
        super(location);
    }

    @Override
    public void init(NodeListener listener) {
        super.init(listener);

        forcements = new ArrayList<Triplet<List<Lane>,Double,Double>>();
        activeLights = new ArrayList<Lane>();
        sequences = new HashMap<Lane,TrafficLightListener>();
        roadSegments = new ArrayList<RoadSegment>();
        activeLight = 0;
        timePassed = 0.0;

        RoadSegment rs;

        for (Node n : getSourceNodes()) {
            rs = getRoadSegment(n);
            roadSegments.add(rs);
        }

        setNodeType(Node.TRAFFICLIGHT_NODE);
    }

    public void registerLanes(List<Lane> lanes, Sequence sequence) {
        for (Lane lane : lanes) {
            if (!sequences.containsKey(lane))
                sequences.put(lane, sequence);
        }
    }
    
    public List<Lane> getGreenLanes() {
        return activeLights;
    }

    @Override
    boolean drivethrough(Car incoming) {
        Lane l = incoming.getNextLane();

        if (l == null)
            return false;

        return activeLights.contains(incoming.getLane()) && l.acceptsCarAdd(incoming);
        
    }


    @Override
    void acceptCar(Car incoming) {
        if (sequences.containsKey(incoming.getCurrentLane()))
            sequences.get(incoming.getCurrentLane()).incrementCarCount();

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

    private List<Triplet<List<Lane>,Double,Double>> forcements;

    public void forceGreen(Road road, List<Lane> lanes, double duration) {
        forcements.add(new Triplet<List<Lane>,Double,Double>(lanes,0.0,duration));

        activeLights.clear();

        changeTrafficLight(road,lanes);
        
    }

    private Road currentRoad; //TODO: it should be possible to let cars from the same road drive

    private void changeTrafficLight(Road newRoad, List<Lane> lanes) {

        if (currentRoad != null &&  currentRoad != newRoad)
            activeLights.clear();

        currentRoad = newRoad;

        for (Lane l : lanes)
            if (lanes.contains(l))
                activeLights.add(l);


        
    }

    @Override
    public void update(double timestep) {
        super.update(timestep);

        timePassed += timestep;

        if (!forcements.isEmpty()) {

            Triplet<List<Lane>,Double,Double> forcement;
            
            for (int i = 0; i < forcements.size(); i++) {
                forcement = forcements.get(i);

                if (forcement.getObject2() >= forcement.getObject3()) {
                    forcements.remove(forcement);
                    i--;
                }  else {
                    forcement.setObject2(forcement.getObject2() + timestep);
                }
            }
        }
        
        if (forcements.isEmpty() && timePassed > trafficLightInterval) {
        	
        	//gets the lane with the longest Qcount in the next avtive road segment.
            int tmp,max = -1;
            
            int currentLight = activeLight;
            
            int i = (currentLight + 1) % roadSegments.size();

            RoadSegment rs;

            while (i != currentLight) {
                rs = roadSegments.get(i);
                if (!forcements.isEmpty() && currentRoad != null && rs.getRoad() != currentRoad)
                    continue;

            	tmp = getLongestQueueCount(roadSegments.get(i));
            	if (tmp > max) {
            		max = tmp;
            		break;
            	}
            	
            	i = (i + 1) % roadSegments.size();
            }

            if (max == 0)
                max = 1;

            trafficLightInterval = 3 * max;//3 seconds for each car can change after with physics formulas..            

            timePassed = 0.0;
            activeLight = i;

            activeLights.clear();

            changeTrafficLight(roadSegments.get(i).getRoad(),roadSegments.get(i).getDestinationLanes(this));

        }
    }

}
