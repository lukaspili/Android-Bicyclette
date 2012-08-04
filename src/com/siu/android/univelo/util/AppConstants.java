package com.siu.android.univelo.util;

import android.app.AlarmManager;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class AppConstants {

    /* Location */
    public static final int MAX_DISTANCE = 75;
    public static final long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    /* GCM */
    public static final String GCM_SERVER_URL = "http://192.168.1.10:8080/gcm-demo";
    public static final String GCM_SERVER_URL_PARAM_REGID = "regId";
    public static final String GCM_SENDER_ID = "siu-soon.com:univelo";

    /* Database */
    public static final int DATABASE_UPDATE_INTERVAL_MS = 1000 * 60 * 60 * 24;
    public static final int DATABASE_DEFAULT_TIMESTAMP_MS = 1342944758;
    public static final String DATABASE_CHECK_TIMESTAMP_KEY = "database_check_timestamp";
    public static final String DATABASE_CREATION_TIMESTAMP_KEY = "database_creation_timestamp";
    public static final String DATABASE_EXISTS = "database_exists";

    /* Station update */
    public static final int STATION_UPDATE_HANDLER_INTERVAL_MS = 1000 * 30;

    /* Animations */
    public static final int ANIMATION_TOPBAR_DURATION_MS = 900;
}
