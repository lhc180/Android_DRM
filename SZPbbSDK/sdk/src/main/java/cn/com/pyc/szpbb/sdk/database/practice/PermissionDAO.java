package cn.com.pyc.szpbb.sdk.database.practice;

import cn.com.pyc.szpbb.sdk.database.bean.Permission;
import cn.com.pyc.szpbb.sdk.database.dbBase.BaseDAOPractice;


interface PermissionDAO extends BaseDAOPractice<Permission>
{
	// 接收Asset的id数组 删除 调用 PermissionConstraint
}
