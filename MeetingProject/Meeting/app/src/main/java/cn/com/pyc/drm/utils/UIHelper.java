package cn.com.pyc.drm.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.AppException;
import cn.com.pyc.drm.utils.manager.SystemBarTintManager;

/**
 * UI操作工具包：<br/>
 * 封装程序UI相关的一些操作
 * 
 * @author hudq
 * 
 */
public class UIHelper {

	/**
	 * 显示自定义颜色状态栏 ,必须在setContentView之后调用<br\>
	 * 
	 * android 4.4之后生效
	 * 
	 * @param activity
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void showTintStatusBar(Activity activity) {
		// 顶部标题栏颜色 
		showTintStatusBar(activity,
				activity.getResources().getColor(R.color.title_bar_bg_color));
	}

	/**
	 * 显示自定义颜色状态栏 ,必须在setContentView之后调用
	 * 
	 * @param activity
	 * @param colorId
	 *            颜色资源id
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void showTintStatusBar(Activity activity, int colorId) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
				return;
			if (activity.getWindow() == null)
				return;
			// 透明状态栏
			activity.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			// 透明导航栏
			activity.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			ViewGroup mRootView = (ViewGroup) activity
					.findViewById(android.R.id.content);
			mRootView = (ViewGroup) mRootView.getChildAt(0);
			mRootView.setFitsSystemWindows(true);
			mRootView.setClipToPadding(true);

			SystemBarTintManager tintManager = new SystemBarTintManager(
					activity);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setNavigationBarTintEnabled(false);
			tintManager.setStatusBarTintColor(colorId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public static void setImmersionType(Activity activity){
//		/**
//		 * 沉浸式状态栏
//		 */
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			// 状态栏透明 需要在创建SystemBarTintManager 之前调用。
//			setTranslucentStatus(true,activity);
//		}
//
//		SystemBarTintManagerImmersion tintManager = new SystemBarTintManagerImmersion(activity);
//		tintManager.setStatusBarTintEnabled(true);
//		// 使StatusBarTintView 和 actionbar的颜色保持一致，风格统一。
//		tintManager.setStatusBarTintResource(0x000000000);
//		// 设置状态栏的文字颜色
//		tintManager.setStatusBarDarkMode(false, activity);
//	}
//	
//	
//	@TargetApi(19)
//	private static void setTranslucentStatus(boolean on,Activity activity) {
//		Window win = activity.getWindow();
//		WindowManager.LayoutParams winParams = win.getAttributes();
//		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//		if (on) {
//			winParams.flags |= bits;
//		} else {
//			winParams.flags &= ~bits;
//		}
//		win.setAttributes(winParams);
//	}
	
	
	/**
	 * 显示自定义颜色状态栏 ,必须在setContentView之后调用
	 * 
	 * @param activity
	 * @param drawable
	 *            drawable资源
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void showTintStatusBar(Activity activity, Drawable drawable) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
				return;
			if (activity.getWindow() == null)
				return;
			// 透明状态栏
			activity.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			// 透明导航栏
			activity.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			ViewGroup mRootView = (ViewGroup) activity
					.findViewById(android.R.id.content);
			mRootView = (ViewGroup) mRootView.getChildAt(0);
			mRootView.setFitsSystemWindows(true);
			mRootView.setClipToPadding(true);

			SystemBarTintManager tintManager = new SystemBarTintManager(
					activity);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setNavigationBarTintEnabled(false);
			tintManager.setStatusBarTintDrawable(drawable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * activity 进入开启的动画
	 * 
	 * @param activity
	 */
	public static void startInAnim(Activity activity) {
		// 进入动画
		activity.overridePendingTransition(R.anim.trans_x_in,
				R.anim.fade_out_scale);
	}

	/**
	 * activity退出返回的动画
	 * 
	 * @param activity
	 */
	public static void finishOutAnim(Activity activity) {
		// 退出动画
		activity.overridePendingTransition(R.anim.fade_in_scale,
				R.anim.trans_x_out);
	}

	/**
	 * 调用finish()销毁，带动画
	 * 
	 * @param activity
	 */
	public static void finishActivity(Activity activity) {
		activity.finish();
		finishOutAnim(activity);
	}

	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void showToast(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	/***
	 * 显示Toast信息
	 * 
	 * @param cont
	 * @param returnCode
	 */
	public static void ToastMessage(Context cont, int returnCode) {
		int msg = -1;
		switch (returnCode) {
		case DRMUtil.RETURN_CODE_LOGIN_FAIL:// 登录失败
			msg = R.string.msg_login_fail;
			break;
		case DRMUtil.RETURN_CODE_EMPTY:// 服务器返回空值
			String error = String.format(
					cont.getString(R.string.http_status_code_error),
					DRMUtil.RETURN_CODE_EMPTY);
			showToast(cont, error);
			return;
		case DRMUtil.RETURN_CODE_NET_DISCONNECTED:// 没有联网
			msg = R.string.net_disconnected;
			break;
		case DRMUtil.RETURN_USERNAME_EMPTY:// 用户名为空
			msg = R.string.msg_login_username_null;
			break;
		case DRMUtil.RETURN_PWD_EMPTY:// 密码为空
			msg = R.string.msg_login_pwd_null;
			break;
		case AppException.TYPE_HTTP_CODE:
			String err = cont.getString(R.string.http_status_code_error,
					returnCode);
			showToast(cont, err);
			return;
		case AppException.TYPE_HTTP_ERROR:
			msg = R.string.http_exception_error;
			break;
		case AppException.TYPE_SOCKET:
			msg = R.string.socket_exception_error;
			break;
		case AppException.TYPE_NETWORK:
			msg = R.string.network_not_connected;
			break;
		case AppException.TYPE_XML:
			msg = R.string.xml_parser_failed;
			break;
		case AppException.TYPE_JSON:
			msg = R.string.xml_parser_failed;
			break;
		case AppException.TYPE_IO:
			msg = R.string.io_exception_error;
			break;
		case AppException.TYPE_RUN:
			msg = R.string.app_run_code_error;
			break;
		case AppException.TYPE_FILE:
			msg = R.string.msg_file_not_find;
			break;
		case AppException.TYPE_FTP_CODE:
			msg = R.string.msg_ftp_error;
			break;
		case AppException.TYPE_OOM_CODE:
			msg = R.string.msg_oom_error;
			break;
		case AppException.TYPE_CIPHER:
			msg = R.string.msg_cipher_error;
			break;

		default:
			msg = R.string.app_run_code_error;
			break;
		}

		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

}
