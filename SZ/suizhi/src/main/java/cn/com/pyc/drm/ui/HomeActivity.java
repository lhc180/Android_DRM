package cn.com.pyc.drm.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.UUID;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.VersionBean;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.bean.event.UpdateBarEvent;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.receiver.NetworkChangedReceiver;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.PathUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.Util_;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.DRMDBHelper;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.help.UIHelper;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager;
import cn.com.pyc.drm.widget.SlideTabHost;
import cn.com.pyc.loger.LogerEngine;
import de.greenrobot.event.EventBus;

/**
 * desc:   HomeActivity.java   新版2.2.9主界面    <br/>
 * author: hudaqiang       <br/>
 * create at 2017/6/29 14:50
 */
public class HomeActivity extends BaseActivityGroup implements View.OnClickListener {

    private ImageView topIv;
    private ImageView bottomIv;
    private ImageView mContentImg;
    private SlideTabHost mTabHost;
    private View mDotView;
    private ImageView mDiscoverImg;
    private ImageView mPersonalImg;
    private TextView mContentText;
    private TextView mDiscoverText;
    private TextView mPersonalText;

    private boolean isLoginComing = false;//是否登陆完进入主页面
    private boolean isStop = false; // ui是否可见
    private NetworkChangedReceiver receiver;
    private Handler mHandler = new Handler();

    //通知，是否显示底部红点消息提示
    public void onEventMainThread(UpdateBarEvent event) {
        if (mDotView != null && event.getType() == BaseEvent.Type.UPDATE_MAIN_DOT) {
            mDotView.setVisibility(event.isShow() ? View.VISIBLE : View.GONE);
        }
    }

    //通知切换到‘我的内容’（BrowserUI和LoginUI发送通知消息）
    public void onEventMainThread(ConductUIEvent event) {
        if (mTabHost != null && event.getType() == BaseEvent.Type.UI_HOME_TAB_1) {
            /*if (Util_.checkLogin()){
            }*/
            selectTab(TAB_1);
        }
        if (event.getType() == BaseEvent.Type.UI_HOME_FINISH) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //HomeActivity.this.finish();
                }
            }, 300);
        }
        if (mTabHost != null && event.getType() == BaseEvent.Type.UI_DIS_LOGIN) {
            selectTab(TAB_2);
        }
        if (mTabHost != null && event.getType() == BaseEvent.Type.UI_HOME_TAB_1_LOGIN) {
            if (Util_.checkLogin()) {
                selectTab(TAB_1);
            }
        }
        if (mTabHost != null && event.getType() == BaseEvent.Type.UI_HOME_TAB_3_LOGIN) {
            selectTab(TAB_3);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIHelper.setFullScreen(this);
        setContentView(R.layout.activity_home2);
        checkData();
        getValue();
        initView();
        initUI();
        registerReceiver();
        checkVersion();
    }

    /*
     * @Params :
     * @Author :songyumei
     * @Date :2017/7/20
     */
