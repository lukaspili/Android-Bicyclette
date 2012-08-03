package com.siu.android.univelo.task;

import android.os.AsyncTask;
import android.util.Log;
import com.siu.android.bicyclette.Station;
import com.siu.android.bicyclette.StationDao;
import com.siu.android.univelo.activity.FavoritesDialog;
import com.siu.android.univelo.dao.DatabaseHelper;

import java.util.List;
import java.util.Set;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GetStationsByIdsTask extends AsyncTask<Void, Void, List<Station>> {

    private FavoritesDialog dialog;
    private Set<Long> ids;
    private Type type;

    public GetStationsByIdsTask(FavoritesDialog dialog, Set<Long> ids, Type type) {
        this.dialog = dialog;
        this.ids = ids;
        this.type = type;
    }

    @Override
    protected List<Station> doInBackground(Void... voids) {
        Log.d(getClass().getName(), "GetStationsByIdsTask");

        return DatabaseHelper.getInstance().getDaoSession().getStationDao().queryBuilder()
                .where(StationDao.Properties.Id.in(ids))
                .list();
    }

    @Override
    protected void onPostExecute(List<Station> stations) {
        if(null == dialog) {
            return;
        }

        dialog.onGetFavoritesTaskFinished(stations, type);
    }

    public static enum Type {
        FAVORITES, ALERTS
    }
}
