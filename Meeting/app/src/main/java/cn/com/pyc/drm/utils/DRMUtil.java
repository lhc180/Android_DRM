package cn.com.pyc.drm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.xml.OEX_Agreement;
import cn.com.pyc.drm.utils.manager.ActicityManager;

/**
 * App应用工具类
 * 
 */
public class DRMUtil
{

	/** 存储位置路径偏移 */
	public static final String getDefaultOffset()
	{
		return "Android/data/Meeting/";
	}

	/**
	 * 图片默认缓存路径
	 * 
	 * @return Android/data/DRM/img_cache
	 */
	public static final String getDefaultImageCachePath()
	{
		return getDefaultOffset() + "img_cache";
	}

	/**
	 * 应用sd卡缓存目录： SDCard/suizhi/
	 */
	public static final String getDefaultSDCardRootPath()
	{
		return Environment.getExternalStorageDirectory().toString()
				+ File.separator + "Meeting" + File.separator;
	}

	/**
	 * 获得下载地址
	 * 
	 * @param myProductId
	 * @return
	 */
	public static String getDownloadUrl(String myProductId)
	{
		String name = (String) SPUtils.get(KEY_VISIT_NAME, "");
		String pwd = (String) SPUtils.get(KEY_VISIT_PWD, "");
		return getProductsFtpDownloadUrl() + File.separator + myProductId
				+ File.separator + getDownloadMD5(name, pwd, myProductId)
				+ File.separator + "android";
	}

	/**
	 * 获得下载地址，（扫描登录）
	 * 
	 * @param myProductId
	 * @return
	 */
	public static String getDownloadUrlByScan(String myProductId)
	{
		String name = (String) SPUtils.get(KEY_VISIT_NAME, "");
		// String pwd = (String) SPUtils.getInfo4SP(KEY_VISIT_PWD);
		return getProductsDownloadUrlByScan() + File.separator + name
				+ File.separator + myProductId + File.separator + "android";
	}

	/**
	 * 获得下载地址，（扫描登录）
	 * 
	 * @param myProductId
	 * @return
	 */
	public static String getDownloadUrlByScan()
	{
		String hostName = (String) SPUtils.get(SCAN_FOR_HOST, "");
		String portName = (String) SPUtils.get(SCAN_FOR_PORT, "");
		String scanUrl = "http://" + hostName + ":" + portName
				+ "/Cloud/client/content/getFtpPath";
		return scanUrl;
	}

	/**
	 * 获取IP
	 */

	public static String getHostAndPort()
	{
		String hostName = (String) SPUtils.get(SCAN_FOR_HOST, "");
		String portName = (String) SPUtils.get(SCAN_FOR_PORT, "");
		String scanUrl = "http://" + hostName + ":" + portName + "/";
		return scanUrl;

	}

	/**
	 * 封面图片下载地址
	 * 
	 * @return
	 */
	public static String getPicIconUrl(String pic_url)
	{
		return getHost() + pic_url;
	}

	/**
	 * 获取设备信息
	 * 
	 * @return
	 */
	public static Bundle getEqumentInfos()
	{

		Bundle bundle = new Bundle();
		// bundle.putString(KEY_NAME, (String) SPUtils.get(KEY_VISIT_NAME, ""));
		bundle.putString(KEY_SYSTEM, "ANDROID");
		bundle.putString(KEY_BOARD, Build.BOARD);
		bundle.putString(KEY_CPU_ABI, Build.CPU_ABI);
		bundle.putString(KEY_CPU_ABI2, Build.CPU_ABI2);
		bundle.putString(KEY_DEVICE, Build.DEVICE);
		bundle.putString(KEY_HOST, Build.HOST);
		bundle.putString(KEY_MODEL, Build.MODEL);
		bundle.putString(KEY_SERIAL, Build.SERIAL);
		bundle.putString(KEY_HARDWARE, Build.HARDWARE);
		bundle.putString(KEY_MANUFACTURER, Build.MANUFACTURER);
		bundle.putString(KEY_RELEASE, "android " + Build.VERSION.RELEASE);
		bundle.putString(KEY_PRODUCT, Build.PRODUCT);
		bundle.putString(KEY_SDKVERSION, "SDK " + Build.VERSION.SDK_INT);
		bundle.putString(KEY_WIFIMAC,
				DeviceUtils.getLocalMacAddress(App.getInstance()));
		bundle.putString(KEY_DEVICETOKEN,
				(String) SPUtils.get(KEY_DEVICETOKEN, ""));
		bundle.putString(KEY_OPVERSION, Build.VERSION.RELEASE);
		bundle.putString(KEY_APP_VERSION,
				CommonUtil.getAppVersionName(App.getInstance()));
		bundle.putString(KEY_IMEI, Constant.IMEI);

		return bundle;
	}

