package cn.com.pyc.drm.utils;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.content.Intent;
import cn.com.pyc.drm.common.AppException;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.xml.OEX_Rights;
import cn.com.pyc.drm.model.xml.XML2JSON_Album;

/**
 * 解析文件
 * 
 * @author hudq
 * 
 */
public class ParserDRMFileUtil
{

	private static final String TAG = "ParserFile";

	private static ReentrantLock lock;

	private static ParserDRMFileUtil instance;

	private DRMDBHelper drmHelper;

	public static ParserDRMFileUtil getInstance()
	{
		if (null == instance)
		{

			synchronized (ParserDRMFileUtil.class)
			{
				if (null == instance)
				{
					instance = new ParserDRMFileUtil();
				}
			}
		}
		if (lock == null)
			lock = new ReentrantLock(true);
		return instance;
	}

	/**
	 * 解析下载的文件
	 * 
	 * @param myProid
	 * @param position
	 * @param author
	 * @param picture_ratio
	 * @param publishDate
	 */
	public void parserDRMFiles(Context context, final String myProid, final int position, final String author, final String picture_ratio,
			final String publishDate)
	{

		DRMLog.e(TAG, "start analytic file : " + myProid);
		if (drmHelper == null)
			drmHelper = new DRMDBHelper(context);
		DRMUtil.Is_analytic = myProid;
		// 下载文件路径
		String filePath = new StringBuffer().append(DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH).append(File.separator).append(myProid).append(DrmPat._DRM).toString();
		// 解析后文件保存路径
		String decodePath = new StringBuffer().append(DRMUtil.DEFAULT_SAVE_FILE_PATH).append(File.separator).append(myProid).toString();
		// 创建解压目录
		if (!FileUtils.createDirectory(decodePath))
		{
			// 创建解压目录失败！
			UIHelper.showToast(context, "create decodePath failed");
			return;
		} else
		{
			DRMLog.i("create decodePath：" + FileUtils.checkFilePathExists(decodePath));
		}
		try
		{
			lock.lock();
			List<FileUtils.CommonFile> list = FileUtils.parserDRMFile(filePath, decodePath);
			XML2JSON_Album albumInfo = null;
			OEX_Rights rights = null;
			if(list!=null){
				for (FileUtils.CommonFile c : list)
				{
					if (c.filetype == FileUtils.CommonFile.FILETYPE.ALBUMINFO)
					{
						albumInfo = FileUtils.parserJSON(new File(c.filepath), list);

					} else if (c.filetype == FileUtils.CommonFile.FILETYPE.RIGHT)
					{
						rights = FileUtils.parserRight(new File(c.filepath));
					}
				}
				// 将专辑信息和权限插入表中
				if (albumInfo != null && rights != null)
				{
					if (!drmHelper.insertDRMData(rights, albumInfo, myProid, author, picture_ratio, publishDate))
					{
						// 插入失败
						DRMLog.i("insert data failed");
					} else
					{
						// 插入成功
						DRMLog.i("insert success");
						for (FileUtils.CommonFile c : list)
						{
//							if (c.filetype == FileUtils.CommonFile.FILETYPE.ALBUMINFO)
//							{
//								FileUtils.deleteFileWithPath(c.filepath);
//							} else if (c.filetype == FileUtils.CommonFile.FILETYPE.RIGHT)
//							{
//								FileUtils.deleteFileWithPath(filePath);
//								FileUtils.deleteFileWithPath(c.filepath);
//							}
						}
					}
				}
				DRMUtil.Is_analytic = "";
				Intent intent = new Intent(DRMUtil.BROADCAST_RELOAD_HOMEITEM);
				intent.putExtra("analytic_position", position);
				context.sendBroadcast(intent);
				DRMLog.e(TAG, "end analytic file : " + myProid);
			}else{
				UIHelper.showToast(context, "对不起，解压文件出错。");
			}
			
			
			
		} catch (AppException e)
		{
			e.printStackTrace();
		} finally
		{
			if (lock != null)
			{
				lock.unlock();
				lock = null;
			}
		}
	}
}
