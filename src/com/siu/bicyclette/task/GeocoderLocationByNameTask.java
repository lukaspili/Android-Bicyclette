package com.siu.bicyclette.task;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.siu.bicyclette.helper.LocationHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GeocoderLocationByNameTask extends AsyncTask<String, Void, GeoPoint> {

    private Geocoder geocoder;

    private Listener listener;

    private String name;

    public GeocoderLocationByNameTask(Geocoder geocoder, Listener listener) {
        this.geocoder = geocoder;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
    }

    @Override
    protected GeoPoint doInBackground(String... args) {

        name = args[0];

        if (null == name) {
            Log.w(getClass().getName(), "Location name to search is null");
            return null;
        }

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(name, 10);
        } catch (IOException e) {
            Log.w(getClass().getName(), "Cannot get location addresses from geocoder", e);
            addresses = new ArrayList<Address>();
        }

        if (addresses.isEmpty()) {
            return null;
        }

        Address address = addresses.get(0);

        return LocationHelper.getGeoPoint(address.getLatitude(), address.getLongitude());
    }

    @Override
    protected void onPostExecute(GeoPoint geoPoint) {

        if (null == geoPoint) {
            listener.onFailure(name);
        } else {
            listener.onSuccess(geoPoint);
        }

        listener.onStop();
    }

    public String getName() {
        return name;
    }

    public interface Listener {

        void onStart();

        void onStop();

        void onSuccess(GeoPoint geoPoint);

        void onFailure(String name);
    }
}
