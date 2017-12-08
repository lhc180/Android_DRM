package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

public class PerconattributeColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "perconattribute";
	public static final String UID = "uid";
	public static final String PERCONSTRAINT_ID = "perconstraint_id";
	public static final String USERNAME = "username";
	public static final String CREATE_TIME = "create_time";

	private static final Map<String, String> mColumnMap = new HashMap<String, String>();

	static
	{
		mColumnMap.put(UID, "varchar primary key ");
		mColumnMap.put(PERCONSTRAINT_ID, "varchar ");
		mColumnMap.put(USERNAME, "varchar ");
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
