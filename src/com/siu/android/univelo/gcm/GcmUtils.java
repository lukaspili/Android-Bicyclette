package com.siu.android.univelo.gcm;

import android.content.Context;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpResponse;
import com.google.android.gcm.GCMRegistrar;
import com.siu.android.andutils.util.HttpUtils;
import com.siu.android.univelo.R;
import com.siu.android.univelo.util.AppConstants;

import java.util.Random;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GcmUtils {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    public static boolean register(Context context, String registrationId, RegisterChecker registerChecker) {
        Log.d(GcmUtils.class.getName(), "Server registering");

        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(GcmUtils.class.getName(), "Attempt #" + i + " to register");

            if (!HttpUtils.post(AppConstants.GCM_SERVER_URL, AppConstants.GCM_SERVER_URL_PARAM_REGID, registrationId)) {
                Log.d(GcmUtils.class.getName(), "Failed to register on attempt " + i);

                if (i == MAX_ATTEMPTS || (null != registerChecker && !registerChecker.shouldContinue())) {
                    break;
                }

                try {
                    Log.d(GcmUtils.class.getName(), "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(GcmUtils.class.getName(), "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }

                // increase backoff exponentially
                backoff *= 2;
            } else {
                GCMRegistrar.setRegisteredOnServer(context, true);
                return true;
            }
        }

        return false;
    }

    public static void unregister(Context context, String registrationId) {
        Log.d(GcmUtils.class.getName(), "Server unregistering");

        if (!HttpUtils.post(AppConstants.GCM_SERVER_URL, AppConstants.GCM_SERVER_URL_PARAM_REGID, registrationId)) {
            Log.d(GcmUtils.class.getName(), "Server unregistration failed");
            return;
        }

        GCMRegistrar.setRegisteredOnServer(context, false);
        Log.d(GcmUtils.class.getName(), "Server unregistered");
    }

    public static interface RegisterChecker {
        boolean shouldContinue();
    }
}
