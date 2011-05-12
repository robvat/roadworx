package trafficownage.simulation;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightSystem {
    
    private List<Node> nodes;//list of all the nodes with trafficLights
    private List<TrafficLightIntersection> intersections;//list of trafficlightIntersections
   
    private double global_time;//time of the overall system
    private double update_timestep;//a double representing the refresh rate of the TLSystem
    
    public void init(List<Node> aNodes) {
        //gets the initializing varibales from the mainloop()..
        nodes = aNodes;
    }
    public void startSystem() {  
        //creats the trafficlightsIntersections. which creats the trafficlights
        for (int i = 0; i < nodes.size(); i++) {
            intersections.add(new TrafficLightIntersection(((Node) nodes).getLocation()));//not sure if this is what i do ther cast part  
        } 
        setSequences();//sets the sequences of the connected traffic lights                
               
        scheduler();
        setTrafficLightTimes();
    }
    public void setSequences() {
        List<List<TrafficLight>> interlist = null;//creates a list of sequences
        for (int i = 0; i < intersections.size(); i++) {//need to add loop with trafficlight.            
            interlist.add(intersections.get(i).getDestinations());//adds the list of destinations to the list of sequences in the of order of the intersections list.
        }
        //need to create the graph and check which are the priority roads. then set the sequences
        
        //need to get the traffic light numbers from each individual intersection and set them to the sequences.
    }
    public void setTrafficLightTimes() {
        //sets the times from the sequences.
    }
    public void scheduler() {
        
    }
    public void Update(double timeStep) {
        global_time += timeStep;
    }
    
}
