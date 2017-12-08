package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class AlbumContentDAOImpl extends SZBaseDAOPracticeImpl<AlbumContent> implements AlbumContentDAO
{

	private static AlbumContentDAOImpl acdi = null;

	public boolean cascadedDelete(String id)
	{
		// System.out.println("");
		return true;
	}

	public static AlbumContentDAOImpl getInstance()
	{
		if (acdi == null)
		{
			acdi = new AlbumContentDAOImpl();
		}
		return acdi;
	}

}
