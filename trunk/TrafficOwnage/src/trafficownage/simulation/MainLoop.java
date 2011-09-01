/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import trafficownage.simulation.TrafficManager.Mapping;
import trafficownage.util.ManhattanMapGenerator;
import trafficownage.util.SingleNodeGenerator;
import trafficownage.util.StringFormatter;

/**
 *
 * @author Gerrit
 */
public class MainLoop implements NodeListener, CarListener {

    private final static long FPS = 20;
    private int speedMultiplier;
    private List<Road> roads;
    private List<Node> nodes;
    private boolean initialized;
    private boolean run;
    private boolean stop;
    private boolean realtime = true;
    private long msStep;
    private double sStep;
    private double currentSpeed;

    private double co2Emission;

    private int carCount;
    private MainLoopListener listener = null;
    private TrafficManager trafficManager = new TrafficManager();
    private static final double DAY = (double) TimeUnit.HOURS.toSeconds(24);
    
    private static final long MEASURE_SPEED_INTERVAL = 5000;

    private double[] exportMoments;
    private int currentExportMoment;
    private Double nextExportMoment;

    private double overallSimulatedTime;
    private double currentDaySimulatedTime;
    private double overallEndTime;
    private boolean eternalLoop = true;
    
    private int currentDay;

    public MainLoop() {
        initialized = false;
        speedMultiplier = 256;
    }

    private static double[] scale(double[] array, double scale) {
        for (int i = 0; i < array.length; i++)
            array[i] = array[i] * scale;

        return array;
    }

    public void setStartTime(double startTime) {
        overallSimulatedTime = startTime;
        currentDaySimulatedTime = overallSimulatedTime % DAY;
    }

    public void setEndTime(double endTime) {
        overallEndTime = endTime;
        eternalLoop = false;
    }

    public void enableFileOutput() {
        String directoryString = System.getenv("USERPROFILE") + "\\TrafficOwnage";
        String resultFileString = directoryString + "\\" + StringFormatter.getDateTimeFileString() + "_results.txt";
        String errorFileString = directoryString + "\\" + StringFormatter.getDateTimeFileString() + "_errors.txt";

        File directory = new File(directoryString);

        if (!directory.exists())
            directory.mkdir();

        File resultFile = new File(resultFileString);
        File errorFile = new File(resultFileString);

        if (!resultFile.exists())
            resultFile.delete();

        if (!errorFile.exists())
            errorFile.delete();

        try {
            resultFile.createNewFile();
            errorFile.createNewFile();
            FileOutputStream resultOutput = new FileOutputStream(resultFileString);
            FileOutputStream errorOutput = new FileOutputStream(errorFileString);

            System.setOut(new PrintStream(resultOutput));
            System.setErr(new PrintStream(errorOutput));

        } catch (IOException ex) {
            System.err.println("Logging to file failed, therefore logging to stdout.");
        }

    }

