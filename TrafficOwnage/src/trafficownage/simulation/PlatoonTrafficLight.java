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
 * @author BeerBrewer
 */
public class PlatoonTrafficLight extends Node implements TrafficLightInterface {
    private HashMap<Road,List<Lane>> laneMap;

    private List<Road> roads;
    
    private double greenTime;

    private double timePassed;

    private int greenRoadIndex;
    private List<Lane> greenLanes;

    private boolean needsLights;
    
    private static final double GREEN_TIME_PER_CAR = 5.0;
    private static final double IGNORE_TRAFFIC_TIME = 10.0;
    private static final double MAX_GREEN_TIME = 120.0;

    private static final int CAR_TRESHOLD = 7; // minimal cars waiting in front of trafficlight for it to turn green
    private static final double MIN_GREEN_TIME = 20.0; // minimal time light needs to stay green. 
    private static final double MAX_TIME_ARRIVAL_CAR = 10.0; // light stays green, as long as car arrives within this time limit.
    private static final int MAX_CARS = 24; // Maximum number of cars to drive through the green light.

    public PlatoonTrafficLight(Point2D.Double location){
        super(location);
    }
    
    @Override
    public void init(NodeListener listener){
        super.init(listener);

        greenLanes = new ArrayList<Lane>();
        laneMap = new HashMap<Road,List<Lane>>();
        roads = new ArrayList<Road>();

        greenRoadIndex = 0;

        timePassed = 0.0;

        if (getNeighbourNodes().size() <= 2) {
            needsLights = false;
            return;
        } else {
            needsLights = true;
        }
        RoadSegment rs1,rs2;

        List<Lane> currentLanes;

        for (Node n1 : getSourceNodes()) {
            currentLanes = new ArrayList<Lane>();

            rs1 = getRoadSegment(n1);

            currentLanes.addAll(rs1.getDestinationLanes(this));

            for (Node n2 : getDestinationNodes()) {
                rs2 = getRoadSegment(n2);

                if (n2 != n1 && rs2.getRoad() == rs1.getRoad()) {
                    currentLanes.addAll(rs2.getDestinationLanes(this));
                    break;
                }
            }

            if (!roads.contains(rs1.getRoad())) {
                roads.add(rs1.getRoad());
                laneMap.put(rs1.getRoad(),currentLanes);
            }
        }

        setNodeType(Node.TRAFFICLIGHT_NODE);

        setGreen(greenRoadIndex, MAX_GREEN_TIME);
    }
    
   
    public boolean drivethrough(Car incoming){
         Lane l = incoming.getNextLane();

        if (l == null)
            return false;

        return (!needsLights || greenLanes.contains(incoming.getLane())) && l.acceptsCarAdd(incoming);
    }
        
     @Override
    void acceptCar(Car incoming) {
        if (incoming.getNextLane() == null || !incoming.getNextLane().acceptsCarAdd(incoming))
            System.err.println("Car did not check correctly if it could join a lane.");

        incoming.getNextLane().addCar(incoming);
    }


    private void setGreen(int roadIndex, double greenTime) {
        //TODO: it should be possible to let cars from the same road drive
        timePassed = 0.0;
        greenRoadIndex = roadIndex;
        Road road = roads.get(roadIndex);
        greenLanes = laneMap.get(road);
        this.greenTime = greenTime;
    }

    // Add that light will stay green, as long as car arrives in the green time (or five seconds?)
    private double getDesiredGreenTime(Road r) {
    	int count = 0;

        double arrivalTime;

        double greenTime = 0.0;
        double maxGreenTime = 0.0;

    	for (Lane l : laneMap.get(r)) {
            if (!l.hasCars())
                continue;

            if (l.getFirstCar().isInQueue())
                arrivalTime = 0.0;
            else
                arrivalTime = l.getFirstCar().getDistanceToLaneEnd() / l.getFirstCar().getVelocity();

            if (arrivalTime > IGNORE_TRAFFIC_TIME)
                continue;

            greenTime = arrivalTime + (l.getCarCount() * GREEN_TIME_PER_CAR);
            maxGreenTime = Math.max(maxGreenTime,greenTime);
    	}

        return maxGreenTime;
    }

    private boolean isCarOnTime(Car car) {
        return (car.getDistanceToLaneEnd() / car.getVelocity()) < (greenTime - timePassed);
    }


    private void checkForNewTraffic() {
        int checkIndex = (greenRoadIndex + 1) % roads.size();

        Road checkRoad = roads.get(checkIndex);

        double desiredGreenTime;

        while (checkIndex != greenRoadIndex) {
            desiredGreenTime = getDesiredGreenTime(checkRoad);

            if (desiredGreenTime > 0.0) {
                setGreen(checkIndex, Math.min(MAX_GREEN_TIME,desiredGreenTime));
                return;
            }

            checkIndex = (checkIndex + 1) % roads.size();
            checkRoad = roads.get(checkIndex);
        }
    }


    private boolean checkForNextRoad;
    @Override
    public void update(double timestep) {
        super.update(timestep);

        if (!needsLights)
            return;

        timePassed += timestep;

        checkForNextRoad = true;

        if (timePassed < greenTime) {

            for (Lane l : greenLanes) {
                if (l.hasCars() && isCarOnTime(l.getFirstCar())) {
                    //current green road is still in use, therefore do not check
                    checkForNextRoad = false;
                    break;
                }
            }
        }

        if (checkForNextRoad || timePassed > greenTime)
                checkForNewTraffic();

    }
         

    public List<Lane> getGreenLanes() {
        return greenLanes;
    }

}
