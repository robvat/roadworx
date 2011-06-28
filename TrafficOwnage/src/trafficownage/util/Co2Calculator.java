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
    private static double[] MOEconstant = {0.870, 0.748, 0.918};
    private static double elurConstant = 2.71828182845904523536028747135266249775724709369995;
    
    public Co2Calculator(double aSpeed, double aAcceleration){
        speed = aSpeed;
        acceleration = aAcceleration;
        
    }
    public double getCo2(){
        double temp = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                temp += Math.pow(MOEconstant[i], elurConstant) * Math.pow(speed, i) * Math.pow(acceleration, j);
            }
        }
        return (Math.log(temp)/1000); // in L/s
    }
    public static void main(String[] args){
        Co2Calculator ele = new Co2Calculator(60, 3);
        System.out.println(ele.getCo2());
    }
}