    public void init() {

        setStartTime((double) TimeUnit.HOURS.toSeconds(0));
//        overallSimulatedTime = (double) TimeUnit.HOURS.toSeconds(6) + (double) TimeUnit.MINUTES.toSeconds(55);
//        currentDaySimulatedTime = overallSimulatedTime % DAY;

        //commenting the setEndTime line will make the application loop eternally
        setEndTime((double) TimeUnit.HOURS.toSeconds(12) + (double) TimeUnit.MINUTES.toSeconds(5));
        
        //Sends console output to text files in a TrafficOwnage folder in your profile folder.
        //the file name is based on the date and time.
        //Example folder: C:\Users\Jonathan\TrafficOwnage\
        //comment this to see the output in NetBeans.
        enableFileOutput();

        
        double[] highwayVelocities = new double[] {80};
        double[] mainRoadVelocities = new double[] {50};
        double[] smallRoadVelocities = new double[] {30};
        
        double kphMsRatio = 1.0 / 3.6;

//        ManhattanMapGenerator gen = new ManhattanMapGenerator();
//        gen.generate(40,
//                40,
//                100.0,
//                new Integer[] {6,20,34},
//                new Integer[] {6,20,34},
//                new Integer[] {12,28},
//                new Integer[] {12,28},
//                scale(highwayVelocities, kphMsRatio), //highway velocities
//                scale(mainRoadVelocities, kphMsRatio), //main road velocities
//                scale(smallRoadVelocities, kphMsRatio) //small road velocities
//        );

        SingleNodeGenerator gen = new SingleNodeGenerator();

        gen.generate(SingleNodeGenerator.NODE_DYNAMIC_TRAFFICLIGHT, new double[] {5000.0,5000.0,5000.0,5000.0},new int[][] {{0,2},{1,3}},50.0 / 3.6,1);

        nodes = gen.getNodes();
        roads = gen.getRoads();

//        int residentialAreas = gen.requestArea(new Rectangle[] {
//            new Rectangle(0,0,11,11),
//            new Rectangle(13,0,14,11),
//            new Rectangle(29,0,11,11),
//
//            new Rectangle(0,13,11,14),
//            new Rectangle(29,13,11,14),
//
//            new Rectangle(0,29,11,11),
//            new Rectangle(13,29,14,11),
//            new Rectangle(29,29,11,11)
//        });
//
//        int innerCity = gen.requestArea(new Rectangle[] {
//            new Rectangle(13,13,14,14)
//        });


        trafficManager.setNodes(nodes);
        trafficManager.setAreas(gen.getAreas());
        
        //                          NAME                                    BENCHMARKED     STARTAREA   ENDAREA     SPAWNINTERVAL(s)    DRIVING
        //                          string                                  boolean         int(area)   int(area)   double(in sec.)     boolean
        trafficManager.addMapping(  "Most congested road",                  true,           0,          4,          7.0,                true);
        trafficManager.addMapping(  "Llittle less congested road",          true,           1,          4,          16.0,               true);
        trafficManager.addMapping(  "Even less congested road",             true,           2,          4,          30.0,               true);
        trafficManager.addMapping(  "Almost not congested road",            true,           3,          4,          60.0,               true);

//        trafficManager.addMapping("Random evening traffic", false,
//                (double) (TimeUnit.HOURS.toSeconds(18)),
//                (double) (TimeUnit.HOURS.toSeconds(20)),
//                ManhattanMapGenerator.ALL_NODES,
//                ManhattanMapGenerator.ALL_NODES,
//                .5,
//                false);
//
//        trafficManager.addMapping("Random evening traffic", false,
//                (double) (TimeUnit.HOURS.toSeconds(20)),
//                (double) (TimeUnit.HOURS.toSeconds(21)),
//                ManhattanMapGenerator.ALL_NODES,
//                ManhattanMapGenerator.ALL_NODES,
//                1.0,
//                false);
//        trafficManager.addMapping("Random evening traffic", false,
//                (double) (TimeUnit.HOURS.toSeconds(21)),
//                (double) (TimeUnit.HOURS.toSeconds(23)) + (TimeUnit.MINUTES.toSeconds(59)) + (TimeUnit.SECONDS.toSeconds(59)),
//                ManhattanMapGenerator.ALL_NODES,
//                ManhattanMapGenerator.ALL_NODES,
//                1.5,
//                false);
//
//        trafficManager.addMapping("Random early morning traffic", false,
//                (double) (TimeUnit.HOURS.toSeconds(0)),
//                (double) (TimeUnit.HOURS.toSeconds(6)),
//                ManhattanMapGenerator.ALL_NODES,
//                ManhattanMapGenerator.ALL_NODES,
//                1.5,
//                false);
//
//        trafficManager.addMapping("Random noon traffic", false,
//                (double) (TimeUnit.HOURS.toSeconds(9)),
//                (double) (TimeUnit.HOURS.toSeconds(15)),
//                ManhattanMapGenerator.ALL_NODES,
//                ManhattanMapGenerator.ALL_NODES,
//                .5,
//                false);
//
//        trafficManager.addMapping("Random morning rush hour traffic", false,
//                (double) (TimeUnit.HOURS.toSeconds(6)),
//                (double) (TimeUnit.HOURS.toSeconds(9)),
//                ManhattanMapGenerator.ALL_NODES,
//                ManhattanMapGenerator.ALL_NODES,
//                .75,
//                false);
//
//        trafficManager.addMapping("Random evening rush hour traffic", false,
//                (double) (TimeUnit.HOURS.toSeconds(15)),
//                (double) (TimeUnit.HOURS.toSeconds(18)),
//                ManhattanMapGenerator.ALL_NODES,
//                ManhattanMapGenerator.ALL_NODES,
//                .75,
//                false);
//
//        trafficManager.addMapping("Residential commuters", false,
//                (double) (TimeUnit.HOURS.toSeconds(6)),
//                (double) (TimeUnit.HOURS.toSeconds(9)),
//                ManhattanMapGenerator.SPAWN_NODES,
//                innerCity,
//                8000,
//                true);
//
//        trafficManager.addMapping("Residential commuters", false,
//                (double) (TimeUnit.HOURS.toSeconds(15)),
//                (double) (TimeUnit.HOURS.toSeconds(18)),
//                innerCity,
//                ManhattanMapGenerator.SPAWN_NODES,
//                8000,
//                true);
//
//        trafficManager.addMapping("Residential to commercial traffic", false,
//                (double) (TimeUnit.HOURS.toSeconds(6)),
//                (double) (TimeUnit.HOURS.toSeconds(9)),
//                residentialAreas,
//                innerCity,
//                4000,
//                false);
//        trafficManager.addMapping("Residential to commercial traffic", false,
//                (double) (TimeUnit.HOURS.toSeconds(15)),
//                (double) (TimeUnit.HOURS.toSeconds(18)),
//                innerCity,
//                residentialAreas,
//                4000,
//                false);
//
//        trafficManager.addMapping("Commuters passing through city", false,
//                (double) (TimeUnit.HOURS.toSeconds(6)),
//                (double) (TimeUnit.HOURS.toSeconds(9)),
//                ManhattanMapGenerator.SPAWN_NODES,
//                ManhattanMapGenerator.SPAWN_NODES,
//                5000,
//                true);
//        trafficManager.addMapping("Commuters passing through city", false,
//                (double) (TimeUnit.HOURS.toSeconds(15)),
//                (double) (TimeUnit.HOURS.toSeconds(18)),
//                ManhattanMapGenerator.SPAWN_NODES,
//                ManhattanMapGenerator.SPAWN_NODES,
//                5000,
//                true);

        setExportMoments(new double[] {
            (TimeUnit.HOURS.toSeconds(0)),
            (TimeUnit.HOURS.toSeconds(1)),
            (TimeUnit.HOURS.toSeconds(2)),
            (TimeUnit.HOURS.toSeconds(3)),
            (TimeUnit.HOURS.toSeconds(4)),
            (TimeUnit.HOURS.toSeconds(5)),
            (TimeUnit.HOURS.toSeconds(6)),
            (TimeUnit.HOURS.toSeconds(7)),
            (TimeUnit.HOURS.toSeconds(8)),
            (TimeUnit.HOURS.toSeconds(9)),
            (TimeUnit.HOURS.toSeconds(10)),
            (TimeUnit.HOURS.toSeconds(11)),
            (TimeUnit.HOURS.toSeconds(12)),
            (TimeUnit.HOURS.toSeconds(13)),
            (TimeUnit.HOURS.toSeconds(14)),
            (TimeUnit.HOURS.toSeconds(15)),
            (TimeUnit.HOURS.toSeconds(16)),
            (TimeUnit.HOURS.toSeconds(17)),
            (TimeUnit.HOURS.toSeconds(18)),
            (TimeUnit.HOURS.toSeconds(19)),
            (TimeUnit.HOURS.toSeconds(20)),
            (TimeUnit.HOURS.toSeconds(21)),
            (TimeUnit.HOURS.toSeconds(22)),
            (TimeUnit.HOURS.toSeconds(23))
        });

        trafficManager.init();

        currentDay = 0;
        sStep = 1.0 / (double) FPS; //Step size in seconds
        msStep = (long) ((sStep * 1000.0) / (double)speedMultiplier); //Step size in milliseconds

        //Init all roads/nodes
        for (Road r : roads) {
            r.init();
        }

        for (Node n : nodes) {
            n.init(this);
        }

        if (listener != null) {
            listener.mapLoaded();
        }
        
        System.out.println("===SETTINGS===");
        System.out.println("DYNAMIC TRAFFIC LIGHTS");        
        System.out.println("-Green time per car: " + TrafficLight.GREEN_TIME_PER_CAR);        
        System.out.println("-First car arrival ignorance time: " + TrafficLight.IGNORE_TRAFFIC_TIME);    
        System.out.println("-Maximum green time: " + TrafficLight.MAX_GREEN_TIME);    
        System.out.println("-Minimum green time: " + TrafficLight.MIN_GREEN_TIME);  
        System.out.println();
        System.out.println("===VELOCITIES===");
        System.out.println("-Highways: " + Arrays.toString(highwayVelocities));
        System.out.println("-Main roads: " + Arrays.toString(mainRoadVelocities));
        System.out.println("-Small roads: " + Arrays.toString(smallRoadVelocities));
        System.out.println();

        initialized = true;
        stop = true;
    }
    
