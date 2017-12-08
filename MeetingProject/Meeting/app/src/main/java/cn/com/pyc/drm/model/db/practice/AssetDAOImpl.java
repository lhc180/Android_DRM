package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.Asset;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPracticeImpl;

public class AssetDAOImpl extends SZBaseDAOPracticeImpl<Asset> implements AssetDAO
{

	private static AssetDAOImpl adi = null;

	public static AssetDAOImpl getInstance()
	{
		if (adi == null)
		{
			adi = new AssetDAOImpl();
		}
		return adi;
	}

}
