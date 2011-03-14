/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package osmparser;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 *
 * @author Gerrit
 */
public class OSMParser {
    private Document _doc;

    private RoadNetwork _rn;
    private List<Road> _roads;
    private HashMap<Integer,Node> _nodemap;
    private Rectangle2D.Double _bounds;

    public OSMParser(String path) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            _doc = builder.parse(new File(path));

            _nodemap = new HashMap<Integer,Node>();
            _roads = new ArrayList<Road>();

            getNodes();

            getRoads();

            _rn = new RoadNetwork(_nodemap,_roads, _bounds);
        } catch (Exception ex) {

        }
    }

    public RoadNetwork getRoadNetwork() {
        return _rn;
    }

    private void getRoads() {
        int i,j;

        try {
            NodeList nodes, ways = _doc.getElementsByTagName("way");

            for (i = 0; i < ways.getLength(); i++) {
                Element way = (Element) ways.item(i);

                NodeList tags = way.getElementsByTagName("tag");

                String name = "";

                for (j = 0; j < tags.getLength(); j++) {
                    Element node = (Element) tags.item(j);
                    if (node.getAttribute("k").equals("name")) {
                        name = node.getAttribute("v");
                        break;
                    }
                }

                if (name.equals(""))
                    continue;

                //construct a new road object
                Road r = new Road(name);

                //get all node elements
                nodes = way.getElementsByTagName("nd");

                //iterate through the nodes and add them to the road
                for (j = 0; j < nodes.getLength(); j++) {
                    Element node = (Element) nodes.item(j);

                    if (node.hasAttribute("ref")) {
                        int id = Integer.parseInt(node.getAttribute("ref"));
                        if (_nodemap.containsKey(id))
                            r.addNode(_nodemap.get(id));
                    }
                }

                //hook up the nodes
                r.getFirstNode().addDestination(r.getLastNode(),r);
                r.getLastNode().addDestination(r.getFirstNode(), r);

                _roads.add(r);
            }

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void getNodes() {
        try {
            NodeList nodes = _doc.getElementsByTagName("node");

            Node node,prev = null;

            double lng_min = Double.MAX_VALUE;
            double lng_max = Double.MIN_VALUE;
            double lat_min = Double.MAX_VALUE;
            double lat_max = Double.MIN_VALUE;

            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                
                if (element.hasAttribute("lon")) {
                    double lng = Double.parseDouble(element.getAttribute("lon"));
                    double lat = Double.parseDouble(element.getAttribute("lat"));
                    int id = Integer.parseInt(element.getAttribute("id"));

                    node = new Node(id,new GeoCoordinate(lng,lat));

                    lng_min = Math.min(lng_min, lng);
                    lng_max = Math.max(lng_max, lng);
                    
                    lat_min = Math.min(lat_min, lat);
                    lat_max = Math.max(lat_max, lat);

                    _nodemap.put(id,node);
                    
                    prev = node;
                }
            }

            _bounds = new Rectangle2D.Double(lng_min,lat_min,(lng_max - lng_min),(lat_max - lat_min));

        } catch (Exception ex) {

        }
    }
}
