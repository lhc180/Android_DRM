package cn.com.pyc.drm.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.utils.ClearKeyUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.PathUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.Util_;
import cn.com.pyc.drm.utils.help.DRMDBHelper;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.loger.LogerEngine;

@Deprecated
public class WelcomeActivity extends Activity {

    private static final String TAG = "Welcome";
    private String weChat_token;
    private String QQ_token;
    private Cancelable wxCancelable;
    private ImageView top_iv;
    private ImageView buttom_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        if (getWindow() != null) getWindow().setFormat(PixelFormat.RGBA_8888);

        getValue();
//        initView();
        initViewAnimator();
        loadData();
    }

    /**
    *@Params :
    *@Author :songyumei
    *@Date :2017/7/12
    */
    protected void initViewAnimator() {
        top_iv = (ImageView) findViewById(R.id.top_iv);
        buttom_iv = (ImageView) findViewById(R.id.buttom_iv);
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);

        int height = wm.getDefaultDisplay().getHeight();
        ObjectAnimator animator = ObjectAnimator.ofFloat(top_iv, "translationY", 0, -height/2); //动画一
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(animator).with(ObjectAnimator.ofFloat(buttom_iv, "translationY", 0, height/2)); //动画二
        animatorSet.setDuration(1000);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                if (!TextUtils.isEmpty(weChat_token)) {
                    checkWeChatAccess_token();
                } else if (!TextUtils.isEmpty(QQ_token)) {
                    checkQQAccess_token();
                } else {
                    checkGeneralLogin();
                }
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animatorSet.start();
            }
        },3000);
    }

    protected void getValue() {
        PathUtil.checkSDCardCrashLog();
        weChat_token = (String) SPUtils.get(DRMUtil.KEY_WECHAT_ACCESS_TOKEN, "");
        QQ_token = (String) SPUtils.get(DRMUtil.KEY_QQ_ACCESS_TOKEN, "");
    }

    protected void initView() {
        long duration = 2000;
        if (!TextUtils.isEmpty(weChat_token)) duration = 1000;

        View view = findViewById(R.id.welcome_layout);
        AlphaAnimation animation = new AlphaAnimation(0.5f, 1.0f);
        animation.setDuration(duration);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        view.startAnimation(animation);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                if (!TextUtils.isEmpty(weChat_token)) {
                    checkWeChatAccess_token();
                } else if (!TextUtils.isEmpty(QQ_token)) {
                    checkQQAccess_token();
                } else {
                    checkGeneralLogin();
                }
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
        });
        /*TextView tvVersion = (TextView) findViewById(R.id.text_version);
        final String versionName = CommonUtil.getAppVersionName(this);
        tvVersion.setText("version " + versionName);*/
    }

    /**
     * 普通登录校验
     */
    private void checkGeneralLogin() {
        //String username = (String) SPUtils.get(DRMUtil.KEY_NAME, "");
        //String loginToken = (String) SPUtils.get(DRMUtil.KEY_LOGIN_TOKEN, "");
        //if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(loginToken)) {
        if (Util_.checkLogin()) {
            SZConfig.LoginConfig.type = DrmPat.LOGIN_GENERAL;
            PathUtil.createFilePath();
            OpenPageUtil.openActivity(this, HomeActivity.class);
            this.overridePendingTransition(R.anim.activity_down_enter,R.anim.activity_down_exit);
        } else {
            ClearKeyUtil.removeKey();
//            OpenPageUtil.openActivity(this, LoginActivity.class);
            OpenPageUtil.openActivity(this, HomeActivity.class);
            this.overridePendingTransition(R.anim.activity_down_enter,R.anim.activity_down_exit);
        }
        finish();
    }

    private void openLoginGuide() {
        showToast(getString(R.string.login_lose_efficacy));
        ClearKeyUtil.removeKey();
        OpenPageUtil.openActivity(this, LoginActivity.class);
        finish();
    }

    /**
     * 检验授权微信凭证（access_token）是否有效
     */
    private void checkWeChatAccess_token() {
        String openid = (String) SPUtils.get(DRMUtil.KEY_WECHAT_OPENID, "");
        String path = "https://api.weixin.qq.com/sns/auth?access_token=" + weChat_token +
                "&openid=" + openid;
        wxCancelable = HttpEngine.get(path, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                showToast(getString(R.string.network_is_not_available));
                openLoginGuide();
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(String arg0) {
                Log.v(TAG, "check: " + arg0);
                try {
                    JSONObject json = new JSONObject(arg0);
                    if (json.getInt("errcode") != 0) {
                        // 登录失效，打开登录页
                        openLoginGuide();
                    } else {
                        SZConfig.LoginConfig.type = DrmPat.LOGIN_WECHAT;
                        PathUtil.createFilePath();
                        OpenPageUtil.openActivity(WelcomeActivity.this, HomeActivity.class);
                        finish();
                    }
                } catch (Exception e) {
                    openLoginGuide();
                }
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查qq登录access_token是否有效
     */
    private void checkQQAccess_token() {
        long expires_in = (Long) SPUtils.get(DRMUtil.KEY_QQ_EXPIRES_IN, 0L);
        long QQ_expires_in = (expires_in - System.currentTimeMillis()) / 1000;
        DRMLog.d(TAG, "QQ_expires_in = " + QQ_expires_in);
        if (QQ_expires_in < 0 || QQ_expires_in == 0) {
            // 过期了(有效期3个月)，打开登录页
            openLoginGuide();
        } else {
            SZConfig.LoginConfig.type = DrmPat.LOGIN_QQ;
            PathUtil.createFilePath();
            OpenPageUtil.openActivity(this, HomeActivity.class);
            finish();
        }
    }

    protected void loadData() {

        //BGOCommandService.startBGOService(this, BGOCommandService.RENAME_DB);
        //测试：
        String sd = Environment.getExternalStorageDirectory().toString() + "/";

        //FileUtils.renameDirectory(sd + "sz/test2", sd + "sz/lrc");


//        FileUtils.copyFile(
//                new File(sd + "sz/lrc"),
//                new File(sd + "sz/demo"), new FileUtils.OnCopyProgressListener() {
//                    @Override
//                    public void onCopyProgress(int progress) {
//                        DRMLog.e("progress：" + progress);
//                    }
//                });
//
//        FileUtils.deleteAllFile(new File(sd + "sz/test2"));

//        Map<String, List<String>> maps = new DataMergeUtil<>().getAllBuyMyProductIdMap();
//        for (Map.Entry<String, List<String>> entry : maps.entrySet()) {
//            DRMLog.e("用户：" + entry.getKey());
//            DRMLog.e("用户下id数：" + entry.getValue().size());
//        }


        // 检查登录，如果升级版本，则检查表中的字段
        if (TextUtils.isEmpty(Constant.getName()))
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HttpEngine.cancelHttp(wxCancelable);
    }

}
