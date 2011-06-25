/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficownage.simulation;

import java.util.List;

/**
 *
 * @author Gerrit
 */
public class Route {

    private Node nextNode = null;
    private Node firstNode = null;
    private boolean endOfRoute;

    private int currentIndex;

    private List<Node> nodeList;

    public Route(List<Node> nodeList) {

        this.nodeList = nodeList;

        endOfRoute = false;
        currentIndex = 0;

        nextNode = nodeList.get(currentIndex);
    }

    public void determineNext(Node approachingNode) {

        if (approachingNode != nextNode)
            return;
        

        Node currentNode = nextNode;

        currentIndex ++;
        if (currentIndex == nodeList.size()) {
            endOfRoute = true;
            nextNode = null;
        } else {
            nextNode = nodeList.get(currentIndex);
        }

    }

    public boolean isEndOfRoute() {
        return endOfRoute;
    }

    public Node getNextNode() {
        //determineNext();

        return nextNode;
    }
}
