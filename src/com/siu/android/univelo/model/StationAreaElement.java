package com.siu.android.univelo.model;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationAreaElement {

    int lat;
    int lon;

    int x;
    int y;


    public StationAreaElement(int lat, int lon, int x, int y) {
        this.lat = lat;
        this.lon = lon;
        this.x = x;
        this.y = y;
    }

    public StationAreaElement(double lat, double lon, int x, int y) {
        this((int) (lat * 1E6), (int) (lon * 1E6), x, y);
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getLon() {
        return lon;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
