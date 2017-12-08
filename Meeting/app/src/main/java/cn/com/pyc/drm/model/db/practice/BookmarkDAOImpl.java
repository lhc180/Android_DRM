package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class BookmarkDAOImpl extends SZBaseDAOPracticeImpl<Bookmark> implements
		BookmarkDAO {
	private static BookmarkDAOImpl daoInstance = new BookmarkDAOImpl();

	public static BookmarkDAOImpl getInstance() {
		return daoInstance;
	}
}
