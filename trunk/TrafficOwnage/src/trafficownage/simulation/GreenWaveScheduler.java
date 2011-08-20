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
public class GreenWaveScheduler {

    private List<GreenWave> greenWaves; //each entry is a list of subsequent roads
    private List<List<Node>> nodeLists; //each entry is a road, made up from nodes

    public GreenWaveScheduler(){

    }

    // in here we need something to determine subsequent roads
    public void init(List<Road> roads){
        nodeLists = new ArrayList<List<Node>>();
        for (Road r : roads) {
                nodeLists.add(r.getNodes());
            }
        
        greenWaves = new ArrayList<GreenWave>();
        for (List<Node> ln : nodeLists){
            GreenWave gv = new GreenWave();
            gv.init(ln);
            greenWaves.add(gv);
        }

    }

    //this class consists of a list of subsequent roads and operators on it.
    private class GreenWave
    {
        private int redNode = 0;
        private int nextNode =0;
        private double counter;
        private static final double OVERLAP_TIME = 3.0;
        private List<Node> ln;
        private List<Double> trafficLightGreen;//list of when a certain traffic light becomes green
        private List<Double> trafficLightRed; //list of when a certain traffic light becomes red

        public void init(List<Node> ln){
            this.ln = ln;
            trafficLightGreen = new ArrayList<Double>();
            trafficLightRed = new ArrayList<Double>();
            counter = 0;            
            double trafficTime = 0.0; //time traffic needs between two node
            for (Node n : ln){
                if (ln.get(0) == n){
                    counter = 0;
                    trafficTime = n.distanceTo(ln.get(1))/n.getRoadSegment(ln.get(1)).getMaxVelocity();
                    trafficLightGreen.add(0.0);
                }
                else
                    trafficLightGreen.add((ln.indexOf(n)*trafficTime) - OVERLAP_TIME);                
                }
            
        }

        //this method initializes the green wave, which means it starts the counter of a green wave
        public void startWave(){
            counter = 0;
            nextNode = 0;
            redNode = 0;
            TrafficLight x = (TrafficLight) ln.get(nextNode);
            double redTime = x.getGreenTime();
            for (Node n: ln){
                trafficLightRed.add(trafficLightGreen.get(ln.indexOf(n)) + redTime + OVERLAP_TIME);
            }
        }


        public void update(double timeStep) {
            System.out.println(timeStep);
            counter += timeStep;
            if (counter >= trafficLightGreen.get(nextNode)){
                TrafficLight x = (TrafficLight) ln.get(nextNode);
                x.setGreen();
                nextNode++;
            }
            if (counter >= trafficLightGreen.get(redNode)){{
                TrafficLight x = (TrafficLight) ln.get(redNode);
                x.setRed();
                redNode++;
            }
        }


    }

}
}
