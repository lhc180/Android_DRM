package cn.com.pyc.drm.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.VersionBean;
import cn.com.pyc.drm.dialog.ContentDialog;
import cn.com.pyc.drm.dialog.TVAnimDialog;
import cn.com.pyc.drm.dialog.TVAnimDialog.OnTVAnimDialogClickListener;
import cn.com.pyc.drm.service.AppUpdateService;
import cn.com.pyc.drm.utils.help.DownloadHelp;
import cn.com.pyc.drm.utils.help.MusicHelp;

/**
 * 控件工具处理
 */
public class ViewUtil {

    /**
     * 获取控件宽;wrap_content
     */
    public static int getWidth(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return view.getMeasuredWidth();
    }

    /**
     * 获取控件高,wrap_content时候
     */
    public static int getHeight(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return view.getMeasuredHeight();
    }

    /**
     * visible view,if this view is gone or invisible.
     *
     * @param view
     */
    public static void showWidget(View view) {
        if (view.getVisibility() == View.GONE
                || view.getVisibility() == View.INVISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * gone view,if this view is visible.
     *
     * @param view
     */
    public static void hideWidget(View view) {
        if (isVisible(view)) {
            view.setVisibility(View.GONE);
            if (view instanceof TextView) {
                ((TextView) view).setText(null);
            }
        }
    }

    /**
     * gone view with animation(fade out)，if this view is visible.
     *
     * @param view
     */
    public static void hideWidgetAnimator(final View view) {
        if (isVisible(view)) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(AnimationUtils
                            .loadAnimation(view.getContext(), android.R.anim.fade_out));
                    hideWidget(view);
                }
            }, 2000);
        }
    }

    /**
     * invisible view,if this view is visible.
     *
     * @param view
     */
    public static void inVisibleWidget(View view) {
        if (isVisible(view)) {
            view.setVisibility(View.INVISIBLE);
            if (view instanceof TextView) {
                ((TextView) view).setText(null);
            }
        }
    }

    /**
     * if this view is visible
     *
     * @param v
     * @return
     */
    public static boolean isVisible(View v) {
        return v.getVisibility() == View.VISIBLE;
    }

    /**
     * * 显示对画框(蓝色标题栏)
     *
     * @param mContext
     * @param content
     * @param subContent 二级文字显示内容
     * @param callBack
     */
    public static void showUserDialog(final Activity mContext,
                                      String content,
                                      String subContent,
                                      final DialogCallBack callBack) {
        final Dialog dialog = new Dialog(mContext, R.style.LoadBgDialog);
        View view = View.inflate(mContext.getApplicationContext(),
                R.layout.dialog_user, null);
        TextView tvView = (TextView) view.findViewById(R.id.dialog_user_lation);
        TextView tvSubView = (TextView) view
                .findViewById(R.id.dialog_user_sublation);
        TextView tvTitle = (TextView) view.findViewById(R.id.dialog_user_title);
        tvSubView.setVisibility(TextUtils.isEmpty(subContent) ? View.INVISIBLE
                : View.VISIBLE);
        tvView.setText(content);
        tvSubView.setText(subContent);
        dialog.setContentView(view);

        // 设置对话框的宽度
        if (tvTitle.getLayoutParams() != null) {
            int width = DeviceUtils.getScreenSize(mContext).x;
            tvTitle.getLayoutParams().width = (int) (width * 0.73);
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        view.findViewById(R.id.dialog_user_positive_text).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (callBack != null) {
                            callBack.onConfirm();
                        }
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
        view.findViewById(R.id.dialog_user_negative_text).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    /**
     * 显示自定义内容的对话框 ，（带关闭的动画）
     *
     * @param activity
     * @param content  提示框显示的内容
     * @param l
     */
    public static void showContentDialog(Activity activity, String content,
                                         OnTVAnimDialogClickListener l) {
        ContentDialog dialog = new ContentDialog(activity, content);
        dialog.setOnTVAnimDialogClickListener(l);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 显示退出对话框
     *
     * @param ac
     */
    public static void showContentDialog(final Activity ac) {

        showContentDialog(ac, "", new OnTVAnimDialogClickListener() {
            @Override
            public void onClick(int dialogId) {
                if (dialogId == TVAnimDialog.DIALOG_CONFIRM) {
                    DRMLog.i("Exit App！");
                    DownloadHelp.stopAllDownload(ac);
                    MusicHelp.release(ac);
                    ac.finish();
                    if (ac.getParent() != null) {
                        ac.getParent().finish();
                    }
                }
            }
        });
    }

    /**
     * 通用显示对话框（自定义标题、内容、确定按钮的显示文本）
     *
     * @param context
     * @param titleText   标题，默认文本“提示”
     * @param contentText 内容
     * @param posBtnText  确定按钮文本，默认“确定”
     * @param callBack
     */
//    public static Dialog showCommonDialog(Context context,
//                                          String titleText,
//                                          String contentText,
//                                          String posBtnText,
//                                          DialogCallBack callBack) {
//        return showCommonDialog(context, titleText, contentText, posBtnText, "", callBack);
//    }

    /**
     * 通用显示对话框（自定义标题、内容、按钮的显示文本）
     *
     * @param context
     * @param titleText
     * @param contentText
     * @param posBtnText
     * @param negBtnText
     * @param callBack
     * @return
     */
    public static Dialog showCommonDialog(Context context,
                                          String titleText,
                                          String contentText,
                                          String posBtnText,
                                          String negBtnText,
                                          final DialogCallBackPat callBack) {
        final Dialog dialog = new Dialog(context, R.style.LoadBgDialog);
        View view = View.inflate(context, R.layout.dialog_common, null);
        Button confirm = (Button) view
                .findViewById(R.id.dialog_common_btn_positive);// 确定
        Button cancel = (Button) view
                .findViewById(R.id.dialog_common_btn_negative);// 取消
        TextView content = (TextView) view
                .findViewById(R.id.dialog_common_content);// 显示内容
        TextView title = (TextView) view.findViewById(R.id.dialog_common_title);// 显示标题

        // 设置对话框的宽度
        if (title.getLayoutParams() != null) {
            int width = DeviceUtils.getScreenSize(context).x;
            title.getLayoutParams().width = (int) (width * 0.74);
        }

        // 标题
        if (!TextUtils.isEmpty(titleText)) {
            title.setText(titleText);
        }
        // 内容
        if (!TextUtils.isEmpty(contentText)) {
            content.setText(contentText);
        }
        // 确定按钮
        if (!TextUtils.isEmpty(posBtnText)) {
            confirm.setText(posBtnText);
        }
        // 取消按钮
        if (!TextUtils.isEmpty(negBtnText)) {
            cancel.setText(negBtnText);
        }

        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onConfirm();
                }
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onCancel();
                }
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        return dialog;
    }

    /**
     * 提示app升级对话框
     *
     * @param mContext
     * @param ov
     * @param isShowDialog
     */
    public static void showUpdateDialog(final Activity mContext,
                                        final VersionBean ov,
                                        boolean isShowDialog) {
        // 提示对话框
        if (!isShowDialog) {
            return;
        }
        final Dialog dialog = new Dialog(mContext, R.style.LoadBgDialog);
        View view = View.inflate(mContext, R.layout.dialog_update_app, null);
        TextView title = (TextView) view.findViewById(R.id.dialog_update_title);
        title.setText(mContext.getString(R.string.version_the_new,
                ov.getVersion()));

        // 设置对话框的宽度
        if (title.getLayoutParams() != null) {
            int width = DeviceUtils.getScreenSize(mContext).x;
            title.getLayoutParams().width = (int) (width * 0.74);
        }
        dialog.setContentView(view);

        view.findViewById(R.id.dialog_update_now).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        // 升级下载
                        AppUpdateService.openAppUpdateService(mContext,
                                ov.getUrl(), ov.getVersion());
                    }
                });
        view.findViewById(R.id.dialog_update_later).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 一个按钮操作的dialog
     *
     * @param context
     * @param titleText
     * @param contentText
     * @param posBtnText
     * @param callBack
     * @return
     */
    public static Dialog showSingleCommonDialog(Context context,
                                                String titleText,
                                                String contentText,
                                                String posBtnText,
                                                final DialogCallBack callBack) {
        final Dialog dialog = new Dialog(context, R.style.LoadBgDialog);
        View view = View.inflate(context, R.layout.dialog_single_common, null);
        Button confirm = (Button) view.findViewById(R.id.dialog_common_btn_positive);// 确定
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
        if (!TextUtils.isEmpty(contentText)) {
            content.setText(contentText);
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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        return dialog;
    }

    /**
     * 1.DialogCallBack <br/>
     * <p>
     * 2.DialogCallBackPat <br/>
     */
    public interface BaseDialogCallBack {
    }

    public interface DialogCallBack extends BaseDialogCallBack {
        void onConfirm();
    }

    public interface DialogCallBackPat extends BaseDialogCallBack {
        void onConfirm();

        void onCancel();
    }

}
