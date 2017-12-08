package cn.com.pyc.drm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.Tencent;

import org.xutils.common.Callback;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.bean.event.DBMakerEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.LogConfig;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.model.LoginModel;
import cn.com.pyc.drm.model.LoginModel.LoginInfo;
import cn.com.pyc.drm.service.BGOCommandService;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.OtherLoginUtil;
import cn.com.pyc.drm.utils.PathUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.SecurityUtil;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern.LogerHelp;
import de.greenrobot.event.EventBus;

/**
 * 账号登录界面
 *
 * @author songyumei
 */
public class LoginActivity extends BaseAssistActivity implements OnClickListener {

    private static String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.et_username)
    EditText mEtUserName; // 账号
    @BindView(R.id.et_password)
    EditText mEtPwd;
    @BindView(R.id.btn_login)
    Button mBtnLogin; // 登录按钮
    @BindView(R.id.tv_wrong)
    TextView tvWrong; // 登录错误提示信息
    @BindView(R.id.tv_forget_psw)
    TextView tvForgetPsw;//忘记密码
    @BindView(R.id.tv_register)
    TextView tvRegister;//注册账号
    @BindView(R.id.weixin_login)
    TextView wxLogin;//微信登录
    @BindView(R.id.qq_login)
    TextView qqLogin;//QQ登录
    @BindView(R.id.logo_imageView)
    ImageView imgLogo;//图片
    @BindView(R.id.iv_close)
    ImageView ivClose;

    //private boolean isRememberKey = true;// 记录用户是否选中记住密码。
    private boolean isFromWeb = false;//记录是从web支付页跳过来的
    private String web_url;
    private OtherLoginUtil otherLoginUtil;


    /***
     * 登录
     *
     * @param loginName 输入的用户名
     * @param passWord  密码
     * @param callback  回调函数
     */
    public static void loginV2(String loginName, String passWord,
                               Callback.CommonCallback<String> callback) {
        Bundle bundle = new Bundle();
        //bundle.putString("username", loginName);
        bundle.putString("loginName", loginName);
        bundle.putString("password", SecurityUtil.getParamByMD5(loginName, passWord));
        HttpEngine.post(APIUtil.getLoginV2Url(), bundle, callback);
    }


    //接收通知，结束登陆页面
    public void onEventMainThread(ConductUIEvent event) {
        if (event.getType() == BaseEvent.Type.UI_LOGIN_FINISH) {
            setResult(Activity.RESULT_OK, new Intent().putExtra("web_url", web_url));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        getValue();
        initView();
        loadData();
    }

    protected void getValue() {
        EventBus.getDefault().register(this);

        isFromWeb = getIntent().getBooleanExtra("web_login", false);
        web_url = getIntent().getStringExtra("web_url");
        DRMLog.e("from web is " + isFromWeb + "; url: " + web_url);

        if (otherLoginUtil == null)
            otherLoginUtil = new OtherLoginUtil(this);
        // 如果是重复登陆则弹出对话框。
        boolean isRepeatLogin = getIntent().getBooleanExtra(DRMUtil.KEY_REPEAT_LOGIN, false);
        if (isRepeatLogin) {
            ViewUtil.showSingleCommonDialog(this,
                    getString(R.string.tip_title),
                    getString(R.string.repeat_login),
                    getString(R.string.xml_dialog_delete_positive), null);
        }
    }


    protected void initView() {
        mEtUserName.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (tvWrong.isShown()) tvWrong.setVisibility(View.GONE);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        mEtPwd.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvWrong.isShown()) tvWrong.setVisibility(View.GONE);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        mBtnLogin.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                loginClick();
            }
        });

        mEtUserName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (CommonUtil.isFastDoubleClick(600)) return false;
                return false;
            }
        });
        mEtPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (CommonUtil.isFastDoubleClick(600)) return false;
                return false;
            }
        });

        otherLogin();
    }

