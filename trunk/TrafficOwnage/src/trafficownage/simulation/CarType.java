/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.Random;

/**
 *
 * @author Gerrit
 */
public enum CarType {
    SUPERCAR(300.0/3.6, (100.0/3.6)/6.0, 1200, 4.0, true),
    LORRY(10.0/3.6, (100.0/3.6)/18.0, 4500, 18.0, false),
    BIGCAR(120.0/3.6, (100.0/3.6)/13, 2000, 6.0, true),
    CAR(150.0/3.6, (100.0/3.6)/10, 1000, 4.5, true),
    MINICAR(120.0/3.6, (100.0/3.6)/15, 600, 2.5, true);


    private double max_v, weight, length, max_acc;
    private Boolean overtake;
    private static Random rand = new Random();

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
        Double rn = rand.nextDouble();
        
        if (rn >= 0 & rn <= 0.05)
            return CarType.SUPERCAR; //.randomizeParameters();
        else if (rn > 0.05 & rn <=0.15)
            return CarType.BIGCAR; //.randomizeParameters();
        else if (rn > 0.15 & rn <= 0.4)
            return CarType.MINICAR; //.randomizeParameters();
        else if (rn > 0.4 & rn <= 0.7)
            return CarType.CAR; //.randomizeParameters();
        else
            return CarType.LORRY; //.randomizeParameters();
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
   
 /**
     * Makes the parameter differ by using normally distributed random numbers
     * Box-Muller method for obtaining those normal random numbers
     * @return the CarType with sort of random parameters
     */
    public CarType randomizeParameters(){
        double[] firstTwo = getBoxMullerNumbers();
        double[] secondTwo = getBoxMullerNumbers();

        this.max_v *= firstTwo[0];
        this.weight *= firstTwo[1];
        this.length *= secondTwo[0];
        this.max_acc *= secondTwo[1];
        return this;
    }

    private double[] getBoxMullerNumbers(){
        double mean = 1, stdDev  = 0.01;
        double r = 0, x = 0, y = 0;
        while (r == 0 || r > 1){
            x = 2*rand.nextDouble()-1;
            y = 2*rand.nextDouble()-1;
            r = x*x + y*y;
        }

        double d = Math.sqrt(-2.0*Math.log(r)/r);

        return new double[]{x*d*stdDev + mean, y*d*stdDev + mean};
    }

    public static void main(String[] args){
        CarType t = getRandomCarType();
        System.out.println(t.getMaxVelocity() + "  " +  t.getMaxAcceleration() + "  " + t.getWeight()  + "  " +  t.getLength());
    }
}
