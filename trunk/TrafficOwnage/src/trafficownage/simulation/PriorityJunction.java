
package trafficownage.simulation;

import java.awt.geom.Point2D;

/**
 *
 * @author frans
 */
public class PriorityJunction extends Node {

    public PriorityJunction(Point2D.Double location){
        super(location);
    }

    @Override
    boolean drivethrough(Car incoming)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void acceptCar(Car incoming)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
