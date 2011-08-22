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

        private int nextNode = 0;
        private double counter;
        private double greenTime; // The greentime at each of the TrafficLights
        private static final double OVERLAP_TIME = 3.0;
        private List<TrafficLight> trafficLightList;

        private List<Double> trafficLightGreen;//list of when a certain traffic light becomes green

        public void init(List<Node> nodeList)
        {
            for(Node p : nodeList)
            {
                if(p instanceof TrafficLight)
                    trafficLightList.add((TrafficLight) p);
                else
                    System.err.print("A non-trafficlight node was found in greenwave");
            }
            trafficLightGreen = new ArrayList<Double>();
            counter = 0;
            double trafficTime = 0.0; //time traffic needs between two node
            for (Node n : nodeList)
            {
                if (nodeList.get(0) == n)
                {
                    counter = 0;
                    trafficTime = n.getRoadSegment(nodeList.get(1)).getLength() / n.getRoadSegment(nodeList.get(1)).getMaxVelocity();
                    trafficLightGreen.add(0.0);
                } else
                {
                    trafficLightGreen.add((nodeList.indexOf(n) * trafficTime) - OVERLAP_TIME);
                }
            }

            // Quicksolve to determine the greentime of the first node (and the nodes after that
            if(nodeList.size() >= 2)
            {
                Road waveRoad = trafficLightList.get(0).getRoadSegment(trafficLightList.get(1)).getRoad();
                greenTime = trafficLightList.get(0).getDesiredGreenTime(waveRoad);
            }
            else
            {
                System.err.print("GWSerr 1: Not enough nodes to have a green wave !!");
            }

        }

        //this method initializes the green wave, which means it starts the counter of a green wave
        public void startWave()
        {
            counter = 0;
            nextNode = 0;
            Road waveRoad  = trafficLightList.get(0).getRoadSegment(trafficLightList.get(1)).getRoad();
            greenTime = trafficLightList.get(0).getDesiredGreenTime(waveRoad);
            
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
                nextNode++;
            }

            
        }
    }
}
