package com.siu.bicyclette.service;

import android.app.IntentService;
import android.content.Intent;
import com.siu.bicyclette.City;
import com.siu.bicyclette.Station;
import com.siu.bicyclette.dao.DatabaseHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class CityUpdateFromStationsService extends IntentService {

    public CityUpdateFromStationsService() {
        super("CityUpdateFromStationsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        List<Station> stations = DatabaseHelper.getInstance().getDaoSession().getStationDao().loadAll();

        Set<String> cityNames = new HashSet<String>();

        for (Station station : stations) {
            cityNames.add(station.getCity());
        }
    }
}
