/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import trafficownage.util.StringFormatter;
import trafficownage.util.Triplet;

/**
 *
 * @author BeerBrewer
 */
public class GreenWaveScheduler
{
    
    public static final boolean ENABLED = true;
    
    public final static int QUEUE_THRESHOLD = 2;
    public final static double GREENTIME_OVERLAP = 6.0;
    public final static double GREENWAVE_COOLDOWN_TIME = 15.0;
    
    private HashMap<TrafficLight, GreenWaveTrafficLight> greenWaveTrafficLightMap;
    private List<GreenWaveTrafficLight> greenWaveTrafficLights;
    private List<Road> roads;
    private List<GreenWave> greenWaves;
    
    public GreenWaveScheduler() {
        roads = new ArrayList<Road>();
    }
    
    public void addGreenWaveRoad(Road road) {
        roads.add(road);
    }
    public void addGreenWaveRoads(Collection<Road> roads) {
        this.roads.addAll(roads);
    }
    
    public void init() {
        if (!ENABLED)
            return;
        
        greenWaveTrafficLightMap = new HashMap<TrafficLight, GreenWaveTrafficLight>();
        
        greenWaveTrafficLights = new ArrayList<GreenWaveTrafficLight>();
        
        initTrafficLights();
        
        initGreenWaves();
    }
    
    public void update(double simulatedTime) {
        if (!ENABLED)
            return;
        
        for (GreenWave greenWave : greenWaves)
            greenWave.update(simulatedTime);
        
        for (GreenWaveTrafficLight greenWaveTrafficLight : greenWaveTrafficLights)
            greenWaveTrafficLight.update(simulatedTime);
    }
    
    private void initTrafficLights() {
        GreenWaveTrafficLight greenWaveTrafficLight;
        
        //go through all roads
        for (Road r : roads) {
            //go through all nodes
            for (Node n : r.getNodes()) {
                //if n is a traffic light
                if (n instanceof TrafficLight) {
                    TrafficLight t = (TrafficLight) n;
                    //add it to our map if not yet present
                    if (!greenWaveTrafficLightMap.containsKey(t)) {
                        greenWaveTrafficLight = new GreenWaveTrafficLight(t);
                        greenWaveTrafficLightMap.put(t, greenWaveTrafficLight);
                        greenWaveTrafficLights.add(greenWaveTrafficLight);
                    }
                }
            }
        }
    }
    
    private void initGreenWaves() {
        greenWaves = new ArrayList<GreenWave>();   
        
        for (Road r : roads) 
            constructGreenWavesFromRoad(r);
        
    }
    
    private void constructGreenWavesFromRoad(Road r) {
        List<Node> nodes;
        
        int i;
        int j;

        List<Node> primaryWaveNodes;
        List<Node> secondaryWaveNodes;
        
        GreenWave primaryWave;
        GreenWave secondaryWave;

        nodes = r.getNodes();

        Node primaryNode;
        primaryWaveNodes = null;
        
        Node secondaryNode;
        secondaryWaveNodes = null;

        i = 0;
        j = nodes.size() - 1;

        while (j >= 0) {
            primaryNode = nodes.get(i);
            secondaryNode = nodes.get(j);

            //check if the node is a trafficlight
            if (primaryNode instanceof TrafficLight || primaryNode instanceof DrivethroughNode) {
                //primaryNode is a trafficlight
                //check if we are currently working on a greenwave. if not,
                //make a new one
                if (primaryWaveNodes == null)
                    primaryWaveNodes = new ArrayList<Node>();
                
                primaryWaveNodes.add(primaryNode);
                   
            } else {
                //no trafficlight: there is a different intersection inbetween.
                if (primaryWaveNodes != null) {
                    
                    primaryWave = new GreenWave();
                    
                    if (primaryWave.init(primaryWaveNodes))
                        greenWaves.add(primaryWave);
                    
                    primaryWaveNodes = null;
                }
            }
            
            //check if the node is a trafficlight
            if (secondaryNode instanceof TrafficLight || secondaryNode instanceof DrivethroughNode) {
                //primaryNode is a trafficlight
                //check if we are currently working on a greenwave. if not,
                //make a new one
                if (secondaryWaveNodes == null)
                    secondaryWaveNodes = new ArrayList<Node>();
                
                secondaryWaveNodes.add(secondaryNode);
                   
            } else {
                //no trafficlight: there is a different intersection inbetween.
                if (secondaryWaveNodes != null) {
                    secondaryWave = new GreenWave();
                    
                    if (secondaryWave.init(secondaryWaveNodes))
                        greenWaves.add(secondaryWave);
                    
                    secondaryWaveNodes = null;
                }
            }

            //in/decrement
            i++;
            j--;
        }
    }
    
    
    
    
    private class GreenWaveTrafficLight {
        