	/**
	 * 通过页数索引得到相应的url
	 * 
	 * @param pageIndex
	 * @return
	 */
	@Deprecated
	public static String getProductListUrl(int pageIndex)
	{
		String KEY_NAME = (String) SPUtils.get(KEY_VISIT_NAME, "");
		String KEY_NAMEmd5 = getParamByMD5(
				(String) SPUtils.get(KEY_VISIT_NAME, ""),
				(String) SPUtils.get(KEY_VISIT_PWD, ""));
		String url = getProductsInfoUrl() + File.separator + KEY_NAME
				+ File.separator + pageIndex + File.separator + KEY_NAMEmd5;
		return url;
	}

	/**
	 * 根据策略(param=MD5(MD5(password+”{”+username+”}”)))生成指定的参数
	 * 
	 * @param username
	 * @param pwd
	 * @return
	 */
	public static String getParamByMD5(String username, String pwd)
	{

		return SecurityUtil.encodeByMD5(SecurityUtil.encodeByMD5(pwd + "{"
				+ username + "}"));
	}

	/**
	 * pssword1 = MD5(password+”{”+username+”}”) md5 = MD5(myProductId +
	 * “{”+password1+”}”)
	 * 
	 * @param username
	 * @param pwd
	 * @param myProductId
	 * @return
	 */
	public static String getDownloadMD5(String username, String pwd,
			String myProductId)
	{
		String pwd1 = SecurityUtil.encodeByMD5(pwd + "{" + username + "}");// pwd1
		String pwd2 = SecurityUtil.encodeByMD5(pwd1 + "{" + username + "}");
		return SecurityUtil.encodeByMD5(myProductId + "{" + pwd2 + "}");
	}

