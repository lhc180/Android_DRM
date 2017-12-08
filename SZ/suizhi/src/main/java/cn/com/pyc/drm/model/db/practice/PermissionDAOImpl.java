package cn.com.pyc.drm.model.db.practice;

import android.content.ContentValues;

import net.sqlcipher.Cursor;

import cn.com.pyc.drm.model.db.bean.Permission;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.StringUtil;

public class PermissionDAOImpl extends SZBaseDAOPracticeImpl<Permission> implements PermissionDAO {

    private static PermissionDAOImpl daoImpl = null;

    public final String EXPIRED = "1"; //标示 1过期；0未过期; 默认0

    public static PermissionDAOImpl getInstance() {
        if (daoImpl == null) {
            daoImpl = new PermissionDAOImpl();
        }
        return daoImpl;
    }

    /**
     * 更新权限，1：过期（true）
     *
     * @param assetId
     * @return
     */
    public int setExpired(String assetId) {
        if (!StringUtil.isEmptyOrNull(getExpired(assetId))) {
            return 0;
        }
        ContentValues values = new ContentValues();
        values.put("expired", EXPIRED);
        int result = dbHelper.update(Permission.class.getSimpleName(), values,
                "asset_id=?", new String[]{assetId});
        DRMLog.v("setExpired[count]: " + result);
        return result;
    }

    public String getExpired(String assetId) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT expired FROM Permission WHERE asset_id=?",
                    new String[]{assetId});
            if (cursor.moveToNext()) {
                String expired = cursor.getString(cursor.getColumnIndex("expired"));
                DRMLog.v("expired = " + expired);
                return expired;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

}
