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
    private List<List<Node>> nodeLists, firstWayRoads, secondWayRoads; //(nodelists)each entry is a road, made up from nodes
    //the firstwayroads and secondwayroads are a division of nodeLists: if firstwayRoads are horizontal, seconwayRoads are veritcal and vice versa.
    private List<GreenWave> firstWayGreenWaves, secondWayGreenWaves; //same as above, but then for greenwaves
    private double timeFirstWayWaves;
    private ArrayList<Double> timeSecondWayWaves;
    private double counter;
    private int nextWave;
    private Boolean reverse;

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
        firstWayRoads = new ArrayList<List<Node>>();
        secondWayRoads = new ArrayList<List<Node>>();
        firstWayGreenWaves = new ArrayList<GreenWave>();
        secondWayGreenWaves = new ArrayList<GreenWave>();

        firstWayRoads.add(nodeLists.get(0));

        for (int i = 1; i < nodeLists.size(); i++) {

            if (hasIntersection(firstWayRoads.get(0), nodeLists.get(i)))
                secondWayRoads.add(nodeLists.get(i));
            else
                firstWayRoads.add(nodeLists.get(i));
        }        

    }

    public boolean hasIntersection(List<Node> roadNodes1, List<Node> roadNodes2){
        for (Node node1 : roadNodes1){
                if (roadNodes2.contains(node1)){
                    return true;
                }
        }
        return false;
    }

    public void scheduleGreenWaves(){
        timeFirstWayWaves = 0.0;
        timeSecondWayWaves = new ArrayList<Double>();
        //for the first secondWayWave; green time +overlapTime.
        //waves after that: index * greenTime + overlapTime.
        //so the scheduler has to find out which secondWayWave is the first-> so sort them!
        secondWayRoads = sort((ArrayList<List<Node>>) secondWayRoads);
        for (List<Node> ln : firstWayRoads){
            GreenWave gv = new GreenWave();
            gv.init(ln);
            firstWayGreenWaves.add(gv);
            greenWaves.add(gv);
        }

        for (List<Node> ln : secondWayRoads){
            GreenWave gv = new GreenWave();
            gv.init(ln);
            secondWayGreenWaves.add(gv);
            greenWaves.add(gv);
        }

        for (int i = 0; i < secondWayRoads.size(); i++){
            timeSecondWayWaves.add((i * secondWayGreenWaves.get(i).getGreenTime()) + GreenWave.OVERLAP_TIME);
        }
    }

    //if reverse = true; the waves go from down to up.
    public void startGreenWaves(Boolean reverse){
        this.reverse = reverse;
        for (GreenWave gv : firstWayGreenWaves){
            gv.startWave(this.reverse);
        }
        counter = 0;
        nextWave = 0;
    }

    public void update(double timeStep){
        counter += timeStep;
        if (counter >= timeSecondWayWaves.get(nextWave)) {
            secondWayGreenWaves.get(nextWave).startWave(reverse);
            nextWave++;
        }
    }

    public ArrayList<List<Node>> sort(ArrayList<List<Node>> original){
        ArrayList<List<Node>> copy = new ArrayList<List<Node>>();
        ArrayList<List<Node>> firstHalf = new ArrayList<List<Node>>();
        ArrayList<List<Node>> secondHalf = new ArrayList<List<Node>>();
        
        if (original.size() > 2){
            for (int i = 0; i < (original.size() / 2); i++){
                firstHalf.add(original.get(i));
            }
            firstHalf = sort(firstHalf);

            for (int i=(original.size() / 2) + 1; i < original.size(); i++){
                secondHalf.add(original.get(i));
            }
            secondHalf = sort(secondHalf);

            if (sort(firstHalf.get(0), secondHalf.get(0)) == 1){
                copy.addAll(firstHalf);
                copy.addAll(secondHalf);
                return copy;
            }
            else if (sort(firstHalf.get(0), secondHalf.get(0)) == -1){
                copy.addAll(secondHalf);
                copy.addAll(firstHalf);
                return copy;
            }
            else
                return original;
        }
        else if (original.size() == 2){
            if (sort(original.get(0),original.get(1)) == 1)
                return original;
            else if (sort(original.get(0),original.get(1)) == -1){
                copy.set(0, original.get(1));
                copy.set(1, original.get(0));
                return copy;
            }
            else
                return original;
        }
        else
            return original;
    }

    //@return: 1 if e1>e2, -1 if e2>e1, 0 if e1 == e2. they are compared by looking at the first nodes.
    // Y-axis is from up to down: low to high!!
    //if x1 smaller than x2(closer to left above), list n1 should be first in list.
    //if y1 smaller than y2(closer to left above), list n1 should be first in list.
    public int sort(List<Node> e1, List<Node> e2){
        if (e1.get(0).getLocation().getX() < e2.get(0).getLocation().getX() || e1.get(0).getLocation().getY() < e2.get(0).getLocation().getY())
            return 1;
        else if (e1.get(0).getLocation().getX() > e2.get(0).getLocation().getX() || e1.get(0).getLocation().getY() > e2.get(0).getLocation().getY())
            return -1;
        else
            return 0;
    }

    //this class consists of a list of subsequent roadsegments (a road) and operators on it.
    private class GreenWave
    {
        private boolean reverse; //if reverse = true, the green wave goes from end to begin
        private Node startNode, endNode;
        private int nextNode = 0; //next node that will be green
        private double counter;
        private double greenTime; // The greentime of each of the TrafficLights
        public static final double OVERLAP_TIME = 3.0; //extra time a car has to arrive at a trafficlight
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
        public void startWave(Boolean reverse)
        {
            this.reverse = reverse;
            counter = 0;
            nextNode = 1; //since the node 0 in the trafficlightlist is a dynamic light, and is used for determining green time
            if (!reverse){
                Road waveRoad = trafficLightList.get(0).getRoadSegment(trafficLightList.get(1)).getRoad();
                greenTime = 10;//greenTime = trafficLightList.get(0).getDesiredGreenTime(waveRoad);
                TrafficLight x = trafficLightList.get(trafficLightList.size() - 1);
                List<Lane> greenLanes = endNode.getRoadSegment((Node) x).getDestinationLanes(x);
                x.setGreen(greenLanes, greenTime);
            }
            else{
                Road waveRoad = trafficLightList.get(trafficLightList.size() - 1).getRoadSegment(trafficLightList.get(trafficLightList.size() - 2)).getRoad();
                greenTime = 10;//greenTime = trafficLightList.get(trafficLightList.size()-1).getDesiredGreenTime(waveRoad);
                TrafficLight x = trafficLightList.get(0);
                List<Lane> greenLanes = startNode.getRoadSegment((Node) x).getDestinationLanes(x);
                x.setGreen(greenLanes, greenTime);
            }

        }

        public void update(double timeStep){
            System.out.println(timeStep);
            counter += timeStep;
            if (counter >= trafficLightGreen.get(nextNode))
            {
                TrafficLight x = trafficLightList.get(nextNode);
                List<Lane> greenLanes = trafficLightList.get(nextNode - 1).getRoadSegment((Node) x).getDestinationLanes(x);
                x.setGreen(greenLanes, greenTime);
                x = trafficLightList.get((trafficLightList.size() - 1) - nextNode);
                greenLanes = trafficLightList.get(((trafficLightList.size() - 1) - nextNode) + 1).getRoadSegment((Node) x).getDestinationLanes(x);
                x.setGreen(greenLanes, greenTime);
                nextNode++;
            }            
        }

        public double getGreenTime(){
            return greenTime;
        }
    }
}
