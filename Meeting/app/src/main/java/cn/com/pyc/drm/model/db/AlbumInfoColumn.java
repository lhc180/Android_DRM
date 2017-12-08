package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

/**
 * 专辑信息表：专辑表。里面有多个资源文件。
 * 
 * @author lixiangyang
 * 
 */
public class AlbumInfoColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "albuminfo";

	public static final String ALBUM_PICTURE = "picture";
	public static final String ALBUM_ALBUMNAME = "albumName";
	public static final String ALBUM_ALBUMID = "albumId";
	public static final String ALBUM_RID = "rid";
	public static final String CONTENT_COUNT = "content_count";
	public static final String ALBUM_ALBUMCATEGORY = "albumCategory";
	public static final String MY_PROID = "myproid";
	public static final String DOWNLOAD_SIZE = "Dwonloadsize";

	private static final Map<String, String> mColumnMap = new HashMap<String, String>();

	static
	{
		mColumnMap.put(_ID, "integer primary key ");
		mColumnMap.put(ALBUM_PICTURE, "varchar ");
		mColumnMap.put(ALBUM_ALBUMNAME, "varchar ");
		mColumnMap.put(ALBUM_ALBUMID, "varchar  ");
		mColumnMap.put(ALBUM_RID, "varchar ");
		mColumnMap.put(CONTENT_COUNT, "varchar ");
		mColumnMap.put(ALBUM_ALBUMCATEGORY, "varchar ");
		mColumnMap.put(MY_PROID, "varchar ");
		mColumnMap.put(DOWNLOAD_SIZE, "varchar ");
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
