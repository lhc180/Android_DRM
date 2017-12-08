package cn.com.pyc.drm.model.dbBase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import net.sqlcipher.database.SQLiteDatabase;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.model.db.bean.Downdata;
import cn.com.pyc.drm.model.db.practice.DowndataDAOImpl;
import cn.com.pyc.drm.utils.DBHelper;
import cn.com.pyc.drm.utils.DRMDBHelper;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.FileUtils;
import cn.com.pyc.drm.utils.GenericsUtils;

public class SZBaseDAO<T> {
	/*
	 * 获取实体对象类型
	 */
	protected Class<T> entityClass;

	private DBHelper dbHelper;// = DBHelper.getInstance(App.getInstance(),
								// Constant.getUserName());

	@SuppressWarnings("unchecked")
	public SZBaseDAO() {
		if (dbHelper == null)
			dbHelper = DBHelper.getInstance(App.getInstance(), Constant.getUserName());
		entityClass = cn.com.pyc.drm.utils.GenericsUtils.getSuperClassGenricType(this.getClass());
		SQLiteDatabase.loadLibs(App.getInstance());
	}

	/*
	 * 数据表创建方法
	 */
	@SuppressWarnings("rawtypes")
	public void create(Class<T> entityClass) {
		StringBuffer sql = new StringBuffer("CREATE TABLE IF NOT EXISTS " + entityClass.getSimpleName() + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
		Field[] fields = entityClass.getDeclaredFields(); // 得到当前类entityClass所有的成员变量
		for (int i = 0; i < fields.length; i++) {
			Field fied = fields[i];
			String fiedName = fied.getName(); // 变量名
			Class fiedType = fied.getType(); // 变量类型
			if (!fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class) && !fiedName.equalsIgnoreCase("id")) {
				if (fiedType.equals(int.class)) { // !fiedName.equalsIgnoreCase("id")原因？？？
					sql.append(fiedName + " INTEGER, ");
				} else if (fiedType.equals(String.class)) {
					sql.append(fiedName + " varchar, ");
				} else if (fiedType.equals(float.class) | fiedType.equals(double.class)) {
					sql.append(fiedName + " double, ");
				} else if (fiedType.equals(long.class)) {
					sql.append(fiedName + " double, ");
				}
			}
		}
		sql.replace(sql.lastIndexOf(","), sql.length(), ")");
		dbHelper.ExecSQL(sql.toString());
	}

	/*
	 * 数据保存方法，包括： 单数据实体保存方法——save(T entity)； 多数据实体保存方法——batchSave(Collection<T>
	 * entityCollection)； 单数据实体级联保存方法——cascadedSave(T entity)；
	 * 多数据实体级联保存方法——cascadedBatchSave(Collection<T> entityCollection)。
	 */

