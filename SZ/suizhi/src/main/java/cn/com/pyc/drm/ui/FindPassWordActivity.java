package cn.com.pyc.drm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.widget.Space;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.Tencent;

import org.xutils.common.Callback;

import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.model.RegisterModel;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.OtherLoginUtil;
import cn.com.pyc.drm.utils.SecurityUtil;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.DownloadHelp;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.help.UIHelper;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.widget.HighlightImageView;
import de.greenrobot.event.EventBus;

import static cn.com.pyc.drm.utils.help.DownloadHelp.getMapParamsString;

/**
 * 找回密码界面
 * Created by songyumei on 2017/7/14.
 */
public class FindPassWordActivity extends BaseAssistActivity implements View.OnClickListener {
    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.al_space_1)
    Space alSpace1;
    @BindView(R.id.back_img)
    HighlightImageView backImg;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.et_reg_phone)
    EditText mEtPhone; // 手机号
    @BindView(R.id.et_reg_phone_code)
    EditText mEtPhoneCode; // 验证码
    @BindView(R.id.et_password)
    EditText mEtPwd;
    @BindView(R.id.btn_confirm_password)
    TextView mBtnRegister; // 确认找回密码按钮
    @BindView(R.id.btn_get_phone_code)
    TextView mBtnGetCode; // 获取验证码按钮
    @BindView(R.id.tv_wrong)
    TextView tvWrong; // 找回密码错误提示信息
    @BindView(R.id.tv_login)
    TextView login;//登录
    @BindView(R.id.et_password_confirm)
    EditText pwsConfirm;
    @BindView(R.id.weixin_login)
    TextView wxLogin;//微信登录
    @BindView(R.id.qq_login)
    TextView qqLogin;//QQ登录
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.rl_verification_code)
    RelativeLayout rlVerificationCode;//倒计时

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private OtherLoginUtil otherLoginUtil;
    private Callback.Cancelable mCancelable;
    private MyCount mc;

    private int intExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pass_word);
        ButterKnife.bind(this);
        getValue();
        initView();
        loadData();
    }

    private void loadData() {
    }

    protected void getValue() {
//        UIHelper.showTintStatusBar(this);
//        EventBus.getDefault().register(this);
        SZConfig.LoginConfig.type = DrmPat.LOGIN_GENERAL;
        otherLoginUtil = new OtherLoginUtil(this);
    }

    protected void initView() {
        ivClose.setOnClickListener(this);
        wxLogin.setOnClickListener(this);
        qqLogin.setOnClickListener(this);
        backImg.setOnClickListener(this);
        mBtnGetCode.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
        login.setOnClickListener(this);

        titleTv.setText(getString(R.string.sz_register));

        //输入6位验证码之前，确认按钮不可点且为灰色显示
//        mBtnRegister.setEnabled(false);
//        mBtnRegister.setButtonColor(getResources().getColor(R.color.gray));
        mEtPhone.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (tvWrong.isShown()) tvWrong.setVisibility(View.GONE);
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        mEtPwd.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (tvWrong.isShown()) tvWrong.setVisibility(View.GONE);
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        mEtPhoneCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //当输入验证码的长度为6的时候，让确认按钮变为蓝色可点击状态
                //这样写不等于6时，状态不能还原
//                if(editable.length()==6){
//                    mBtnRegister.setEnabled(true);
//                    mBtnRegister.setBackgroundColor(getResources().getColor(R.color
// .title_bg_color));
//                    mBtnRegister.setCornerRadius(4);
//                }
                boolean isLegal = editable.length() == 6;
//                mBtnRegister.setEnabled(isLegal);
//                mBtnRegister.setButtonColor(getResources().getColor(isLegal ? R.color
//                        .title_bg_color : R.color.gray));
            }
        });
        //如果验证码还没有填写，确认按钮为灰色且不可点击
//        if (StringUtil.isEmpty(getPhoneCodeNew())){
//            mBtnRegister.setClickable(false);
//            mBtnRegister.setBackgroundColor(getResources().getColor(R.color.gray));
//        }
//        mEtPhone.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (CommonUtil.isFastDoubleClick(600)) return false;
//                changeScrollView();
//                return false;
//            }
//        });
//        pwsConfirm.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (CommonUtil.isFastDoubleClick(600)) return false;
//                changeScrollView();
//                return false;
//            }
//        });
//        mEtPhoneCode.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (CommonUtil.isFastDoubleClick(600)) return false;
//                changeScrollView();
//                return false;
//            }
//        });
//        mEtPwd.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (CommonUtil.isFastDoubleClick(600)) return false;
//                changeScrollView();
//                return false;
//            }
//        });
    }

    /*
     * @Params :
     * @Author :songyumei
     * @Date :2017/7/12
     */
