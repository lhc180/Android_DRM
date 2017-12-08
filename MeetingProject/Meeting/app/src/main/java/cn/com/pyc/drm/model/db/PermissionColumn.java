package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限表
 * 
 * @author lixiangyang
 * 
 */
public class PermissionColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "permission";
	public static final String CREATE_TIME = "create_time";
	public static final String ASSENT_ID = "assent_id";
	public static final String ELEMENT = "element";

	private static final Map<String, String> mColumnMap = new HashMap<String, String>();

	static
	{
		mColumnMap.put(_ID, "integer ");
		mColumnMap.put(CREATE_TIME, "varchar");
		mColumnMap.put(ASSENT_ID, "varchar");
		mColumnMap.put(ELEMENT, "varchar");
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
