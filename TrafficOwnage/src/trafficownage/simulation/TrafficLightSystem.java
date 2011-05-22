package trafficownage.simulation;

import java.util.List;
import java.util.ArrayList;


public class TrafficLightSystem {

    
    private List<Node> nodes;//list of all the nodes with trafficLights
    private List<TrafficLightIntersection> intersections;//list of trafficlightIntersections
   
    private double global_time;//time of the overall system
    private double update_timestep;//a double representing the refresh rate of the TLSystem
    
    public void init(ArrayList<Node> aNodes) {
        nodes = aNodes;
        
        //creats the trafficlightsIntersections. which creats the trafficlights
        for (int i = 0; i < nodes.size(); i++) {
            intersections.add(new TrafficLightIntersection(((Node) nodes).getLocation()));//not sure if this is what i do ther cast part  
        } 
        setSequences();//sets the sequences of the connected traffic lights               
               
        scheduler();
        setTrafficLightTimes();
    }
    public void setSequences() {
       /*List<List<Lane>> sequence_list = null;//creates a list of sequences
        for (int i = 0; i < intersections.size(); i++) {//per intersection
            for (int j = 0; j < intersections.get(i).getNumberOfLights(); j++) {//gets the number of trafficlights
                for (int k = 0; k < intersections.size() - 1; k++) {//checks all the other intersections if their destination is this node
                    if(intersections.get(i).getTrafficLights(k).getDestinationLanes() == intersections.get(j).getTrafficLights(k).getDestinationLanes());{
                        sequence_list.add(intersections.get(j).getTrafficLights(k).getDestinationLanes());
                    }
                }                                
            }
        }
        //now need to link the traffic Lights.
        
        //selects the biggest sequence
        int biggestOne = 0;
        for(int i = 0; i <  sequence_list.size(); i++){            
            if(biggestOne < sequence_list.get(i).size() || biggestOne == 0){
                biggestOne = sequence_list.get(i).size();
            }
        }*/
        //need to create the graph and check which are the priority roads. then set the sequences
        
        //need to get the traffic light numbers from each individual intersection and set them to the sequences.
    }
    public void setTrafficLightTimes() {
        //sets the times from the sequences. only used for intelligent traffic lights
    }
    public void scheduler() {
        //only going to be used if we have an evolutionary algorithm to schedule need to have traffic densities per road
        //could calculate that after the system runs for a while and it incrementally changes
        //or have one where it changes a few variable each time and saves the best configuration
        //problem is global max or mins will be hard to search for without proper simulation
    }
    public void Update(double timeStep) {
        global_time += timeStep;
    }
    
}
