/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

/**
 *
 * @author Gerrit
 */
public enum CarType {
    LORRY(20.0, 50, 4500, 12.0),
    CAR(45.0, 100.0, 1000, 4.5),
    MINICAR(28.0, 75.0, 600, 2.5);

    private double max_v, weight, length, max_acc;

    CarType(double max_v, double max_acc, double weight, double length) {
        this.max_v = max_v;
        this.max_acc = max_acc;
        this.weight = weight;
        this.length = length;
    }

    /**
     * @return the max_v
     */
    public double getMaxV() {
        return max_v;
    }

    /**
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @return the length
     */
    public double getLength() {
        return length;
    }

    /**
     * @return the max_acc
     */
    public double getMaxAcceleration() {
        return max_acc;
    }

    

}
