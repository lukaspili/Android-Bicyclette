//package com.siu.android.univelo.service;
//
//import android.location.Geocoder;
//import android.util.Log;
//import com.siu.android.andutils.Application;
//import com.siu.android.univelo.app.task.GeocoderLocationByNameTask;
//
//import java.util.Locale;
//
///**
// * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
// */
//public class GeocoderService {
//
//    private Geocoder geocoder;
//    private GeocoderLocationByNameTask geocoderLocationByNameTask;
//
//    public GeocoderService() {
//        geocoder = new Geocoder(Application.getContext(), Locale.FRANCE);
//    }
//
//    public void startLocationByName(String name, GeocoderLocationByNameTask.Listener geocoderLocationByNameTaskListener) {
//
//        Log.d(getClass().getName(), "Cancel previous geocoder location by name task if running");
//        stopLocationByNameIfRunning();
//
//        Log.d(getClass().getName(), "Run new geocoder location by name task");
//        geocoderLocationByNameTask = new GeocoderLocationByNameTask(geocoder, geocoderLocationByNameTaskListener);
//        geocoderLocationByNameTask.execute(name);
//    }
//
//    public void stopLocationByNameIfRunning() {
//
//        Log.d(getClass().getName(), "Stop location by name task");
//
//        if (null == geocoderLocationByNameTask) {
//            Log.d(getClass().getName(), "There is no location by name task to stop");
//            return;
//        }
//
//        geocoderLocationByNameTask.cancel(true);
//    }
//}
