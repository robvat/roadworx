/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Gerrit
 */
public class SpawnNode extends Node {

    private List<Lane> lanes;
//    private double timePassed, spawnInterval;

    public SpawnNode(Point2D.Double location) {
        super(location);

        lanes = new LinkedList<Lane>();
    }

    @Override
    public void init(NodeListener listener) {
        super.init(listener);

        RoadSegment rs;

        for (Node n : getDestinationNodes()) {
            rs = getRoadSegment(n);
            lanes.addAll(rs.getSourceLanes(this));
        }
    }

    @Override
    boolean drivethrough(Car incoming) {
        return false;
    }

    @Override
    void acceptCar(Car incoming) {
    }


    @Override
    public void update(double timestep) {
        super.update(timestep);
    }

}
