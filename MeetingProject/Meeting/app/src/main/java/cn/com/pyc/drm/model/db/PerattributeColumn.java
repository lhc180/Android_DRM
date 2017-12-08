package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

public class PerattributeColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "perattribute";
	public static final String UID = "uid";
	public static final String PERMISSION_ID = "permission_id";
	public static final String ELEMENT = "element";
	public static final String VALUE = "value";
	public static final String CREATE_TIME = "create_time";

	private static final Map<String, String> mColumnMap = new HashMap<String, String>();

	static
	{
		mColumnMap.put(UID, "varchar primary key ");
		mColumnMap.put(PERMISSION_ID, "varchar ");
		mColumnMap.put(ELEMENT, "varchar ");
		mColumnMap.put(VALUE, "varchar");
		mColumnMap.put(CREATE_TIME, "DOUBLE");
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
