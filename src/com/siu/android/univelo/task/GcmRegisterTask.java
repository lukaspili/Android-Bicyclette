package com.siu.android.univelo.task;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import com.siu.android.univelo.activity.MapActivity;
import com.siu.android.univelo.gcm.GcmUtils;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GcmRegisterTask extends AsyncTask<Void, Void, Boolean> {


    private MapActivity activity;
    private String registrationId;

    public GcmRegisterTask(MapActivity activity, String registrationId) {
        this.activity = activity;
        this.registrationId = registrationId;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d(getClass().getName(), "GcmRegisterTask");

        // At this point all attempts to register with the app
        // server failed, so we need to unregister the device
        // from GCM - the app will try to register again when
        // it is restarted. Note that GCM will send an
        // unregistered callback upon completion, but
        // GCMIntentService.onUnregistered() will ignore it.
        if (!GcmUtils.register(activity, registrationId, new Checker())) {
            Log.d(getClass().getName(), "GCM register failed");
            GCMRegistrar.unregister(activity);
            return false;
        }

        Log.d(getClass().getName(), "GCM register done");
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (null == activity) {
            return;
        }

        activity.onGcmRegisterTaskFinished(success);
    }

    public String getRegistrationId() {
        return registrationId;
    }

    private class Checker implements GcmUtils.RegisterChecker {
        @Override
        public boolean shouldContinue() {
            return !isCancelled();
        }
    }
}
