package com.siu.bicyclette.task;

import android.os.AsyncTask;
import com.siu.bicyclette.model.StationStatus;

import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GetBikeStationsFromDatabaseTask extends AsyncTask<Void, Void, List<StationStatus>> {

    @Override
    protected List<StationStatus> doInBackground(Void... voids) {
        return null;
    }
}
