/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author Gerrit
 */
public class Sequence {
    private List<TrafficLight> nodes;

    private Queue<Integer> throughputs;
    
    private int throughputSum;

    private double cycleStepTime;
    private double currentCycleTime;
    private int cycleRemember;
    
    private double averageThroughput;

    public Sequence(List<TrafficLight> nodes, double cycleStepTime, int cycleRemember) {
        this.nodes = nodes;

        this.cycleStepTime = cycleStepTime;
        this.cycleRemember = cycleRemember;

        currentCycleTime = 0.0;

        throughputs = new LinkedList<Integer>();
    }

    public void update(double timestep) {
        currentCycleTime += timestep;

        if (currentCycleTime > cycleStepTime) {
            currentCycleTime = 0.0;

            int throughput = 0;
            for (TrafficLight n : nodes) {
                throughput += n.pollAcceptedCars();
                if (n.getIncomingLanes().size() == 8)
                    System.out.println("Booya!");
            }

            if (throughputs.size() == cycleRemember)
                throughputSum -= throughputs.poll();

            throughputs.add(throughput);
            throughputSum += (double)throughput;

            averageThroughput = (double)throughputSum / (double)throughputs.size();
        }


    }

    public double getAverageThroughput() {
        return averageThroughput;
    }
}
