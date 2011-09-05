/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayDeque;
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
    /**
     * These static ints represent the type of node placed in the middle of the system
     */
    public static final int NODE_NORMAL_JUNCTION = 0;
    public static final int NODE_DYNAMIC_TRAFFICLIGHT = 1;
    public static final int NODE_RANDOM_TRAFFICLIGHT = 2;
    public static final int NODE_ROUNDABOUT = 3;
    public static final int NODE_FIXED_TRAFFICLIGHT = 4;
 
    
    private Point2D.Double location;
    private List<RoadSegment> roads;
    private Road priorityRoad = null;

    private List<Node> neighbourNodes;
    private List<Node> destinationNodes;
    private List<Node> sourceNodes;
    
    private LinkedList<List<Lane>> simultaneousLaneSets;

    private HashMap<Node,RoadSegment> neighbourRoads;
    
    private int nodeSize;
    private static int[][][][][] INTERSECTION_ARRAY;

    private List<Lane> incomingLanes;

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
        
        this.nodeSize = neighbourNodes.size();

        sortNodes();

        determineIncomingLanes();

        determineAllowedDirections();
        
        determineDrivePermissions();
        
        determineLaneSets();
    }
    

    public LinkedList<List<Lane>> getLaneSets() {
        return simultaneousLaneSets;
    }

    private ArrayDeque<ArrayDeque<Integer>> searchSpace;
    private void determineLaneSets() {
        
        simultaneousLaneSets = new LinkedList<List<Lane>>();
        
        searchSpace = new ArrayDeque<ArrayDeque<Integer>>();
        
        List<Lane> incomingLanes = getIncomingLanes();        
        
        for (int i = 0; i < incomingLanes.size(); i++) 
            searchSpace.add(new ArrayDeque<Integer>(Arrays.asList(new Integer[] {i})));
        
        
        int lastLaneIndex;
        boolean isEndNode;
        
        ArrayDeque<Integer> currentLaneDeque;
        
        while (!searchSpace.isEmpty()) {
            currentLaneDeque = searchSpace.pollLast();
            
            lastLaneIndex = currentLaneDeque.getLast();
            
            isEndNode = true;
            
            if (lastLaneIndex + 1 < incomingLanes.size()) {
                for (int i = lastLaneIndex + 1; i < incomingLanes.size(); i++) {
                    
                    if (!intersects(i,currentLaneDeque)) {
                        isEndNode = false;
                        
                        ArrayDeque<Integer> newLaneDeque = currentLaneDeque.clone();
                        newLaneDeque.add(i);

                        searchSpace.add(newLaneDeque);
                    }
                }
            } 
            
            if (isEndNode) {
                List<Lane> lanes = new ArrayList<Lane>();
                
                for (Integer i : currentLaneDeque) {
                    lanes.add(incomingLanes.get(i));
                }
                
                //System.out.println(Arrays.deepToString(currentLaneDeque.toArray(new Integer[0])));
                simultaneousLaneSets.add(lanes);
            }
        }
        
        boolean remove = false;
        
        List<Lane> lanes1 = null;
        for (int i = 0; i < simultaneousLaneSets.size(); i++) {
            lanes1 = simultaneousLaneSets.get(i);
            
            remove = false;
            
            for (List<Lane> lanes2 : simultaneousLaneSets) {
                if (lanes1 != lanes2 && isSubsetOf(lanes2, lanes1)) {
                    remove = true;
                    break;
                }
            }
            
            if (remove)
                simultaneousLaneSets.remove(i);
            else
                i++;
        }        
    }
    
    private boolean isSubsetOf(List<Lane> parent, List<Lane> child) {
        for (Lane childLane : child) 
            if (!parent.contains(childLane))
                return false;
        
        return true;
        
    }
    
    private boolean intersects(Integer newLaneIndex, ArrayDeque<Integer> otherLaneIndices) {
        
        Lane newLane = getIncomingLanes().get(newLaneIndex);
        
        Lane otherLane;
        
        for (Integer otherLaneIndex : otherLaneIndices) {
            otherLane = getIncomingLanes().get(otherLaneIndex);
            
            if (intersects(newLane, otherLane))
                return true;
        }
        
        return false;
    }
    
    private boolean intersects(Lane startLane1, Lane startLane2) {
        List<Node> nodes1 = startLane1.getAllowedDirections();
        List<Node> nodes2 = startLane2.getAllowedDirections();
        
        boolean sameSource = startLane1.getStartNode() == startLane2.getStartNode();
        boolean sameDestination;
                
        //for all nodes we are allowed to drive to from startLane1
        for (Node node1 : nodes1) {
            
            if (node1.getRoadSegment(this) == null) {
                System.err.println("Cant find neighbour: " + this.toString() + "," + node1.toString());
                continue;
            }
            
            //for all lanes we can access from node1
            for (Lane endLane1 : node1.getRoadSegment(this).getSourceLanes(this)) {  
                
                //for all nodes we are allowed to drive to from startLane2
                for (Node node2 : nodes2) {
                    
                    if (node2.getRoadSegment(this) == null) {
                        System.err.println("Cant find neighbour: " + this.toString() + "," + node2.toString());
                        continue;
                    }
                    //for all lanes we can access from node2
                    for (Lane endLane2 : node2.getRoadSegment(this).getSourceLanes(this)) {
                        
                        sameDestination = endLane1.getEndNode() == endLane2.getEndNode();
                        if (!(sameSource && sameDestination) && this.intersects(startLane1, endLane1, startLane2, endLane2)) {
                            return true;
                        }
                        
                    }
                }
            }
        }
        
        return false;
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
                
                if (sourceIndex != destinationIndex)
                    couples.add(new Integer[] {sourceIndex, destinationIndex});  
                
                
            }
        }
        
        if (INTERSECTION_ARRAY == null)
            INTERSECTION_ARRAY = new int[256][][][][];
        
        if (INTERSECTION_ARRAY[nodeSize] == null) {
        
            INTERSECTION_ARRAY[nodeSize] = new int[roadCount][roadCount][roadCount][roadCount];

            //we filled the array, lets start checking for possible        
            for (Integer[] couple1 : couples) {            
                for (Integer[] couple2 : couples) {
                    INTERSECTION_ARRAY[nodeSize][couple1[0]][couple1[1]][couple2[0]][couple2[1]] = getIntersectionType(couple1[0], couple1[1], couple2[0], couple2[1]);
//                    System.out.println(
//                            "Route 1: " + couple1[0] + " to " + couple1[1] + ", " +
//                            "Route 2: " + couple2[0] + " to " + couple2[1] +
//                            ". Intersection type: " + getIntersectionTypeString(getIntersectionType(couple1[0], couple1[1], couple2[0], couple2[1]))
//                            );
                    
                }
            }
        }
    }
    
    public boolean intersects(Lane startLane1, Lane endLane1, Lane startLane2, Lane endLane2) {
        int s1 = neighbourNodes.indexOf(startLane1.getStartNode());
        int e1 = neighbourNodes.indexOf(endLane1.getEndNode());
        int s2 = neighbourNodes.indexOf(startLane2.getStartNode());
        int e2 = neighbourNodes.indexOf(endLane2.getEndNode());
        
        int intersects = INTERSECTION_ARRAY[nodeSize][s1][e1][s2][e2];
        
        if (intersects == INTERSECTION_NONE)
            return false;
        else if (intersects == INTERSECTION_ALWAYS)
            return true;
        else if (intersects == INTERSECTION_SAME_DESTINATION) {
            //TODO: Here we need to check which lane is more right or left
            int rightDistance1 = rightDistance(neighbourNodes.indexOf(endLane1.getEndNode()), neighbourNodes.indexOf(startLane1.getStartNode()));
            int rightDistance2 = rightDistance(neighbourNodes.indexOf(endLane1.getEndNode()), neighbourNodes.indexOf(startLane2.getStartNode()));
            
            //if the first route is closer to the right of the destination,
            //it intersects if its lane is more right than the lane of the second route
            if (rightDistance1 < rightDistance2 && endLane1.getLaneId() <= endLane2.getLaneId())
                return true;
            //if the first route is further to the right of the destination,
            //it intersects if its lane is more left than the lane of the second route
            else if (rightDistance1 > rightDistance2 && endLane1.getLaneId() >= endLane2.getLaneId())
                return true;
            //if the source is the same, we just have to make sure we're not crossing lane id
            else if (rightDistance1 == rightDistance2 && Math.signum(startLane1.getLaneId() - startLane2.getLaneId()) != Math.signum(endLane1.getLaneId() - endLane2.getLaneId()))
                return true;
            //all of the above is not the case: happy driving!
            else
                return false;
        } else if (intersects == INTERSECTION_SAME_SOURCE) {
            //TODO: check this too
            int rightDistance1 = rightDistance(neighbourNodes.indexOf(startLane1.getStartNode()), neighbourNodes.indexOf(endLane1.getEndNode()));
            int rightDistance2 = rightDistance(neighbourNodes.indexOf(startLane1.getStartNode()), neighbourNodes.indexOf(endLane2.getEndNode()));
            
            if (startLane1.getLaneId() < startLane2.getLaneId()) {
                //in this case, startLane1 is RIGHT of startLane2.
                //so, endLane1 has to be closer to the right than endLane2
                if (rightDistance1 < rightDistance2) 
                    return false;
                else 
                    return true;
                
            } else {
                //in this case, startLane2 is RIGHT of startLane1.
                //so, endLane2 has to be closer to the right than endLane1
                if (rightDistance2 < rightDistance1) 
                    return false;
                else 
                    return true;
                
            }
        } else {
            //in case of doubt/exception: interssection
            return true;
        }
        
        
    }
    
    public final static int INTERSECTION_NONE = 0;
    public final static int INTERSECTION_SAME_DESTINATION = 1;
    public final static int INTERSECTION_SAME_SOURCE = 2;
    public final static int INTERSECTION_ALWAYS = 3;    
    
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
        
        //THIS ORDER IS IMPORTANT, WE FIRST CHECK FOR DESTINATIONS SINCE THIS WILL ENSURE
        //THAT IN THE LANE INTERSECTION CHECK, IT CHECKS IF THERE IS NO LANE CROSSING WHEN
        //BOTH ROUTES TRAVEL FROM THE SAME SOURCE TO THE SAME DESTINATION
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
    
    public int rightDistance(Lane l1, Lane l2) {
        return rightDistance(neighbourNodes.indexOf(l1.getStartNode()), 
                neighbourNodes.indexOf(l2.getStartNode()));
    }
    
    private int rightDistance(int a, int b) {
        if (a < b)
            a += neighbourNodes.size();
        
        return a - b;
    }
    
    private boolean between(int a1, int a2, int b) {
        return (b > a1 && b < a2);
    }

    private void determineAllowedDirections() {

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


    private void determineIncomingLanes() {
        RoadSegment r;
        
        for (Node n : getNeighbourNodes()) {
            r = this.getRoadSegment(n);

            for (Lane l : r.getDestinationLanes(this)) {
                incomingLanes.add(l);
            }
        }
    }

    public Lane getLaneMapping(Lane incoming, Node outgoing) {
        int id = incoming.getLaneId();
        
        
        
        RoadSegment rs = getRoadSegment(outgoing);
        
        List<Lane> outLanes = rs.getSourceLanes(this);
        
        Lane lowest = null;
        
        for (Lane lane : outLanes) {
            if (lowest == null || lane.getLaneId() < lowest.getLaneId())
                lowest = lane;
            
            if (lane.getLaneId() == id)
                return lane;
        }
        
        return lowest;
        
//        if (laneMap.containsKey(incoming))
//            return laneMap.get(incoming);
//        else
//            return null;
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
