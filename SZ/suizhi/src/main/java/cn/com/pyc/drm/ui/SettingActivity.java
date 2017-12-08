package cn.com.pyc.drm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.lang.ref.WeakReference;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.VersionBean;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.bean.event.UpdateBarEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.MusicMode;
import cn.com.pyc.drm.db.manager.DownDataPatDBManager;
import cn.com.pyc.drm.service.AppUpdateService;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.FileUtils;
import cn.com.pyc.drm.utils.FormatUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.PathUtil;
import cn.com.pyc.drm.utils.Util_;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.ViewUtil.DialogCallBack;
import cn.com.pyc.drm.utils.help.DRMDBHelper;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.com.pyc.drm.utils.help.ProgressHelp;
import cn.com.pyc.drm.utils.help.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.ExecutorManager;
import cn.com.pyc.drm.utils.manager.ShareMomentEngine;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager.OnCheckResultListener;
import de.greenrobot.event.EventBus;

/**
 * 用户中心
 *
 * @author update by hudq
 */
public class SettingActivity extends BaseActivity implements OnClickListener {

    private static final int MSG_GET_CACHE = 0x33;
    private static final int MSG_CLEAR_CACHE = 0x35;

    private View mCheckUpdate;
    private View mClearLayout;
    private TextView tv_cache;
    private TextView tv_version;
    private AVLoadingIndicatorView mProgressWheel;
    private ImageView imgDot;
    private TextView tv_account;

    private String saveFilePath;
    private String downloadFilePath;
    private Handler mHandler = new MyHandler(this);

    /**
     * 接收页面通知事件监听
     */
    public void onEventMainThread(UpdateBarEvent event) {
        if (event.getType() == BaseEvent.Type.UPDATE_SETTING_BAR &&
                mProgressWheel != null && mCheckUpdate != null) {
            mCheckUpdate.setEnabled(!event.isShow());
            mProgressWheel.setVisibility(event.isShow() ? View.VISIBLE : View.GONE);
        } else if (event.getType() == BaseEvent.Type.UPDATE_SETTING_CACHE) {
            if (Util_.checkLogin() && tv_cache != null) {
                loadCache();
            }
        }
    }

    //接收主页面切换通知重新加载缓存大小
    public void onEventMainThread(ConductUIEvent event) {
        if (event.getType() == BaseEvent.Type.UPDATE_SETTING_CACHE) {
            if (Util_.checkLogin() && tv_cache != null) {
                loadCache();
            }
        }
        if (event.getType() == BaseEvent.Type.UI_SETTING_UPDATE) {
            checkLoginState();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ActicityManager.getInstance().add(this);
        getValue();
        initView();
        loadData();
        loadCheckVersion(false);
    }

    @Override
    protected void getValue() {
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkLoginState();
    }

    @Override
    protected void loadData() {
        if (CommonUtil.isServiceRunning(this, AppUpdateService.class.getName())) {
            mProgressWheel.setVisibility(View.VISIBLE);
            mCheckUpdate.setEnabled(false);
        }
        String currentVersionName = CommonUtil.getAppVersionName(this);
        tv_version.setText(getString(R.string.cur_version_name, currentVersionName));
        checkLoginState();
    }


    private void checkLoginState() {
        if (Util_.checkLogin()) {
            //设置账号显示
            tv_account.setText(Constant.getLoginName());
            findViewById(R.id.btn_loginout).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_loginout).setOnClickListener(this);
            loadCache();
        } else {
            tv_account.setText("未登录");
            findViewById(R.id.mine_account).setOnClickListener(this);
            findViewById(R.id.btn_loginout).setVisibility(View.GONE);
            tv_cache.setText("0KB");
        }
    }

    // 获取用户下载的文件缓存的大小
    private void loadCache() {
        saveFilePath = PathUtil.getFilePrefixPath();
        downloadFilePath = PathUtil.getDRMPrefixPath();
        ExecutorManager.getInstance().execute(new LoadCacheThread(this));
    }

