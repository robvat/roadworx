/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public interface CarListener {
    void reachedDestination(Car car, Node destination);
    void positionChanged(Car car);
}
