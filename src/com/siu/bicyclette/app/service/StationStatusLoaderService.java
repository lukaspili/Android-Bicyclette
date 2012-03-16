package com.siu.bicyclette.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.siu.bicyclette.dao.StationDao;
import com.siu.bicyclette.model.StationStatus;
import com.siu.bicyclette.service.StationStatusService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationStatusLoaderService extends Service {

    private Timer timer;

    private StationStatusService stationsStatusWS = new StationStatusService();


    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        stationsStatusWS = new StationStatusService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Log.d(getClass().getName(), "Start timer");
                List<StationStatus> stationStatus = stationsStatusWS.getStationsStatus();
                StationDao stationDao = new StationDao(StationStatusLoaderService.this);
                stationDao.open();
                stationDao.updateStationsStatus(stationStatus);
                stationDao.close();
            }
        }, 0, 10 * 1000);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
    }

}
