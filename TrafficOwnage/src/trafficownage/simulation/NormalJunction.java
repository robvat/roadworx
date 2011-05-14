/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;

/**
 *
 * @author Stefan
 */
public class NormalJunction extends Node {

    public NormalJunction(Point2D.Double location){
        super(location);
    }

    @Override
    public boolean drivethrough(Car incoming) {
        // TODO: first check if the junction is clear
        // if so return false, else continue

        Lane incomingLane = incoming.getLane();
        

        return true; // just to get rid of the missing return statement error
    }

    @Override
    void acceptCar(Car incoming) {

    }

    @Override
    void update(double timestep) {
        for (Lane l : getIncomingLanes()) {
            //TODO: here we have to determine who will be first
        }
    }

}
