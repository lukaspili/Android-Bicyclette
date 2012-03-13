package com.siu.bicyclette.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.siu.bicyclette.R;
import com.siu.bicyclette.helper.LocationHelper;
import com.siu.bicyclette.map.EnhancedMapView;
import com.siu.bicyclette.map.ItemizedOverlay;
import com.siu.bicyclette.service.GeocoderService;
import com.siu.bicyclette.service.LocationService;
import com.siu.bicyclette.task.GeocoderLocationByNameTask;
import com.siu.bicyclette.toast.AppToast;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class BikesMapActivity extends SherlockMapActivity {

    private EnhancedMapView mapView;

    private LocationService locationService;
    private LocationService.LocationResultListener locationResultListener;

    private GeocoderService geocoderService;
    private GeocoderLocationByNameTask.Listener geocoderLocationByNameTaskListener;

    private ItemizedOverlay positionItemizedOverlay;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.bikes_map_activity);

        mapView = (EnhancedMapView) findViewById(R.id.map);

        initActionBar();
        initLocation();
        initMap();

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

    private void initActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    private void initLocation() {

        locationService = new LocationService();
        locationResultListener = new LocationService.LocationResultListener() {

            @Override
            public void onLocationSuccess(Location location) {
                locatePositionOnMap(LocationHelper.getGeoPoint(location));
            }

            @Override
            public void onLocationFailure() {
                new AppToast(BikesMapActivity.this, R.string.map_error_getcurrentlocation).show();
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
            public void onSuccess(GeoPoint geoPoint) {
                // don't hide the progess bar because the position location will show it again
                locatePositionOnMap(geoPoint);
            }

            @Override
            public void onFailure(String name) {
                new AppToast(BikesMapActivity.this, String.format(getString(R.string.map_error_getlocation), name)).show();
                setSupportProgressBarIndeterminateVisibility(false);
            }
        };
    }

    private void initMap() {

        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(6);
        mapView.getController().setCenter(LocationHelper.getFranceGeoPoint());

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

        positionItemizedOverlay = new ItemizedOverlay(getResources().getDrawable(R.drawable.ic_maps_pin));

        mapView.getOverlays().add(positionItemizedOverlay);
    }

    private void locatePositionOnMap(GeoPoint geoPoint) {

        positionItemizedOverlay.getOverlayItems().clear();
        positionItemizedOverlay.addOverlay(new OverlayItem(geoPoint, null, null));

        mapView.getController().setZoom(13);
        mapView.getController().animateTo(geoPoint);

        // bike location
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}