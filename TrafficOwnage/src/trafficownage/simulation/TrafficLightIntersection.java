package trafficownage.simulation;

import java.util.HashMap;
import java.util.List;
import java.awt.geom.Point2D;

public class TrafficLightIntersection extends Node{
    
    private HashMap<Lane, TrafficLight> traffic_lights; //List with all the lanes the node has.
    private HashMap<TrafficLight, Counting> carsCounted; //List with per trafficlight, how many cars passed.
    private double time; // keeps track of how long you're counting cars
    private int day = 0; // how many days you're counting
    private int hour = 0; //which hour on the day you are
    private static final double NUMBER = 3600000; //an hour in milliseconds
    private List<Road> roads;
    
   
    
    public TrafficLightIntersection(Point2D.Double location) {
        super(location);

        traffic_lights = new HashMap<Lane,TrafficLight>();
        carsCounted = new HashMap<TrafficLight, Counting>();
    }
    
    @Override
    public void init() {
        super.init();

        for (Lane l : getIncomingLanes()) {
            traffic_lights.put(l,new TrafficLight());
            carsCounted.put(traffic_lights.get(l), new Counting());
        }
    }

    //class that has hours and counted cars in that hour
    private class Counting{
        private int[] carsCounted = new int[24]; //every entry stands for an hour
        public Counting(){
            for (int i = 0; i<24; i++){
                carsCounted[i] = 0;
            }
        }

        public void aCarPassed(int hour){
                carsCounted[hour]++;
        }

        public int getHowManyCarsPassed(int hour, int day){
            return carsCounted[hour]/day;
        }
    }

    @Override 
    void acceptCar(Car incoming) {
                // add a car to the node when its passing the traffic light
       carsCounted.get(traffic_lights.get(incoming.getLane())).aCarPassed(hour);
    }

    @Override
    boolean drivethrough(Car incoming) {
        Lane l = incoming.getLane();
        TrafficLight t = traffic_lights.get(l);
        if(t.getCurrentLight() == TrafficLight.GREEN) {
            //let him drive through
            return true;
        }
        else if(t.getCurrentLight() == TrafficLight.YELLOW){
            // if the breaking distance is bigger than the distance to the light: let him pass
            // else let him break
            if (incoming.getDistanceToLaneEnd() <
                    (incoming.getVelocity()/2.0)*(incoming.getVelocity()/incoming.getDriverType().getMaxComfortableDeceleration()))
                return true;
            else
                return false;
        }
        else {
            //dont let him drive through
            return false;
        }
    }

    @Override
    public void update(double timestep) {
        super.update(timestep);
        // TODO Auto-generated method stub
        time += timestep;
        if (time >= NUMBER){
            hour++;
            if (hour >= 24){
                hour =0;
                day++;
            }
            time = (NUMBER-time)+ timestep;
            // do something with the counted carSs
            resetTime();
        }

    }
    
    public int getNumberOfLights(){
        return traffic_lights.size();
    }
    
    public TrafficLight getTrafficLight(int trafficLightNumber){
        return traffic_lights.get(trafficLightNumber);
    }

    public TrafficLight getTrafficLight(Lane l){
        return traffic_lights.get(l);
    }

    public int getCarsPassed(Lane l, int hourOfTheDay){
        return carsCounted.get(traffic_lights.get(l)).getHowManyCarsPassed(hourOfTheDay, day);
    }

    public void resetTime(){
    }    
}
