package cn.com.pyc.szpbb.sdk.db;

import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import cn.com.pyc.szpbb.sdk.bean.DownData;

/**
 * 下载文件夹的数据管理,断点下载: DownFolderData数据表
 * 
 */
public class DownDataDBManager
{
	private DbManager dbManager;

	public static DownDataDBManager Builder()
	{
		return new DownDataDBManager();
	}

	private DownDataDBManager()
	{
		dbManager = x.getDb(DbConfig.daoConfig());
		DbConfig.createTableIfNotExist(dbManager, DownData.class);
	}

	/**
	 * 删除所有记录
	 */
	public void deleteAll()
	{
		try
		{
			if (dbManager != null)
			{
				dbManager.delete(DownData.class);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 保存或更新记录
	 * 
	 * @param data
	 */
	public void saveOrUpdate(DownData data)
	{
		try
		{
			// dbManager.replace(data); // 没有就插入，有就更新
			dbManager.saveOrUpdate(data);
		} catch (DbException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 查询记录
	 * 
	 * @param folderId
	 * @return
	 */
	public DownData findByFolderId(String folderId)
	{
		DownData data2 = null;
		try
		{
			data2 = dbManager.selector(DownData.class)
					.where(WhereBuilder.b("folderId", "=", folderId)).findFirst();
		} catch (DbException e)
		{
			e.printStackTrace();
		}
		return data2;
	}

	/**
	 * 通过myProId删除记录
	 * 
	 * @param folderId
	 * @return
	 */
	public int deleteByFolderId(String folderId)
	{
		int result = -1;
		try
		{
			result = dbManager.delete(DownData.class,
					WhereBuilder.b("folderId", "=", folderId));
		} catch (DbException e)
		{
			e.printStackTrace();
		}
		return result;
	}

}
