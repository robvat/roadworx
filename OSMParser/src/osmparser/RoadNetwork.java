/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package osmparser;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Gerrit
 */
public class RoadNetwork {
    private HashMap<Integer,Node> _nodemap;
    private List<Road> _roads;
    private Rectangle2D.Double _bounds;

    public RoadNetwork(HashMap<Integer, Node> nodemap, List<Road> roads, Rectangle2D.Double bounds) {
        _nodemap = nodemap;
        _roads = roads;
        _bounds = bounds;
    }

    public List<Road> getRoads() {
        return _roads;
    }

    public HashMap<Integer, Node> getNodeMap() {
        return _nodemap;
    }

    public Rectangle2D.Double getBounds() {
        return _bounds;
    }

    @Override
    public String toString() {
        String returnstring = Integer.toString(_roads.size()) + " roads, " + Integer.toString(_nodemap.size()) + " nodes.";
        for (Road r : _roads)
            returnstring += "\n" + r.toString();

        return returnstring;
    }
}
