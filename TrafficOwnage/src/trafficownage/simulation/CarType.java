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
    SUPERCAR(300.0/3.6, (100.0/3.6)/6.0, 1200, 4.0, true),
    LORRY(90.0/3.6, (100.0/3.6)/18.0, 4500, 18.0, false),
    BIGCAR(120.0/3.6, (100.0/3.6)/13, 2000, 6.0, true),
    CAR(150.0/3.6, (100.0/3.6)/10, 1000, 4.5, true),
    MINICAR(120.0/3.6, (100.0/3.6)/15, 600, 2.5, true);


    private double max_v, weight, length, max_acc;
    private Boolean overtake;

    CarType(double max_v, double max_acc, double weight, double length, boolean overtake) {
        this.max_v = max_v;
        this.max_acc = max_acc;
        this.weight = weight;
        this.length = length;
        this.overtake = overtake;
    }

    public static CarType getRandomCarType() {
        // 10% supercar
        // cars are worth 70% of traffic, trucks 30%
        // Minicar has 25%, Car has 30%, Bigcar has 10%
        Double rn = Math.random();
        
        if (rn >= 0 & rn <= 0.05)
            return CarType.SUPERCAR;
        else if (rn > 0.05 & rn <=0.15)
            return CarType.BIGCAR;
        else if (rn > 0.15 & rn <= 0.4)
            return CarType.MINICAR;
        else if (rn > 0.4 & rn <= 0.7)
            return CarType.CAR;
        else
            return CarType.LORRY;
    }

    /**
     * @return the max_v
     */
    public double getMaxVelocity() {
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

    public Boolean doesOvertake(){
        return overtake;
    }

}
