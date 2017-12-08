package cn.com.pyc.drm.utils;

import java.io.File;

import android.os.Environment;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;

/**
 * 目录操作util类
 * 
 * @author hudq
 * 
 */
public class PathFileUtil
{

	/**
	 * 创建保存下载文件的目录
	 */
	public static void createFilePath()
	{
		String userName = Constant.getUserName();
		String id = (String) SPUtils.get(DRMUtil.KEY_MEETINGID, "");
		userName = userName + DrmPat.UNDER_LINE + id;
		DRMLog.d("fileDirName=" + userName);
//		if (DRMUtil.DEFAULT_SAVE_FILE_PATH == null)
//		{
			StringBuffer mFileSb = new StringBuffer();
			DRMUtil.DEFAULT_SAVE_FILE_PATH = mFileSb.append(Environment.getExternalStorageDirectory().toString()).append(File.separator)
					.append(DRMUtil.getDefaultOffset()).append(userName).append(File.separator).append("file").toString();
//		}

//		if (DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH == null)
//		{
			StringBuffer mDownloadSb = new StringBuffer();
			DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH = mDownloadSb.append(Environment.getExternalStorageDirectory().toString()).append(File.separator)
					.append(DRMUtil.getDefaultOffset()).append(userName).append(File.separator).append("Download").append(File.separator).append("file")
					.toString();
//		}
			StringBuffer localmFileSb = new StringBuffer();
			DRMUtil.DEFAULT_SAVE_FATHER_FILE_PATH = localmFileSb.append(Environment.getExternalStorageDirectory().toString()).append(File.separator)
					.append(DRMUtil.getDefaultOffset()).toString();
			
			
		DRMUtil.createDirectory();
		DRMUtil.createDirectoryAfterLogin();

	}

	/**
	 * 用户名切换后，对应的用户目录重新初始化
	 */
	public static void destoryFilePath()
	{
		DRMUtil.DEFAULT_SAVE_FILE_PATH = null;
		DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH = null;
	}

}
