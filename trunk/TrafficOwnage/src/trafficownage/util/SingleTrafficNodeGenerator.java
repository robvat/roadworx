/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.util;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import trafficownage.simulation.*;

/**
 *
 * @author frans
 */
public class SingleTrafficNodeGenerator {
    
    private Node[] nodeArray;
    private RoadSegment[] rsArray;
    private Road[] roadArray;
    private HashMap<Integer, List<Node>> areas;

    /**
     * Creates a network to benchmark 1 node
     * @param laneSize length of the roadsegments attached
     * @param numLanes number of lanes on each side going in (and out again)
     */
    public SingleTrafficNodeGenerator(double laneSize, int numLanes)
    {
            nodeArray = new Node[] {
            new Roundabout(new Point2D.Double(0.0,0.0), 5),
            new SpawnNode(new Point2D.Double(0.0,laneSize), 2),
            new SpawnNode(new Point2D.Double(0.0, -laneSize), 2),
            new SpawnNode(new Point2D.Double(laneSize, 0.0), 2),
            new SpawnNode(new Point2D.Double(-laneSize, 0.0), 2)
        };

        roadArray = new Road[] {
            new Road("Vertical road"),
            new Road("Horizontal road")
        };


        rsArray = new RoadSegment[] {
            new RoadSegment(roadArray[0],50 / 3.6,nodeArray[1],nodeArray[0]),
            new RoadSegment(roadArray[0],50 / 3.6,nodeArray[0],nodeArray[2]),
            new RoadSegment(roadArray[0],50 / 3.6,nodeArray[3],nodeArray[0]),
            new RoadSegment(roadArray[0],50 / 3.6,nodeArray[0],nodeArray[4])
        };

        int lanesPerSide = numLanes;

        for (RoadSegment rs : rsArray) {
            for (int i = 0; i < lanesPerSide; i++) {
                rs.addLeftStartLane(i, false);
                rs.addLeftEndLane(lanesPerSide + i, false);
            }
        }

        roadArray[0].addLast(rsArray[0]);
        roadArray[0].addLast(rsArray[1]);

        roadArray[1].addLast(rsArray[2]);
        roadArray[1].addLast(rsArray[3]);

        areas = new HashMap<Integer,List<Node>>();

        areas.put(0,Arrays.asList(new Node[] {nodeArray[1],nodeArray[2],nodeArray[3],nodeArray[4]}));
        areas.put(1,Arrays.asList(new Node[] {nodeArray[0]}));

    }

    /**
     * @return the nodeArray
     */
    public Node[] getNodeArray()
    {
        return nodeArray;
    }

    /**
     * @return the roadArray
     */
    public Road[] getRoadArray()
    {
        return roadArray;
    }

    /**
     * @return the areas
     */
    public HashMap<Integer, List<Node>> getAreas()
    {
        return areas;
    }
    
}