        private final Comparator<Reservation> RESERVATION_COMPARATOR = new Comparator<Reservation>() {
            public int compare(Reservation o1, Reservation o2) {
                return (int)Math.signum(o2.startTime - o1.startTime);
            }
        };
        
        private TrafficLight trafficLight;
        
        private List<Reservation> reservations;
        
        public GreenWaveTrafficLight(TrafficLight trafficLight) {
            this.trafficLight = trafficLight;
            reservations = new ArrayList<Reservation>();
        }
        
        private boolean canSchedule(List<Lane> laneSet, double startTime, double endTime) {
            for (Reservation reservation : reservations) 
                if (reservation.intersects(laneSet, startTime, endTime))
                    return false;
            
            return true;            
        }
        private boolean schedule(List<Lane> laneSet, double startTime, double endTime) {
            
            for (Reservation reservation : reservations) 
                if (reservation.intersects(laneSet, startTime, endTime))
                    return false;
            
            reservations.add(new Reservation(laneSet, startTime, endTime));
                        
            Collections.sort(reservations, RESERVATION_COMPARATOR);
            
            return true;
        }
        
        public void update(double simulatedTime) {
            if (reservations.isEmpty())
                return;
            
            if (simulatedTime >= reservations.get(0).startTime) {
                Reservation reservation = reservations.get(0);
                
                trafficLight.activateGreenWave(reservation.timeSpan, reservation.laneSet);
                reservations.remove(0);
            }
        }
        
        private class Reservation {
            private double startTime, endTime, timeSpan;
            private List<Lane> laneSet;

            public Reservation(List<Lane> laneSet, double startTime, double endTime) {
                this.laneSet = laneSet;
                this.startTime = startTime;
                this.endTime = endTime;
                this.timeSpan = endTime - startTime;
            }

            private boolean between(double time, double startTime, double endTime) {
                return (time >= startTime && time <= endTime);
            }

            public boolean intersects(List<Lane> laneSet, double startTime, double endTime) {
                double myStartTime = this.startTime;
                double myEndTime = this.endTime;
                
                List<Lane> myLaneSet = this.laneSet;

                if (between(startTime, myStartTime - GREENWAVE_COOLDOWN_TIME, myEndTime + GREENWAVE_COOLDOWN_TIME) || between(endTime, myStartTime - GREENWAVE_COOLDOWN_TIME, myEndTime + GREENWAVE_COOLDOWN_TIME) || between(myStartTime, startTime - GREENWAVE_COOLDOWN_TIME, endTime + GREENWAVE_COOLDOWN_TIME) || between(myEndTime, startTime - GREENWAVE_COOLDOWN_TIME, endTime + GREENWAVE_COOLDOWN_TIME))
                    if (!trafficLight.canEnableLaneSetsSimultaneously(myLaneSet, laneSet))
                        return true;
                

                return false;
            }
        }
        
    }
    
    private class GreenWave {
            
        private GreenWaveTrafficLight firstTrafficLight;
        private List<Lane> firstTrafficLightLanes;
        
        private GreenWaveTrafficLight[] trafficLights;        
        private Double[] timeDistances;
        private RoadSegment[] roadSegments;
        
        private boolean active;
        
        public GreenWave() {
        }
        
        private void update(double simulatedTime) {
            if (active) 
                checkForNewWave(simulatedTime);
        }
        
        private void checkForNewWave(double simulatedTime) {
            boolean potentialWave = false;
            
            for (Lane lane : firstTrafficLightLanes) {
                if (lane.hasCars() && (lane.getQueueCount() > 0 || firstTrafficLight.trafficLight.determineArrivalTime(lane.getFirstCar()) >= GREENTIME_OVERLAP))
                        potentialWave = true;
                    
            }
            
            if (potentialWave) {
                tryScheduleGreenWave(simulatedTime);
            }
        }
        