	/**
	 * 获取指定文件的MD5
	 * 
	 * @param file
	 * @return
	 */
	public static String getMD5ByFile(File file)
	{
		String value = null;
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(file);
			MappedByteBuffer byteBuffer = in.getChannel().map(
					FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != in)
			{
				try
				{
					in.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return value;
	}

	public static byte[] long2Bytes(long num)
	{
		byte[] byteNum = new byte[8];
		for (int ix = 0; ix < 8; ++ix)
		{
			int offset = 64 - (ix + 1) * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		return byteNum;
	}

	public static long bytes2Long(byte[] byteNum)
	{
		long num = 0;
		for (int ix = 0; ix < 8; ++ix)
		{
			num <<= 8;
			num |= (byteNum[ix] & 0xff);
		}
		return num;
	}

	/**
	 * 解析contentNames 将contentNames对应的字符串中的所有双引号之内的内容添加到一个list中
	 * 
	 * @param json
	 */
	public static ArrayList<String> parserJSONToArrayList(String json)
	{

		json.replaceAll("\\{", "");
		json.replaceAll("\\}", "");
		json.replaceAll("\"", "");

		ArrayList<String> contents = new ArrayList<String>();
		Pattern p = Pattern.compile("\"(.*?)\"");
		Matcher m = p.matcher(json);
		while (m.find())
		{
			contents.add(m.group().trim());
		}

		return contents;
	}

	// public static String getRemoteFileName(String downloadUri)
	// {
	// return downloadUri.substring(downloadUri.lastIndexOf("/") + 1,
	// downloadUri.length());
	// }

	/**
	 * 获得DRM文件下载的路径
	 * 
	 * @param name
	 * @return
	 */
	public static String getDownloadFilePath(String name)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH)
				.append(File.separator).append(name);
		return sb.toString();
	}

	// 权限类型
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
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_DISPLAY = "display";
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_PLAY = "play";
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_PRINT = "print";
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_EXECUTE = "execute";
	public static final String OEX_AGREEMENT_PERMISSION_TYPE_EXPORT = "export";

	// 实际部署服务器主机名
	public static final String getHost()
	{
		return "http://www.suizhi.net";
	}

	// 登录
	public static final String getLoginUrl()
	{
		// return getHost() + "/DRM/security/login/terminalLogin";
		// eg=http://www.suizhi.net/DRM/client/account/login?username=s001&password=20790144f555d7765911e46bc9fc35bc
		return getHost() + "/DRM/client/account/login";
	}

	// 设备信息
	public static final String getEquipmentInfoUrl()
	{
		// http://www.suizhi.net/DRM/account/userEquipment/setEquipmentInfo
		return getHost() + "/DRM/account/userEquipment/setEquipmentInfo";
	}

	// 产品list
	// eg=http://www.suizhi.net/DRM/client/product/list?username=s001&token=406810548&page=1&category=0
	public static final String getProductsInfoUrl()
	{
		// return getHost() + "/DRM/account/purchased/showProducts";
		return getHost() + "/DRM/client/product/list";
	}

	// 下载
	public static final String getProductsDownloadUrl()
	{
		return getHost() + "/DRM/account/downloadProductDevice2";
	}

	// 下载（扫描登录的时候使用）
	// eg=
	// http://video.suizhi.net:8654/Cloud/account/downloadProductDevice2/s001/3f9007b9-fc15-48f4-9bd8-c8a65078a7c6/android
	// "http://video.suizhi.net:8654/Cloud/account/downloadProductDevice2";
	public static final String getProductsDownloadUrlByScan()
	{
		return "http://video.suizhi.net:8654/Cloud/account/downloadProductDevice2";
	}

	// 获取ftp路径（扫描登录的时候使用）
	public static final String getProductsFtpDownloadUrlByScan()
	{
		return "http://video.suizhi.net:8654/Cloud/client/content/getFtpPath";
		// return "http://192.168.85.13:8657/Cloud/client/content/getFtpPath";
	}

	// 获取ftp路径
	public static final String getProductsFtpDownloadUrl()
	{
		return getHost() + "/DRM/client/content/getFtpPath";
	}

	/**
	 * 获取会议的名称。
	 * eg:http://video.suizhi.net:8654/Cloud/client/content/getDistributeInfo
	 * ?username=cpecc&id=37dcdedd-e091-4c49-8f6c-912557903d15
	 * 
	 * @return
	 */
	public static final String getMeetingNameUrl()
	{
		String hostName = (String) SPUtils.get(SCAN_FOR_HOST, "");
		String portName = (String) SPUtils.get(SCAN_FOR_PORT, "");
		String scanUrl = "http://" + hostName + ":" + portName
				+ "/Cloud/client/content/getDistributeInfo";
		DRMLog.d("getMeetingNameUrl: " + scanUrl);
		return scanUrl;
	}

	/** 视频和图书图片的缓存路径 */
	public final static String DEFAULT_SAVE_IMAGE_PATH = new StringBuffer()
			.append(Environment.getExternalStorageDirectory().toString())
			.append(File.separator).append(getDefaultOffset())
			.append("img_cache").toString();

	
	/** 文件的保存父路径*/
	public static String DEFAULT_SAVE_FATHER_FILE_PATH = null;

	
	/** drm文件解析保存路径 AppContext.username初始值为null，登陆后需重新初始化,注销后置为null */
	public static String DEFAULT_SAVE_FILE_PATH = null;

	/** drm文件下载保存路径 AppContext.username初始值为null，登陆后需重新初始化 ,注销后置为null */
	public static String DEFAULT_SAVE_FILE_DOWNLOAD_PATH = null;

	/** 音乐的缓存路径 */
	public final static String DEFAULT_HIGH_SPEED_FUZZY_PATH = new StringBuffer()
			.append(Environment.getExternalStorageDirectory().toString())
			.append(File.separator).append(getDefaultOffset()).append("fuzzy")
			.toString();

	/**
	 * 创建应用的缓存目录
	 */
	public static void createDirectory()
	{
		if (CommonUtil.isSdCardCanUsed())
		{
			FileUtils.createDirectory(getDefaultSDCardRootPath());
			FileUtils.createDirectory(DRMUtil.DEFAULT_SAVE_IMAGE_PATH);
			FileUtils.createDirectory(DRMUtil.DEFAULT_HIGH_SPEED_FUZZY_PATH);
		}
	}

	/**
	 * 登录之后创建用户名称的目录
	 */
	public static void createDirectoryAfterLogin()
	{
		if (CommonUtil.isSdCardCanUsed())
		{
			FileUtils.createDirectory(DRMUtil.DEFAULT_SAVE_FILE_PATH);
			FileUtils.createDirectory(DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH);
		}
	}

	/**
	 * 标示：未登录时，第一次进入APP,在登陆成功后=false <br/>
	 * 如果已经登录，保存密码的情况下，isForFirstTime值为true.
	 * */
	public static boolean isForFirstTime = true;
	// 插入数据
	public static boolean isInsertDRMData = true;
	// 判断是否从主界面跳转
	public static final String from_DownloadMain = "from_DownloadMainActivity";
	// 判断是否从pdf界面跳转
	public static final String from_MuPDFActivity = "from_MuPDFActivity";
	// 判断是否从music界面跳转
	public static final String from_MusicHome = "from_MusicHomeActivity";
	// 判断是否从music专辑列表跳转
	public static final String from_MusicAlbum = "from_MusicAlbumActivity";

	// staticactivityforeturn
	public static final int RefreshMyMeetingActivity = 20;
	// ----------------------------------- 参数的key值
	// ----------------------------------/
	public static final String KEY_PWD = "password";
	// 登录成功返回的token，单点登录时候使用。访问产品列表直接带上username和token,无需检查登录。
	public static final String KEY_LOGIN_TOKEN = "token";
	// 保存的用户名，账号登录时候，同username；扫码时候两者不相等
	public static final String KEY_VISIT_NAME = "visitname";
	public static final String KEY_VISIT_PWD = "visitpwd";

	// 扫描身份验证名
	public static final String KEY_SCAN_VERFY_NAME = "scan_verfy_name";
	// 手机号 
	public static final String KEY_PHONE_NUMBER = "phone_number";
	// 投票URL
	public static final String VOTE_URL = "vote_url";
	
	

	// 会议ID
	public static final String KEY_MEETINGID = "MeetingId";
	// 扫描获取的主机名字
	public static final String SCAN_FOR_HOST = "scan_url_for_host";
	// 扫描获取的端口
	public static final String SCAN_FOR_PORT = "scan_url_for_port";

	// 扫描获取的主机名字和端口
	public static final String PERMISSION_VERIFY_HOST = "Permission_verify_IP";

	// ----------------------------------- 参数的key值
	// ----------------------------------/

	public static final String KEY_SYSTEM = "system";
	public static final String KEY_NAME_Cache = "usernameCache";
	public static final String PwdIsSaved = "PwdIsSaved";
	public static final String KEY_BOARD = "board";
	public static final String KEY_IMEI = "IMEI";
	public static final String KEY_CPU_ABI = "CPU_ABI";
	public static final String KEY_CPU_ABI2 = "CPU_ABI2";
	public static final String KEY_DEVICE = "device";
	public static final String KEY_HOST = "host";
	public static final String KEY_MODEL = "model";
	public static final String KEY_SERIAL = "serial";
	public static final String KEY_HARDWARE = "hardware";
	public static final String KEY_MANUFACTURER = "manufacturer";
	public static final String KEY_RELEASE = "release";
	public static final String KEY_PRODUCT = "product";
	public static final String KEY_SDKVERSION = "SDKversion";
	public static final String KEY_WIFIMAC = "wifimac";
	public static final String KEY_DEVICETOKEN = "deviceToken";
	public static final String KEY_OPVERSION = "opVersion";
	public static final String KEY_EQUIPMENT = "isRegisterEquipment";
	public static final String KEY_APP_VERSION = "app_version";

	// 主页面相关广播
	public static final String BROADCAST_CLOSE_ACTIVITY = "com.main.broadcast.close_activity";
	public static final String BROADCAST_RELOAD_HOMEITEM = "com.main.broadcast.reload_home_item";
	public static final String BROADCAST_CLEAR_DOWNLOADED = "com.main.broadcast.clear_downloaded";

	// 音乐广播
	public static final String BROADCAST_MUSIC_PROGRESS = "com.neter.broadcast.receiver.Music_Progress";// 音乐播放器进度条显示。
	public static final String BROADCAST_MUSIC_OBTAIN_TIME = "com.neter.broadcast.receiver.Music_Obtain_Time";// 第一次进入显示总时间
	public static final String BROADCAST_MUSIC_STATUSBAR = "com.neter.broadcast.receiver.Music_Statusbar";// 音乐状态栏notification广播
	public static final String BROADCAST_MUSIC_Close_Music = "com.neter.broadcast.receiver.close_music";// 关闭音乐

	// 关闭登陆界面
	public static final String BROADCAST_CLEAR_LOGIN_ACTIVITY = "com.main.broadcast.clear_login_activity";
	// 关闭我的会议界面
	public static final String BROADCAST_CLEAR_MYMEETING_ACTIVITY = "com.main.broadcast.clear_mymeeting_activity";

	
	
	public static final int RETURN_PWD_EMPTY = 994;
	public static final int RETURN_USERNAME_EMPTY = 995;

	public static final int RETURN_CODE_NET_DISCONNECTED = 996;
	public static final int RETURN_CODE_EMPTY = 997;
	public static final int RETURN_CODE_LOGIN_FAIL = 998;

	/** 标示文件解析，正在解析值不为空=myProductId */
	public static String Is_analytic = "";

	/** 记录点击的主页瀑布流产品的位置position */
	public static int positionOld;

	/** outline位置 */
	public static int OutlinePosition = 0;

	/** 正在播放的专辑 */
	public volatile static Album Music_Album_Playing = null;

	public static HashSet<String> sDocMimeTypesSet = new HashSet<String>()
	{
		/**
		 * 
		 */
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
		int cpu = DeviceUtils.getCPUCoresNum();
		if (cpu > 4) return 4;
		if (cpu > 8) return 5;
		return 2;
	}

}