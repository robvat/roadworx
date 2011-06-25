/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.ui;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import trafficownage.simulation.Car;
import trafficownage.simulation.Lane;
import trafficownage.simulation.MainLoop;
import trafficownage.simulation.Node;
import trafficownage.simulation.Road;
import trafficownage.simulation.RoadSegment;
import trafficownage.simulation.StupidTrafficLight;
import trafficownage.simulation.TrafficLightInterface;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class MapComponent extends JComponent implements MouseWheelListener, MouseMotionListener, MouseListener, ComponentListener {

    //SETTINGS, WILL BE ADDED TO GUI!
    private static final boolean FOLLOW_CAR = true;

    MainLoop mainLoop;

    BufferedImage back_layer;
    BufferedImage car_layer;

    private double ppm;
    private Point2D.Double center;
    private double width;
    private double height;
    private Rectangle2D.Double frame_bounds;

    private boolean map_invalid = false;

    private final static Color BACKGROUND_COLOR = new Color(64,64,64);

    private final static Color ROAD_COLOR = new Color(255,255,64);

    private final static Color RED_LIGHT_COLOR = new Color(255,64,64);
    private final static Color GREEN_LIGHT_COLOR = new Color(0,192,0);

    private final static Color NODE_COLOR = new Color(255,64,64);

    private final static Color CAR_QUEUE_COLOR = new Color(32,72,192);
    private final static Color CAR_DEFAULT_COLOR = new Color(0,0,0);

    private final static double NODE_STROKE_WIDTH = 6.0;

    private final static double CAR_WIDTH = 3.0;
    private final static double LANE_SPACE = 4.0;

    private final static Color INFO_COLOR = new Color(255,255,255);
    private final static Font INFO_FONT = new Font(Font.SANS_SERIF,Font.BOLD,16);

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

        synchronized(mainLoop.getSyncObject()) {
            gr.drawImage(back_layer,0,0,null);

            car_count = 0;

            gr.setStroke(new BasicStroke((int)(ppm * CAR_WIDTH),BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));

            for (Road r : map_roads)
                for (RoadSegment rs : r.getSegments())
                    drawRoadSegmentCars(gr,rs);


            drawInfo(gr);
        }

    }


    private void drawLaneLight(Graphics2D gr, Lane l) {
        Line2D.Double line = lane_coords.get(l);
        gr.drawOval(
                (int)(line.x2 - (CAR_WIDTH / 2)),
                (int)(line.y2 - (CAR_WIDTH / 2)),
                (int)CAR_WIDTH,
                (int)CAR_WIDTH);
    }

    private int car_count;

    private void drawMap() {
        map_invalid = false;

        back_layer = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = (Graphics2D)back_layer.createGraphics();

        gr.setColor(BACKGROUND_COLOR);
        gr.fillRect(0,0,(int)width,(int)height);

        double halfwidth = width / 2;
        double halfheight = height / 2;

        double mpp = 1.0/ppm;

        frame_bounds = new Rectangle2D.Double(center.x - (mpp * halfwidth), center.y - (mpp * halfheight), mpp * width, mpp * height);

        gr.setStroke(new BasicStroke((int)(ppm * CAR_WIDTH),BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
        gr.setColor(ROAD_COLOR);
        
        for (Road r : map_roads) 
            for (RoadSegment rs : r.getSegments())
                drawRoadSegment(gr,rs);
        

        for (Node n : map_nodes)
            drawNode(gr,n);
        
    }


    private Car selected_car;
    public void setSelectedCar(Car car) {
        selected_car = car;
    }

    private Point2D.Double start_point, end_point, point;

    private HashMap<Lane,Line2D.Double> lane_coords;

    private double xMetricToPixel(double xMetric) {
        return ppm * (frame_bounds.getMaxX() - xMetric);
    }
    private double yMetricToPixel(double yMetric) {
        return ppm * (frame_bounds.getMaxY() - yMetric);
    }
    private double xPixelToMetric(double xPixel) {
        return frame_bounds.getMaxX() - (xPixel / ppm);
    }
    private double yPixelToMetric(double yPixel) {
        return frame_bounds.getMaxY() - (yPixel / ppm);
    }

    private void drawRoadSegment(Graphics2D gr, RoadSegment r) {
        start_point = r.getStartNode().getLocation();
        end_point = r.getEndNode().getLocation();

        double x1_start = xMetricToPixel(start_point.x);//ppm * (frame_bounds.getMaxX() - start_point.x);
        double x2_start = xMetricToPixel(end_point.x);//ppm * (frame_bounds.getMaxX() - end_point.x);
        double y1_start = yMetricToPixel(start_point.y);//ppm * (frame_bounds.getMaxY() - start_point.y);
        double y2_start = yMetricToPixel(end_point.y);//ppm * (frame_bounds.getMaxY() - end_point.y);

        double dx = x2_start - x1_start;
        double dy = y2_start - y1_start;

        double length = Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy,2.0));

        double line_dx = (LANE_SPACE * ppm) * (dy / length);
        double line_dy = (LANE_SPACE * ppm) * (dx / length);

        /*x1_start -= ((r.getLanesPerSide() - .5) * line_dx);
        x2_start -= ((r.getLanesPerSide() - .5) * line_dx);

        y1_start += ((r.getLanesPerSide() - .5) * line_dy);
        y2_start += ((r.getLanesPerSide() - .5) * line_dy);*/

        Line2D.Double line1;
        Line2D.Double line2;

        List<Lane> laneList;

        laneList = r.getStartLanes();

        int i = 0;

        for (Lane l : laneList) {
            
            line1 = lane_coords.get(l);
            line1.x1 = x1_start - (((double)(laneList.size() - i) - .5) * line_dx);
            line1.x2 = x2_start - (((double)(laneList.size() - i) - .5) * line_dx);
            line1.y1 = y1_start + (((double)(laneList.size() - i) - .5) * line_dy);
            line1.y2 = y2_start + (((double)(laneList.size() - i) - .5) * line_dy);
            gr.draw(line1);

            i++;
        }

        laneList = r.getEndLanes();

        int j = 0;

        for (Lane l : laneList) {

            line2 = lane_coords.get(l);
            line2.x1 = x2_start + (((double)(laneList.size() - j) - .5) * line_dx);
            line2.x2 = x1_start + (((double)(laneList.size() - j) - .5) * line_dx);
            line2.y1 = y2_start - (((double)(laneList.size() - j) - .5) * line_dy);
            line2.y2 = y1_start - (((double)(laneList.size() - j) - .5) * line_dy);
            gr.draw(line2);

            j++;

            /*x1 += line_dx;
            x2 += line_dx;

            y1 -= line_dy;
            y2 -= line_dy;*/
        }

    }


    public void update() {
        if (selected_car != null && FOLLOW_CAR) {
            Lane l = selected_car.getLane();

            Line2D.Double laneLine = lane_coords.get(l);

            if (laneLine != null) {

                double x1 = xPixelToMetric(laneLine.x1);
                double x2 = xPixelToMetric(laneLine.x2);
                double y1 = yPixelToMetric(laneLine.y1);
                double y2 = yPixelToMetric(laneLine.y2);

                double ratio = ((selected_car.getPosition() + selected_car.getBack()) / 2.0) / l.getLength();

                center.x = x1 + (ratio * (x2 - x1));
                center.y = y1 + (ratio * (y2 - y1));

                map_invalid = true;
            }
        }

        repaint();
    }


    private void drawInfo(Graphics2D gr) {

        DecimalFormat twoDForm = new DecimalFormat("#.#");		 

        int i = 0;

        String cars = "Rendered cars: " + Integer.toString(car_count);

        if (selected_car != null) {
            String acc = "a: " + twoDForm.format(selected_car.getAcceleration()) + " m/s^2";
            String vel_kph = "v(km/h): " + twoDForm.format(selected_car.getVelocity() * 3.6) + " km/h";
            String vel_ms = "v(m/s): " + twoDForm.format(selected_car.getVelocity()) + " m/s";
            String pos = "p: " + twoDForm.format(selected_car.getPosition()) + "m";
            drawText(gr,acc,vel_kph,vel_ms,pos,cars);
        } else {
            drawText(gr,cars);
        } 
    }

    private void drawText(Graphics2D gr, String... lines) {
        // get metrics from the graphics
        FontMetrics metrics = gr.getFontMetrics(INFO_FONT);
        // get the height of a line of text in this font and render context
        int line_height = metrics.getHeight();

        gr.setFont(INFO_FONT);
        gr.setColor(INFO_COLOR);

        int i = 0;

        for (String line : lines) {
            gr.drawString(line,12,12 + (line_height * i) + 2);
            i++;
        }

    }

    private int drawLaneContent(Graphics2D gr, double length, List<Lane> lanes, int i) {

        double dx,dy;

        double car_x1,car_y1,car_x2,car_y2;

        double light_x1,light_y1,light_x2,light_y2;

        double lightLength = Math.min(length,15.0) / length;

        double car_start,car_end;

        Line2D.Double line;

        for (Lane l : lanes) {

            line = lane_coords.get(l);

            i++;

            dx = line.x2 - line.x1;
            dy = line.y2 - line.y1;


            if (l.getEndNode().getNodeType() == Node.TRAFFICLIGHT_NODE && ((TrafficLightInterface)l.getEndNode()).getGreenLanes().contains(l)) {
                gr.setColor(GREEN_LIGHT_COLOR);

                light_x1 = line.x2 - (lightLength * dx);
                light_x2 = line.x2;
                light_y1 = line.y2 - (lightLength * dy);
                light_y2 = line.y2;

                gr.drawLine((int)light_x1,(int)light_y1,(int)light_x2,(int)light_y2);
            }

            if (!l.hasCars())
                continue;

            Car c = l.getFirstCar();
            while (c != null) {
                car_start = c.getPosition() / length;
                car_end = c.getBack() / length;

                if (c.isInQueue())
                    gr.setColor(CAR_QUEUE_COLOR);
                else
                    gr.setColor(CAR_DEFAULT_COLOR);

                car_x1 = line.x1 + (car_start*dx);
                car_y1 = line.y1 + (car_start*dy);
                car_x2 = line.x1 + (car_end*dx);
                car_y2 = line.y1 + (car_end*dy);

                gr.drawLine((int)car_x1,(int)car_y1,(int)car_x2,(int)car_y2);

                car_count++;

                c = c.getCarBehind();
            }

            i++;
        }

        return i;
    }

    private void drawRoadSegmentCars(Graphics2D gr, RoadSegment r) {
        
        double length = r.getLength();

        int i = 0;

        i = drawLaneContent(gr, length, r.getStartLanes(), i);
        i = drawLaneContent(gr, length, r.getEndLanes(), i);

        
    }

    private void drawNode(Graphics2D gr, Node n) {
        point = n.getLocation();

        int x = (int)(ppm * (frame_bounds.getMaxX() - point.x));
        int y = (int)(ppm * (frame_bounds.getMaxY() - point.y));

        int r = (int)(ppm * ((double)n.getIncomingLanes().size() * 2));//TODO: Fix this to have proper sizes
        
        int d = r*2;

        gr.setColor(NODE_COLOR);//n.getPriority()]);
        gr.setStroke(new BasicStroke((int)(ppm * NODE_STROKE_WIDTH)));
        gr.drawOval(x-r, y-r, d, d);
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

        lane_coords = new HashMap<Lane,Line2D.Double>();

        for (Road r : map_roads) {

            for (RoadSegment rs : r.getSegments()) {

                for (Lane l : rs.getStartLanes())
                    lane_coords.put(l,new Line2D.Double(0,0,0,0));

                for (Lane l : rs.getEndLanes())
                    lane_coords.put(l,new Line2D.Double(0,0,0,0));
                
            }
        }

        center = new Point2D.Double(0.0,0.0);
        ppm = 0.5;
    }

    public void componentResized(ComponentEvent e) {
        map_invalid = true;
    }

    public void componentMoved(ComponentEvent e) { }

    public void componentShown(ComponentEvent e) { }

    public void componentHidden(ComponentEvent e) { }
}
