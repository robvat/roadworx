/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import trafficownage.util.Pair;
import trafficownage.util.Triplet;

/**
 *
 * @author Gerrit
 */
public class Sequence implements TrafficLightListener {
    private TrafficLight[] trafficLights;

    private List<Lane>[] trafficLightLanes;

    private Queue<Integer> throughputs;
    
    private int throughputSum;
    private int cycleRemember;
    
    private double averageThroughput;

    private Road road;

    public Sequence(Road road, TrafficLight[] trafficLights, int cycleRemember) {
        this.road = road;

        this.trafficLights = trafficLights;

        this.cycleRemember = cycleRemember;

        throughputs = new LinkedList<Integer>();
        throughput = 0;

        registerLanes();
    }

    public Road getRoad() {
        return road;
    }

    private void registerLanes() {
        
        RoadSegment rs = null;
        TrafficLight tl1,tl2;

        trafficLightLanes = new List[trafficLights.length];

        List<Lane> currentLanes;

        if (trafficLights.length < 2)
            return;

        for (int i = 0; i < trafficLights.length-1; i++) {
            tl1 = trafficLights[i];
            tl2 = trafficLights[i+1];

            rs = tl1.getRoadSegment(tl2);

            currentLanes = rs.getSourceLanes(tl1);

            if (i == 0) {
                RoadSegment rsStart = null;
                for (Node n : tl1.getSourceNodes()) {
                    rsStart = tl1.getRoadSegment(n);
                    if (rsStart.getNextSegment() != rs && rsStart.getPreviousSegment() != rs)
                        rsStart = null;

                    if (rsStart != null) {
                        List<Lane> startLanes = rsStart.getDestinationLanes(tl1);
                        tl1.registerLanes(startLanes, this);
                        trafficLightLanes[i] = startLanes;
                        break;
                    }
                }
                
            }
            
            tl2.registerLanes(currentLanes, this);
            trafficLightLanes[i+1] = currentLanes;
            
        }
    }

    private boolean running = false;
    private int currentLight = 0;
    private double currentLightEnableTime = 0.0;
    private double currentTime;

    public void start(Triplet<TrafficLight,Double,Double>[] timings) {
        System.out.println("Starting sequence...");
        this.timings = timings;

        currentTime = 0.0;
        currentLight = 0;
        currentLightEnableTime = timings[currentLight].getObject2();
        running = true;
        
    }

    private void enableCurrentLight() {
        trafficLights[currentLight].forceGreen(road, trafficLightLanes[currentLight], timings[currentLight].getObject3());
    }

    private boolean advanceLight() {
        currentLight++;
        if (currentLight < timings.length) {
            currentLightEnableTime = timings[currentLight].getObject2();
            return true;
        } else {
            return false;
        }

    }

    private Triplet<TrafficLight,Double,Double>[] timings;

    public Triplet<TrafficLight,Double,Double>[] determineTimings(double passthroughTime, double overlap) {
        Triplet<TrafficLight,Double,Double>[] timings = new Triplet[trafficLights.length];

        List<Lane> lanes,nextLanes;

        double travelTime = 0.0;
        double currentTime = 0.0;
        double startTime,duration;

        RoadSegment rs = null;

        TrafficLight tl = null;

        for (int i = 0; i < trafficLightLanes.length; i++) {
            lanes = trafficLightLanes[i];
            tl = trafficLights[i];

            startTime = currentTime + travelTime;
            duration = passthroughTime + overlap;

            timings[i] = new Triplet<TrafficLight,Double,Double>(tl,startTime,duration);

            currentTime = startTime;

            if (i < (trafficLightLanes.length - 1)) {
                nextLanes = trafficLightLanes[i+1];
                rs = nextLanes.get(0).getRoadSegment();
                travelTime = rs.getLength() / rs.getMaxVelocity();
            } else if (rs != null) {
                RoadSegment rs2;
                for (Node n : trafficLights[i].getDestinationNodes()) {
                    rs2 = trafficLights[i].getRoadSegment(n);
                    if (rs2.getNextSegment() != rs && rs2.getPreviousSegment() != rs)
                        rs2 = null;

                    if (rs2 != null) {
                        travelTime = rs2.getLength() / rs2.getMaxVelocity();
                        break;
                    }
                }

            }
        }

        return timings;
    }

    public TrafficLight[] getTrafficLights() {
        return trafficLights;
    }

    private int throughput;

    public void update(double timestep) {
        if (!running)
            return;

        currentTime += timestep;

        if (currentTime > currentLightEnableTime) {
            System.out.println("Next light on at " + trafficLights[currentLight].getLocation() + ".");
            enableCurrentLight();
            if (!advanceLight()) {
                running = false;
            }
        }

        
    }

    public void updateThroughput() {
        if (throughputs.size() == cycleRemember)
            throughputSum -= throughputs.poll();

        throughputs.add(throughput);
        throughputSum += (double)throughput;

        throughput = 0;

        averageThroughput = (double)throughputSum / (double)throughputs.size();
    }


    public double getAverageThroughput() {
        return averageThroughput;
    }

    public void incrementCarCount() {
        throughput++;
    }
}
