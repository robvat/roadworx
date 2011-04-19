/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

/**
 *
 * @author Gerrit
 */
public class Car implements Updatable {
    private CarType carType;
    private DriverModel driverModel;

    private float velocity;
    private float acceleration;

    private float position;

    public Car(CarType carType, DriverModel driverModel) {
        this.carType = carType;
        this.driverModel = driverModel;
    }
    
    /**
     * @return the velocity
     */
    public float getVelocity() {
        return velocity;
    }

    /**
     * @return the acceleration
     */
    public float getAcceleration() {
        return acceleration;
    }

    /**
     * @return the position
     */
    public float getPosition() {
        return position;
    }

    public void update(int dT) {
        
    }
}
