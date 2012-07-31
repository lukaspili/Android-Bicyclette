package com.siu.bicyclette.task;

import android.os.AsyncTask;
import android.util.Log;
import com.siu.android.bicyclette.Station;
import com.siu.android.bicyclette.StationDao;
import com.siu.bicyclette.activity.FavoritesDialog;
import com.siu.bicyclette.dao.DatabaseHelper;

import java.util.List;
import java.util.Set;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GetFavoritesTask extends AsyncTask<Void, Void, List<Station>> {

    private FavoritesDialog dialog;
    private Set<Long> ids;

    public GetFavoritesTask(FavoritesDialog dialog, Set<Long> ids) {
        this.dialog = dialog;
        this.ids = ids;
    }

    @Override
    protected List<Station> doInBackground(Void... voids) {
        Log.d(getClass().getName(), "GetFavoritesTask");

        return DatabaseHelper.getInstance().getDaoSession().getStationDao().queryBuilder()
                .where(StationDao.Properties.Id.in(ids))
                .list();
    }

    @Override
    protected void onPostExecute(List<Station> stations) {
        if(null == dialog) {
            return;
        }

        dialog.onGetFavoritesTaskFinished(stations);
    }
}
