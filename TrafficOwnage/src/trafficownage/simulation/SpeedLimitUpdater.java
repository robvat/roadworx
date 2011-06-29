/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gerrit
 */
public abstract class SpeedLimitUpdater {

    private List<Lane> laneList;

    private RoadSegment rs;
    private double maxAllowedVelocity[];
    private int currentMaxAllowedVelocityIndex;

    public SpeedLimitUpdater(RoadSegment rs) {
        this.rs = rs;        
    }

    public void init(double maxAllowedVelocity[]) {
        this.maxAllowedVelocity = maxAllowedVelocity;
        this.currentMaxAllowedVelocityIndex = 0;

        this.laneList = new ArrayList<Lane>();
        this.laneList.addAll(rs.getStartLanes());
        this.laneList.addAll(rs.getEndLanes());
    }

    protected double getMaxAllowedVelocity() {
        return maxAllowedVelocity[currentMaxAllowedVelocityIndex];
    }

    protected RoadSegment getRoadSegment() {
        return rs;
    }

    protected List<Lane> getLaneList() {
        return laneList;
    }

    protected void lowerSpeedLimit() {
        rs.lowerSpeedLimit();
    }
    protected void raiseSpeedLimit() {
        rs.raiseSpeedLimit();
    }


    public abstract void update(double timestep);
}
