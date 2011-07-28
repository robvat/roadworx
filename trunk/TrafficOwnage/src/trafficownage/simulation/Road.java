/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Road {
    private RoadSegment startSegment, endSegment;

    private LinkedList<RoadSegment> segments;

    private List<Node> roadNodes;

    private String roadName;
    private boolean vertical;

    public Road(String roadName) {
        this.roadName = roadName;

        segments = new LinkedList<RoadSegment>();
        overallCO2Emission = 0.0;
    }

    public static final int HIGH_PRIORITY = 1;
    public static final int MEDIUM_PRIORITY = 2;
    public static final int LOW_PRIORITY = 3;


    public String getRoadName() {
        return roadName;
    }

    public RoadSegment getFirstSegment() {
        return startSegment;
    }

    public RoadSegment getLastSegment() {
        return endSegment;
    }

    public List<RoadSegment> getSegments() {
        return segments;
    }

    public List<Node> getNodes() {
        return roadNodes;
    }

    public void addLast(RoadSegment segment) {

        if (endSegment != null && startSegment != null) {

            if (segment.getEndNode() == startSegment.getStartNode() && segment.getStartNode() == endSegment.getEndNode()) {
                endSegment.setNextSegment(segment);
                startSegment.setPreviousSegment(segment);
                segment.setNextSegment(startSegment);
                segment.setPreviousSegment(endSegment);
                startSegment = segment;
                endSegment = segment;
            } else {
                endSegment.setNextSegment(segment);
                segment.setPreviousSegment(endSegment);
                segment.setNextSegment(null);
            }
        }

        if (startSegment == null) {
            startSegment = segment;
            startSegment.setNextSegment(null);
            startSegment.setPreviousSegment(null);
        }

        endSegment = segment;

        segments.addLast(segment);
    }

    public void addFirst(RoadSegment segment) {

        if (endSegment != null && startSegment != null) {
            startSegment.setPreviousSegment(segment);
            segment.setNextSegment(startSegment);
            segment.setPreviousSegment(null);
        }

        if (endSegment == null) {
            endSegment = segment;
            endSegment.setNextSegment(null);
            endSegment.setPreviousSegment(null);
        }

        startSegment = segment;

        segments.addFirst(segment);
    }

    public void init() {
        LinkedList<Node> nodes = new LinkedList<Node>();

        RoadSegment rs = startSegment;
        nodes.addLast(rs.getStartNode());

        while (rs != null) {
            rs.init();
            nodes.addLast(rs.getEndNode());
            rs = rs.getNextSegment();
        }

        roadNodes = nodes;
    }

    private double overallCO2Emission;

    public double pollOveralCO2Emission() {
        double tmp = overallCO2Emission;
        overallCO2Emission = 0.0;
        return tmp;
    }

    public void update(double timestep) {

        RoadSegment rs1 = startSegment;
        RoadSegment rs2 = endSegment;


        if (startSegment == endSegment && segments.size() > 1) {
            rs1 = rs1.getNextSegment();
            rs2 = rs2.getPreviousSegment();
        }

        boolean done = false;

        while(!done) {
            updateLanes(timestep, rs1.getEndLanes());
            updateLanes(timestep, rs2.getStartLanes());

            rs1.updateSpeedLimits(timestep);
            rs1.updateCO2Emission(timestep);
            overallCO2Emission += rs1.pollOveralCO2Emission();

            if (rs1 == endSegment && rs2 == startSegment) {
                done = true;
            } else {
                rs1 = rs1.getNextSegment();
                rs2 = rs2.getPreviousSegment();
            }
        }
        
    }

    private void updateLanes(double timestep, List<Lane> lanes) {
        for (Lane l : lanes)
            l.update(timestep);
    }

}
