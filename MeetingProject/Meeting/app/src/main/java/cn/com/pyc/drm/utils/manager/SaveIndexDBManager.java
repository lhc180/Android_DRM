package cn.com.pyc.drm.utils.manager;

import java.io.File;

import android.content.Context;
import cn.com.pyc.drm.bean.SaveIndex;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

/**
 * 保存专辑中子文件的记录索引
 * 
 * @author qd
 * 
 */
public class SaveIndexDBManager
{

	private String TAG = SaveIndexDBManager.class.getSimpleName();
	private static final String DB_NAME = "config.db";

	private DbUtils db;

	/**
	 * 
	 * @param ctx
	 * @return
	 */
	public static SaveIndexDBManager Builder(Context ctx)
	{
		return new SaveIndexDBManager(ctx);
	}

	private SaveIndexDBManager(Context context)
	{
		String dbDir = DRMUtil.getDefaultSDCardRootPath() + "dbDir" + File.separator;
		File file = new File(dbDir);
		if (!file.exists())
		{
			file.mkdirs();
		}
		// 创建DbUtils单例（根据dbName的不同，创建多个实例） 默认： dbVersion=1
		db = DbUtils.create(context, dbDir, DB_NAME);
	}

	/**
	 * 删除SaveIndex表
	 */
	public void dropTable()
	{
		try
		{
			if (db != null && db.tableIsExist(SaveIndex.class))
			{
				DRMLog.e(TAG, "dropTable");
				db.dropTable(SaveIndex.class);
			}
		} catch (DbException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 删除数据库
	 */
	public void dropDb()
	{
		try
		{
			DRMLog.e(TAG, "dropDb");
			db.close();
			db.dropDb();
			db = null;

		} catch (DbException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 删除所有记录
	 */
	public void deleteAll()
	{
		try
		{
			if (db != null && db.tableIsExist(SaveIndex.class))
			{
				DRMLog.e(TAG, "deleteAll");
				db.deleteAll(SaveIndex.class);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 保存数据实体对象
	 * 
	 * @param index
	 * @param myProId
	 * @param fileType
	 *            文件类型VIDEO,PDF，MUSIC
	 * @return
	 */
	public boolean saveDb(int index, String myProId, String fileType)
	{
		boolean flag = true;
		try
		{
			db.createTableIfNotExist(SaveIndex.class);
			// 查询
			SaveIndex so = findByMyProId(myProId);
			if (so == null)
			{
				so = new SaveIndex();
				so.setPositonIndex(String.valueOf(index));
				so.setMyProId(myProId);
				so.setTime(System.currentTimeMillis());
				so.setFileType(fileType);
				db.save(so);
				DRMLog.e(TAG, "save index");
			} else
			{
				// 更新数据
				so.setPositonIndex(String.valueOf(index));
				so.setTime(System.currentTimeMillis());
				db.update(so, WhereBuilder.b("myProId", "=", myProId), "positonIndex", "time");
				DRMLog.e(TAG, "update index");
			}
		} catch (DbException e)
		{
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 根据myProId删除一条数据
	 * 
	 * @param myProId
	 * @return
	 */
	public boolean deleteByMyProId(String myProId)
	{
		boolean flag = true;
		try
		{
			db.delete(SaveIndex.class, WhereBuilder.b("myProId", "=", myProId));
		} catch (DbException e)
		{
			e.printStackTrace();
			flag = false;
		}
		return flag;

	}

	/**
	 * 根据myProId查询<br\>
	 * 
	 * 返回值判断是否为null
	 * 
	 * @param myProId
	 * @return
	 */
	private SaveIndex findByMyProId(String myProId)
	{
		SaveIndex o = null;
		try
		{
			o = db.findFirst(Selector.from(SaveIndex.class).where(WhereBuilder.b("myProId", "=", myProId)));
		} catch (DbException e)
		{
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * 根据myProId查询获取当前index
	 * 
	 * @param myProId
	 * @return
	 */
	public int findIndexByMyProId(String myProId)
	{
		SaveIndex o = null;
		try
		{
			o = db.findFirst(Selector.from(SaveIndex.class).where(WhereBuilder.b("myProId", "=", myProId)));
		} catch (DbException e)
		{
			e.printStackTrace();
		}
		return o != null ? ConvertToUtils.toInt(o.getPositonIndex()) : -1;
	}

}
