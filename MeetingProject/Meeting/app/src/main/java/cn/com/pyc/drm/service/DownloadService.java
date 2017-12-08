package cn.com.pyc.drm.service;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.model.DownloadInfo;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.manager.DownloadTaskManager;

/**
 * drm文件下载任务service
 * 
 * @author hudq
 * 
 */
public class DownloadService extends Service
{

	private String TAG = DownloadService.class.getSimpleName();
	private Context context;

	// private final int count = Runtime.getRuntime().availableProcessors() * 3;

	// 下载完毕
	public static final String ACTION_FINISH = "cn.com.pyc.drm.Action_Finish";
	// 下载异常
	public static final String ACTION_ERROR = "cn.com.pyc.drm.Action_Error";
	// 连接异常
	public static final String ACTION_CONNECT_ERROR = "cn.com.pyc.drm.Action_Connect_Error";
	// 正在连接
	public static final String ACTION_CONNECTING = "cn.com.pyc.drm.Action_Connecting";
	// 更新ui
	public static final String ACTION_UPDATE = "cn.com.pyc.drm.Action_Update";
	// 停止某一个下载任务
	public static final String ACTION_STOP = "cn.com.pyc.drm.Action_Stop";
	// 停止所有下载(退出应用主页面时)
	public static final String ACTION_ALL_STOP = "cn.com.pyc.drm.Action_All_Stop";

	// 下载任务的集合
	private static Map<String, DownloadTaskManager> sTasks;

	@Override
	public void onCreate()
	{
		super.onCreate();
		context = this;
		if (sTasks == null)
		{
			sTasks = new LinkedHashMap<String, DownloadTaskManager>();
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		DRMLog.d(TAG, "onDestroy");
		DownloadTaskManager.closeExecutorService();
		if (sTasks != null)
		{
			sTasks.clear();
			sTasks = null;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent == null) { return super.onStartCommand(intent, flags,
				startId); }
		int opt = intent.getIntExtra("option", 0);
		DownloadInfo info = (DownloadInfo) intent
				.getParcelableExtra("DownloadInfo");

		switch (opt) {
		case ConstantValue.START_DOWNLOAD:
			// 添加下载任务,下载的专辑信息
			startTask(info);
			break;
		default:
			break;
		}
		String action = intent.getAction();
		if (ACTION_STOP.equalsIgnoreCase(action))
		{
			// 通过myProId，取出下载任务，暂停下载
			String myProId = intent.getStringExtra("myProId");
			DownloadTaskManager task = sTasks.get(myProId);
			if (task != null)
			{
				task.isPause = true;
			}
		} else if (ACTION_ALL_STOP.equalsIgnoreCase(action))
		{
			// 停止所有下载
			DRMLog.i("stop task ,size = " + sTasks.size());
			Iterator<String> itor = sTasks.keySet().iterator();
			while (itor.hasNext())
			{
				DownloadTaskManager task = sTasks.get(itor.next());
				if (task != null && !task.isPause)
				{
					DRMLog.d(TAG, "stop all task");
					task.isPause = true;
				}
			}
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 启动下载任务
	 * 
	 * @param info
	 */
	private void startTask(DownloadInfo info)
	{
		DRMLog.e(TAG, "1.satrt download : " + info.getProductName());
		DownloadTaskManager task = new DownloadTaskManager(context, info);
		task.downloadInfo();
		// 将下载任务加到map集合中
		sTasks.put(info.getMyProId(), task);
		// DRMLog.d(TAG, "开启第" + sTasks.size() + "个下载任务");
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/**
	 * 删除所有残留的不完整的文件
	 * 
	 * @param myProid
	 */
	// @Deprecated
	// protected void delAllFile(final String myProid)
	// {
	// String decodePath = DRMUtil.DEFAULT_SAVE_FILE_PATH + File.separator
	// + myProid;
	// // String decodePathdrm = DRMUtil.DEFAULT_SAVE_FILE_PATH +
	// // File.separator + myProid + ".drm";
	// FileUtils.delFolder(decodePath);
	// // FileUtils.delFolder(decodePathdrm);
	// new AsyncTask<String, String, String>()
	// {
	// protected String doInBackground(String... params)
	// {
	// String Album_Id = AlbumDAOImpl.getInstance().findAlbumId(
	// myProid);// 获取专辑ID
	// if (Album_Id != null)
	// {
	// AlbumDAOImpl.getInstance().DeleteAlbum(Album_Id);
	// String albumContentIdList = AlbumDAOImpl.getInstance()
	// .findalbumContentId(Album_Id);
	// if (albumContentIdList != null)
	// {
	// AlbumDAOImpl.getInstance().DeletealbumContent(Album_Id);
	// }
	// String RightContentIdList = AlbumDAOImpl.getInstance()
	// .findRightId(Album_Id);
	// if (RightContentIdList != null)
	// {
	// AlbumDAOImpl.getInstance().DeleteRight(Album_Id);
	// }
	// ArrayList<String> assetIdList = AlbumDAOImpl.getInstance()
	// .findAssetId(Album_Id);
	// if (assetIdList != null && assetIdList.size() > 0)
	// {
	// AlbumDAOImpl.getInstance().DeleteAsset(Album_Id);
	// for (int i = 0; i < assetIdList.size(); i++)
	// {
	// String assetId = assetIdList.get(i);
	// String PermissionId = AlbumDAOImpl.getInstance()
	// .findPermissionId(assetId);
	// if (PermissionId != null)
	// {
	// AlbumDAOImpl.getInstance().DeletePermission(
	// assetId);
	// ArrayList<String> perconstraintIdList = AlbumDAOImpl
	// .getInstance().findPerconstraintId(
	// PermissionId);
	// if (perconstraintIdList != null)
	// {
	// AlbumDAOImpl.getInstance()
	// .DeletePerconstraint(PermissionId);
	// }
	// }
	// }
	// }
	// }
	// return "";
	// }
	//
	// protected void onPostExecute(String result)
	// {
	// };
	// }.execute();
	// }

}
