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
    private List<Road> roadsConnected; // list of all the roads connected: NOT NEEDED SEE NODE
    private List<Car> cars;
    private List<Double> times; // a list of times, the cars have to spend on the roundabout

    /*
     * Constructs a roundabout
     * @param roads all the roads that are connected
     * The constructor assumes the roads are given in a
     * clockwise fashion
     */
    public Roundabout(Point2D.Double location, double radius, Road[] roads)
    {
        super(location);
        
        this.size = (radius * 2) * Math.PI;
        for(int i = 0; i < (roads.length - 1);i++)
        {
           suc = roadsConnected.add(roads[i]);
        }
    }

    // Drivethrough decides wether car gets accepter or not
    public boolean drivethrough(Car incoming)
    {
        double factor;
        double chance = acceptChance();

        if(Math.random() <= chance)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void acceptCar(Car incoming)
    {
       double time, factor = 0;
        // he's part of the node and gets a waiting time assigned
        cars.add(incoming);

        try
            {
                factor = directionPercentage(incoming); // not all around!
            } catch (wrongFromException e) {
                // TODO needs to print a not that bad error to the logger
                System.out.println("The lane where car" + incoming + "came "
                        + "from isn't part of this roundabout");
            } catch (wrongToException f)
            {
               /*
                * TODO: big error, needs to be logged and send back
                * to his original road
                */
                System.out.println("The lane where car" + incoming + "is going "
                        + "to isn't part of this roundabout");
            }

        time = (size * factor) / speed;
        times.add(new Double(time));
    }

    /**
     * Each update the cars alrready on the roundabout advance.
     */
    public void update(double timestep)
    {
        double newTime;
        for(int i = 0; i < cars.size(); i++)
        {
             newTime = times.get(i) - timestep;
             if(newTime < 0)
             {
                 //Time to put him at his new Lane
                 getRidOfHim(i);
             }
             else
             {
                 times.set(i, newTime);
             }
        }
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


    /*
     * Car at spot i in the lists, will be put on his way
     * with a little knapsack and everything!
     */
    private void getRidOfHim(int i)
    {
        //TODO: change Car (retrieve lane from road) and change lane
        Car rem = cars.get(i);
        //Node next
        /*
         * Put the car on his way again!
         */
        cars.remove(i);
        times.remove(i);
    }

    /*
     * Calculates the percentage of roundabout the car wants to travel
     */
    private double directionPercentage(Car c) throws wrongFromException,
            wrongToException
    {
        Node direction = null;
        Road togo,from = null;
        int[] pos = new int[2];
        pos[0] = 666;
        pos[1] = 666;
        int size, v;
        double outcome;
        size = roadsConnected.size();

        // TODO: direction = c.blabla
        //from = c.currentRoad // and get rid of the = null's
        togo = super.getRoad(direction);
        for(int i = 0; i < roadsConnected.size(); i++)
        {
            /*
             * TODO: equals might be slow, use id's in the future
             */
           if(togo.equals(roadsConnected.get(i)))
               pos[0] = i + 1;
           else if(from.equals(roadsConnected.get(i)))
               pos[1] = i + 1;
        }
        if(pos[0] == 666)
            throw new wrongToException();
        if(pos[1] == 666)
            throw new wrongFromException();
        if(pos[0] < pos[1])
        {
            v = size - pos[1];
            v = v + pos[0];
            outcome = (v / size);
        }
        else if (pos[0] == pos[1])
        {
            outcome = 0;
        }
        else
        {
            v = pos[0] - pos[1];
            outcome = (v / size);
        }

        return outcome;
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    /*
     * remember pos 0 = to go
     * pos 1 = where you're from
     */

    private class wrongFromException extends Exception
    {
        public wrongFromException()
        {
            super();
        }
    }

    private class wrongToException extends Exception
    {
        public wrongToException()
        {
            super();
        }
    }

    @Override
    public String toString()
    {
        return "Roads Connected " + roadsConnected.size() +
                " Cars on driverway" + cars.size();
    }

    public void logTimePrint()
    {
        // TODO in the log, have a overview of all the cars and their times
    }
}
