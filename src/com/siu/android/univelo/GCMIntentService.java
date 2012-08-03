package com.siu.android.univelo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.siu.android.univelo.gcm.GcmUtils;
import com.siu.android.univelo.util.AppConstants;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService() {
        super(AppConstants.GCM_SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(getClass().getName(), "Device registered with registration id : " + registrationId);
        GcmUtils.register(context, registrationId, null);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            GcmUtils.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(getClass().getName(), "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
    }

    @Override
    protected void onError(Context context, String s) {
    }


}
