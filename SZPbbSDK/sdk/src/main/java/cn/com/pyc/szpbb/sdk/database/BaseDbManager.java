package cn.com.pyc.szpbb.sdk.database;

import android.content.Context;
import android.text.TextUtils;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;

import cn.com.pyc.szpbb.common.Constant;
import cn.com.pyc.szpbb.common.SZApplication;
import cn.com.pyc.szpbb.sdk.SZInitInterface;
import cn.com.pyc.szpbb.sdk.database.bean.SZAlbum;
import cn.com.pyc.szpbb.sdk.database.bean.SZAlbumContent;
import cn.com.pyc.szpbb.sdk.database.bean.Asset;
import cn.com.pyc.szpbb.sdk.database.bean.SZBookmark;
import cn.com.pyc.szpbb.sdk.database.bean.Perconattribute;
import cn.com.pyc.szpbb.sdk.database.bean.Perconstraint;
import cn.com.pyc.szpbb.sdk.database.bean.Permission;
import cn.com.pyc.szpbb.sdk.database.bean.Right;
import cn.com.pyc.szpbb.sdk.database.practice.AlbumContentDAOImpl;
import cn.com.pyc.szpbb.sdk.database.practice.AlbumDAOImpl;
import cn.com.pyc.szpbb.sdk.database.practice.AssetDAOImpl;
import cn.com.pyc.szpbb.sdk.database.practice.BookmarkDAOImpl;
import cn.com.pyc.szpbb.sdk.database.practice.PerconattributeDAOImpl;
import cn.com.pyc.szpbb.sdk.database.practice.PerconstraintDAOImpl;
import cn.com.pyc.szpbb.sdk.database.practice.PermissionDAOImpl;
import cn.com.pyc.szpbb.sdk.database.practice.RightDAOImpl;
import cn.com.pyc.szpbb.sdk.db.DownData2DBManager;
import cn.com.pyc.szpbb.sdk.db.DownDataDBManager;
import cn.com.pyc.szpbb.util.SZLog;

public class BaseDbManager {

    private final String TAG = "SZM";

    public static BaseDbManager Builder() {
        return new BaseDbManager();
    }

    private BaseDbManager() {
    }

    public DBHelper getDBHelper() {
        String name = SZInitInterface.getUserName(false);
        if (TextUtils.isEmpty(name))
            throw new IllegalArgumentException(
                    "name is not required empty!");
        return DBHelper.getInstance(SZApplication.getInstance(), name);
    }


    /**
     * 获取SQLiteDatabase
     *
     * @param isReadOnly true: readOnly;otherwise false;
     * @return SQLiteDatabase
     */
    public net.sqlcipher.database.SQLiteDatabase getSQLiteDatabase(boolean isReadOnly) {
        DBHelper helper = getDBHelper();
        return isReadOnly ? helper.getReadableDatabase(Constant.getClipherKey()) : helper
                .getWritableDatabase(Constant.getClipherKey()); // key最好使用加密字符串
    }

    /**
     * 获取SQLiteDatabase
     *
     * @return SQLiteDatabase
     */
    public net.sqlcipher.database.SQLiteDatabase getSQLiteDatabase() {
        return getSQLiteDatabase(false);
    }

    /*
     * sql语句执行，带条件
     *
     * @param sql
     * @param bindArgs 只支持byte[], String, Long ,Double。(为null则执行execSQL(sql))
     */
    public void execSql(String sql, Object[] bindArgs) {
        SQLiteDatabase db = getSQLiteDatabase();
        db.execSQL(sql, bindArgs);
    }

