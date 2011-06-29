/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import trafficownage.simulation.TrafficManager.Mapping;
import trafficownage.util.ManhattanMapGenerator;
import trafficownage.util.TimeString;

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
    private boolean realtime = false;
    private long msStep;
    private double sStep;
    private double currentSpeed;

    private double co2Emission;

    private int carCount;
    private MainLoopListener listener = null;
    private TrafficManager trafficManager = new TrafficManager();
    private static final double DAY = (double) TimeUnit.HOURS.toSeconds(24);

    private double[] exportMoments;
    private int currentExportMoment;
    private Double nextExportMoment;

    public MainLoop() {
        initialized = false;
        speedMultiplier = 16;
    }

    public void init() {

        simulatedTime = (double) TimeUnit.HOURS.toSeconds(0);

        ManhattanMapGenerator gen = new ManhattanMapGenerator();
        gen.generate(40, 40, 100.0, new Integer[] {6,20,34}, new Integer[] {6,20,34}, new Integer[] {12,28}, new Integer[] {12,28});

        nodes = gen.getNodes();
        roads = gen.getRoads();

        int residentialAreas = gen.requestArea(new Rectangle[] {
            new Rectangle(0,0,11,11),
            new Rectangle(13,0,14,11),
            new Rectangle(29,0,11,11),

            new Rectangle(0,13,11,14),
            new Rectangle(29,13,11,14),

            new Rectangle(0,29,11,11),
            new Rectangle(13,29,14,11),
            new Rectangle(29,29,11,11)
        });

        int innerCity = gen.requestArea(new Rectangle[] {
            new Rectangle(13,13,14,14)
        });


        trafficManager.setNodes(nodes);
        trafficManager.setAreas(gen.getAreas());

        trafficManager.addMapping("Random evening traffic", false,
                (double) (TimeUnit.HOURS.toSeconds(20)),
                (double) (TimeUnit.HOURS.toSeconds(23)) + (TimeUnit.MINUTES.toSeconds(59)) + (TimeUnit.SECONDS.toSeconds(59)),
                ManhattanMapGenerator.ALL_NODES,
                ManhattanMapGenerator.ALL_NODES,
                2.0,
                false);

        trafficManager.addMapping("Random early morning traffic", false,
                (double) (TimeUnit.HOURS.toSeconds(0)),
                (double) (TimeUnit.HOURS.toSeconds(7)),
                ManhattanMapGenerator.ALL_NODES,
                ManhattanMapGenerator.ALL_NODES,
                2.0,
                false);
        
        trafficManager.addMapping("Random noon traffic", false,
                (double) (TimeUnit.HOURS.toSeconds(9)),
                (double) (TimeUnit.HOURS.toSeconds(15)),
                ManhattanMapGenerator.ALL_NODES,
                ManhattanMapGenerator.ALL_NODES,
                3.0,
                false);

        trafficManager.addMapping("Random morning rush hour traffic", false,
                (double) (TimeUnit.HOURS.toSeconds(7)),
                (double) (TimeUnit.HOURS.toSeconds(9)),
                ManhattanMapGenerator.ALL_NODES,
                ManhattanMapGenerator.ALL_NODES,
                .75,
                false);

        trafficManager.addMapping("Random evening rush hour traffic", false,
                (double) (TimeUnit.HOURS.toSeconds(15)),
                (double) (TimeUnit.HOURS.toSeconds(18)),
                ManhattanMapGenerator.ALL_NODES,
                ManhattanMapGenerator.ALL_NODES,
                .75,
                false);

        trafficManager.addMapping("Residential commuters", false,
                (double) (TimeUnit.HOURS.toSeconds(6)),
                (double) (TimeUnit.HOURS.toSeconds(9)),
                ManhattanMapGenerator.SPAWN_NODES,
                innerCity,
                8000,
                true);

        trafficManager.addMapping("Residential commuters", false,
                (double) (TimeUnit.HOURS.toSeconds(15)),
                (double) (TimeUnit.HOURS.toSeconds(18)),
                innerCity,
                ManhattanMapGenerator.SPAWN_NODES,
                8000,
                true);

        trafficManager.addMapping("Residential to commercial traffic", false,
                (double) (TimeUnit.HOURS.toSeconds(6)),
                (double) (TimeUnit.HOURS.toSeconds(9)),
                residentialAreas,
                innerCity,
                3000,
                false);
        trafficManager.addMapping("Residential to commercial traffic", false,
                (double) (TimeUnit.HOURS.toSeconds(6)),
                (double) (TimeUnit.HOURS.toSeconds(9)),
                innerCity,
                residentialAreas,
                3000,
                false);

        trafficManager.addMapping("Commuters passing through city", false,
                (double) (TimeUnit.HOURS.toSeconds(6)),
                (double) (TimeUnit.HOURS.toSeconds(9)),
                ManhattanMapGenerator.SPAWN_NODES,
                ManhattanMapGenerator.SPAWN_NODES,
                5000,
                true);
        trafficManager.addMapping("Commuters passing through city", false,
                (double) (TimeUnit.HOURS.toSeconds(6)),
                (double) (TimeUnit.HOURS.toSeconds(9)),
                ManhattanMapGenerator.SPAWN_NODES,
                ManhattanMapGenerator.SPAWN_NODES,
                5000,
                true);

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

            while (exportMoments[i] <= simulatedTime) 
                i = (i + 1) % exportMoments.length;
            

            currentExportMoment = (i - 1) % exportMoments.length;
            
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
    private double simulatedTime;

    public double getSimulatedTime() {
        return simulatedTime;
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

        double timetaken;

        while (!stop) {

            if (!run) {
                continue;
            }

            start = System.currentTimeMillis();

            simulatedTime += sStep;
            if (simulatedTime >= DAY) {
                simulatedTime = 0.0;
                co2Emission = 0.0;
            }

            synchronized (syncObject) {

                trafficManager.update(simulatedTime, sStep);

                for (Node n : nodes) {
                    n.update(sStep);
                }

                for (Road r : roads) {
                    r.update(sStep);
                    co2Emission += r.pollOveralCO2Emission() / 1000.0;
                }


                if (nextExportMoment != null && simulatedTime >= nextExportMoment) {

                    System.out.println("Export at " + TimeString.getTimeString(nextExportMoment));

                    System.out.println("Overall CO2 emission of today: " + (int)co2Emission);

                    trafficManager.export();

                    System.out.println();

                    currentExportMoment = (currentExportMoment + 1) % exportMoments.length;
                    nextExportMoment = exportMoments[currentExportMoment];
                }

                if (listener != null)
                    listener.nextFrame(sStep);
            }


            end = System.currentTimeMillis();

            span = end - start;

            leftover = Math.max(0, msStep - span);

            timetaken = Math.max(.1,(double)span + leftover);
            
            currentSpeed = sStep / ((double)timetaken / 1000.0);
            if (Double.POSITIVE_INFINITY == currentSpeed)
                System.err.println("MAY NOT HAPPEND!");

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
}
