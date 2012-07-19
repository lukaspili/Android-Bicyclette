package com.siu.bicyclette.service;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.siu.android.andutils.Application;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
 */
public class LocationService {

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private LocationManager locationManager;

    private Geocoder geocoder;

    private LocationListener gpsLocationListener = new LocationListener();
    private LocationListener networkLocationListener = new LocationListener();

    private GetLastLocationHandler lastLocationHandler = new GetLastLocationHandler();

    private Timer getLastLocationTaskTimer;

    private LocationResultListener locationResultListener;

    public LocationService() {
        locationManager = (LocationManager) Application.getContext().getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(Application.getContext(), Locale.FRANCE);
    }

    public LocationService(LocationManager locationManager, Geocoder geocoder) {
        this.locationManager = locationManager;
        this.geocoder = geocoder;
    }

    public void startCurrentLocation(LocationResultListener locationResultListener) {

        Log.d(getClass().getName(), "Start current location");

        this.locationResultListener = locationResultListener;

        locationResultListener.onLocationStart();

        if (!isGpsEnabled() && !isNetworkEnabled()) {
            Log.w(getClass().getName(), "Gps and network are disabled, cannot get current location");
            stopCurrentLocation();
            return;
        }

        if (isGpsEnabled()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
        }

        if (isNetworkEnabled()) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
        }

        if (null != getLastLocationTaskTimer) {
            getLastLocationTaskTimer.cancel();
        }

        getLastLocationTaskTimer = new Timer();
        getLastLocationTaskTimer.schedule(new GetLastLocationTask(), 10000);
    }

    public void stopCurrentLocation() {

        Log.d(getClass().getName(), "Stop current location");

        getLastLocationTaskTimer.cancel();
        locationManager.removeUpdates(gpsLocationListener);
        locationManager.removeUpdates(networkLocationListener);

        locationResultListener.onLocationStop();
    }

    private boolean isGpsEnabled() {

        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), "Gps provider is not enabled", e);
        }

        return false;
    }

    private boolean isNetworkEnabled() {

        try {
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), "Network provider is not enabled", e);
        }

        return false;
    }

    private class GetLastLocationHandler extends Handler {

        @Override
        public void handleMessage(Message message) {

            Log.d(getClass().getName(), "Received message from get last location task");

            stopCurrentLocation();

            if (null == message.obj) {
                Log.d(getClass().getName(), "Unkown last location");
                locationResultListener.onLocationFailure();
                return;
            }

            locationResultListener.onLocationSuccess((Location) message.obj);
        }
    }

    private class GetLastLocationTask extends TimerTask {

        @Override
        public void run() {

            Log.d(getClass().getName(), "Start get last location task");

            Location networkLocation = null, gpsLocation = null, bestLocation = null;

            if (isGpsEnabled()) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (isNetworkEnabled()) {
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (gpsLocation != null && networkLocation != null) {

                if (gpsLocation.getTime() > networkLocation.getTime()) {
                    bestLocation = gpsLocation;
                } else {
                    bestLocation = networkLocation;
                }

            } else if (gpsLocation != null) {
                bestLocation = gpsLocation;

            } else if (networkLocation != null) {
                bestLocation = networkLocation;
            }

            Message message = lastLocationHandler.obtainMessage();
            message.obj = bestLocation;
            message.sendToTarget();
        }
    }

    public interface LocationResultListener {

        void onLocationSuccess(Location location);

        void onLocationFailure();

        void onLocationStart();

        void onLocationStop();
    }

    private class LocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            locationResultListener.onLocationSuccess(location);
            stopCurrentLocation();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }
}


//    private boolean isBetterLocation(Location location, Location currentBestLocation) {
//
//        if (currentBestLocation == null) {
//            return true;
//        }
//
//        long timeDelta = location.getTime() - currentBestLocation.getTime();
//        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
//        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
//        boolean isNewer = timeDelta > 0;
//
//        if (isSignificantlyNewer) {
//            return true;
//        } else if (isSignificantlyOlder) {
//            return false;
//        }
//
//        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
//        boolean isLessAccurate = accuracyDelta > 0;
//        boolean isMoreAccurate = accuracyDelta < 0;
//        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
//
//        boolean isFromSameProvider = isSameProvider(location.getProvider(),
//                currentBestLocation.getProvider());
//
//        if (isMoreAccurate) {
//            return true;
//        } else if (isNewer && !isLessAccurate) {
//            return true;
//        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
//            return true;
//        }
//        return false;
//    }
//
//    private boolean isSameProvider(String provider1, String provider2) {
//
//        if (provider1 == null) {
//            return provider2 == null;
//        }
//
//        return provider1.equals(provider2);
//    }