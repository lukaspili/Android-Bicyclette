package com.siu.bicyclette.app.activity;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.siu.bicyclette.City;
import com.siu.bicyclette.R;
import com.siu.bicyclette.Station;
import com.siu.bicyclette.app.map.EnhancedMapView;
import com.siu.bicyclette.app.map.ItemizedOverlay;
import com.siu.bicyclette.app.task.GeocoderLocationByNameTask;
import com.siu.bicyclette.app.toast.AppToast;
import com.siu.bicyclette.model.StationCluster;
import com.siu.bicyclette.model.ClusteredGeoPoint;
import com.siu.bicyclette.model.StationClusterPoint;
import com.siu.bicyclette.model.StationStatus;
import com.siu.bicyclette.service.*;
import com.siu.bicyclette.util.LocationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationsMapActivity extends SherlockMapActivity {

    public static int count = 0;

    private EnhancedMapView mapView;

    private LocalBroadcastManager localBroadcastManager;

    private StationStatusService stationStatusService;
    private BroadcastReceiver stationStatusUpdateReceiver;
    private BroadcastReceiver cityGetByLocationReceiver;
    private BroadcastReceiver stationsGetByCityReceiver;
    private BroadcastReceiver getCurrentLocationReceiver;
    private BroadcastReceiver calculateClusterReceiver;

    private LocationService locationService;
    private LocationService.LocationResultListener locationResultListener;

    private GeocoderService geocoderService;
    private GeocoderLocationByNameTask.Listener geocoderLocationByNameTaskListener;

    private ItemizedOverlay positionItemizedOverlay;
    private ItemizedOverlay stationOverlay;

    private City currentCity;
    private List<StationCluster> stationClusters = new ArrayList<StationCluster>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.stations_map_activity);

        mapView = (EnhancedMapView) findViewById(R.id.map);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        initStations();
        initActionBar();
        initLocation();
        initMap();

        stationStatusService.startStationStatusUpdate(stationStatusUpdateReceiver);
        localBroadcastManager.registerReceiver(getCurrentLocationReceiver, new IntentFilter(GetCurrentLocationService.class.getSimpleName()));
        localBroadcastManager.registerReceiver(cityGetByLocationReceiver, new IntentFilter(CityGetByLocationService.class.getSimpleName()));
        localBroadcastManager.registerReceiver(stationsGetByCityReceiver, new IntentFilter(StationsGetByCityService.class.getSimpleName()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        startCurrentLocation();
//        locationService.startCurrentLocation(locationResultListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        locationService.stopCurrentLocation();
        geocoderService.stopLocationByNameIfRunning();
    }

    @Override
    protected void onDestroy() {

        stationStatusService.stopStationsStatusUpdate();
        localBroadcastManager.unregisterReceiver(cityGetByLocationReceiver);
        localBroadcastManager.unregisterReceiver(stationStatusUpdateReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.bikes_map_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_search_address:

                // stop potential running tasks
                locationService.stopCurrentLocation();

                onSearchRequested();

                break;

            case R.id.menu_location:

                // stop potential running tasks
                geocoderService.stopLocationByNameIfRunning();

                startCurrentLocation();

                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {

        setIntent(intent);

        String name;

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            name = intent.getStringExtra(SearchManager.QUERY);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            name = intent.getDataString();
        } else {
            return;
        }

        geocoderService.startLocationByName(name, geocoderLocationByNameTaskListener);
    }

    private void initStations() {

        stationStatusService = new StationStatusService(this);
        stationStatusUpdateReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                List<StationStatus> stationStatuses = intent.getParcelableArrayListExtra(Intent.EXTRA_RETURN_RESULT);

//                if (stations.isEmpty()) {
//                    Log.d(getClass().getName(), "Stations are empty, there is no status to update");
//                    return;
//                }
            }
        };

        getCurrentLocationReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                setSupportProgressBarIndeterminateVisibility(false);

                Location location = intent.getExtras().getParcelable(Intent.EXTRA_RETURN_RESULT);

                if (null == location) {
                    new AppToast(StationsMapActivity.this, R.string.map_error_getcurrentlocation).show();
                    return;
                }

                intent = new Intent(StationsMapActivity.this, CityGetByLocationService.class);
                intent.putExtra(CityGetByLocationService.EXTRA_LAT, location.getLatitude());
                intent.putExtra(CityGetByLocationService.EXTRA_LONG, location.getLongitude());

                startService(intent);
            }
        };

        cityGetByLocationReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                City city = intent.getExtras().getParcelable(Intent.EXTRA_RETURN_RESULT);

                if (null == city || city.equals(currentCity)) {
                    Log.d(getClass().getName(), "City not found or same as previous : " + currentCity.getName());
                    return;
                }

                currentCity = city;

                intent = new Intent(context, StationsGetByCityService.class);
                intent.putExtra(StationsGetByCityService.EXTRA_CITY, currentCity);

                startService(intent);
            }
        };

        stationsGetByCityReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                List<Station> stations = intent.getParcelableArrayListExtra(Intent.EXTRA_RETURN_RESULT);

                GeoPoint bottomLeftGeoPoint = mapView.getProjection().fromPixels(0, mapView.getHeight() - 1);
                GeoPoint topRightGeoPoint = mapView.getProjection().fromPixels(mapView.getWidth() - 1, 0);

                intent = new Intent(StationsMapActivity.this, StationClustersCalculationService.class);

                Point point = new Point();
                Station station;
                List<StationCluster> clusterPoints = new ArrayList<StationCluster>();
                StationClusterPoint stationClusterPoint;

                long time = System.currentTimeMillis();
                int c1 = 0, c2 = 0;

                while (!stations.isEmpty()) {

                    c1++;

                    station = stations.get(0);
                    stations.remove(0);

                    stationClusterPoint = new StationClusterPoint(station);
                    mapView.getProjection().toPixels(stationClusterPoint, point);
                    stationClusterPoint.setPoint(point);

                    if (stationClusterPoint.getLatitudeE6() < bottomLeftGeoPoint.getLatitudeE6() || stationClusterPoint.getLongitudeE6() < bottomLeftGeoPoint.getLongitudeE6() ||
                            stationClusterPoint.getLatitudeE6() > topRightGeoPoint.getLatitudeE6() || stationClusterPoint.getLongitudeE6() > topRightGeoPoint.getLongitudeE6()) {
                        continue;
                    }

                    for (StationCluster clusterPoint : clusterPoints) {

                        c2++;

                        if (clusterPoint.isInCluster(stationClusterPoint)) {
                            clusterPoint.addPoint(stationClusterPoint);
                            stationClusterPoint = null;
                            break;
                        }
                    }

                    if (null != stationClusterPoint) {
                        clusterPoints.add(new StationCluster(stationClusterPoint));
                    }
                }

                Log.d(getClass().getName(), "Complete in " + (System.currentTimeMillis() - time) + " ms.");
                Log.d(getClass().getName(), "Loop 1 = " + c1);
                Log.d(getClass().getName(), "Loop 2 = " + c2);
                Log.d(getClass().getName(), "Cluster points = " + clusterPoints.size());

                time = System.currentTimeMillis();

                stationOverlay.getOverlayItems().clear();

                for (StationCluster clusterPoint : clusterPoints) {
                    stationOverlay.addOverlay(new OverlayItem(mapView.getProjection().fromPixels(clusterPoint.getX(), clusterPoint.getY()), null, null));
                }
                Log.d(getClass().getName(), "Draw in " + (System.currentTimeMillis() - time) + " ms.");


                mapView.invalidate();
            }
        };

        calculateClusterReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {


            }
        };
    }

    private void updateMap() {

    }

    private void initActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    private void initLocation() {

//        locationService = new LocationService();
//        locationResultListener = new LocationService.LocationResultListener() {
//
//            @Override
//            public void onLocationSuccess(Location location) {
////                startCityLocation(location);
//                locatePositionOnMap(LocationUtils.getGeoPoint(location));
//            }
//
//            @Override
//            public void onLocationFailure() {
//                new AppToast(StationsMapActivity.this, R.string.map_error_getcurrentlocation).show();
//            }
//
//            @Override
//            public void onLocationStart() {
//                setSupportProgressBarIndeterminateVisibility(true);
//            }
//
//            @Override
//            public void onLocationStop() {
//                setSupportProgressBarIndeterminateVisibility(false);
//            }
//        };

        geocoderService = new GeocoderService();
        geocoderLocationByNameTaskListener = new GeocoderLocationByNameTask.Listener() {

            @Override
            public void onStart() {
                setSupportProgressBarIndeterminateVisibility(true);
            }

            @Override
            public void onStop() {
                setSupportProgressBarIndeterminateVisibility(false);
            }

            @Override
            public void onSuccess(GeoPoint geoPoint) {
//                startCityLocation(LocationUtils.getLocation(geoPoint));
                locatePositionOnMap(geoPoint);
            }

            @Override
            public void onFailure(String name) {
                new AppToast(StationsMapActivity.this, String.format(getString(R.string.map_error_getlocation), name)).show();
            }
        };
    }

    private void initMap() {

        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(6);
        mapView.getController().setCenter(LocationUtils.getFranceGeoPoint());

        mapView.setOnChangeListener(new EnhancedMapView.OnChangeListener() {

            @Override
            public void onChange(EnhancedMapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom) {

                Log.d(getClass().getName(), "Zoom level : " + newZoom);

                // zoom is too large
                if (newZoom <= 6) {
                    positionItemizedOverlay.getOverlayItems().clear();
                    mapView.postInvalidate();
                }

                // if map was pined or zoomed out
                // don't show any center if zoom isn't enought
                else if (newZoom > 6 && (!newCenter.equals(oldCenter) || newZoom < oldZoom)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // bikes location
                        }
                    });
                }
            }
        });

        positionItemizedOverlay = new ItemizedOverlay(getResources().getDrawable(R.drawable.ic_maps_indicator_current_position));
        stationOverlay = new ItemizedOverlay(getResources().getDrawable(R.drawable.ic_maps_pin));

        mapView.getOverlays().add(positionItemizedOverlay);
        mapView.getOverlays().add(stationOverlay);
    }

    private void locatePositionOnMap(GeoPoint geoPoint) {

        positionItemizedOverlay.getOverlayItems().clear();
        positionItemizedOverlay.addOverlay(new OverlayItem(geoPoint, null, null));

//        mapView.getController().setZoom(13);
        mapView.getController().animateTo(geoPoint);
    }

    private void startCurrentLocation() {
        setSupportProgressBarIndeterminateVisibility(true);
        startService(new Intent(this, GetCurrentLocationService.class));
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}

