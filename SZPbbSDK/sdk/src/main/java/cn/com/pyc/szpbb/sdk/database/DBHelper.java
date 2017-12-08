package cn.com.pyc.szpbb.sdk.database;

import android.app.Activity;
import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;

import cn.com.pyc.szpbb.util.SZLog;

public class DBHelper extends net.sqlcipher.database.SQLiteOpenHelper {
    public static final String DB_LABLE = ".db"; // 应用数据库创建名称别名
    private static final int DB_VERSION = 1; // 数据库版本
    private static volatile DBHelper mDBHelper;

    /**
     * 设置SQlite close,关闭并销毁当前DBOpenHelper<br/>
     */
    static void closeDB() {
        if (mDBHelper != null) {
            mDBHelper.close();
            SZLog.d("SQLite close!");
        }
        mDBHelper = null;
        mDBHelper = null;
    }

    private DBHelper(Context context, String dbName) {
        super(context, dbName, null, DB_VERSION);
    }

    /**
     * 获取DBHelper实例
     *
     * @param context Application Context
     * @param dbName  dbName
     * @return SQLiteOpenHelper实例
     */
    synchronized static DBHelper getInstance(Context context, String dbName) {
        if ((context instanceof Activity)) {
            throw new IllegalArgumentException("the 'context' maybe cause memory leaks. please " +
                    "give ApplicationContext.");
        }
        if (mDBHelper == null) {
            // 创建数据库之前调用，sqlClipher加密库。
            SQLiteDatabase.loadLibs(context.getApplicationContext());
            String databaseName = dbName + DB_LABLE;
            SZLog.d("dBase called: " + databaseName);
            mDBHelper = new DBHelper(context, dbName);
            //db = mDBHelper.getDB();
        }
        return mDBHelper;
    }

    @Override
    public void onCreate(net.sqlcipher.database.SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(net.sqlcipher.database.SQLiteDatabase db, int oldVersion, int
            newVersion) {
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////

//    public void DeleteBookmark(String ContentId) {
//        getDB();
//        db.execSQL("DELETE FROM SZBookmark WHERE content_id=?",
//                new Object[]{ContentId});
//    }
//
//    public void DeleteAlbum(String id) {
//        getDB();
//        db.execSQL("DELETE FROM SZAlbum WHERE _id=?", new Object[]{id});
//    }
//
//    public void DeleteAsset(String right_id) {
//        getDB();
//        db.execSQL("DELETE FROM Asset WHERE right_id=?",
//                new Object[]{right_id});
//    }
//
//    public void DeletePermission(String _id) {
//        getDB();
//        db.execSQL("DELETE FROM Permission WHERE _id=?", new Object[]{_id});
//    }
//
//    public void DeletePerconstraint(String permission_id) {
//        getDB();
//        db.execSQL("DELETE FROM Perconstraint WHERE Permission_id=?",
//                new Object[]{permission_id});
//    }
//
//    public void DeleteAlbumContent(String album_id) {
//        getDB();
//        db.execSQL("DELETE FROM SZAlbumContent WHERE album_id=?",
//                new Object[]{album_id});
//    }
//
//    public void DeleteRight(String _id) {
//        getDB();
//        db.execSQL("DELETE FROM Right WHERE _id=?", new Object[]{_id});
//    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////

//    public long insert(String Table_Name, ContentValues values) {
//        getDB();
//        return db.insert(Table_Name, null, values);
//    }
//
//    public void DeleteTableData(String table) {
//        getDB();
//        db.execSQL("DELETE FROM " + table);
//    }
//
//    public int delete(String Table_Name, String id) {
//        getDB();
//        return db.delete(Table_Name, BaseColumns._ID + "=?",
//                new String[]{id});
//    }
//
//    public int update(String Table_Name, ContentValues values,
//                      String whereClause, String[] whereArgs) {
//        getDB();
//        return db.update(Table_Name, values, whereClause, whereArgs);
//    }
//
//    public net.sqlcipher.Cursor query(String Table_Name, String[] columns, String whereStr,
//                                      String[] whereArgs) {
//        getReadDB();
//        return db.query(Table_Name, columns, whereStr, whereArgs, null, null,
//                null);
//    }
//
//    public net.sqlcipher.Cursor query(String Table_Name, String[] columns, String selection,
//                                      String[] selectionArgs, String groupBy, String having,
//                                      String orderBy) {
//        getReadDB();
//        return db.query(Table_Name, columns, selection, selectionArgs, groupBy,
//                having, orderBy);
//    }
//
//    public net.sqlcipher.Cursor rawQuery(String sql, String[] selectionArgs) {
//        getReadDB();
//        return db.rawQuery(sql, selectionArgs);
//    }

    /*
     * sql语句，带条件
     *
     * @param sql
     * @param bindArgs ： only byte[], String, Long and Double are supported in
     *                 bindArgs，null is ignored.
     */
//    public void execSQL(String sql, Object bindArgs) {
//        getDB();
//        if (bindArgs == null)
//            db.execSQL(sql);
//        else
//            db.execSQL(sql, new Object[]{bindArgs});
//    }
//
//    public void closeDb() {
//        if (db != null) {
//            db.close();
//            db = null;
//        }
//    }

//    protected net.sqlcipher.database.SQLiteDatabase getReadDB() {
//        if (db == null) {
//            db = mDBHelper.getReadableDatabase(Constant.getClipherKey());
//        }
//        return db;
//    }

    //    protected net.sqlcipher.database.SQLiteDatabase getDB() {
//        if (db == null) { // key最好使用加密字符串
//            db = mDBHelper.getWritableDatabase(Constant.getClipherKey());
//        }
//        return db;
//    }
}
