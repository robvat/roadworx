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
public abstract class SpeedLimitUpdater {

    private List<Lane>[] laneLists;

    private RoadSegment rs;
    private double maxAllowedVelocity;

    public SpeedLimitUpdater(RoadSegment rs) {
        this.rs = rs;        
    }

    public void init(double maxAllowedVelocity) {
        this.maxAllowedVelocity = maxAllowedVelocity;

        this.laneLists = new List[2];
        this.laneLists[0] = rs.getStartLanes();
        this.laneLists[1] = rs.getEndLanes();
    }

    protected double getMaxAllowedVelocity() {
        return maxAllowedVelocity;
    }

    protected List<Lane>[] getLaneLists() {
        return laneLists;
    }

    protected void updateSpeedLimits(List<Lane> lanes, double multiplier) {
        for (Lane lane : lanes)
            lane.setMaxVelocity(Math.min(lane.getMaxVelocity() * multiplier, maxAllowedVelocity));
        
    }

    public abstract void update(double timestep);
}
