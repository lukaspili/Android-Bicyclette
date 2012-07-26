package com.siu.bicyclette.task;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpResponse;
import com.siu.android.andutils.util.HttpUtils;
import com.siu.android.andutils.util.NetworkUtils;
import com.siu.android.bicyclette.Station;
import com.siu.android.bicyclette.StationDao;
import com.siu.bicyclette.Application;
import com.siu.bicyclette.R;
import com.siu.bicyclette.activity.MapActivity;
import com.siu.bicyclette.dao.DatabaseHelper;
import com.siu.bicyclette.model.StationStatus;
import com.siu.bicyclette.util.ByteUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class GetStatusTask extends AsyncTask<Void, Void, Boolean> {

    private MapActivity activity;

    public GetStatusTask(MapActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.d(getClass().getName(), "GetStatusTask");

        if (!NetworkUtils.isOnline()) {
            return false;
        }

        String url = Application.getContext().getString(R.string.webservice_status_url, Application.getContext().getString(R.string.city_value));
        HttpResponse response = HttpUtils.request(url, HttpUtils.HttpMethod.GET);

        if (null == response) {
            Log.d(getClass().getName(), "Cannot get response for status update");
            return false;
        }

        int size = Application.getContext().getResources().getInteger(R.integer.webservice_status_size);
        byte[] bytes = new byte[size];

        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(response.getEntity().getContent(), size);
            bis.read(bytes);
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error during reading response stream", e);
            return false;
        } finally {
            IOUtils.closeQuietly(bis);
        }

        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(b);
        }
        Log.d(getClass().getName(), "Bytes : " + builder.toString());

        SQLiteDatabase database = DatabaseHelper.getInstance().getDaoMaster().getDatabase();
        database.beginTransaction();
        try {
            ContentValues contentValues;
            String where = StationDao.Properties.Id.columnName + "=?";
            String[] args = new String[1];

            for (int i = 1; i < bytes.length; i = i + 4) {
                int id = ByteUtils.unsignedShortToInt(bytes, i);
                if (id == 0) {
                    break;
                }

                args[0] = Integer.toString(id);
                contentValues = new ContentValues();
                contentValues.put(StationDao.Properties.Free.columnName, bytes[i + 2]);
                contentValues.put(StationDao.Properties.Available.columnName, bytes[i + 3]);
                database.update(StationDao.TABLENAME, contentValues, where, args);

//                Log.d(getClass().getName(), "" + id + bytes[i + 2] + bytes[i + 3]);
            }

            database.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(getClass().getName(), "Error getting station status", e);
            return false;
        } finally {
            database.endTransaction();
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        activity.onGetStatusTaskFinished(result);
    }
}
