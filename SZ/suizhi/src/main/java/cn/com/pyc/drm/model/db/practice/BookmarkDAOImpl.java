package cn.com.pyc.drm.model.db.practice;

import net.sqlcipher.Cursor;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class BookmarkDAOImpl extends SZBaseDAOPracticeImpl<Bookmark> implements
		BookmarkDAO {
	private static BookmarkDAOImpl daoInstance = new BookmarkDAOImpl();

	public static BookmarkDAOImpl getInstance() {
		return daoInstance;
	}

    private Bookmark getBookmark(Cursor cursor) {
        Bookmark bookmark = new Bookmark();
        bookmark.setId(cursor.getString(cursor.getColumnIndex("_id")));
        bookmark.setContent_ids(cursor.getString(cursor.getColumnIndex("content_ids")));
        bookmark.setTime(cursor.getString(cursor.getColumnIndex("time")));
        bookmark.setContent(cursor.getString(cursor.getColumnIndex("content")));
        bookmark.setPagefew(cursor.getString(cursor.getColumnIndex("pagefew")));
        return bookmark;
    }

    /**
     * 查询书签
     *
     * @param content_id
     * @param page_fews
     * @return
     */
    public Bookmark findBookmarkById(String content_id, int page_fews) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM Bookmark WHERE content_ids=? AND pagefew=?",
                    new String[]{content_id, (page_fews + "")});
            if (cursor.moveToNext()) {
                return getBookmark(cursor);
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

    // 查询所有的书签
    public List<Bookmark> findAllBookmarkById(String content_id) {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery("SELECT * FROM Bookmark WHERE content_ids=?",
                    new String[]{content_id});
            while (cursor.moveToNext()) {
                bookmarks.add(getBookmark(cursor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bookmarks;
    }

    // 删除书签
    public void deleteBookMark(String asset_id) {
        dbHelper.DeleteBookmark(asset_id);
    }

}
