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
    public static final int GREEN = 0;
    public static final int YELLOW = 1;
    public static final int RED = 2;

    private static final double YELLOW_DURATION = 3.0; //how long a light stays yellow

    private double yellow_time; // how long the light has been yellow

    private int light;//the color of the light    

    public TrafficLight(){
    }
    

    public int getCurrentLight() {
        return light;
    }

    public void setYellow(){
        light = YELLOW;

        yellow_time = 0.0;
    }

    public void setGreen(){
        light = GREEN;
    }

    // updates the trafficlight: when the traffic light is on yellow it will
    //update the time and if the yellow light duration has passed, turns the
    //light to red.
    public void update(double timestep) {
        if (light == YELLOW) {
            if (yellow_time >= YELLOW_DURATION)
                light = RED;
            else
                yellow_time += timestep;
        }
    }
}
