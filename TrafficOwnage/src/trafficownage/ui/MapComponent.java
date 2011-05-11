/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import sun.font.Font2D;
import trafficownage.simulation.Car;
import trafficownage.simulation.Lane;
import trafficownage.simulation.MainLoop;
import trafficownage.simulation.Node;
import trafficownage.simulation.Road;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class MapComponent extends JComponent implements MouseWheelListener, MouseMotionListener, MouseListener, ComponentListener {

    MainLoop mainLoop;

    BufferedImage back_layer;
    BufferedImage car_layer;

    private double ppm;
    private Point2D.Double center;
    private double width;
    private double height;
    private Rectangle2D.Double frame_bounds;

    private boolean map_invalid = false;

    public void init(MainLoop mainLoop) {
        this.mainLoop = mainLoop;

        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);

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


        if (back_layer == null || map_invalid)
            drawMap();

        gr = (Graphics2D)g;

        gr.drawImage(back_layer,0,0,null);

        for (Road r : map_roads)
            drawRoadCars(gr,r);


        if (selected_car != null)
            drawCarInfo(gr,selected_car);

    }

    private final static Color ROADCOLORS[] = {new Color(255,0,0), new Color(255,48,48), new Color(255,96,96)};
    private final static Color NODECOLORS[] = {new Color(24,64,255), new Color(32,72,255), new Color(48,96,255)};
    private final static double[] NODERADIUSES = {10.0,15.0,25.0};
    private final static double[] ROADWIDTHS = {4.0,6.0,10.0};

    private final static Font INFO_FONT = new Font(Font.SANS_SERIF,Font.BOLD,12);

    private void drawMap() {
        map_invalid = false;

        back_layer = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = (Graphics2D)back_layer.createGraphics();

        double halfwidth = width / 2;
        double halfheight = height / 2;

        double mpp = 1.0/ppm;

        frame_bounds = new Rectangle2D.Double(center.x - (mpp * halfwidth), center.y - (mpp * halfheight), mpp * width, mpp * height);

        for (Road r : map_roads) {
            drawRoad(gr,r);
        }

        for (Node n : map_nodes) {
            drawNode(gr,n);
        }
    }


    private Car selected_car;
    private void setSelectedCar(Car car) {
        selected_car = car;
    }

    private Point2D.Double start_point, end_point, point;
    
    private void drawRoad(Graphics2D gr, Road r) {
        start_point = r.getStartNode().getLocation();
        end_point = r.getEndNode().getLocation();

        gr.setColor(ROADCOLORS[1]);//r.getPriority()]);
        gr.setStroke(new BasicStroke((int)(ppm * ROADWIDTHS[1])));//r.getPriority()])));

        gr.drawLine(
                (int)(ppm * (frame_bounds.getMaxX() - start_point.x)),
                (int)(ppm * (frame_bounds.getMaxY() - start_point.y)),
                (int)(ppm * (frame_bounds.getMaxX() - end_point.x)),
                (int)(ppm * (frame_bounds.getMaxY() - end_point.y))
                );
    }



    private void drawCarInfo(Graphics2D gr, Car c) {
        gr.setColor(new Color(0,0,0,192));// = new Rectangle2D.Double();

        DecimalFormat twoDForm = new DecimalFormat("#.##");
		 ;

        String acc = "a: " + twoDForm.format(c.getAcceleration()) + "m/s^2";
        String vel = "v: " + twoDForm.format(c.getVelocity()) + "m/s";
        String pos = "p: " + twoDForm.format(c.getPosition()) + "m";

           // get metrics from the graphics
        FontMetrics metrics = gr.getFontMetrics(INFO_FONT);
        // get the height of a line of text in this font and render context
        int line_height = metrics.getHeight();
        // get the advance of my text in this font and render context
        int line_width = Math.max(metrics.stringWidth(acc),Math.max(metrics.stringWidth(vel),metrics.stringWidth(pos)));


        gr.fillRect(8,8,line_width + 8,(line_height + 2) * 3 + 8);

        gr.setColor(Color.white);
        gr.drawString(acc,12,12 + line_height + 2);
        gr.drawString(vel,12,12 + (line_height * 2) + 2);
        gr.drawString(pos,12,12 + (line_height * 3) + 2);


    }

    private void drawRoadCars(Graphics2D gr, Road r) {

        start_point = r.getStartNode().getLocation();
        end_point = r.getEndNode().getLocation();

        gr.setStroke(new BasicStroke((int)(ppm * ROADWIDTHS[1])));//r.getPriority()])));

        double x1 = ppm * (frame_bounds.getMaxX() - start_point.x);
        double y1 = ppm * (frame_bounds.getMaxY() - start_point.y);
        double x2 = ppm * (frame_bounds.getMaxX() - end_point.x);
        double y2 = ppm * (frame_bounds.getMaxY() - end_point.y);
        
        double dx = x2 - x1;
        double dy = y2 - y1;


        double length = r.getLength();
        double pos;

        int carside = (int)(ppm * ROADWIDTHS[1]);
        int carhalf = carside / 2;

        for (Lane l : r.getAllLanes()) {

            if (l.getCars().isEmpty())
                continue;
            
            selected_car = l.getCars().get(0);

            for (Car c : l.getCars()) {
                pos = c.getPosition() / length;

                if (c == selected_car)
                    gr.setColor(Color.green);//r.getPriority()]);
                else
                    gr.setColor(Color.black);//r.getPriority()]);
                gr.fillOval((int)(x1 + (pos*dx))-carhalf, (int)(y1 + (pos*dy))-carhalf,carside,carside);
            }
        }
    }

    private void drawNode(Graphics2D gr, Node n) {
        point = n.getLocation();

        int x = (int)(ppm * (frame_bounds.getMaxX() - point.x));
        int y = (int)(ppm * (frame_bounds.getMaxY() - point.y));

        int r = (int)(ppm * NODERADIUSES[1]);//n.getPriority()]);
        
        int d = r*2;

        gr.setColor(NODECOLORS[1]);//n.getPriority()]);
        gr.fillOval(x-r, y-r, d, d);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        
        if (e.getWheelRotation() < 0) {
            //zoom in
            ppm *= 1.25;
        } else {
            ppm *= .8;
        }

        map_invalid = true;

        repaint();
    }

    Point offset;

    public void mouseDragged(MouseEvent e) {

        double mpp = 1.0 / ppm;

        center.x -= mpp * ((double)offset.x - e.getX());
        center.y -= mpp * ((double)offset.y - e.getY());

        map_invalid = true;
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


    private List<Node> map_nodes;
    private List<Road> map_roads;

    private void initMap() {

        map_nodes = mainLoop.getNodes();
        map_roads = mainLoop.getRoads();

        center = new Point2D.Double(0.0,0.0);
        ppm = 1.3;
    }

    public void componentResized(ComponentEvent e) {
        map_invalid = true;
    }

    public void componentMoved(ComponentEvent e) { }

    public void componentShown(ComponentEvent e) { }

    public void componentHidden(ComponentEvent e) { }
}
