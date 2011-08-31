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
import trafficownage.util.StringFormatter;

/**
 *
 * @author Gerrit
 */
public class TrafficManager {
    private HashMap<Integer,List<Node>> areas;

    private List<Mapping> mappings;
    private List<Node> allNodes;


    private List<Mapping> benchmarkedMappings;

    private double simulatedTime;

    private HashMap<Car,Double> departureTimes;

    private Random rand;

    public TrafficManager() {
        areas = new HashMap<Integer,List<Node>>();
        mappings = new ArrayList<Mapping>();
        benchmarkedMappings = new ArrayList<Mapping>();
        rand = new Random();
    }

    public void init() {
        this.departureTimes = new HashMap<Car,Double>();
    }

    public void setNodes(List<Node> nodes) {
        this.allNodes = nodes;
    }
    
    public void setAreas(HashMap<Integer,List<Node>> areas) {
        this.areas = areas;
    }

    public void export() {
        for (Mapping mapping : mappings) {
            if (mapping.isBenchmarked())
                mapping.export();
        }
    }

    public void addMapping(String name, boolean benchmarked, int spawnArea, int targetArea, double spawnInterval, boolean driving) {
        addMapping(name, benchmarked,null,spawnArea,targetArea,spawnInterval, null, driving);
    }

    public void addMapping(String name, boolean benchmarked, int spawnArea, int targetArea, double spawnInterval, CarType carType, boolean driving) {
        addMapping(name, benchmarked,null,spawnArea,targetArea,spawnInterval,carType, driving);
    }

    public void addMapping(String name, boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, int spawnNumber, boolean driving) {
        addMapping(name, benchmarked, startTime,endTime,spawnArea,targetArea,spawnNumber, null, driving);
    }

    public void addMapping(String name, boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, int spawnNumber, CarType carType, boolean driving) {
        double spawnInterval = (endTime - startTime) / (double)spawnNumber;

        addMapping(name, benchmarked, startTime,endTime,spawnArea,targetArea,spawnInterval,carType, driving);
    }

    public void addMapping(String name, boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, double spawnInterval, boolean driving) {
        addMapping(name, benchmarked, new Pair<Double,Double>(startTime,endTime), spawnArea, targetArea, spawnInterval, null, driving);
    }

    public void addMapping(String name, boolean benchmarked, double startTime, double endTime, int spawnArea, int targetArea, double spawnInterval, CarType carType, boolean driving) {
        addMapping(name, benchmarked, new Pair<Double,Double>(startTime,endTime), spawnArea, targetArea, spawnInterval, carType, driving);
    }

    public void addMapping(String name, boolean benchmarked, Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval, boolean driving) {
        addMapping(name, benchmarked, timeSpan, spawnArea, targetArea, spawnInterval, null, driving);
    }

    public void addMapping(String name, boolean benchmarked, Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval, CarType carType, boolean driving) {

        Mapping m = new Mapping(name, benchmarked, timeSpan, spawnArea, targetArea, spawnInterval, carType, driving);

        if (benchmarked)
            benchmarkedMappings.add(m);
        
        mappings.add(m);
    }

    private static double DAY = (double)TimeUnit.DAYS.toSeconds(1);

    public void resetBenchmarks() {
        for (Mapping mapping : mappings)
            if (mapping.isBenchmarked())
                mapping.reset();
    }

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

    public class CarStatistics implements CarListener {

        private Mapping parentMapping;
        private double departureTime;
        private double arrivalTime;
        private double timeTravelled;
        private double benchmarkValue;
        private double queueTime;
        private double distanceTravelled;
        private double averageVelocity;
        private double currentPosition;

        public CarStatistics(Mapping parentMapping, double departureTime) {
            this.parentMapping = parentMapping;
            this.departureTime = departureTime;

            currentPosition = Double.MAX_VALUE;
        }

        public void reachedDestination(Car car, Node destination) {
            //store the time of arrival
            arrivalTime = simulatedTime;

            //determine how long we travelled
            timeTravelled = getArrivalTime() - getDepartureTime();

            //now we know the time travelled AND the distance travelled, lets find out the average velocity
            averageVelocity = distanceTravelled / timeTravelled;

            //calculate and store the benchmark value
            benchmarkValue = getTimeTravelled() / car.getRoute().getOptimalTravelTime();

            queueTime = car.getQueueTime();

            parentMapping.carReachedDestination(getBenchmarkValue(), getQueueTime(), getAverageVelocity());
        }


        public void positionChanged(Car car) {
            double newPosition = car.getPosition();

            if (newPosition > currentPosition)
                distanceTravelled += (newPosition - currentPosition);

            currentPosition = newPosition;
        }

        public double getDistanceTravelled() {
            return distanceTravelled;
        }

