package trafficownage.simulation;

import java.util.HashMap;
import java.util.List;
import java.awt.geom.Point2D;

public class TrafficLightIntersection extends Node{
    
    private String light;//the color of the light
    private List<Road> roads_connected; //roads connected to the node /im guessing
    private HashMap<Node, Road> destinations;
    private List<Lane> cars;//all the cars on each lane
    
    private static int delay;//how long the light lasts for
    private static double start_time;//time when the lights change to green
    private static double global_time;
    private static int yellow_time;
    private static int duration;
    
    public TrafficLightIntersection(Point2D.Double location, Road[] roads, HashMap<Node, Road> destination_roads) {

        super(location);
        destinations = destination_roads;//not sure if i can just do this
        for (int i = 0; i < roads.length; i++) {
            roads_connected.add(roads[i]);
        }//not sure if roads connected counts as possible destinations
        
        for (int i = 0; i < destinations.size(); i++) {
            //number_of_lights = 
        }
    }
    @Override 
    void acceptCar(Car incoming) {
        if(light == "green") {
            //accept
        }
        else {
          //dont accept
        }            
    }

    @Override
    boolean drivethrough(Car incoming) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    void update(double timestep) {
        // TODO Auto-generated method stub
        
    }
    public void setMainLightsTime() {
        
        if(global_time == start_time && global_time > start_time + duration - yellow_time) {
            light = "green";
        }
        else if(global_time == start_time + duration - yellow_time && global_time >= start_time + duration) {
            light = "yellow";
        }            
        else
            light = "red";
        
        start_time += delay;
        
    }
    public void changeTimes() {
        //TODO: will change the times everytime the traffic light systems updates its timers to optimze it
    }
}
