package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

/**
 * 书签表:
 * 
 * @author lixiangyang
 * 
 */
public class BookMarkColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "bookmark";

	public static final Map<String, String> mColumnMap = new HashMap<String, String>();

	public static final String FILE_INDEX = "file_index";
	public static final String FILE_ID = "file_id";
	public static final String BOOK_MARK_CONTENT = "bookmark_content";
	public static final String BOOK_MARK_TIME = "bookmark_addtime";
	static
	{

		mColumnMap.put(_ID, "integer primary key autoincrement");
		mColumnMap.put(FILE_INDEX, "varchar");
		mColumnMap.put(FILE_ID, "varchar");
		mColumnMap.put(BOOK_MARK_CONTENT, "varchar");
		mColumnMap.put(BOOK_MARK_TIME, "varchar");

	}

	@Override
	public String getTableName()
	{
		return TABLE_NAME;
	}

	@Override
	protected Map<String, String> getTableMap()
	{
		return mColumnMap;
	}

}
