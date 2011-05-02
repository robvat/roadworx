/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;
/**
 *
 * @author Gerrit
 */
public abstract class Node
{
    

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
    abstract void update();
}
