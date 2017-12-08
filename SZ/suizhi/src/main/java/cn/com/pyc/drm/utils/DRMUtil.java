package cn.com.pyc.drm.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import java.util.HashSet;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.xml.OEX_Agreement;
import cn.com.pyc.drm.utils.manager.ActicityManager;

/**
 * App应用工具类
 */
public class DRMUtil
{

	/*********************************** 参数值的key ***************************************/

	public static final String KEY_SUPER_TOKEN = "token"; // 登录成功返回的token
	public static final String KEY_SUPER_NAME = "name"; // 登录成功返回的用户名（超级用户名）
	public static final String KEY_ACCOUNT_ID = "accountId"; // 登录成功返回的唯一用户id

	public static final String KEY_VISIT_NAME = "visitname"; // 保存的登录用户名(输入的名称)
	public static final String KEY_VISIT_PWD = "visitpwd"; // 保存的密码

	//public static final String KEY_REMEMBER_NAME = "username"; // 登录成功返回的用户名（超级用户名）,记住时才保存
	//public static final String KEY_REMEMBER_PWD = "password"; // 登录成功返回的密码,记住时才保存
    public static final String KEY_EQUIPMENT = "isRegisterEquipment"; // 设备是否注册成功
	public static final String KEY_APP_VERSION = "app_version"; // 程序版本号
	public static final String KEY_REPEAT_LOGIN = "isRepeatLogin"; // 是否是重复登陆
	public static final String KEY_SELECT_PRO_ID = "pro_id"; // 当前选择专辑的proId
	public static final String KEY_CHECK_DATA = "check_data"; // 是否检查过数据

	//update by hudaqiang
	public static final String KEY_WECHAT_ACCESS_TOKEN = "key_wechat_access_token"; // 微信access_token
	public static final String KEY_WECHAT_EXPIRES_IN = "key_wechat_expires_in"; // 微信access_token过期时间
	public static final String KEY_WECHAT_REFRESH_TOKEN = "key_wechat_refresh_token"; // 微信refresh_token
	public static final String KEY_WECHAT_UNIONID = "key_wechat_unionid"; // 微信unionid
	public static final String KEY_WECHAT_OPENID = "key_wechat_openid"; // 微信openid

	public static final String KEY_QQ_ACCESS_TOKEN = "key_qq_access_token"; // QQ的access_token
	public static final String KEY_QQ_OPENID = "key_qq_openid"; // QQ的openid
	public static final String KEY_QQ_EXPIRES_IN = "key_qq_expires_in"; // QQ的过期时间expires_in
	public static final String KEY_VISIT_TOKEN = "key_visit_token";
	public static final String KEY_VISIT_ACCOUNTID = "key_visit_accountid";

	/*********************************** 以上参数值的key ***************************************/

	/**
	 * 获取设备信息
	 *
	 * @return bundle
	 */
	public static Bundle getEquipmentInfos()
	{
		Bundle bundle = new Bundle();
		bundle.putString("username", Constant.getName()); // 必传
		bundle.putString("IMEI", Constant.IMEI); // 必传
		bundle.putString("opVersion", Build.VERSION.RELEASE); // 必传
		bundle.putString("SDKversion", "SDK " + Build.VERSION.SDK_INT); // 必传
		bundle.putString("system", "ANDROID");
		bundle.putString("board", Build.BOARD);
		bundle.putString("device", Build.DEVICE);
		bundle.putString("host", Build.HOST);
		bundle.putString("model", Build.MODEL);
		bundle.putString("serial", Build.SERIAL);
		bundle.putString("hardware", Build.HARDWARE);
		bundle.putString("manufacturer", Build.MANUFACTURER);
		bundle.putString("release", "android " + Build.VERSION.RELEASE);
		bundle.putString("product", Build.PRODUCT);
		bundle.putString("wifimac", DeviceUtils.getLocalMacAddress(App.getInstance()));
		return bundle;
	}

	/**
	 * 权限类型
	 */
	public static OEX_Agreement.PERMISSION_TYPE getType(String s)
	{
		switch (s) {
		case OEX_AGREEMENT_PERMISSION_TYPE_DISPLAY:
			return OEX_Agreement.PERMISSION_TYPE.DISPLAY;
		case OEX_AGREEMENT_PERMISSION_TYPE_PLAY:
			return OEX_Agreement.PERMISSION_TYPE.PLAY;
		case OEX_AGREEMENT_PERMISSION_TYPE_EXECUTE:
			return OEX_Agreement.PERMISSION_TYPE.EXECUTE;
		case OEX_AGREEMENT_PERMISSION_TYPE_EXPORT:
			return OEX_Agreement.PERMISSION_TYPE.EXPORT;
		case OEX_AGREEMENT_PERMISSION_TYPE_PRINT:
			return OEX_Agreement.PERMISSION_TYPE.PRINT;
		}
		return null;
	}

	/* 权限类型 */
	private static final String OEX_AGREEMENT_PERMISSION_TYPE_DISPLAY = "display";
	private static final String OEX_AGREEMENT_PERMISSION_TYPE_PLAY = "play";
	private static final String OEX_AGREEMENT_PERMISSION_TYPE_PRINT = "print";
	private static final String OEX_AGREEMENT_PERMISSION_TYPE_EXECUTE = "execute";
	private static final String OEX_AGREEMENT_PERMISSION_TYPE_EXPORT = "export";

