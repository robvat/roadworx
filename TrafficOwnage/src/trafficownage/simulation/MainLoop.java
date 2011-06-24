/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import trafficownage.util.BigMapGenerator;
import trafficownage.ui.SimulationUpdateListener;
import trafficownage.util.ManhattanMapGenerator;

/**
 *
 * @author Gerrit
 */
public class MainLoop implements Runnable {
    private final static long FPS = 20;

    private final static long SPEED_MULTIPLIER = 4;

    private List<Road> roads;
    private List<Node> nodes;

    private boolean run;
    private boolean realtime = true;

    private SimulationUpdateListener listener = null;


    Road spawnroad = null;

    public void init() {
//
//        BigMapGenerator gen = new BigMapGenerator();
//        gen.generate(10000.0,250,20,35,5.0);

//        ManhattanMapGenerator gen = new ManhattanMapGenerator();
//        gen.generate(64,16,80.0,8,5,5);
//
//        nodes = gen.getNodes();
//        roads = gen.getRoads();

        Node[] nodearray = new Node[] {
            new SpawnNode(new Point2D.Double(-400.0,0.0), 5.0),
            new DrivethroughNode(new Point2D.Double(0.0,0.0)),
            new DrivethroughNode(new Point2D.Double(400.0,0.0))

        };

        Road r1 = new Road("Hoofdweggetje");

        RoadSegment[] segments1 = new RoadSegment[] {
            new RoadSegment(r1,200.0 / 3.6, nodearray[0],nodearray[1]),
            new RoadSegment(r1,200.0 / 3.6, nodearray[1],nodearray[2])
        };
//
//        RoadSegment endsegment = new RoadSegment(r1,nodearray[3],nodearray[4]);
//
        int i = 0;

        for (RoadSegment segment : segments1) {
            segment.addLeftStartLane(i,false);
            i++;
            segment.addLeftStartLane(i,false);
            i++;
            r1.addLast(segment);
        }
//
//        endsegment.addLeftEndLane(i, 50.0/3.6, false);
//        endsegment.addLeftStartLane(i, 50.0/3.6, false);
//        r1.addLast(endsegment);
//
//        Road r2 = new Road("Zijweggetje");
//
//        RoadSegment[] segments2 = new RoadSegment[] {
//            new RoadSegment(r1,nodearray[5],nodearray[2]),
//            new RoadSegment(r1,nodearray[2],nodearray[6])
//        };
//
//        for (RoadSegment segment : segments2) {
//            segment.addLeftStartLane(i,50.0/3.6,false);
//            segment.addLeftEndLane(i,50.0/3.6,false);
//            r2.addLast(segment);
//        }
//
        nodes = Arrays.asList(nodearray);
        roads = Arrays.asList(new Road[] {r1});

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

                for (Node n : nodes)
                    n.update(s_step);
                

                for (Road r : roads) 
                    r.update(s_step);
                

                listener.carsUpdated();

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
