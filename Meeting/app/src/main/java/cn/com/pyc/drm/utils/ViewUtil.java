package cn.com.pyc.drm.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.UpdateVersionBean;
import cn.com.pyc.drm.dialog.ContentDialog;
import cn.com.pyc.drm.dialog.TVAnimDialog.OnTVAnimDialogClickListener;
import cn.com.pyc.drm.service.AppUpdateService;

/**
 * 控件工具处理
 * 
 * @author hudq
 * 
 */
public class ViewUtil {

	/**
	 * 获取控件宽
	 */
	public static int getWidth(View view) {
		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		return view.getMeasuredWidth();
	}

	/**
	 * 获取控件高
	 */
	public static int getHeight(View view) {
		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		return view.getMeasuredHeight();
	}

	/**
	 * 显示升级的对画框
	 * 
	 * @param mContext
	 * @param ov
	 * @param isShowDialog
	 */
	public static final void showUpdateDialog(final Activity mContext, Object ov, boolean isShowDialog) {
		// 提示对话框
		if (!isShowDialog) {
			return;
		}
		if (ov instanceof UpdateVersionBean) {
			final UpdateVersionBean version = (UpdateVersionBean) ov;

			AlertDialog.Builder builder = new Builder(mContext);
			View view = View.inflate(mContext.getApplicationContext(), R.layout.dialog_user, null);
			TextView tView = (TextView) view.findViewById(R.id.Cancellation);
			TextView tvSubView = (TextView) view.findViewById(R.id.subCancellation);
			tvSubView.setVisibility(View.VISIBLE);
			tView.setText(mContext.getString(R.string.ask_update_version_new));
			tvSubView.setText(mContext.getString(R.string.the_new_version, version.getVersion()));
			builder.setView(view);
			final AlertDialog dialogVersion = builder.create();
			dialogVersion.show();

			view.findViewById(R.id.confirm).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 升级下载
					if (dialogVersion.isShowing()) {
						dialogVersion.dismiss();
					}

					AppUpdateService.openAppUpdateService(mContext, version.getUrl(), version.getVersion());
				}
			});
			view.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (dialogVersion.isShowing()) {
						dialogVersion.dismiss();
					}
				}
			});
		}
	}

	/**
	 * 显示自定义内容的提示对话框
	 * 
	 * @param context
	 * @param content
	 *            提示框显示的内容
	 * @param l
	 */
	public static final void showContentDialog(Context context, String content, OnTVAnimDialogClickListener l) {
		ContentDialog dialog = new ContentDialog(context, content);
		dialog.setOnTVAnimDialogClickListener(l);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	/**
	 * 显示对话框（自定义标题、内容、确定按钮的文本显示）
	 * 
	 * @param context
	 * @param titleText
	 *            标题，默认文本“提示”
	 * @param contetnText
	 *            内容
	 * @param posBtnText
	 *            确定按钮文本，默认“确定”
	 * @param callBack
	 */
	public static final Dialog showCommonDialog(Context context, String titleText, String contetnText, String posBtnText, final DialogCallBack callBack) {
		final Dialog dialog = new Dialog(context, R.style.LoadBgDialog);
		View view = View.inflate(context, R.layout.dialog_common, null);
		Button confirm = (Button) view.findViewById(R.id.dialog_common_btn_positive);// 确定
		Button cancel = (Button) view.findViewById(R.id.dialog_common_btn_negative);// 取消
		TextView content = (TextView) view.findViewById(R.id.dialog_common_content);// 显示内容
		TextView title = (TextView) view.findViewById(R.id.dialog_common_title);// 显示标题

		// 设置对话框的宽度
		if (title.getLayoutParams() != null) {
			int width = DeviceUtils.getScreenSize(context).x;
			title.getLayoutParams().width = (int) (width * 0.75);
		}

		// 标题
		if (!TextUtils.isEmpty(titleText)) {
			title.setText(titleText);
		}
		// 内容
		if (!TextUtils.isEmpty(contetnText)) {
			content.setText(contetnText);
		}
		// 确定按钮
		if (!TextUtils.isEmpty(posBtnText)) {
			confirm.setText(posBtnText);
		}

		dialog.setContentView(view);

		confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (callBack != null) {
					callBack.onConfirm();
				}
				if (dialog != null)
					dialog.dismiss();
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialog != null)
					dialog.dismiss();
			}
		});
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		return dialog;
	}

	/**
	 * 提示app升级对话框
	 * 
	 * @param mContext
	 * @param ov
	 * @param isShowDialog
	 */
	public static final void showUpdateDialog(final Activity mContext, final UpdateVersionBean ov, boolean isShowDialog) {
		// 提示对话框
		if (!isShowDialog) {
			return;
		}
		AlertDialog.Builder builder = new Builder(mContext);
		View view = View.inflate(mContext.getApplicationContext(), R.layout.dialog_update_app, null);
		TextView content = (TextView) view.findViewById(R.id.dialog_update_content);
		content.setText(mContext.getString(R.string.version_update_new));
		// TextView tip = (TextView)
		// view.findViewById(R.id.dialog_update_tip);
		// tip.setText("");

		TextView title = (TextView) view.findViewById(R.id.dialog_update_title);// 显示标题
		title.setText(mContext.getString(R.string.version_the_new, ov.getVersion()));

		// 设置对话框的宽度
		if (title.getLayoutParams() != null) {
			int width = DeviceUtils.getScreenSize(mContext).x;
			title.getLayoutParams().width = (int) (width * 0.72);
		}

		builder.setView(view);
		final AlertDialog dialogVersion = builder.create();
		dialogVersion.setCanceledOnTouchOutside(false);
		dialogVersion.show();

		view.findViewById(R.id.dialog_update_now).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissDialog(dialogVersion);
				// 升级下载
				AppUpdateService.openAppUpdateService(mContext, ov.getUrl(), ov.getVersion());
			}
		});
		view.findViewById(R.id.dialog_update_later).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissDialog(dialogVersion);
			}
		});
	}

	private static void dismissDialog(Dialog dialog) {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
	}

	public interface DialogCallBack {
		void onConfirm();
	}

}