    /**
     * 方法：检查某表列是否存在
     *
     * @param tableName  表名
     * @param columnName 列名
     * @return boolean
     */
    public boolean checkColumn(String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            // 查询一行
            SQLiteDatabase db = getSQLiteDatabase();
            cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
            result = (cursor != null && cursor.getColumnIndex(columnName) != -1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 给表添加一个不存在的列 <br/>
     * 如果该表不存在，需先创建表
     *
     * @param tableName  数据表名
     * @param columnName 列名
     */
    public void addColumn(String tableName, String columnName) {
        // 检查表字段
        boolean columnExist = checkColumn(tableName, columnName);
        if (columnExist) return;

        SQLiteDatabase db = getSQLiteDatabase();
        db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName);
    }

    /**
     * 销毁数据库实例,db和helper
     */
    public void destoryDBHelper() {
        DBHelper.closeDB();
    }

    /**
     * 删除数据库
     *
     * @param context Application Context
     * @param name    Sqlite name.
     * @return boolean
     */
    public boolean deleteDatabase(Context context, String name) {
        return context.deleteDatabase(name);
    }

    /**
     * 创建数据表
     */
    public boolean createDBTable() {
        AlbumDAOImpl.getInstance().create(SZAlbum.class);
        RightDAOImpl.getInstance().create(Right.class);
        AssetDAOImpl.getInstance().create(Asset.class);
        PermissionDAOImpl.getInstance().create(Permission.class);
        PerconstraintDAOImpl.getInstance().create(Perconstraint.class);
        PerconattributeDAOImpl.getInstance().create(Perconattribute.class);
        AlbumContentDAOImpl.getInstance().create(SZAlbumContent.class);
        BookmarkDAOImpl.getInstance().create(SZBookmark.class);

        SZLog.v(TAG, "create dbTae success!");
        return true;
    }

    /**
     * 清除所有表中的数据
     */
    public void deleteAllTableData() {
        deleteTableData(Perconattribute.class.getSimpleName());
        deleteTableData(Perconstraint.class.getSimpleName());
        deleteTableData(Permission.class.getSimpleName());
        deleteTableData(Asset.class.getSimpleName());
        deleteTableData(Right.class.getSimpleName());
        deleteTableData(SZAlbumContent.class.getSimpleName());
        deleteTableData(SZAlbum.class.getSimpleName());
        deleteTableData(SZBookmark.class.getSimpleName());
        DownDataDBManager.Builder().deleteAll();
        DownData2DBManager.Builder().deleteAll();
    }

    /**
     * 删除指定表数据
     *
     * @param tableName 表名
     */
    public void deleteTableData(String tableName) {
        SQLiteDatabase db = getSQLiteDatabase();
        //db.execSQL("DELETE FROM " + tableName);
        int result = db.delete(tableName, null, null);
        SZLog.i("delete: " + tableName + ",result = " + result);
    }

    /*
     * 指定条件删除，eg: db.delete(table, id + "=?", new String[] { id });
     *
     * @param tableName
     * @param whereClause
     * @param whereArgs
     * @return
     */
//    public int deleteTableDataByArgs(String tableName, String whereClause,
//                                     String[] whereArgs) {
//        SQLiteDatabase db = getSQLiteDatabase();
//        return db.delete(tableName, whereClause, whereArgs);
//    }

    /**
     * 删除专辑的相关内容（文件数据，权限） <br/>
     * 更新专辑时操作
     *
     * @param myProId 文件夹folderId
     */
    @Deprecated
    public void deleteAlbumAttachInfos(String myProId) {
        // 获取专辑ID
        String album_Id = AlbumDAOImpl.getInstance().findAlbumId(myProId);
        // 1.删除专辑album
        AlbumDAOImpl.getInstance().deleteAlbumByMyProId(myProId);
        // 2.删除文件albumcontent
        AlbumContentDAOImpl.getInstance().deleteAlbumContentByMyProId(myProId);

        SZLog.v(TAG, "album_id: " + album_Id);
        if (album_Id == null) return;

        String albumContentId = AlbumContentDAOImpl.getInstance().findAlbumContentId(album_Id);
        if (albumContentId != null) {
            // 2.删除AlbumContent,如果存在的话
            AlbumContentDAOImpl.getInstance().deleteAlbumContentById(album_Id);
        }

        String RightContentId = RightDAOImpl.getInstance().findRightId(album_Id);
        if (RightContentId != null) {
            // 3.删除Right
            RightDAOImpl.getInstance().deleteRight(album_Id);
        }
        List<String> assetIdList = AssetDAOImpl.getInstance().findAssetId(album_Id);
        if (assetIdList != null && !assetIdList.isEmpty()) {
            // 4.删除Asset
            AssetDAOImpl.getInstance().deleteAsset(album_Id);

            for (int i = 0; i < assetIdList.size(); i++) {
                String assetId = assetIdList.get(i);
                // 5.删除书签
                BookmarkDAOImpl.getInstance().deleteBookMark(assetId);
                String PermissionId = PermissionDAOImpl.getInstance().findPermissionId(assetId);
                if (PermissionId != null) {
                    // 6.删除Permission
                    PermissionDAOImpl.getInstance().deletePermission(assetId);
                    List<String> perconstraintIdList = PerconstraintDAOImpl.getInstance()
                            .findPerconstraintId(PermissionId);
                    if (perconstraintIdList != null) {
                        // 7.删除Perconstraint
                        PerconstraintDAOImpl.getInstance().deletePerconstraint(PermissionId);
                    }
                }
            }
        }
    }

    /**
     * 删除保存的文件相关内容（权限，数据）
     *
     * @param contentId 文件fileId
     */
    public void deleteAlbumContentAttachInfos(String contentId) {
        SZAlbumContent ac = AlbumContentDAOImpl.getInstance().findAlbumContentByContentId(contentId);
        if (ac == null) return;

        Asset asset = (Asset) AssetDAOImpl.getInstance().findByQuery(new String[]{"_id"},
                new String[]{ac.getAsset_id()}, Asset.class).get(0);
        @SuppressWarnings("unchecked")
        List<Permission> permissions = (List<Permission>) PermissionDAOImpl
                .getInstance().findByQuery(new String[]{"asset_id"},
                        new String[]{ac.getAsset_id()}, Permission.class);
        if (permissions != null) {
            for (Permission permission : permissions) {
                PerconstraintDAOImpl.getInstance().deletePerconstraint(permission.getId());
                PermissionDAOImpl.getInstance().delete(permission);
            }
        }
        AssetDAOImpl.getInstance().delete(asset);
        AlbumContentDAOImpl.getInstance().deleteAlbumContentByContenId(contentId);
    }

    /*
     * 查询数据
     *
     * @param table
     * @param columns       查询列，传null返回所有列
     * @param selection     查询条件，传null返回所有行
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy       Passing null will use the default sort order
     * @return
     */
//    public Cursor queryData(String table, String[] columns,
//                            String selection, String[] selectionArgs, String groupBy,
//                            String having, String orderBy) {
//        if (helper == null)
//            throw new IllegalStateException("the 'helper' must be init");
//
//        return helper.query(table, columns, selection, selectionArgs, groupBy,
//                having, orderBy);
//    }

    /*
     * 查询
     *
     * @param sql           sql语句
     * @param selectionArgs 查询条件，eg: "SELECT * FROM TableName WHERE id=?", new String[] {
     *                      id }
     * @return
     */
//    public Cursor rawQueryData(String sql, String[] selectionArgs) {
//        if (helper == null)
//            throw new IllegalStateException("the 'helper' must be init");
//
//        return helper.rawQuery(sql, selectionArgs);
//    }

    /*
     * 更新数据
     *
     * @param table
     * @param values
     * @param whereClause 更新条件，传null更新所有行
     * @param whereArgs
     * @return
     */
//    public int updateData(String table, ContentValues values,
//                          String whereClause, String[] whereArgs) {
//        if (helper == null)
//            throw new IllegalStateException("the 'helper' must be init");
//
//        if (values == null || values.size() <= 0)
//            throw new IllegalArgumentException(
//                    "the 'values' required not empty and null.");
//
//        return helper.update(table, values, whereClause, whereArgs);
//    }

    /*
     * 插入数据
     *
     * @param table
     * @param values
     * @return
     */
//    public long insertData(String table, ContentValues values) {
//        if (helper == null)
//            throw new IllegalStateException("the 'helper' must be init");
//        if (values == null || values.size() <= 0)
//            throw new IllegalArgumentException(
//                    "the 'values' required not empty and null.");
//        return helper.insert(table, values);
//    }

}
