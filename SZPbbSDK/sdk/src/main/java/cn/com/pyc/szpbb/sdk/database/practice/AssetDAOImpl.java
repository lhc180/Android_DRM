package cn.com.pyc.szpbb.sdk.database.practice;

import net.sqlcipher.Cursor;

import java.util.ArrayList;

import cn.com.pyc.szpbb.sdk.database.bean.Asset;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPracticeImpl;


public class AssetDAOImpl extends BaseDAOPracticeImpl<Asset> implements AssetDAO {

    private static String tableName;

    public static AssetDAOImpl getInstance() {
        if (tableName == null) {
            tableName = Asset.class.getSimpleName();
        }
        return new AssetDAOImpl();
    }

    private AssetDAOImpl() {
    }

    /**
     * 根据right_id查询单个文件权限表
     *
     * @param right_id
     * @return
     */
    public ArrayList<String> findAssetId(String right_id) {
        String result;
        ArrayList<String> als = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE right_id=?", new
                    String[]{right_id});
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
     * 删除asset
     *
     * @param right_id
     */
    public void deleteAsset(String right_id) {
        db.execSQL("DELETE FROM " + tableName + " WHERE right_id=?", new String[]{right_id});
    }

}
