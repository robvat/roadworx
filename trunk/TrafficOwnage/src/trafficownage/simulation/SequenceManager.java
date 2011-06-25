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
public class SequenceManager {
    private List<Sequence> sequences;

    private static final double MEASURE_TIME = 120.0; //every so many seconds the sequences will update their average
    private static final int MEASURE_MEMORY = 5; //the number of measurements to be remembered

    public void init(double stepSize, List<Road> roads) {
        sequences = new ArrayList<Sequence>();

        ArrayList<TrafficLight> seq = null;

        for (Road r : roads) {

            for (Node n : r.getNodes()) {

                if (n instanceof TrafficLight) {
                    if (seq == null)
                        seq = new ArrayList<TrafficLight>();
                    
                    seq.add((TrafficLight)n);

                } else if (seq != null) {
                    sequences.add(new Sequence(seq,MEASURE_TIME,MEASURE_MEMORY));
                    seq = null;
                }
            }
            
            if (seq != null) {
                sequences.add(new Sequence(seq,MEASURE_TIME,MEASURE_MEMORY));
                seq = null;
            }


        }
    }

    public void update(double timestep) {
        for (Sequence s : sequences) {
            s.update(timestep);
        }
    }
}
