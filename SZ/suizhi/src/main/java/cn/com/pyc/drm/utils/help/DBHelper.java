package cn.com.pyc.drm.utils.help;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;

import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.utils.DRMLog;

/**
 * Sqlite工具类
 */
public class DBHelper extends net.sqlcipher.database.SQLiteOpenHelper {

    public static final String DB_LABEL = "v3";
    private static final int DB_VERSION = 4;
    private static final String DB_KEY = Constant.getCipherKey();

    private static volatile net.sqlcipher.database.SQLiteDatabase db;
    private static volatile DBHelper mDBHelper;

    /**
     * 设置SQlite,销毁当前DBHelper。<br/>
     * <p>
     * <br/>
     * 创建数据库之前，必须调用销毁，否则可能创建不成功！<br/>
     * <p>
     * DBHelper = null; db.close。
     */
    public static void setMdbHelperNULL() {
        if (db != null && db.isOpen()) db.close();
        db = null;
        if (mDBHelper != null) mDBHelper.close();
        mDBHelper = null;

        DRMLog.d("mdbHelper is null,and db also set null");
    }

    private DBHelper(Context context, String name) {
        super(context, name, null, DB_VERSION);
    }

    /**
     * 获取DBHelper实例
     *
     * @param context
     * @param name    DataBase Name
     * @return
     */
    public static DBHelper getInstance(Context context, String name) {
        context = context.getApplicationContext();
        if (null == mDBHelper) {
            synchronized (DBHelper.class) {
                if (null == mDBHelper) {
                    // 创建数据库之前调用，sqlClipher加密库。
                    net.sqlcipher.database.SQLiteDatabase.loadLibs(context);
                    String dbName = name + DB_LABEL;// 数据库名 = 名称 + "v3"
                    DRMLog.d("dBase called: " + dbName);
                    mDBHelper = new DBHelper(context, dbName);
                    db = mDBHelper.getWritableDatabase(DB_KEY);
                }
            }
        }
        return mDBHelper;
    }

    @Override
    public void onCreate(net.sqlcipher.database.SQLiteDatabase db) {

        // DBHelper.db = db;
        // operateTable(db, "");
    }

    @Override
    public void onUpgrade(net.sqlcipher.database.SQLiteDatabase db,
                          int oldVersion, int newVersion) {
    }

    // @Deprecated
    // public void operateTable(SQLiteDatabase db, String actionString)
    // {
    // Class<DatabaseColumn>[] columnsClasses = DatabaseColumn.getSubClasses();
    // DatabaseColumn columns = null;
    // for (int i = 0; i < columnsClasses.length; i++)
    // {
    // try
    // {
    // columns = columnsClasses[i].newInstance();
    // if ("".equals(actionString) || actionString == null)
    // {
    // DRMLog.d("create table:" + columns.getTableCreateor());
    // db.execSQL(columns.getTableCreateor());
    // } else
    // {
    // DRMLog.d("drop table:" + columns.getTableName());
    // db.execSQL(actionString + columns.getTableName());
    // }
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // }
    // }

    // ///////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////

    public void DeleteBookmark(String Asset_id) {
        getDB();
        db.execSQL("DELETE FROM Bookmark WHERE content_ids=?",
                new Object[]{Asset_id});
    }

    public void DeleteAlbum(String id) {
        getDB();
        db.execSQL("DELETE FROM Album WHERE _id=?", new Object[]{id});
    }

    public void DeleteAsset(String right_id) {
        getDB();
        db.execSQL("DELETE FROM Asset WHERE right_id=?",
                new Object[]{right_id});
    }

    public void DeletePermission(String _id) {
        getDB();
        db.execSQL("DELETE FROM Permission WHERE _id=?", new Object[]{_id});
    }

    public void DeletePerconstraint(String permission_id) {
        getDB();
        db.execSQL("DELETE FROM Perconstraint WHERE Permission_id=?",
                new Object[]{permission_id});
    }

