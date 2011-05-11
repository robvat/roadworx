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
    LORRY(28, .14f, 4500, 12),
    CAR(45, 100.0f, 1000, 4);

    private int max_v, weight, length;
    private float max_acc;

    CarType(int max_v, float max_acc, int weight, int length) {
        this.max_v = max_v;
        this.max_acc = max_acc;
        this.weight = weight;
        this.length = length;
    }

    /**
     * @return the max_v
     */
    public int getMaxV() {
        return max_v;
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @return the max_acc
     */
    public float getMaxAcceleration() {
        return max_acc;
    }

    

}
