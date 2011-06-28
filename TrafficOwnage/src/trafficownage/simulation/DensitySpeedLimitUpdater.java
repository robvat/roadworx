/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Gerrit
 */
public class DensitySpeedLimitUpdater extends SpeedLimitUpdater {


    public DensitySpeedLimitUpdater(RoadSegment rs) {
        super(rs);
    }

    private static final double LOW_DENSITY_THRESHOLD = .1;
    private static final double HIGH_DENSITY_THRESHOLD = .4;
    
    private static final double HIGH_DENSITY_MULTIPLIER = 1.0;
    private static final double LOW_DENSITY_MULTIPLIER = 1.0;

    private static final double UPDATE_TIME = TimeUnit.MINUTES.toSeconds(1);
    private static final int UPDATE_MEMORY = 5;
    private static final int UPDATE_NEWSPEED_STEPS = 10;
    private static final double UPDATE_STEP = UPDATE_TIME / (double)UPDATE_MEMORY;

    private double currentTime = 0.0;
    private Queue<Double>[] lastDensities;
    private double[] densitySum;
    private double[] densityAverage;

    @Override
    public void init(double maxAllowedVelocity) {
        super.init(maxAllowedVelocity);

        currentTime = 0.0;
        densitySum = new double[2];
        densityAverage = new double[2];
        lastDensities = new LinkedList[2];


        for (int i = 0; i < 2; i++) {
            densitySum[i] = 0.0;
            lastDensities[i] = new LinkedList<Double>();
        }
    }

    private int currentStep;

    @Override
    public void update(double timestep) {
        currentTime += timestep;

        if (currentTime >= UPDATE_STEP) {
            currentTime = 0.0;
            
            currentStep++;

            List<Lane> lanes;
            Queue<Double> densities;

            for (int i = 0; i < 2; i++) {
                lanes = super.getLaneLists()[i];
                densities = lastDensities[i];

                double currentDensity = 0.0;

                for (Lane l : lanes)
                    currentDensity += l.getDensity();

                currentDensity /= lanes.size();

                if (densities.size() == UPDATE_MEMORY)
                    densitySum[i] -= densities.poll();

                densitySum[i] += currentDensity;

                densities.add(currentDensity);

                densityAverage[i] = densitySum[i] / densities.size();
            }
        }

        if (currentStep >= UPDATE_NEWSPEED_STEPS) {

            for (int i = 0; i < 2; i++) {
                 List<Lane> lanes = super.getLaneLists()[i];
                if (densityAverage[i] < LOW_DENSITY_THRESHOLD) {
                    updateSpeedLimits(lanes, LOW_DENSITY_MULTIPLIER);
                } else if (densityAverage[i] > HIGH_DENSITY_THRESHOLD) {
                    //System.out.println("Raising...");
                    updateSpeedLimits(lanes, HIGH_DENSITY_MULTIPLIER);
                }
            }

            currentStep = 0;
        }

    }

}
