package cn.com.pyc.drm.model.db.practice;

import cn.com.pyc.drm.model.db.bean.Permission;
import cn.com.pyc.drm.model.dbBase.SZBaseDAOPractice;

public interface PermissionDAO extends SZBaseDAOPractice<Permission>{
   //接收Asset的id数组  删除  调用 PermissionConstraint
}
