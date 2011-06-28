/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.util;

/**
 *
 * @author Tester
 */
public class Co2Calculator {
    
    private double speed, acceleration; 
    private double[] EFC = {0.553, 0.161, -0.00289, 0.266, 0.511, 0.183};//emissionFunctionConstants
    private double[] EFCTruck = {0.904, 1.13, -0.00427, 2.81, 3.45, 1.22};
    
    public Co2Calculator(double aSpeed, double aAcceleration, int carType){
        speed = aSpeed;
        acceleration = aAcceleration;
    }
    public double getCo2(){
        
        double calculatedemmision =  EFC[0] + (EFC[1]*speed) + (EFC[2]*Math.pow(speed, 2)) 
                + (EFC[3]*acceleration) + (EFC[4]*Math.pow(acceleration, 2)) + (EFC[5]*speed*acceleration);  
                
        return calculatedemmision;//grams/sec
    }
    public static void main(String[] args){
        Co2Calculator ele = new Co2Calculator(0, 0, 1);
        System.out.println(ele.getCo2());
    }
}
