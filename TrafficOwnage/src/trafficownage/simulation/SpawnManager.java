/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import trafficownage.util.Pair;
import trafficownage.util.Pathfinding;
import trafficownage.util.Triplet;

/**
 *
 * @author Gerrit
 */
public class SpawnManager {
    private HashMap<Integer,List<Node>> areas;

    private List<Mapping> mappings;
    private List<Node> allNodes;

    private Random rand;

    public SpawnManager() {
        areas = new HashMap<Integer,List<Node>>();
        mappings = new ArrayList<Mapping>();
        rand = new Random();
    }

    public void init(List<Node> allNodes, HashMap<Integer,List<Node>> areas) {
        this.allNodes = allNodes;
        this.areas = areas;
    }

    public void addMapping(int spawnArea, int targetArea, double spawnInterval) {
        mappings.add(new Mapping(null,spawnArea,targetArea,spawnInterval,null));
    }

    public void addMapping(double startTime, double endTime, int spawnArea, int targetArea, double spawnInterval) {
        mappings.add(new Mapping(new Pair<Double,Double>(startTime,endTime),spawnArea,targetArea,spawnInterval,null));
    }

    public void addMapping(double startTime, double endTime, int spawnArea, int targetArea, int spawnNumber) {
        double spawnInterval = (endTime - startTime) / (double)spawnNumber;

        mappings.add(new Mapping(new Pair<Double,Double>(startTime,endTime),spawnArea,targetArea,spawnInterval,null));
    }

    public void update (double simulatedTime, double timeStep) {

        for (Mapping m : mappings) {
            if (isActive(simulatedTime, m)) {
                
                if (!m.isActivated())
                    m.activate();

                m.update(timeStep);
            } else if (m.isActivated()) {
                m.deactivate();
            }
        }
        
    }

    private boolean isActive(double simulatedTime, Mapping mapping) {
        return (mapping.getTimeSpan() == null || (simulatedTime >= mapping.getTimeSpan().getObject1() && simulatedTime <= mapping.getTimeSpan().getObject2()));
    }

    private void spawnCar(CarType carType, DriverType driverType, int spawnArea, int targetArea) {
        if (!areas.containsKey(spawnArea) || !areas.containsKey(targetArea)) {
            System.err.println("Spawn or target area does not exist.");
            return;
        }

        List<Node> spawnNodes = areas.get(spawnArea);
        List<Node> targetNodes = areas.get(targetArea);

        Node spawnNode = null;
        Node targetNode = null;

        while (spawnNode == null || targetNode == null || spawnNode == targetNode) {
            spawnNode = selectRandomNode(spawnNodes);
            targetNode = selectRandomNode(targetNodes);
        }

        Car c = generateCar(carType, driverType, spawnNode,targetNode);
        spawnNode.addSpawnCar(c);
    }

    private Car generateCar(CarType carType, DriverType driverType, Node spawnNode, Node targetNode) {
        Car car = new Car();
        car.init(carType, driverType);
        car.setRoute(new Route(Pathfinding.fastestRoute(car,spawnNode,targetNode,allNodes)));
        
        return car;
    }

    private Node selectRandomNode(List<Node> nodes) {
        return nodes.get(rand.nextInt(nodes.size()));
    }

    private class Mapping {
        private Pair<Double,Double> timeSpan;
        private int spawnArea, targetArea;
        private double spawnInterval;
        private double timePassed;
        private boolean activated;

        private CarType carType;

        public Mapping(Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval, CarType carType) {
            this.timeSpan = timeSpan;
            this.spawnArea = spawnArea;
            this.targetArea = targetArea;
            this.spawnInterval = spawnInterval;
            this.timePassed = 0.0;

            this.carType = carType;

            this.activated = false;
        }

        public Pair<Double,Double> getTimeSpan() {
            return timeSpan;
        }

        public boolean isActivated() {
            return activated;
        }

        public void activate() {
            activated = true;
            timePassed = 0.0;
        }

        public void deactivate() {
            activated = false;
        }
        
        public void update(double timeStep) {
            timePassed += timeStep;

            if (timePassed >= spawnInterval) {

                if (carType != null)
                     spawnCar(carType,DriverType.getRandomDriverType(),spawnArea, targetArea);
                else
                    spawnCar(CarType.getRandomCarType(),DriverType.getRandomDriverType(),spawnArea, targetArea);

                timePassed = 0;
            }
        }


    }
}
