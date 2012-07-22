package com.siu.bicyclette;

import android.preference.Preference;
import android.preference.PreferenceManager;
import com.siu.bicyclette.dao.DatabaseHelper;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class Application extends com.siu.android.andutils.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (Application.getContext().getResources().getBoolean(R.bool.application_debug)) {
            Application.getContext().deleteDatabase(DatabaseHelper.DB_NAME);
            PreferenceManager.getDefaultSharedPreferences(Application.getContext()).edit()
                    .remove(Application.getContext().getString(R.string.application_preferences_database_creation_timestamp))
                    .remove(Application.getContext().getString(R.string.application_preferences_database_update_timestamp_ms))
                    .commit();
        }
    }
}
