package com.siu.bicyclette.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.siu.android.andutils.Application;
import com.siu.android.bicyclette.DaoMaster;
import com.siu.android.bicyclette.DaoSession;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class DatabaseHelper {

    public static String DB_PATH = "/data/data/" + Application.getContext().getPackageName() + "/databases/";
    public static String DB_NAME = "stations.db";

    private static DatabaseHelper instance;

    private SQLiteDatabase database;

    private DaoMaster daoMaster;
    private DaoSession daoSession;

    private Boolean databaseExists;

    private DatabaseHelper() {

    }

//    public boolean createDatabaseIfNotExists() {
//
//        Application.getContext().deleteDatabase(DB_NAME);
//
//        // check if database exists only once
//        if (null == databaseExists) {
//            File file = new File(DB_PATH + DB_NAME);
//            databaseExists = file.exists();
//        }
//
//        if (databaseExists) {
//            return true;
//        }
//
//        SQLiteDatabase database = getWritableDatabase();
//        database.close();
//
//        databaseExists = true;
//
//        return copyDataBase();
//    }

    public static DatabaseHelper getInstance() {
        if (null == instance) {
            instance = new DatabaseHelper();
        }

        return instance;
    }

//    public SQLiteDatabase getDatabase() {
//
//        if (null == database) {
//            createDatabaseIfNotExists();
//            database = getWritableDatabase();
//        }
//
//        return database;
//    }

    public DaoMaster getDaoMaster() {
        if (null == daoMaster) {
            database = new OpenHelper(Application.getContext()).getWritableDatabase();
            daoMaster = new DaoMaster(database);
        }

        return daoMaster;
    }

    public DaoSession getDaoSession() {
        if (null == daoSession) {
            daoSession = getDaoMaster().newSession();
        }

        return daoSession;
    }

    public void close() {
        if (null != database && database.isOpen()) {
            database.close();
        }
    }

    public static void initEmpty() {
        SQLiteDatabase db = new OpenHelper(Application.getContext()).getWritableDatabase();
        db.close();
    }

    private static class OpenHelper extends SQLiteOpenHelper {

        private OpenHelper(Context context) {
            super(context, DB_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        }
    }
}
