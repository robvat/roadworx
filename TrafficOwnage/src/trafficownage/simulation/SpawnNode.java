/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Gerrit
 */
public class SpawnNode extends Node {

    private List<Lane> lanes;
    private double timePassed, spawnInterval;

    public SpawnNode(Point2D.Double location, double spawnInterval) {
        super(location);

        lanes = new LinkedList<Lane>();

        this.spawnInterval = spawnInterval;
    }

    @Override
    public void init() {
        super.init();

        RoadSegment rs;

        for (Node n : getDestinationNodes()) {
            rs = getRoadSegment(n);
            lanes.addAll(rs.getSourceLanes(this));
        }

        timePassed = 0.0;
    }

    @Override
    boolean drivethrough(Car incoming) {
        return false;
    }

    @Override
    void acceptCar(Car incoming) {
    }

    Random rand = new Random();

    private Car generateRandomCar() {
        Car car = new Car();

        int r = rand.nextInt(3);

        if (r == 0)
            car.init(CarType.CAR, DriverType.NORMAL);
        else if (r == 1)
            car.init(CarType.LORRY, DriverType.NORMAL);
        else if (r == 2)
            car.init(CarType.MINICAR, DriverType.NORMAL);
        
        return car;
    }

    private Lane lane;
    private boolean success;
    private Car car;

    @Override
    void update(double timestep) {
        timePassed += timestep;

        while (timePassed > spawnInterval) {
            timePassed = 0.0;

            car = generateRandomCar();

            lane = lanes.get(rand.nextInt(lanes.size()));
            if (lane.acceptsCar(car)) {
                lane.addCar(car);
                success = true;
            }
        }
    }

}
