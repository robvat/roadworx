/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import trafficownage.simulation.Car;
import trafficownage.simulation.Node;
import trafficownage.simulation.RoadSegment;

/**
 *
 * @author Gerrit Drost <gerritdrost@gmail.com>
 */
public class Pathfinding {


    public static List<Node> fastestRoute(Car car, Node start_node, Node end_node, List<Node> nodes) {

        List<Node> solution = new LinkedList<Node>();

        double nodecost;
        RoadSegment rs;

        ArrayList<Node> opennodes = new ArrayList<Node>();
        ArrayList<Node> closednodes = new ArrayList<Node>();

        //Choose a node and set up H
        //depending on the player we start move in the x or y direction
        start_node.g = 0;
        start_node.h = start_node.getLocation().distance(end_node.getLocation());
        //Set up F
        start_node.f = start_node.h + start_node.g;

        //Add the initial position
        opennodes.add(start_node);

        Node cur_node = null;

        while (!opennodes.isEmpty()) {
            //SELECT THE NEXT NODE
            cur_node = null;

            for (Node n : opennodes) {
                if (cur_node == null || (n.f < cur_node.f) || (n.f == cur_node.f && n.h < cur_node.h)) {
                    cur_node = n;
                }
            }
            //SWITCH THE NODE TO THE CLOSED LIST
            opennodes.remove(cur_node);
            closednodes.add(cur_node);

            if (cur_node == end_node) {

                Node n = end_node;

                while (n != start_node) {
                    solution.add(n);
                    n = n.parent;
                }

                //Force a quit of the algorithm by clearing the opennodes
                opennodes.clear();

            } else {
                //FOR ALL NEIGHBOURS
                for (Node n : cur_node.getDestinationNodes()) {
                    //NOT ALLOWED TO BE ONT HE CLOSED LIST
                    if (!closednodes.contains(n)) {
                        //IF NOT ON THE OPEN LIST, PUT IT THERE
                        rs = cur_node.getRoadSegment(n);
                        nodecost = cur_node.g + (rs.getLength() + rs.getMaxSpeed());

                        if (!opennodes.contains(n)) {

                            n.parent = cur_node;

                            n.g = nodecost;

                            n.h = n.getLocation().distance(end_node.getLocation());

                            n.f = n.g + n.h;

                            opennodes.add(n);

                        } else {
                            if (nodecost < n.g) {
                                n.parent = cur_node;
                                n.g = nodecost;
                            }
                        }
                    }
                }
            }
        }

        return solution;

    }

    public static List<Node> shortestRoute(Node start_node, Node end_node, List<Node> nodes) {

        List<Node> solution = new LinkedList<Node>();

        double nodecost;

        ArrayList<Node> opennodes = new ArrayList<Node>();
        ArrayList<Node> closednodes = new ArrayList<Node>();

        //Choose a node and set up H
        //depending on the player we start move in the x or y direction
        start_node.g = 0;
        start_node.h = start_node.getLocation().distance(end_node.getLocation());
        //Set up F
        start_node.f = start_node.h + start_node.g;

        //Add the initial position
        opennodes.add(start_node);

        Node cur_node = null;

        while (!opennodes.isEmpty()) {
            //SELECT THE NEXT NODE
            cur_node = null;

            for (Node n : opennodes) {
                if (cur_node == null || (n.f < cur_node.f) || (n.f == cur_node.f && n.h < cur_node.h)) {
                    cur_node = n;
                }
            }
            //SWITCH THE NODE TO THE CLOSED LIST
            opennodes.remove(cur_node);
            closednodes.add(cur_node);

            if (cur_node == end_node) {

                Node n = end_node;
                
                while (n != start_node) {
                    solution.add(n);
                    n = n.parent;
                }

                //Force a quit of the algorithm by clearing the opennodes
                opennodes.clear();

            } else {
                //FOR ALL NEIGHBOURS
                for (Node n : cur_node.getDestinationNodes()) {
                    //NOT ALLOWED TO BE ONT HE CLOSED LIST
                    if (!closednodes.contains(n)) {
                        //IF NOT ON THE OPEN LIST, PUT IT THERE

                        nodecost = cur_node.g + (cur_node.getLocation().distance(n.getLocation()));

                        if (!opennodes.contains(n)) {

                            n.parent = cur_node;

                            n.g = nodecost;

                            n.h = n.getLocation().distance(end_node.getLocation());

                            n.f = n.g + n.h;

                            opennodes.add(n);

                        } else {
                            if (nodecost < n.g) {
                                n.parent = cur_node;
                                n.g = nodecost;
                            }
                        }
                    }
                }
            }
        }

        return solution;

    }
}
