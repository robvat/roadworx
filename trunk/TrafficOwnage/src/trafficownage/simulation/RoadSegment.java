/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.LinkedList;
import java.util.List;
import trafficownage.util.Averager;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class RoadSegment {
    private Node startNode;
    private Node endNode;

    private LinkedList<Lane> startLanes; //lanes originating from the startNode
    private LinkedList<Lane> endLanes; //lanes originating from the endNode
    private LinkedList<Lane> allLanes; //all lanes

    private RoadSegment nextSegment, previousSegment;

    private SpeedLimitUpdater speedLimitUpdater;

    private double avgCO2EmissionPerKilometer;

    private Road parent;

    private double maxVelocity[];
    private int maxVelocityIndex;
    private double length;

    public RoadSegment(Road parent, double[] maxVelocity, Node startNode, Node endNode) {
        this.startNode = startNode;
        this.endNode = endNode;

        this.maxVelocity = maxVelocity;
        this.maxVelocityIndex = 0;

        this.speedLimitUpdater = new EmissionBasedSpeedLimitUpdater(this);

        this.startLanes = new LinkedList<Lane>();
        this.endLanes = new LinkedList<Lane>();
        this.allLanes = new LinkedList<Lane>();

        this.length = startNode.distanceTo(endNode);
        this.toKilometerRatio = this.length / 1000.0;

        this.co2Averager = new Averager(CO2_MEMORY_SIZE);

        overallCO2Emission = 0.0;

        this.parent = parent;
    }

    public double getMaxVelocity() {
        return maxVelocity[maxVelocityIndex];
    }

    public double getLoweredSpeedLimitRatio() {
        return (maxVelocity.length > 1) ? (double)maxVelocityIndex / (double)(maxVelocity.length - 1) : 0.0;
    }

    public void lowerSpeedLimit() {
        if (maxVelocityIndex < maxVelocity.length - 1)
            maxVelocityIndex++;

        updateSpeedLimits();
    }
    public void raiseSpeedLimit() {
        if (maxVelocityIndex > 0)
            maxVelocityIndex--;

        updateSpeedLimits();
    }

    private void updateSpeedLimits() {
        for (Lane l : allLanes) {
            l.setMaxVelocity(maxVelocity[maxVelocityIndex]);
        }
    }

    public Road getRoad() {
        return parent;
    }

    public Node getStartNode() {
        return startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public List<Lane> getDestinationLanes(Node destination) {
        return destination == startNode ? endLanes : startLanes;
    }

    public List<Lane> getSourceLanes(Node source) {
        return source == startNode ? startLanes : endLanes;
    }

    public void addLeftStartLane(int laneId, boolean ending) {
        addLeftStartLane(laneId, null, ending);
    }

    public void addLeftStartLane(int laneId, List<Node> allowedDirections, boolean ending) {
        addLeftLane(startLanes, laneId, startNode, endNode, allowedDirections, ending);

        if (!startNode.getDestinationNodes().contains(endNode))
            startNode.addDestination(endNode, this);

        if (!endNode.getSourceNodes().contains(startNode))
            endNode.addSource(startNode, this);
    }

    public void addLeftEndLane(int laneId, boolean ending) {
        addLeftEndLane(laneId, null, ending);
    }
    
    public void addLeftEndLane(int laneId, List<Node> allowedDirections, boolean ending) {
        addLeftLane(endLanes, laneId, endNode, startNode, allowedDirections, ending);

        if (!endNode.getDestinationNodes().contains(startNode))
            endNode.addDestination(startNode, this);

        if (!startNode.getSourceNodes().contains(endNode))
            startNode.addSource(endNode, this);
    }

    private void addLeftLane(LinkedList<Lane> laneList, int laneId, Node startNode, Node endNode, List<Node> allowedDirections, boolean ending) {
        Lane newLane = new Lane(laneId, this, startNode, endNode, allowedDirections, maxVelocity[maxVelocityIndex]);

        if (laneList.size() > 0) {
            newLane.setRightNeighbour(laneList.getLast());
            laneList.getLast().setLeftNeighbour(newLane);
        }

        laneList.addLast(newLane);
        allLanes.add(newLane);
    }

    public List<Lane> getStartLanes() {
        return startLanes;
    }

    public List<Lane> getEndLanes() {
        return endLanes;
    }

    public void addRightStartLane(int laneId, boolean ending) {
        addRightStartLane(laneId, null, ending);
    }

    public void addRightStartLane(int laneId, List<Node> allowedDirections, boolean ending) {
        addRightLane(startLanes, laneId, startNode, endNode, allowedDirections);

        if (!startNode.getDestinationNodes().contains(endNode))
            startNode.addDestination(endNode, this);

        if (!endNode.getSourceNodes().contains(startNode))
            endNode.addSource(startNode, this);
    }

    public void addRightEndLane(int laneId, boolean ending) {
        addRightEndLane(laneId, null, ending);
    }

    public void addRightEndLane(int laneId, List<Node> allowedDirections, boolean ending) {
        addRightLane(endLanes, laneId, endNode, startNode, allowedDirections);

        if (!endNode.getDestinationNodes().contains(startNode))
            endNode.addDestination(startNode, this);

        if (!startNode.getSourceNodes().contains(endNode))
            startNode.addSource(endNode, this);
    }

    private void addRightLane(LinkedList<Lane> laneList, int laneId, Node startNode, Node endNode, List<Node> allowedDirections) {
        Lane newLane = new Lane(laneId, this, startNode, endNode, allowedDirections, maxVelocity[maxVelocityIndex]);

        if (laneList.size() > 0) {
            newLane.setLeftNeighbour(laneList.getFirst());
            laneList.getFirst().setRightNeighbour(newLane);
        }

        laneList.addFirst(newLane);
        allLanes.add(newLane);
    }

    public double getLength() {
        return length;
    }

    /**
     * @return the nextSegment
     */
    public RoadSegment getNextSegment() {
        return nextSegment;
    }

    /**
     * @param nextSegment the nextSegment to set
     */
    public void setNextSegment(RoadSegment nextSegment) {
        this.nextSegment = nextSegment;
    }

    /**
     * @return the previousSegment
     */
    public RoadSegment getPreviousSegment() {
        return previousSegment;
    }

    public void init() {
        speedLimitUpdater.init(maxVelocity);
    }

    /**
     * @param previousSegment the previousSegment to set
     */
    public void setPreviousSegment(RoadSegment previousSegment) {
        this.previousSegment = previousSegment;
    }

    public double pollOveralCO2Emission() {
        double tmp = overallCO2Emission;
        overallCO2Emission = 0.0;
        return tmp;
    }

    public double getAverageCo2EmissionPerKilometer() {
        return avgCO2EmissionPerKilometer;
    }


    private Averager co2Averager;
    private double toKilometerRatio;

    private static final double CO2_CYCLE_LENGTH = 60.0;
    private static final int CO2_MEMORY_SIZE = 5;

    private double currentCycleTime;

    private double overallCO2Emission;
    public void updateCO2Emission(double timestep) {
        currentCycleTime += timestep;

        if (currentCycleTime >= CO2_CYCLE_LENGTH) {
            currentCycleTime = 0.0;

            double currentCo2EmissionPerKilometer = 0.0;

            for (Lane l : allLanes)
                currentCo2EmissionPerKilometer += l.pollCo2Emission();

            overallCO2Emission += currentCo2EmissionPerKilometer;

            co2Averager.addTerm(currentCo2EmissionPerKilometer);
            
            avgCO2EmissionPerKilometer = co2Averager.getAverage() / toKilometerRatio;
            
        }
    }

    public void updateSpeedLimits(double timestep) {  
        speedLimitUpdater.update(timestep);
    }


}
