package cn.com.pyc.drm.service;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.event.UpdateBarEvent;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DownloadUtils;
import de.greenrobot.event.EventBus;

/**
 * 下载更新服务
 * 
 * @author hudaqiang
 * 
 */
public class AppUpdateService extends Service
{

	/** 下载apk的名称 */
	private final String APK_NAME =  "meeting";

	/** apk下载的地址。 */
	public static final String DOWNLOAD_URL = "download_url";
	/** 新的apk版本号。 */
	public static final String NEW_VERSION = "new_version";
	private final int mNotificationId = 100;
	private NotificationManager mNotificationManager = null;
	private Notification mNotification = null;
	private PendingIntent mPendingIntent = null;
	private int mProgress = 0;
	private String updateVersion;
	private String mDownloadUrl;

	private File destDir = null;
	private File destFile = null;

	private static final int DOWNLOAD_FAIL = -1;
	private static final int DOWNLOAD_SUCCESS = 0;
	private Handler mHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case DOWNLOAD_SUCCESS:
				Toast.makeText(getApplicationContext(), "下载成功~", Toast.LENGTH_SHORT).show();
				install(destFile);
				break;
			case DOWNLOAD_FAIL:
				Toast.makeText(getApplicationContext(), "下载失败~", Toast.LENGTH_SHORT).show();
				mNotificationManager.cancel(mNotificationId);
				break;
			default:
				break;
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent != null)
		{
			mDownloadUrl = intent.getStringExtra(DOWNLOAD_URL);
			updateVersion = intent.getStringExtra(NEW_VERSION);
			DRMLog.d("mDownloadUrl=" + mDownloadUrl);
			DRMLog.d("updateVersion=" + updateVersion);
		} else
		{
			// 下载路径为空
			Toast.makeText(getApplicationContext(), "下载地址不可用", Toast.LENGTH_SHORT).show();
			stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}
		if (!CommonUtil.isSdCardCanUsed())
		{
			Toast.makeText(getApplicationContext(), "SD卡不可用", Toast.LENGTH_SHORT).show();
			stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}
		// apk下载保存地址
		destDir = new File(DRMUtil.getDefaultSDCardRootPath());
		if (!destDir.exists())
			destDir.mkdirs();
		if (destDir.exists())
		{
			File destFile = new File(destDir.getPath(), APK_NAME + updateVersion + ".apk");
			if (destFile.exists() && destFile.isFile() && checkApkFile(destFile.getPath()))
			{
				install(destFile);
				stopSelf();
				return super.onStartCommand(intent, flags, startId);
			}
		}

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotification = new Notification();

		mNotification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.notification_app_update);

		Intent completingIntent = new Intent();
		completingIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		completingIntent.setClass(getApplication().getApplicationContext(), AppUpdateService.class);

		mPendingIntent = PendingIntent.getActivity(AppUpdateService.this, R.string.app_name, completingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mNotification.icon = R.drawable.ic_menu_notification;
		mNotification.tickerText = "开始下载...";
		mNotification.contentIntent = mPendingIntent;
		mNotification.contentView.setProgressBar(R.id.app_upgrade_progressbar, 100, 0, false);
		mNotification.contentView.setTextViewText(R.id.app_upgrade_title, getString(R.string.app_name) + updateVersion);
		mNotification.contentView.setTextViewText(R.id.app_upgrade_progresstext, "0%");
		mNotificationManager.cancel(mNotificationId);
		mNotificationManager.notify(mNotificationId, mNotification);
		// 通知设置页面的progressbar显示
		EventBus.getDefault().post(new UpdateBarEvent(true));
		new UpgradeTask().execute();
		return super.onStartCommand(intent, flags, startId);
	}

	class UpgradeTask extends AsyncTask<Object, Object, Object>
	{

		@Override
		protected Object doInBackground(Object... params)
		{

			destFile = new File(destDir.getPath(), APK_NAME + updateVersion + ".apk");
			if (destFile.exists() && destFile.isFile() && checkApkFile(destFile.getPath()))
			{
				install(destFile);
			} else
			{
				try
				{
					DownloadUtils.download(mDownloadUrl, destFile, false, downloadListener);
				} catch (Exception e)
				{
					Message msg = mHandler.obtainMessage();
					msg.what = DOWNLOAD_FAIL;
					mHandler.sendMessage(msg);
					e.printStackTrace();
				}
			}

			stopSelf();
			return null;
		}
	}

	private DownloadUtils.DownloadListener downloadListener = new DownloadUtils.DownloadListener()
	{
		@Override
		public void downloading(int progress)
		{
			if (progress > mProgress)
			{
				mNotification.contentView.setProgressBar(R.id.app_upgrade_progressbar, 100, progress, false);
				mNotification.contentView.setTextViewText(R.id.app_upgrade_progresstext, progress + "%");
				mNotificationManager.notify(mNotificationId, mNotification);
			}
			mProgress = progress;
		}

		@Override
		public void downloaded()
		{
			mPendingIntent = null;
			mPendingIntent = PendingIntent.getActivity(AppUpdateService.this, R.string.app_name, installIntent(destFile), PendingIntent.FLAG_UPDATE_CURRENT);
			mNotification.contentView.setViewVisibility(R.id.app_upgrade_progressblock, View.VISIBLE);
			mNotification.contentView.setProgressBar(R.id.app_upgrade_progressbar, 100, 100, false);
			mNotification.defaults = Notification.DEFAULT_SOUND;
			mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
			mNotification.contentIntent = mPendingIntent;
			mNotification.contentView.setTextViewText(R.id.app_upgrade_progresstext, "下载完成，点击安装");
			if (destFile.exists() && destFile.isFile() && checkApkFile(destFile.getPath()))
			{
				Message msg = mHandler.obtainMessage();
				msg.what = DOWNLOAD_SUCCESS;
				mHandler.sendMessage(msg);
			}
			mNotificationManager.cancel(mNotificationId);
			mNotificationManager.notify(mNotificationId, mNotification);
			// 下载完成，通知设置页面的bar隐藏
			EventBus.getDefault().post(new UpdateBarEvent(false));
		}
	};

	/** 检查未安装的apkfile */
	public boolean checkApkFile(String apkFilePath)
	{
		boolean result = false;
		try
		{
			PackageManager pManager = getPackageManager();
			PackageInfo pInfo = pManager.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES);
			if (pInfo == null)
			{
				result = false;
			} else
			{
				result = true;
			}
		} catch (Exception e)
		{
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	public void install(File apkFile)
	{
		Uri uri = Uri.fromFile(apkFile);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		startActivity(intent);
	}

	private Intent installIntent(File apkFile)
	{
		Uri uri = Uri.fromFile(apkFile);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		return intent;
	}

	// 安装新的apk
	protected void installNewApk(File file)
	{
		/**
		 * <action android:name="android.intent.action.VIEW" /> <category
		 * android:name="android.intent.category.DEFAULT" /> <data
		 * android:scheme="content" /> <data android:scheme="file" /> <data
		 * android:mimeType="application/vnd.android.package-archive" />
		 */
		// 调用系统的安装程序
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		startActivity(intent);
	}

	/**
	 * 开启更新服务
	 * 
	 * @param ctx
	 * @param downloadUrl
	 *            下载地址
	 * @param newVersion
	 *            新版本号
	 */
	public static final void openAppUpdateService(Context ctx, String downloadUrl, String newVersion)
	{
		Intent intent = new Intent(ctx, AppUpdateService.class);
		intent.putExtra(AppUpdateService.DOWNLOAD_URL, downloadUrl);
		intent.putExtra(AppUpdateService.NEW_VERSION, newVersion);
		ctx.startService(intent);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (mHandler != null)
		{
			mHandler.removeMessages(DOWNLOAD_FAIL);
			mHandler.removeMessages(DOWNLOAD_SUCCESS);
			mHandler = null;
		}
	}

}