    private void setExportMoments(double[] times) {
        Arrays.sort(times);

        exportMoments = times;

        if (exportMoments.length == 0) {
            nextExportMoment = null;
        } else {

            int i = 0;

            int checked = 0;
            
            while (exportMoments[i] <= overallSimulatedTime && checked != exportMoments.length) {
                i = (i + 1) % exportMoments.length;
                checked++;
            }            

            currentExportMoment = Math.max(0,i % exportMoments.length);
            
            nextExportMoment = exportMoments[currentExportMoment];
        }

    }

    public List<Mapping> getBenchmarkedMappings() {
        return trafficManager.getBenchmarkedMappings();
    }

    public int getSpeedMultiplier() {
        return speedMultiplier;
    }
    
    public void setSpeedMultiplier(int speedMultiplier) {
        synchronized (syncObject) {
            this.speedMultiplier = speedMultiplier;
        msStep = (long) ((sStep * 1000.0) / (double)speedMultiplier); //Step size in milliseconds
        }
    }

    public void init(MainLoopListener listener) {
        init();
        setUIListener(listener);
    }

    public void setUIListener(MainLoopListener listener) {
        this.listener = listener;
    }
    Random randy = new Random();
    private final Object syncObject = new Object();

    public Object getSyncObject() {
        return syncObject;
    }

