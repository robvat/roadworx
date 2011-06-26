/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

/**
 *
 * @author Gerrit
 */
public interface MainLoopListener {
    void benchmarkCarAdded(Car car);
    void logMessage(String message);
    void mapLoaded();
    void nextFrame(double timestep);
}
