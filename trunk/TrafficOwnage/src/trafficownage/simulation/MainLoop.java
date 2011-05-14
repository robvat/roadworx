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
    private final static long FPS = 30;

    private List<Road> roads;
    private List<Node> nodes;

    private boolean run;

    private UIListener listener = null;

    public void init() {

        double SIDE_DISTANCE = 200.0;
        double SLOPE_DISTANCE = Math.sqrt(2*(SIDE_DISTANCE * SIDE_DISTANCE));
        double MAX_SPEED = 120.0 / 3.6;

        Node[] node_array = {
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
        };


        nodes = new ArrayList<Node>();
        nodes.addAll(Arrays.asList(node_array));

        roads = new ArrayList<Road>();
        roads.addAll(Arrays.asList(road_array));
        
        for (Node n : nodes)
            n.init();
        
        for (Road r : roads)
            r.init();


    }

    public void init(UIListener listener) {
        init();
        setUIListener(listener);
    }

    public void setUIListener(UIListener listener) {
        this.listener = listener;
    }

    Random randy = new Random();
    public void addCar() {

        for (Road road : roads) {
            for (Lane lane : road.getAllLanes()) {
                Car car = new Car();
                car.init(CarType.CAR, DriverType.NORMAL);
                lane.addCar(car);
            }
        }
        
        /*Road road = roads.get(randy.nextInt(roads.size()));

        Lane lane = road.getAllLanes().get(randy.nextInt(road.getAllLanes().size()));

        Car car = new Car();
        car.init(CarType.CAR, DriverType.NORMAL);
        lane.addCar(car);*/
    }

    public void run() {


        run = true;

        double s_step = 1.0 / (double)FPS; //Step size in seconds

        long ms_step = (long)(s_step * 1000.0); //Step size in milliseconds

        long span,start,end,leftover;

        double simulated_time = 0.0;
        double car_counter = 0.0;

        while (run) {

            start = System.currentTimeMillis();

            for (Node n : nodes) {
                n.update(s_step);
            }

            for (Road r : roads) {

                r.update(s_step);

                //TODO: THIS IS TEST CODE, SHOULD BE DELETED SOME TIME
                if (car_counter > 2.5) {
                    //lets add a car
                    car_counter = 0.0;
                    
                }

                simulated_time += s_step;
                car_counter += s_step;

                //System.out.println("Simulated: " + simulated_time);

                if (listener != null)
                    new Thread(new Runnable() {
                        public void run() {
                            listener.carsUpdated();
                        }
                    }).start();

            }

            end = System.currentTimeMillis();

            span = end - start;

            leftover = Math.max(0, ms_step - span);

            try {
                Thread.sleep(leftover);
            } catch (InterruptedException ex) {

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
