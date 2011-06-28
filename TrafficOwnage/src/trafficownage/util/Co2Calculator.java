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
    private static double[] EFCpetrol = {0.553, 0.161, -0.00289, 0.266, 0.511, 0.183};//emissionFunctionConstantsPetrol
    private static double[] EFCdeisel = {0.324, 0.0859, 0.00496, -0.0586, 0.448, 0.230};
    private static double[] EFClorry = {0.904, 1.13, -0.00427, 2.81, 3.45, 1.22};
    private double[] carType;
    
    public Co2Calculator(double aSpeed, double aAcceleration, int aCarType){
        speed = aSpeed;
        acceleration = aAcceleration;
        if(aCarType == 0) carType = EFCpetrol;
        else if (aCarType == 1) carType = EFCdeisel;
        else carType = EFClorry;
    }
    public double getCo2(){
        
        double calculatedemmision =  carType[0] + (carType[1]*speed) + (carType[2]*Math.pow(speed, 2)) 
                + (carType[3]*acceleration) + (carType[4]*Math.pow(acceleration, 2)) + (carType[5]*speed*acceleration);  
                
        return Math.max(0, calculatedemmision);//grams/sec
    }
}
