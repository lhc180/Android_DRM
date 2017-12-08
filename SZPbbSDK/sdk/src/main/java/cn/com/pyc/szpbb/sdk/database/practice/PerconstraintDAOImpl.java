package cn.com.pyc.szpbb.sdk.database.practice;

import net.sqlcipher.Cursor;

import java.util.ArrayList;

import cn.com.pyc.szpbb.sdk.database.bean.Perconstraint;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPracticeImpl;


public class PerconstraintDAOImpl extends BaseDAOPracticeImpl<Perconstraint> implements
        PerconstraintDAO {
    private static String table;

    public static PerconstraintDAOImpl getInstance() {
        if (table == null) {
            table = Perconstraint.class.getSimpleName();
        }
        return new PerconstraintDAOImpl();
    }

    private PerconstraintDAOImpl() {
    }

    /**
     * 查询文件权限信息约束表
     *
     * @param permission_id
     * @return
     */
    public ArrayList<String> findPerconstraintId(String permission_id) {
        String result;
        ArrayList<String> als = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + table + " WHERE permission_id=?",
                    new String[]{permission_id});
            while (cursor.moveToNext()) {
                result = cursor.getString(0);
                als.add(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return als;
    }

    /**
     * 根据permission_id删除Perconstraint
     *
     * @param permission_id
     */
    public void deletePerconstraint(String permission_id) {
        db.execSQL("DELETE FROM " + table + " WHERE permission_id=?", new String[]{permission_id});
    }
}