//    private void checkToken() {
//        if (!TextUtils.isEmpty(getWeChatToken())) {
//            checkWeChatAccess_token();
//        } else if (!TextUtils.isEmpty(getQQToken())) {
//            checkQQAccess_token();
//        } else {
//            checkGeneralLogin();
//        }
//    }

    private void getValue() {
        EventBus.getDefault().register(this);
        createIfNot();
        PathUtil.checkSDCardCrashLog();
    }

    //非绥知账号登录时，数据库可能需要重新创建;备用，实际登录成功时已经添加创建代码
    private void createIfNot() {
        if (SZConfig.LoginConfig.type != DrmPat.LOGIN_GENERAL && Util_.checkLogin()) {
            // 非绥知账号登录（扫码，微信，QQ）
            DRMDBHelper.destoryDBHelper();
            DRMDBHelper drmDB = new DRMDBHelper(this.getApplicationContext());
            drmDB.createDBTable();
            PathUtil.createFilePath();
        }
    }


    private void initView() {
        //mTitle = ((TextView) findViewById(R.id.home_top_title));
        mTabHost = ((SlideTabHost) findViewById(R.id.home_container));
        mDotView = findViewById(R.id.home_menu_dot_img);

        mContentImg = ((ImageView) findViewById(R.id.home_menu_content_img));
        mDiscoverImg = ((ImageView) findViewById(R.id.home_menu_discover_img));
        mPersonalImg = ((ImageView) findViewById(R.id.home_menu_personal_img));

        mContentText = ((TextView) findViewById(R.id.home_menu_content_text));
        mDiscoverText = ((TextView) findViewById(R.id.home_menu_discover_text));
        mPersonalText = ((TextView) findViewById(R.id.home_menu_personal_text));

        topIv = ((ImageView) findViewById(R.id.home_top_iv));
        bottomIv = ((ImageView) findViewById(R.id.home_bottom_iv));
        View welcomeLin = findViewById(R.id.home_welcome_lin);

        findViewById(R.id.home_menu_content_layout).setOnClickListener(this);
        findViewById(R.id.home_menu_discover_layout).setOnClickListener(this);
        findViewById(R.id.home_menu_personal_layout).setOnClickListener(this);

        showTintBar(R.color.transparent);
        //是否是从登录页进入主界面
        int type = getIntent().getIntExtra(KeyHelp.LOGIN_FLAG, Constant.OTHER_TYPE);
        if (type == Constant.LOGIN_TYPE) {
            isLoginComing = true;
            welcomeLin.setVisibility(View.GONE);
            UIHelper.cancelFullScreen(HomeActivity.this);
        } else {
            isLoginComing = false;
            welcomeLin.setVisibility(View.VISIBLE);
            initViewAnimator();
        }

//        mTabHost.setOnTabChangedListener(new SlideTabHost.OnTabChangedListener() {
//            @Override
//            public void onTabChanged(int newTabIndex, boolean isFirstBuild) {
//                selectTab(newTabIndex);
//            }
//        });
    }


    /**
     * @Params :
     * @Author :songyumei
     * @Date :2017/7/12
     */
    private void initViewAnimator() {
        int height = Constant.screenHeight;
        ObjectAnimator animatorTop = ObjectAnimator.ofFloat(topIv, "translationY", 0, -height / 2);
        ObjectAnimator animatorBottom = ObjectAnimator.ofFloat(bottomIv, "translationY", 0,
                height / 2);
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(animatorTop).with(animatorBottom);
        animatorSet.setDuration(1000);
        animatorSet.setStartDelay(3 * 1000);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //完成动画后，取消全屏
                UIHelper.cancelFullScreen(HomeActivity.this);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    private void initUI() {
        final Context ctx = HomeActivity.this;
        mTabHost.addTabAndContentGenerateListener(false, new SlideTabHost.GenerateViewListener() {
            @Override
            public View generateView() {
                Intent intent = new Intent(ctx, MyContentActivity.class)/*.putExtra(KeyHelp
                .SWICH_CONTENT,KeyHelp.content)*/;
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return getLocalActivityManager().startActivity(UUID.randomUUID().toString(),
                        intent).getDecorView();
            }
        });
        mTabHost.addTabAndContentGenerateListener(true, new SlideTabHost.GenerateViewListener() {
            @Override
            public View generateView() {
                Intent intent = new Intent(ctx, DiscoverActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return getLocalActivityManager().startActivity(UUID.randomUUID().toString(),
                        intent).getDecorView();
            }
        });
        mTabHost.addTabAndContentGenerateListener(false, new SlideTabHost.GenerateViewListener() {
            @Override
            public View generateView() {
                Intent intent = new Intent(ctx, SettingActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return getLocalActivityManager().startActivity(UUID.randomUUID().toString(),
                        intent).getDecorView();
            }
        });

        mDiscoverImg.setSelected(true);
        mDiscoverText.setSelected(true);
        if (!isLoginComing) {
            selectTab(TAB_2);
        }
    }


    private void registerReceiver() {
        receiver = new NetworkChangedReceiver(this) {
            @Override
            protected void onNetChange(boolean isConnect) {
                if (isConnect) {
                    forceUpdate();
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        ViewUtil.showContentDialog(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_menu_content_layout: {//我的内容
                if (isSameTab(TAB_1)) return;
                if (Util_.checkLogin()) {
                    selectTab(TAB_1);
                    showTintBar(R.color.main_bg_color);
                } else {
                    startActivity(new Intent(this, RegisterActivity.class).putExtra(KeyHelp
                            .SWICH_CONTENT, Constant.CONTENT_LOGIN));
                    overridePendingTransition(R.anim.activity_music_open, android.R.anim.fade_out);
                }
            }
            break;
            case R.id.home_menu_discover_layout: {//发现
                if (isSameTab(TAB_2)) return;
                selectTab(TAB_2);
                showTintBar(R.color.main_bg_color);
            }
            break;
            case R.id.home_menu_personal_layout: {//个人中心
                if (isSameTab(TAB_3)) return;
                selectTab(TAB_3);
                showTintBar(R.color.title_bg_color);
                //通知设置页面更新缓存大小显示（登陆后个人中心页面可能已经加载，不会再重新加载了）
                EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UPDATE_SETTING_CACHE));
            }
            break;
            default:
                break;
        }
    }

    private void showTintBar(int colorId) {
        DRMLog.e("manufacturer:" + Build.BRAND);
        if ("vivo".equalsIgnoreCase(Build.BRAND)) return;
        UIHelper.showTintStatusBar(this, getResources().getColor(colorId));
    }

    private boolean isSameTab(int tabIndex) {
        return mTabHost.getCurrentTabIndex() == tabIndex;
    }

    private void resetMenuStatus() {
        mContentImg.setSelected(false);
        mContentText.setSelected(false);
        mDiscoverImg.setSelected(false);
        mDiscoverText.setSelected(false);
        mPersonalImg.setSelected(false);
        mPersonalText.setSelected(false);
    }

    /**
     * 底部菜单选择
     *
     * @param tabIndex 菜单索引标签
     */
    private void selectTab(int tabIndex) {
        resetMenuStatus();
        if (tabIndex == TAB_1) {
            mContentImg.setSelected(true);
            mContentText.setSelected(true);
        } else if (tabIndex == TAB_2) {
            mDiscoverImg.setSelected(true);
            mDiscoverText.setSelected(true);
        } else if (tabIndex == TAB_3) {
            mPersonalImg.setSelected(true);
            mPersonalText.setSelected(true);

            EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_SETTING_UPDATE));
        }
        mTabHost.setCurrentTabIndex(tabIndex);
    }

    private void checkVersion() {
        if (!CommonUtil.isNetConnect(this)) return;
        XMLParserVersionManager.getInstance().checkUpdate(this,
                new XMLParserVersionManager.OnCheckResultListener() {
                    @Override
                    public void onResult(boolean hasNewVersion, Object result) {
                        if (hasNewVersion) {
                            final VersionBean o = (VersionBean) result;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ViewUtil.showUpdateDialog(
                                            HomeActivity.this, o, !isStop);
                                }
                            }, 2000);
                            mDotView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.abortReceiver(this);
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        XMLParserVersionManager.getInstance().shutDownCheckTask();
    }

    private void checkData() {
        // 检查登录，如果升级版本，则检查表中的字段
        if (!Util_.checkLogin())
            return;
        // 获取保存的版本号
        String mSaveVersion = (String) SPUtils.get(DRMUtil.KEY_APP_VERSION, "");
        // 当前app版本号
        String mCurVersion = CommonUtil.getAppVersionName(App.getInstance());
        // 是否升级版本了
        if (TextUtils.equals(mSaveVersion, mCurVersion))
            return;
        // 保存版本号
        SPUtils.save(DRMUtil.KEY_APP_VERSION, mCurVersion);

        DRMDBHelper helper = new DRMDBHelper(this.getApplicationContext());
        helper.checkTable();

        LogerEngine.info(this, "WelcomeUI,check db Column.", null);
    }

}
