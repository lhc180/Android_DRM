package cn.com.pyc.drm.utils.manager;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import cn.com.pyc.drm.bean.UpdateVersionBean;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.model.DownloadInfo;
import cn.com.pyc.drm.model.db.bean.Downdata;
import cn.com.pyc.drm.model.db.practice.DowndataDAOImpl;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.ViewUtil;

/**
 * 检查版本更新
 * 
 * @author hudq
 * 
 */
public class XMLParserVersionManager
{

	private static final String checkVersionUrl = "http://ftp.suizhi.net:8080/androidMeeting/version.xml";

	private static XMLParserVersionManager instance;

	private AsyncTask<Void, Void, UpdateVersionBean> mUpdateTask;

	private Handler mHandler;

	public XMLParserVersionManager() {
		mHandler = new Handler(Looper.getMainLooper());
	}


	public static XMLParserVersionManager getInstance()
	{
		if (instance == null)
		{
			instance = new XMLParserVersionManager();
		}
		return instance;
	}

	/**
	 * CheckUpdateTask是否在running。
	 * 
	 * @return
	 */
	public boolean isCheckUpdateTaskRunning()
	{
		return mUpdateTask != null && mUpdateTask.getStatus() == AsyncTask.Status.RUNNING;
	}

	/**
	 * 检查更新
	 * 
	 * @param l
	 */
	public void checkUpdate(final Activity ctx,final boolean isStop, final OnCheckResultListener l)
	{
		if (isCheckUpdateTaskRunning())
		{
			shutDownCheckTask();
		}

		mUpdateTask = new AsyncTask<Void, Void, UpdateVersionBean>()
		{

			@Override
			protected UpdateVersionBean doInBackground(Void... params)
			{
				if (isCancelled())
				{
					return null;
				}
				InputStream in = requestNetWorkForXML();
				return xmlParserVersionInfo(in);
			}

			@Override
			protected void onPostExecute(UpdateVersionBean result)
			{
				if (result == null)
					return;
				String version = result.getVersion();
				DRMLog.i("Version = " + version);
				if (TextUtils.isEmpty(version))
					return;
				int r = CommonUtil.getAppVersionName(ctx).compareTo(version);
				DRMLog.i("比较版本结果 r = " + r);
				if (l != null)
				{
					// 当前版本比服务器上版本低r<0,则提示升级
					l.onResult(r < 0, result);
					if(r<0){
						final UpdateVersionBean o = (UpdateVersionBean) result;
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (!isStop) ViewUtil.showUpdateDialog(ctx, o, !isStop);
							}
						}, 2000);
					}
				}
			}
		};
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			mUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else
		{
			mUpdateTask.execute();
		}
	}



	/**
	 * 检查更新
	 *
	 * @param l
	 */
	public void checkUpdate(final Activity ctx, final OnCheckResultListener l)
	{
		if (isCheckUpdateTaskRunning())
		{
			shutDownCheckTask();
		}

		mUpdateTask = new AsyncTask<Void, Void, UpdateVersionBean>()
		{

			@Override
			protected UpdateVersionBean doInBackground(Void... params)
			{
				if (isCancelled())
				{
					return null;
				}
				InputStream in = requestNetWorkForXML();
				return xmlParserVersionInfo(in);
			}

			@Override
			protected void onPostExecute(UpdateVersionBean result)
			{
				if (result == null)
					return;
				String version = result.getVersion();
				DRMLog.i("Version = " + version);
				if (TextUtils.isEmpty(version))
					return;
				int r = CommonUtil.getAppVersionName(ctx).compareTo(version);
				DRMLog.i("比较版本结果 r = " + r);
				if (l != null)
				{
					// 当前版本比服务器上版本低r<0,则提示升级
					l.onResult(r < 0, result);
				}
			}
		};
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			mUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else
		{
			mUpdateTask.execute();
		}
	}
	/**
	 * 关闭检查更新
	 */
	public void shutDownCheckTask()
	{
		if (mUpdateTask != null)
		{
			mUpdateTask.cancel(true);
			mUpdateTask = null;
		}
	}

	/**
	 * 解析版本信息
	 * 
	 * @return
	 */
	private UpdateVersionBean xmlParserVersionInfo(InputStream in)
	{
		if (in == null)
			return null;
		UpdateVersionBean info = new UpdateVersionBean();
		try
		{
			// xml解析器
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in, "UTF-8");
			// 获取解析事件的类型
			int eventType = parser.getEventType();
			// 只要没有解析到文档的结尾就继续往下解析
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				switch (eventType)
				{
				case XmlPullParser.START_DOCUMENT:
					DRMLog.d("start document");
					break;
				case XmlPullParser.START_TAG:// 开始节点
					if (parser.getName().equalsIgnoreCase("info"))
					{

					} else if (parser.getName().equalsIgnoreCase("version"))
					{

						String version = parser.nextText();
						info.setVersion(version);

					} else if (parser.getName().equalsIgnoreCase("url"))
					{

						String url = parser.nextText();
						info.setUrl(url);
					}
					break;
				case XmlPullParser.END_TAG:// 结束节点
					if (parser.getName().equalsIgnoreCase("info"))
					{

					}
					break;
				}
				// 继续往下解析
				eventType = parser.next();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (in != null)
				try
				{
					in.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
		}

		return info;
	}

	private InputStream requestNetWorkForXML()
	{
		try
		{
			HttpURLConnection con = (HttpURLConnection) new URL(checkVersionUrl).openConnection();
			con.setConnectTimeout(30 * 1000);
			con.setRequestMethod("POST");
			int code = con.getResponseCode();
			if (code == HttpURLConnection.HTTP_OK)
			{
				return con.getInputStream();
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 检查更新的回调
	 * 
	 * @author hudq
	 * 
	 */
	public interface OnCheckResultListener
	{
		public void onResult(boolean hasNewVersion, Object result);
	}

	/**
	 * 检查已下载数据记录的回调
	 * 
	 * @author hudq
	 * 
	 */
	public interface OnCheckDownloadDataListener
	{
		/**
		 * 
		 *            only single record data value is true; otherwise false.
		 * @param position
		 *            数据源，当数据表中的进度记录多余一条时，数据源 is count，info.size()。
		 */
		public void onExist(boolean isSingelData, int position, Object obj);
	}

	/**
	 * 检测数据表中是否存在未完成下载的数据
	 * 
	 * @param oNetList
	 * @param l
	 */
	public void checkExistDownloadData(List<DownloadInfo> oNetList, OnCheckDownloadDataListener l)
	{
		// 调试使用
		if (App.Config.DEVELOPER_MODE)
		{
			List<Downdata> mDatas = DowndataDAOImpl.getInstance().findAllDowndata();
			DRMLog.e("checkExistDownloadData", "Downdata.size() = " + mDatas.size());
		}

		// 获取保存的版本号
		String mSaveVersion = (String) SPUtils.get(DRMUtil.KEY_APP_VERSION, "");
		// 当前app版本号
		String mCurVersion = CommonUtil.getAppVersionName(App.getInstance());
		// 版本号不等，则版本变化了！
		if (!TextUtils.equals(mSaveVersion, mCurVersion))
		{
			// 1.查询进度数据库，查看是否有未下载完成的记录
			// 2.如果有记录，记录=1，校验数据正确，提示下载并确定开始下载；记录>1,仅提示用户有未完成的下载任务。

			// 保存版本号
			SPUtils.save(DRMUtil.KEY_APP_VERSION, mCurVersion);

			List<Downdata> mDatas = DowndataDAOImpl.getInstance().findAllDowndata();
			if (mDatas.isEmpty())
			{
				return;
			}
			if (mDatas.size() == 1)
			{
				String myProId = mDatas.get(0).getMyProduct_id();
				int count = oNetList.size();
				for (int i = 0; i < count; i++)
				{
					DownloadInfo o = oNetList.get(i);
					if (TextUtils.equals(myProId, o.getMyProId()))
					{
						// 找到一条数据
						if (l != null)
						{
							l.onExist(true, i, o);
						}
						break;
					}
				}
			} else if (mDatas.size() > 1)
			{
				if (l != null)
				{
					l.onExist(false, 0, mDatas.size());
				}
			}
		}
	}

}
