/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package osmparser;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Gerrit
 */
public class Node {
    private GeoCoordinate _coord;
    private int _id;

    private List<Node> _destinations;
    //private List<Node> _sources;
    private HashMap<Node,Road> _road_map;

    public Node(int id, GeoCoordinate coord) {
        initVars(id,coord);
    }

    private void initVars(int id, GeoCoordinate coord) {
        _id = id;
        _coord = coord;

        _destinations = new ArrayList<Node>();
        //_sources = new ArrayList<Node>();
        _road_map = new HashMap<Node,Road>();
    }

    public GeoCoordinate getCoordinate() {
        return _coord;
    }

    public int getId() {
          return _id;
    }

    public void addDestination(Node n, Road r) {
        _destinations.add(n);
        _road_map.put(n, r);
    }

    public List<Node> getDestinations() {
        return _destinations;
    }

    public Road getRoadTo(Node destination) {
        if (_road_map.containsKey(destination))
            return _road_map.get(destination);

        return null;
    }

    @Override
    public String toString() {
        return "id: " + Integer.toString(_id) + "\n" + _coord.toString();
    }
}
