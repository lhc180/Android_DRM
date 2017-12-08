package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.Downdata;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

/**
 * @author YF created on 2014-12-25 当前用户下载数据的DAO
 */
public class DowndataDAOImpl extends SZBaseDAOPracticeImpl<Downdata> implements
		DowndataDAO {
	private static DowndataDAOImpl daoInstance = new DowndataDAOImpl();

	public static DowndataDAOImpl getInstance() {
		return daoInstance;
	}

}
