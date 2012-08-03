package com.siu.android.univelo.task;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpResponse;
import com.siu.android.andutils.Application;
import com.siu.android.andutils.util.HttpUtils;
import com.siu.android.andutils.util.NetworkUtils;
import com.siu.android.univelo.activity.MapActivity;
import com.siu.android.univelo.dao.DatabaseHelper;
import com.siu.android.univelo.util.AppConstants;
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

//        long databaseCreationDefaultTimestamp = Application.getContext().getResources().getInteger(R.integer.application_database_creation_default_timestamp);

        // get database creation timestamp or the default database timestamp
//        long databaseCreationTimestamp = preferences.getLong(AppConstants.DATABASE_CREATION_TIMESTAMP_KEY, AppConstants.DATABASE_DEFAULT_TIMESTAMP_MS);

        // check if db exists
        boolean dbExists = preferences.getBoolean(AppConstants.DATABASE_EXISTS, false);

        Log.d(getClass().getName(), "Database existence : " + dbExists);

        // check database update only every 24 hours
        if (preferences.getLong(AppConstants.DATABASE_CHECK_TIMESTAMP_KEY, 0) + AppConstants.DATABASE_UPDATE_INTERVAL_MS >= System.currentTimeMillis()) {
            Log.d(getClass().getName(), "Database already checked in previous 24 hours, exit");
            return true;
        }

        if (!NetworkUtils.isOnline()) {
            Log.d(getClass().getName(), "No network connection, cannot download from server");

            if (dbExists) {
                Log.d(getClass().getName(), "Local database already exists, exit");
                return true;
            }

            return copyLocal();
        }

        // get new version from server if exists
        long databaseCreationTimestamp = preferences.getLong(AppConstants.DATABASE_CREATION_TIMESTAMP_KEY, AppConstants.DATABASE_DEFAULT_TIMESTAMP_MS);
        HttpResponse response = HttpUtils.request("http://bicyclette.indri.fr/api/checkdb.php?c=paris&v=" + databaseCreationTimestamp, HttpUtils.HttpMethod.GET);

        // no new database version from server
        if (null == response || response.getStatusLine().getStatusCode() == 302) {
            if (null == response) {
                Log.d(getClass().getName(), "Cannot get response from server");
            } else {
                Log.d(getClass().getName(), "There is no new database version");
                preferences.edit()
                        .putLong(AppConstants.DATABASE_CHECK_TIMESTAMP_KEY, System.currentTimeMillis())
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

            if (dbExists) {
                Log.d(getClass().getName(), "Local database already exists, exit");
                return true;
            }

            return copyLocal();
        }

        preferences.edit()
                .putLong(AppConstants.DATABASE_CREATION_TIMESTAMP_KEY, System.currentTimeMillis())
                .putLong(AppConstants.DATABASE_CHECK_TIMESTAMP_KEY, System.currentTimeMillis())
                .putBoolean(AppConstants.DATABASE_EXISTS, true)
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

        preferences.edit().putBoolean(AppConstants.DATABASE_EXISTS, true).commit();

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
