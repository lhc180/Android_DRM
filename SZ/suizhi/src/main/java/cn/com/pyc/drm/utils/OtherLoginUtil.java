package cn.com.pyc.drm.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.model.BaseModel;
import cn.com.pyc.drm.model.LoginModel;
import cn.com.pyc.drm.ui.BaseAssistActivity;
import cn.com.pyc.drm.ui.HomeActivity;
import cn.com.pyc.drm.ui.IntermediaryActivity;
import cn.com.pyc.drm.ui.LoginActivity;
import cn.com.pyc.drm.utils.help.DRMDBHelper;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.widget.ToastShow;
import de.greenrobot.event.EventBus;

/**
 * 其他登录方式
 * Created by songyumei on 2017/7/13.
 */

public class OtherLoginUtil {
    private static String TAG = OtherLoginUtil.class.getSimpleName();
    private BaseAssistActivity mContext;
    private IWXAPI api;
    private Tencent tencent;
    private Callback.Cancelable mQQCancelable;

    public OtherLoginUtil(BaseAssistActivity context) {
        mContext = context;
        Context ctx = mContext.getApplicationContext();
        api = WXAPIFactory.createWXAPI(ctx, SZConfig.WEIXIN_APPID, true);
        tencent = Tencent.createInstance(SZConfig.QQ_APPID, ctx);
    }

    public void cancelable() {
        HttpEngine.cancelHttp(mQQCancelable);
    }

    /**
     * 微信登录
     */
    public void weChatLogin() {
        SZConfig.LoginConfig.type = DrmPat.LOGIN_WECHAT;
        mContext.showToast(mContext.getString(R.string.please_waiting));
        if (api == null)
            return;
        if (!api.isWXAppInstalled()) {
            mContext.showToast("没有安装微信客户端");
            String url = "http://android.myapp.com/myapp/detail.htm?apkName=com.tencent.mm";
            OpenPageUtil.openBrowserOfSystem(mContext, url);
            return;
        }
        if (!api.registerApp(SZConfig.WEIXIN_APPID)) {
            mContext.showToast("应用注册到微信失败");
            return;
        }
        if (!senAuth()) {
            mContext.showToast("启动微信授权失败");
            //return;
        }
        // 成功
    }

    public void weChatLogin2(String webBuyUrl) {
        SZConfig.LoginConfig.type = DrmPat.LOGIN_WECHAT;
        mContext.showToast(mContext.getString(R.string.please_waiting));
        if (api == null)
            return;
        if (!api.isWXAppInstalled()) {
            mContext.showToast("没有安装微信客户端");
            String url = "http://android.myapp.com/myapp/detail.htm?apkName=com.tencent.mm";
            OpenPageUtil.openBrowserOfSystem(mContext, url);
            return;
        }
        if (!api.registerApp(SZConfig.WEIXIN_APPID)) {
            mContext.showToast("应用注册到微信失败");
            return;
        }
        if (!senAuth()) {
            mContext.showToast("启动微信授权失败");
            //return;
        }
        //购买时跳转到登录传递一个url作为标志,在WXEntryActivity中读取
        SPUtils.save("temp_webBuy_url", webBuyUrl);
        // 成功
    }

    /**
     * 登录授权
     */
    private boolean senAuth() {
        final SendAuth.Req req = new SendAuth.Req();

        req.scope = SZConfig.WEIXIN_SCOPE;

        req.state = "suizhi_wechat_login";

        return api.sendReq(req);
    }

//    public void showToast(String msg) {
//        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
//    }