        public double getDepartureTime() {
            return departureTime;
        }

        /**
         * @return the averageVelocity
         */
        public double getAverageVelocity() {
            return averageVelocity;
        }
        /**
         * @return the arrivalTime
         */
        public double getArrivalTime() {
            return arrivalTime;
        }

        /**
         * @return the queueTime
         */
        public double getQueueTime() {
            return queueTime;
        }

        /**
         * @return the timeTravelled
         */
        public double getTimeTravelled() {
            return timeTravelled;
        }

        /**
         * @return the benchmarkValue
         */
        public double getBenchmarkValue() {
            return benchmarkValue;
        }

    }

    public class Mapping {
        private Pair<Double,Double> timeSpan;
        private int spawnArea, targetArea;
        private double spawnInterval;
        private double timePassed;
        private boolean activated;
        private boolean benchmarked;
        private boolean driving;

        private List<Car> benchmarkedCars;

        private double meanAverageIndexValue;
        private double meanAverageQueueTime;
        private double meanAverageVelocity;

        private CarType carType;

        private String name;

        public Mapping(String name, boolean benchmarked, Pair<Double,Double> timeSpan, int spawnArea, int targetArea, double spawnInterval, CarType carType, boolean driving) {
            this.name = name;
            this.timeSpan = timeSpan;
            this.spawnArea = spawnArea;
            this.targetArea = targetArea;
            this.spawnInterval = spawnInterval;
            this.timePassed = 0.0;
            this.benchmarked = benchmarked;
            this.driving = driving;

            this.benchmarkedCars = new ArrayList<Car>();
            
            this.carType = carType;

            lambda = (double)spawnInterval * ((double)1/timeUnit);
            L = Math.exp(-lambda);

            determineSpawnInterval();

            this.activated = false;
        }

        public void reset() {
            this.benchmarkedCars = new ArrayList<Car>();
            this.arrivals = 0;
            this.meanAverageIndexValue = 0.0;
        }

        public void export() {
            int n = arrivals;

            double indexValueSdSum = 0.0;
            double queueTimeSdSum = 0.0;
            double averageVelocitySdSum = 0.0;
            for (Car car : benchmarkedCars) {
                indexValueSdSum += Math.pow(car.getCarStatisticsListener().getBenchmarkValue() - meanAverageIndexValue,2.0);
                queueTimeSdSum += Math.pow(car.getCarStatisticsListener().getQueueTime() - meanAverageQueueTime,2.0);
                averageVelocitySdSum += Math.pow(car.getCarStatisticsListener().getAverageVelocity() - meanAverageVelocity,2.0);
            }

            double indexValueVariance = indexValueSdSum / (double)n;
            double queueTimeVariance = queueTimeSdSum / (double)n;
            double averageVelocityVariance = averageVelocitySdSum / (double)n;

            System.out.println("Results from " + this.getName() + ".");
            System.out.println("-Average index value: " + StringFormatter.getTwoDecimalDoubleString(meanAverageIndexValue) + ". variance: " + StringFormatter.getTwoDecimalDoubleString(indexValueVariance));
            System.out.println("-Average queue time: " + StringFormatter.getTimeString(meanAverageQueueTime) + ". variance: " + StringFormatter.getTimeString(queueTimeVariance));
            System.out.println("-Average velocity: " + StringFormatter.getTwoDecimalDoubleString(meanAverageVelocity * 3.6) + "kph. variance: " + StringFormatter.getTwoDecimalDoubleString(averageVelocityVariance * 3.6) + "kph.");
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
            return meanAverageIndexValue;
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

            RoadSegment rs = spawnNode.getRoadSegment(route.getObject2().get(0));

            if (driving)
                car.initSpeed(rs.getMaxVelocity());

            car.setRoute(new Route(route.getObject1(),route.getObject2()));

            if (benchmarked) {
                benchmarkedCars.add(car);
                departureTimes.put(car,simulatedTime);
                car.addCarStatisticsListener(new CarStatistics(this, simulatedTime));
            }

            return car;
        }

        private int arrivals = 0;
        public void carReachedDestination(double benchmarkIndexValue, double queueTime, double averageVelocity) {
            meanAverageIndexValue = ((meanAverageIndexValue * (double)arrivals) + benchmarkIndexValue) / (double)(arrivals + 1);
            meanAverageQueueTime = ((meanAverageQueueTime * (double)arrivals) + queueTime) / (double)(arrivals + 1);
            meanAverageVelocity = ((averageVelocity * (double)arrivals) + averageVelocity) / (double)(arrivals + 1);

            arrivals++;
        }

        private DecimalFormat twoDForm = new DecimalFormat("#.##");

        @Override
        public String toString() {
            return name + ": " + twoDForm.format(meanAverageIndexValue);
        }
    }
}
