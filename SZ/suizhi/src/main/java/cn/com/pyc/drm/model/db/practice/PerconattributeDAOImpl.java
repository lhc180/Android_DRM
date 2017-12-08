package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.Perconattribute;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class PerconattributeDAOImpl extends
		SZBaseDAOPracticeImpl<Perconattribute> implements PerconattributeDAO
{
	private static PerconattributeDAOImpl daoImpl = new PerconattributeDAOImpl();

	public static PerconattributeDAOImpl getInstance()
	{
		return daoImpl;
	}

	private PerconattributeDAOImpl()
	{
	}
}
