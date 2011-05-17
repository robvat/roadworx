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

    private static final double YELLOW_DURATION = 3.0;

    private double yellow_time;

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

    public void update(double timestep) {
        if (light == YELLOW) {
            if (yellow_time >= YELLOW_DURATION)
                light = RED;
            else
                yellow_time += timestep;
        }
    }
}
