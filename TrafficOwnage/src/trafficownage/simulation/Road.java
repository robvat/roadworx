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

    private String roadName;

    public Road(String roadName) {
        this.roadName = roadName;

        segments = new LinkedList<RoadSegment>();
    }

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

}
