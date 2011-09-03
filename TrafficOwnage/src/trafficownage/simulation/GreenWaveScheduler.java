/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author BeerBrewer
 */
public class GreenWaveScheduler
{
    private HashMap<TrafficLight, GreenWaveTrafficLight> trafficLightMap;
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
        trafficLightMap = new HashMap<TrafficLight, GreenWaveTrafficLight>();
        
        initTrafficLights();
        
        initGreenWaves();
    }
    
    public void update(double simulatedTime) {
        //System.out.println("OI!");
    }
    
    private void initTrafficLights() {
        //go through all roads
        for (Road r : roads) {
            //go through all nodes
            for (Node n : r.getNodes()) {
                //if n is a traffic light
                if (n instanceof TrafficLight) {
                    TrafficLight t = (TrafficLight) n;
                    //add it to our map if not yet present
                    if (!trafficLightMap.containsKey(t))
                        trafficLightMap.put(t, new GreenWaveTrafficLight(t));
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

        List<Node> primaryWave;
        List<Node> secondaryWave;

        nodes = r.getNodes();

        Node primaryNode;
        primaryWave = null;
        
        Node secondaryNode;
        secondaryWave = null;

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
                if (primaryWave == null)
                    primaryWave = new ArrayList<Node>();
                
                primaryWave.add(primaryNode);
                   
            } else {
                //no trafficlight: there is a different intersection inbetween.
                if (primaryWave != null) {
                    greenWaves.add(new GreenWave(primaryWave));
                    primaryWave = null;
                }
            }
            
            //check if the node is a trafficlight
            if (secondaryNode instanceof TrafficLight || secondaryNode instanceof DrivethroughNode) {
                //primaryNode is a trafficlight
                //check if we are currently working on a greenwave. if not,
                //make a new one
                if (secondaryWave == null)
                    secondaryWave = new ArrayList<Node>();
                
                secondaryWave.add(secondaryNode);
                   
            } else {
                //no trafficlight: there is a different intersection inbetween.
                if (secondaryWave != null) {
                    greenWaves.add(new GreenWave(secondaryWave));
                    secondaryWave = null;
                }
            }

            //in/decrement
            i++;
            j--;
        }
    }
    
    
    private class GreenWaveTrafficLight {
        private TrafficLight trafficLight;
        
        public GreenWaveTrafficLight(TrafficLight trafficLight) {
            this.trafficLight = trafficLight;
        }
        
    }
    
    private class GreenWave {
        private GreenWaveTrafficLight firstTrafficLight;
        
        private GreenWaveTrafficLight[] trafficLights;
        
        private Double[] distances;
        
        public GreenWave(List<Node> nodes) {
            init(nodes);
        }
        
        private void update(double simulatedTime) {
            
        }
        
        private void init(List<Node> nodes) {
            ArrayList<GreenWaveTrafficLight> trafficLights = new ArrayList<GreenWaveTrafficLight>();
            ArrayList<Double> distances = new ArrayList<Double>();
            
            GreenWaveTrafficLight greenWaveTrafficLight;
            
            Node previousNode = null;
            double distance = 0.0;
            
            for (Node node : nodes) {
                if (previousNode != null) 
                    distance += node.getRoadSegment(previousNode).getLength();
                
                if (node instanceof TrafficLight) {
                    greenWaveTrafficLight = trafficLightMap.get((TrafficLight)node);                    
                    
                    if (firstTrafficLight == null)
                        firstTrafficLight = greenWaveTrafficLight;
                    
                    trafficLights.add(greenWaveTrafficLight);
                    distances.add(distance);
                    distance = 0.0;                    
                }
                
                previousNode = node;
            }
            
            this.distances = distances.toArray(new Double[0]);
            this.trafficLights = trafficLights.toArray(new GreenWaveTrafficLight[0]);
        }
        
        
    }
}
