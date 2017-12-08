package cn.com.pyc.szpbb.sdk;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;

import cn.com.pyc.szpbb.common.SZApplication;
import cn.com.pyc.szpbb.sdk.database.BaseDbManager;
import cn.com.pyc.szpbb.sdk.database.DBHelper;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAO;
import cn.com.pyc.szpbb.util.PathUtil;
import cn.com.pyc.szpbb.util.SZLog;

/**
 * 数据库操作处理
 */
public abstract class SZDBInterface {

    /**
     * 打开创建DB
     */
    public static void openDB() {
        BaseDbManager.Builder().getDBHelper();
    }

    public static SQLiteDatabase getDatabase(boolean isReadOnly) {
        return BaseDbManager.Builder().getSQLiteDatabase(isReadOnly);
    }

    /**
     * 关闭数据库并置为null <br/>
     * <p>
     * DBHelper为单例； <br/>
     * 注销、切换账号时，可能会需要重新创建数据库，需要关闭后再创建<br/>
     */
    public static void closeDB() {
        BaseDbManager.Builder().destoryDBHelper();
        PathUtil.destorySaveFilePath();
    }

    /**
     * 执行sql语句
     *
     * @param sql      sql语句
     * @param bindArgs 只支持byte[], String, Long ,Double。传null则忽略
     */
    public static void execSQL(String sql, Object[] bindArgs) {
        BaseDbManager.Builder().execSql(sql, bindArgs);
    }


    /**
     * 删除数据库db <br/>
     * 数据库名称 userName.sqlite <br/>
     * Delete an existing private SQLiteDatabase associated with this Context's
     * application package.
     *
     * @param dBName 数据库名
     */
    public static void dropDatabase(String dBName) {
        dBName = dBName + DBHelper.DB_LABLE;
        boolean isDrop = BaseDbManager.Builder().deleteDatabase(SZApplication.getInstance(),
                dBName);
        SZLog.v("sz", "dropDb " + dBName + ": " + isDrop);
    }

    /**
     * 创建专辑和文件需要的所有数据表 <br/>
     * 建议放在后台服务中进行。<br/>
     */
    public static boolean createTables() {
        return BaseDbManager.Builder().createDBTable();
    }

    /**
     * 删除所有表中的数据
     */
    public static void deleteTableData() {
        BaseDbManager.Builder().deleteAllTableData();
    }

    /**
     * 删除指定表中数据
     *
     * @param tableName 表名
     */
    public static void deleteTableData(String tableName) {
        BaseDbManager.Builder().deleteTableData(tableName);
    }

    /**
     * 方法：检查某表columnName列是否存在
     *
     * @param tableName  表名
     * @param columnName 列名
     * @return boolean
     */
    public static boolean checkColumnExist(String tableName, String columnName) {
        return BaseDbManager.Builder().checkColumn(tableName, columnName);
    }

    /**
     * 给表添加一个不存在的列 <br/>
     * 如果该表不存在，请先创建表
     *
     * @param tableName  数据表名
     * @param columnName 列名
     */
    public static void addColumn(String tableName, String columnName) {
        BaseDbManager.Builder().addColumn(tableName, columnName);
    }
//
//    /**
//     * 是否存在下载文件的记录
//     *
//     * @param fileId
//     * @return
//     */
//    public static boolean existDownFileRecord(String fileId) {
//        return DownData2DBManager.Builder().findByFileId(fileId) != null;
//    }
//
//
//    /**
//     * 是否存在下载文件夹的记录
//     *
//     * @param folderId
//     * @return
//     */
//    @Deprecated
//    public static boolean existDownFolderRecord(String folderId) {
//        return DownDataDBManager.Builder().findByFolderId(folderId) != null;
//    }
//

    /**
     * 查询所有
     *
     * @param cls   Clazz
     * @param isAsc 是否升序排列数据查询
     * @return List
     */
    public static List<?> findAll(Class<Object> cls, boolean isAsc) {
        return new BaseDAO<Object>().findAll(cls, isAsc ? "ASC" : "DESC");
    }

    /**
     * 保存：单数据实体保存方法
     *
     * @param entity 实体对象Object
     */
    protected static void save(Object entity) {
        new BaseDAO<>().save(entity);
    }

    /*
     * 保存：单数据实体级联保存方法
     *
     * @param entity
     */
    protected static void cascadedSave(Object entity) {
        new BaseDAO<>().cascadedSave(entity);
    }

    /**
     * 更新：单数据实体更新方法
     *
     * @param entity
     */
    protected static void update(Object entity) {
        new BaseDAO<>().update(entity);
    }

    /*
     * 更新：多数据实体更新方法
     *
     * @param entity
     */
//    protected static void cascadedUpdate(Object entity) {
//        new BaseDAO<>().update(entity);
//    }

}
