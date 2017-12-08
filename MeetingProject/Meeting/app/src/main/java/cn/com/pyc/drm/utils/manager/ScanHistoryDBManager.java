package cn.com.pyc.drm.utils.manager;

import android.content.Context;
import android.database.Cursor;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.bean.ScanMechanismHistory;
import cn.com.pyc.drm.utils.DRMLog;

public class ScanHistoryDBManager {

    private String TAG = ScanHistoryDBManager.class.getSimpleName();
    private static final String DB_NAME = "meet_history.db";
    private DbUtils db;

    /**
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
            db.createTableIfNotExist(ScanHistory.class);// 扫码会议历史记录
            db.createTableIfNotExist(ScanMechanismHistory.class);// 扫码机构历史记录
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public void checkScanHistoryTable() {
        boolean createTimeExist = checkColumnExist(ScanHistory.class.getSimpleName(), "createTime");
        if (!createTimeExist) {
            // 增加author字段
            db.getDatabase().execSQL("ALTER TABLE " + ScanHistory.class.getSimpleName() + " ADD COLUMN createTime");
        }
    }

    /**
     * 保存数据实体对象ScanHistory
     * <p>
     * 文件类型VIDEO,PDF，MUSIC
     *
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
                so.setClient_url(sh.getClient_url());
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
                so.setClient_url(sh.getClient_url());
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
     * 保存数据实体对象ScanHistory
     * <p>
     * 文件类型VIDEO,PDF，MUSIC
     *
     * @return
     */
    public boolean saveScanMechanismHistory(MechanismBean mb) {
        boolean flag = true;
        try {
            // 查询
            ScanMechanismHistory so = findScanMechanismHistory(mb.getServerAddress());
            if (so == null) {
                so = new ScanMechanismHistory();
                so.setServerAddress(mb.getServerAddress());
                so.setServerName(mb.getServerName());
                so.setSZUserName(mb.getSZUserName());
                db.save(so);
                DRMLog.e(TAG, "save index");
            } else {
                // 更新数据
                so.setServerName(mb.getServerName());
                so.setSZUserName(mb.getSZUserName());
                db.update(so, WhereBuilder.b("ServerAddress", "=", mb.getServerAddress()), "ServerName", "SZUserName");
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
     * @return
     */
    public boolean deleteMechanismByServerAddress(String sa) {
        boolean flag = true;
        try {
            db.delete(ScanMechanismHistory.class, WhereBuilder.b("ServerAddress", "=", sa));
        } catch (DbException e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 根据myProId查询<br\>
     * <p>
     * 返回值判断是否为null
     *
     * @return
     */
    public List<MechanismBean> findAllMechanismScanHistory() {
        List<ScanMechanismHistory> mScanMechanismHistory = null;
        List<MechanismBean> mMechanismBeanList = null;
        try {
            mScanMechanismHistory = db.findAll(Selector.from(ScanMechanismHistory.class));
        } catch (DbException e) {
            e.printStackTrace();
        }
        // 把ScanMechanismHistory转化为MechanismBean 以便和会议界面统一显示。
        if (mScanMechanismHistory != null && mScanMechanismHistory.size() > 0) {
            mMechanismBeanList = new ArrayList<MechanismBean>();
            for (int x = 0; x < mScanMechanismHistory.size(); x++) {
                ScanMechanismHistory scanmb = mScanMechanismHistory.get(x);
                MechanismBean mb = new MechanismBean();
                mb.setSZUserName(scanmb.getSZUserName());
                mb.setServerName(scanmb.getServerName());
                mb.setServerAddress(scanmb.getServerAddress());
                mMechanismBeanList.add(mb);
            }

        }
        return mMechanismBeanList;
    }


    /**
     * 根据meetingId删除一条数据ScanHistory
     *
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
     * 根据ServerAddress查询ScanMechanismHistory<br\>
     * <p>
     * 返回值判断是否为null
     *
     * @return
     */
    public  ScanMechanismHistory findScanMechanismHistory(String SA) {
        ScanMechanismHistory o = null;
        try {
            o = db.findFirst(Selector.from(ScanMechanismHistory.class).where(WhereBuilder.b("ServerAddress", "=", SA)));
        } catch (DbException e) {
            e.printStackTrace();
        }
        return o;
    }

    /**
     * 根据meetingId查询ScanHistory<br\>
     * <p>
     * 返回值判断是否为null
     *
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
     * <p>
     * 返回值判断是否为null
     *
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
     * @param tableName  表名
     * @param columnName 列名
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
