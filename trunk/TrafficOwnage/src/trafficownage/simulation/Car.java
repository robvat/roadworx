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
    private DriverType driverType;

    private float velocity;
    private float acceleration;

    private float position;

    public Car(CarType carType, DriverType driverType) {
        this.carType = carType;
        this.driverType = driverType;
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
        //TODO: Update the cars position
        float dVtoLeader = 50f - velocity;
        float dPtoLeader = 20f;

        float s = driverType.getMinimumDistanceToLeader() +
                (velocity*driverType.getDesiredTimeHeadway()) +
                ((velocity * dVtoLeader) / (2 * (float)Math.sqrt(driverType.getMaxAcceleration() * driverType.getMaxComfortableDeceleration())));

        float acc = driverType.getMaxAcceleration() * (float)Math.abs(1 - Math.pow((velocity / 50f),4) - (s/dPtoLeader));

        acceleration = acc;
        velocity = (acceleration / 1000f) * (float)dT;
    }
}
