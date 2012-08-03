package com.siu.android.univelo.task;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.siu.android.andutils.util.HttpUtils;
import com.siu.android.andutils.util.NetworkUtils;
import com.siu.android.bicyclette.StationDao;
import com.siu.android.univelo.Application;
import com.siu.android.univelo.R;
import com.siu.android.univelo.activity.MapActivity;
import com.siu.android.univelo.dao.DatabaseHelper;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

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
        long time = System.currentTimeMillis();

        if (!NetworkUtils.isOnline() || isCancelled()) {
            return false;
        }

        String url = Application.getContext().getString(R.string.webservice_status_url, Application.getContext().getString(R.string.city_value));
        HttpResponse response = HttpUtils.request(url, HttpUtils.HttpMethod.GET);

        if (isCancelled()) {
            return false;
        }

        if (null == response) {
            Log.d(getClass().getName(), "Cannot get response for status update");
            return false;
        }

        BufferedReader reader = null;
        SQLiteDatabase database = null;

        try {
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            if (null == reader) {
                Log.w(getClass().getName(), "Response reader is null");
                return false;
            }

            JsonElement element = new JsonParser().parse(reader);
            if (!element.isJsonArray()) {
                Log.w(getClass().getName(), "Response is not json array");
                return false;
            }

            JsonArray globalArray = element.getAsJsonArray();
            JsonArray array;

            ContentValues contentValues;
            String where = StationDao.Properties.Id.columnName + "=?";
            String[] args = new String[1];

            database = DatabaseHelper.getInstance().getDaoMaster().getDatabase();
            database.beginTransaction();

            for (Iterator<JsonElement> it = globalArray.iterator(); it.hasNext(); ) {
                // get element as array
                array = it.next().getAsJsonArray();

                // get the id
                args[0] = array.get(0).getAsString();

                // get free and available infos
                contentValues = new ContentValues();
                contentValues.put(StationDao.Properties.Free.columnName, array.get(1).getAsInt());
                contentValues.put(StationDao.Properties.Available.columnName, array.get(2).getAsInt());

                database.update(StationDao.TABLENAME, contentValues, where, args);
            }

            database.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(getClass().getName(), "Error during downloading and parsing stations status json", e);
            return false;

        } finally {
            IOUtils.closeQuietly(reader);

            if (null != database) {
                database.endTransaction();
            }
        }

        Log.d(getClass().getName(), "Done in " + (System.currentTimeMillis() - time) + " ms");


//        int size = Application.getContext().getResources().getInteger(R.integer.webservice_status_size);
//        byte[] bytes = new byte[size];
//
//        BufferedInputStream bis = null;
//
//        try {
//            bis = new BufferedInputStream(response.getEntity().getContent(), size);
//            bis.read(bytes);
//        } catch (IOException e) {
//            Log.e(getClass().getName(), "Error during reading response stream", e);
//            return false;
//        } finally {
//            IOUtils.closeQuietly(bis);
//        }
//
//        StringBuilder builder = new StringBuilder();
//        for (byte b : bytes) {
//            builder.append(b);
//        }
//        Log.d(getClass().getName(), "Bytes : " + builder.toString());
//
//        SQLiteDatabase database = DatabaseHelper.getInstance().getDaoMaster().getDatabase();
//        database.beginTransaction();
//        try {
//            ContentValues contentValues;
//            String where = StationDao.Properties.Id.columnName + "=?";
//            String[] args = new String[1];
//
//            for (int i = 1; i < bytes.length; i = i + 4) {
//                int id = ByteUtils.unsignedShortToInt(bytes, i);
//                if (id == 0) {
//                    break;
//                }
//
//                args[0] = Integer.toString(id);
//                contentValues = new ContentValues();
//                contentValues.put(StationDao.Properties.Free.columnName, bytes[i + 2]);
//                contentValues.put(StationDao.Properties.Available.columnName, bytes[i + 3]);
//                database.update(StationDao.TABLENAME, contentValues, where, args);
//
////                Log.d(getClass().getName(), "" + id + bytes[i + 2] + bytes[i + 3]);
//            }
//
//            database.setTransactionSuccessful();
//
//        } catch (Exception e) {
//            Log.e(getClass().getName(), "Error getting station status", e);
//            return false;
//        } finally {
//            database.endTransaction();
//        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        activity.onGetStatusTaskFinished(result);
    }
}
