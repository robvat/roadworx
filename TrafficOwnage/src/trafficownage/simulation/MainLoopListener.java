/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.List;

/**
 *
 * @author Gerrit
 */
public interface MainLoopListener {
    void benchmarkCarAdded(Car car);
    void mapLoaded();
    void nextFrame();
}
