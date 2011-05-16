package trafficownage.simulation;

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
        List<List<Node>> interlist = null;//creates a list of sequences
        //going to have to change depending on how the lights are represented or numbered.
        for (int i = 0; i < intersections.size(); i++) {//selects intersection (i)
            for(int k = 0; k < intersections.get(i).getNumberOfLights(); k++){//gets all the trafficlights that insection(i) has.
                for(int j = 0; j < intersections.size(); j++){//check to see if node(i) is connected to node(j)
                    if(intersections.get(j).getDestinations() == intersections.get(i).getDestinations())
                    interlist.add(intersections.get(j).getDestinations());//creates a list of destinations nodes from each node.
                }
            }
        }
        //now need to link the traffic Lights.
        
        //selects the biggest sequence
        int biggestOne = 0;
        for(int i = 0; i <  interlist.size(); i++){            
            if(biggestOne < interlist.get(i).size() || biggestOne == 0){
                biggestOne = interlist.get(i).size();
            }
        }
        //need to create the graph and check which are the priority roads. then set the sequences
        
        //need to get the traffic light numbers from each individual intersection and set them to the sequences.
    }
    public void setTrafficLightTimes() {
        //sets the times from the sequences. only used for intelligent traffic lights
    }
    public void scheduler() {
        //only going to be used if we have an evolutionary algorithm to schedule need to have traffi desities per road
        //could calculate that after the system runs for awhile and it incrementally changes
        //or have one where it changes a few variable each time and saves the best configuration
        //problem is global max or mins will be hard to search for without proper simulation
    }
    public void Update(double timeStep) {
        global_time += timeStep;
    }
    
}