	/*
	 * 单数据实体保存方法
	 */
	@SuppressWarnings("rawtypes")
	public synchronized boolean save(T entity) {
		// ContentValues负责存储一些键值对，键是一个String类型，而值是基本类型
		ContentValues contentValues = new ContentValues();
		Field[] fields = entityClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field fied = fields[i];
			String fiedName = fied.getName();
			Class fiedType = fied.getType();
			if (!fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
				String methodName = "get" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
				try {
					if (methodName.equals("getId")) {
						String temp = entity.getClass().getMethod(methodName).invoke(entity).toString();
						if (!temp.equals(""))
							contentValues.put(BaseColumns._ID, temp);
					} else {
						String temp = entity.getClass().getMethod(methodName).invoke(entity).toString();
						contentValues.put(fiedName, temp);
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		}
		if (dbHelper.insert(entityClass.getSimpleName(), contentValues) != -1) {
			return true;
		}
		return false;
	}

	/*
	 * 多数据实体保存方法
	 */
	@SuppressWarnings("rawtypes")
	public int[] batchSave(Collection<T> entityCollection) {
		Iterator it = entityCollection.iterator();
		int[] result = new int[entityCollection.size()];
		int count = 0;
		while (it.hasNext()) {
			Object singleEntity = it.next();
			ContentValues contentValues = new ContentValues();
			Field[] fields = singleEntity.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field fied = fields[i];
				String fiedName = fied.getName();
				Class fiedType = fied.getType();
				if (!fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
					String methodName = "get" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
					try {
						if (methodName.equals("getId")) {
							String temp = singleEntity.getClass().getMethod(methodName).invoke(singleEntity).toString();
							if (!temp.equals(""))
								contentValues.put(BaseColumns._ID, temp);
						} else {
							String temp = singleEntity.getClass().getMethod(methodName).invoke(singleEntity).toString();
							contentValues.put(fiedName, temp);
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
			result[count] = (int) dbHelper.insert(singleEntity.getClass().getSimpleName(), contentValues);
			count++;
		}
		return result;
	}

	/*
	 * 级联保存
	 */
	@SuppressWarnings("rawtypes")
	private int[] batchSaveEntity(List<Object> entityList) {
		int[] result = new int[entityList.size()];
		for (int count = 0; count < entityList.size(); count++) {
			ContentValues contentValues = new ContentValues();
			Field[] fields = entityList.get(count).getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field fied = fields[i];
				String fiedName = fied.getName();
				Class fiedType = fied.getType();
				if (!fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
					String methodName = "get" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
					try {
						if (methodName.equals("getId")) {
							String temp = entityList.get(count).getClass().getMethod(methodName).invoke(entityList.get(count)).toString();
							if (!temp.equals(""))
								contentValues.put(BaseColumns._ID, temp);
						} else {
							String temp = entityList.get(count).getClass().getMethod(methodName).invoke(entityList.get(count)).toString();
							contentValues.put(fiedName, temp);
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
			result[count] = (int) dbHelper.insert(entityList.get(count).getClass().getSimpleName(), contentValues);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Object> saveIterator(List<Object> objList) {
		List<Object> tempList = new Vector<Object>();
		for (Object obj : objList) {
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.getType().equals(List.class)) {
					String methodName = "get" + (char) (field.getName().charAt(0) - 32) + field.getName().substring(1);// 装配方法名称
					try {
						List temp = (List) obj.getClass().getMethod(methodName).invoke(obj);
						if (!temp.isEmpty()) {
							batchSaveEntity(temp);
							tempList.addAll(temp);
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return tempList;
	}

	/*
	 * 单数据实体级联保存方法
	 */
	public boolean cascadedSave(T entity) {
		save(entity);
		List<Object> objList = new Vector<Object>();
		objList.add(entity);
		boolean flag = true;
		while (flag == true) {
			objList = saveIterator(objList);
			if (!objList.isEmpty()) {
				flag = true;
			} else
				flag = false;
		}
		return true;
	}

	/*
	 * 多数据实体级联保存方法
	 */
	public boolean cascadedBatchSave(Collection<T> entityCollection) {
		batchSave(entityCollection);
		List<Object> objList = new Vector<Object>();
		objList.addAll(entityCollection);
		boolean flag = true;
		while (flag == true) {
			objList = saveIterator(objList);
			if (!objList.isEmpty()) {
				flag = true;
			} else
				flag = false;
		}
		return true;
	}

	/*
	 * 数据删除方法，包括： 单数据实体删除方法——delete(T entity)，deleteById(Serializable id)；
	 * 多数据实体删除方法——batchDelete(Collection<T> entityCollection)；
	 * 单数据实体级联删除方法——cascadedDeleteById(Serializable id)；
	 * 多数据实体级联删除方法——cascadedBatchDelete(Collection<T> entityCollection)。
	 */

	/*
	 * 单数据实体删除方法
	 */

	public int delete(T entity) {
		String id = "";
		try {
			id = entity.getClass().getMethod("getId").invoke(entity).toString();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return dbHelper.delete(entityClass.getSimpleName(), id);
	}

	/**
	 * 查询专辑_id
	 * 
	 * @param myproduct_id
	 * @return
	 */
	public String findAlbumId(String myproduct_id) {
		String result;
		Cursor cursor = dbHelper.rawQuery("select * from Album where myproduct_id=?", new String[] { myproduct_id });
		if (cursor.moveToNext()) {
			result = cursor.getString(0);
		} else {
			result = null;
		}
		cursor.close();
		return result;
	}

	/**
	 * 根据MyProId查询专辑
	 * 
	 * @param mypid
	 * @return
	 */
	public Album findAlbumByMyproId(String mypid) {
		Album album = null;
		Cursor cursor = dbHelper.rawQuery("select * from Album where myproduct_id=?", new String[] { mypid });
		if (cursor.moveToNext()) {
			album = new Album();
			album.setId(cursor.getString(cursor.getColumnIndex("_id")));
			album.setCategory(cursor.getString(cursor.getColumnIndex("category")));
			album.setItem_number(cursor.getString(cursor.getColumnIndex("item_number")));
			album.setModify_time(cursor.getString(cursor.getColumnIndex("modify_time")));
			album.setMyproduct_id(cursor.getString(cursor.getColumnIndex("myproduct_id")));
			album.setName(cursor.getString(cursor.getColumnIndex("name")));
			album.setPicture(cursor.getString(cursor.getColumnIndex("picture")));
			album.setProduct_id(cursor.getString(cursor.getColumnIndex("product_id")));
			album.setRight_id(cursor.getString(cursor.getColumnIndex("right_id")));
			album.setUsername(cursor.getString(cursor.getColumnIndex("username")));
			album.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
			album.setPicture_ratio(cursor.getString(cursor.getColumnIndex("picture_ratio")));
			album.setPublishDate(cursor.getString(cursor.getColumnIndex("publishDate")));
		} else {
			album = null;
		}
		cursor.close();
		return album;
	}

	/**
	 * 删除对应myProd的Album
	 * 
	 * @param myProId
	 */
	public void deleteAlbumByMyProId(String myProId) {
		int result = dbHelper.getDB().delete(Album.class.getSimpleName(), "myproduct_id=?", new String[] { myProId });
		DRMLog.w("deleteAlbumByMyProId，result = " + result);
	}

	public void DeleteAlbumContentByasset_id(String asset_id) {
		int result = dbHelper.getDB().delete(AlbumContent.class.getSimpleName(), "asset_id=?", new String[] { asset_id });
	}

	// 根据专辑_id删除专辑
	public void DeleteAlbum(String _id) {
		dbHelper.DeleteAlbum(_id);
	}

	// 删除书签
	public void DeleteBookMark(String Asset_id) {
		dbHelper.DeleteBookmark(Asset_id);
	}

	// 根据ID删除书签
	public void DeleteBookMarkById(String _id) {
		dbHelper.DeleteBookmarkByID(_id);
	}

	// 查询专辑Right的_id
	public String findRightId(String _id) {
		String result;
		Cursor cursor = dbHelper.rawQuery("select * from Right where _id=?", new String[] { _id });
		if (cursor.moveToNext()) {
			result = cursor.getString(0);
		} else {
			result = null;
		}
		cursor.close();
		return result;
	}

	// 删除right
	public void DeleteRight(String _id) {
		dbHelper.DeleteRight(_id);
	}

	/**
	 * 删除表中数据
	 * 
	 * @param table
	 */
	public void DeleteTableData(String table) {
		DRMLog.i("DeleteTableData : " + table);
		dbHelper.DeleteTableData(table);
	}

	// 查询单个文件权限表
	public ArrayList<String> findAssetId(String right_id) {
		String result;
		ArrayList<String> al = new ArrayList<String>();
		Cursor cursor = dbHelper.rawQuery("select * from Asset where right_id=?", new String[] { right_id });
		while (cursor.moveToNext()) {
			result = cursor.getString(0);
			al.add(result);
		}
		cursor.close();
		return al;
	}

	public void DeleteAsset(String mypid) {
		dbHelper.DeleteAsset(mypid);
	}

	// 查询单个文件权限表
	public String findPermissionId(String _id) {
		String result;
		Cursor cursor = dbHelper.rawQuery("select * from Permission where _id=?", new String[] { _id });
		if (cursor.moveToNext()) {
			result = cursor.getString(0);
		} else {
			result = null;
		}
		cursor.close();
		return result;
	}

	public void DeletePermission(String _id) {
		dbHelper.DeletePermission(_id);
	}

	// 查询文件权限信息约束表
	public ArrayList<String> findPerconstraintId(String Permission_id) {
		String result;
		ArrayList<String> al = new ArrayList<String>();
		Cursor cursor = dbHelper.rawQuery("select * from Perconstraint where Permission_id=?", new String[] { Permission_id });
		while (cursor.moveToNext()) {
			result = cursor.getString(0);
			al.add(result);
		}
		cursor.close();
		return al;
	}

	public void DeletePerconstraint(String Permission_id) {
		dbHelper.DeletePerconstraint(Permission_id);
	}

	// 查询文件权限信息约束表
	public String findAlbumContentId(String album_id) {
		String result;
		Cursor cursor = dbHelper.rawQuery("select * from AlbumContent where album_id=?", new String[] { album_id });
		if (cursor.moveToNext()) {
			result = cursor.getString(0);
		} else {
			result = null;
		}
		cursor.close();
		return result;
	}

	public void DeleteAlbumContent(String album_id) {
		dbHelper.DeleteAlbumContent(album_id);
	}

	/**
	 * 添加正在下载文件进度信息。(保存断点下载的进度)
	 * 
	 * @param myProId
	 * @param Percentage
	 * @param fomatTotalSize
	 * @param completeSize
	 * @param localPath
	 * @param ftpPath
	 */
	public synchronized void updateSaveDownData(String myProId, int Percentage, String fomatTotalSize, long completeSize, String localPath, String ftpPath) {
		Downdata data = DowndataDAOImpl.getInstance().findDowndataById(myProId);
		fomatTotalSize = (fomatTotalSize == null) ? "0M" : fomatTotalSize;
		if (data != null) {
			DRMLog.d("SZBaseDAO", "update data from Downdata table");
			Downdata d = new Downdata();
			d.setId(data.getId());
			d.setIsDownload("0");// 0 未下载完成,1 下载异常
			d.setLocalpath(localPath);// 本地文件路径
			d.setFtpPath(ftpPath);
			d.setProgress(String.valueOf(Percentage));// 下载进度百分比
			d.setTotalSize(fomatTotalSize);// 网络上文件总大小
			d.setFileOffsetstr(FileUtils.convertStorage(completeSize));// 本地已下载文件大小,格式化后的eg：22M
			d.setCompleteSize(completeSize);
			d.setMyProduct_id(myProId);// 文件的唯一ID
			// 更新数据表
			DowndataDAOImpl.getInstance().update(d);
		} else {
			DRMLog.d("SZBaseDAO", "insert data to Downdata table");
			long currentTime = System.currentTimeMillis();
			Downdata d = new Downdata();
			d.setId(String.valueOf(currentTime));// 唯一id
			d.setIsDownload("0");// 0 未下载完成,1 下载异常
			d.setLocalpath(localPath);// 本地文件路径
			d.setFtpPath(ftpPath);
			d.setProgress(String.valueOf(Percentage));// 下载进度百分比
			d.setTotalSize(fomatTotalSize);// 网络上文件总大小
			d.setFileOffsetstr(FileUtils.convertStorage(completeSize));// 本地已下载文件大小
			d.setCompleteSize(completeSize);
			d.setMyProduct_id(myProId);// 文件的唯一ID
			// 保存数据到表中
			DowndataDAOImpl.getInstance().save(d);
		}
	}

	/**
	 * 查询保存的下载文件进度数据
	 * 
	 * @param myProId
	 * @return
	 */
	public Downdata findDowndataById(String myProId) {
		Downdata data = null;
		Cursor cursor = null;
		try {
			cursor = dbHelper.rawQuery("select * from Downdata where myProduct_id=?", new String[] { myProId });
			if (cursor.moveToNext()) {
				DRMLog.d("findDowndataById");
				data = new Downdata();
				data.setId(cursor.getString(cursor.getColumnIndex("_id")));
				data.setFileOffsetstr(cursor.getString(cursor.getColumnIndex("fileOffsetstr")));
				data.setIsDownload(cursor.getString(cursor.getColumnIndex("isDownload")));
				data.setLocalpath(cursor.getString(cursor.getColumnIndex("localpath")));
				data.setMyProduct_id(cursor.getString(cursor.getColumnIndex("myProduct_id")));
				data.setTotalSize(cursor.getString(cursor.getColumnIndex("totalSize")));
				data.setProgress(cursor.getString(cursor.getColumnIndex("progress")));
				data.setCompleteSize(cursor.getInt(cursor.getColumnIndex("completeSize")));
				data.setFtpPath(cursor.getString(cursor.getColumnIndex("ftpPath")));
			} else {
				data = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return data;
	}

	/**
	 * 删除下载进度记录
	 * 
	 * @param myProId
	 */
	public void deleteDowndataByMyProId(String myProId) {
		int result = dbHelper.getDB().delete(Downdata.class.getSimpleName(), "myProduct_id=?", new String[] { myProId });
		DRMLog.w("deleteDowndataByMyProId，result = " + result);
	}

	/**
	 * 查询全部的保存的进度数据
	 * 
	 * @param myProId
	 * @return
	 */
	public List<Downdata> findAllDowndata() {
		Cursor cursor = null;
		List<Downdata> datas = new ArrayList<Downdata>();
		try {
			DRMLog.d("findAllDowndata");
			cursor = dbHelper.rawQuery("select * from Downdata", null);
			while (cursor.moveToNext()) {
				Downdata data = new Downdata();
				data.setId(cursor.getString(cursor.getColumnIndex("_id")));
				data.setFileOffsetstr(cursor.getString(cursor.getColumnIndex("fileOffsetstr")));
				data.setIsDownload(cursor.getString(cursor.getColumnIndex("isDownload")));
				data.setLocalpath(cursor.getString(cursor.getColumnIndex("localpath")));
				data.setMyProduct_id(cursor.getString(cursor.getColumnIndex("myProduct_id")));
				data.setTotalSize(cursor.getString(cursor.getColumnIndex("totalSize")));
				data.setProgress(cursor.getString(cursor.getColumnIndex("progress")));
				data.setCompleteSize(cursor.getInt(cursor.getColumnIndex("completeSize")));
				data.setFtpPath(cursor.getString(cursor.getColumnIndex("ftpPath")));

				datas.add(data);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return datas;
	}

	/**
	 * 查询书签
	 * 
	 * @param contentid
	 * @param pagefews
	 * @return
	 */
	public Bookmark findBookmarkById(String id) {
		Bookmark ddata = new Bookmark();
		Cursor cursor = dbHelper.rawQuery("select * from Bookmark where _id=?", new String[] { id });
		if (cursor.moveToNext()) {
			String _id = cursor.getString(cursor.getColumnIndex("_id"));
			String content_id = cursor.getString(cursor.getColumnIndex("content_ids"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			String content = cursor.getString(cursor.getColumnIndex("content"));
			String pagefew = cursor.getString(cursor.getColumnIndex("pagefew"));
			ddata.setId(_id);
			ddata.setContent_ids(content_id);
			ddata.setTime(time);
			ddata.setContent(content);
			ddata.setPagefew(pagefew);
		} else {
			ddata = null;
		}
		cursor.close();
		return ddata;
	}

	// 查询所有的书签
	public List<Bookmark> findAllBookmarkById(String contentid) {
		List<Bookmark> bookmarks = new ArrayList<Bookmark>();
		Cursor cursor = dbHelper.rawQuery("select * from Bookmark where content_ids=?", new String[] { contentid });
		while (cursor.moveToNext()) {
			Bookmark ddata = new Bookmark();
			String _id = cursor.getString(cursor.getColumnIndex("_id"));
			String content_id = cursor.getString(cursor.getColumnIndex("content_ids"));
			String time = cursor.getString(cursor.getColumnIndex("time"));
			String content = cursor.getString(cursor.getColumnIndex("content"));
			String pagefew = cursor.getString(cursor.getColumnIndex("pagefew"));
			ddata.setId(_id);
			ddata.setContent_ids(content_id);
			ddata.setTime(time);
			ddata.setContent(content);
			ddata.setPagefew(pagefew);

			bookmarks.add(ddata);
		}
		cursor.close();
		return bookmarks;
	}

	/*
	 * 多数据实体删除方法
	 */
	@SuppressWarnings("rawtypes")
	public int[] batchDelete(Collection<T> entityCollection) {
		Iterator it = entityCollection.iterator();
		int[] result = new int[entityCollection.size()];
		int count = 0;
		while (it.hasNext()) {
			Object singleEntity = it.next();
			try {
				String id = "";
				id = singleEntity.getClass().getMethod("getId").invoke(singleEntity).toString();
				result[count] = dbHelper.delete(singleEntity.getClass().getSimpleName(), id);
				count++;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	// 根据id删除table中数据
	public int deleteEntityByIdAndName(String id, String tableName) {
		return dbHelper.delete(tableName, id);
	}

	/**
	 * 级联删除
	 */
	@SuppressWarnings("unused")
	private class StackItem {
		private String id;
		private String entityName;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getEntityName() {
			return entityName;
		}

		public void setEntityName(String entityName) {
			this.entityName = entityName;
		}
	}

	private List<Object> iterator(List<Object> objList, Stack stack) {
		List<Object> tempList = new Vector<Object>();
		for (Object obj : objList) {
			StackItem stackItem = new StackItem();
			/* 主表id即为子表外键，先通过对象得到对象类名，再通过类名查找类中定义的方法，得到对象中存储的id */
			String foreignId = null;
			try {
				foreignId = obj.getClass().getMethod("getId").invoke(obj).toString();
				stackItem.id = foreignId;
				stackItem.entityName = obj.getClass().getSimpleName();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			}
			//
			// /*通过主表的领域值查找子表名，并查询子表内容*/
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.getType().equals(List.class)) {
					String childTableName = GenericsUtils.getFieldGenericType(field).getSimpleName();// 装配子表名称
					Cursor cursor = dbHelper.query(childTableName, null, obj.getClass().getSimpleName().charAt(0) + obj.getClass().getSimpleName().substring(1) + "_id =?", new String[] { foreignId });
					while (cursor.moveToNext()) {
						Object singleObj;
						try {
							singleObj = GenericsUtils.getFieldGenericType(field).newInstance();
							Field[] singleFields = singleObj.getClass().getDeclaredFields();
							for (int i = 0; i < singleFields.length; i++) {
								Field fied = singleFields[i];
								String fiedName = fied.getName();
								Class fiedType = fied.getType();
								if (!fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
									String methodName = "set" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
									try {
										if (methodName.equals("setId")) {
											int idColumn = cursor.getColumnIndex(BaseColumns._ID);
											String idValue = cursor.getString(idColumn);
											singleObj.getClass().getMethod(methodName, new Class[] { String.class }).invoke(singleObj, new Object[] { idValue });
										} else {
											int idColumn = cursor.getColumnIndex(fiedName);
											String Value = cursor.getString(idColumn);
											singleObj.getClass().getMethod(methodName, new Class[] { String.class }).invoke(singleObj, new Object[] { Value });
										}
									} catch (IllegalAccessException e) {
										e.printStackTrace();
									} catch (IllegalArgumentException e) {
										e.printStackTrace();
									} catch (InvocationTargetException e) {
										e.printStackTrace();
									} catch (NoSuchMethodException e) {
										e.printStackTrace();
									}
								}
							}
							tempList.add(singleObj);
						} catch (InstantiationException | IllegalAccessException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
			stack.push(stackItem);
		}
		return tempList;
	}

	/*
	 * 单数据实体级联删除方法
	 */
	@SuppressLint("UseValueOf")
	@SuppressWarnings("rawtypes")
	public boolean cascadedDeleteById(String id) {
		List<Object> objList = new Vector<Object>();
		Cursor cursor = dbHelper.query(entityClass.getSimpleName(), null, BaseColumns._ID + "=?", new String[] { id });
		while (cursor.moveToNext()) {
			T obj;
			try {
				obj = entityClass.newInstance();
				Field[] fields = obj.getClass().getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field fied = fields[i];
					String fiedName = fied.getName();
					Class fiedType = fied.getType();
					if (!fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
						String methodName = "set" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
						try {
							if (methodName.equals("setId")) {
								int idColumn = cursor.getColumnIndex(BaseColumns._ID);
								String idValue = cursor.getString(idColumn);
								obj.getClass().getMethod(methodName, new Class[] { String.class }).invoke(obj, new Object[] { idValue });
							} else {
								int idColumn = cursor.getColumnIndex(fiedName);
								String Value = cursor.getString(idColumn);
								obj.getClass().getMethod(methodName, new Class[] { String.class }).invoke(obj, new Object[] { Value });
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						}
					}
				}
				objList.add(obj);
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}

		}
		Stack<StackItem> stack = new Stack<StackItem>();
		boolean flag = true;

		while (flag == true) {
			objList = iterator(objList, stack);
			if (!objList.isEmpty()) {
				flag = true;
			} else
				flag = false;
		}
		while (!stack.isEmpty()) {
			StackItem stackItem = stack.pop();
			deleteEntityByIdAndName(stackItem.getId(), stackItem.getEntityName());
		}
		return true;
	}

	/*
	 * 多数据实体级联删除方法
	 */
	public boolean cascadedBatchDelete(Collection<T> entityCollection) {
		List<Object> objList = new Vector<Object>();
		objList.addAll(entityCollection);
		Stack<StackItem> stack = new Stack<StackItem>();

		boolean flag = true;
		while (flag == true) {
			objList = iterator(objList, stack);
			if (!objList.isEmpty()) {
				flag = true;
			} else
				flag = false;
		}
		while (!stack.isEmpty()) {
			StackItem stackItem = stack.pop();
			deleteEntityByIdAndName(stackItem.getId(), stackItem.getEntityName());
		}
		return true;
	}

	/*
	 * 数据更新方法，包括： 单数据实体更新方法——update(T entity)；
	 * 多数据实体更新方法——batchUpdate(Collection<T> entityCollection)；
	 * 单数据实体级联更新方法——cascadedUpdate(T entity)，cascadedSetActiveById(Serializable
	 * id, boolean active)； 多数据实体级联更新方法——cascadedBatchUpdate(Collection<T>
	 * entityCollection)。
	 */

	/*
	 * 单数据实体更新方法
	 */
	@SuppressWarnings("rawtypes")
	public synchronized int update(T entity) {
		int result = 0;
		ContentValues contentValues = new ContentValues();
		Field[] fields = entityClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field fied = fields[i];
			String fiedName = fied.getName();
			Class fiedType = fied.getType();
			if (!fiedName.equalsIgnoreCase("id") && !fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
				try {
					String methodName = "get" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
					String temp = entity.getClass().getMethod(methodName).invoke(entity).toString();
					contentValues.put(fiedName, temp);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			result = dbHelper.update(entityClass.getSimpleName(), contentValues, "_id=?", new String[] { entity.getClass().getMethod("getId").invoke(entity).toString() });
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * 多数据实体更新方法
	 */
	@SuppressWarnings("rawtypes")
	public int[] batchUpdate(Collection<T> entityCollection) {
		Iterator it = entityCollection.iterator();
		int[] result = new int[entityCollection.size()];
		int count = 0;
		while (it.hasNext()) {
			Object singleEntity = it.next();
			ContentValues contentValues = new ContentValues();
			Field[] fields = singleEntity.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field fied = fields[i];
				String fiedName = fied.getName();
				Class fiedType = fied.getType();
				if (!fiedName.equalsIgnoreCase("id") && !fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
					String temp;
					try {
						String methodName = "get" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
						temp = singleEntity.getClass().getMethod(methodName).invoke(singleEntity).toString();
						contentValues.put(fiedName, temp);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				result[count] = dbHelper.update(entityClass.getSimpleName(), contentValues, "_id=?", new String[] { singleEntity.getClass().getMethod("getId").invoke(singleEntity).toString() });
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			count++;
		}
		return result;
	}

	/*
	 * 级联更新实体
	 */
	@SuppressWarnings("rawtypes")
	private int[] batchUpdateEntity(List<Object> entityList) {
		int[] result = new int[entityList.size()];
		int count = 0;
		for (Object obj : entityList) {
			ContentValues contentValues = new ContentValues();
			Field[] fields = obj.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field fied = fields[i];
				String fiedName = fied.getName();
				Class fiedType = fied.getType();
				if (!fiedName.equalsIgnoreCase("id") && !fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
					try {
						String methodName = "get" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
						String temp = obj.getClass().getMethod(methodName).invoke(obj).toString();
						contentValues.put(fiedName, temp);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				result[count] = dbHelper.update(obj.getClass().getSimpleName(), contentValues, "_id=?", new String[] { obj.getClass().getMethod("getId").invoke(obj).toString() });
				count++;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Object> updateIterator(List<Object> objList) {
		List<Object> tempList = new Vector<Object>();
		for (Object obj : objList) {
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.getType().equals(List.class)) {
					String methodName = "get" + (char) (field.getName().charAt(0) - 32) + field.getName().substring(1);// 装配方法名称
					try {
						List temp = (List) obj.getClass().getMethod(methodName).invoke(obj);
						if (!temp.isEmpty()) {
							batchUpdateEntity(temp);
							tempList.addAll(temp);
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return tempList;
	}

	/*
	 * 单数据实体级联更新方法
	 */
	public boolean cascadedUpdate(T entity) {
		update(entity);
		List<Object> objList = new Vector<Object>();
		objList.add(entity);
		boolean flag = true;
		while (flag == true) {
			objList = updateIterator(objList);
			if (!objList.isEmpty()) {
				flag = true;
			} else
				flag = false;
		}
		return true;
	}

	/*
	 * 多数据实体级联更新方法
	 */
	public boolean cascadedBatchUpdate(Collection<T> entityCollection) {
		batchUpdate(entityCollection);
		List<Object> objList = new Vector<Object>();
		objList.addAll(entityCollection);
		boolean flag = true;
		while (flag == true) {
			objList = updateIterator(objList);
			if (!objList.isEmpty()) {
				flag = true;
			} else
				flag = false;
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public T findById(String id) {
		T obj = null;
		Cursor cursor = dbHelper.query(entityClass.getSimpleName(), null, BaseColumns._ID + "=?", new String[] { String.valueOf(id) });
		if (cursor.moveToNext()) {
			try {
				obj = entityClass.newInstance();
				Field[] fields = obj.getClass().getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field fied = fields[i];
					String fiedName = fied.getName();
					Class fiedType = fied.getType();
					if (!fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
						String methodName = "set" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
						if (methodName.equals("setId")) {
							try {
								int idColumn = cursor.getColumnIndex(BaseColumns._ID);
								String idValue = cursor.getString(idColumn);
								obj.getClass().getMethod(methodName, new Class[] { String.class }).invoke(obj, new Object[] { idValue });
							} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
								e.printStackTrace();
							}
						} else {
							try {
								int idColumn = cursor.getColumnIndex(fiedName);
								String Value = cursor.getString(idColumn);
								obj.getClass().getMethod(methodName, new Class[] { String.class }).invoke(obj, new Object[] { Value });
							} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}

	@SuppressWarnings("rawtypes")
	public List<T> findAll(Class<T> entityClass, String order) {
		List<T> objList = new Vector<T>();
		Cursor cursor = dbHelper.query(entityClass.getSimpleName(), null, null,
				null, null, null, BaseColumns._ID + " " + order);
		while (cursor.moveToNext()) {
			T obj;
			try {
				obj = entityClass.newInstance();
				Field[] fields = obj.getClass().getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field fied = fields[i];
					String fiedName = fied.getName();
					Class fiedType = fied.getType();
					if (!fiedName.equalsIgnoreCase("serialVersionUID")
							&& !fiedType.equals(Collections.class)
							&& !fiedType.equals(Map.class)
							&& !fiedType.equals(List.class)
							&& !fiedType.equals(Set.class)) {
						String methodName = "set"
								+ (char) (fiedName.charAt(0) - 32)
								+ fiedName.substring(1);
						try {
							if (methodName.equals("setId")) {
								int idColumn = cursor
										.getColumnIndex(BaseColumns._ID);
								String idValue = cursor.getString(idColumn);
								obj.getClass()
										.getMethod(methodName,
												new Class[] { String.class })
										.invoke(obj, new Object[] { idValue });
							} else {
								int idColumn = cursor.getColumnIndex(fiedName);
								String Value = cursor.getString(idColumn);
								obj.getClass()
										.getMethod(methodName,
												new Class[] { String.class })
										.invoke(obj, new Object[] { Value });
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						}
					}
				}
				objList.add(obj);
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
		}
		if(cursor != null){
			cursor.close();
			cursor = null;
		}

		return objList;
	}

	@SuppressWarnings("rawtypes")
	public List<?> findByQuery(String sql, Class<?> entityClass) {
		if (dbHelper == null) {
			dbHelper = DBHelper.getInstance(App.getInstance(), Constant.getUserName());
		}
		String class_name = entityClass.getSimpleName();
		boolean isAlbumContent = tabIsExist(class_name);
		// 如果表，在数据库中不存在，则重新创建。
		if (!isAlbumContent) {
			DRMDBHelper helper = new DRMDBHelper(App.getInstance());
			helper.createDBTable();
		}

		List<Object> objList = new Vector<Object>();
		Cursor cursor = dbHelper.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			Object obj;
			try {
				obj = entityClass.newInstance();
				Field[] fields = obj.getClass().getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field fied = fields[i];
					String fiedName = fied.getName();
					Class fiedType = fied.getType();
					if (!fiedName.equalsIgnoreCase("serialVersionUID") && !fiedType.equals(Collections.class) && !fiedType.equals(Map.class) && !fiedType.equals(List.class) && !fiedType.equals(Set.class)) {
						String methodName = "set" + (char) (fiedName.charAt(0) - 32) + fiedName.substring(1);
						try {
							if (methodName.equals("setId")) {
								int idColumn = cursor.getColumnIndex(BaseColumns._ID);
								String idValue = cursor.getString(idColumn);
								obj.getClass().getMethod(methodName, new Class[] { String.class }).invoke(obj, new Object[] { idValue });
							} else {
								int idColumn = cursor.getColumnIndex(fiedName);
								if(idColumn!=-1){
									String Value = cursor.getString(idColumn);
									obj.getClass().getMethod(methodName, new Class[] { String.class }).invoke(obj, new Object[] { Value });
								}
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						}
					}
				}
				objList.add(obj);
			} catch (InstantiationException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
		// dbHelper.close();
		return objList;
	}

	public boolean tabIsExist(String tabName) {
		boolean result = false;
		if (tabName == null) {
			return false;
		}
		Cursor cursor = null;
		try {

			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tabName.trim() + "' ";
			cursor = dbHelper.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return result;
	}

	public List<?> findByQuery(String[] elements, String[] args, Class<?> entityClass) {
		if (elements.length != args.length)
			return null;
		StringBuffer sql = new StringBuffer("select * from " + entityClass.getSimpleName() + " where ");
		for (int i = 0; i < elements.length; i++) {
			if (i < elements.length - 1) {
				sql.append(elements[i] + " = '" + args[i] + "' " + "and ");
			} else {
				sql.append(elements[i] + " = '" + args[i] + "' ");
			}

		}
		// sql.append("order by _id desc");
		List<?> mLists = findByQuery(sql.toString(), entityClass);
		return mLists;
	}

	public List<?> findByQueryOrder(String[] elements, String[] args, Class<?> entityClass, String order) {
		if (elements.length != args.length)
			return null;
		StringBuffer sql = new StringBuffer("select * from " + entityClass.getSimpleName() + " where ");
		for (int i = 0; i < elements.length; i++) {
			if (i < elements.length - 1) {
				sql.append(elements[i] + " = '" + args[i] + "' " + "and ");
			} else {
				sql.append(elements[i] + " = '" + args[i] + "' ");
			}

		}
		sql.append(order);
		List<?> mLists = findByQuery(sql.toString(), entityClass);
		return mLists;
	}

	@SuppressWarnings("unchecked")
	public T findObjectByQuery(String[] elements, String[] args, Class<?> entityClass) {
		if (elements.length != args.length)
			return null;
		StringBuffer sql = new StringBuffer("select * from " + entityClass.getSimpleName() + " where ");
		for (int i = 0; i < elements.length; i++) {
			sql.append(elements[i] + " = '" + args[i] + "' ");
		}
		T result = (T) findByQuery(sql.toString(), entityClass).get(0);
		// dbHelper.close();
		return result;
	}

	/* 自定义SQL操作 */
	public void executeBySql(String sql) {
		dbHelper.ExecSQL(sql);
		dbHelper.closeDb();
	}

}
