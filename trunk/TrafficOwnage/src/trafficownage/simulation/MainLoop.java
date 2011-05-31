/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import trafficownage.util.MapGenerator;
import trafficownage.ui.SimulationUpdateListener;

/**
 *
 * @author Gerrit
 */
public class MainLoop implements Runnable {
    private final static long FPS = 25;

    private final static long SPEED_MULTIPLIER = 16;

    private List<Road> roads;
    private List<Node> nodes;

    private boolean run;
    private boolean realtime = true;

    private SimulationUpdateListener listener = null;


    Road spawnroad = null;

    public void init() {

        MapGenerator gen = new MapGenerator();
        gen.generate(2000.0,75,5,10.0);

        nodes = gen.getNodes();
        roads = gen.getRoads();


    }

    public void init(SimulationUpdateListener listener) {
        init();
        setUIListener(listener);
    }

    public void setUIListener(SimulationUpdateListener listener) {
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

    public void run() {
        run = true;

        double s_step = 1.0 / (double)FPS; //Step size in seconds

        long ms_step = (long)(s_step * 1000.0) / SPEED_MULTIPLIER; //Step size in milliseconds

        long span,start,end,leftover;

        double simulated_time = 0.0;

        //Init all roads/nodes

        for (Node n : nodes)
            n.init();

        while (run) {

            start = System.currentTimeMillis();

            //System.out.println(simulated_time);

            synchronized(syncObject){

                for (Node n : nodes) {
                    n.update(s_step);
                }

                for (Road r : roads) {

                    for (RoadSegment rs : r.getSegments())
                        rs.update(s_step);

                    listener.carsUpdated();
                    
                }
            }

            simulated_time += s_step;

            end = System.currentTimeMillis();

            span = end - start;

            leftover = Math.max(0, ms_step - span);

            if (realtime) {
                try {
                    Thread.sleep(leftover);
                } catch (InterruptedException ex) {

                }
            }
        }
    }

    public void stop() {
        run = false;
    }
    
    public List<Node> getNodes() {
        return nodes;
    }

    public List<Road> getRoads() {
        return roads;
    }
}
