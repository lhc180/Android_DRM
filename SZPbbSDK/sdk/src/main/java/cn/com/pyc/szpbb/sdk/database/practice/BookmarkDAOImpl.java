package cn.com.pyc.szpbb.sdk.database.practice;

import net.sqlcipher.Cursor;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.szpbb.sdk.database.bean.SZBookmark;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPracticeImpl;


public class BookmarkDAOImpl extends BaseDAOPracticeImpl<SZBookmark> implements BookmarkDAO {
    private static String table;

    public static BookmarkDAOImpl getInstance() {
        if (table == null) {
            table = SZBookmark.class.getSimpleName();
        }
        return new BookmarkDAOImpl();
    }

    private BookmarkDAOImpl() {
    }

    /**
     * 删除文件的书签
     *
     * @param content_id
     */
    public void deleteBookMark(String content_id) {
        db.execSQL("DELETE FROM " + table + " WHERE content_id=?", new String[]{content_id});
    }

    /**
     * 删除指定页码的书签
     *
     * @param content_id
     * @param page
     */
    public void deleteBookMarkByPage(String content_id, int page) {
        db.execSQL("DELETE FROM " + table + " WHERE content_id=? AND pagefew=?",
                new String[]{content_id, String.valueOf(page)});
    }

    /**
     * 查询书签
     *
     * @param content_id
     * @param page
     * @return
     */
    public SZBookmark findBookmarkById(String content_id, int page) {
        SZBookmark mark = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + table + " WHERE content_id=? AND pagefew=?",
                    new String[]{content_id, (page + "")});
            if (cursor.moveToNext()) {
                mark = new SZBookmark();
                mark.setId(cursor.getString(cursor.getColumnIndex("_id")));
                mark.setContent_id(cursor.getString(cursor.getColumnIndex("content_id")));
                mark.setTime(cursor.getLong(cursor.getColumnIndex("time")));
                mark.setContent(cursor.getString(cursor.getColumnIndex("content")));
                mark.setPagefew(cursor.getInt(cursor.getColumnIndex("pagefew")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mark;
    }

    // 查询所有的书签
    public List<SZBookmark> findAllBookmarkById(String content_id) {
        List<SZBookmark> marks = new ArrayList<SZBookmark>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + table + " WHERE content_id=?", new
                    String[]{content_id});
            while (cursor.moveToNext()) {
                SZBookmark mark = new SZBookmark();
                mark.setId(cursor.getString(cursor.getColumnIndex("_id")));
                mark.setContent_id(cursor.getString(cursor.getColumnIndex("content_id")));
                mark.setTime(cursor.getLong(cursor.getColumnIndex("time")));
                mark.setContent(cursor.getString(cursor.getColumnIndex("content")));
                mark.setPagefew(cursor.getInt(cursor.getColumnIndex("pagefew")));
                marks.add(mark);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return marks;
    }
}
