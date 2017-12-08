package cn.com.pyc.drm.utils;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.db.*;

public class DBHelper extends SQLiteOpenHelper
{

	// 数据库版本，升级时修改
	private static final int DB_VERSION = 3;
	// 应用数据库创建名称标示
	public static final String DB_LABLE = "v3";
	// 数据库默认名称，当数据库名称为空时候使用
	//private static final String DB_DEFAULT_NAME = "drm";

	private static SQLiteDatabase db;

	private static DBHelper mdbHelper;

	/**
	 * 设置SQlite实例化null
	 * 
	 * @param mdbHelper
	 */
	public static void setMdbHelperNULL()
	{
		mdbHelper = null;
		if (db != null && db.isOpen())
			db.close();
		db = null;

		DRMLog.i("mdbHelper is null,and db also");
	}

	public DBHelper(Context context, String name)
	{
		super(context, name, null, DB_VERSION);
	}

	// public DBHelper(Context context)
	// {
	// super(context, DB_DEFAULT_NAME, null, DB_VERSION);
	// }

	/**
	 * (访问同步) <br>
	 * 
	 * 获取DBHelper实例
	 * 
	 * @param context
	 * @param name
	 *            DataBase Name
	 * @return
	 */
	public synchronized static DBHelper getInstance(Context context, String name)
	{
		// 创建数据库之前调用，sqlClipher加密库。
		SQLiteDatabase.loadLibs(context);
		String dbName = name + DB_LABLE;
		// 扫码登录时，数据库名 = 用户名 + 扫码id；
		String id = (String) SPUtils.get(DRMUtil.KEY_MEETINGID, "");
		dbName = dbName + DrmPat.UNDER_LINE + id;
		DRMLog.d("dataBase called " + dbName);
		if (mdbHelper == null)
		{
			mdbHelper = new DBHelper(context, dbName);
			db = mdbHelper.getWritableDatabase("mykey");
		}
		return mdbHelper;
	}

	@Deprecated
	@Override
	public void onCreate(SQLiteDatabase db)
	{

		DBHelper.db = db;
		operateTable(db, "");
		DRMLog.d("DBHelper", "onCreate DB");

		/*
		 * SQLiteDatabase.loadLibs(AppContext.appContext); Log.d("db",
		 * "onCreate DB");
		 */
	}

