package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPractice;

public interface AlbumDAO   extends SZBaseDAOPractice<Album> {
	//根据专辑ID  删除元组  调用AlbumContent 
	public boolean cascadedDelete(String id);
}
