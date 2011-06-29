/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Gerrit
 */
public class EmissionBasedSpeedLimitUpdater extends SpeedLimitUpdater {


    public EmissionBasedSpeedLimitUpdater(RoadSegment rs) {
        super(rs);
    }
    
    private static final double HIGH_EMISSION_RATE = 5000.0;
    private static final double LOW_EMISSION_RATE = 1000.0;

    private static final double UPDATE_TIME = TimeUnit.MINUTES.toSeconds(1);
    private double currentTime = 0.0;

    @Override
    public void init(double[] maxAllowedVelocity) {
        super.init(maxAllowedVelocity);

        currentTime = 0.0;        
    }
    
    @Override
    public void update(double timestep) {
        currentTime += timestep;

        if (currentTime >= UPDATE_TIME) {
            currentTime = 0.0;

            if (getRoadSegment().getAverageCo2EmissionPerKilometer() < LOW_EMISSION_RATE) {
                this.raiseSpeedLimit();
            } else if (getRoadSegment().getAverageCo2EmissionPerKilometer()  > HIGH_EMISSION_RATE) {
                this.lowerSpeedLimit();
            }
        }   
    }

}
