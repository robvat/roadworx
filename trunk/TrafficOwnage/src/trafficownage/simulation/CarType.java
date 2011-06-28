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
    SUPERCAR(300.0/3.6, (100.0/3.6)/6.0, 1200, 4.0, 0, true),
    LORRY(90.0/3.6, (100.0/3.6)/18.0, 4500, 18.0, 2, false),
    BIGCAR(120.0/3.6, (100.0/3.6)/13, 2000, 6.0, 1, true),
    CAR(150.0/3.6, (100.0/3.6)/10, 1000, 4.5, 0, true),
    MINICAR(120.0/3.6, (100.0/3.6)/15, 600, 2.5, 0, true);


    public static final int FUEL_PETROL = 0, FUEL_DIESEL = 1, FUEL_LORRY = 2;
    
    private double maxVelocity, weight, length, maxAcceleration;
    private int fuelType;
    private Boolean overtake;
    private static Random rand = new Random();

    CarType(double maxVelocity, double maxAcceleration, double weight, double length, int fuelType, boolean overtake) {
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.weight = weight;
        this.length = length;
        this.overtake = overtake;
        this.fuelType = fuelType;
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

    
    public int getFuelType() {
        return fuelType;
    }
    
    /**
     * @return the max_v
     */
    public double getMaxVelocity() {
        return maxVelocity;
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
        return maxAcceleration;
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

        this.maxVelocity *= firstTwo[0];
        this.weight *= firstTwo[1];
        this.length *= secondTwo[0];
        this.maxAcceleration *= secondTwo[1];
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
