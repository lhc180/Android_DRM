package cn.com.pyc.drm.model.db.practice;

import net.sqlcipher.Cursor;

import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;
import cn.com.pyc.drm.utils.DRMLog;

public class AlbumDAOImpl extends SZBaseDAOPracticeImpl<Album> implements AlbumDAO {
    private static AlbumDAOImpl adi = null;

    public static AlbumDAOImpl getInstance() {
        if (adi == null) {
            adi = new AlbumDAOImpl();
        }
        return adi;
    }

    /**
     * 查询专辑的id
     *
     * @param myProId
     * @return
     */
    public String findAlbumId(String myProId) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM Album WHERE myproduct_id=?",
                    new String[]{myProId});
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

    private Album getAlbum(Cursor cursor) {
        Album album = new Album();
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
        album.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
        album.setPicture_ratio(cursor.getString(cursor.getColumnIndex("picture_ratio")));
        album.setPublishDate(cursor.getString(cursor.getColumnIndex("publishDate")));
        album.setSave_Last_add_time(cursor.getString(cursor.getColumnIndex("save_Last_add_time")));
        album.setSave_Last_modify_time(cursor.getString(cursor.getColumnIndex
                ("save_Last_modify_time")));
        return album;
    }

    /**
     * 根据myProId查询专辑
     *
     * @param myProId
     * @return
     */
    public Album findAlbumByMyProId(String myProId) {
        Album album = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM Album WHERE myproduct_id=?",
                    new String[]{myProId});
            if (cursor.moveToNext()) {
                album = getAlbum(cursor);
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
     * 根据MyProId查询专辑category
     *
     * @param myProId
     * @return
     */
    public String findAlbumCategoryByMyProId(String myProId) {
        String result = "";
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT category FROM Album WHERE myproduct_id=?",
                    new String[]{myProId});
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
     * @param myProId
     */
    public void deleteAlbumByMyProId(String myProId) {
        int result = dbHelper.getDB().delete(Album.class.getSimpleName(),
                "myproduct_id=?", new String[]{myProId});
        DRMLog.w("delete: " + result);
    }

    // 根据专辑_id删除专辑
//    public void deleteAlbum(String _id) {
//        dbHelper.DeleteAlbum(_id);
//    }
}
