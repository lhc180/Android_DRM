package cn.com.pyc.szpbb.sdk.database.practice;

import android.content.ContentValues;

import net.sqlcipher.Cursor;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.szpbb.sdk.database.bean.SZAlbumContent;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPracticeImpl;
import cn.com.pyc.szpbb.util.SZLog;


public class AlbumContentDAOImpl extends BaseDAOPracticeImpl<SZAlbumContent> implements
        AlbumContentDAO {

    private static String tableName;

    public static AlbumContentDAOImpl getInstance() {
        if (tableName == null) {
            tableName = SZAlbumContent.class.getSimpleName();
        }
        return new AlbumContentDAOImpl();
    }

    private AlbumContentDAOImpl() {
    }

    // 查询文件权限信息约束表
    public String findAlbumContentId(String album_id) {
        String result;
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE album_id=?",
                new String[]{album_id});
        if (cursor.moveToNext()) {
            result = cursor.getString(0);
        } else {
            result = null;
        }
        cursor.close();
        return result;
    }

    private SZAlbumContent getAlbumContent(Cursor cursor) {
        SZAlbumContent ac = new SZAlbumContent();
        // ac.setId(cursor.getString(cursor.getColumnIndex("_id")));
        ac.setMypro_id(cursor.getString(cursor.getColumnIndex("mypro_id")));
        ac.setName(cursor.getString(cursor.getColumnIndex("name")));
        ac.setContent_id(cursor.getString(cursor.getColumnIndex("content_id")));
        ac.setAlbum_id(cursor.getString(cursor.getColumnIndex("album_id")));
        ac.setModify_time(cursor.getString(cursor.getColumnIndex("modify_time")));
        ac.setAsset_id(cursor.getString(cursor.getColumnIndex("asset_id")));
        ac.setFile_type(cursor.getString(cursor.getColumnIndex("file_type")));
        ac.setFile_path(cursor.getString(cursor.getColumnIndex("file_path")));
        return ac;
    }

    /**
     * 根据文件id,查询文件
     *
     * @param content_id
     * @return
     */
    public SZAlbumContent findAlbumContentByContentId(String content_id) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE content_id=?", new
                    String[]{content_id});
            if (cursor.moveToNext()) {
                return getAlbumContent(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 根据文件夹的id,查询所有文件
     *
     * @param mypro_id
     * @return
     */
    public List<SZAlbumContent> findAlbumContentByMyProId(String mypro_id) {
        List<SZAlbumContent> contents = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE mypro_id=?", new
                    String[]{mypro_id});
            while (cursor.moveToNext()) {
                contents.add(getAlbumContent(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contents;
    }

    /**
     * 更新AlbumContent内容
     *
     * @param ac
     * @return
     */
    public int updateAlbumContent(SZAlbumContent ac) {
        String content_id = ac.getContent_id();
        ContentValues values = new ContentValues();
        values.put("content_id", content_id);
        values.put("name", ac.getName());
        //values.put("mypro_id", ac.getMypro_id());
        //values.put("file_type", ac.getFile_type());
        values.put("modify_time", ac.getModify_time());
        values.put("file_path", ac.getFile_path());
        return db.update(tableName, values, "content_id=?", new String[]{content_id});
    }

    /**
     * 删除专辑下文件
     *
     * @param mypro_id 即文件夹folderId
     */
    public int deleteAlbumContentByMyProId(String mypro_id) {
        int result = db.delete(tableName, "mypro_id=?", new String[]{mypro_id});
        SZLog.w("delete，result = " + result);
        return result;
    }

    /**
     * 删除对应id的文件
     *
     * @param contentId 即文件fileId
     */
    public int deleteAlbumContentByContenId(String contentId) {
        int result = db.delete(tableName, "content_id=?", new String[]{contentId});
        SZLog.w("delete，result = " + result);
        return result;
    }

    /**
     * 是否存在此id的数据
     *
     * @param content_id
     * @return
     */
    public boolean existAlbumContent(String content_id) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM " + tableName + " WHERE content_id=?",
                    new String[]{content_id});
            if (cursor.moveToNext()) {
                SZLog.v("exist SZAlbumContent: " + cursor.getString(0));
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

    /*
     * 根据albumId删除AlbumContent
     *
     * @param album_id
     */
    public void deleteAlbumContentById(String album_id) {
        db.execSQL("DELETE FROM " + tableName + " WHERE album_id=?", new String[]{album_id});
    }

       /*
     * 通过conetntId查询album_id
     *
     * @param contentId
     * @return
     */
//    public String findAlbumIdByContentId(String contentId) {
//        String result = null;
//        Cursor cursor = db
//                .rawQuery(
//                        "SELECT album_id FROM SZAlbumContent WHERE content_id=? OR content_id=?",
//                        new String[]{contentId, "\"" + contentId + "\""});
//        if (cursor.moveToNext()) {
//            result = cursor.getString(cursor.getColumnIndex("album_id"));
//        }
//        cursor.close();
//        return result;
//    }


}
