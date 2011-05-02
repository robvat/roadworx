/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.List;
/**
 *
 * @author frans
 */
public class Roundabout extends Node
{
    public static final double speed = 9.72222222; //TODO: turn into a function
    private double size; // circumference in meters
    private boolean suc;
    private List<Road> roadsConnected; // list of all the roads connected
    private List<Car> cars;
    private List<Double> times; // a list of times, the cars have to spend on the roundabout

    public Roundabout(Point2D.Double location, double radius, Road[] roads)
    {
        super(location);
        
        this.size = (radius * 2) * Math.PI;
        for(int i = 0; i < (roads.length - 1);i++)
        {
           suc = roadsConnected.add(roads[i]);
        }
    }


    public boolean drivethrough(Car incoming)
    {
        /* atm a normal amount of cars you have
         to stop for but quite random */
        // TODO: find a more realistic way to descide this
        if(cars.size() > 2)
            return false;
        else
            return true;

    }

    public void acceptCar(Car incoming)
    {
        double time;
        double chance = acceptChance();
        //First try to get on the road
        if(Math.random() <= chance)
        {
            //Got through?
        //That means he's part of the node and gets a waiting time assigned
            cars.add(incoming);
            time = size / speed;
            times.add(new Double(time));
           /* TODO: Cars need to be able to be on nodes (and NOT on roads)
            * before this can be implemented fully
            */
        }

    }

    public void update(double timestep)
    {
        throw new UnsupportedOperationException("Not supported yet.");
        //TODO
        // needs delta Time (timestep)
    }

    /* private method using a "homebrew" formula for calculating the chance
     * of acceptance
     */
    private double acceptChance()
    {
        int defaultCar = CarType.CAR.getLength();
        int slots = (int)(size / defaultCar); // needs to be an approximation
        int carAm = cars.size();
        int[] rational = new int[2];
        double dFinal = 1;

        if(carAm < 1)
            return 1;
        else if(carAm > (slots - 2))
            return 0;
        else
        {
            for(int i = 1; i <= carAm; i++)
            {
                if(i == 1)
                {
                    rational[0] = (slots - 2);
                    rational[1] = slots;
                }
                else
                {
                    slots--;    // Warning do not use these ints afterwards
                    carAm--;
                    rational[0] = (slots - 2);
                    rational[1] = slots;
                }
                dFinal = dFinal * (rational[0]/rational[1]);
            }
            return dFinal;
        }
    }

}
