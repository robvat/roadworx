package trafficownage.simulation;

import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tester
 */
public class SequenceScheduler {
    
    private List<Sequence> sequences;
    private List<Double> startTimes;
    
    public SequenceScheduler(List<Sequence> sequenceList){
        sequences = sequenceList;
        //put random times as start times to start sorting
        for (int i = 0; i < sequences.size(); i++) {
            startTimes.add(Math.random()*100); //not sure what time variable to use here depends how many sequences i guess            
        }
    }
    
    public void init(){
        while(finsih() != true){
            calculateHowClose(sequences);
            selectBest(sequences);
            crossOverBestOnes(sequences);
            incrementTimes(sequences);            
        }
    
        
    private boolean finsih(){
        return false;
    }
    
    private void calculateHowClose(List<Sequence> sequencesList){
        //need to check how well it all matches with the times now
    }
    
    private void selectBest(List<Sequence> sequencesList){
        //select the sequences with best 
    }
    
    private void crossOverBestOnes(List<Sequence> sequencesList){
        //mix up the selected Best ones
    }
    
    private void incrementTimes(List<Sequence> sequencesList){
        //change the times of all the sequences a little and start again
    }
}
