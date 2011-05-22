/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import trafficownage.ui.UIListener;

/**
 *
 * @author Gerrit
 */
public class MainLoop implements Runnable {
    private final static long FPS = 25;

    private final static long SPEED_MULTIPLIER = 4;

    private List<Road> roads;
    private List<Node> nodes;

    private boolean run;
    private boolean realtime = true;

    private UIListener listener = null;

    private Node ze_dummy;
    private Node ze_intersection;

    public void init() {

        double SIDE_DISTANCE = 100.0;
        double SLOPE_DISTANCE = Math.sqrt(2*(SIDE_DISTANCE * SIDE_DISTANCE));
        double MAX_SPEED = 120.0 / 3.6;

        /*Node[] node_array = {
            new DummyNode(new Point2D.Double(0.0,0.0)), //0
            new DummyNode(new Point2D.Double(-SIDE_DISTANCE,0.0)), //1
            new DummyNode(new Point2D.Double(SIDE_DISTANCE,0.0)), //2
            new DummyNode(new Point2D.Double(0.0,SIDE_DISTANCE)), //3
            new DummyNode(new Point2D.Double(0.0,-SIDE_DISTANCE)), //4
            new DummyNode(new Point2D.Double(-SIDE_DISTANCE,-SIDE_DISTANCE)), //5
            new DummyNode(new Point2D.Double(SIDE_DISTANCE,-SIDE_DISTANCE)), //6
            new DummyNode(new Point2D.Double(-SIDE_DISTANCE,SIDE_DISTANCE)), //7
            new DummyNode(new Point2D.Double(SIDE_DISTANCE,SIDE_DISTANCE)), //8
            new DummyNode(new Point2D.Double(0.0,-2 * SIDE_DISTANCE)), //9
            new DummyNode(new Point2D.Double(0.0,2 * SIDE_DISTANCE)), //10
            new DummyNode(new Point2D.Double(2 * SIDE_DISTANCE,0.0)), //11
            new DummyNode(new Point2D.Double(-2 * SIDE_DISTANCE,0.0)) //12
        };

        Road[] road_array = {
            new Road(node_array[0],node_array[1],SIDE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[0],node_array[2],SIDE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[0],node_array[3],SIDE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[0],node_array[4],SIDE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[0],node_array[5],SLOPE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[0],node_array[6],SLOPE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[0],node_array[7],SLOPE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[0],node_array[8],SLOPE_DISTANCE,MAX_SPEED,1,false),

            //new Road(n[9],n[4],100.0,13.9,1,false),
            new Road(node_array[9],node_array[5],SLOPE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[9],node_array[6],SLOPE_DISTANCE,MAX_SPEED,1,false)
                    ,
            new Road(node_array[10],node_array[7],SLOPE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[10],node_array[8],SLOPE_DISTANCE,MAX_SPEED,1,false),

            new Road(node_array[11],node_array[6],SLOPE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[11],node_array[8],SLOPE_DISTANCE,MAX_SPEED,1,false),

            new Road(node_array[12],node_array[5],SLOPE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[12],node_array[7],SLOPE_DISTANCE,MAX_SPEED,1,false)
        };*/

        ze_dummy = new DummyNode(new Point2D.Double(-SIDE_DISTANCE,0.0));
        ze_intersection = new NormalJunction(new Point2D.Double(0.0,0.0));

        Node[] node_array = {
            ze_intersection, //0
            ze_dummy, //1
            new DummyNode(new Point2D.Double(SIDE_DISTANCE,0.0)), //2
            new DummyNode(new Point2D.Double(0.0,SIDE_DISTANCE)), //3
            new DummyNode(new Point2D.Double(0.0,-SIDE_DISTANCE)), //4
        };

        Road[] road_array = {
            new Road(node_array[0],node_array[1],SIDE_DISTANCE,MAX_SPEED,2,false),
            new Road(node_array[0],node_array[2],SIDE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[0],node_array[3],SIDE_DISTANCE,MAX_SPEED,1,false),
            new Road(node_array[0],node_array[4],SIDE_DISTANCE,MAX_SPEED,1,false),
        };


        nodes = new ArrayList<Node>();
        nodes.addAll(Arrays.asList(node_array));

        roads = new ArrayList<Road>();
        roads.addAll(Arrays.asList(road_array));
        


    }

    public void init(UIListener listener) {
        init();
        setUIListener(listener);
    }

    public void setUIListener(UIListener listener) {
        this.listener = listener;
    }

    Random randy = new Random();
    private final Object syncObject = new Object();


    public void addCar() {
        
        synchronized(syncObject) {
            Car car = new Car();

            if (randy.nextInt(2) == 0)
                car.init(CarType.CAR, DriverType.NORMAL);
            else
                car.init(CarType.LORRY, DriverType.NORMAL);

            ze_dummy.getRoad(ze_intersection).getLanes(ze_intersection).get(0).addCar(car);
        }
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

        for (Road r : roads)
            r.init();

        while (run) {

            start = System.currentTimeMillis();

            synchronized(syncObject){

                for (Node n : nodes) {
                    n.update(s_step);
                }

                for (Road r : roads) {

                    r.update(s_step);

                    simulated_time += s_step;

                    listener.carsUpdated();
                    
                }
            }
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
