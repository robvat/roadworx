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
        /*Node[] n = {
            new DummyNode(new Point2D.Double(0.0,0.0)),
            new DummyNode(new Point2D.Double(-100.0,0.0)),
            new DummyNode(new Point2D.Double(100.0,0.0)),
            new DummyNode(new Point2D.Double(0.0,100.0)),
            new DummyNode(new Point2D.Double(0.0,-100.0)),
            new DummyNode(new Point2D.Double(-100.0,-100.0)),
            new DummyNode(new Point2D.Double(100.0,-100.0)),
            new DummyNode(new Point2D.Double(-100.0,100.0)),
            new DummyNode(new Point2D.Double(100.0,100.0))
        };

        nodes = new ArrayList<Node>();
        nodes.addAll(Arrays.asList(n));

        Road[] r = {
            new Road(n[0],n[1],100.0,13.9,1,false),
            new Road(n[0],n[2],100.0,13.9,1,false),
            new Road(n[0],n[3],100.0,13.9,1,false),
            new Road(n[0],n[4],100.0,13.9,1,false),
            new Road(n[0],n[5],142.0,13.9,1,false),
            new Road(n[0],n[6],142.0,13.9,1,false),
            new Road(n[0],n[7],142.0,13.9,1,false),
            new Road(n[0],n[8],142.0,13.9,1,false),
            new Road(n[1],n[2],200.0,13.9,1,false)
        };*/

        Node[] n = {
            new DummyNode(new Point2D.Double(-150.0,0.0)),
            new DummyNode(new Point2D.Double(150.0,0.0))
        };

        nodes = new ArrayList<Node>();
        nodes.addAll(Arrays.asList(n));

        Road[] r = {
            new Road(n[0],n[1],300.0,56.0,1,false),
        };

        roads = new ArrayList<Road>();
        roads.addAll(Arrays.asList(r));
    }

    public void init(UIListener listener) {
        init();
        setUIListener(listener);
    }

    public void setUIListener(UIListener listener) {
        this.listener = listener;
    }

    public void addCar() {
        Road road = roads.get(0);
        Lane lane = road.getAllLanes().get(0);
        Car car = new Car();
        car.init(CarType.CAR, DriverType.NORMAL);
        lane.addCar(car);
    }

    public void run() {

        Random randy = new Random();

        run = true;

        double s_step = 1.0 / (double)FPS; //Step size in seconds

        long ms_step = (long)(s_step * 1000.0); //Step size in milliseconds

        long span,start,end,leftover;

        double simulated_time = 0.0;
        double car_counter = 0.0;

        while (run) {
            for (Road r : roads) {

                start = System.currentTimeMillis();

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

                end = System.currentTimeMillis();

                span = end - start;

                leftover = Math.max(0, ms_step - span);

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
