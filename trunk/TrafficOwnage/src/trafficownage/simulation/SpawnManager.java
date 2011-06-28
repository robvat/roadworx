/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.text.DecimalFormat;
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

    private List<Mapping> benchmarkedMappings;

    private double simulatedTime;

    private HashMap<Car,Double> departureTimes;

    private Random rand;

    public SpawnManager() {
        areas = new HashMap<Integer,List<Node>>();
        mappings = new ArrayList<Mapping>();
        benchmarkedMappings = new ArrayList<Mapping>();
        rand = new Random();
    }

    public void init(List<Node> allNodes, HashMap<Integer,List<Node>> areas) {
        this.allNodes = allNodes;
        this.areas = areas;

        this.departureTimes = new HashMap<Car,Double>();
    }

    public void addMapping(String name, boolean benchmarked, int spawnArea, int targetArea, double spawnInterval) {
        addMapping(name, benchmarked,null,spawnArea,targetArea,spawnInterval, null);
    }

    public void addMapping(String name, boolean benchmarked, int spawnArea, int targetArea, double spawnInterval, CarType carType) {
        addMapping(name, benchmarked,null,spawnArea,targetArea,spawnInterval,carType);
    }

    public void addMapping(String name, boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, int spawnNumber) {
        addMapping(name, benchmarked, startTime,endTime,spawnArea,targetArea,spawnNumber, null);
    }

    public void addMapping(String name, boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, int spawnNumber, CarType carType) {
        double spawnInterval = (endTime - startTime) / (double)spawnNumber;

        addMapping(name, benchmarked, startTime,endTime,spawnArea,targetArea,spawnInterval,carType);
    }

    public void addMapping(String name, boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, double spawnInterval) {
        addMapping(name, benchmarked, new Pair<Double,Double>(startTime,endTime), spawnArea, targetArea, spawnInterval, null);
    }

    public void addMapping(String name, boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, double spawnInterval, CarType carType) {
        addMapping(name, benchmarked, new Pair<Double,Double>(startTime,endTime), spawnArea, targetArea, spawnInterval, carType);
    }

    public void addMapping(String name, boolean benchmarked, Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval) {
        addMapping(name, benchmarked, timeSpan, spawnArea, targetArea, spawnInterval, null);
    }

    public void addMapping(String name, boolean benchmarked, Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval, CarType carType) {

        Mapping m = new Mapping(name, benchmarked, timeSpan, spawnArea, targetArea, spawnInterval, carType);

        if (benchmarked)
            benchmarkedMappings.add(m);
        
        mappings.add(m);
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

    public List<Mapping> getBenchmarkedMappings() {
        return benchmarkedMappings;
    }

    private boolean isActive(double simulatedTime, Mapping mapping) {
        return (mapping.getTimeSpan() == null || (simulatedTime >= mapping.getTimeSpan().getObject1() && simulatedTime <= mapping.getTimeSpan().getObject2()));
    }


    private Node selectRandomNode(List<Node> nodes) {
        return nodes.get(rand.nextInt(nodes.size()));
    }


    public class Mapping implements CarListener {
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

        private String name;

        public Mapping(String name, boolean benchmarked, Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval, CarType carType) {
            this.name = name;
            this.timeSpan = timeSpan;
            this.spawnArea = spawnArea;
            this.targetArea = targetArea;
            this.spawnInterval = spawnInterval;
            this.timePassed = 0.0;
            this.benchmarked = benchmarked;

            this.benchmarkedCars = new ArrayList<Car>();
            this.results = new HashMap<Car,Double>();
            
            this.carType = carType;

            lambda = (double)spawnInterval * ((double)1/timeUnit);
            L = Math.exp(-lambda);

            determineSpawnInterval();

            this.activated = false;
        }

        public String getName() {
            return name;
        }

        private double lambda;
        private double L;

        private static final double timeUnit = 0.05;

        private Random rand = new Random();

        private void determineSpawnInterval() {
            int k = 0;
            double p = 1.0;

            while (p > L) {
                p *= rand.nextDouble();
                k++;
            }

            spawnInterval = (double)(k-1) * timeUnit;
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

                determineSpawnInterval();

                timePassed = 0;
            }
        }

        public boolean isBenchmarked() {
            return benchmarked;
        }

        public double getBenchmarkResults() {
            return result;
        }

        public int getArrivedCarCount() {
            return arrivals;
        }

        public List<Car> getBenchmarkedCars() {
            return benchmarkedCars;
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

//            Pair<Double,List<Node>> route2 = Pathfinding.shortestRoute(targetNode, spawnNode, allNodes);
//            if (Math.random() < 0.5)
//                    route = route2;
            car.setRoute(new Route(route.getObject1(),route.getObject2()));

            if (benchmarked) {
                benchmarkedCars.add(car);
                departureTimes.put(car,simulatedTime);
                car.addListener(this);
            }

            return car;
        }

        private int arrivals = 0;
        public void reachedDestination(Car car, Node destination) {
            arrivals++;

            double timeTravelled = simulatedTime - departureTimes.get(car);
            double benchmarkValue = timeTravelled / car.getRoute().getOptimalTravelTime();


            result = ((result * (double)results.size()) + benchmarkValue) / (double)(results.size() + 1);
            results.put(car, benchmarkValue);
        }

        private DecimalFormat twoDForm = new DecimalFormat("#.##");

        @Override
        public String toString() {
            return name + ": " + twoDForm.format(result);
        }



    }
}