    /**
     * qq登录
     */
    public void QQLogin() {
        SZConfig.LoginConfig.type = DrmPat.LOGIN_QQ;
        mContext.showToast(mContext.getString(R.string.please_waiting));
        // //如果session无效，就开始登录
        if (tencent != null && !tencent.isSessionValid()) {
            // 登录
            tencent.login(mContext, SZConfig.QQ_SCOPE, mQQLoginUIListener);

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

    public IUiListener mQQLoginUIListener = new IUiListener() {

        @Override
        public void onCancel() {
            DRMLog.d("登录取消");
            mContext.showToast("QQ登录取消");
        }

        @Override
        public void onComplete(Object arg0) {
            DRMLog.d(TAG, "onComplete: " + arg0.toString());
            parserQQJson(arg0.toString());
        }

        @Override
        public void onError(UiError arg0) {
            DRMLog.d(TAG, "onError: " + arg0.errorMessage);
            mContext.showToast(mContext.getString(R.string.load_server_failed));
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
                // 过期时间=当前时间+有效时间
                long valid_time = System.currentTimeMillis() + (expires_in * 1000);

                SPUtils.save(DRMUtil.KEY_QQ_ACCESS_TOKEN, access_token);
                SPUtils.save(DRMUtil.KEY_QQ_OPENID, openid);
                SPUtils.save(DRMUtil.KEY_QQ_EXPIRES_IN, valid_time);

                // 跳转主页面（获取qq用户信息）
                getUserInfo(openid, access_token, String.valueOf(expires_in));

            } else {
                mContext.showToast("QQ登录失败！");
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
        com.tencent.connect.UserInfo info = new UserInfo(mContext, qqToken);
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
                    postQQLogin(object);
                } else {
                    mContext.showToast("获取用户QQ信息失败");
                }
            } catch (Exception e) {
                mContext.showToast("Invalid json format");
            }
        }

        @Override
        public void onCancel() {
        }
    };

