package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class AlbumDAOImpl extends SZBaseDAOPracticeImpl<Album> implements AlbumDAO
{
	private static AlbumDAOImpl adi = null;

	public boolean cascadedDelete(String id)
	{
		return true;
	}

	public static AlbumDAOImpl getInstance()
	{
		if (adi == null)
		{
			adi = new AlbumDAOImpl();
		}
		return adi;
	}
}
