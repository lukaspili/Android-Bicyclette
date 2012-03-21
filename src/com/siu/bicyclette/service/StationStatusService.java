package com.siu.bicyclette.service;

import android.app.IntentService;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.siu.bicyclette.StationDao;
import com.siu.bicyclette.dao.DatabaseHelper;
import com.siu.bicyclette.model.StationStatus;

import java.util.ArrayList;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationStatusService {

    private static final int DELAY = 20000;

    private static StationStatusResource stationStatusResource = new StationStatusResource();

    private static LocalBroadcastManager localBroadcastManager;


    private Context context;

    private BroadcastReceiver stationStatusUpdateReceiver;

    private boolean stationStatusUpdateRegistered;

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            Log.d(getClass().getName(), "Run new runnable");
            context.startService(new Intent(context, StationStatusUpdateService.class));
            handler.postDelayed(this, DELAY);
        }
    };

    public StationStatusService(Context context) {
        this.context = context;
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void startStationStatusUpdate(BroadcastReceiver stationStatusUpdateReceiver) {

        if (null != this.stationStatusUpdateReceiver) {
            Log.w(getClass().getName(), "Station status update already started");
            return;
        }

        this.stationStatusUpdateReceiver = stationStatusUpdateReceiver;
        localBroadcastManager.registerReceiver(stationStatusUpdateReceiver, new IntentFilter(StationStatusUpdateService.class.getSimpleName()));

        handler.removeCallbacks(runnable);
        handler.post(runnable);
    }

    public void stopStationsStatusUpdate() {
        handler.removeCallbacks(runnable);
        localBroadcastManager.unregisterReceiver(stationStatusUpdateReceiver);
    }

    public static class StationStatusUpdateService extends IntentService {

        public StationStatusUpdateService() {
            super("StationStatusUpdateService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {

            final ArrayList<StationStatus> stationStatus = stationStatusResource.getStationsStatus();

            SQLiteDatabase database = DatabaseHelper.getInstance().getDatabase();

            long time = System.currentTimeMillis();

            database.beginTransaction();

            try {
                ContentValues contentValues;
                String where = "_id=?";
                String[] args = new String[1];

                for (StationStatus s : stationStatus) {

                    contentValues = new ContentValues();
                    contentValues.put(StationDao.Properties.Available.name, s.getBikes());
                    contentValues.put(StationDao.Properties.Free.name, s.getPlaces());

                    args[0] = Integer.toString(s.getId());

                    database.update(StationDao.TABLENAME, contentValues, where, args);
                }

                database.setTransactionSuccessful();

            } finally {
                database.endTransaction();
            }

            Log.d(getClass().getName(), "Station status update done in " + (System.currentTimeMillis() - time) + " ms");

            intent = new Intent(getClass().getSimpleName());
            intent.putParcelableArrayListExtra(Intent.EXTRA_RETURN_RESULT, stationStatus);

            localBroadcastManager.sendBroadcast(intent);
        }
    }
}