    @Override
    protected void initView() {
        findViewById(R.id.back_img).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.title_tv)).setText(getString(R.string.user_center));
        tv_version = (TextView) findViewById(R.id.tv_version);// 版本号控制
        tv_cache = (TextView) findViewById(R.id.tv_cache);
        tv_account = (TextView) findViewById(R.id.tv_account);
        mProgressWheel = (AVLoadingIndicatorView) findViewById(R.id.progress_wheel);
        imgDot = (ImageView) findViewById(R.id.iv_dot);
        mClearLayout = findViewById(R.id.clear_storager);
        mClearLayout.setOnClickListener(this);
        mClearLayout.setEnabled(true);
        mCheckUpdate = findViewById(R.id.check_update);
        mCheckUpdate.setOnClickListener(this);
        findViewById(R.id.about_us_page).setOnClickListener(this);
        //findViewById(R.id.back_img).setOnClickListener(this);
        //findViewById(R.id.sweep_coder).setOnClickListener(this);
    }

    private static class LoadCacheThread implements Runnable {
        private WeakReference<SettingActivity> reference;

        private LoadCacheThread(SettingActivity context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void run() {
            if (reference == null) return;
            SettingActivity activity = reference.get();
            if (activity == null) return;
            try {
                if (activity.saveFilePath == null
                        || activity.downloadFilePath == null) return;
                String fileSize = "0KB";
                File analyticalFile = new File(activity.saveFilePath);
                File downloadFile = new File(activity.downloadFilePath);
                long dirSize = FileUtils.getDirSize(analyticalFile) + FileUtils.getDirSize
                        (downloadFile);
                if (dirSize > 0) {
                    fileSize = FormatUtil.formatSize(dirSize);
                }
                Message message = Message.obtain();
                message.what = MSG_GET_CACHE;
                message.obj = fileSize;
                activity.mHandler.sendMessageDelayed(message, 200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.back_img:
//                finishUI();
//                break;
            case R.id.clear_storager:
                clearCache();
                break;
            case R.id.check_update:
                checkVersionInfo();
                break;
            case R.id.about_us_page:
                openActivity(SZInfoActivity.class);
                UIHelper.startInAnim(this);
                break;
            case R.id.btn_loginout:
                loginOut();
                break;
            case R.id.sweep_coder:
                OpenPageUtil.openZXingCode(this);
                break;
            case R.id.mine_account:
                if (!Util_.checkLogin()) {
                    startActivity(new Intent(this, LoginActivity.class).putExtra(KeyHelp
                            .SWICH_CONTENT_2, Constant.SETTING_LOGIN));
                }
                break;
            default:
                break;
        }
    }

    private void checkVersionInfo() {
        if (CommonUtil.isFastDoubleClick(1000)) {
            showToast(getString(R.string.click_too_fast));
            return;
        }
        if (!XMLParserVersionManager.getInstance().isCheckUpdateTaskRunning()) {
            showToast(getString(R.string.version_checking));
            loadCheckVersion(true);
            // 发送通知，通知home主界面的提示点不显示。
            EventBus.getDefault().post(new UpdateBarEvent(false, BaseEvent.Type
                    .UPDATE_MAIN_DOT));
        }
    }

    private void loadCheckVersion(final boolean isShowDialog) {
        if (!CommonUtil.isNetConnect(this)) return;

        XMLParserVersionManager.getInstance().checkUpdate(this,
                new OnCheckResultListener() {

                    @Override
                    public void onResult(boolean hasNewVersion, Object result) {
                        VersionBean o = (VersionBean) result;
                        if (hasNewVersion) {
                            imgDot.setVisibility(View.VISIBLE);
                            ViewUtil.showUpdateDialog(SettingActivity.this, o,
                                    isShowDialog);
                            if (isShowDialog) imgDot.setVisibility(View.GONE); // 点击过后不显示dotImg
                        } else {
                            if (isShowDialog)
                                showToast(getString(R.string.cur_is_newclient));
                        }
                    }
                });
    }

    /**
     * 清除缓存空间
     */
    private void clearCache() {
        // 判断是否正在播放音乐
        closeAudioSing();
        ViewUtil.showUserDialog(this,
                getString(R.string.ask_clear_local_content),
                getString(R.string.clear_can_reloaded), new DialogCallBack() {
                    @Override
                    public void onConfirm() {
                        showBgLoading(getString(R.string.clear_working));
                        ExecutorManager.getInstance().execute(
                                new ClearThread(SettingActivity.this));
                    }
                });
    }

    private static class ClearThread implements Runnable {
        private WeakReference<SettingActivity> reference;

        private ClearThread(SettingActivity context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void run() {
            if (reference == null) return;
            SettingActivity activity = reference.get();
            if (activity == null) return;
            if (Util_.checkLogin()) {
                FileUtils.deleteAllFile(activity.saveFilePath + "/");
                FileUtils.deleteAllFile(activity.downloadFilePath + "/");
                DRMDBHelper.deleteTableData();
            }
            ImageLoadHelp.clearCache();
            DownDataPatDBManager.Builder().deleteAll();
            ShareMomentEngine.clearSharePosition();
            ProgressHelp.clearProgress();
            activity.mHandler.sendEmptyMessageDelayed(MSG_CLEAR_CACHE, 400);
        }
    }

    /**
     * 注销
     */
    private void loginOut() {
        ViewUtil.showUserDialog(this, getString(R.string.loginout_ask_now),
                getString(R.string.loginout_can_reload), new DialogCallBack() {
                    @Override
                    public void onConfirm() {
                        closeAudioSing();
                        sendBroadcast(new Intent(DRMUtil.BROADCAST_CLOSE_ACTIVITY2));
                        reInitValue();
                        LoginActivity.verifyAppVersion();
                    }
                });
    }

    private void closeAudioSing() {
        if (MusicMode.STATUS != MusicMode.Status.STOP) {
            MusicHelp.release(this);
        }
    }

    // 注销后，重新需要初始化的变量
    private void reInitValue() {
        DRMDBHelper.setInitData(this);
        checkLoginState();

        //清除webView网页
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_BROWSER_CLEAR));
        //注销后，也需要通知发现界面更新状态（注销情况后）
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UPDATE_DISCOVER));

        startActivity(new Intent(this, LoginActivity.class).putExtra(KeyHelp.SWICH_CONTENT_2,
                Constant.DISCOVER_LOGIN));

//        finish();
//        if (getParent() != null) {
//            HomeActivity parent = ((HomeActivity) getParent());
//            if (parent.getCurrentActivity() != null) {
//                parent.getCurrentActivity().finish();
//            }
//            parent.finish();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActicityManager.getInstance().remove(this);
        XMLParserVersionManager.getInstance().shutDownCheckTask();
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    @Override
    public void onBackPressed() {
        ViewUtil.showContentDialog(this);
    }

    private static class MyHandler extends Handler {
        private WeakReference<SettingActivity> reference;

        private MyHandler(SettingActivity context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (reference == null) return;
            SettingActivity mActivity = reference.get();
            if (mActivity == null) return;
            switch (msg.what) {
                case MSG_GET_CACHE: {
                    mActivity.mClearLayout.setEnabled(true);
                    mActivity.tv_cache.setText((String) msg.obj);
                }
                break;
                case MSG_CLEAR_CACHE: {
                    mActivity.hideBgLoading();
                    mActivity.sendBroadcast(new Intent(DRMUtil.BROADCAST_CLEAR_DOWNLOADED));
                    ExecutorManager.getInstance().execute(new LoadCacheThread(mActivity));
                    mActivity.mClearLayout.setEnabled(false);
                }
                break;
                default:
                    break;
            }
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case CaptureActivity.REQUEST_CODE_SCAN: {
//                // 扫码登录获取产品信息
//                if (resultCode == CaptureActivity.RESULT_OK && data != null) {
//                    Bundle bundle = data.getExtras();
//                    final String result = bundle.getString(CaptureActivity.DECODED_CONTENT_KEY);
//                    ViewUtil.showCommonDialog(this, "扫描结果", result,
//                            getString(R.string.copy), "", new ViewUtil.DialogCallBackPat() {
//                                @Override
//                                public void onConfirm() {
//                                    ClipboardUtil.copyText(getApplicationContext(), result);
//                                    showToast(getString(R.string.copy_to_clip));
//                                }
//
//                                @Override
//                                public void onCancel() {
//                                }
//                            });
//                }
//            }
//            break;
//            default:
//                break;
//        }
//    }

}
