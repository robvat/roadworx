package trafficownage.simulation;

import java.util.HashMap;
import java.util.List;
import java.awt.geom.Point2D;

public class TrafficLightIntersection extends Node{
    
    private HashMap<Lane, TrafficLight> traffic_lights; //List with all the lanes the node has.
    private double time; // keeps track of how long you're counting cars
    private static final double NUMBER = 3600000; //an hour in milliseconds
    private int carsCounted; // cars counted in running hour
    private int totalCarsCounted; //overall cars counted
    private List<Road> roads;
    
   
    
    public TrafficLightIntersection(Point2D.Double location) {
        super(location);

        traffic_lights = new HashMap<Lane,TrafficLight>();
    }
    
    @Override
    public void init() {
        super.init();

        for (Lane l : getIncomingLanes()) {
            traffic_lights.put(l,new TrafficLight());
        }
    }

    @Override 
    void acceptCar(Car incoming) {
                // add a car to the node when its passing the traffic light
    }

    @Override
    boolean drivethrough(Car incoming) {
        Lane l = incoming.getLane();
        TrafficLight t = traffic_lights.get(l);
        if(t.getCurrentLight() == TrafficLight.GREEN) {
            //let him drive through
        }
        else if(t.getCurrentLight() == TrafficLight.YELLOW){
           // get the distance to the light and the velocity -> decide wether to break or not
            //let the car decide, not the intersection! For now: it is red :P
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
            // do something with the counted carSs
            resetTime();
        }

    }
    
    public int getNumberOfLights(){
        return traffic_lights.size();
    }
    
    public TrafficLight getTrafficLights(int trafficLightNumber){
        return traffic_lights.get(trafficLightNumber);
    }

    public void resetTime(){
    }    
}
