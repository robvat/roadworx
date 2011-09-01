/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.util;

/**
 *
 * @author Gerrit
 */
public class VarianceCalculator {
    
    public VarianceCalculator() {
        reset();
    }

    private double sum;
    private double squareSum;
    private double n;

    public void reset() {
        sum = 0.0;
        squareSum = 0.0;
        n = 0.0;
    }

    public void addTerm(double term) {
        sum += term;
        squareSum += (term * term);
        n += 1.0;
    }

    public double getAverage() {
        return sum / n;
    }

    public double getVariance() {
        double avg = getAverage();

        return (squareSum / n) - (avg * avg);
    }
}