	@Deprecated
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		if (oldVersion == newVersion)
		{
			return;
		}
		operateTable(db, "DROP TABLE IF EXISTS ");
		onCreate(db);
	}

	@Deprecated
	public void operateTable(SQLiteDatabase db, String actionString)
	{
		Class<DatabaseColumn>[] columnsClasses = DatabaseColumn.getSubClasses();
		DatabaseColumn columns = null;
		for (int i = 0; i < columnsClasses.length; i++)
		{
			try
			{
				columns = columnsClasses[i].newInstance();
				if ("".equals(actionString) || actionString == null)
				{
					DRMLog.d("drm", "create table:" + columns.getTableCreateor());
					db.execSQL(columns.getTableCreateor());
				} else
				{
					DRMLog.d("drm", "drop table:" + columns.getTableName());
					db.execSQL(actionString + columns.getTableName());
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public long insert(String Table_Name, ContentValues values)
	{
		getDB();
		return db.insert(Table_Name, null, values);
	}

	/**
	 * @param Table_Name
	 * @param id
	 * @return 影响行数
	 */
	public int delete(String Table_Name, int id)
	{
		getDB();
		String ids = String.valueOf(id);
		return delete(Table_Name, ids);// db.delete(Table_Name, BaseColumns._ID
										// + "=?", new String[] { id + "" });
	}

	public int delete(String Table_Name, String id)
	{
		getDB();
		return db.delete(Table_Name, BaseColumns._ID + "=?", new String[] { id });
	}

	public void DeleteBookmark(String Asset_id)
	{
		getDB();
		db.execSQL("delete from Bookmark where content_ids=?", new Object[] { Asset_id });
	}

	public void DeleteBookmarkByID(String id)
	{
		getDB();
		db.execSQL("delete from Bookmark where _id=?", new Object[] { id });
	}
	public void DeleteAlbum(String id)
	{
		getDB();
		db.execSQL("delete from Album where _id=?", new Object[] { id });
	}

	public void DeleteAsset(String right_id)
	{
		getDB();
		db.execSQL("delete from Asset where right_id=?", new Object[] { right_id });
	}

	public void DeletePermission(String _id)
	{
		getDB();
		db.execSQL("delete from Permission where _id=?", new Object[] { _id });
	}

	public void DeletePerconstraint(String permission_id)
	{
		getDB();
		db.execSQL("delete from Perconstraint where Permission_id=?", new Object[] { permission_id });
	}

	public void DeleteAlbumContent(String album_id)
	{
		getDB();
		db.execSQL("delete from AlbumContent where album_id=?", new Object[] { album_id });
	}

	public void DeleteRight(String _id)
	{
		getDB();
		db.execSQL("delete from Right where _id=?", new Object[] { _id });
	}

	public void DeleteTableData(String table)
	{
		getDB();
		db.execSQL("delete from " + table, new Object[] {});
	}

	/**
	 * @param Table_Name
	 * @param values
	 * @param WhereClause
	 * @param whereArgs
	 * @return 影响行数
	 */
	public int update(String Table_Name, ContentValues values, String WhereClause, String[] whereArgs)
	{
		getDB();
		return db.update(Table_Name, values, WhereClause, whereArgs);
	}

	/**
	 * 
	 * @param Table_Name
	 * @param columns
	 * @param whereStr
	 * @param whereArgs
	 * @return
	 */
	public Cursor query(String Table_Name, String[] columns, String whereStr, String[] whereArgs)
	{
		if (db == null)
		{
			db = getReadableDatabase("mykey");
		}
		return db.query(Table_Name, columns, whereStr, whereArgs, null, null, null);
	}

	public Cursor query(String Table_Name, String[] columns, String whereStr, String[] whereArgs, String groupBy, String having, String orderBy)
	{
		if (db == null)
		{
			db = getReadableDatabase("mykey");
		}
		return db.query(Table_Name, columns, whereStr, whereArgs, groupBy, having, orderBy);
	}

	public Cursor rawQuery(String sql, String[] args)
	{
		if (db == null)
		{
			db = getReadableDatabase("mykey");
		}
		return db.rawQuery(sql, args);
	}

	/**
	 * Sql语句执行
	 * 
	 * @param sql
	 */
	public void ExecSQL(String sql)
	{
		getDB();
		db.execSQL(sql);
	}

	public void closeDb()
	{
		if (db != null)
		{
			db.close();
			db = null;
		}
	}

	public SQLiteDatabase getDB()
	{
		if (db == null)
		{
			db = mdbHelper.getWritableDatabase("mykey");
		}
		return db;
	}

	/*
	 * 设置界面清空所有表信息。 MY_PROID
	 * 
	 * @return
	 */
	@Deprecated
	public void deleteAll(Context settingActivity)
	{
		db.execSQL("delete from albuminfo where _id > ? ", new Object[] { 0 });
		db.execSQL("delete from content where _id > ? ", new Object[] { 0 });// 专辑表中每个资源的内容
		db.execSQL("delete from right where _id > ? ", new Object[] { 0 });
		db.execSQL("delete from asset where _id > ? ", new Object[] { 0 });
		db.execSQL("delete from permission where _id > ? ", new Object[] { 0 });
		db.execSQL("delete from perconstraint where _id > ? ", new Object[] { 0 });
		settingActivity.getContentResolver().notifyChange(Uri.parse("content://offfile"), null);
	}

	/*
	 * 删除本地文件删除后的专辑表。
	 * 
	 * @return
	 */
	@Deprecated
	public void deleteLocal(ArrayList<String> myProidy)
	{
		for (String mp : myProidy)
		{
			db.execSQL("delete from albuminfo where myproid = ? ", new Object[] { mp });
		}
		db.close();
	}

}
