package trafficownage.simulation;

import java.util.HashMap;
import java.util.List;
import java.awt.geom.Point2D;

public class TrafficLightIntersection extends Node{
    
    private String light;//the color of the light
    private List<Road> roads_connected; //roads connected to the node 
    private HashMap<Node, Road> destinations;
    private List<Lane> lanes; //List with alles the lanes the node has.
    private List<TrafficLight> traffic_lights;
    private static double time;
    private static final double NUMBER = 3600000; //one hour in milliseconds
    private int carsCounted;
    
   
    
    public TrafficLightIntersection(Point2D.Double location) {
        super(location);
    }

    public void init( Road[] roads, HashMap<Node, Road> destination_roads, List<TrafficLight> Traffic_lights){
        traffic_lights = Traffic_lights;
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
                // add a car to the node when its passing the traffic light
    }

    @Override
    boolean drivethrough(Car incoming) {
        if(light == "green") {
            //let him drive through
        }
        else if(light == "yellow"){
           // get the distance to the light and the velocity -> decide wether to break or not
        }
        else {
            //dont let him drive through
        }
        return false;
    }

    @Override
    void update(double timestep) {
        // TODO Auto-generated method stub
        time += timestep;
        if (time >= NUMBER){
            // do something with the counted cars
            resetTime();
        }

    }

    public void resetTime(){
        time = 0;
        carsCounted = 0;
    }

}
