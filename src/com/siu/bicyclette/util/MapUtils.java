package com.siu.bicyclette.util;

import com.google.android.maps.MapView;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public final class MapUtils {

    private MapUtils() {
    }

    public static double distanceFrom(double latitude1, double longitude1, double latitude2, double longitude2) {

        double earthRadius = 3958.75;

        double dLat = Math.toRadians(latitude2 - latitude1);
        double dLng = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public static int latitudeToX(double latitude, MapView mapView) {
        return (int) Math.round(((-1 * latitude) + 90) * (mapView.getHeight() / 180));
    }

    public static int longitudeToX(double longitude, MapView mapView) {
        return (int) Math.round((longitude + 180) * (mapView.getWidth() / 360));
    }
}
