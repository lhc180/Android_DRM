package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

public class TestColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "";

	private static final Map<String, String> mColumnMap = new HashMap<String, String>();

	static
	{

		mColumnMap.put(_ID, "integer primary key autoincrement");

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
