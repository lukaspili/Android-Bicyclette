//package com.siu.bicyclette.service;
//
//import android.app.IntentService;
//import android.content.Intent;
//import android.location.Location;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//import com.siu.bicyclette.City;
//import com.siu.bicyclette.dao.DatabaseHelper;
//
//import java.util.List;
//
///**
// * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
// */
//public class CityGetByLocationService extends IntentService {
//
//    public final static String EXTRA_LAT = "lat";
//    public final static String EXTRA_LONG = "long";
//
//    private final static int DEFAULT_DISTANCE_METER = 1000 * 1000;
//
//    public CityGetByLocationService() {
//        super("CityGetByLocationService");
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//
//        Log.d(getClass().getName(), "Start new CityGetByLocationService");
//
//        long time = System.currentTimeMillis();
//
//        double coordLat = intent.getExtras().getDouble(EXTRA_LAT);
//        double coordLong = intent.getExtras().getDouble(EXTRA_LONG);
//
//        List<City> cities = DatabaseHelper.getInstance().getDaoSession().getCityDao().loadAll();
//
//        if (null == cities || cities.isEmpty()) {
//            Log.w(getClass().getName(), "Cities are empty");
//            return;
//        }
//
//        float[][] res = new float[2][4];
//        res[0][0] = DEFAULT_DISTANCE_METER;
//
//        for (int i = 0; i < cities.size(); i++) {
//
//            City city = cities.get(i);
//            Location.distanceBetween(coordLat, coordLong, city.getCoordLat(), city.getCoordLong(), res[1]);
//
//            if (res[1][0] < res[0][0]) {
//                res[0][0] = res[1][0];
//                res[0][3] = i;
//            }
//        }
//
//        if (res[0][0] == DEFAULT_DISTANCE_METER) {
//            Log.w(getClass().getName(), "Not found any city nearest than the default distance of " + DEFAULT_DISTANCE_METER);
//            return;
//        }
//
//        City city = cities.get((int) res[0][3]);
//
//        Log.d(getClass().getName(), "Nearest city " + city.getName() + ", found in " + (System.currentTimeMillis() - time) + " ms");
//
//        intent = new Intent(getClass().getSimpleName());
//        intent.putExtra(Intent.EXTRA_RETURN_RESULT, city);
//
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//    }
//}
