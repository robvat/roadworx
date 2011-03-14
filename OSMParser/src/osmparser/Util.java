/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package osmparser;

/**
 *
 * @author Gerrit
 */
public class Util {

    private static double earthRadius = 3958.75;

    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        int meterConversion = 1609;

        return new Double(dist * meterConversion).doubleValue();
    }

    public static double calculateHorizontalDistance(double lat, double lng1, double lng2) {
        return calculateDistance(lat, lng1, lat, lng2);
    }

    public static double calculateVerticalDistance(double lng, double lat1, double lat2) {
        return calculateDistance(lat1, lng, lat2, lng);
    }



}
