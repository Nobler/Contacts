package com.wdjhzw.contacts.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

/**
 * This is a Assets Database Manager Use it, you can use a assets database file
 * in you application It will copy the database file to
 * "/data/data/[your application package name]/databases" when you first time
 * you use it Then you can get a SQLiteDatabase object by the assets database
 * file
 * 
 * @author RobinTang
 * @time 2012-09-20
 * 
 * 
 *       How to use: 1. Initialize AssetsDatabaseManager 2. Get
 *       AssetsDatabaseManager 3. Get a SQLiteDatabase object through database
 *       file 4. Use this database object
 * 
 *       //this method is only need call one time
 *       AssetsDatabaseManager.initManager(getApplication()); // get a
 *       AssetsDatabaseManager AssetsDatabaseManager mg =
 *       AssetsDatabaseManager.getManager(); // get SQLiteDatabase object,
 *       db1.db is a file in assets folder object SQLiteDatabase db1 =
 *       mg.getDatabase("db1.db"); //every operate by you want db1.*
 * 
 *       Of cause, you can use
 *       AssetsDatabaseManager.getInstance().getDatabase("xx") to get a database
 *       when you need use a database
 */
public class AssetsDatabaseManager {
    private static Context mContext = null;// Context of application
    private static String mDBPath = null;
    private static volatile AssetsDatabaseManager mInstance = null;// Singleton Pattern

    // A mapping from assets database file to SQLiteDatabase object
    private Map<String, SQLiteDatabase> mDBOpened = new HashMap<String, SQLiteDatabase>();

    public static void initManager(Context context) {
        if (mContext == null) {
            mContext = context;
        }
    }

    public static AssetsDatabaseManager getInstance() {
        if (mInstance == null) {
            synchronized (AssetsDatabaseManager.class) {
                if (mInstance == null) {
                    mInstance = new AssetsDatabaseManager();
                }
            }
        }
        return mInstance;
    }

    private AssetsDatabaseManager() {
        mDBPath = mContext.getFilesDir().getPath();
        mDBPath = mDBPath.substring(0, mDBPath.lastIndexOf("/"))
                + "/databases/";
    }

    /**
     * Get a assets database, if this database is opened this method is only
     * return a copy of the opened database
     * 
     * @param dbName
     *            , the assets file which will be opened for a database
     * @return, if success it return a SQLiteDatabase object else return null
     */
    public SQLiteDatabase getDatabase(String dbName) {
        if (mContext == null) {
            return null;
        }

        if (mDBOpened.get(dbName) != null) {
            return (SQLiteDatabase) mDBOpened.get(dbName);
        }

        String dbFullPath = mDBPath + dbName;
        SharedPreferences dbSP = mContext.getSharedPreferences("assets_db",
                Context.MODE_PRIVATE);

        // check whether the database was copied and valid
        if (!dbSP.getBoolean(dbName, false) || !new File(dbFullPath).exists()) {
            File dbDir = new File(mDBPath);

            // 数据库文件夹不存在，且创建失败时返回null
            if (!dbDir.exists() && !dbDir.mkdirs()) {
                return null;
            }

            if (!copyAssetsToFile(dbName, dbFullPath)) {
                return null;
            }

            dbSP.edit().putBoolean(dbName, true).commit();
        }

        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFullPath, null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        if (db != null) {
            mDBOpened.put(dbName, db);
        }

        return db;
    }

    private boolean copyAssetsToFile(String assets, String db) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            AssetManager am = mContext.getAssets();
            inputStream = am.open(assets);
            outputStream = new FileOutputStream(db);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public boolean closeDatabase(String dbName) {
        if (mDBOpened.get(dbName) != null) {
            ((SQLiteDatabase) mDBOpened.get(dbName)).close();
            mDBOpened.remove(dbName);

            return true;
        }
        return false;
    }

    static public void closeAllDatabase() {
        AssetsDatabaseManager mg = mInstance;

        if (mg != null) {
            for (int i = 0; i < mg.mDBOpened.size(); ++i) {
                if (mg.mDBOpened.get(i) != null) {
                    mg.mDBOpened.get(i).close();
                }
            }
            mg.mDBOpened.clear();
        }
    }
}
