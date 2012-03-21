package com.siu.bicyclette.app.activity;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.siu.bicyclette.model.StationStatus;
import com.siu.bicyclette.service.*;
import com.siu.bicyclette.util.LocationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationsMapActivity extends SherlockMapActivity {

    private EnhancedMapView mapView;

    private LocalBroadcastManager localBroadcastManager;

    private StationStatusService stationStatusService;
    private BroadcastReceiver stationStatusUpdateReceiver;
    private BroadcastReceiver cityGetByLocationReceiver;
    private BroadcastReceiver stationsGetByCityReceiver;

    private LocationService locationService;
    private LocationService.LocationResultListener locationResultListener;

    private GeocoderService geocoderService;
    private GeocoderLocationByNameTask.Listener geocoderLocationByNameTaskListener;

    private ItemizedOverlay positionItemizedOverlay;
    private ItemizedOverlay stationOverlay;

    private City currentCity;
    private List<Station> stations = new ArrayList<Station>();

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
        localBroadcastManager.registerReceiver(cityGetByLocationReceiver, new IntentFilter(CityGetByLocationService.class.getSimpleName()));
        localBroadcastManager.registerReceiver(stationsGetByCityReceiver, new IntentFilter(StationsGetByCityService.class.getSimpleName()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        locationService.startCurrentLocation(locationResultListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationService.stopCurrentLocation();
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

                locationService.startCurrentLocation(locationResultListener);

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

                if (stations.isEmpty()) {
                    Log.d(getClass().getName(), "Stations are empty, there is no status to update");
                    return;
                }
            }
        };

        cityGetByLocationReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                currentCity = intent.getExtras().getParcelable(Intent.EXTRA_RETURN_RESULT);

                intent = new Intent(context, StationsGetByCityService.class);
                intent.putExtra(StationsGetByCityService.EXTRA_CITY, currentCity);

                startService(intent);
            }
        };

        stationsGetByCityReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                List<Station> stationsRes = intent.getParcelableArrayListExtra(Intent.EXTRA_RETURN_RESULT);

                stations.clear();
                stations.addAll(stationsRes);

                stationOverlay.getOverlayItems().clear();

                for (Station station : stations) {
                    stationOverlay.addOverlay(new OverlayItem(LocationUtils.getGeoPoint(station.getCoordLat(), station.getCoordLong()), null, null));
                }

                mapView.invalidate();
            }
        };
    }


    private void initActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    private void initLocation() {

        locationService = new LocationService();
        locationResultListener = new LocationService.LocationResultListener() {

            @Override
            public void onLocationSuccess(Location location) {
                startCityLocation(location);
                locatePositionOnMap(LocationUtils.getGeoPoint(location));
            }

            @Override
            public void onLocationFailure() {
                new AppToast(StationsMapActivity.this, R.string.map_error_getcurrentlocation).show();
            }

            @Override
            public void onLocationStart() {
                setSupportProgressBarIndeterminateVisibility(true);
            }

            @Override
            public void onLocationStop() {
                setSupportProgressBarIndeterminateVisibility(false);
            }
        };

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
                startCityLocation(LocationUtils.getLocation(geoPoint));
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
    }

    private void startCityLocation(Location location) {

        Intent intent = new Intent(this, CityGetByLocationService.class);
        intent.putExtra(CityGetByLocationService.EXTRA_LAT, location.getLatitude());
        intent.putExtra(CityGetByLocationService.EXTRA_LONG, location.getLongitude());

        startService(intent);
    }

    private void locatePositionOnMap(GeoPoint geoPoint) {

        positionItemizedOverlay.getOverlayItems().clear();
        positionItemizedOverlay.addOverlay(new OverlayItem(geoPoint, null, null));

        mapView.getController().setZoom(13);
        mapView.getController().animateTo(geoPoint);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
