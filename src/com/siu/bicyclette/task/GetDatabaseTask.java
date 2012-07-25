package com.siu.bicyclette.task;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpResponse;
import com.siu.android.andutils.Application;
import com.siu.android.andutils.util.HttpUtils;
import com.siu.android.andutils.util.NetworkUtils;
import com.siu.bicyclette.R;
import com.siu.bicyclette.activity.MapActivity;
import com.siu.bicyclette.dao.DatabaseHelper;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GetDatabaseTask extends AsyncTask<Void, Void, Boolean> {

    private MapActivity activity;
    private SharedPreferences preferences;

    public GetDatabaseTask(MapActivity activity) {
        this.activity = activity;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.d(getClass().getName(), "GetDatabaseTask");

        String databaseCreationTimestampKey = Application.getContext().getString(R.string.application_preferences_database_creation_timestamp);
        String databaseUpdateTimestampKey = Application.getContext().getString(R.string.application_preferences_database_update_timestamp_ms);

        long databaseCreationDefaultTimestamp = Application.getContext().getResources().getInteger(R.integer.application_database_creation_default_timestamp);
        long databaseCreationTimestamp = preferences.getLong(databaseCreationTimestampKey, databaseCreationDefaultTimestamp);

        boolean dbExists = databaseCreationTimestamp != databaseCreationDefaultTimestamp;
        Log.d(getClass().getName(), "Database existence : " + dbExists);

        if (dbExists) {
            long databaseUpdateTimestamp = preferences.getLong(databaseUpdateTimestampKey, System.currentTimeMillis());
            if (databaseUpdateTimestamp >= System.currentTimeMillis() + 1000 * 60 * 60 * 24) {
                Log.d(getClass().getName(), "Database already checked in previous 24 hours, exit");
                return true;
            }
        }

        if (!NetworkUtils.isOnline()) {
            Log.d(getClass().getName(), "No network connection, cannot download from server");

            if (dbExists) {
                Log.d(getClass().getName(), "Local database already exists, exit");
                return true;
            }

            return copyLocal();
        }

        HttpResponse response = HttpUtils.request("http://bicyclette.indri.fr/api/checkdb.php?c=paris&v=" + databaseCreationTimestamp, HttpUtils.HttpMethod.GET);

        // no new database version from server
        if (null == response || response.getStatusLine().getStatusCode() == 302) {
            if (null == response) {
                Log.d(getClass().getName(), "Cannot get response from server");
            } else {
                Log.d(getClass().getName(), "There is no new database version");
                preferences.edit()
                        .putLong(databaseUpdateTimestampKey, System.currentTimeMillis())
                        .commit();
            }

            if (dbExists) {
                Log.d(getClass().getName(), "Local database already exists, exit");
                return true;
            }

            return copyLocal();
        }

        // new database version from server
        Log.d(getClass().getName(), "Copy database from server");

        try {
            copy(response.getEntity().getContent());
        } catch (Exception e) {
            Log.e(getClass().getName(), "Cannot copy database from server", e);

            if (!dbExists) {
                return copyLocal();
            }

            return true;
        }

        preferences.edit()
                .putLong(databaseCreationTimestampKey, System.currentTimeMillis() / 1000)
                .putLong(databaseUpdateTimestampKey, System.currentTimeMillis())
                .commit();

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (null == activity) {
            return;
        }

        activity.onGetDatabaseTaskFinished(result);
    }

    private boolean copyLocal() {
        Log.d(getClass().getName(), "Copy database from local");

        DatabaseHelper.initEmpty();

        try {
            copy(Application.getContext().getAssets().open(DatabaseHelper.DB_NAME));
        } catch (Exception e) {
            Log.e(getClass().getName(), "Cannot copy database from asset", e);
            return false;
        }

        return true;
    }

    private void copy(InputStream is) throws Exception {
        OutputStream os = null;

        try {
            is = Application.getContext().getAssets().open(DatabaseHelper.DB_NAME);
            os = new FileOutputStream(DatabaseHelper.DB_PATH + DatabaseHelper.DB_NAME);
            IOUtils.copy(is, os);
        } finally {
            if (null != is)
                IOUtils.closeQuietly(is);
            if (null != os)
                IOUtils.closeQuietly(os);
        }

        Log.d(getClass().getName(), "Copy database is done successfully");
    }

    public void setActivity(MapActivity activity) {
        this.activity = activity;
    }
}
