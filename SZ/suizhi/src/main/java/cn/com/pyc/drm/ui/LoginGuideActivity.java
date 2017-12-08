package cn.com.pyc.drm.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.common.Constants;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.LogConfig;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.dialog.TVAnimDialog;
import cn.com.pyc.drm.dialog.TVAnimDialog.OnTVAnimDialogClickListener;
import cn.com.pyc.drm.model.BaseModel;
import cn.com.pyc.drm.model.LoginModel;
import cn.com.pyc.drm.model.LoginModel.LoginInfo;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DataMergeUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.PathUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.DRMDBHelper;
import cn.com.pyc.drm.utils.help.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.widget.FlatButton;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.ToastShow;
import cn.com.pyc.loger.LogerEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern.LogerHelp;
import de.greenrobot.event.EventBus;

/**
 * 登录指示页
 *
 * @author hudq
 */
@Deprecated
public class LoginGuideActivity extends BaseAssistActivity implements OnClickListener {
    private static final String TAG = "LoginGuide";
    private IWXAPI api;
    private Tencent tencent;
    private Cancelable mQQCancelable;
    private List<String> idsSum = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_guide);
        initView();
        getValue();
    }

    protected void initView() {
        FlatButton weChatBtn = (FlatButton) findViewById(R.id.btn_wechat_login);
        FlatButton QQBtn = (FlatButton) findViewById(R.id.btn_qq_login);
        HighlightImageView szBtn = (HighlightImageView) findViewById(R.id.iv_sz_login);
        TextView tvVersion = (TextView) findViewById(R.id.txt_version);
        TextView tvRegister = (TextView) findViewById(R.id.tv_register);
        final String versionName = CommonUtil.getAppVersionName(this);
        tvVersion.setText(getString(R.string.version_name, versionName));

        weChatBtn.setOnClickListener(this);
        QQBtn.setOnClickListener(this);
        szBtn.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    protected void getValue() {
        api = WXAPIFactory.createWXAPI(this, SZConfig.WEIXIN_APPID, true);
        EventBus.getDefault().register(this);
        // 如果是重复登陆则弹出对话框。
        boolean isRepeatLogin = getIntent().getBooleanExtra(DRMUtil.KEY_REPEAT_LOGIN, false);
        if (isRepeatLogin) {
            ViewUtil.showSingleCommonDialog(this,
                    getString(R.string.tip_title),
                    getString(R.string.repeat_login),
                    getString(R.string.xml_dialog_delete_positive), null);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        HttpEngine.cancelHttp(mQQCancelable);
    }

    @Override
    public void onBackPressed() {
        // UIHelper.showExitTips(this);
        showExitDialog();
    }

    private void showExitDialog() {
        ViewUtil.showContentDialog(this, "", new OnTVAnimDialogClickListener() {
            @Override
            public void onClick(int dialogId) {
                if (dialogId == TVAnimDialog.DIALOG_CONFIRM) {
                    finish();
                    ActicityManager.getInstance().exit();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_wechat_login:
                weChatLogin();
                break;
            case R.id.btn_qq_login:
                QQLogin();
                break;
            case R.id.iv_sz_login:
                szLogin();
                break;
            case R.id.tv_register:
                openActivity(RegisterActivity.class);
                break;
            default:
                break;
        }
    }

    /**
     * 微信登录
     */
    private void weChatLogin() {
        SZConfig.LoginConfig.type = DrmPat.LOGIN_WECHAT;
        showToast(getString(R.string.please_waiting));
        if (api == null)
            return;
        if (!api.isWXAppInstalled()) {
            showToast("没有安装微信客户端");
            String url = "http://android.myapp.com/myapp/detail.htm?apkName=com.tencent.mm";
            OpenPageUtil.openBrowserOfSystem(this, url);
            return;
        }
        if (!api.registerApp(SZConfig.WEIXIN_APPID)) {
            showToast("应用注册到微信失败");
            return;
        }
        if (!senAuth()) {
            showToast("启动微信授权失败");
            //return;
        }
        // 成功
    }

    /**
     * 登录授权
     *
     * @return
     */
    private boolean senAuth() {
        final SendAuth.Req req = new SendAuth.Req();

        req.scope = SZConfig.WEIXIN_SCOPE;

        req.state = "suizhi_wechat_login";

        return api.sendReq(req);
    }

    /**
     * qq登录
     */
    private void QQLogin() {
        SZConfig.LoginConfig.type = DrmPat.LOGIN_QQ;
        showToast(getString(R.string.please_waiting));
        tencent = Tencent.createInstance(SZConfig.QQ_APPID, this.getApplicationContext());
        // //如果session无效，就开始登录
        if (tencent != null && !tencent.isSessionValid()) {
            // 登录
            tencent.login(this, SZConfig.QQ_SCOPE, mQQLoginUIlistener);

            // tencent.setOpenId(QQ_openid);
            // tencent.setAccessToken(QQ_token, QQ_expires_in);
            // tencent.login(this, SZConfig.QQ_SCOPE, QQUIlistener);
            //
            // if (tencent != null && tencent.isSessionValid())
            // {
            // // 本地已经有保存openid和accesstoken的情况下，先调用mTencent.setAccesstoken().
            //
            // } else
            // {
            // // 未登陆提示用户使用QQ号登陆
            // }
        }
    }

    /**
     * sz账号登录
     */
    private void szLogin() {
        OpenPageUtil.openActivity(this, LoginActivity.class);
        finish();
        UIHelper.startInAnim(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, mQQLoginUIlistener);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private IUiListener mQQLoginUIlistener = new IUiListener() {

        @Override
        public void onCancel() {
            DRMLog.d("登录取消");
        }

        @Override
        public void onComplete(Object arg0) {
            DRMLog.d(TAG, "onComplete: " + arg0.toString());
            parserQQJson(arg0.toString());
        }

        @Override
        public void onError(UiError arg0) {
            DRMLog.d(TAG, "onError: " + arg0.errorMessage);
            showToast(getString(R.string.load_server_failed));
        }
    };

    private void parserQQJson(String json) {
        if (TextUtils.isEmpty(json))
            return;

        try {
            JSONObject object = new JSONObject(json);
            int code = object.getInt("ret");
            if (code == 0) {
                String access_token = object.optString(Constants.PARAM_ACCESS_TOKEN);
                String openid = object.optString(Constants.PARAM_OPEN_ID);
                long expires_in = object.optLong(Constants.PARAM_EXPIRES_IN);
                long valid_time = System.currentTimeMillis() + (expires_in * 1000); // 过期时间
                // =
                // 当前时间+有效时间

                SPUtils.save(DRMUtil.KEY_QQ_ACCESS_TOKEN, access_token);
                SPUtils.save(DRMUtil.KEY_QQ_OPENID, openid);
                SPUtils.save(DRMUtil.KEY_QQ_EXPIRES_IN, valid_time);

                // 跳转主页面（获取qq用户信息）
                getUserInfo(openid, access_token, String.valueOf(expires_in));

            } else {
                showToast("QQ登录失败！");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取用户信息
     *
     * @param openId
     * @param token
     * @param expire_in
     */
    private void getUserInfo(String openId, String token, String expire_in) {
        QQToken qqToken = tencent.getQQToken();
        qqToken.setOpenId(openId);
        qqToken.setAccessToken(token, expire_in);
        com.tencent.connect.UserInfo info = new UserInfo(getApplicationContext(), qqToken);
        info.getUserInfo(mQQUserInfoListener);
    }

    private IUiListener mQQUserInfoListener = new IUiListener() {

        @Override
        public void onError(UiError arg0) {
        }

        @Override
        public void onComplete(Object arg0) {
            String json = arg0.toString();
            DRMLog.d(json);
            if (TextUtils.isEmpty(json))
                return;
            try {
                JSONObject object = new JSONObject(json);
                int code = object.getInt("ret");
                if (code == 0) {
                    showBgLoading(getString(R.string.please_waiting));
                    postQQLogin(object);
                } else {
                    showToast("获取用户信息失败");
                }
            } catch (Exception e) {
                showToast("Invalid json format");
            }
        }

        @Override
        public void onCancel() {
        }
    };

    protected void postQQLogin(JSONObject object) {
        final String openid = (String) SPUtils.get(DRMUtil.KEY_QQ_OPENID, "");
        String access_token = (String) SPUtils.get(DRMUtil.KEY_QQ_ACCESS_TOKEN, "");
        String nickname = object.optString("nickname");
        String figureurl_qq2 = object.optString("figureurl_qq_2");

        final Bundle bundle = new Bundle();
        bundle.putString("openId", openid);
        bundle.putString("accessToken", access_token);
        bundle.putString("nickname", nickname);
        bundle.putString("figureurl", figureurl_qq2);

        mQQCancelable = HttpEngine.post(APIUtil.getQQLoginUrl(), bundle, new Callback
                .CommonCallback<String>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                DRMLog.d(TAG, "onFailed: " + arg0.getMessage());
                hideBgLoading();
                showToast(getString(R.string.load_server_failed));
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(String arg0) {
                DRMLog.d(TAG, "ok:" + arg0);
                LoginModel o = JSON.parseObject(arg0, LoginModel.class);
                if (o != null && o.isSuccess()) {
                    LoginInfo oo = o.getData();
                    loginSuccess(oo, openid);
                } else {
                    addLog("QQ登录失败：" + arg0 + "，参数：" + bundle.toString());
                    ToastShow.getToast().showFail(getApplicationContext(), "登录失败~");
                    hideBgLoading();
                    finish();
                }
            }
        });
    }

    private void loginSuccess(LoginInfo o, String openid) {
        Constant.setAccountId(o.getAccountId());
        Constant.setName(o.getUsername());
        Constant.setToken(o.getToken());

        SPUtils.save(DRMUtil.KEY_VISIT_NAME, o.getUsername());
        SPUtils.save(DRMUtil.KEY_VISIT_PWD, openid);
        //SPUtils.save(DRMUtil.KEY_REMEMBER_NAME, o.getUsername());
        //SPUtils.save(DRMUtil.KEY_REMEMBER_PWD, openid);

        // 创建数据库和文件保存目录
        DRMDBHelper.destoryDBHelper();
        DRMDBHelper drmDB = new DRMDBHelper(this.getApplicationContext());
        drmDB.createDBTable();
        PathUtil.createFilePath();

        // 成功，注册设备信息
        registerDeviceInfo(LoginGuideActivity.this, null);
    }


    /**
     * 微信登陆成功，接收通知，结束当前页面
     *
     * @param event
     */
    public void onEventMainThread(ConductUIEvent event) {
        if (event.getType() == BaseEvent.Type.UI_LOGINGUIDE_FINISH) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (!LoginGuideActivity.this.isFinishing())
                        LoginGuideActivity.this.finish();
                }
            }, 2000);
        }
    }

    /**
     * 注册设备信息
     *
     * @param atc      当前activity上下文
     * @param textView 用来显示注册结果,为null不显示
     */
    public static void registerDeviceInfo(final BaseAssistActivity atc, final TextView textView) {
        Bundle bundle = DRMUtil.getEquipmentInfos();
        HttpEngine.post(APIUtil.getEquipmentInfoUrl(), bundle, new Callback
                .CommonCallback<String>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                DRMLog.e("onError： " + arg0.getMessage());
                atc.showToast(atc.getString(R.string.register_device_fail));
            }

            @Override
            public void onFinished() {
                atc.hideBgLoading();
            }

            @Override
            public void onSuccess(String result) {
                DRMLog.d("onSuccess: " + result);
                BaseModel model = JSON.parseObject(result, BaseModel.class);
                if (model == null) {
                    atc.showToast(atc.getString(R.string.register_device_fail));
                    return;
                }
                if (model.isSuccess()) {
                    signSuccess(atc);
                } else {
                    atc.showToast(model.getMsg());
                    if (textView != null) {
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(model.getMsg());
                    }
                }
            }
        });
    }

    private static void signSuccess(BaseAssistActivity activity) {
        SPUtils.save(DRMUtil.KEY_EQUIPMENT, true);
        DRMUtil.isFirstToMain = false;
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_LOGINGUIDE_FINISH));

        List<String> idsSum = new DataMergeUtil<>().getAllBuyMyProductIdList();
        if (idsSum.isEmpty()) {  //本地sd卡中myProId为空，直接回主界面
            activity.openActivity(HomeActivity.class);
        } else {
            if (!(boolean) SPUtils.get(DRMUtil.KEY_CHECK_DATA, false)) {  //没有检查过数据，去检查数据页面
                SPUtils.save(DRMUtil.KEY_CHECK_DATA, true);//标志已检查过
                activity.openActivity(IntermediaryActivity.class);
            } else {
                activity.openActivity(HomeActivity.class);
            }
        }
        activity.finish();
    }

    //List集合转化成逗号分隔的字符串
    private static String convertIds(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            builder.append(id).append(",");
        }
        return builder.toString();
    }

    //添加日志记录
    private static void addLog(String content) {
        ExtraParams extraParams = LogConfig.getBaseExtraParams();
        extraParams.file_name = LogerHelp.getFileName();
        LogerEngine.debug(App.getInstance(), content, extraParams);
    }

}
