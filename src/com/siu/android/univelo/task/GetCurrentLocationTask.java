package com.siu.android.univelo.task;

import android.location.Location;
import android.os.AsyncTask;
import com.siu.android.univelo.activity.MapActivity;
import com.siu.android.univelo.location.LastLocationFinder;
import com.siu.android.univelo.util.AppConstants;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GetCurrentLocationTask extends AsyncTask<Void, Void, Location> {

    private MapActivity activity;
    private LastLocationFinder lastLocationFinder;

    public GetCurrentLocationTask(MapActivity activity, LastLocationFinder lastLocationFinder) {
        this.activity = activity;
        this.lastLocationFinder = lastLocationFinder;
    }

    @Override
    protected Location doInBackground(Void... voids) {
        return lastLocationFinder.getLastBestLocation(AppConstants.MAX_DISTANCE, AppConstants.MAX_TIME);
    }

    @Override
    protected void onPostExecute(Location location) {
        if(null == activity) {
            return;
        }

        activity.onGetCurrentLocationTaskFinished(location);
    }
}
