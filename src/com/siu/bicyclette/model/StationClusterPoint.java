package com.siu.bicyclette.model;

import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.siu.bicyclette.Station;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationClusterPoint extends GeoPoint {

    int x;
    int y;

    private Station station;

    public StationClusterPoint(Station station) {
        super((int) (station.getCoordLat() * 1E6), (int) (station.getCoordLong() * 1E6));
        this.station = station;
    }

    public void setPoint(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public Station getStation() {
        return station;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
