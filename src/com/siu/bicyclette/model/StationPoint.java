package com.siu.bicyclette.model;

import android.graphics.Point;
import com.google.android.maps.GeoPoint;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationPoint extends GeoPoint {

    int x;
    int y;

    public StationPoint(double lat, double lon) {
        super((int) (lat * 1E6), (int) (lon * 1E6));
    }

    public StationPoint(int lat, int lon, int x, int y) {
        super(lat, lon);
        this.x = x;
        this.y = y;
    }

    public StationPoint(double lat, double lon, int x, int y) {
        this((int) (lat * 1E6), (int) (lon * 1E6), x, y);
    }

    public void setPoint(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
