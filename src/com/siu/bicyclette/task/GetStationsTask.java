package com.siu.bicyclette.task;

import android.os.AsyncTask;
import android.util.Log;
import com.siu.android.bicyclette.Station;
import com.siu.android.bicyclette.StationDao;
import com.siu.bicyclette.activity.MapActivity;
import com.siu.bicyclette.dao.DatabaseHelper;

import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GetStationsTask extends AsyncTask<Double, Void, List<Station>> {

    private MapActivity activity;

    public GetStationsTask(MapActivity activity) {
        this.activity = activity;
    }

    @Override
    protected List<Station> doInBackground(Double... coords) {
        Log.d(getClass().getName(), "GetStationsTask");

        double neLat = coords[0];
        double neLon = coords[1];
        double swLat = coords[2];
        double swLon = coords[3];

        return DatabaseHelper.getInstance().getDaoSession().getStationDao().queryBuilder()
                .where(StationDao.Properties.Latitude.between(swLat, neLat), StationDao.Properties.Longitude.between(swLon, neLon))
                .list();
    }

    @Override
    protected void onPostExecute(List<Station> stations) {
        if (null == activity) {
            return;
        }

        activity.onGetStationsTaskFinished(stations);
    }

    public void setActivity(MapActivity activity) {
        this.activity = activity;
    }
}
