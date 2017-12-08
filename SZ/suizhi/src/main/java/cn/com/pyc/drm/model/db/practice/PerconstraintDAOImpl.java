package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.Perconstraint;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class PerconstraintDAOImpl extends SZBaseDAOPracticeImpl<Perconstraint>
		implements PerconstraintDAO
{
	private static PerconstraintDAOImpl daoImpl = new PerconstraintDAOImpl();

	public static PerconstraintDAOImpl getInstance()
	{
		return daoImpl;
	}

	private PerconstraintDAOImpl()
	{
	}
}
