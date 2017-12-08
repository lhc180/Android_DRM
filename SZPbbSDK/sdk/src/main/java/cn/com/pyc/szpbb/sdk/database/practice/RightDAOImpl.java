package cn.com.pyc.szpbb.sdk.database.practice;

import net.sqlcipher.Cursor;

import cn.com.pyc.szpbb.sdk.database.bean.Right;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPracticeImpl;


public class RightDAOImpl extends BaseDAOPracticeImpl<Right> implements RightDAO {

    public static RightDAOImpl getInstance() {
        return new RightDAOImpl();
    }

    private RightDAOImpl() {
    }

    // 查询专辑Right的_id
    public String findRightId(String _id) {
        String result = null;
        String table = Right.class.getSimpleName();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + table + " WHERE _id=?", new String[]{_id});
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

    // 删除right
    public void deleteRight(String _id) {
        String table = Right.class.getSimpleName();
        db.execSQL("DELETE FROM " + table + " WHERE _id=?", new String[]{_id});
    }


}