// test
//                GeoPoint geoPoint = LocationUtils.getParisGeoPoint();
//
//                Point testPoint = mapView.getProjection().toPixels(geoPoint, null);
//
//                int i = 0, x = 25, y = 25;
//                stationOverlay.addOverlay(new OverlayItem(mapView.getProjection().fromPixels(testPoint.x + i, testPoint.y + i), null, null));
//                stationOverlay.addOverlay(new OverlayItem(mapView.getProjection().fromPixels(testPoint.x + i - x, testPoint.y + i), null, null));
//
//                i = 100;
//                stationOverlay.addOverlay(new OverlayItem(mapView.getProjection().fromPixels(testPoint.x + i, testPoint.y + i), null, null));
//                stationOverlay.addOverlay(new OverlayItem(mapView.getProjection().fromPixels(testPoint.x + i - x, testPoint.y + i - y), null, null));
//
//                i = 200;
//                stationOverlay.addOverlay(new OverlayItem(mapView.getProjection().fromPixels(testPoint.x + i, testPoint.y + i), null, null));
//                stationOverlay.addOverlay(new OverlayItem(mapView.getProjection().fromPixels(testPoint.x + i + x, testPoint.y + i - y), null, null));
//
//                i = 300;
//                stationOverlay.addOverlay(new OverlayItem(mapView.getProjection().fromPixels(testPoint.x + i, testPoint.y + i), null, null));
//                stationOverlay.addOverlay(new OverlayItem(mapView.getProjection().fromPixels(testPoint.x + i - x, testPoint.y + i + y), null, null));


