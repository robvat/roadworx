/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import trafficownage.util.Triplet;

/**
 *
 * @author Gerrit
 */
public class SequenceManager {
    private Sequence[] sequences;

    private static final double MEASURE_TIME = 120.0; //every so many seconds the sequences will update their average
    private static final int MEASURE_MEMORY = 4; //the number of measurements to be remembered

    public void init(double stepSize, List<Road> roads) {
        List<Sequence> tmp = new ArrayList<Sequence>();

        LinkedList<TrafficLight> seq1 = null;
        LinkedList<TrafficLight> seq2 = null;

        for (Road r : roads) {

            if (r.getPriority() > 1)
                continue;

            for (Node n : r.getNodes()) {

                if (n instanceof TrafficLight) {
                    if (seq1 == null) {
                        seq1 = new LinkedList<TrafficLight>();
                        seq2 = new LinkedList<TrafficLight>();
                    }
                    
                    seq1.addFirst((TrafficLight)n);
                    seq2.addLast((TrafficLight)n);

                } else if (seq1 != null) {
                    tmp.add(new Sequence(r,seq1.toArray(new TrafficLight[0]),MEASURE_MEMORY));
                    tmp.add(new Sequence(r,seq2.toArray(new TrafficLight[0]),MEASURE_MEMORY));
                    seq1 = null;
                    seq2 = null;
                }
            }
            
            if (seq1 != null) {
                tmp.add(new Sequence(r,seq1.toArray(new TrafficLight[0]),MEASURE_MEMORY));
                tmp.add(new Sequence(r,seq2.toArray(new TrafficLight[0]),MEASURE_MEMORY));
                seq1 = null;
                seq2 = null;
            }
        }

        sequences = tmp.toArray(new Sequence[0]);
    }

    private double currentCycleTime;

    private boolean justonce = true;

    public void update(double timestep) {
        currentCycleTime += timestep;

        for (Sequence s : sequences) {
            s.update(timestep);
        }

        if (currentCycleTime >= MEASURE_TIME) {
            currentCycleTime = 0.0;

            for (Sequence s : sequences) {
                s.updateThroughput();
            }

            sequences = SequenceComparator.sort(sequences);

            if (justonce) {
                justonce = false;
                Triplet<TrafficLight,Double,Double>[] timings = sequences[0].determineTimings(30.0,5.0);
                sequences[0].start(timings);
            }

        }
    }
}
