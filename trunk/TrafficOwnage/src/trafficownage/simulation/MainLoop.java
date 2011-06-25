/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import trafficownage.util.ManhattanMapGenerator;

/**
 *
 * @author Gerrit
 */
public class MainLoop {
    private final static long FPS = 20;

    private final static long SPEED_MULTIPLIER = 32;

    private List<Road> roads;
    private List<Node> nodes;

    private boolean initialized;
    private boolean run;
    private boolean stop;
    private boolean realtime = true;

    private long msStep;
    private double sStep;

    private MainLoopListener listener = null;

    private SpawnManager spawnManager = new SpawnManager();

    private static final double DAY = (double)TimeUnit.HOURS.toSeconds(24);

    public MainLoop() {
        initialized = false;
    }

    public void init() {

        simulatedTime = (double)TimeUnit.HOURS.toSeconds(6);

        ManhattanMapGenerator gen = new ManhattanMapGenerator();
        gen.generate(8,4,120.0,4,5,15);

        nodes = gen.getNodes();
        roads = gen.getRoads();

        spawnManager.init(nodes,gen.getAreas());

        spawnManager.addMapping(
                (double)(TimeUnit.HOURS.toSeconds(6)),
                (double)(TimeUnit.HOURS.toSeconds(9)),
                ManhattanMapGenerator.SPAWN_NODES,
                ManhattanMapGenerator.SPAWN_NODES,
                10000);

        spawnManager.addMapping(
                (double)(TimeUnit.HOURS.toSeconds(8)),
                (double)(TimeUnit.HOURS.toSeconds(10)),
                ManhattanMapGenerator.SPAWN_NODES,
                ManhattanMapGenerator.LOCAL_NODES,
                7500);

        spawnManager.addMapping(
                ManhattanMapGenerator.LOCAL_NODES,
                ManhattanMapGenerator.LOCAL_NODES,
                5.0);


        sStep = 1.0 / (double)FPS; //Step size in seconds
        msStep = (long)(sStep * 1000.0) / SPEED_MULTIPLIER; //Step size in milliseconds

        //Init all roads/nodes
        for (Node n : nodes)
            n.init();

        if (listener != null)
            listener.mapLoaded();

        initialized = true;
        stop = true;
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
        if (initialized)
            run = !run;
    }

    public void stop() {
        if (initialized) {
            stop = true;
        }
    }

    public void start() {
        if (!initialized)
            return;

        if (!run && !stop) {
            pause();
            return;
        } else {
            new Thread(runner).start();
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

        long span,start,end,leftover;

        while (!stop) {

            if (!run)
                continue;

            start = System.currentTimeMillis();

            //System.out.println(simulated_time);

            synchronized(syncObject){

                spawnManager.update(simulatedTime,sStep);

                for (Node n : nodes)
                    n.update(sStep);
                

                for (Road r : roads) 
                    r.update(sStep);

                listener.nextFrame();
            }

            simulatedTime = (simulatedTime + sStep) % DAY;

            end = System.currentTimeMillis();

            span = end - start;

            leftover = Math.max(0, msStep - span);

            if (realtime) {
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
}