//                while (stationsRes.size() != 0) {
//
//                    Station station = stationsRes.get(stationsRes.size() - 1);
//
//                    long time2 = System.currentTimeMillis();
//                    mapView.getProjection().toPixels(LocationUtils.getGeoPoint(station.getCoordLat(), station.getCoordLong()), point);
//                    Log.d(getClass().getName(), "Coord to pixel : " + (System.currentTimeMillis() - time2) + " ms");
//
//                    for(Station target : stationsRes) {
//
//
//                    }
//
//                    long time2 = System.currentTimeMillis();
//                    mapView.getProjection().toPixels(LocationUtils.getGeoPoint(station.getCoordLat(), station.getCoordLong()), point);
//                    Log.d(getClass().getName(), "Coord to pixel : " + (System.currentTimeMillis() - time2) + " ms");
//
//                    stationAreaElement = new StationAreaElement(station.getCoordLat(), station.getCoordLong(), point.x, point.y);
//
//
//                }

//                Collections.sort(stations, new Comparator<Station>() {
//
//                    @Override
//                    public int compare(Station station, Station station1) {
//
//                        if(station.getCoordLat() > station1.getCoordLat())
//                        return 0;
//                    }
//                });

//                KdTree.WeightedSqrEuclid<Station> manhattan = new KdTree.WeightedSqrEuclid<Station>(2, 2000);
//                tree.addPoint(new double[]{30, 40}, null);
//                tree.nearestNeighbor(new double[]{30, 40}, 10, true);


