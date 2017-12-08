package cn.com.pyc.szpbb.sdk.database.practice;

import net.sqlcipher.Cursor;

import cn.com.pyc.szpbb.sdk.database.bean.SZAlbum;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPracticeImpl;
import cn.com.pyc.szpbb.util.SZLog;


public class AlbumDAOImpl extends BaseDAOPracticeImpl<SZAlbum> implements AlbumDAO {
    private static String tableName;

    //public boolean cascadedDelete(String id) {
    //    return true;
    //}

    public static AlbumDAOImpl getInstance() {
        if (tableName == null) {
            tableName = SZAlbum.class.getSimpleName();
        }
        return new AlbumDAOImpl();
    }

    private AlbumDAOImpl() {
    }

    /**
     * 查询专辑的id
     *
     * @param myproduct_id
     * @return
     */
    public String findAlbumId(String myproduct_id) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE myproduct_id=?",
                    new String[]{myproduct_id});
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
     * 根据myProId查询专辑
     *
     * @param myproduct_id
     * @return
     */
    public SZAlbum findAlbumByMyproId(String myproduct_id) {
        SZAlbum album = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE myproduct_id=?",
                    new String[]{myproduct_id});
            if (cursor.moveToNext()) {
                album = new SZAlbum();
                album.setId(cursor.getString(cursor.getColumnIndex("_id")));
                album.setCategory(cursor.getString(cursor.getColumnIndex("category")));
                album.setItem_number(cursor.getString(cursor.getColumnIndex("item_number")));
                album.setModify_time(cursor.getString(cursor.getColumnIndex("modify_time")));
                album.setMyproduct_id(cursor.getString(cursor.getColumnIndex("myproduct_id")));
                album.setName(cursor.getString(cursor.getColumnIndex("name")));
                album.setPicture(cursor.getString(cursor.getColumnIndex("picture")));
                album.setProduct_id(cursor.getString(cursor.getColumnIndex("product_id")));
                album.setRight_id(cursor.getString(cursor.getColumnIndex("right_id")));
                album.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                album.setPublish_date(cursor.getString(cursor.getColumnIndex("publish_date")));
                // album.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
                // album.setPicture_ratio(cursor.getString(cursor
                // .getColumnIndex("picture_ratio")));
                // album.setSave_Last_add_time(cursor.getString(cursor
                // .getColumnIndex("save_Last_add_time")));
                // album.setSave_Last_modify_time(cursor.getString(cursor
                // .getColumnIndex("save_Last_modify_time")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return album;
    }

    /**
     * 是否存在album
     *
     * @param myproduct_id
     * @return
     */
    public boolean existAlbum(String myproduct_id) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM " + tableName + " WHERE myproduct_id=?",
                    new String[]{myproduct_id});
            if (cursor.moveToNext()) {
                SZLog.v("exist SZAlbum: " + cursor.getString(0));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * 根据MyProId查询专辑category
     *
     * @param myproduct_id
     * @return
     */
    public String findAlbumCategoryByMyProId(String myproduct_id) {
        String result = "";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT category FROM " + tableName + " WHERE myproduct_id=?",
                    new String[]{myproduct_id});
            if (cursor.moveToNext()) {
                result = cursor.getString(cursor.getColumnIndex("category"));
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
     * 删除对应myProd的Album
     *
     * @param myproduct_id
     */
    public int deleteAlbumByMyProId(String myproduct_id) {
        int result = db.delete(tableName, "myproduct_id=?", new String[]{myproduct_id});
        SZLog.w("delete: " + result);
        return result;
    }

    // 根据专辑的_id删除专辑
    public void deleteAlbumById(String _id) {
        db.execSQL("DELETE FROM " + tableName + " WHERE _id=?", new String[]{_id});
    }
}
