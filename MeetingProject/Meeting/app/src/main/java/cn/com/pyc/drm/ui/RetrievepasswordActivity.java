package cn.com.pyc.drm.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.ReturnDataBean;
import cn.com.pyc.drm.bean.ReturnVerificationDataBean;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.http.WSHelper;
import cn.com.pyc.drm.utils.DialogUtil;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.widget.ClearEditText;
import cn.com.pyc.drm.widget.FlatButton;
import cn.com.pyc.drm.widget.HighlightImageView;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (找回密码)
 * @date 2016-8-15 下午5:19:14
 */
public class RetrievepasswordActivity extends BaseActivity implements OnClickListener, WSHelper.OnLoadDataListener<ReturnDataBean> {

    @ViewInject(R.id.title_tv)
    private TextView mtv_title_tv;

    @ViewInject(R.id.et_phone_number)
    private ClearEditText met_phone_number;

    @ViewInject(R.id.et_verification_code)
    private ClearEditText met_verification_code;

    @ViewInject(R.id.btn_real_name)
    private ClearEditText mbtn_real_name;

    @ViewInject(R.id.btn_set_newpassword)
    private ClearEditText mbtn_set_newpassword;

    @ViewInject(R.id.btn_verification_code)
    private FlatButton mbtn_verification_code;

    @ViewInject(R.id.rl_verification_code)
    private RelativeLayout mrl_verification_code;

    @ViewInject(R.id.tv_second)
    private TextView mtv_second;

    @ViewInject(R.id.btn_confirm)
    private FlatButton mbtn_confirm;

    @ViewInject(R.id.back_img)
    private HighlightImageView mback_img;

    @ViewInject(R.id.al_tv_notice)
    private TextView mal_tv_notice;

    private MyCount2 mc;

    private String phoneNumber;

