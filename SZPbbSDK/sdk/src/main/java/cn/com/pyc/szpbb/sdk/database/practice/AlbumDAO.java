package cn.com.pyc.szpbb.sdk.database.practice;

import cn.com.pyc.szpbb.sdk.database.bean.SZAlbum;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPractice;


interface AlbumDAO extends BaseDAOPractice<SZAlbum>
{
	// 根据专辑ID 删除元组 调用AlbumContent
	//public boolean cascadedDelete(String id);
}
