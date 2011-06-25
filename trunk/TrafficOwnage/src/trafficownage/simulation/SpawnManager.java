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
import trafficownage.util.Triplet;

/**
 *
 * @author Gerrit
 */
public class SpawnManager {
    private HashMap<Integer,List<Node>> areas;

    private List<Mapping> mappings;

    private double timePassed = 0.0;

    private Random rand;

    public SpawnManager() {
        areas = new HashMap<Integer,List<Node>>();
        mappings = new ArrayList<Mapping>();
        rand = new Random();
    }

    public void init(HashMap<Integer,List<Node>> areas) {
        this.areas = areas;
    }

    public void addMapping(int spawnArea, int targetArea, double spawnInterval) {
        mappings.add(new Mapping(null,spawnArea,targetArea,spawnInterval));
    }

    public void addMapping(double startTime, double endTime, int spawnArea, int targetArea, double spawnInterval) {
        mappings.add(new Mapping(new Pair<Double,Double>(startTime,endTime),spawnArea,targetArea,spawnInterval));
    }

    public void update (double timeStep) {
        timePassed += timeStep;

        for (Mapping m : mappings) {
            if (isActive(m))
                m.update(timeStep);
        }
        
    }

    private boolean isActive(Mapping mapping) {
        return (mapping.getTimeSpan() == null || (timePassed >= mapping.getTimeSpan().getObject1() && timePassed <= mapping.getTimeSpan().getObject2()));
    }

    private void spawnCar(int spawnArea, int targetArea) {
        if (!areas.containsKey(spawnArea) || !areas.containsKey(targetArea)) {
            System.err.println("Spawn or target area does not exist.");
            return;
        }

        List<Node> spawnNodes = areas.get(spawnArea);
        List<Node> targetNodes = areas.get(targetArea);

        Node spawnNode = selectRandomNode(spawnNodes);
        Node targetNode = selectRandomNode(targetNodes);

        Car c = generateCar(spawnNode,targetNode);
        spawnNode.addSpawnCar(c);
    }

    private Car generateCar(Node spawnNode, Node targetNode) {
        Car car = new Car();
        car.init(CarType.LORRY, DriverType.NORMAL);
        
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

        public Mapping(Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval) {
            this.timeSpan = timeSpan;
            this.spawnArea = spawnArea;
            this.targetArea = targetArea;
            this.spawnInterval = spawnInterval;
            this.timePassed = 0.0;
        }

        public Pair<Double,Double> getTimeSpan() {
            return timeSpan;
        }
        
        public void update(double timeStep) {
            timePassed += timeStep;

            if (timePassed >= spawnInterval) {
                spawnCar(spawnArea, targetArea);

                timePassed = 0;
            }
        }


    }
}
