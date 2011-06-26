/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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

    private double simulatedTime;

    private HashMap<Car,Double> departureTimes;

    private Random rand;

    public SpawnManager() {
        areas = new HashMap<Integer,List<Node>>();
        mappings = new ArrayList<Mapping>();
        rand = new Random();
    }

    public void init(List<Node> allNodes, HashMap<Integer,List<Node>> areas) {
        this.allNodes = allNodes;
        this.areas = areas;

        this.departureTimes = new HashMap<Car,Double>();
    }

    public void addMapping(boolean benchmarked, int spawnArea, int targetArea, double spawnInterval) {
        addMapping(benchmarked,null,spawnArea,targetArea,spawnInterval, null);
    }

    public void addMapping(boolean benchmarked, int spawnArea, int targetArea, double spawnInterval, CarType carType) {
        addMapping(benchmarked,null,spawnArea,targetArea,spawnInterval,carType);
    }

    public void addMapping(boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, int spawnNumber) {
        addMapping(benchmarked, startTime,endTime,spawnArea,targetArea,spawnNumber, null);
    }

    public void addMapping(boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, int spawnNumber, CarType carType) {
        double spawnInterval = (endTime - startTime) / (double)spawnNumber;

        addMapping(benchmarked, startTime,endTime,spawnArea,targetArea,spawnInterval,carType);
    }

    public void addMapping(boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, double spawnInterval) {
        addMapping(benchmarked, new Pair<Double,Double>(startTime,endTime), spawnArea, targetArea, spawnInterval, null);
    }

    public void addMapping(boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, double spawnInterval, CarType carType) {
        addMapping(benchmarked, new Pair<Double,Double>(startTime,endTime), spawnArea, targetArea, spawnInterval, carType);
    }

    public void addMapping(boolean benchmarked, Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval) {
        addMapping(benchmarked, timeSpan, spawnArea, targetArea, spawnInterval, null);
    }

    public void addMapping(boolean benchmarked, Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval, CarType carType) {
        mappings.add(new Mapping(benchmarked, timeSpan, spawnArea, targetArea, spawnInterval, carType));
    }

    private static double DAY = (double)TimeUnit.DAYS.toSeconds(1);

    public void update (double simulatedTime, double timeStep) {

        if (simulatedTime < this.simulatedTime) {
            //what a beautiful new day
            for (Car c : departureTimes.keySet())
                departureTimes.put(c,departureTimes.get(c) - DAY);
        }

        this.simulatedTime = simulatedTime;

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


    private Node selectRandomNode(List<Node> nodes) {
        return nodes.get(rand.nextInt(nodes.size()));
    }


    private class Mapping implements CarListener {
        private Pair<Double,Double> timeSpan;
        private int spawnArea, targetArea;
        private double spawnInterval;
        private double timePassed;
        private boolean activated;
        private boolean benchmarked;

        private List<Car> benchmarkedCars;

        private HashMap<Car,Double> results;
        private double result;

        private CarType carType;

        public Mapping(boolean benchmarked, Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval, CarType carType) {
            this.timeSpan = timeSpan;
            this.spawnArea = spawnArea;
            this.targetArea = targetArea;
            this.spawnInterval = spawnInterval;
            this.timePassed = 0.0;
            this.benchmarked = benchmarked;

            this.benchmarkedCars = new ArrayList<Car>();
            this.results = new HashMap<Car,Double>();
            
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

        public boolean isBenchmarked() {
            return benchmarked;
        }

        public double getBenchmarkResults() {
            return result;
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

            Pair<Double,List<Node>> route = Pathfinding.fastestRoute(car,spawnNode,targetNode,allNodes);
            car.setRoute(new Route(route.getObject1(),route.getObject2()));

            if (benchmarked) {
                benchmarkedCars.add(car);
                departureTimes.put(car,simulatedTime);
                car.addListener(this);
            }

            return car;
        }

        public void reachedDestination(Car car, Node destination) {
            double timeTravelled = simulatedTime - departureTimes.get(car);
            double benchmarkValue = timeTravelled / car.getRoute().getOptimalTravelTime();


            result = ((result * (double)results.size()) + benchmarkValue) / (double)(results.size() + 1);
            results.put(car, benchmarkValue);
        }



    }
}
