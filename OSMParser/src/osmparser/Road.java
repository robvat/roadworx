/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package osmparser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gerrit
 */
public class Road {
    private String _name;
    private double _length;
    private List<Node> _nodes;

    private Node _first_node,
            _last_node;

    private Node _prev;

    public Road (String name) {
        _name = name;
        _nodes = new ArrayList<Node>();
        _length = 0.0;
        _prev = null;
    }

    public String getName() {
        return _name;
    }
    
    public double getLength() {
        return _length;
    }

    public List<Node> getNodes() {
        return _nodes;
    }

    public Node getFirstNode() {
        return _first_node;
    }

    public Node getLastNode() {
        return _last_node;
    }

    public void addNode(Node n) {
        if (!_nodes.isEmpty()) {
            _length += distance(_last_node.getCoordinate(),n.getCoordinate());
        } else {
            _first_node = n;
        }

        _nodes.add(n);
        
        _last_node = n;
    }

    @Override
    public String toString() {
        return "Origin: " + _first_node.toString() + "\n Destination: " + _last_node.toString() + "\n Length: " + Double.toString(_length) + "m";
    }

    private double distance(GeoCoordinate c1, GeoCoordinate c2) {
        double theta = c1.getLongitude() - c2.getLongitude();
        double dist = Math.sin(deg2rad(c1.getLatitude())) * Math.sin(deg2rad(c2.getLatitude())) + Math.cos(deg2rad(c1.getLatitude())) * Math.cos(deg2rad(c2.getLatitude())) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


}
