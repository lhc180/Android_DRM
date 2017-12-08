package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.Right;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class RightDAOImpl extends SZBaseDAOPracticeImpl<Right> implements
		RightDAO
{

	private static RightDAOImpl daoImpl = new RightDAOImpl();

	public static RightDAOImpl getInstance()
	{
		return daoImpl;
	}

	private RightDAOImpl()
	{
	}
}
