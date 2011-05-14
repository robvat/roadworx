/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;

/**
 *
 * @author Gerrit
 */
public class DummyNode extends Node {
        //TODO: THIS DOES NOT WORK PROPERLY YET DUE TO THE NEW LANE IMPLEMENTATION!e {

    public DummyNode(Point2D.Double location) {
        super(location);
    }
    
    @Override
    boolean drivethrough(Car incoming) {
        return true;
    }

    @Override
    void acceptCar(Car incoming) {
    }

    @Override
    void update(double timestep) {
    }

    @Override
    public void init() {
        
    }

}
