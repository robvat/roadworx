/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.lang.Math;
/**
 *
 * @author frans
 */
public class Roundabout implements Node {

    double size; // circumference in meters
    Road[] roadsConnected; // list of all the roads connected

    public Roundabout(double radius,Road[] roads) {
    this.size = (radius * 2) * Math.PI;
    this.roadsConnected = roads;

    }


    public boolean drivethrough(Car incoming) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void AcceptCar(Car incoming) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void Update() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
