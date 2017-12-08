package cn.com.pyc.drm.receiver;

import cn.com.pyc.drm.ui.WelcomeActivity;
import cn.com.pyc.drm.utils.DRMLog;
import cn.jpush.android.api.JPushInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * jppush
 * 
 * @author hudq
 * 
 */
public class JPushReceiver extends BroadcastReceiver {

	private final String TAG = "JPushReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		DRMLog.d(TAG, "onReceive - " + intent.getAction() + "\nextras: "
				+ printBundle(bundle));
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = bundle
					.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			DRMLog.d(TAG, "接收Registration Id : " + regId);
			// send the Registration Id to your server...

		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
				.getAction())) {
			DRMLog.d(
					TAG,
					"接收到推送下来的自定义消息: "
							+ bundle.getString(JPushInterface.EXTRA_MESSAGE));

		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
				.getAction())) {
			DRMLog.d(TAG, "接收到推送下来的通知");

		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
				.getAction())) {
			String msg = bundle.getString(JPushInterface.EXTRA_EXTRA);
			DRMLog.d(TAG, "用户点击打开了通知: " + msg);

			openApp(context);

		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent
				.getAction())) {
			DRMLog.d(
					TAG,
					"用户收到到RICH PUSH CALLBACK: "
							+ bundle.getString(JPushInterface.EXTRA_EXTRA));
			// 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
			// 打开一个网页等..

		} else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent
				.getAction())) {
			boolean connected = intent.getBooleanExtra(
					JPushInterface.EXTRA_CONNECTION_CHANGE, false);
			DRMLog.w(TAG, intent.getAction() + " connected state change to "
					+ connected);
		} else {
			DRMLog.d(TAG, "Unhandled intent - " + intent.getAction());
		}
	}

	private void openApp(Context context) {
		Intent intentNotification = new Intent(Intent.ACTION_MAIN);
		intentNotification.addCategory(Intent.CATEGORY_LAUNCHER);
		intentNotification.setClass(context, WelcomeActivity.class);
		intentNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		context.startActivity(intentNotification);
	}

	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}

}
