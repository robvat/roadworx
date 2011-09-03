/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.*;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class DrivethroughNode extends Node {

    public DrivethroughNode(Point2D.Double location) {
        super(location);
    }

    @Override
    boolean drivethrough(Car incoming) {
        Lane lane = getLaneMapping(incoming.getLane());
        
        if (lane == null || (lane != null && lane.getLastCar() != null && lane.getLastCar().getBack() < incoming.getLength()))
            return false;
        else
            return true;
    }

    @Override
    void acceptCar(Car incoming) {
        
        if (incoming.getNextLane() == null || !incoming.getNextLane().acceptsCarAdd(incoming))
        {
            System.err.println("Car did not check correctly if it could join a lane.");
        }
        
        incoming.getNextLane().addCar(incoming);
    }

    @Override
    public void update(double timestep) {
        super.update(timestep);

    }
}
