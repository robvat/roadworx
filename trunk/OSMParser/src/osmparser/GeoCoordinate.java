/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package osmparser;

/**
 *
 * @author Gerrit
 */
public class GeoCoordinate {
    private double _lng,_lat;

    public GeoCoordinate(double lng, double lat) {
        _lng = lng;
        _lat = lat;
    }

    public double getLongitude() {
        return _lng;
    }

    public double getLatitude() {
        return _lat;
    }

    @Override
    public String toString() {
        return "lng: " + Double.toString(_lng) + ", lat: " + Double.toString(_lat);
    }
}
