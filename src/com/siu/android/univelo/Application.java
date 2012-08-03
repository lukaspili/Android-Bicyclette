package com.siu.android.univelo;

import android.preference.PreferenceManager;
import android.util.Log;
import com.siu.android.univelo.dao.DatabaseHelper;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class Application extends com.siu.android.andutils.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (Application.getContext().getResources().getBoolean(R.bool.application_debug)) {
            Log.d(getClass().getName(), "Debug mode, remove all");

            Application.getContext().deleteDatabase(DatabaseHelper.DB_NAME);

            PreferenceManager.getDefaultSharedPreferences(Application.getContext()).edit()
                    .remove(Application.getContext().getString(R.string.application_preferences_database_creation_timestamp))
                    .remove(Application.getContext().getString(R.string.application_preferences_database_update_timestamp_ms))
                    .remove(Application.getContext().getString(R.string.application_preferences_favorites))
                    .remove(Application.getContext().getString(R.string.application_preferences_alerts))
                    .commit();
        }
    }
}
