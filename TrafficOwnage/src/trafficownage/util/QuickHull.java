/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.util;

import trafficownage.simulation.Node;
import java.util.ArrayList;

/*
 * Copyright (c) 2007 Alexander Hristov.
 * http://www.ahristov.com
 *
 * Feel free to use this code as you wish, as long as you keep this copyright
 * notice. The only limitation on use is that this code cannot be republished
 * on other web sites.
 *
 * As usual, this code comes with no warranties of any kind.
 *
 *
 */

/*
 * Class changed to fit into this project by Gerrit Drost <gerritdrost@gmail.com>
 * Original source: http://www.ahristov.com/tutorial/geometry-games/convex-hull.html
 */

public class QuickHull {

    public ArrayList<Node> quickHull(ArrayList<Node> points) {
        ArrayList<Node> convexHull = new ArrayList<Node>();

        if (points.size() < 3) {
            return (ArrayList) points.clone();
        }
        // find extremals
        int minPoint = -1, maxPoint = -1;

        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;

        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).getLocation().x < minX) {
                minX = points.get(i).getLocation().x;
                minPoint = i;
            }
            if (points.get(i).getLocation().x > maxX) {
                maxX = points.get(i).getLocation().x;
                maxPoint = i;
            }
        }

        Node A = points.get(minPoint);
        Node B = points.get(maxPoint);

        convexHull.add(A);
        convexHull.add(B);

        points.remove(A);
        points.remove(B);

        ArrayList<Node> leftSet = new ArrayList<Node>();
        ArrayList<Node> rightSet = new ArrayList<Node>();

        for (int i = 0; i < points.size(); i++) {
            Node p = points.get(i);

            if (pointLocation(A, B, p) == -1)
                leftSet.add(p);
            else
                rightSet.add(p);

        }

        hullSet(A, B, rightSet, convexHull);
        hullSet(B, A, leftSet, convexHull);

        points.add(A);
        points.add(B);

        return convexHull;
    }

    public int pointLocation(Node A, Node B, Node P) {
        double cp1 = (B.getLocation().x - A.getLocation().x) * (P.getLocation().y - A.getLocation().y) - (B.getLocation().y - A.getLocation().y) * (P.getLocation().x - A.getLocation().x);
        return (cp1 > 0) ? 1 : -1;
    }

    public double distance(Node A, Node B, Node C) {

        double ABx = B.getLocation().x - A.getLocation().x;
        double ABy = B.getLocation().y - A.getLocation().y;
        double num = ABx * (A.getLocation().y - C.getLocation().y) - ABy * (A.getLocation().x - C.getLocation().x);

        if (num < 0) {
            num = -num;
        }

        return num;
    }

    public void hullSet(Node A, Node B, ArrayList<Node> set, ArrayList<Node> hull) {

        int insertPosition = hull.indexOf(B);

        if (set.isEmpty()) {
            return;
        } else if (set.size() == 1) {
            Node p = set.get(0);
            set.remove(p);
            hull.add(insertPosition, p);
            return;
        }

        double dist = Double.MIN_VALUE;

        int furthestPoint = -1;
        for (int i = 0; i < set.size(); i++) {
            Node p = set.get(i);

            double distance = distance(A, B, p);

            if (distance > dist) {
                dist = distance;
                furthestPoint = i;
            }
        }

        Node P = set.get(furthestPoint);
        set.remove(furthestPoint);
        hull.add(insertPosition, P);

        // Determine who's to the left of AP
        ArrayList<Node> leftSetAP = new ArrayList<Node>();
        for (int i = 0; i < set.size(); i++) {
            Node M = set.get(i);
            if (pointLocation(A, P, M) == 1) {
                //set.remove(M);
                leftSetAP.add(M);
            }
        }

        // Determine who's to the left of PB
        ArrayList<Node> leftSetPB = new ArrayList<Node>();
        for (int i = 0; i < set.size(); i++) {
            Node M = set.get(i);
            if (pointLocation(P, B, M) == 1) {
                //set.remove(M);
                leftSetPB.add(M);
            }
        }
        hullSet(A, P, leftSetAP, hull);
        hullSet(P, B, leftSetPB, hull);
    }
}
