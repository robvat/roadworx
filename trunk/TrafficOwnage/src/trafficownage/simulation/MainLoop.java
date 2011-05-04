/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import trafficownage.ui.UIListener;

/**
 *
 * @author Gerrit
 */
public class MainLoop implements Runnable {
    private final static long TIMESTEP = 10;

    private List<Road> roads;
    private List<Node> nodes;

    private boolean run;

    private UIListener listener = null;

    public void init() {
        Node[] n = {
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
            new Road(n[0],n[1],100.0,13.9,1,false,false),
            new Road(n[0],n[2],100.0,13.9,1,false,false),
            new Road(n[0],n[3],100.0,13.9,1,false,false),
            new Road(n[0],n[4],100.0,13.9,1,false,false),
            new Road(n[0],n[5],142.0,13.9,1,false,false),
            new Road(n[0],n[6],142.0,13.9,1,false,false),
            new Road(n[0],n[7],142.0,13.9,1,false,false),
            new Road(n[0],n[8],142.0,13.9,1,false,false),
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

    public void run() {

        run = true;

        double step = (double)TIMESTEP / 1000.0;

        long start,end;

        while (run) {
            for (Road r : roads) {
                start = System.currentTimeMillis();
                r.update(step);

                if (listener != null)
                    listener.carsUpdated();

                end = System.currentTimeMillis();
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
