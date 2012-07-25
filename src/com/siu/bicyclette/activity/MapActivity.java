package com.siu.bicyclette.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.siu.android.andgapisutils.map.EnhancedMapView;
import com.siu.android.andgapisutils.util.LocationUtils;
import com.siu.android.bicyclette.Station;
import com.siu.bicyclette.Application;
import com.siu.bicyclette.R;
import com.siu.bicyclette.dao.DatabaseHelper;
import com.siu.bicyclette.map.RoundedMapView;
import com.siu.bicyclette.map.StationOverlayItem;
import com.siu.bicyclette.map.StationsOverlay;
import com.siu.bicyclette.task.GetDatabaseTask;
import com.siu.bicyclette.task.GetStationsTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class MapActivity extends com.google.android.maps.MapActivity {

    private static final int ZOOM_LIMIT = 15;

    private EnhancedMapView mapView;
    private RelativeLayout topLayout;
    private ImageView jaugeBackgroundRepeat;
    private ImageView jaugeRepeat;
    private TextView jaugeLeftText;
    private TextView jaugeRightText;
    private ImageButton availableButton;
    private ImageButton freeButton;
    private ImageButton locateButton;
    private ImageButton favoritesButton;
    private ImageButton alertButton;
    private ImageButton addFavoriteButton;

    private GetDatabaseTask getDatabaseTask;
    private GetStationsTask getStationsTask;
    private boolean databaseReady;

    private List<Station> stations;
    private StationsOverlay stationsOverlay;

    private InfoType infoType = InfoType.AVAILABLE;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map_activity);

        mapView = ((RoundedMapView) findViewById(R.id.map)).getMapView();
        topLayout = (RelativeLayout) findViewById(R.id.map_top);
        jaugeBackgroundRepeat = (ImageView) findViewById(R.id.map_top_jauge_background_repeat);
        jaugeRepeat = (ImageView) findViewById(R.id.map_top_jauge_repeat);
        jaugeLeftText = (TextView) findViewById(R.id.map_top_jauge_left_text);
        jaugeRightText = (TextView) findViewById(R.id.map_top_jauge_right_text);
        availableButton = (ImageButton) findViewById(R.id.map_bottom_available_button);
        freeButton = (ImageButton) findViewById(R.id.map_bottom_free_button);
        locateButton = (ImageButton) findViewById(R.id.map_bottom_location_button);
        favoritesButton = (ImageButton) findViewById(R.id.map_bottom_favorites_button);
        alertButton = (ImageButton) findViewById(R.id.map_top_alert);
        favoritesButton = (ImageButton) findViewById(R.id.map_top_favorite);

        initMap();
        initButtons();

        stations = new ArrayList<Station>();

        startGetDatabaseTask();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        initStation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            stopGetDatabaseTaskIfRunning();
            stopGetStationsTaskIfRunning();
        }

    }

    private void initMap() {
        mapView.setClickable(true);
        mapView.getController().setZoom(18);
        mapView.getController().setCenter(LocationUtils.getParisGeoPoint());

        mapView.setOnChangeListener(new EnhancedMapView.OnChangeListener() {
            @Override
            public void onChange(EnhancedMapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom) {
                if (!databaseReady) {
                    Log.d(getClass().getName(), "Map changed but database not ready yet, exit");
                    return;
                }

                if (newZoom <= ZOOM_LIMIT) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stationsOverlay.getOverlayItems().clear();
                            stations.clear();
                            mapView.invalidate();
                            hideStation();
                        }
                    });

                    return;
                }

                if (!newCenter.equals(oldCenter) || newZoom != oldZoom) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideStation();
                            startGetStationsTask();
                        }
                    });
                }
            }
        });

        stationsOverlay = new StationsOverlay(this);
        mapView.getOverlays().add(stationsOverlay);
    }

    private void initButtons() {
        availableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (infoType == InfoType.AVAILABLE) {
                    return;
                }

                availableButton.setImageResource(R.drawable.avail_pushed);
                freeButton.setImageResource(R.drawable.free);
                infoType = InfoType.AVAILABLE;
            }
        });

        freeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (infoType == InfoType.FREE) {
                    return;
                }

                availableButton.setImageResource(R.drawable.avail);
                freeButton.setImageResource(R.drawable.free_pushed);
                infoType = InfoType.FREE;
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favoritesButton.setImageResource(R.drawable.star);
            }
        });

        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertButton.setImageResource(R.drawable.alert);
            }
        });
    }

    private void initStation() {
        Station station = new Station();
        station.setTotal(20);
        station.setAvailable(5);
        station.setFree(15);

        jaugeLeftText.setText(String.valueOf(station.getAvailable()));
        jaugeRightText.setText(String.valueOf(station.getFree()));
        jaugeRepeat.getLayoutParams().width = (jaugeBackgroundRepeat.getWidth() / station.getTotal()) * station.getAvailable();
    }


    /* Database task */
    private void startGetDatabaseTask() {
        getDatabaseTask = new GetDatabaseTask(this);
        getDatabaseTask.execute();
    }

    public void onGetDatabaseTaskFinished(boolean result) {
        if (!result) {
            Log.wtf(getClass().getName(), "Cannot initialize database, application will not start");
            Toast.makeText(this, "Impossible d'initialiser la base de donnée, veuillez relancer l'application ou nous contacter", Toast.LENGTH_LONG).show();
            return;
        }

        databaseReady = true;
        startGetStationsTask();
    }

    private void stopGetDatabaseTaskIfRunning() {
        if (null == getDatabaseTask) {
            return;
        }

        getDatabaseTask.cancel(true);
        getDatabaseTask.setActivity(null);
        getDatabaseTask = null;
    }


    /* Get stations task */
    public void startGetStationsTask() {
        GeoPoint bottomLeftGeoPoint = mapView.getProjection().fromPixels(0, mapView.getHeight() - 1);
        GeoPoint topRightGeoPoint = mapView.getProjection().fromPixels(mapView.getWidth() - 1, 0);

        getStationsTask = new GetStationsTask(this);
        getStationsTask.execute(topRightGeoPoint.getLatitudeE6() / 1E6, topRightGeoPoint.getLongitudeE6() / 1E6,
                bottomLeftGeoPoint.getLatitudeE6() / 1E6, bottomLeftGeoPoint.getLongitudeE6() / 1E6);
    }

    public void onGetStationsTaskFinished(List<Station> receivedStations) {
        if (null == receivedStations) {
            Log.d(getClass().getName(), "No stations from database");
            return;
        }

        Log.d(getClass().getName(), "Stations : " + receivedStations.size());

        stations.clear();
        stations.addAll(receivedStations);

        stationsOverlay.getOverlayItems().clear();
        stationsOverlay.addStations(stations);
        mapView.invalidate();
    }

    private void stopGetStationsTaskIfRunning() {
        if (null == getStationsTask) {
            return;
        }

        getStationsTask.cancel(true);
        getStationsTask.setActivity(null);
        getDatabaseTask = null;
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public void showStation(Station station) {
        topLayout.setVisibility(View.VISIBLE);
    }

    public void hideStation() {
        topLayout.setVisibility(View.GONE);
    }


    private static enum InfoType {
        AVAILABLE, FREE
    }
}