    private AlertDialog clearDlg;

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-8-15 下午5:20:45
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrievepassword);
        init_Value();
        init_View();
        initListener();
        load_Data();
    }

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-8-15 下午5:21:01
     */
    private void initListener() {
        // TODO Auto-generated method stub
        mbtn_verification_code.setOnClickListener(this);
        mrl_verification_code.setOnClickListener(this);
        mbtn_confirm.setOnClickListener(this);
        mback_img.setOnClickListener(this);
    }

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-8-15 下午5:20:47
     */
    @Override
    protected void init_View() {
        // TODO Auto-generated method stub
        mtv_title_tv.setText("找回密码");
    }

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-8-15 下午5:20:47
     */
    @Override
    protected void load_Data() {
        // TODO Auto-generated method stub

    }

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-8-15 下午5:20:47
     */
    @Override
    protected void init_Value() {
        ViewUtils.inject(this);
        ActicityManager.getInstance().add(this);
        // 自定义状态栏
        UIHelper.showTintStatusBar(this);
        phoneNumber = getIntent().getStringExtra("phone_number");
        if (!TextUtils.isEmpty(phoneNumber)) {
            met_phone_number.setText(phoneNumber);
            mbtn_set_newpassword.requestFocus();
        }
    }

    /**
     * @Description: (用一句话描述该文件做什么)
     * @author 李巷阳
     * @date 2016-8-16 下午6:10:36
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                finish();
                break;
            case R.id.btn_verification_code:
                // 获取手机验证码
                getVerfyCode();
                break;
            case R.id.btn_confirm:
                // 修改密码
                toRetrievepassword();
                break;
            default:
                break;
        }
    }

    /**
     * @Description: (修改密码)
     * @author 李巷阳
     * @date 2016-8-16 下午6:15:22
     */
    private void toRetrievepassword() {
        hideNotice();
        // 手机号
        final String phonenumber = getPhoneNo();
        // 手机验证码
        String secutrity = getSecutrity();
        // 设置真实姓名
        String real_name = getReal_name();
        // 确认密码
        String newpsd_set = getNewpsd_set();

        if (!StringUtil.isMobileNO(phonenumber)) {
            setControlPrompt("请您输入正确的手机号");
            return;
        }
        if (TextUtils.isEmpty(secutrity)) {
            setControlPrompt("手机验证码不能为空");
            return;
        }
        if (TextUtils.isEmpty(real_name)) {
            setControlPrompt("真实姓名不能为空");
            return;
        }
        if (TextUtils.isEmpty(newpsd_set)) {
            setControlPrompt("新密码不能为空");
            return;
        }
        if (newpsd_set.length() < 6) {
            setControlPrompt("\"设置密码\"须6位或以上任意字符。");
            return;
        }
        WSHelper.newBuilder().setmUrl(Constant.RetrievePW_URL).setMethodName(Constant.RetrievePW_MethodName).putParam("phone", phonenumber).putParam("username", real_name).putParam("password", newpsd_set).putParam("phonecode", secutrity).setCallback(RetrievepasswordActivity.this).build(mBaseActivity, new TypeToken<ReturnDataBean>() {}.getType());
    }
    /**
     * @Description: (获取验证码)
     * @author 李巷阳
     * @date 2016-8-16 下午6:11:46
     */
    private void getVerfyCode() {

        hideNotice();
        String phonenumber = getPhoneNo();
        if (!StringUtil.isMobileNO(phonenumber)) {
            UIHelper.showToast(getApplicationContext(), "请输入正确的手机号");
            return;
        }

        WSHelper.newBuilder().setmUrl(Constant.GetPhoneCode_URL).setMethodName(Constant.GetPhoneCode_MethodName).putParam("phone", phonenumber).putParam("flag","1").setCallback(new WSHelper.OnLoadDataListener<ReturnVerificationDataBean>() {
            @Override
            public void onSuccess(ReturnVerificationDataBean vcb) {
                if ("true".equals(vcb.getResult())) {
                    UIHelper.showToast(getApplicationContext(), vcb.getMsg());
                    startGetCode();// 获取验证码
                    return;
                } else {
                        // 如果getCode等于02.则提示用户去注册。
                        if(Constant.key_code.equals(vcb.getCode()))
                        {
                            DialogUtil.showDialog_Prompt(mBaseActivity, "去注册","取消", "账号未注册,点击进入注册界面?", new DialogUtil.onDialogPromptListener() {
                                @Override
                                public void Onclick_Dialog_Confirm_Listener(View v) {
                                    Bundle bundle=new Bundle();
                                    bundle.putString("phone_number",getPhoneNo());
                                    openActivity(RegisterActivity.class,bundle);
                                    finish();
                                }
                                @Override
                                public void Onclick_Dialog_Cancel_Listener(View v) {
                                }
                            });
                        }
                    UIHelper.showToast(getApplicationContext(), vcb.getMsg());
                }
                setMyCountTimerCancel();
            }

            @Override
            public void onError(String error_str, Exception e) {
                showToast(error_str);
                setMyCountTimerCancel();
            }
        }).build(mBaseActivity, new TypeToken<ReturnVerificationDataBean>() {}.getType());


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        setMyCountTimerCancel();
    }

    private void setControlPrompt(String strPrompt) {
        UIHelper.showToast(getApplicationContext(), strPrompt);
        mal_tv_notice.setVisibility(View.VISIBLE);
        mal_tv_notice.setText(strPrompt);
    }


    /**
     * @Description: (修改成功的回调)
     * @author 李巷阳
     * @date 2016-8-16 下午6:16:48
     */
    protected void setRegisterReturnCode(ReturnDataBean bm) {
        if ("true".equals(bm.getResult())) {

            DialogUtil.showDialog_Prompt(mBaseActivity, "去登录","取消", "密码修改成功,请使用新密码登录。", new DialogUtil.onDialogPromptListener() {
                @Override
                public void Onclick_Dialog_Confirm_Listener(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("phone_number", getPhoneNo());
                    openActivity(LoginMenuActivity.class, bundle);
                    finish();
                }

                @Override
                public void Onclick_Dialog_Cancel_Listener(View v) {

                }
            });
//            AlertDialog.Builder builder = new Builder(this);
//            View view = View.inflate(getApplicationContext(), R.layout.dialog_user, null);
//
//            TextView tv_info = (TextView) view.findViewById(R.id.Cancellation);//提示信息
//            LinearLayout confirm = (LinearLayout) view.findViewById(R.id.confirm);// 确定
//
//            LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);// 取消
//            TextView tv_btn0 = (TextView) view.findViewById(R.id.exitBtn0);
//            tv_btn0.setText("去登录");
//            tv_info.setText("密码修改成功,请使用新密码登录。");
//            builder.setView(view);
//            final AlertDialog dialog = builder.create();
//            dialog.show();
//            confirm.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString("phone_number", getPhoneNo());
//                    openActivity(LoginMenuActivity.class, bundle);
//                    finish();
//                    if (dialog != null) {
//                        dialog.dismiss();
//                    }
//                }
//            });
//            cancel.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (dialog != null) dialog.dismiss();
//                }
//            });

        } else {
            mal_tv_notice.setText(bm.getMsg());
            mal_tv_notice.setVisibility(View.VISIBLE);


        }
        UIHelper.showToast(getApplicationContext(), bm.getMsg());
    }



    private void startGetCode() {
        mbtn_verification_code.setVisibility(View.INVISIBLE);
        mrl_verification_code.setVisibility(View.VISIBLE);
//        mbtn_verification_code.setEnabled(false);
//        mbtn_verification_code.setTextColor(getResources().getColor(R.color.black));
        mc = new MyCount2(60000, 1000);
        mc.start();
        // 获取手机验证码的时候，设置手机号不可编辑
        met_phone_number.setEnabled(false);
        met_phone_number.setTextColor(getResources().getColor(R.color.gray_stroke));
    }

    // 获取手机号
    public String getPhoneNo() {
        return met_phone_number.getText().toString().trim();
    }

    // 获取手机验证码
    public String getSecutrity() {
        return met_verification_code.getText().toString().trim();
    }

    // 获取真实姓名
    public String getReal_name() {
        return mbtn_real_name.getText().toString().trim();
    }

    // 确认密码
    public String getNewpsd_set() {
        return mbtn_set_newpassword.getText().toString().trim();
    }

    // 隐藏提示信息
    private void hideNotice() {
        if (mal_tv_notice.getVisibility() == View.VISIBLE) mal_tv_notice.setVisibility(View.INVISIBLE);
        mal_tv_notice.setText(null);
    }

    @Override
    public void onSuccess(ReturnDataBean returnDataBean) {
        setRegisterReturnCode(returnDataBean);
    }

    @Override
    public void onError(String error_str, Exception e) {
        showToast(error_str);
    }

    /* 定义一个倒计时的内部类 */
    class MyCount2 extends CountDownTimer {
        public MyCount2(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            setMyCountTimerCancel();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mtv_second.setText(""+millisUntilFinished / 1000);
//            mbtn_verification_code.setText("重新获取 (" + millisUntilFinished / 1000 + ")");
        }
    }

    private void setMyCountTimerCancel() {

        // 设置手机号可以编辑setFocusable
        met_phone_number.setEnabled(true);
        met_phone_number.setTextColor(getResources().getColor(R.color.black));

        mrl_verification_code.setVisibility(View.INVISIBLE);
        mbtn_verification_code.setVisibility(View.VISIBLE);

//        mbtn_verification_code.setEnabled(true);
//        mbtn_verification_code.setTextColor(getResources().getColor(R.color.white));
//        mbtn_verification_code.setText("获取验证码");
        if (mc != null) {
            mc.cancel();
            mc = null;
        }
    }


    //        new AsyncTask<String, String, String>() {
    //            protected String doInBackground(String... params) {
    //                String return_str = RequestHttpManager.getGetPhoneCode(params[0]);
    //                return return_str;
    //            }
    //
    //            @Override
    //            protected void onPostExecute(String result) {
    //                if (result != null) {
    //                    ReturnDataBean vcb = JSON.parseObject(result, ReturnDataBean.class);
    //                    if ("true".equals(vcb.getResult())) {
    //                        UIHelper.showToast(getApplicationContext(), vcb.getMsg());
    //                        return;
    //                    } else {
    //                        UIHelper.showToast(getApplicationContext(), vcb.getMsg());
    //                    }
    //                } else {
    //                    UIHelper.showToast(getApplicationContext(), "获取短信验证码失败");
    //                }
    //                setMyCountTimerCancel();
    //            }
    //        }.execute(phonenumber);
    //    /**
    //     * @Description: (获取验证码)
    //     * @author 李巷阳
    //     * @date 2016-8-16 下午6:11:46
    //     */
    //    private void getVerfyCode() {
    //
    //        hideNotice();
    //
    //        String phonenumber = getPhoneNo();
    //
    //        if (!StringUtil.isMobileNO(phonenumber)) {
    //            UIHelper.showToast(getApplicationContext(), "请输入正确的手机号");
    //            return;
    //        }
    //
    //        // mbtn_verification_code
    //        // .setBackgroundDrawable(getResources().getDrawable(R.drawable.imb_white1));
    //        mbtn_verification_code.setEnabled(false);
    //        mbtn_verification_code.setTextColor(getResources().getColor(R.color.black));
    //        mc = new MyCount2(60000, 1000);
    //        mc.start();
    //        // 获取手机验证码的时候，设置手机号不可编辑
    //        met_phone_number.setEnabled(false);
    //        met_phone_number.setTextColor(getResources().getColor(R.color.gray_stroke));
    //
    //        new AsyncTask<String, String, String>() {
    //            protected String doInBackground(String... params) {
    //                String return_str = RequestHttpManager.getGetPhoneCode(params[0]);
    //                return return_str;
    //            }
    //
    //            @Override
    //            protected void onPostExecute(String result) {
    //                if (result != null) {
    //                    ReturnDataBean vcb = JSON.parseObject(result, ReturnDataBean.class);
    //                    if ("true".equals(vcb.getResult())) {
    //                        UIHelper.showToast(getApplicationContext(), vcb.getMsg());
    //                        return;
    //                    } else {
    //                        UIHelper.showToast(getApplicationContext(), vcb.getMsg());
    //                    }
    //                } else {
    //                    UIHelper.showToast(getApplicationContext(), "获取短信验证码失败");
    //                }
    //                setMyCountTimerCancel();
    //            }
    //        }.execute(phonenumber);
    //
    //    }
    //        showBgLoading("正在注册");
    //        new AsyncTask<String, String, String>() {
    //            protected String doInBackground(String... params) {
    //                String return_str = RequestHttpManager.getRetrievePassword(params[0], params[1], params[2], params[3]);
    //                return return_str;
    //            }
    //
    //            @Override
    //            protected void onPostExecute(String result) {
    //                if (result != null) {
    //                    ReturnDataBean bm = JSON.parseObject(result, ReturnDataBean.class);
    //                    setRegisterReturnCode(bm);
    //                } else {
    //                    UIHelper.showToast(getApplicationContext(), "注册失败");
    //                }
    //            }
    //        }.execute(phonenumber, real_name, newpsd_set, secutrity);

}
