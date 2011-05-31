/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.ui;

import trafficownage.simulation.Car;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public interface SimulationUpdateListener {
    void carsUpdated();
    void carAdded(Car car);
}
