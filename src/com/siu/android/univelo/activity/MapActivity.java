package com.siu.android.univelo.activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.maps.GeoPoint;
import com.siu.android.andgapisutils.map.EnhancedMapView;
import com.siu.android.andgapisutils.util.LocationUtils;
import com.siu.android.bicyclette.Station;
import com.siu.android.univelo.R;
import com.siu.android.univelo.location.LastLocationFinder;
import com.siu.android.univelo.location.LastLocationFinderFactory;
import com.siu.android.univelo.map.CurrentLocationOverlay;
import com.siu.android.univelo.map.RoundedMapView;
import com.siu.android.univelo.map.StationsOverlay;
import com.siu.android.univelo.task.*;
import com.siu.android.univelo.util.AppConstants;
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
    private static final int ZOOM_INITIAL = 16;
    private static final int ZOOM_LOCATION = 18;

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
    private ProgressBar jaugeProgressBar;

    private Handler updateStationHandler;
    private Runnable updateStationRunnable;

    private GetDatabaseTask getDatabaseTask;
    private GetStatusTask getStatusTask;
    private GetStationsTask getStationsTask;
    private boolean databaseReady;

    private GcmRegisterTask gcmRegisterTask;

    private GetCurrentLocationTask getCurrentLocationTask;
    private LastLocationFinder lastLocationFinder;
    private CurrentLocationOverlay currentLocationOverlay;

    private List<Station> stations;
    private StationsOverlay stationsOverlay;

    private Set<Long> favoritesStations;
    private String favoritesPreferences;

    private Set<Long> alertStations;
    private String alertPreferences;

    private SharedPreferences preferences;

    private Station currentStation;

    private StationInfo infoType = StationInfo.AVAILABLE;

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
        addFavoriteButton = (ImageButton) findViewById(R.id.map_top_favorite);

        initMap();
        initLocation();
        initButtons();

        stations = new ArrayList<Station>();

        // register to GCM
        startGcm();

        // check and download database if needed
        startGetDatabaseTask();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startCurrentLocation();
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            stopGetDatabaseTaskIfRunning();
            stopGetStationsTaskIfRunning();

            // update station task and handler
            stopGetStatusTaskIfRunning();
            stopUpdateStationHandler();

            // stop current location service and task
            stopGetCurrentLocationIfRunning();

            // stop gcm
            stopGcmIfRunning();
        }

        super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return new FavoritesDialog(this);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        initFavoritesStationsIfNotDone();
        initAlertStationsIfNotDone();
        ((FavoritesDialog) dialog).start(favoritesStations, alertStations);
    }

    private void initMap() {
        mapView.setClickable(true);
        mapView.getController().setZoom(ZOOM_INITIAL);
        mapView.getController().setCenter(LocationUtils.getParisGeoPoint());

        mapView.setOnChangeListener(new EnhancedMapView.OnChangeListener() {
            @Override
            public void onChange(EnhancedMapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom) {
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
                            startGetStationsTask();
                        }
                    });
                }
            }
        });

        stationsOverlay = new StationsOverlay(this);
        mapView.getOverlays().add(stationsOverlay);

        currentLocationOverlay = new CurrentLocationOverlay();
        mapView.getOverlays().add(currentLocationOverlay);
    }

    private void initLocation() {
        lastLocationFinder = LastLocationFinderFactory.getLastLocationFinder(this);
        lastLocationFinder.setChangedLocationListener(getCurrentLocationListener);
    }

    private void initButtons() {
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCurrentLocation();
            }
        });

        availableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (infoType == StationInfo.AVAILABLE) {
                    return;
                }

                availableButton.setImageResource(R.drawable.avail_pushed);
                freeButton.setImageResource(R.drawable.free);
                infoType = StationInfo.AVAILABLE;

                updateCurrentStationStatus(); // update top bar infos if exists

                stationsOverlay.updateMarkers();

                if (null != currentStation) {
                    stationsOverlay.setStationSelectedAlpha(currentStation);
                }

                mapView.invalidate();
            }
        });

        freeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (infoType == StationInfo.FREE) {
                    return;
                }

                availableButton.setImageResource(R.drawable.avail);
                freeButton.setImageResource(R.drawable.free_pushed);
                infoType = StationInfo.FREE;

                updateCurrentStationStatus(); // update top bar infos if exists

                stationsOverlay.updateMarkers();

                if (null != currentStation) {
                    stationsOverlay.setStationSelectedAlpha(currentStation);
                }

                mapView.invalidate();
            }
        });

        addFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null == currentStation) {
                    return;
                }

                if (isFavoriteStation(currentStation)) {
                    removeFavoriteStation(currentStation);
                    addFavoriteButton.setImageResource(R.drawable.star_disabled);
                } else {
                    addFavoriteStation(currentStation);
                    addFavoriteButton.setImageResource(R.drawable.star);
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

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(1);
            }
        });
    }


    /* #Gcm */
    private void startGcm() {
        // dev mode only
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);

        String registrationId = GCMRegistrar.getRegistrationId(this);
        if (registrationId.equals("")) {
            GCMRegistrar.register(this, AppConstants.GCM_SENDER_ID);
        } else {
            if (GCMRegistrar.isRegisteredOnServer(this)) {
                Log.d(getClass().getName(), "GCM already registered");
            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                startGcmRegisterTask(registrationId);
            }
        }
    }

    private void startGcmRegisterTask(String registrationId) {
        if (null != gcmRegisterTask) {
            Log.d(getClass().getName(), "GCM register task is already running");
            return;
        }

        gcmRegisterTask = new GcmRegisterTask(this, registrationId);
    }


    public void onGcmRegisterTaskFinished(boolean success) {
        if (!success) {
            Log.d(getClass().getName(), "Try GCM register again");
            startGcmRegisterTask(gcmRegisterTask.getRegistrationId());
        }

        gcmRegisterTask = null;
    }

    public void stopGcmIfRunning() {
        if (null != gcmRegisterTask) {
            gcmRegisterTask.cancel(true);
            gcmRegisterTask = null;
        }

        if (GCMRegistrar.isRegistered(this)) {
            GCMRegistrar.onDestroy(this);
        }
    }


    /* #Location */
    private void startCurrentLocation() {
        if (null != getCurrentLocationTask) {
            return;
        }

        getCurrentLocationTask = new GetCurrentLocationTask(this, lastLocationFinder);
        getCurrentLocationTask.execute();

        Toast.makeText(this, "Localisation en cours...", Toast.LENGTH_SHORT).show();
    }

    public void onGetCurrentLocationTaskFinished(Location location) {
        updateCurrentLocation(location);
    }

    private void updateCurrentLocation(Location location) {
        getCurrentLocationTask = null;

        if (null == location) {
            Log.d(getClass().getName(), "Current location is null");
            return;
        }

        currentLocationOverlay.setCurrentLocation(location);

        mapView.getController().animateTo(LocationUtils.getGeoPoint(location));
        mapView.getController().setZoom(ZOOM_LOCATION);
    }

    private void stopGetCurrentLocationIfRunning() {
        lastLocationFinder.cancel();

        if (null != getCurrentLocationTask) {
            getCurrentLocationTask.cancel(true);
            getCurrentLocationTask = null;
        }
    }

    private LocationListener getCurrentLocationListener = new LocationListener() {
        public void onLocationChanged(Location l) {
            updateCurrentLocation(l);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }
    };


    /* #Database task */
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

        startUpdateStationHandler();
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


    /* Get status task */
    public void startUpdateStationHandler() {
        updateStationRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(getClass().getName(), "Update station handler");
                startGetStatusTask();

                updateStationHandler.postDelayed(this, AppConstants.STATION_UPDATE_HANDLER_INTERVAL_MS);
            }
        };

        updateStationHandler = new Handler();
        updateStationHandler.post(updateStationRunnable);
    }

    public void stopUpdateStationHandler() {
        updateStationHandler.removeCallbacks(updateStationRunnable);
    }

    public void startGetStatusTask() {
        if (!databaseReady) {
            Log.d(getClass().getName(), "Database not ready yet, exit");
            return;
        }

        stopGetStatusTaskIfRunning();

        getStatusTask = new GetStatusTask(this);
        getStatusTask.execute();
    }

    public void onGetStatusTaskFinished(boolean result) {
        if (result) {
            startGetStationsTask();
        }
    }

    public void stopGetStatusTaskIfRunning() {
        if (null == getStatusTask) {
            return;
        }

        getStatusTask.cancel(true);
        getStatusTask = null;
    }


    /* Get stations task */
    public void startGetStationsTask() {
        if (!databaseReady) {
            Log.d(getClass().getName(), "Database not ready yet, exit");
            return;
        }

        stopGetStationsTaskIfRunning();

        GeoPoint bottomLeftGeoPoint = mapView.getProjection().fromPixels(0, mapView.getHeight() - 1);
        GeoPoint topRightGeoPoint = mapView.getProjection().fromPixels(mapView.getWidth() - 1, 0);

        getStationsTask = new GetStationsTask(this);
        getStationsTask.execute(topRightGeoPoint.getLatitudeE6() / 1E6, topRightGeoPoint.getLongitudeE6() / 1E6,
                bottomLeftGeoPoint.getLatitudeE6() / 1E6, bottomLeftGeoPoint.getLongitudeE6() / 1E6);
    }

    public void onGetStationsTaskFinished(List<Station> receivedStations) {
        stopGetStationsTaskIfRunning();

        if (null == receivedStations) {
            Log.d(getClass().getName(), "No stations from database");
            return;
        }

        stations.clear();
        stations.addAll(receivedStations);

        stationsOverlay.getOverlayItems().clear();
        stationsOverlay.addStations(stations);
        mapView.invalidate();

        if (null != currentStation) {
            if (stations.contains(currentStation)) {
                updateCurrentStationStatus();
                stationsOverlay.setStationSelectedAlpha(currentStation);
            } else {
                hideCurrentStationIfShown();
                stationsOverlay.setNoStationSelectedAlpha();
            }
        }
    }

    private void stopGetStationsTaskIfRunning() {
        if (null == getStationsTask) {
            return;
        }

        getStationsTask.cancel(true);
        getDatabaseTask = null;
    }


    /* #Current station */
    public void showCurrentStation(Station station) {
        // no other station is currently shown, animate the top bar
        if (null == currentStation) {
            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -topLayout.getHeight(), 0);
            translateAnimation.setDuration(AppConstants.ANIMATION_TOPBAR_DURATION_MS);
            translateAnimation.setFillAfter(true);
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // if first time, make top visible
                    if (topLayout.getVisibility() != View.VISIBLE) {
                        topLayout.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            topLayout.startAnimation(translateAnimation);
        }

        currentStation = station;

        updateCurrentStationStatus();

        topTitle.setText(station.getName());
        addFavoriteButton.setImageResource(isFavoriteStation(currentStation) ? R.drawable.star : R.drawable.star_disabled);
        alertButton.setImageResource(isAlertStation(currentStation) ? R.drawable.alert : R.drawable.alert_disabled);
    }

    public void hideCurrentStationIfShown() {
        if (null == currentStation) {
            return;
        }

        currentStation = null;

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -topLayout.getHeight());
        translateAnimation.setDuration(AppConstants.ANIMATION_TOPBAR_DURATION_MS);
        translateAnimation.setFillAfter(true);
        topLayout.startAnimation(translateAnimation);
    }

    private void updateCurrentStationStatus() {
        if (null == currentStation) {
            return;
        }

        final int left = getStationInfo(currentStation);
        int right = getOppositeStationInfo(currentStation);

        jaugeLeftText.setText(String.valueOf(left));
        jaugeRightText.setText(String.valueOf(right));

        double width = (new Double(jaugeBackgroundRepeat.getWidth()) / new Double(left + right)) * left;
        jaugeRepeat.getLayoutParams().width = (int) width;

//        final double unit = width / new Double(100);
//
//        AsyncTask<Void, Integer, Void> task = new AsyncTask<Void, Integer, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                for (int i = 1; i <= 100; i++) {
//                    publishProgress();
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                return null;
//            }
//
//            @Override
//            protected void onProgressUpdate(Integer... ints) {
//                jaugeRepeat.getLayoutParams().width = (int) (unit * ints[0]);
//            }
//        };
//        task.execute();
    }

    public void showStationFromDialog(Station station) {
        // station is not shown on the map
        if (!stations.contains(station)) {
            mapView.getController().animateTo(LocationUtils.getGeoPoint(station.getLatitude(), station.getLongitude()));
        }

        showCurrentStation(station);
        stationsOverlay.setStationSelectedAlphaAndFocus(station);
        mapView.invalidate();
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


    public int getStationInfo(Station station) {
        return (infoType == StationInfo.AVAILABLE) ? station.getAvailable() : station.getFree();
    }

    public int getOppositeStationInfo(Station station) {
        return (infoType == StationInfo.AVAILABLE) ? station.getFree() : station.getAvailable();
    }


    public static enum StationInfo {
        AVAILABLE, FREE
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public Set<Long> getFavoritesStations() {
        return favoritesStations;
    }
}