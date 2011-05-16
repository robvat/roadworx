/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.List;

/**
 *
 * @author JonaForce
 */
public class TrafficLight {
    private int delay;//how long the light lasts for
    private double start_time;//time when the lights change to green
    private double global_time;
    private int yellow_time;
    private int duration;
    private String light;//the color of the light
    private List<Lane> destinations;

    public TrafficLight(){
    }

    public void init(int Delay, double starttime, double globaltime, int yellowtime, int Duration){
        delay = Delay;
        start_time = starttime;
        global_time = globaltime;
        yellow_time = yellowtime;
        duration = Duration;
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

    public String getLight(){
        return light;
    }

    public int getDuration(){
        return duration;
    }
    public List<Lane> getDestinationLanes(){
        return destinations;
    }
}
