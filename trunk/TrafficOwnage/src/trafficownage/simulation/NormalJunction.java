/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;

/**
 *
 * @author Stefan
 */
public class NormalJunction extends Node{

    Road[] sortedRoads;

    public NormalJunction(Point2D.Double location){
        super(location);
        Road[] sortedRoads = this.sortRoads();
    }

    @Override
    public boolean drivethrough(Car incoming) {
        // TODO: first check if the junction is clear
        // if so return false, else continue

        Lane incomingLane = incoming.getLane();
        int indexIncomingRoad = 0;
        int indexDestinationRoad = 0;

        for(int i = 0; i < sortedRoads.length; i++){
            if(sortedRoads[i] != null && this.getRoad(incoming.getNextNode()).equals(sortedRoads[i])){
                indexDestinationRoad = i;
            }else{
                for(int j = 0; j < sortedRoads[i].getAllLanes().size(); j++){
                    if(incomingLane.equals(sortedRoads[i].getAllLanes().get(j))){
                        indexIncomingRoad = i;
                    }
                }
            }
        }

        if(indexDestinationRoad == (indexIncomingRoad + 1) % sortedRoads.length){
            // you can just turn right
            return true;
        } else if(indexDestinationRoad == (indexIncomingRoad + 2) % sortedRoads.length){
            // TODO: check right, because you want to go straight ahead
        } else{
            // TODO: check right and opposite, because you want to go left
        }


        return true; // just to get rid of the missing return statement error
    }

    @Override
    void acceptCar(Car incoming) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void update(double timestep) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
