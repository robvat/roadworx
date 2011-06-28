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


    private static double[][] carTypes = new double[][] {
        {0.553, 0.161, -0.00289, 0.266, 0.511, 0.183}, //PETROL
        {0.324, 0.0859, 0.00496, -0.0586, 0.448, 0.230}, //DIESEL
        {0.904, 1.13, -0.00427, 2.81, 3.45, 1.22} //LORRY
    };
    
    public static double calculate(double speed, double acceleration, int carType) {

        if (acceleration <= 0.0)
            return 0.0;

        double calculatedEmmision =
                carTypes[carType][0] +
                (carTypes[carType][1] * speed) +
                (carTypes[carType][2] * Math.pow(speed, 2)) +
                (carTypes[carType][3] * acceleration) +
                (carTypes[carType][4] * Math.pow(acceleration, 2)) +
                (carTypes[carType][5] * speed * acceleration); 
                
        return Math.max(0, calculatedEmmision);//grams/sec
    }
}
