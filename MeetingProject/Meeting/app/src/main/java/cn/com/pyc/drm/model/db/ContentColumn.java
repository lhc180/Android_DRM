package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

/**
 * 专辑表中每个资源的内容信息存储表
 * 
 * @author lixiangyang
 * 
 */
public class ContentColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "content";
	public static final String PRO_ID = "proid";
	public static final String PRO_NAME = "pro_name";
	public static final String ALBUMID = "albumId";

	private static final Map<String, String> mColumnMap = new HashMap<String, String>();

	static
	{
		mColumnMap.put(_ID, "integer primary key ");
		mColumnMap.put(PRO_ID, "varchar");
		mColumnMap.put(PRO_NAME, "varchar ");
		mColumnMap.put(ALBUMID, "varchar ");
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
