/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.util;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Gerrit
 */
public class Averager {
    private Queue<Double> values;
    private double sum;
    private int memorySize;
    private double average;

    public Averager(int memorySize) {
        values = new LinkedList<Double>();
        this.memorySize = memorySize;
        sum = 0.0;
    }

    public void addTerm(double term) {
        if (values.size() == memorySize)
            sum -= values.poll();

        values.add(term);
        sum += term;

        average = sum / values.size();
    }

    public double getAverage() {
        return average;
    }
}