//    private void otherLogin() {
//        wxLogin.setOnClickListener(this);
//        qqLogin.setOnClickListener(this);
//        backImg.setOnClickListener(this);
//        mBtnGetCode.setOnClickListener(this);
//        mBtnRegister.setOnClickListener(this);
//        login.setOnClickListener(this);
//
//        otherLoginUtil = new OtherLoginUtil(this);
//    }

    /*
    private void changeScrollView() {
        if (scrollView.getScrollY() >= scrollView.getHeight()) return;
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, ivTop.getHeight());
            }
        }, 300);*/
        /*llAround.postDelayed(new Runnable() {
            @Override
            public void run() {
                llAround.scrollTo(0, alSpace1.getHeight());
            }
        }, 300);
    }
    */

    private String getPhoneNumberNew() {
        return mEtPhone.getText().toString().trim();
    }

    private String getPhoneCodeNew() {
        return mEtPhoneCode.getText().toString().trim();
    }

    private String getPasswordNew() {
        return mEtPwd.getText().toString();
    }

    private String getPasswordConfirmNew() {
        return pwsConfirm.getText().toString();
    }

    private void findPassWord() {
        hideNotice();
        //获取手机号
        if (StringUtil.isEmpty(getPhoneNumberNew())) {
            showWrongString(getString(R.string.msg_register_phone_null));
            return;
        }

        //手机格式错误
        if (!StringUtil.isMobileNO(getPhoneNumberNew())) {
            //UIHelper.showToast(getApplicationContext(), getString(R.string.print_right_phone));
            showWrongString(getString(R.string.print_right_phone));
            return;
        }

        if (StringUtil.isEmpty(getPhoneCodeNew())) {
            showWrongString(getString(R.string.msg_validatecode_null));
            return;
        }

        if (StringUtil.isEmpty(getPasswordNew())) {
            showWrongString(getString(R.string.msg_newpasswrod_null));
            return;
        }

        if (StringUtil.isEmpty(getPasswordConfirmNew())) {
            showWrongString(getString(R.string.msg_confrimpassword_null));
            return;
        }

        if (!TextUtils.equals(getPasswordConfirmNew(), getPasswordNew())) {
            showWrongString(getString(R.string.msg_confrim_new_password_dis));
            return;
        }

        //网络是否连接
        if (!CommonUtil.isNetConnect(FindPassWordActivity.this)) {
            showWrongString(getString(R.string.net_disconnected));
            return;
        }
        //手机格式错误
        if (!StringUtil.isMobileNO(getPhoneNumberNew())) {
            UIHelper.showToast(getApplicationContext(), getString(R.string.print_right_phone));
            return;
        }

        ViewUtil.hideWidget(tvWrong);

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("loginName", getPhoneNumberNew());
        map.put("password", getPasswordNew());
        map.put("validateCode", getPhoneCodeNew());
        String authKey = SecurityUtil.encodeMD5BySalt(getMapParamsString(map), SZConfig.LOGIN_SALT);
        map.put("authKey", authKey);

        mCancelable = HttpEngine.get(APIUtil.getFindPassWord(), map, new Callback
                .CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                DRMLog.d("onSuccess: " + result);
                RegisterModel o = JSON.parseObject(result, RegisterModel.class);
                if (o.getCode() == 1000) {
                    // 召回密码成功
                    openActivity(LoginActivity.class);
                } else {
                    // 召回密码失败
                    setMyCountTimerCancel();
                }
                showToast(o.getMsg());
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showToast("找回密码失败，无法连接服务器");
                setMyCountTimerCancel();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /*
     * 接收事件总线的回调，由BGOCommandService回调发出通知信息
     */
//    public void onEventMainThread(DBMakerEvent event) {
//        boolean isDBMaker = event.isMaker();
//        DRMLog.d(TAG, "onEvent: receive answer " + isDBMaker);
//        if (isDBMaker) {
//
//        }
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //EventBus.getDefault().unregister(this);
        HttpEngine.cancelHttp(mCancelable);
    }

    @Override
    public void onBackPressed() {
        intExtra = getIntent().getIntExtra(KeyHelp.SWICH_CONTENT_2, 0);
        if (intExtra == Constant.CONTENT_LOGIN) {
            EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_HOME_TAB_3_LOGIN));
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            finish();
        }
        overridePendingTransition(R.anim.fade_in_scale, R.anim.activity_music_close);
    }

    private void showWrongString(String text) {
        if (!tvWrong.isShown()) tvWrong.setVisibility(View.VISIBLE);
        tvWrong.setText(text);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.weixin_login:
                // 微信登录
                otherLoginUtil.weChatLogin();
                break;
            case R.id.qq_login:
                // QQ登录
                otherLoginUtil.QQLogin();
                break;
            case R.id.back_img:
                OpenPageUtil.openActivity(FindPassWordActivity.this,
                        LoginActivity.class);
                UIHelper.finishActivity(FindPassWordActivity.this);
                break;
            case R.id.btn_get_phone_code:
                // 获取验证码
                getVerifyCode();
                break;
            case R.id.btn_confirm_password:
                // 找回密码
                findPassWord();
                break;
            case R.id.tv_login:
                openActivity(LoginActivity.class);
                finish();
                break;
            case R.id.iv_close:
//                OpenPageUtil.openActivity(this, HomeActivity.class);
                intExtra = getIntent().getIntExtra(KeyHelp.SWICH_CONTENT_2, 0);
                EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_DIS_LOGIN));
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

    private void getVerifyCode() {
        hideNotice();
        //获取手机号
        if (StringUtil.isEmpty(getPhoneNumberNew())) {
            showWrongString(getString(R.string.msg_register_phone_null));
            return;
        }
        //网络是否连接
        if (!CommonUtil.isNetConnect(FindPassWordActivity.this)) {
            showWrongString(getString(R.string.net_disconnected));
            return;
        }
        //手机格式错误
        if (!StringUtil.isMobileNO(getPhoneNumberNew())) {
            //UIHelper.showToast(getApplicationContext(), getString(R.string.print_right_phone));
            showWrongString(getString(R.string.print_right_phone));
            return;
        }
        mBtnGetCode.setVisibility(View.INVISIBLE);
        rlVerificationCode.setVisibility(View.VISIBLE);

        mc = new MyCount(60000, 1000);
        mc.start();

        //获取手机验证码的网络请求
        getVerificationCode();
    }

    private void getVerificationCode() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("loginName", getPhoneNumberNew());
        String authKey = SecurityUtil.encodeMD5BySalt(DownloadHelp.getMapParamsString(map),
                SZConfig.LOGIN_SALT);
        map.put("authKey", authKey);
        HttpEngine.get(APIUtil.getFindPassWordCodeUrl(), map, new Callback.CommonCallback<String>
                () {
            @Override
            public void onSuccess(String result) {
                DRMLog.d("onSuccess: " + result);
                RegisterModel o = JSON.parseObject(result, RegisterModel.class);
                if (o.getCode() == 1000) {
                    //发送验证码成功
                } else {
                    // 发送验证码失败
                    setMyCountTimerCancel();
                }
                showToast(o.getMsg());
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showToast("获取验证码失败,无法连接服务器");
                setMyCountTimerCancel();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }


    /*
     * 获取Map内添加的字符串，进行拼接
     */
//    private static String getMapParamsString(LinkedHashMap params) {
//        String preParams = "";
//        Iterator iterator = params.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry entry = (Map.Entry) iterator.next();
//            Object val = entry.getValue();
//            preParams += String.valueOf(val);
//        }
//        //DRMLog.d("preParams: " + preParams);
//        return preParams;
//    }

    private void hideNotice() {
        if (tvWrong.getVisibility() == View.VISIBLE)
            tvWrong.setVisibility(View.INVISIBLE);
        tvWrong.setText(null);
    }

    /* 定义一个倒计时的内部类 */
    class MyCount extends CountDownTimer {
        MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            setMyCountTimerCancel();//取消倒计时
        }

        @Override
        public void onTick(long millisUntilFinished) {
//			mbtn_verification_code.setText("重新获取 (" + millisUntilFinished / 1000 + ")");
//            tvTime.setText("" + (millisUntilFinished / 1000));
            tvTime.setText(String.valueOf(millisUntilFinished / 1000));
        }
    }

    /**
     * 取消倒计时
     */
    private void setMyCountTimerCancel() {
        mBtnGetCode.setVisibility(View.VISIBLE);
        rlVerificationCode.setVisibility(View.INVISIBLE);
        if (mc != null) {
            mc.cancel();
            mc = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, otherLoginUtil
                    .mQQLoginUIListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
