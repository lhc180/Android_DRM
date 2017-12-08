//package cn.com.pyc.drm.utils;
//
//import android.os.Handler;
//import android.os.Message;
//import android.text.TextUtils;
//import cn.com.pyc.drm.common.AppException;
//import cn.com.pyc.drm.utils.manager.ExecutorManager;
//
//import org.apache.http.NameValuePair;
//
//import java.util.List;
//
///**
// * 
// * @author hudq
// * 
// */
//
//@Deprecated
//public abstract class NetHelper
//{
//
//	private final int MSG_OK = 0x11;
//	private final int MSG_ERROR = 0x22;
//
//	private Handler handler = new Handler()
//	{
//		@Override
//		public void handleMessage(android.os.Message msg)
//		{
//			switch (msg.what)
//			{
//			case MSG_OK:
//			{
//				onSuccess((String) msg.obj);
//			}
//				break;
//			case MSG_ERROR:
//			{
//				onFailed((String) msg.obj);
//			}
//				break;
//			}
//			onCompete();
//		};
//	};
//
//	/**
//	 * 访问网络，登录注册设备
//	 * 
//	 * @param url
//	 * @param params
//	 */
//	public void requestNetWork(final String url, final List<NameValuePair> params)
//	{
//		onStart();
//		Thread thread = new Thread(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				String result = HttpUtil.postByHttpClient(url, params);
//				Message msg = Message.obtain();
//				msg.obj = result;
//				if (!TextUtils.equals("-1", result) 
//						&& !TextUtils.equals(String.valueOf(AppException.TYPE_HTTP_ERROR), result)
//						&& !TextUtils.equals(String.valueOf(AppException.TYPE_RUN), result) 
//						&& !TextUtils.equals(String.valueOf(AppException.TYPE_IO), result))
//				{
//					// 成功
//					msg.what = MSG_OK;
//				} else
//				{
//					msg.what = MSG_ERROR;
//				}
//				handler.sendMessage(msg);
//			}
//		});
//		ExecutorManager.getInstance().execute(thread);
//	}
//
//	public abstract void onStart();
//
//	public abstract void onSuccess(String result);
//
//	public abstract void onFailed(String error);
//
//	public abstract void onCompete();
//}
