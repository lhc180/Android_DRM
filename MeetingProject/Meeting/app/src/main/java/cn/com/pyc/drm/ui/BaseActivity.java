package cn.com.pyc.drm.ui;

import com.lidroid.xutils.ViewUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.widget.ToastShow;
import cn.jpush.android.api.JPushInterface;

public abstract class BaseActivity extends Activity {

	public ToastShow tos;
	private Dialog loadingBgDlg;
	private Dialog loadingDialog;

	private String oldMsg;
	protected Toast toast = null;
	private long oneTime = 0;
	private long twoTime = 0;
	private LoadingIndicatorView loadingIndicatorDlg;
	public BaseActivity mBaseActivity ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		if (getWindow() != null)
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		ActicityManager.getInstance().add(this);
		tos = ToastShow.getInstances_();
		mBaseActivity=this;
	}

	/**
	 * 初始化控件
	 */
	protected abstract void init_View();

	/**
	 * 加载数据
	 */
	protected abstract void load_Data();

	/**
	 * 获取传递参数
	 */
	protected abstract void init_Value();

	/**
	 * 打印Tost
	 */
	public void showToast(int resId) {
		Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
	}

	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	// 解决Toast重复弹出，Toast单例模式
	public void showToast(Context context, String s) {
		if (toast == null) {
			toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
			toast.show();
			oneTime = System.currentTimeMillis();
		} else {
			twoTime = System.currentTimeMillis();
			if (s.equals(oldMsg)) {
				if (twoTime - oneTime > Toast.LENGTH_SHORT) {
					toast.show();
				}
			} else {
				oldMsg = s;
				toast.setText(s);
				toast.show();
			}
		}
		oneTime = twoTime;
	}

	/**
	 * 通过类名启动Activity
	 * @param class
	 */
	public void openActivity(Class<?> clz) {
		openActivity(clz, null);
	}

	/**
	 * 通过类名启动Activity，并且含有Bundle数据
	 * @param class
	 * @param bundle
	 *            传递的bundle数据
	 */
	public void openActivity(Class<?> clz, Bundle bundle) {
		Intent intent = new Intent(this, clz);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		startActivity(intent);
	}

	/**
	 * 显示加载loading框
	 */
	public void showBgLoading(String msg) {
		loadingBgDlg = new LoadingBgDialog(this, msg);
		loadingBgDlg.setCancelable(true); // true设置返回取消
		loadingBgDlg.setCanceledOnTouchOutside(false);
		loadingBgDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog_) {
				if (loadingBgDlg != null && loadingBgDlg.isShowing()) {
					loadingBgDlg.dismiss();
					loadingBgDlg = null;
				}
			}
		});
		loadingBgDlg.show();
	}

	/**
	 * 显示加载loading框,带背景
	 */
	public void showBgLoading() {
		showBgLoading(null);
	}

	/**
	 * 隐藏加载loading，带背景
	 */
	public void hideBgLoading() {
		if (loadingBgDlg != null) {
			loadingBgDlg.dismiss();
		}
	}

	/**
	 * loading对话框，带背景
	 * 
	 * @author qd
	 * 
	 */
	class LoadingBgDialog extends Dialog {

		String content = "";

		// Context context;

		public LoadingBgDialog(Context context, String content) {
			super(context, R.style.LoadBgDialog);
			// this.context = context;
			this.content = content;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			this.setContentView(R.layout.dialog_bgloading);
			TextView tvContent = (TextView) this.findViewById(R.id.tv_dialog);
			if (!TextUtils.isEmpty(content)) {
				tvContent.setText(content);
			} else {
				tvContent.setText(getString(R.string.load_ing));
			}
		}
	}

	/**
	 * 显示加载进度框，不带背景
	 */
	public void showLoading() {
		if (loadingDialog != null && loadingDialog.isShowing()) {
			loadingDialog.dismiss();
			loadingDialog = null;
		}
		loadingDialog = new LoadingDialog(this);
		loadingDialog.setCancelable(true); // true设置返回取消
		loadingDialog.setCanceledOnTouchOutside(false);
		loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog_) {
				if (loadingDialog != null && loadingDialog.isShowing()) {
					loadingDialog.dismiss();
					loadingDialog = null;
				}
			}
		});
		loadingDialog.show();
	}

	/**
	 * 隐藏加载进度框，不带背景
	 */
	public void hideLoading() {
		if (loadingDialog != null) {
			loadingDialog.dismiss();
		}
	}

	/**
	 * loading 加载框，不带背景的
	 * 
	 * @author hudq
	 * 
	 */
	class LoadingDialog extends Dialog {

		public LoadingDialog(Context context) {
			super(context, R.style.LoadDialog);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			this.setContentView(R.layout.dialog_loading);
		}
	}

	/**
	 * 显示加载loading框
	 */
	public void showIndicatorViewLoading() {
		loadingIndicatorDlg = new LoadingIndicatorView(this);
		loadingIndicatorDlg.setCancelable(true); // true设置返回取消
		loadingIndicatorDlg.setCanceledOnTouchOutside(false);
		loadingIndicatorDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog_) {
				if (loadingIndicatorDlg != null && loadingIndicatorDlg.isShowing()) {
					loadingIndicatorDlg.dismiss();
					loadingIndicatorDlg = null;
				}
			}
		});
		loadingIndicatorDlg.show();
	}

	/**
	 * 
	 * @Description: (加载框)
	 * @author 李巷阳
	 * @date 2016-7-22 上午10:45:37
	 */
	class LoadingIndicatorView extends Dialog {

		/**
		 * @param context
		 */
		public LoadingIndicatorView(Context context) {
			super(context, R.style.LoadDialog);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			this.setContentView(R.layout.loadingindicator_loading);
		}
	}

	/**
	 * 隐藏加载进度框，不带背景
	 */
	public void hideLoadingIndicatorView() {
		if (loadingIndicatorDlg != null) {
			loadingIndicatorDlg.dismiss();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		JPushInterface.onPause(this);
	}

}
