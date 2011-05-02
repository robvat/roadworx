/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import trafficownage.simulation.MainLoop;
import trafficownage.simulation.Node;
import trafficownage.simulation.Road;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class MapComponent extends JComponent implements MouseWheelListener, MouseMotionListener, MouseListener {

    MainLoop mainLoop;

    BufferedImage back_layer;
    BufferedImage car_layer;

    private double ppm;
    private Point2D.Double center;
    private double width;
    private double height;
    private Rectangle2D.Double frame_bounds;

    public void init(MainLoop mainLoop) {
        this.mainLoop = mainLoop;

        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        initMap();

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (mainLoop == null)
            return;

        Graphics2D gr;

        width = (double)getWidth();
        height = (double)getHeight();

        back_layer = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_ARGB);
        gr = (Graphics2D)back_layer.createGraphics();

        drawMap(gr);

        gr = (Graphics2D)g;

        gr.drawImage(back_layer,0,0,null);

    }

    private final static Color ROADCOLORS[] = {new Color(255,0,0), new Color(255,48,48), new Color(255,96,96)};
    private final static Color NODECOLORS[] = {new Color(24,64,255), new Color(32,72,255), new Color(48,96,255)};
    private final static double[] NODERADIUSES = {10.0,15.0,25.0};
    private final static double[] ROADWIDTHS = {4.0,6.0,10.0};

    private void drawMap(Graphics2D gr) {
        double halfwidth = width / 2;
        double halfheight = height / 2;

        double mpp = 1.0/ppm;

        frame_bounds = new Rectangle2D.Double(center.x - (mpp * halfwidth), center.y - (mpp * halfheight), mpp * width, mpp * height);

        for (MapRoad r : map_roads) {
            if (r.inBounds(frame_bounds))
                drawRoad(gr,r);
        }

        for (MapNode n : map_nodes) {
            if (n.inBounds(frame_bounds))
                drawNode(gr,n);
        }
    }

    private Point2D.Double start_point, end_point, point;
    
    private void drawRoad(Graphics2D gr, MapRoad r) {
        start_point = r.getStartPoint();
        end_point = r.getEndPoint();

        gr.setColor(ROADCOLORS[r.getPriority()]);
        gr.setStroke(new BasicStroke((int)(ppm * ROADWIDTHS[r.getPriority()])));

        gr.drawLine(
                (int)(ppm * (frame_bounds.getMaxX() - start_point.x)),
                (int)(ppm * (frame_bounds.getMaxY() - start_point.y)),
                (int)(ppm * (frame_bounds.getMaxX() - end_point.x)),
                (int)(ppm * (frame_bounds.getMaxY() - end_point.y))
                );
    }

    private void drawNode(Graphics2D gr, MapNode n) {
        point = n.getLocation();

        int x = (int)(ppm * (frame_bounds.getMaxX() - point.x));
        int y = (int)(ppm * (frame_bounds.getMaxY() - point.y));

        int r = (int)(ppm * NODERADIUSES[n.getPriority()]);
        
        int d = r*2;

        gr.setColor(NODECOLORS[n.getPriority()]);
        gr.fillOval(x-r, y-r, d, d);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        
        if (e.getWheelRotation() < 0) {
            //zoom in
            ppm *= 1.25;
        } else {
            ppm *= .8;
        }

        repaint();
    }

    Point offset;

    public void mouseDragged(MouseEvent e) {
        double mpp = 1.0 / ppm;
        center.x -= mpp * ((double)offset.x - e.getX());
        center.y -= mpp * ((double)offset.y - e.getY());
        repaint();
        offset = e.getPoint();
    }


    public void mousePressed(MouseEvent e) {
        offset = e.getPoint();
    }

    public void mouseMoved(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    private class MapNode {
        private Point2D.Double location;

        private int priority;

        public MapNode(Point2D.Double location) {
            this(location,1);
        }

        public MapNode(Point2D.Double location, int priority) {
            this.location = location;
        }

        public Point2D.Double getLocation() {
            return location;
        }
        
        public boolean inBounds(Rectangle2D.Double rect) {
            return (location.x >= rect.getMinX() && location.x <= rect.getMaxX() && location.y >= rect.getMinY() && location.y <= rect.getMaxY());
        }

        public int getPriority() {
            return priority;
        }
    }

    private class MapRoad {
        private Point2D.Double startPoint, endPoint;
        private Point2D.Double[] points;

        private int priority;

        public MapRoad(Point2D.Double startPoint, Point2D.Double endPoint) {
            this(startPoint, endPoint, 1);

        }
        public MapRoad(Point2D.Double startPoint, Point2D.Double endPoint, int priority) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;

            this.points = new Point2D.Double[] {startPoint, endPoint};

            this.priority = priority;
        }

        public boolean inBounds(Rectangle2D.Double rect) {
            return true;
        }

        public Point2D.Double getStartPoint() {
            return startPoint;
        }
        
        public Point2D.Double getEndPoint() {
            return endPoint;
        }

        public Point2D.Double[] getPoints() {
            return points;
        }

        public int getPriority() {
            return priority;
        }
    }

    private List<MapNode> map_nodes;
    private List<MapRoad> map_roads;

    private void initMap() {
        map_nodes = new ArrayList<MapNode>();
        map_roads = new ArrayList<MapRoad>();

        for (Node n : mainLoop.getNodes())
            map_nodes.add(new MapNode(n.getLocation()));

        for (Road r : mainLoop.getRoads())
            map_roads.add(new MapRoad(r.node1.getLocation(), r.node2.getLocation()));

        center = new Point2D.Double(0.0,0.0);
        ppm = 1.3;
    }
}
