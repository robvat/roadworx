/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Gerrit
 */
public class SequenceComparator implements Comparator<Sequence> {

    public int compare(Sequence o1, Sequence o2) {
        if (o1.getAverageThroughput() < o2.getAverageThroughput())
            return -1;
        else if (o1.getAverageThroughput() == o2.getAverageThroughput())
            return 0;
        else
            return 1;
    }

    private static final SequenceComparator SEQUENCE_COMPARATOR = new SequenceComparator();

    public static Sequence[] sort(Sequence[] array) {
        Arrays.sort(array,SEQUENCE_COMPARATOR);
        return array;
    }

}
