package cn.com.pyc.drm.utils.manager;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.utils.DRMLog;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

public class ScanHistoryDBManager {

	private String TAG = ScanHistoryDBManager.class.getSimpleName();
	private static final String DB_NAME = "meet_history.db";

	private DbUtils db;

	/**
	 * 
	 * @param ctx
	 * @return
	 */
	public static ScanHistoryDBManager Builder(Context ctx) {
		return new ScanHistoryDBManager(ctx);
	}

	private ScanHistoryDBManager(Context context) {
		// 创建DbUtils单例（根据dbName的不同，创建多个实例） 默认： dbVersion=1
		db = DbUtils.create(context, DB_NAME);
		CreateTable();
	}

	private void CreateTable() {
		try {
			db.createTableIfNotExist(ScanHistory.class);// 扫码历史记录
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	public void checkScanHistoryTable() {
		boolean createTimeExist = checkColumnExist(ScanHistory.class.getSimpleName(), "createTime");
		if (!createTimeExist)
		{
			// 增加author字段
			db.getDatabase().execSQL("ALTER TABLE " + ScanHistory.class.getSimpleName() + " ADD COLUMN createTime");
		}
	}

	/**
	 * 保存数据实体对象ScanHistory
	 * 
	 * @param index
	 * @param myProId
	 * @param fileType
	 *            文件类型VIDEO,PDF，MUSIC
	 * @return
	 */
	public boolean saveScanHistory(ScanHistory sh) {
		boolean flag = true;
		try {
			// 查询
			ScanHistory so = findScanHistoryByMeetingID(sh.getMeetingId());
			if (so == null) {
				so = new ScanHistory();
				so.setMeetingId(sh.getMeetingId());
				so.setScanDataSource(sh.getScanDataSource());
				so.setMeetingType(sh.getMeetingType());
				so.setMeetingName(sh.getMeetingName());
				so.setUsername(sh.getUsername());
				so.setTime(System.currentTimeMillis());
				so.setIsverifys(sh.getIsverifys());
				so.setVerify_url(sh.getVerify_url());
				so.setVote_title(sh.getVote_title());
				so.setVote_url(sh.getVote_url());
				so.setCreateTime(sh.getCreateTime());
				db.save(so);
				DRMLog.e(TAG, "save index");
			} else {
				// 更新数据
				so.setTime(System.currentTimeMillis());
				so.setIsverifys(sh.getIsverifys());
				so.setVerify_url(sh.getVerify_url());
				so.setMeetingName(sh.getMeetingName());
				so.setUsername(sh.getUsername());
				so.setScanDataSource(sh.getScanDataSource());
				so.setVote_title(sh.getVote_title());
				so.setVote_url(sh.getVote_url());
				so.setCreateTime(sh.getCreateTime());
				db.update(so, WhereBuilder.b("meetingId", "=", sh.getMeetingId()), "time", "isverifys", "verify_url", "meetingName", "ScanDataSource", "Vote_title", "Vote_url", "createTime");
				DRMLog.e(TAG, "update index");
			}
		} catch (DbException e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 根据meetingId删除一条数据ScanHistory
	 * 
	 * @param myProId
	 * @return
	 */
	public boolean deleteByMeetingID(String meetingId) {
		boolean flag = true;
		try {
			db.delete(ScanHistory.class, WhereBuilder.b("meetingId", "=", meetingId));
		} catch (DbException e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 根据meetingId查询ScanHistory<br\>
	 * 
	 * 返回值判断是否为null
	 * 
	 * @param myProId
	 * @return
	 */
	private ScanHistory findScanHistoryByMeetingID(String meetingId) {
		ScanHistory o = null;
		try {
			o = db.findFirst(Selector.from(ScanHistory.class).where(WhereBuilder.b("meetingId", "=", meetingId)));
		} catch (DbException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * 根据myProId查询<br\>
	 * 
	 * 返回值判断是否为null
	 * 
	 * @param myProId
	 * @return
	 */
	public List<ScanHistory> findAllScanHistory() {
		List<ScanHistory> list = null;
		try {
			list = db.findAll(Selector.from(ScanHistory.class).orderBy("createTime", true));

		} catch (DbException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 删除ScanHistory所有记录
	 */
	public void deleteAllScanHistory() {
		try {
			if (db != null && db.tableIsExist(ScanHistory.class)) {
				DRMLog.e(TAG, "deleteAll");
				db.deleteAll(ScanHistory.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除ScanHistory表
	 */
	public void dropTableScanHistory() {
		try {
			if (db != null && db.tableIsExist(ScanHistory.class)) {
				DRMLog.e(TAG, "dropTable");
				db.dropTable(ScanHistory.class);
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除数据库
	 */
	public void dropDb() {
		try {
			DRMLog.e(TAG, "dropDb");
			db.close();
			db.dropDb();
			db = null;

		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 方法：检查某表列是否存在
	 * 
	 * @param tableName
	 *            表名
	 * @param columnName
	 *            列名
	 * @return
	 */
	public boolean checkColumnExist(String tableName, String columnName) {
		boolean result = false;
		Cursor cursor = null;
		try {

			// 查询一行
			android.database.sqlite.SQLiteDatabase ds = db.getDatabase();
			cursor = ds.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
			result = (cursor != null && cursor.getColumnIndex(columnName) != -1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor && !cursor.isClosed()) {
				cursor.close();
			}
		}

		return result;
	}
}
