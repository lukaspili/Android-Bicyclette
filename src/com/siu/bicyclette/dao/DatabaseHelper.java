package com.siu.bicyclette.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.siu.bicyclette.Application;

import java.io.*;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/" + Application.getAppContext().getPackageName() + "/databases/";
    private static String DB_NAME = "stations.sqlite";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 6);
        this.context = context;
    }

    public boolean isDatabaseExists() {
        File file = new File(DB_PATH + DB_NAME);
        return file.exists();
    }

    public boolean createDatabaseIfNotExists() {

        context.deleteDatabase(DB_NAME);

        if (isDatabaseExists()) {
            return true;
        }

        SQLiteDatabase database = getWritableDatabase();
        database.close();

        return copyDataBase();
    }

    private boolean copyDataBase() {

        Log.d(getClass().getName(), "Copy database");

        InputStream is = null;
        OutputStream os = null;

        try {
            is = context.getAssets().open(DB_NAME);
            os = new FileOutputStream(DB_PATH + DB_NAME);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            os.flush();

        } catch (IOException e) {
            Log.e(getClass().getName(), "Error during database copy", e);
            return false;

        } finally {

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }

            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {

                }
            }
        }

        return true;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
