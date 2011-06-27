/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import trafficownage.util.ManhattanMapGenerator;

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
    private int carCount;
    private MainLoopListener listener = null;
    private SpawnManager spawnManager = new SpawnManager();
    private static final double DAY = (double) TimeUnit.HOURS.toSeconds(24);

    public MainLoop() {
        initialized = false;
        speedMultiplier = 16;
    }

    public void init() {

        simulatedTime = (double) TimeUnit.HOURS.toSeconds(8);

        ManhattanMapGenerator gen = new ManhattanMapGenerator();
        gen.generate(16, 16, 100.0, 4, 5, 15);

        nodes = gen.getNodes();
        roads = gen.getRoads();

        spawnManager.init(nodes, gen.getAreas());

        spawnManager.addMapping(false,
                (double) (TimeUnit.HOURS.toSeconds(8)),
                (double) (TimeUnit.HOURS.toSeconds(9)),
                ManhattanMapGenerator.SPAWN_NODES,
                ManhattanMapGenerator.SPAWN_NODES,
                5000);

        spawnManager.addMapping(true,
                (double) (TimeUnit.HOURS.toSeconds(8)),
                (double) (TimeUnit.HOURS.toSeconds(9)),
                ManhattanMapGenerator.LOCAL_NODES,
                ManhattanMapGenerator.SPAWN_NODES,
                10000);

        /*spawnManager.addMapping(false,
                (double) (TimeUnit.HOURS.toSeconds(8)),// + TimeUnit.MINUTES.toSeconds(2)),
                (double) (TimeUnit.HOURS.toSeconds(10)),
                ManhattanMapGenerator.SPAWN_NODES,
                ManhattanMapGenerator.LOCAL_NODES,
                500000);

        /*spawnManager.addMapping(false,
                ManhattanMapGenerator.LOCAL_NODES,
                ManhattanMapGenerator.LOCAL_NODES,
                0.5);*/


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

            //System.out.println(simulated_time);

            synchronized (syncObject) {

                spawnManager.update(simulatedTime, sStep);

                for (Node n : nodes) {
                    n.update(sStep);
                }


                for (Road r : roads) {
                    r.update(sStep);
                }

                if (listener != null)
                    listener.nextFrame(sStep);
            }

            simulatedTime = (simulatedTime + sStep) % DAY;

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
