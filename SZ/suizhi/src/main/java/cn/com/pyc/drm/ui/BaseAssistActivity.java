package cn.com.pyc.drm.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.jpush.android.api.JPushInterface;

/**
 * Activity基类帮助类,不包含检测剪贴板分享逻辑
 *
 * @author hudq
 */
public abstract class BaseAssistActivity extends Activity {

    private Dialog loadingBgDlg;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkDevice();
    }

    private void checkDevice() {
        if (!CommonUtil.isSdCardCanUsed()) {
            showToastLong(getString(R.string.disable_sd_card_not_download));
        }
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }

    public void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 通过类名启动Activity
     *
     * @param clz
     */
    public void openActivity(Class<?> clz) {
        openActivity(clz, null);
    }

    /**
     * 通过类名启动Activity，并且含有Bundle数据
     *
     * @param clz
     * @param bundle 传递的bundle数据
     */
    public void openActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 显示加载loading框,带背景
     */
    public void showBgLoading(String msg) {
        if (loadingBgDlg == null) {
            loadingBgDlg = new LoadingBgDialog(this, msg);
            loadingBgDlg.setCancelable(true); // true设置返回取消
            loadingBgDlg.setCanceledOnTouchOutside(false);
        }
        if (!loadingBgDlg.isShowing()) {
            loadingBgDlg.show();
        }
    }

    /**
     * 隐藏加载loading，带背景
     */
    public void hideBgLoading() {
        if (loadingBgDlg != null) {
            loadingBgDlg.dismiss();
            loadingBgDlg = null;
        }
    }

    /**
     * loading对话框，带背景
     *
     * @author qd
     */
    class LoadingBgDialog extends Dialog {

        private String content;

        private LoadingBgDialog(Context context, String content) {
            super(context, R.style.LoadBgDialog);
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
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.setCancelable(true); // true设置返回取消
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    /**
     * 隐藏加载进度框，不带背景
     */
    public void hideLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    /**
     * loading 加载框，不带背景的
     *
     * @author hudq
     */
    class LoadingDialog extends Dialog {

        private LoadingDialog(Context context) {
            super(context, R.style.LoadDialog);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            this.setContentView(R.layout.dialog_loading);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
        MusicHelp.showFloatView(this);
    }
}
