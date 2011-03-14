/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package osmparser;


/**
 *
 * @author Gerrit
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        double
            lat1=50.85305,
            lon1= 5.65738,

            lat2=50.85304,
            lon2=5.66595;

        //float dist = Util.calculateDistance(lat1, lon1, lat2, lon2);
        double v_dist = Util.calculateVerticalDistance(lon1, lat1, lat2);
        double h_dist = Util.calculateHorizontalDistance(lat1, lon1, lon2);
        double dist = Math.sqrt(Math.pow(v_dist, 2.0f) + Math.pow(h_dist,2.0f));

        OSMParser parser = new OSMParser("C:\\Shared\\maastricht_bastion.osm");

        RoadNetwork map = parser.getRoadNetwork();

        System.out.println(map);

        System.out.println(map.getBounds());

    }

}
