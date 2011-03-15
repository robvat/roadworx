/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package osmparser.ui;

import com.jhlabs.map.Ellipsoid;
import com.jhlabs.map.proj.MercatorProjection;
import com.jhlabs.map.proj.Projection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import osmparser.Node;
import osmparser.Road;
import osmparser.RoadNetwork;

/**
 *
 * @author Gerrit
 */
public class MapComponent extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener, HierarchyBoundsListener {

    //edit this to change the look of the grid
    private final Stroke roadFillStroke = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
    private final Stroke roadOutlineStroke = new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);

    private final Stroke mapOutlineStroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final Paint backgroundPaint = new Color(255, 255, 255);

    private final Paint roadOutlinePaint = new Color(255, 0, 0);
    private final Paint mapOutlinePaint = new Color(0, 0, 0);
    private final Paint roadFillPaint = new Color(255, 255, 0);
    private RoadNetwork _rn;

    private int _componentwidth, _componentheight;

    private double _mapwidth, _mapheight;

    private double _scale;
    private Point2D.Double _coordinateoffset;
    private Point2D.Double _offset;
    Projection p;

    public MapComponent() {
        p = new MercatorProjection();
        Ellipsoid unarySphere = new Ellipsoid(null, 1, 0, null);
        p.setEllipsoid(unarySphere);
        p.initialize();

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addHierarchyBoundsListener(this);
    }

    public void loadRoadNetwork(RoadNetwork rn) {
        _rn = rn;

        _scale = 1.0;
    }

    @Override
    public void paintComponent(Graphics g) {
        //first call the super method.
        super.paintComponent(g);

        Graphics2D gr = (Graphics2D) g;

        //set antialiasing to true so it will look better
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        _componentwidth = getWidth();
        _componentheight = getHeight();

        calculateInitialScale();

        if (_rn == null || _scale == 0.0) {
            return;
        }

        gr.setPaint(backgroundPaint);
        gr.fillRect(
                (int) _offset.x,
                (int) _offset.y,
                (int) _mapwidth,
                (int) _mapheight);

        for (Road r : _rn.getRoads()) {
            drawRoad(gr, r);
        }

        gr.setPaint(mapOutlinePaint);
        gr.setStroke(mapOutlineStroke);
        gr.drawRect(
                (int) _offset.x,
                (int) _offset.y,
                (int) _mapwidth,
                (int) _mapheight);

    }

    private void drawRoad(Graphics2D gr, Road r) {
        Node current;

        int[] xPoints = new int[r.getNodes().size()];
        int[] yPoints = new int[r.getNodes().size()];

        for (int i = 0; i < r.getNodes().size(); i++) {
            current = r.getNodes().get(i);

            Point2D.Double current_p = new Point2D.Double();

            p.transform(current.getCoordinate().getLongitude(), current.getCoordinate().getLatitude(), current_p);

            xPoints[i] = (int) (_coordinateoffset.x + (current_p.x * _scale));
            yPoints[i] = (int) (_coordinateoffset.y + (current_p.y * _scale));
        }


        gr.setPaint(roadOutlinePaint);
        gr.setStroke(roadOutlineStroke);
        gr.drawPolyline(xPoints, yPoints, r.getNodes().size());

        gr.setPaint(roadFillPaint);
        gr.setStroke(roadFillStroke);
        gr.drawPolyline(xPoints, yPoints, r.getNodes().size());
    }

    private void calculateInitialScale() {
        double componentwidth = (double) getWidth();
        double componentheight = (double) getHeight();

        p.setProjectionLatitude(_rn.getBounds().getCenterY());
        p.setProjectionLatitudeDegrees(_rn.getBounds().getHeight());

        p.setProjectionLongitude(_rn.getBounds().getCenterX());
        p.setProjectionLongitudeDegrees(_rn.getBounds().getWidth());

        Point2D.Double topleft = new Point2D.Double();
        Point2D.Double bottomright = new Point2D.Double();

        p.transform(_rn.getBounds().getMinX(), _rn.getBounds().getMinY(), topleft);

        p.transform(_rn.getBounds().getMaxX(), _rn.getBounds().getMaxY(), bottomright);

        double map_ratio = (bottomright.x - topleft.x) / (bottomright.y - topleft.y);

        double component_ratio = componentwidth / componentheight;

        if (component_ratio > map_ratio) {
            _scale = componentheight / (bottomright.y - topleft.y);
        } else {
            _scale = componentwidth / (bottomright.x - topleft.x);
        }

        _mapwidth = (bottomright.x - topleft.x) * _scale;
        _mapheight = (bottomright.y - topleft.y) * _scale;

        _offset = new Point2D.Double(
                (componentwidth / 2) - (_mapwidth / 2),
                (componentheight / 2) - (_mapheight / 2));

        _coordinateoffset = new Point2D.Double(
                _offset.x - (topleft.x * _scale),
                _offset.y - (topleft.y * _scale));

    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        double rotation = (double)e.getWheelRotation() * (double)e.getScrollAmount();
    }

    public void ancestorMoved(HierarchyEvent e) {
    }

    public void ancestorResized(HierarchyEvent e) {

    }
}
