package com.siu.bicyclette.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationCluster {

    private static final int DISTANCE = 40;

    private int x;
    private int y;

    private int totalX;
    private int totalY;

    private int maxX;
    private int maxY;

    private int minX;
    private int minY;

    private List<StationClusterPoint> stationClusterPoints = new ArrayList<StationClusterPoint>();

    public StationCluster(StationClusterPoint stationClusterPoint) {
        addPoint(stationClusterPoint);
    }

    public void addPoint(StationClusterPoint stationClusterPoint) {

        totalX += stationClusterPoint.x;
        totalY += stationClusterPoint.y;

        stationClusterPoints.add(stationClusterPoint);

        x = totalX / stationClusterPoints.size();
        y = totalY / stationClusterPoints.size();

        maxX = x + DISTANCE;
        maxY = y + DISTANCE;

        minX = x - DISTANCE;
        minY = y - DISTANCE;
    }

    public boolean isInCluster(StationClusterPoint stationClusterPoint) {

        return maxX == 0 || (stationClusterPoint.x <= maxX && stationClusterPoint.y <= maxY &&
                stationClusterPoint.x >= minX && stationClusterPoint.y >= minY);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<StationClusterPoint> getStationClusterPoints() {
        return stationClusterPoints;
    }
}
