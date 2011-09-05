package trafficownage.simulation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
/**
 * A roundabout found on a road near you!
 * @author frans
 */
public class Roundabout extends Node
{
    /**
     * The average speed on a roundabout, used in the calculation
     */
    public static final double speed = 6.0; //FIXME: turn into a function
    private double size; // circumference in meters
    private boolean suc;
    private List<Road> roadsConnected; // list of all the roads connected: NOT NEEDED SEE NODE
    private List<Car> cars;
    private List<Double> times; // a list of times, the cars have to spend on the roundabout
    private List<Lane> destinations; //List of destinations of the cars
    private long carsAdded; //validation testing

    /**
     *  Constructs a roundabout
     *  Assumes all the roads get added in a clockwise manner
     * @param location Location on the map
     * @param radius Radius of the roundabout in meters
     */
    public Roundabout(Point2D.Double location, double radius)
    {
        super(location);
        
        this.size = (radius * 2) * Math.PI;

        cars = new ArrayList<Car>();
        times = new ArrayList<Double>();
        destinations = new ArrayList<Lane>();
        // Warning, doesn't have any roads yet at this point!
    }

    /**
     * Decides wether you can drive onto the roundabout or not
     * @param incoming A car heading for the Node
     */
    public boolean drivethrough(Car incoming)
    {
        // incoming is NOT USED!
        double chance = acceptChance();

        Lane l = incoming.getNextLane();

        if (l == null || !l.acceptsCarAdd(incoming))
            return false;


        if(Math.random() <= chance)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Placing the car on the roundabout (No matter what)
     * @param incoming The car you want placed on the roundabout
     */
    public void acceptCar(Car incoming)
    {

        if (incoming.getNextLane() == null || !incoming.getNextLane().acceptsCarAdd(incoming)) {
            System.err.println("Car did not check correctly if it could join a lane.");
            incoming.determineNextLane();
        }

        double time, factor = 0;

        // he's part of the node and gets a waiting time assigned
        cars.add(incoming);
        destinations.add(incoming.getNextLane());
        incoming.getCurrentLane().removeCar(incoming);
        incoming.setNoLane();

        try
        {
            factor = directionPercentage(incoming); // not all around!
        } catch (wrongFromException e) {
            // FIXME needs to print a not that bad error to the logger
            System.out.println("The lane where car" + incoming + "came "
                    + "from isn't part of this roundabout");
        } catch (wrongToException f)
        {
           /*
            * FIXME: big error, needs to be logged and send back
            * to his original road
            */
            System.out.println("The lane where car" + incoming + "is going "
                    + "to isn't part of this roundabout");
        }

        time = (size * factor) / speed;
        times.add(new Double(time));
        carsAdded++;
    }

    /**
     * Each update, the cars already on the roundabout advance
     * @param timestep amount of seconds that time has shifted
     */
    public void update(double timestep)
    {
        super.update(timestep);
        
        double newTime;
        for(int i = 0; i < cars.size(); i++)
        {
             newTime = times.get(i) - timestep;
             if(newTime < 0)
             {
                 Car daCar = cars.get(i);
                 //Time to put him on his new Lane
                 if(destinations.get(i).acceptsCarAdd(daCar))
                 {
                    getRidOfHim(i);
                    i--; // 1 is removed so the index needs to be lowered again
                    /*
                     * Cars.size should be less from this point on
                     * Another car should be at the same position
                     * so the same position needs to be checked again
                     */
                 }
             }
             else
             {
                 times.set(i, newTime);
             }
        }
    }

    /**
     * private method using a "homebrew" formula for calculating the chance
     * of acceptance
     */
    private double acceptChance()
    {
        double defaultCar = CarType.MEDIUMCAR.getLength();
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


    /**
     * Car at spot i in the lists, will be put on his way
     * with a little knapsack and everything!
     */
    private void getRidOfHim(int i)
    {
//        List<Lane> possibleLanes;
        Car rem = cars.get(i);
//        Node next = rem.getNextNode();
//        RoadSegment togo = super.getRoadSegment(next);
//        possibleLanes = togo.getDestinationLanes(next);
//        rem.setLane(possibleLanes.get(0));
        destinations.get(i).addCar(rem);
        
        // FIXME Check if something is on lane 0 and put him on lane 1
        // FIXME !!! Car.advance() or not ??, time for next node
        cars.remove(i);
        times.remove(i);
        destinations.remove(i);
    }

    /**
     * Calculates the percentage of roundabout the car wants to travel
     */
    private double directionPercentage(Car c) throws wrongFromException,
            wrongToException
    {
        Node togo, from = null;
        int[] pos = new int[2];
        pos[0] = 666; //togo
        pos[1] = 666; //from
        int size, v;
        double outcome;
        List<Node> destinations = super.getDestinationNodes();
        size = destinations.size();

        togo = c.getNextNode();
        from = c.getPreviousNode();
        // Assuming there are no 1-way lanes going to roundabout-node
        for(int i = 0; i < size; i++)
        {
           // FIXME: might use id's in the future
           if(togo.equals(destinations.get(i)))
               pos[0] = i + 1;
           else if(from.equals(destinations.get(i)))
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
            outcome = ((double)v / (double)size);
        }
        else if (pos[0] == pos[1])
        {
            outcome = 0;
        }
        else
        {
            v = pos[0] - pos[1];
            outcome = ((double)v / (double)size);
        }

        return outcome;
    }

    /**
     *
     */
    @Override
    public void init(NodeListener listener) {
        super.init(listener);
        // might add here more later!
    }

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
        return "Node:" + this.getLocation() + " Cars passed through: " + carsAdded + " Cars on driverway: " + cars.size();
    }
}
