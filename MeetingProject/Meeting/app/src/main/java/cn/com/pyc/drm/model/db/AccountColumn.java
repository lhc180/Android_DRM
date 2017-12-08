package cn.com.pyc.drm.model.db;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户表 -- 登陆后返回所有未下载的内容显示界面，没有存入数据库。
 * 
 * @author lixiangyang
 * 
 */
public class AccountColumn extends DatabaseColumn
{

	public static final String TABLE_NAME = "account";
	public static final String USERNAME = "username";
	public static final String CREATE_TIME = "create_time";

	private static final Map<String, String> mColumnMap = new HashMap<String, String>();

	static
	{
		mColumnMap.put(_ID, "integer primary key ");
		mColumnMap.put(USERNAME, "varchar ");
		mColumnMap.put(CREATE_TIME, "TimeStamp NOT NULL DEFAULT (datetime('now','localtime'))");
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
