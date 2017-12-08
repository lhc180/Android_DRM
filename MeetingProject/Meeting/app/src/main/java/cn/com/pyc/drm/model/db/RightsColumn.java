package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

public class RightsColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "rights";

	public static final String RIGHTS_UID = "rights_uid";
	public static final String RIGHTS_VERSION = "rights_version";
	public static final String ACCOUNT_ID = "account_id";
	public static final String PRO_ALBUM_ID = "pro_album_id";
	public static final String USERNAME = "username";
	public static final String CREATE_TIME = "create_time";

	private static final Map<String, String> mColumnMap = new HashMap<String, String>();

	static
	{
		mColumnMap.put(_ID, "integer primary key ");
		mColumnMap.put(USERNAME, "varchar ");
		mColumnMap.put(RIGHTS_UID, "varchar ");
		mColumnMap.put(RIGHTS_VERSION, "varchar ");
		mColumnMap.put(ACCOUNT_ID, "varchar ");
		mColumnMap.put(PRO_ALBUM_ID, "varchar ");
		mColumnMap.put(CREATE_TIME, "DOUBLE ");
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