        private void tryScheduleGreenWave(double simulatedTime) {
            double greenTime = TrafficLight.GREEN_TIME;
            
            double currentTime = 0.0;
            
            int i;
            GreenWaveTrafficLight previousTrafficLight = firstTrafficLight;
            Node previousNode = previousTrafficLight.trafficLight;
            
            GreenWaveTrafficLight currentTrafficLight;
            Node currentNode;
            
            RoadSegment roadSegment;
            
            double timeDistance;
            double startTime, endTime;
            
            List<Lane> laneSet;
            
            Triplet<List<Lane>,Double,Double>[] reservations = new Triplet[trafficLights.length];
            
            boolean canSchedule = firstTrafficLight.canSchedule(firstTrafficLightLanes, simulatedTime, simulatedTime + greenTime);
            currentTime = greenTime;
            
            if (canSchedule) {
                for (i = 0; i < trafficLights.length; i++) {
                    roadSegment = roadSegments[i];
                    currentTrafficLight = trafficLights[i];
                    currentNode = currentTrafficLight.trafficLight;

                    timeDistance = timeDistances[i];

                    startTime = currentTime + timeDistance - GREENTIME_OVERLAP;
                    endTime = currentTime + timeDistance + greenTime;

                    laneSet = roadSegment.getDestinationLanes(currentNode);
                    
                    reservations[i] = new Triplet<List<Lane>,Double,Double>(laneSet, simulatedTime + startTime, simulatedTime + endTime);
                    
                    if (!currentTrafficLight.canSchedule(laneSet, simulatedTime + startTime, simulatedTime + endTime)) {
                        canSchedule = false;
                        break;
                    }

                    currentTime += timeDistance + greenTime;
                    previousTrafficLight = currentTrafficLight;
                    previousNode = currentNode;
                }
            }
            
            if (canSchedule) {
//                System.out.println();
//                System.out.println("Scheduling green wave: ");
                
                firstTrafficLight.schedule(firstTrafficLightLanes, simulatedTime, simulatedTime + greenTime);
//                System.out.println(firstTrafficLight.trafficLight.toString() + ": " + StringFormatter.getTimeString(simulatedTime) + " until " + StringFormatter.getTimeString(simulatedTime + greenTime));
                
                Triplet<List<Lane>,Double,Double> reservation;
                
                for (i = 0; i < trafficLights.length; i++) {
                    reservation = reservations[i];
                    trafficLights[i].schedule(reservation.getObject1(), reservation.getObject2(), reservation.getObject3());
//                    System.out.println(trafficLights[i].trafficLight.toString() + ": " + StringFormatter.getTimeString(reservation.getObject2()) + " until " + StringFormatter.getTimeString(reservation.getObject3()));
                }
            }   
        }
        
        private boolean init(List<Node> nodes) {
            ArrayList<GreenWaveTrafficLight> trafficLights = new ArrayList<GreenWaveTrafficLight>();
            ArrayList<Double> timeDistances = new ArrayList<Double>();
            ArrayList<RoadSegment> roadSegments = new ArrayList<RoadSegment>();
            
            GreenWaveTrafficLight greenWaveTrafficLight;
            
            Node previousNode = null;
            RoadSegment currentSegment;
            double timeDistance = 0.0;
            
            boolean addNextNode = false;
            
            for (Node node : nodes) {
                
                if (previousNode != null) {
                    
                    if (addNextNode)
                        roadSegments.add(previousNode.getRoadSegment(node));
                    
                    currentSegment = node.getRoadSegment(previousNode);
                    timeDistance += currentSegment.getLength() / currentSegment.getMaxVelocity();
                }
                
                if (node instanceof TrafficLight) {
                    greenWaveTrafficLight = greenWaveTrafficLightMap.get((TrafficLight)node);                    
                    
                    if (firstTrafficLight == null && previousNode != null) {
                        firstTrafficLight = greenWaveTrafficLight;
                        firstTrafficLightLanes = node.getRoadSegment(previousNode).getDestinationLanes(node);
                    } else {
                        trafficLights.add(greenWaveTrafficLight);
                        timeDistances.add(timeDistance);
                        addNextNode = true;
                    }
                    
                    timeDistance = 0.0;
                }
                
                previousNode = node;
            }
            
            this.timeDistances = timeDistances.toArray(new Double[0]);
            this.trafficLights = trafficLights.toArray(new GreenWaveTrafficLight[0]);
            this.roadSegments = roadSegments.toArray(new RoadSegment[0]);
                
            return (firstTrafficLight != null && !trafficLights.isEmpty());
        }
        
        
    }
}