	/**
	 * 标示：未登录时第一次进入APP,在登陆成功后=false <br/>
	 * 如果已经登录，保存密码的情况下，isFirstToMain值为true.
	 * */
	public static boolean isFirstToMain = true;
	// 插入数据
	public static boolean isInsertDRMData = true;


	// 判断跳转标示。
	//public static final String JUMP_FLAG = "Judging_jump";
	// 判断是否从主界面跳转
	//public static final String BY_MAIN_UI = "from_DownloadMainActivity";
	// 判断是否从pdf界面跳转
	//public static final String BY_MUPDF_UI = "from_MuPDFActivity";
	// 判断是否从music界面跳转
	//public static final String BY_MUSICHOME_UI = "from_MusicHomeActivity";
	// 判断是否从music专辑列表跳转
	//public static final String BY_MUSICALBUM_UI = "from_MusicAlbumActivity";





	// 主页面相关广播
	public static final String BROADCAST_PARSER_OVER_RELOAD = "com.drm.parser_over_reload";// 解析完成刷新界面
	public static final String BROADCAST_CLOSE_ACTIVITY = "com.drm.close_activity"; // 发送广播通知主界面，停止下载，清除数据，关闭主界面。
	public static final String BROADCAST_CLOSE_ACTIVITY2 = "com.drm.close_activity2"; // 发送广播通知主界面，停止下载，清除数据，关闭主界面。
	public static final String BROADCAST_CLEAR_DOWNLOADED = "com.drm.clear_downloaded"; // 清除缓存后，重发送广播，通知main页面重新加载内容
	public static final String BROADCAST_CLEAR_DOWNLOADED_ALBUM = "com.drm.clear_downloaded_album";// 删除单个专辑
	// 音乐进度
	public static final String BROADCAST_MUSIC_PROGRESS = "com.drm.receiver.Music_Progress";// 音乐播放器进度条显示。
	public static final String BROADCAST_MUSIC_OBTAIN_TIME = "com.drm.receiver.Music_Obtain_Time";// 第一次进入显示总时间
	// 音乐定时
	public static final String BROADCAST_MUSIC_TIMER = "com.drm.receiver.Music_Timer";
	public static final String BROADCAST_MUSIC_TIMER_END = "com.drm.receiver.Music_Timer_End";

//	public static final int RETURN_PWD_EMPTY = 994;
//	public static final int RETURN_USERNAME_EMPTY = 995;
//
//	public static final int RETURN_CODE_NET_DISCONNECTED = 996;
//	public static final int RETURN_CODE_EMPTY = 997;
//	public static final int RETURN_CODE_LOGIN_FAIL = 998;

	/* 标示文件解析analysis，正在解析值不为空,"=myProductId" */
	//public static String ANALYSIS_ID = null;
	// 记录当前album的proId
	//public static String CURRENT_ALBUM_ID = null;
	/* 记录点击的主页瀑布流产品的位置position, main click position */
	//public static int MCLICK_POSITION = -1;
	/* outline目录位置 */
	public static int OUTLINE_POSITION = 0;
	/* 正在播放的音乐专辑 */
	//public volatile static Album OBJECT_PLAYING = null;

	public static HashSet<String> sDocMimeTypesSet = new HashSet<String>()
	{
		private static final long serialVersionUID = 4671759073481773507L;

		{
			add("text/plain");
			add("text/plain");
			add("application/pdf");
			add("application/msword");
			add("application/vnd.ms-excel");
			add("application/vnd.ms-excel");
		}
	};

	/**
	 * 发送App异常崩溃报告
	 *
	 * @param cont
	 * @param crashReport
	 */
	@SuppressLint({ "NewApi", "InlinedApi" })
	public static void sendAppCrashReport(final Context cont,
			final String crashReport)
	{
		AlertDialog.Builder builder = new Builder(cont,
				android.R.style.Theme_Holo_Light);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_error);
		builder.setMessage(R.string.app_error_message);
		builder.setPositiveButton(R.string.submit_report,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						// 发送异常报告
						Intent i = new Intent(Intent.ACTION_SEND);
						// i.setType("text/plain"); //模拟器
						i.setType("message/rfc822"); // 真机
						i.putExtra(Intent.EXTRA_EMAIL,
								new String[] { "pyc@suizhi.net" });
						i.putExtra(Intent.EXTRA_SUBJECT,
								"DRM_Android客户端 - 错误报告");
						i.putExtra(Intent.EXTRA_TEXT, crashReport);
						cont.startActivity(Intent.createChooser(i, "发送错误报告"));
						// 退出
						ActicityManager.getInstance().exit();
					}
				});
		builder.setNegativeButton(R.string.okay,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						// 退出
						ActicityManager.getInstance().exit();
					}
				});
		builder.show();
	}

	/**
	 * 根据cpu的核心数，判断同时下载的任务数量
	 *
	 * @return
	 */
	public static int getTaskCount()
	{
		final int LOW_COUNT = 2;
		final int MIDDLE_COUNT = 3;
		final int HIGH_COUNT = 4;
		int cpu = DeviceUtils.getCPUCoresNum();
		if (cpu > 4) // 大于等于4核，小于等于8核
			return MIDDLE_COUNT;
		if (cpu > 8) // 大于8核
			return HIGH_COUNT;
		return LOW_COUNT;
	}

}