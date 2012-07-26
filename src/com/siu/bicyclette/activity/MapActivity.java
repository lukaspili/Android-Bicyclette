package com.siu.bicyclette.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.maps.GeoPoint;
import com.siu.android.andgapisutils.map.EnhancedMapView;
import com.siu.android.andgapisutils.util.LocationUtils;
import com.siu.android.bicyclette.Station;
import com.siu.bicyclette.R;
import com.siu.bicyclette.map.RoundedMapView;
import com.siu.bicyclette.map.StationsOverlay;
import com.siu.bicyclette.task.GetDatabaseTask;
import com.siu.bicyclette.task.GetStationsTask;
import com.siu.bicyclette.task.GetStatusTask;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class MapActivity extends com.google.android.maps.MapActivity {

    private static final int ZOOM_LIMIT = 15;

    private EnhancedMapView mapView;
    private RelativeLayout topLayout;
    private ImageView jaugeBackgroundRepeat;
    private ImageView jaugeRepeat;
    private TextView topTitle;
    private TextView jaugeLeftText;
    private TextView jaugeRightText;
    private ImageButton availableButton;
    private ImageButton freeButton;
    private ImageButton locateButton;
    private ImageButton favoritesButton;
    private ImageButton alertButton;
    private ImageButton addFavoriteButton;

    private GetDatabaseTask getDatabaseTask;
    private GetStatusTask getStatusTask;
    private GetStationsTask getStationsTask;
    private boolean databaseReady;

    private List<Station> stations;
    private StationsOverlay stationsOverlay;

    private Set<Long> favoritesStations;
    private String favoritesPreferences;

    private Set<Long> alertStations;
    private String alertPreferences;

    private SharedPreferences preferences;

    private Station currentStation;

    private InfoType infoType = InfoType.AVAILABLE;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map_activity);

        mapView = ((RoundedMapView) findViewById(R.id.map)).getMapView();
        topLayout = (RelativeLayout) findViewById(R.id.map_top);
        jaugeBackgroundRepeat = (ImageView) findViewById(R.id.map_top_jauge_background_repeat);
        jaugeRepeat = (ImageView) findViewById(R.id.map_top_jauge_repeat);
        topTitle = (TextView) findViewById(R.id.map_top_title);
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
                            stationsOverlay.clearOverlayItems();
                            stations.clear();
                            hideCurrentStationIfShown();
                        }
                    });

                    return;
                }

                if (!newCenter.equals(oldCenter) || newZoom != oldZoom) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideCurrentStationIfShown();
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

                updateCurrentStationStatus(); // update top bar infos if exists

                mapView.invalidate();
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

                updateCurrentStationStatus(); // update top bar infos if exists

                mapView.invalidate();
            }
        });

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null == currentStation) {
                    return;
                }

                if (isFavoriteStation(currentStation)) {
                    removeFavoriteStation(currentStation);
                    favoritesButton.setImageResource(R.drawable.star_disabled);
                } else {
                    addFavoriteStation(currentStation);
                    favoritesButton.setImageResource(R.drawable.star);
                }
            }
        });

        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null == currentStation) {
                    return;
                }

                if (isAlertStation(currentStation)) {
                    removeAlertStation(currentStation);
                    alertButton.setImageResource(R.drawable.alert_disabled);
                } else {
                    addAlertStation(currentStation);
                    alertButton.setImageResource(R.drawable.alert);
                }
            }
        });
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
        startGetStatusTask();
    }

    private void stopGetDatabaseTaskIfRunning() {
        if (null == getDatabaseTask) {
            return;
        }

        getDatabaseTask.cancel(true);
        getDatabaseTask.setActivity(null);
        getDatabaseTask = null;
    }


    /* Get status task */
    public void startGetStatusTask() {
        getStatusTask = new GetStatusTask(this);
        getStatusTask.execute();
    }

    public void onGetStatusTaskFinished(boolean result) {
        startGetStationsTask();
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


    /* Current station */
    public void showCurrentStation(Station station) {
        currentStation = station;

        topLayout.setVisibility(View.VISIBLE);
        updateCurrentStationStatus();

        topTitle.setText(station.getName());
        favoritesButton.setImageResource(isFavoriteStation(currentStation) ? R.drawable.star : R.drawable.star_disabled);
        alertButton.setImageResource(isAlertStation(currentStation) ? R.drawable.alert : R.drawable.alert_disabled);
    }

    public void hideCurrentStationIfShown() {
        if (null == currentStation) {
            return;
        }

        currentStation = null;
        topLayout.setVisibility(View.GONE);
    }

    private void updateCurrentStationStatus() {
        if (null == currentStation) {
            return;
        }

        int total = currentStation.getAvailable() + currentStation.getFree();

        jaugeLeftText.setText(String.valueOf(getInfoTypeStatus(currentStation)));
        jaugeRightText.setText(String.valueOf(total));
        jaugeRepeat.getLayoutParams().width = (jaugeBackgroundRepeat.getWidth() / total) * getInfoTypeStatus(currentStation);
    }


    /* Favorites stations */
    private void initFavoritesStationsIfNotDone() {
        if (null != favoritesStations) {
            return;
        }

        favoritesStations = new HashSet<Long>();

        favoritesPreferences = getPreferences().getString(getString(R.string.application_preferences_favorites), "");
        if (StringUtils.isNotEmpty(favoritesPreferences)) {
            for (String id : StringUtils.split(favoritesPreferences, ";")) {
                favoritesStations.add(Long.valueOf(id));
            }
        }
    }

    private void addFavoriteStation(Station station) {
        initFavoritesStationsIfNotDone();

        favoritesStations.add(station.getId());
        favoritesPreferences += station.getId() + ";";
        getPreferences().edit().putString(getString(R.string.application_preferences_favorites), favoritesPreferences).commit();
    }

    private void removeFavoriteStation(Station station) {
        initFavoritesStationsIfNotDone();

        favoritesStations.remove(station.getId());

        StringBuilder builder = new StringBuilder();
        for (Long id : favoritesStations) {
            builder.append(id).append(";");
        }

        favoritesPreferences = builder.toString();

        getPreferences().edit().putString(getString(R.string.application_preferences_favorites), favoritesPreferences).commit();
    }

    private boolean isFavoriteStation(Station station) {
        initFavoritesStationsIfNotDone();
        return favoritesStations.contains(station.getId());
    }


    /* Alert stations */
    private void initAlertStationsIfNotDone() {
        if (null != alertStations) {
            return;
        }

        alertStations = new HashSet<Long>();

        alertPreferences = getPreferences().getString(getString(R.string.application_preferences_alerts), "");
        if (StringUtils.isNotEmpty(alertPreferences)) {
            for (String id : StringUtils.split(alertPreferences, ";")) {
                alertStations.add(Long.valueOf(id));
            }
        }
    }

    private void addAlertStation(Station station) {
        initAlertStationsIfNotDone();

        alertStations.add(station.getId());
        alertPreferences += station.getId() + ";";
        getPreferences().edit().putString(getString(R.string.application_preferences_alerts), alertPreferences).commit();
    }

    private void removeAlertStation(Station station) {
        initAlertStationsIfNotDone();

        alertStations.remove(station.getId());

        StringBuilder builder = new StringBuilder();
        for (Long id : alertStations) {
            builder.append(id).append(";");
        }

        alertPreferences = builder.toString();
        getPreferences().edit().putString(getString(R.string.application_preferences_alerts), alertPreferences).commit();
    }

    private boolean isAlertStation(Station station) {
        initAlertStationsIfNotDone();
        return alertStations.contains(station.getId());
    }


    private SharedPreferences getPreferences() {
        if (null == preferences) {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        return preferences;
    }


    public int getInfoTypeStatus(Station station) {
        return (infoType == InfoType.AVAILABLE) ? station.getAvailable() : station.getFree();
    }

    public static enum InfoType {
        AVAILABLE, FREE
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}