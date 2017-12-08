package cn.com.pyc.szpbb.sdk.database.practice;

import net.sqlcipher.Cursor;

import cn.com.pyc.szpbb.sdk.database.bean.Permission;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPracticeImpl;


public class PermissionDAOImpl extends BaseDAOPracticeImpl<Permission> implements PermissionDAO {
    private static String tableName;

    public static PermissionDAOImpl getInstance() {
        if (tableName == null) {
            tableName = Permission.class.getSimpleName();
        }
        return new PermissionDAOImpl();
    }

    private PermissionDAOImpl() {
    }

    // 查询单个文件权限表
    public String findPermissionId(String _id) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT * FROM " + tableName + " WHERE _id=?", new String[]{_id});
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 删除Permission约束
     *
     * @param _id
     */
    public void deletePermission(String _id) {
        db.execSQL("DELETE FROM " + tableName + " WHERE _id=?", new String[]{_id});
    }

}