    public void setRealtime(boolean realtime) {
        this.realtime = realtime;
    }


    public double getSimulationDayTime() {
        return currentDaySimulatedTime;
    }

    public void pause() {
        synchronized (syncObject) {
            if (initialized) {
                run = !run;
            }
        }
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void stop() {
        synchronized (syncObject) {
            if (initialized) {
                stop = true;
            }
        }
    }

    public void start() {
        synchronized (syncObject) {
            if (!initialized) {
                return;
            }

            if (!run && !stop) {
                pause();
                return;
            } else {
                new Thread(runner).start();
            }
        }
    }
    private Runnable runner = new Runnable() {

        public void run() {
            loop();
        }
    };

    public double getCO2Emission() {
        return co2Emission;
    }

    public void loop() {
        run = true;
        stop = false;

        long span, start, end, leftover;

        //double timetaken;
        
        double diff;
        
        long measureSpeedRealStart = System.currentTimeMillis();
        double measureSpeedSimInterval = (double)MEASURE_SPEED_INTERVAL / 1000;
        double measureSpeedSimStart = overallSimulatedTime;

        while (!stop) {

            if (!run) {
                continue;
            }

            start = System.currentTimeMillis();
            
            //FOR TEH AVERAGES WE NEED DIS!
            avgVelocity = velocitySum / (double)carsUpdated;
            avgAcceleration = accelerationSum / (double)carsUpdated;

            accelerationSum = 0.0;
            velocitySum = 0.0;

            carsUpdated = 0;

            overallSimulatedTime += sStep;
            currentDaySimulatedTime += sStep;
            

            synchronized (syncObject) {

                trafficManager.update(overallSimulatedTime, currentDaySimulatedTime, sStep);

                for (Node n : nodes) {
                    n.update(sStep);
                }

                for (Road r : roads) {
                    r.update(sStep);
                    co2Emission += r.pollOveralCO2Emission() / 1000.0;
                }

                
                if (nextExportMoment != null) {
                    diff = currentDaySimulatedTime - nextExportMoment;
                        if (diff >= 0.0 & diff < (sStep * 2.0)) {
                            
                        System.out.println();
                        
                        System.out.println("Export at " + StringFormatter.getTimeString(nextExportMoment));

                        System.out.println("Overall CO2 emission of today: " + (int)co2Emission);

                        trafficManager.export();

                        System.out.println();

                        currentExportMoment = (currentExportMoment + 1) % exportMoments.length;
                        nextExportMoment = exportMoments[currentExportMoment];
                    }
                }

                if (listener != null)
                    listener.nextFrame(sStep);
            }

            
            if (!eternalLoop) {
                if (overallSimulatedTime > overallEndTime)
                    //TODO: FUGLY!!!
                    System.exit(1);
            }

            if (overallSimulatedTime >= ((double)currentDay * DAY)) {

                currentDaySimulatedTime = overallSimulatedTime % DAY;
                co2Emission = 0.0;
                trafficManager.resetBenchmarks();
                currentDay++;

                System.out.println();
                System.out.println("Welcome to day " + currentDay + ".");
                System.out.println();
            }

            end = System.currentTimeMillis();
            
            if ((end - measureSpeedRealStart) >= MEASURE_SPEED_INTERVAL) {
                measureSpeedRealStart = end;

                currentSpeed = (currentDaySimulatedTime - measureSpeedSimStart) / measureSpeedSimInterval;

                measureSpeedSimStart = currentDaySimulatedTime;
            }

            span = end - start;            

            leftover = Math.max(0, msStep - span);
            
            
            
//            currentSpeed = sStep / ((double)timetaken / 1000.0);
//            
//            if (Double.POSITIVE_INFINITY == currentSpeed)
//                System.err.println("MAY NOT HAPPEN!");

            if (realtime && leftover > 0) {
                try {
                    Thread.sleep(leftover);
                } catch (InterruptedException ex) {
                }
            }
        }

        //cleanup
        init();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public int getCarCount() {
        return carCount;
    }

    public void carAdded(Car car) {
        carCount++;
        car.addListener(this);
    }

    public void reachedDestination(Car car, Node destination) {
        carCount--;
    }

    int carsUpdated = 0;
    private double velocitySum;
    private double accelerationSum;
    private double avgVelocity;
    private double avgAcceleration;

    public void positionChanged(Car car) {
        if (Double.isInfinite(car.getAcceleration()) || Double.isNaN(car.getAcceleration()))
            return;

        velocitySum += car.getVelocity();
        accelerationSum += car.getAcceleration();

        carsUpdated++;
    }

    public double getAverageVelocity() {
        return avgVelocity;
    }

    public double getAverageAcceleration() {
        return avgAcceleration;
    }
}
