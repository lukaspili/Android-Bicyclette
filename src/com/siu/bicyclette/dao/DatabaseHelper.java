package com.siu.bicyclette.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.siu.android.andutils.Application;
import com.siu.bicyclette.DaoMaster;
import com.siu.bicyclette.DaoSession;

import java.io.*;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/" + Application.getContext().getPackageName() + "/databases/";
    private static String DB_NAME = "stations.sqlite";

    private static DatabaseHelper instance;

    private Context context;

    private SQLiteDatabase database;

    private DaoMaster daoMaster;

    private DaoSession daoSession;

    private Boolean databaseExists;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 6);
        this.context = context;
    }

    public boolean createDatabaseIfNotExists() {

        context.deleteDatabase(DB_NAME);

        // check if database exists only once
        if (null == databaseExists) {
            File file = new File(DB_PATH + DB_NAME);
            databaseExists = file.exists();
        }

        if (databaseExists) {
            return true;
        }

        SQLiteDatabase database = getWritableDatabase();
        database.close();

        databaseExists = true;

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

    public static DatabaseHelper getInstance() {

        if (null == instance) {
            instance = new DatabaseHelper(Application.getContext());
        }

        return instance;
    }

    public SQLiteDatabase getDatabase() {

        if (null == database) {
            createDatabaseIfNotExists();
            database = getWritableDatabase();
        }

        return database;
    }

    public DaoMaster getDaoMaster() {

        if (null == daoMaster) {
            daoMaster = new DaoMaster(getDatabase());
        }

        return daoMaster;
    }

    public DaoSession getDaoSession() {

        if (null == daoSession) {
            daoSession = getDaoMaster().newSession();
        }

        return daoSession;
    }

}