    private void postQQLogin(JSONObject object) {
        mContext.showBgLoading(mContext.getString(R.string.please_waiting));
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
                mContext.hideBgLoading();
                mContext.showToast(mContext.getString(R.string.load_server_failed));
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(String arg0) {
                DRMLog.d(TAG, "ok:" + arg0);
                LoginModel o = JSON.parseObject(arg0, LoginModel.class);
                if (o != null && o.isSuccess()) {
                    LoginModel.LoginInfo oo = o.getData();
                    loginSuccess(oo, openid);
                } else {
                    ToastShow.getToast().showFail(mContext, "QQ登录失败~");
                    mContext.hideBgLoading();
                    mContext.finish();
                }
            }
        });
    }

    private void loginSuccess(LoginModel.LoginInfo o, String openid) {
        Constant.setAccountId(o.getAccountId());
        Constant.setName(o.getUsername());
        Constant.setToken(o.getToken());

        SPUtils.save(DRMUtil.KEY_VISIT_NAME, o.getUsername());
        SPUtils.save(DRMUtil.KEY_VISIT_PWD, openid);
        //SPUtils.save(DRMUtil.KEY_REMEMBER_NAME, o.getUsername());
        //SPUtils.save(DRMUtil.KEY_REMEMBER_PWD, openid);

        // 创建数据库和文件保存目录
        DRMDBHelper.destoryDBHelper();
        DRMDBHelper drmDB = new DRMDBHelper(mContext);
        drmDB.createDBTable();
        PathUtil.createFilePath();

        // 成功，注册设备信息
        registerDeviceInfo(null);
    }

    /**
     * 注册设备信息
     *
     * @param textView 用来显示注册结果,为null不显示
     */
    public void registerDeviceInfo(final TextView textView) {
        Bundle bundle = DRMUtil.getEquipmentInfos();
        HttpEngine.post(APIUtil.getEquipmentInfoUrl(), bundle, new Callback
                .CommonCallback<String>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                DRMLog.e("onError： " + arg0.getMessage());
                mContext.showToast(mContext.getString(R.string.register_device_fail));
                //注册设备失败，移除保存的key值
                ClearKeyUtil.removeKey();
            }

            @Override
            public void onFinished() {
                mContext.hideBgLoading();
            }

            @Override
            public void onSuccess(String result) {
                DRMLog.d("onSuccess: " + result);
                BaseModel model = JSON.parseObject(result, BaseModel.class);
                if (model == null) {
                    //注册设备失败，移除保存的key值
                    ClearKeyUtil.removeKey();
                    mContext.showToast(mContext.getString(R.string.register_device_fail));
                    return;
                }
                if (model.isSuccess()) {
                    signSuccess();
                } else {
                    //注册设备失败，移除保存的key值
                    ClearKeyUtil.removeKey();
                    if (textView != null) {
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(model.getMsg());
                    } else {
                        mContext.showToast(model.getMsg());
                    }
                }
            }
        });
    }

    public void registerDeviceInfo2(final String webUrl) {
        Bundle bundle = DRMUtil.getEquipmentInfos();
        HttpEngine.post(APIUtil.getEquipmentInfoUrl(), bundle, new Callback
                .CommonCallback<String>() {
            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                DRMLog.e("onError： " + arg0.getMessage());
                mContext.showToast(mContext.getString(R.string.register_device_fail));
                //注册设备失败，移除保存的key值
                ClearKeyUtil.removeKey();
            }

            @Override
            public void onFinished() {
                mContext.hideBgLoading();
            }

            @Override
            public void onSuccess(String result) {
                DRMLog.d("onSuccess: " + result);
                BaseModel model = JSON.parseObject(result, BaseModel.class);
                if (model == null) {
                    //注册设备失败，移除保存的key值
                    ClearKeyUtil.removeKey();
                    mContext.showToast(mContext.getString(R.string.register_device_fail));
                    return;
                }
                if (model.isSuccess()) {
                    signSuccess2(webUrl);
                } else {
                    //注册设备失败，移除保存的key值
                    ClearKeyUtil.removeKey();
                    mContext.showToast(model.getMsg());
                }
            }
        });
    }

    private void signSuccess() {
        SPUtils.save(DRMUtil.KEY_EQUIPMENT, true);
        DRMUtil.isFirstToMain = false;
        ////EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_LOGINGUIDE_FINISH));
        //登录成功通知发现界面更新状态
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UPDATE_DISCOVER));

        List<String> idsSum = new DataMergeUtil<>().getAllBuyMyProductIdList();
        if (idsSum.isEmpty()) {  //本地sd卡中myProId为空，直接回主界面
            mContext.openActivity(HomeActivity.class);
        } else {
            if (!(boolean) SPUtils.get(DRMUtil.KEY_CHECK_DATA, false)) {  //没有检查过数据，去检查数据页面
                SPUtils.save(DRMUtil.KEY_CHECK_DATA, true);//标志已检查过
                mContext.openActivity(IntermediaryActivity.class);
            } else {
//                EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_HOME_FINISH));
                int intExtra = mContext.getIntent().getIntExtra(KeyHelp.SWICH_CONTENT_2, 0);
                if (intExtra == Constant.CONTENT_LOGIN) {
                    //通知主界面切换到我的内容
                    EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type
                            .UI_HOME_TAB_1_LOGIN));
                }
                if (intExtra == Constant.SETTING_LOGIN) {
                    //通知主界面切换到个人中心
                    EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type
                            .UI_HOME_TAB_3_LOGIN));
                }
                if (intExtra == Constant.DISCOVER_LOGIN) {
                    //通知主界面切换到发现
                    EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_DIS_LOGIN));
                    //刷新我的内容页面
                    ConductUIEvent conductUIEvent = new ConductUIEvent(BaseEvent.Type
                            .UI_HOME_TAB_1);
                    conductUIEvent.setNet(true);
                    EventBus.getDefault().post(conductUIEvent);
                }
                mContext.startActivity(new Intent(mContext, HomeActivity.class)
                        .putExtra(KeyHelp.LOGIN_FLAG, Constant.LOGIN_TYPE));
            }
        }
        LoginActivity.verifyAppVersion();
        mContext.finish();
    }

    private void signSuccess2(String webUrl) {
        SPUtils.save(DRMUtil.KEY_EQUIPMENT, true);
        DRMUtil.isFirstToMain = false;
        ////EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_LOGINGUIDE_FINISH));
        // 从web页立即购买点击后登录成功,不用通知发现界面更新状态（保证加载session统一），
        // BrowserActivity页面会重新加载带token的url
        //EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UPDATE_DISCOVER));

        LoginActivity.verifyAppVersion();
        mContext.setResult(Activity.RESULT_OK, new Intent().putExtra("web_url", webUrl));
        mContext.finish();
        //第三方登录方式时，通知登录页结束
        EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UI_LOGIN_FINISH));
    }
}
