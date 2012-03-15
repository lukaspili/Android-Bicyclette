package com.siu.bicyclette.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.siu.bicyclette.dao.StationDao;
import com.siu.bicyclette.helper.UrlHelper;
import com.siu.bicyclette.model.StationStatus;
import com.siu.bicyclette.ws.StationStatusWS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class BikeService extends Service {

    private Timer timer;
    private StationStatusWS stationsStatusWS;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        stationsStatusWS = new StationStatusWS();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Log.d(getClass().getName(), "Start timer");
                List<StationStatus> stationStatus = stationsStatusWS.getStationsStatus();
                StationDao stationDao = new StationDao(BikeService.this);
                stationDao.open();
                stationDao.updateStationsStatus(stationStatus);
                stationDao.close();
            }
        }, 0, 3000);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        this.timer.cancel();
    }

}