//                for (Station station : stations) {
//
////                    long time2 = System.currentTimeMillis();
//                    mapView.getProjection().toPixels(LocationUtils.getGeoPoint(station.getCoordLat(), station.getCoordLong()), point);
////                    Log.d(getClass().getName(), "Coord to pixel : " + (System.currentTimeMillis() - time2) + " ms");
//
//                    stationAreaElement = new StationAreaElement(station.getCoordLat(), station.getCoordLong(), point.x, point.y);
//
//                    StationPointAreasLoop:
//                    for (StationArea stationPointArea : stationAreas) {
//
//                        count++;
//
//                        if (stationPointArea.isInArea(stationAreaElement)) {
//                            stationPointArea.addElement(stationAreaElement);
//                            stationAreaElement = null;
//                            break StationPointAreasLoop;
//                        }
//                    }
//
//                    if (null != stationAreaElement) {
////                        Log.d(getClass().getName(), "Add new zone : " + currentPoint.x + ";" + currentPoint.y);
//                        stationAreas.add(new StationArea(stationAreaElement));
//                    }
//                }
//
//                Log.d(getClass().getName(), "Station point areas size " + stationAreas.size() + ", done in " + (System.currentTimeMillis() - time) + " ms. Count = " + count);
//                count = 0;


//
//                stations.clear();
//                stations.addAll(stationsRes);
//
//                stationOverlay.getOverlayItems().clear();
//
//                for (StationArea stationArea : stationAreas) {
//                    stationOverlay.addOverlay(new OverlayItem(stationArea.getAverageGeoPoint(mapView.getProjection()), null, null));
//                }


//                KdTree.SqrEuclid<StationPoint> tree = new KdTree.SqrEuclid<StationPoint>(2, 2000);
//
//                long time2 = System.currentTimeMillis();
//
//                List<StationPoint> stationPoints = new ArrayList<StationPoint>();
//                StationPoint stationPoint;
//
//                for (Iterator<Station> it = stations.iterator(); it.hasNext(); ) {
//
//                    Station station = it.next();
//
//                    stationPoint = new StationPoint(station.getCoordLat(), station.getCoordLong());
//                    mapView.getProjection().toPixels(stationPoint, point);
//                    stationPoint.setPoint(point);
//
//                    double[] location = new double[]{point.x, point.y};
//
//                    tree.addPoint(location, stationPoint);
//                    stationPoints.add(stationPoint);
//                }
//
//                Log.d(getClass().getName(), "Tree filled in " + (System.currentTimeMillis() - time2) + " ms");
//
//
//                List<GeoPoint> clusterGeoPoints = new ArrayList<GeoPoint>();
//
//                long time3 = System.currentTimeMillis();
//
//                while (!stationPoints.isEmpty()) {
//
//                    List<KdTree.Entry<StationPoint>> entries = tree.nearestNeighbor(new double[]{stationPoints.get(0).getX(), stationPoints.get(0).getY()}, stationPoints.size(), true);
//
////                    long time = System.currentTimeMillis();
//                    Collections.reverse(entries);
////                    Log.d(getClass().getName(), "Entries = " + entries.size() + "reversed in " + (System.currentTimeMillis() - time) + " ms ");
//
//                    int lat = 0, lon = 0;
//                    for (KdTree.Entry<StationPoint> entry : entries) {
//
//                        if (entry.distance >= 40) {
//                            break;
//                        }
//
////                        Log.d(getClass().getName(), "Distance in kdtree : " + entry.distance);
//
//                        lat += entry.value.getLatitudeE6();
//                        lon += entry.value.getLongitudeE6();
//
//                        stationPoints.remove(entry.value);
//                    }
//
//                    lat /= entries.size();
//                    lon /= entries.size();
//
//                    clusterGeoPoints.add(new GeoPoint(lat, lon));
//                }
//
//                Log.d(getClass().getName(), "Tree processed in " + (System.currentTimeMillis() - time3) + " ms");
//                Log.d(getClass().getName(), "Clusters = " + clusterGeoPoints.size());

//                stationOverlay.getOverlayItems().clear();

//                for (ClusterPoint clusterPoint : clusterPoints) {
//                    stationOverlay.addOverlay(new OverlayItem(new GeoPoint(), null, null));
//                }


//                for (Iterator<StationPoint> it = stationPoints.iterator(); it.hasNext(); ) {
//
//                    stationPoint = it.next();
//
//                    List<KdTree.Entry<StationPoint>> entries = tree.nearestNeighbor(new double[]{stationPoint.getX(), stationPoint.getY()}, 2000, true);
//
//                    int lat = 0, lon = 0;
//                    for (KdTree.Entry<StationPoint> entry : entries) {
//
//                        Log.d(getClass().getName(), "Distance in kdtree : " + entry.distance);
//
//                        lat += entry.value.getLatitudeE6();
//                        lon += entry.value.getLongitudeE6();
//
//                        stationPoints.remove(entry.value);
//                    }
//
//                    lat /= entries.size();
//                    lon /= entries.size();
//
//                    clusterGeoPoints.add(new GeoPoint(lat, lon));
//                }


//                        if (Math.pow((clusterPoint.getX() - clusteredGeoPoint.getX()), 2) + Math.pow((clusterPoint.getY() - clusteredGeoPoint.getY()), 2) <= 700) {