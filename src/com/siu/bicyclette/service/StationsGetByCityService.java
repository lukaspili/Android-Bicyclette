package com.siu.bicyclette.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.siu.bicyclette.City;
import com.siu.bicyclette.CityDao;
import com.siu.bicyclette.Station;
import com.siu.bicyclette.StationDao;
import com.siu.bicyclette.dao.DatabaseHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationsGetByCityService extends IntentService {

    public static final String EXTRA_CITY = "city";

    public StationsGetByCityService() {
        super("StationsGetByLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(getClass().getName(), "Start new StationsGetByLocationService");

        City city = intent.getExtras().getParcelable(EXTRA_CITY);

        ArrayList<Station> stations = (ArrayList) DatabaseHelper.getInstance().getDaoSession().getStationDao()
                .queryBuilder().where(StationDao.Properties.City.eq(city.getName())).list();

        if (null == stations || stations.isEmpty()) {
            Log.d(getClass().getName(), "No station found for city " + city.getName());
            return;
        }

        Log.d(getClass().getName(), "Found " + stations.size() + " stations for city " + city.getName());

        intent = new Intent(getClass().getSimpleName());
        intent.putParcelableArrayListExtra(Intent.EXTRA_RETURN_RESULT, stations);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
