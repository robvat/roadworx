/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Gerrit Drpst <gerritdrost@gmail.com>
 */
public abstract class Node
{
    private Point2D.Double location;
    private List<RoadSegment> roads;
    private Road priorityRoad = null;

    private List<Node> neighbourNodes;
    private List<Node> destinationNodes;
    private List<Node> sourceNodes;

    private HashMap<Node,RoadSegment> neighbourRoads;
    
    private int[][][][] intersectionArray;

    private List<Lane> incomingLanes;
    private HashMap<Lane,Lane> laneMap;

    private NodeListener listener;

    public double f,g,h; //pathfinding variables
    public Node parent;

    public int nodeType;

    public List<Car> spawnCars;

    public static int DEFAULT_NODE = 0;
    public static int TRAFFICLIGHT_NODE = 1;

    public Node(Point2D.Double location) {
        this.location = location;

        neighbourNodes = new ArrayList<Node>();
        neighbourRoads = new HashMap<Node,RoadSegment>();
        
        destinationNodes = new ArrayList<Node>();
        sourceNodes = new ArrayList<Node>();
        
        incomingLanes = new ArrayList<Lane>();
        roads = new ArrayList<RoadSegment>();

        spawnCars = new ArrayList<Car>();

        laneMap = new HashMap<Lane, Lane>();

        nodeType = 0;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public int getNodeType() {
        return nodeType;
    }

    public void init(NodeListener listener) {
        this.listener = listener;

        sortNodes();

        determineIncomingLanes();

        determineLaneMapping();
        
        determineDrivePermissions();
    }
    
    private void determineDrivePermissions() {
        
        int roadCount = neighbourNodes.size();
        
        /**
         * this array will contain all possible couples of start and destination indices.
         * Since this is hard to explain, I will just show how the array will look, this
         * should be self-explanatory:
         * couples[0] = {0,1}; (going from node at index 0 to the node at index 1)
         * couples[1] = {0,2}; (going from node at index 0 to the node at index 2)
         * couples[2] = {0,3};
         * couples[3] = {1,0};
         * couples[4] = {1,2};
         * couples[5] = {1,3};
         * couples[6] = {2,0};
         * etc. etc.
         */
        List<Integer[]> couples = new ArrayList<Integer[]>();
        
        for (Node source : getSourceNodes()) {
            
            int sourceIndex = neighbourNodes.indexOf(source);
            
            for (Node destionation : getDestinationNodes()) {
                
                int destinationIndex = neighbourNodes.indexOf(destionation);
                
                if (sourceIndex != destinationIndex) {
                    couples.add(new Integer[] {sourceIndex, destinationIndex});  
                }
                
            }
        }
        
        intersectionArray = new int[roadCount][roadCount][roadCount][roadCount];
        
        //we filled the array, lets start checking for possible        
        for (Integer[] couple1 : couples) {            
            for (Integer[] couple2 : couples) {
                if (!Arrays.equals(couple1, couple2)) {
                    intersectionArray[couple1[0]][couple1[1]][couple2[0]][couple2[1]] = getIntersectionType(couple1[0], couple1[1], couple2[0], couple2[1]);
                    System.out.println(
                            "Route 1: " + couple1[0] + " to " + couple1[1] + ", " + 
                            "Route 2: " + couple2[0] + " to " + couple2[1] + 
                            ". Intersection type: " + getIntersectionTypeString(getIntersectionType(couple1[0], couple1[1], couple2[0], couple2[1]))
                            );
                }
            }
        }
    }
    
    public final static int INTERSECTION_NONE = 0;
    public final static int INTERSECTION_SAME_DESTINATION = 1;
    public final static int INTERSECTION_SAME_SOURCE = 2;
    public final static int INTERSECTION_DESTINATION_IS_SOURCE = 3;
    public final static int INTERSECTION_ALWAYS = 4;    
    
    public String getIntersectionTypeString(int intersectionType) {
        switch (intersectionType) {
            case INTERSECTION_NONE:
                return "No Intersection";
            case INTERSECTION_ALWAYS:
                return "Definite intersection";
            case INTERSECTION_SAME_DESTINATION:
                return "Possible intersection: same destination node";
            case INTERSECTION_SAME_SOURCE:
                return "Possible intersection: same source node";
            case INTERSECTION_DESTINATION_IS_SOURCE:
                return "Possible intersection: a source node of the one route equals the destination node of another route";
            default:
                return "Unknown intersection type";
                
        }
    }
    
    /**
     * returns true if the route from sourceIndex1 to sourceIndex2 intersects
     * @param sourceIndex1
     * @param destinationIndex1
     * @param sourceIndex2
     * @param destinationIndex2
     * @return 
     */
    private int getIntersectionType(int sourceIndex1, int destinationIndex1, int sourceIndex2, int destinationIndex2) {
        
        //int roadCount = neighbourNodes.size();
        
        if (destinationIndex1 == destinationIndex2) {
            //they have the same destination, always intersect
            return INTERSECTION_SAME_DESTINATION;
        } else if (sourceIndex1 == sourceIndex2) {
            return INTERSECTION_SAME_SOURCE;
        } else if 
                (
                    rightDistance(sourceIndex1, destinationIndex1) == 1
                || //OR
                    rightDistance(sourceIndex2, destinationIndex2) == 1
                ) {
            //one of the cars is turning to the right: no intersection
            return INTERSECTION_NONE;
        } else if (sourceIndex1 == destinationIndex2) {
            if (rightDistance(sourceIndex1, destinationIndex1) > rightDistance(sourceIndex1, sourceIndex2))
                return INTERSECTION_ALWAYS;
            else
                return INTERSECTION_NONE;
        } else if (sourceIndex2 == destinationIndex1) {
            if (rightDistance(sourceIndex2, destinationIndex2) > rightDistance(sourceIndex2, sourceIndex1))
                return INTERSECTION_ALWAYS;
            else
                return INTERSECTION_NONE;
        } else {
            int a1 = Math.min(sourceIndex1, destinationIndex1);
            int a2 = Math.max(sourceIndex1, destinationIndex1);
            int b1 = Math.min(sourceIndex2, destinationIndex2);
            int b2 = Math.max(sourceIndex2, destinationIndex2);

            if
                    (
                        (between(a1, a2, b1) && !between(a1,a2,b2)) 
                    || //OR
                        (between(a1, a2, b2) && !between(a1,a2,b1))
                    )
                return INTERSECTION_ALWAYS;
            else
                return INTERSECTION_NONE;
        }
        
    }
    
    private int rightDistance(int a, int b) {
        if (a < b)
            a += neighbourNodes.size();
        
        return a - b;
    }
    
    private boolean between(int a1, int a2, int b) {
        return (b > a1 && b < a2);
    }

    private void determineLaneMapping() {

        if (getNeighbourNodes().size() == 2) {
            RoadSegment segment1 = getRoadSegment(getNeighbourNodes().get(0));
            RoadSegment segment2 = getRoadSegment(getNeighbourNodes().get(1));

            mapLanes(segment1,segment2);
        } else {
            RoadSegment segment1;
            RoadSegment segment2;

            for (Node n1 : getNeighbourNodes()) {

                segment1 = getRoadSegment(n1);

                for (Node n2 : getNeighbourNodes()) {

                    if (n1 == n2)
                        continue;

                    segment2 = getRoadSegment(n2);

                    if (segment1.getNextSegment() == segment2 || segment1.getPreviousSegment() == segment2) {
                        mapLanes(segment1,segment2);
                    }
                }

            }            
        }
        
        for (Node n : getSourceNodes()) {
            RoadSegment rs = getRoadSegment(n);
            for (Lane l : rs.getDestinationLanes(this)) {
                if (l.getAllowedDirections() == null) {
                    l.setAllowedDirections(getAllowedDirections(n));
                }
            }
        }
}

    private List<Node> getAllowedDirections(Node sourceNode) {
        List<Node> directionList = new ArrayList<Node>();

        for (Node n : getDestinationNodes()) {
            if (n != sourceNode) {
                directionList.add(n);
            }
        }

        return directionList;
    }

    public void addSpawnCar(Car car) {
        spawnCars.add(car);
    }

    private void mapLanes(RoadSegment rs1, RoadSegment rs2) {

        List<Lane> lanes1,lanes2;

        lanes1 = rs1.getDestinationLanes(this);
        lanes2 = rs2.getSourceLanes(this);

        for (Lane l1 : lanes1)
            for (Lane l2 : lanes2)
                if (l1.getLaneId() == l2.getLaneId())
                    laneMap.put(l1, l2);

        lanes1 = rs2.getDestinationLanes(this);
        lanes2 = rs1.getSourceLanes(this);
        for (Lane l1 : lanes1)
            for (Lane l2 : lanes2)
                if (l1.getLaneId() == l2.getLaneId())
                    laneMap.put(l1, l2);
    }

    private void determineIncomingLanes() {
        RoadSegment r;
        
        for (Node n : getNeighbourNodes()) {
            r = this.getRoadSegment(n);

            for (Lane l : r.getDestinationLanes(this)) {
                incomingLanes.add(l);
            }
        }
    }

    public void mapLane(Lane incoming, Lane outgoing) {
        laneMap.put(incoming, outgoing);
    }

    public Lane getLaneMapping(Lane incoming) {
        if (laneMap.containsKey(incoming))
            return laneMap.get(incoming);
        else
            return null;
    }

    public List<Lane> getIncomingLanes() {
        return incomingLanes;
    }

    public double distanceTo(Node destination) {
        return Math.sqrt(
                Math.pow(location.x - destination.location.x, 2) +
                Math.pow(location.y - destination.location.y, 2)
                );
    }

    public void addSource(Node n, RoadSegment r) {
        roads.add(r);
        sourceNodes.add(n);

        if (!neighbourNodes.contains(n))
            neighbourNodes.add(n);

        if (!neighbourRoads.containsKey(n))
            neighbourRoads.put(n,r);
    }
    
    public void addDestination(Node n, RoadSegment r) {
        roads.add(r);
        destinationNodes.add(n);

        if (!neighbourNodes.contains(n))
            neighbourNodes.add(n);
        
        if (!neighbourRoads.containsKey(n))
            neighbourRoads.put(n,r);
    }

    public RoadSegment getRoadSegment(Node destination) {
        return neighbourRoads.get(destination);
    }

    public Point2D.Double getLocation() {
        return location;
    }

    public List<Node> getNeighbourNodes() {
        return neighbourNodes;
    }

    public List<Node> getDestinationNodes() {
        return destinationNodes;
    }

    public List<Node> getSourceNodes() {
        return sourceNodes;
    }
    
    

    private void sortNodes() {
        double
                x = this.getLocation().x,
                y = this.getLocation().y,
                max_angle,
                angle;
        
        Collections.sort(sourceNodes,nodeComparator);
        Collections.sort(destinationNodes,nodeComparator);
        Collections.sort(neighbourNodes,nodeComparator);
    }
    
    private Comparator<Node> nodeComparator = new Comparator<Node>() {
        public int compare(Node n1, Node n2) {
            double angle1 = Math.atan2(
                            (getLocation().getX() - n1.getLocation().y),
                            (getLocation().getY() - n1.getLocation().x)
                        );
            
            double angle2 = Math.atan2(
                            (getLocation().getX() - n2.getLocation().y),
                            (getLocation().getY() - n2.getLocation().x)
                        );
            
            if (angle1 > angle2) 
                return 1;
            else if (angle1 < angle2) 
                return -1;
            else
                return 0;
                
            
        }
    };


    /* incoming cars need to know wether to brake or continue driving */
    abstract boolean drivethrough(Car incoming);
    /* TODO: crossroads without lights need drivers to check so drivers
     need a function for this that will have to be called by that node */

    
    /* once a car is at the border it has to be accepted
     by the new node and leave the old road */
    abstract void acceptCar(Car incoming);

    /* Cars can be on a node for a longer time so nodes need to be
     updated aswell (if a car has to be 40 sec on a node then the node needs to
     know the time */
    public void update(double timestep) {
        int i = 0;
        Car car;
        for (i = 0; i > -1 && i < spawnCars.size(); i++) {
            car = spawnCars.get(i);
            Node n = car.getNextNode();
            
            RoadSegment rs = getRoadSegment(n);

            List<Lane> lanes = rs.getSourceLanes(this);

            for (Lane l : lanes) {
                if (!car.doesOvertake() && l.getRightNeighbour() != null)
                    continue;

                if (l.acceptsCarAdd(car) && !hasToBrake(car,l)) {
                    l.addCar(car);
                    spawnCars.remove(car);

                    if (listener != null)
                        listener.carAdded(car);

                    i--;
                }
            }
        }
    }

    /**
     * Returns the priority road set first on this node
     */
    public Road getPriorityRoad()
    {
        return priorityRoad;
    }

    /**
     * Sets a priorityRoad on this node (used in PriorityJunction
     * @param r The road which is a priority road
     * @return Wether it could be added (or if the road is not part of the whole
     *  or if there already is a priority road)
     */
    public boolean setPriorityRoad(Road r)
    {
        if(priorityRoad != null)
        {
            System.err.print("Already a priority road there!");
            return false;
        }
        /* Check all roads and see if 1 is part of a priorityroad (and
         * Hope another is too!)
         */
        for (RoadSegment n : roads)
        {
            if(r.equals(n.getRoad()))
            {
                priorityRoad = r;
                return true;
            }
        }
        System.err.print("Road isn't part of node");
        priorityRoad = r; //Same function on request of Gerrit
        return false;
    }

    private boolean hasToBrake(Car car, Lane lane) {
        if (!lane.hasCars())
            return false;
         else
            return ( lane.getLastCar().getBack() < car.getDriverType().getMinimumDistanceToLeader());
        
    }

    @Override
    public String toString() {
        return location.toString();
    }
}
