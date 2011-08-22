/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author BeerBrewer
 */
public class GreenWaveScheduler
{

    private List<GreenWave> greenWaves; //each entry is a list of subsequent roads
    private List<List<Node>> nodeLists; //each entry is a road, made up from nodes

    public GreenWaveScheduler()
    {
    }

    // in here we need something to determine subsequent roads
    public void init(List<Road> roads)
    {
        nodeLists = new ArrayList<List<Node>>();
        for (Road r : roads)
        {
            nodeLists.add(r.getNodes());
        }

        greenWaves = new ArrayList<GreenWave>();
        for (List<Node> ln : nodeLists)
        {
            GreenWave gv = new GreenWave();
            gv.init(ln);
            greenWaves.add(gv);
        }

    }

    //this class consists of a list of subsequent roads and operators on it.
    private class GreenWave
    {
        private boolean reverse; //if reverse = true, the green wave goes from end to begin
        private Node startNode, endNode;
        private int nextNode = 0; //next node that will be green
        private double counter;
        private double greenTime; // The greentime of each of the TrafficLights
        private static final double OVERLAP_TIME = 3.0; //extra time a car has to arrive at a trafficlight
        private List<TrafficLight> trafficLightList;

        private List<Double> trafficLightGreen;//list of when a certain traffic light becomes green

        public void init(List<Node> nodeList)
        {
            startNode = nodeList.get(0);
            endNode = nodeList.get(nodeList.size() - 1);

            for(Node p : nodeList)
            {
                if(p instanceof TrafficLight && p != startNode && p != endNode)
                    trafficLightList.add((TrafficLight) p);
                else
                    System.err.print("A non-trafficlight node was found in greenwave");
            }
            trafficLightGreen = new ArrayList<Double>();
            counter = 0;
            double trafficTime = 0.0; //time traffic needs between two node
            for (Node n : trafficLightList)
            {
                if (trafficLightList.get(0) == n){
                    counter = 0;
                    trafficTime = n.getRoadSegment(trafficLightList.get(1)).getLength() / n.getRoadSegment(nodeList.get(1)).getMaxVelocity();
                    trafficLightGreen.add(0.0);
                } else
                {
                    trafficLightGreen.add((trafficLightList.indexOf(n) * trafficTime) - OVERLAP_TIME);
                }
            }
        }

        //this method initializes the green wave, which means it starts the counter of a green wave
        //@param the boolean stands for if you start the green wave from the begin or the end(reverse = true).
        public void startWave(Boolean Reverse)
        {
            reverse = Reverse;
            counter = 0;
            nextNode = 1; //since the node 0 in the trafficlightlist is a dynamic light, and is used for determining green time
            if (!reverse){
                Road waveRoad = trafficLightList.get(0).getRoadSegment(trafficLightList.get(1)).getRoad();
                greenTime = trafficLightList.get(0).getDesiredGreenTime(waveRoad);
                TrafficLight x = trafficLightList.get(trafficLightList.size()-1);
                List<Lane> greenLanes = endNode.getRoadSegment((Node) x).getDestinationLanes(x);
                x.setGreen(greenLanes, greenTime);
            }
            else{
                Road waveRoad = trafficLightList.get(trafficLightList.size()-1).getRoadSegment(trafficLightList.get(trafficLightList.size()-2)).getRoad();
                greenTime = trafficLightList.get(trafficLightList.size()-1).getDesiredGreenTime(waveRoad);
                TrafficLight x = trafficLightList.get(0);
                List<Lane> greenLanes = startNode.getRoadSegment((Node) x).getDestinationLanes(x);
                x.setGreen(greenLanes, greenTime);
            }

        }

        public void update(double timeStep)
        {
            System.out.println(timeStep);
            counter += timeStep;
            if (counter >= trafficLightGreen.get(nextNode))
            {
                TrafficLight x = trafficLightList.get(nextNode);
                List<Lane> greenLanes = trafficLightList.get(nextNode - 1).getRoadSegment((Node) x).getDestinationLanes(x);
                x.setGreen(greenLanes, greenTime);
                x = trafficLightList.get((trafficLightList.size()-1) - nextNode);
                greenLanes = trafficLightList.get(((trafficLightList.size()-1)-nextNode) +1).getRoadSegment((Node) x).getDestinationLanes(x);
                x.setGreen(greenLanes, greenTime);
                nextNode++;
            }

            
        }
    }
}
