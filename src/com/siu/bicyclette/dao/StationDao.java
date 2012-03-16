package com.siu.bicyclette.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.siu.bicyclette.model.Station;
import com.siu.bicyclette.model.StationStatus;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jonathan
 * Date: 15/03/12
 * Time: 23:08
 * To change this template use File | Settings | File Templates.
 */
public class StationDao {

    private SQLiteDatabase db;
    private DatabaseHelper helper;

    public StationDao(Context context) {
        helper = new DatabaseHelper(context);
    }

    public SQLiteDatabase open() {
        if (db == null || !db.isOpen()) {
            db = helper.getWritableDatabase();
        }
        return db;
    }

    public void close() {
        db.close();
    }

    public void updateStationsStatus(List<StationStatus> stationsStatus) {
        for (StationStatus stationStatus : stationsStatus) {
            ContentValues cv = new ContentValues();
            cv.put("free", stationStatus.getPlaces());
            cv.put("available", stationStatus.getBikes());
            db.update("velibs", cv, "_id=?", new String[]{String.valueOf(stationStatus.getId())});
        }
    }

    public List<Station> getStations() {


    }

}
