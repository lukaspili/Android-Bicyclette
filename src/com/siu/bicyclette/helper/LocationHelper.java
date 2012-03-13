package com.siu.bicyclette.helper;

import android.location.Location;
import android.util.Log;
import com.google.android.maps.GeoPoint;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public final class LocationHelper {

    private static final double PARIS_LATITUDE = 48.856614;
    private static final double PARIS_LONGITUDE = 2.352222;

    private static final int FRANCE_LATITUDE_E6 = 46227638;
    private static final int FRANCE_LONGITUDE_E6 = 2213749;

    private LocationHelper() {
    }

    public static GeoPoint getGeoPoint(String latitudeAsString, String longitudeAsString) {

        double latitude, longitude;

        try {
            latitude = Double.valueOf(latitudeAsString);
            longitude = Double.valueOf(longitudeAsString);
        } catch (NumberFormatException e) {
            Log.w(LocationHelper.class.getName(), "Cannot cast latitude " + latitudeAsString + " or longiture " + longitudeAsString + " to double values", e);
            return null;
        }

        return getGeoPoint(latitude, longitude);
    }

    public static GeoPoint getGeoPoint(double latitude, double longitude) {
        return new GeoPoint((int) (latitude * 1e6), (int) (longitude * 1e6));
    }

    public static GeoPoint getGeoPoint(Location location) {
        return getGeoPoint(location.getLatitude(), location.getLongitude());
    }

    public static GeoPoint getParisGeoPoint() {
        return getGeoPoint(PARIS_LATITUDE, PARIS_LONGITUDE);
    }

    public static GeoPoint getFranceGeoPoint() {
        return new GeoPoint(FRANCE_LATITUDE_E6, FRANCE_LONGITUDE_E6);
    }
}
