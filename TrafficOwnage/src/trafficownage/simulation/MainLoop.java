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

        

        //MapGenerator gen = new MapGenerator();

        //gen.generate(200.0,50);

        Node[] nodeArray = new Node[] {
                new DrivethroughNode(new Point2D.Double(-500.0,0)),
                new DrivethroughNode(new Point2D.Double(-400.0,0)),
                new DrivethroughNode(new Point2D.Double(-300.0,0)),
                new DrivethroughNode(new Point2D.Double(-200.0,0)),
                new DrivethroughNode(new Point2D.Double(-100.0,0)),
                new Roundabout(new Point2D.Double(0.0,0.0),20.0),
                new DrivethroughNode(new Point2D.Double(100.0,0.0)),
                new DrivethroughNode(new Point2D.Double(200.0,0.0)),
                new DrivethroughNode(new Point2D.Double(300.0,0.0)),
                new DrivethroughNode(new Point2D.Double(400.0,0.0)),
                new DrivethroughNode(new Point2D.Double(500.0,0.0)),
                new DrivethroughNode(new Point2D.Double(0.0,100.0)),
                new DrivethroughNode(new Point2D.Double(0.0,-100.0))

        };

        Road r = new Road("Mainroad");



        for (int i = 0; i < 10; i++) {
            RoadSegment rs = new RoadSegment(nodeArray[i], nodeArray[i+1]);

            for (int j = 0; j < 2; j++) {
                rs.addLeftStartLane(j, 50.0 / 3.6, false);
                rs.addLeftEndLane(j, 50.0 / 3.6, false);
            }

            r.addLast(rs);
        }

        spawnroad = new Road ("Sideroad");

        RoadSegment rs1 = new RoadSegment(nodeArray[11],nodeArray[5]);
        RoadSegment rs2 = new RoadSegment(nodeArray[5],nodeArray[12]);

        for (int j = 0; j < 2; j++) {
            rs1.addLeftStartLane(j, 50.0 / 3.6, false);
            rs1.addLeftEndLane(j, 50.0 / 3.6, false);
            rs2.addLeftStartLane(j, 50.0 / 3.6, false);
            rs2.addLeftEndLane(j, 50.0 / 3.6, false);
        }

        spawnroad.addLast(rs1);
        spawnroad.addLast(rs2);


          nodes = Arrays.asList(nodeArray);
          roads = Arrays.asList(new Road[] {r,spawnroad});

//        MapGenerator gen = new MapGenerator();
//        gen.generate(500.0,25);
//
//        nodes = gen.getNodes();
//        roads = gen.getRoads();


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


    public Car addCar() {
        
        synchronized(syncObject) {

            Car car;

            Road r = spawnroad;//roads.get(randy.nextInt(roads.size()));

            car = generateRandomCar();
            r.getFirstSegment().getDestinationLanes(r.getFirstSegment().getEndNode()).get(randy.nextInt(2)).addCar(car);
            
            car = generateRandomCar();
            r.getLastSegment().getDestinationLanes(r.getLastSegment().getStartNode()).get(randy.nextInt(2)).addCar(car);
            //r.getFirstSegment().getDestinationLanes(r.getFirstSegment().getStartNode()).get(1).addCar(car);
            //ze_dummy.getRoad(ze_intersection).getLanes(ze_intersection).get(0).addCar(car);

            return car;
        }
    }

    public Car generateRandomCar() {
        Car car = new Car();

            int rand = randy.nextInt(3);

            if (rand == 0)
                car.init(CarType.CAR, DriverType.NORMAL);
            else if (rand == 1)
                car.init(CarType.LORRY, DriverType.NORMAL);
            else if (rand == 2)
                car.init(CarType.MINICAR, DriverType.NORMAL);
            return car;
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
