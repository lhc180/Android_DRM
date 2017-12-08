package cn.com.pyc.drm.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.SPUtils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * 存储一下临时常量或值<br/>
 * <br/>
 * <br/>
 * 在APP的onCreate()开始时调用一下
 * 
 * @author hudq
 * 
 */
public final class Constant {

	public static int screenWidth;
	public static int screenHeight;
	public static String IMEI;
	// 默认值3
	public static int sTaskCount = 2;

	public static DisplayImageOptions options;

	private static String username;
	private static String password;

	private static String key_mobile;

	public static String no_verify = "0"; // 无验证
	public static String sms_verify = "1"; // 短信验证
	public static String attendee_verify = "2"; // 参会人验证
	public static String sign_verify = "3"; // 签到验证

	public static String return_true = "true";

	public static String return_false = "false";

	public static String version = "version";

	// 扫描历史记录 CaptureActivity 用到的常量
	public static final int SCANHISTORY_CODE = 0x2000;
	public static final String DECODED_CONTENT_KEY = "codedContent";
	public static final int REQUEST_CODE_SCAN = 0x79a;
	public static final int PARSE_BARCODE_FAIL = 300;
	public static final int PARSE_BARCODE_SUC = 200;

	// 判断是哪个界面跳船
	public static final String isForActivity = "isForActivity";
	public static final String isForLoginActivity = "ForLoginActivity";
	public static final String isForWelcomeActivity = "ForForWelcomeActivity";

	// /**
	// * 登录方式
	// *
	// * @author hudq
	// *
	// */
	// public static class LoginConfig
	// {
	// // 默认账号密码登录
	// public static int type = DrmPat.LOGIN_GENERAL;
	// }

	/***
	 * 初始化一些值，在APP的onCreate()开始时调用
	 * 
	 * @param context
	 */
	public static void init(Context context) {
		sTaskCount = DRMUtil.getTaskCount();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		IMEI = DeviceUtils.getIMEI(context);
		options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.transparent).showImageOnFail(R.drawable.transparent).showImageOnLoading(R.drawable.transparent).resetViewBeforeLoading(false)
		// default
				.cacheInMemory(false).cacheOnDisc(true) // 加载图片时会在磁盘中加载缓存
				// default
				.cacheOnDisk(true)
				// .displayer(
				// new RoundedBitmapDisplayer(CommonUtil.dip2px(context,
				// 3.0f)))// 是否设置为圆角，弧度为多少
				.displayer(new FadeInBitmapDisplayer(600)).bitmapConfig(Bitmap.Config.RGB_565) // default
				.build();
	}

	public static String getKey_mobile() {

		key_mobile = (String) SPUtils.get(DRMUtil.KEY_PHONE_NUMBER, "");

		return key_mobile;
	}

	public static void setKey_mobile(String key_mobile) {
		Constant.key_mobile = key_mobile;
	}

	/**
	 * 用户名(唯一性。相同即是同一个用户)
	 * 
	 * @return
	 */
	public static String getUserName() {
		username = (String) SPUtils.get(DRMUtil.KEY_VISIT_NAME, "");
		DRMLog.i("userName: " + username);
		return username;
	}

	public static void setUserName(String username) {
		Constant.username = username;
	}

	public static String getPassWord() {
		password = (String) SPUtils.get(DRMUtil.KEY_VISIT_PWD, "");
		return password;
	}

	public static void setPassWord(String password) {
		Constant.password = password;
	}

}