//    private void setScrollViewChildHeigth() {
//        WindowManager wm = (WindowManager) this
//                .getSystemService(Context.WINDOW_SERVICE);
//
//        int width = wm.getDefaultDisplay().getWidth();
//        int height = wm.getDefaultDisplay().getHeight();
//        LinearLayout ll_child = (LinearLayout) findViewById(R.id.ll_child);
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ll_child.getLayoutParams();
//        //获取当前控件的布局对象
//        params.width = width;
//        params.height = height;//设置当前控件布局的高度
//        ll_child.setLayoutParams(params);//将设置好的布局参数应用到控件中
//    }

    /**
     * 其他方式登录
     * <p>
     * songyumei
     */
    private void otherLogin() {
        ivClose.setOnClickListener(this);
        wxLogin.setOnClickListener(this);
        qqLogin.setOnClickListener(this);

        tvForgetPsw.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }


    private String getUsernameNew() {
        return mEtUserName.getText().toString().trim();
    }

    private String getPasswordNew() {
        return mEtPwd.getText().toString();
    }

    private void loginClick() {
        if (StringUtil.isEmpty(getUsernameNew())) {
            showWrongString(getString(R.string.msg_login_username_null));
            return;
        }
        if (StringUtil.isEmpty(getPasswordNew())) {
            showWrongString(getString(R.string.msg_login_pwd_null));
            return;
        }
        if (!CommonUtil.isNetConnect(LoginActivity.this)) {
            showWrongString(getString(R.string.net_disconnected));
            return;
        }
        ViewUtil.hideWidget(tvWrong);
        logining();

        // 开启服务，创建需要的数据表。
        //BGOCommandService.startBGOService(this, BGOCommandService.CREATE_DB);
    }

    private void logining() {
        showBgLoading(getString(R.string.login_ing));
        SZConfig.LoginConfig.type = DrmPat.LOGIN_GENERAL;
        loginV2(getUsernameNew(), getPasswordNew(),
                new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        DRMLog.d("onSuccess: " + result);
                        LoginModel o = JSON.parseObject(result, LoginModel.class);
                        if (o.isSuccess()) {
                            loginSuccess(o.getData());
                        } else {
                            // 失败
                            //loginFail(getString(R.string.msg_login_fail));
                            loginFail(o.getMsg());
                        }
                    }

                    @Override
                    public void onFinished() {
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        DRMLog.e("onError: " + arg0.getMessage());
                        loginFail(getString(R.string.login_fail_disconnected));
                        addLog(arg0.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                    }
                });
    }

    //登录成功,(密码要加密保存？)
    private void loginSuccess(LoginInfo o) {
        Constant.setAccountId(o.getAccountId());
        Constant.setName(o.getUsername());
        Constant.setToken(o.getToken());

        SPUtils.save(DRMUtil.KEY_VISIT_NAME, getUsernameNew()); //登录输入的用户名称
        SPUtils.save(DRMUtil.KEY_VISIT_PWD, getPasswordNew());
        SPUtils.save(DRMUtil.KEY_VISIT_ACCOUNTID, o.getAccountId());
//        if (isRememberKey) {
//            SPUtils.save(DRMUtil.KEY_REMEMBER_NAME, o.getUsername());
//            SPUtils.save(DRMUtil.KEY_REMEMBER_PWD, getPasswordNew());
//        }
        //通知设置页面更新缓存大小显示
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UPDATE_SETTING_CACHE));
        // 开启服务，创建需要的数据表。
        BGOCommandService.startBGOService(this, BGOCommandService.CREATE_DB);
        //来自web购买的登录
        /*if (isFromWeb) {
            hideBgLoading();
            setResult(Activity.RESULT_OK, new Intent().putExtra("web_url", web_url));
            finish();
        }*/
    }

    // 登录失败
    private void loginFail(String failStr) {
        showWrongString(failStr);
        hideBgLoading();
    }

    /**
     * 接收事件总线的回调，由BGOCommandService.java中发出通知信息
     */
    public void onEventMainThread(DBMakerEvent event) {
        boolean isDBMaker = event.isMaker();
        DRMLog.d(TAG, "onEvent: receive answer " + isDBMaker);
        if (isDBMaker) {
            // 创建应用文件保存目录
            PathUtil.createFilePath();

            if (isFromWeb) {
                otherLoginUtil.registerDeviceInfo2(web_url);
            } else {
                otherLoginUtil.registerDeviceInfo(tvWrong);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        otherLoginUtil.cancelable();
    }

    @Override
    public void onBackPressed() {
        int intExtra = getIntent().getIntExtra(KeyHelp.SWICH_CONTENT_2, 0);
        if (intExtra == Constant.CONTENT_LOGIN) {
            EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_HOME_TAB_3_LOGIN));
            startActivity(new Intent(this, HomeActivity.class)
                    /*.putExtra(KeyHelp.LOGIN_FLAG, Constant.LOGIN_TYPE)*/);
        } else {
            finish();
        }
        overridePendingTransition(R.anim.fade_in_scale, R.anim.activity_music_close);
    }

    protected void loadData() {
        String visitName = Constant.getLoginName();
        if (!TextUtils.isEmpty(visitName)) // && !isRememberKey
        {
            // 非第一次登录，用户名已存在！
            if (SZConfig.LoginConfig.type == DrmPat.LOGIN_GENERAL) {
                mEtUserName.setText(visitName);
                mEtPwd.requestFocus();
            }
        }
    }

    private void showWrongString(String text) {
        if (!tvWrong.isShown()) tvWrong.setVisibility(View.VISIBLE);
        tvWrong.setText(text);
    }

    //添加日志记录
    private void addLog(String content) {
        ExtraParams extraParams = LogConfig.getBaseExtraParams();
        extraParams.file_name = LogerHelp.getFileName();
        LogerEngine.debug(this, content, extraParams);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.weixin_login:
                if (isFromWeb) {
                    otherLoginUtil.weChatLogin2(web_url);
                } else {
                    otherLoginUtil.weChatLogin();
                }
                break;
            case R.id.qq_login:
                otherLoginUtil.QQLogin();
                break;
            case R.id.tv_forget_psw:
                int intExtra = getIntent().getIntExtra(KeyHelp.SWICH_CONTENT_2, 0);
                if (intExtra == Constant.CONTENT_LOGIN) {
                    startActivity(new Intent(this, FindPassWordActivity.class).putExtra(KeyHelp
                            .SWICH_CONTENT_2, intExtra));
                } else {
                    openActivity(FindPassWordActivity.class);
                }
                finish();
                break;
            case R.id.tv_register:
                openActivity(RegisterActivity.class);
                finish();
                break;
            case R.id.iv_close:
                EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_DIS_LOGIN));
                intExtra = getIntent().getIntExtra(KeyHelp.SWICH_CONTENT_2, 0);
                if (intExtra == Constant.CONTENT_LOGIN) {
                    startActivity(new Intent(this, HomeActivity.class)
                    /*.putExtra(KeyHelp.LOGIN_FLAG, Constant.LOGIN_TYPE)*/);
                } else {
                    finish();
                }
                overridePendingTransition(R.anim.fade_in_scale, R.anim.activity_music_close);
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data,
                    otherLoginUtil.mQQLoginUIListener);
        }
    }

    //用于校验app和统计数据
    public static void verifyAppVersion() {
        Bundle params = new Bundle();
        HttpEngine.post(APIUtil.getForceUpdateUrl(), params, new Callback
                .CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }
}
