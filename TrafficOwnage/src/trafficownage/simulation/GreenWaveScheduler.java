/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.util.List;

/**
 *
 * @author BeerBrewer
 */
public class GreenWaveScheduler {

    private List<GreenWave> greenWaves;

    public GreenWaveScheduler(){

    }

    // in here we need something to determine subsequent roads
    public void init(){

    }

    //this class consists of a list of subsequent roads and operators on it.
    private class GreenWave
    {
        private List<Road> roads;

        public void init(List<Road> roads){
            this.roads = roads;
        }


    }

}
