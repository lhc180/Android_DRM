package cn.com.pyc.drm.model.db.practice;

import android.content.ContentValues;

import net.sqlcipher.Cursor;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class AlbumContentDAOImpl extends SZBaseDAOPracticeImpl<AlbumContent> implements
        AlbumContentDAO {

    private static AlbumContentDAOImpl acdImpl = null;

    public static AlbumContentDAOImpl getInstance() {
        if (acdImpl == null) {
            acdImpl = new AlbumContentDAOImpl();
        }
        return acdImpl;
    }

    private AlbumContent getAlbumContentByCursor(Cursor cursor) {
        AlbumContent ac = new AlbumContent();
        // ac.setId(cursor.getString(cursor.getColumnIndex("_id")));
        ac.setMyProId(cursor.getString(cursor.getColumnIndex("myProId")));
        ac.setName(cursor.getString(cursor.getColumnIndex("name"))
                .replaceAll("\"", ""));
        ac.setContent_id(cursor.getString(
                cursor.getColumnIndex("content_id")).replaceAll("\"", ""));
        ac.setAlbum_id(cursor.getString(cursor.getColumnIndex("album_id")));
        ac.setModify_time(cursor.getString(cursor
                .getColumnIndex("modify_time")));
        ac.setAsset_id(cursor.getString(cursor.getColumnIndex("asset_id")));
        ac.setFileType(cursor.getString(cursor.getColumnIndex("fileType")));
        ac.setCollectionId(cursor.getString(cursor.getColumnIndex("collectionId")));
        ac.setCurrentItemId(cursor.getString(cursor.getColumnIndex("currentItemId")));
        ac.setLatestItemId(cursor.getString(cursor.getColumnIndex("latestItemId")));
        ac.setMusicLrcId(cursor.getString(cursor.getColumnIndex("musicLrcId")));
        ac.setContentSize(cursor.getLong(cursor.getColumnIndex("contentSize")));
        return ac;
    }

    /**
     * 通过conetntId查询album_id
     *
     * @param contentId
     * @return
     */
    public String findAlbumIdByContentId(String contentId) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(
                    "SELECT album_id FROM AlbumContent WHERE content_id=? OR content_id=?",
                    new String[]{contentId, "\"" + contentId + "\""});
            if (cursor.moveToNext()) {
                result = cursor.getString(cursor.getColumnIndex("album_id"));
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
     * 根据文件id,查询文件
     *
     * @param content_id (老版本的contentId可能带引号)
     * @return
     */
    public AlbumContent findAlbumContentByContentId(String content_id) {
        AlbumContent ac = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(
                    "SELECT * FROM AlbumContent WHERE content_id=? OR content_id=?",
                    new String[]{content_id, "\"" + content_id + "\""});
            if (cursor.moveToNext()) {
                ac = getAlbumContentByCursor(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ac;
    }


    /**
     * 根据文件的集合id,查询文件
     *
     * @param collectionId
     * @return
     */
    public AlbumContent findAlbumContentByCollectionId(String collectionId) {
        AlbumContent ac = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(
                    "SELECT * FROM AlbumContent WHERE collectionId=?",
                    new String[]{collectionId});
            if (cursor.moveToNext()) {
                ac = getAlbumContentByCursor(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ac;
    }

    /**
     * 根据collectionId判断是否存在文件
     *
     * @param collectionId
     * @return
     */
    public boolean existAlbumContentById(String collectionId) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(
                    "SELECT content_id FROM AlbumContent WHERE collectionId=?",
                    new String[]{collectionId});
            return cursor.moveToNext();
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
     * 根据myProId,查询所有文件
     *
     * @param myProId
     * @return
     */
    public List<AlbumContent> findAlbumContentByMyProId(String myProId) {
        List<AlbumContent> contents = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM AlbumContent WHERE myProId=?",
                    new String[]{myProId});
            while (cursor.moveToNext()) {
                AlbumContent ac = getAlbumContentByCursor(cursor);
                contents.add(ac);
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
     * 获取本地文件的id
     *
     * @param myProId
     * @return
     */
    public List<String> findAlbumContentIdByMyProId(String myProId) {
        List<String> ids = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT content_id FROM AlbumContent WHERE myProId=?",
                    new String[]{myProId});
            while (cursor.moveToNext()) {
                ids.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ids;
    }

    /**
     * 更新collectionId,lrcId 等
     *
     * @return
     */
    public int updateAlbumContentByItemId(String itemId, String collectionId, String lrcId,
                                          String myProId, long contentSize) {
        ContentValues values = new ContentValues();
        values.put("collectionId", collectionId);
        values.put("musicLrcId", lrcId);
        values.put("myProId", myProId);
        values.put("contentSize", contentSize);
        return dbHelper.update(AlbumContent.class.getSimpleName(), values,
                "content_id=?", new String[]{itemId});
    }


    /*
     * 更新albumContent内容
     *
     * @param ac
     * @return
     */
//    public int updateAlbumContent(AlbumContent ac) {
//        String content_id = ac.getContent_id().replaceAll("\"", "");
//        ContentValues values = new ContentValues();
//        values.put("content_id", content_id);
//        values.put("name", ac.getName());
//        values.put("myProId", ac.getMyProId());
//        values.put("fileType", ac.getFileType());
//        return dbHelper.update(AlbumContent.class.getSimpleName(), values,
//                "content_id=? OR content_id=?", new String[]{content_id,
//                        "\"" + content_id + "\""});
//    }

    /*
     * 根据albumId删除AlbumContent
     *
     * @param album_id
     */
//    public void DeleteAlbumContent(String album_id) {
//        dbHelper.DeleteAlbumContent(album_id);
//    }

    // 查询文件权限信息约束表
    @Deprecated
    public String findAlbumContentId(String album_id) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM AlbumContent WHERE album_id=?",
                    new String[]{album_id});
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
     * 根据albumId删除AlbumContent
     *
     * @param CollectionId
     */
    public void deleteAlbumContentByCollectionId(String CollectionId) {
        dbHelper.DeleteAlbumContentByCollectionId(CollectionId);
    }

    /**
     * 根据content_id删除
     *
     * @param content_id
     */
    public void deleteAlbumContentByContentId(String content_id) {
        dbHelper.getDB().execSQL(
                "DELETE FROM AlbumContent WHERE content_id=? OR content_id=?",
                new String[]{content_id, "\"" + content_id + "\""});
    }

    /*
     * 根据文件id,查询是否存在此文件
     *
     * @param itemId (老版本的contentId可能带引号)
     * @return
     */
//    public boolean existAlbumContent(String itemId) {
//        Cursor cursor = null;
//        try {
//            cursor = dbHelper.rawQuery(
//                    "SELECT * FROM AlbumContent WHERE content_id=? OR content_id=?",
//                    new String[]{itemId, "\"" + itemId + "\""});
//            return cursor.moveToNext();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return false;
//    }

}
