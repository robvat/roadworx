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
    private HashMap<RoadSegment,RoadSegment> oppositeRoadSegments;
    
    private List<Lane> activeLights;
    private double trafficLightInterval;
    private int activeLight = 0;

    private double timePassed;

    public TrafficLight(Point2D.Double location) {
        super(location);
    }

    @Override
    public void init(NodeListener listener) {
        super.init(listener);

        activeLights = new ArrayList<Lane>();

        roadSegments = new ArrayList<RoadSegment>();
        oppositeRoadSegments = new HashMap<RoadSegment,RoadSegment>();

        activeLight = 0;
        timePassed = 0.0;

        RoadSegment rs1,rs2;

        for (Node n1 : getSourceNodes()) {
            rs1 = getRoadSegment(n1);
            roadSegments.add(rs1);

            for (Node n2 : getDestinationNodes()) {
                rs2 = getRoadSegment(n2);

                if (n2 != n1 && rs2.getRoad() == rs1.getRoad()) {
                    oppositeRoadSegments.put(rs1,rs2);
                    break;
                }
            }
        }

        setNodeType(Node.TRAFFICLIGHT_NODE);
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

        if (incoming.getNextLane() == null || !incoming.getNextLane().acceptsCarAdd(incoming))
            System.err.println("Car did not check correctly if it could join a lane.");

        incoming.getNextLane().addCar(incoming);
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

    private Pair<Integer,Double> getQueueInfo(RoadSegment rs) {
    	int count = 0;
        double timeHeadway = 0.0;
    	for (Lane l : rs.getDestinationLanes(this)) {

            if (!l.hasCars())
                continue;

            if (l.getCarCount() > count) {
                count = l.getCarCount();
                timeHeadway = l.getFirstCar().getDistanceToLaneEnd() * l.getFirstCar().getVelocity();
            }
    	}

    	return new Pair<Integer,Double>(count, timeHeadway);
    }

    @Override
    public void update(double timestep) {
        super.update(timestep);

        timePassed += timestep;

        if (timePassed > trafficLightInterval) {
            
            Pair<Integer,Double> next = null;
       
            activeLight = (activeLight + 1) % roadSegments.size();

            RoadSegment rs = roadSegments.get(activeLight);

            next = getQueueInfo(rs);

            if (!activeLights.isEmpty() && next.getObject1() == 0)
                return;


            trafficLightInterval = Math.min(120.0, next.getObject2() + (6 * next.getObject1()));//3 seconds for each car can change after with physics formulas..

            timePassed = 0.0;

            activeLights.clear();

            RoadSegment rs1 = roadSegments.get(activeLight);
            RoadSegment rs2;

            changeTrafficLight(rs1.getRoad(),rs1.getDestinationLanes(this));

            if (oppositeRoadSegments.containsKey(rs1)) {
                rs2 = oppositeRoadSegments.get(rs1);
                changeTrafficLight(rs1.getRoad(),rs2.getDestinationLanes(this));
            }

        }
    }

}
