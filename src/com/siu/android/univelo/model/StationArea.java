package com.siu.android.univelo.model;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationArea {

    private static final int DISTANCE = 1000;

    private int totalX = 0;
    private int totalY = 0;

    private int maxDistance = DISTANCE;

    private int maxX;
    private int maxY;
    private int minY;
    private int minX;

    private GeoPoint averageGeoPoint;
    private List<StationAreaElement> elements = new ArrayList<StationAreaElement>();

    public StationArea(StationAreaElement element) {
        elements.add(element);
    }

    public boolean isInArea(StationAreaElement newElement) {

        return maxX == 0 || Math.pow((maxX - newElement.x), 2) + Math.pow((maxY - newElement.y), 2) <= DISTANCE ||
                Math.pow((minX - newElement.x), 2) + Math.pow((minY - newElement.y), 2) <= DISTANCE;
    }

    public void addElement(StationAreaElement element) {

        totalX += element.x;
        totalY += element.y;

        maxX = (element.x > maxX || maxX == 0) ? element.x : maxX;
        minX = (element.x < minX || maxX == 0) ? element.x : minX;

        maxY = (element.y > maxY || maxY == 0) ? element.y : maxY;
        minY = (element.y < minY || minY == 0) ? element.y : minY;

        elements.add(element);
        averageGeoPoint = null;
    }

    public GeoPoint getAverageGeoPoint(Projection projection) {

        if (null == averageGeoPoint) {

            totalX /= elements.size();
            totalY /= elements.size();

            averageGeoPoint = projection.fromPixels(totalX, totalY);
        }

        return averageGeoPoint;
    }
}
