package com.siu.bicyclette.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.siu.bicyclette.Application;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GetCurrentLocationService extends IntentService {

    private LocationManager locationManager;

    private LocationListener gpsLocationListener = new LocationListener();
    private LocationListener networkLocationListener = new LocationListener();

    private GetLastLocationHandler lastLocationHandler = new GetLastLocationHandler();

    private Timer getLastLocationTaskTimer;

    public GetCurrentLocationService() {
        super("GetCurrentLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(getClass().getName(), "Start new GetCurrentLocationService");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

        getLastLocationTaskTimer.cancel();

        locationManager.removeUpdates(gpsLocationListener);
        locationManager.removeUpdates(networkLocationListener);
    }

    private void sendResult(Location location) {

        Intent intent = new Intent(getClass().getSimpleName());
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, location);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
            stopCurrentLocation();
            sendResult((Location) message.obj);
        }
    }

    private class GetLastLocationTask extends TimerTask {

        @Override
        public void run() {

            Log.d(getClass().getName(), "Start new GetLastLocationTask");

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

    private class LocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            stopCurrentLocation();
            sendResult(location);
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