    public void DeleteAlbumContent(String album_id) {
        getDB();
        db.execSQL("DELETE FROM AlbumContent WHERE album_id=?",
                new Object[]{album_id});
    }

    public void DeleteAlbumContentByCollectionId(String CollectionId) {
        getDB();
        db.execSQL("DELETE FROM AlbumContent WHERE collectionId =?",
                new Object[]{CollectionId});
    }


    public void DeleteRight(String _id) {
        getDB();
        db.execSQL("DELETE FROM Right WHERE _id=?", new Object[]{_id});
    }

    public void DeleteTableData(String table) {
        getDB();
        db.execSQL("DELETE FROM " + table);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * 插入数据
     *
     * @param Table_Name
     * @param values
     * @return
     */
    public long insert(String Table_Name, ContentValues values) {
        getDB();
        return db.insert(Table_Name, null, values);
    }

    /**
     * @param Table_Name
     * @param id
     * @return 影响行数
     */
    public int delete(String Table_Name, int id) {
        getDB();
        return delete(Table_Name, String.valueOf(id));
    }

    public int delete(String Table_Name, String id) {
        getDB();
        return db.delete(Table_Name, BaseColumns._ID + "=?",
                new String[]{id});
    }

    /**
     * @param Table_Name
     * @param values
     * @param WhereClause
     * @param whereArgs
     * @return 影响行数
     */
    public int update(String Table_Name, ContentValues values,
                      String WhereClause, String[] whereArgs) {
        getDB();
        return db.update(Table_Name, values, WhereClause, whereArgs);
    }

    /**
     * @param Table_Name
     * @param columns
     * @param whereStr
     * @param whereArgs
     * @return
     */
    public net.sqlcipher.Cursor query(String Table_Name, String[] columns,
                                      String whereStr, String[] whereArgs) {
        return query(Table_Name, columns, whereStr, whereArgs, null, null, null);
    }

    public net.sqlcipher.Cursor query(String Table_Name, String[] columns,
                                      String whereStr, String[] whereArgs, String groupBy, String
                                              having,
                                      String orderBy) {
        if (db == null) {
            db = getReadableDatabase(DB_KEY);
        }
        return db.query(Table_Name, columns, whereStr, whereArgs, groupBy,
                having, orderBy);
    }

    public net.sqlcipher.Cursor rawQuery(String sql, String[] args) {
        if (db == null) {
            db = getReadableDatabase(DB_KEY);
        }
        return db.rawQuery(sql, args);
    }

    /**
     * Sql语句执行
     *
     * @param sql
     */
    public void ExecSQL(String sql) {
        getDB();
        db.execSQL(sql);
    }

    public void closeDb() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    public net.sqlcipher.database.SQLiteDatabase getDB() {
        if (db == null) {
            db = getWritableDatabase(DB_KEY);
        }
        return db;
    }

    /*
     * 设置界面清空所有表信息
     *
     * @return
     */
    @Deprecated
    public void deleteAll(Context activity) {
        db.execSQL("DELETE FROM albuminfo WHERE _id > ? ", new Object[]{0});
        db.execSQL("DELETE FROM content WHERE _id > ? ", new Object[]{0});// 专辑表中每个资源的内容
        db.execSQL("DELETE FROM right WHERE _id > ? ", new Object[]{0});
        db.execSQL("DELETE FROM asset WHERE _id > ? ", new Object[]{0});
        db.execSQL("DELETE FROM permission WHERE _id > ? ", new Object[]{0});
        db.execSQL("DELETE FROM perconstraint WHERE _id > ? ",
                new Object[]{0});
        activity.getContentResolver().notifyChange(
                Uri.parse("content://offfile"), null);
    }

    /*
     * 删除本地文件删除后的专辑表。
     *
     * @return
     */
    @Deprecated
    public void deleteLocal(ArrayList<String> myProidy) {
        for (String mp : myProidy) {
            db.execSQL("DELETE FROM albuminfo WHERE myproid=? ",
                    new Object[]{mp});
        }
        db.close();
    }

}
