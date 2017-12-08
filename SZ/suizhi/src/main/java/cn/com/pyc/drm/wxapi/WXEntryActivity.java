package cn.com.pyc.drm.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.SZConfig;
import cn.com.pyc.drm.model.LoginModel;
import cn.com.pyc.drm.model.LoginModel.LoginInfo;
import cn.com.pyc.drm.ui.BaseAssistActivity;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.OtherLoginUtil;
import cn.com.pyc.drm.utils.PathUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.help.DRMDBHelper;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.widget.ToastShow;

/**
 * 请求微信回调
 */
public class WXEntryActivity extends BaseAssistActivity implements IWXAPIEventHandler {
    private String TAG = "WXEntry";
    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;

    private Cancelable mWXCancelable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, SZConfig.WEIXIN_APPID, false);

        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq arg0) {
    }

    @Override
    public void onResp(BaseResp arg0) {
//        switch (arg0.errCode) {
//            case BaseResp.ErrCode.ERR_OK: {
//                SendAuth.Resp resp = (SendAuth.Resp) arg0; // 用于分享时不要有这个，不能强转
//                getToken(resp.code);
//            }
//            break;
//            case BaseResp.ErrCode.ERR_USER_CANCEL:
//            case BaseResp.ErrCode.ERR_AUTH_DENIED:
//            default:
//                Toast.makeText(getApplicationContext(), "登录取消", Toast.LENGTH_SHORT).show();
//                finish();
//                break;
//        }
        wxHandleByType(arg0);
    }

    private void wxHandleByType(BaseResp arg0) {
        switch (arg0.getType()) {
            case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX://分享回调
            {
                if (arg0.errCode == BaseResp.ErrCode.ERR_OK) {
                    //分享成功
                } else if (arg0.errCode == BaseResp.ErrCode.ERR_USER_CANCEL
                        || arg0.errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                    //showToast("分享取消");
                    finish();
                }
            }
            break;
            case ConstantsAPI.COMMAND_SENDAUTH: //登录回调
            {
                if (arg0.errCode == BaseResp.ErrCode.ERR_OK) {
                    SendAuth.Resp resp = (SendAuth.Resp) arg0; // 用于分享时不要有这个，不能强转
                    getToken(resp.code);
                } else if (arg0.errCode == BaseResp.ErrCode.ERR_USER_CANCEL
                        || arg0.errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                    showToast("登录取消");
                    finish();
                }
            }
            break;
        }
    }


    /**
     * 通过code请求access_token
     *
     * @param code
     */
    private void getToken(String code) {
        String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + SZConfig
                .WEIXIN_APPID + "&secret=" + SZConfig.WEIXIN_APPSECRET + "&code=" + code +
                "&grant_type=authorization_code";

        showBgLoading(getString(R.string.logining));

        HttpEngine.get(path, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                showToast("请求微信授权失败");
                hideBgLoading();
                finish();
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(String arg0) {
                Log.v(TAG, "tok: " + arg0);
                try {
                    JSONObject json = new JSONObject(arg0);
                    String openid = json.getString("openid").toString().trim();
                    String access_token = json.getString("access_token").toString().trim();
                    String expires_in = json.getString("expires_in").toString().trim();
                    String refresh_token = json.getString("refresh_token").toString().trim();

                    SPUtils.save(DRMUtil.KEY_WECHAT_ACCESS_TOKEN, access_token);
                    SPUtils.save(DRMUtil.KEY_WECHAT_EXPIRES_IN, expires_in);
                    SPUtils.save(DRMUtil.KEY_WECHAT_REFRESH_TOKEN, refresh_token);
                    SPUtils.save(DRMUtil.KEY_WECHAT_OPENID, openid);

                    refeshToken(refresh_token);

                    getUid(openid, access_token);

                } catch (Exception e) {
                    showToast("微信客户端授权失败");
                    hideBgLoading();
                    finish();
                }
            }
        });
    }

    /**
     * 获取用户唯一标识
     *
     * @param openId
     * @param accessToken
     */

    private void getUid(String openId, String accessToken) {

        String path = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken +
                "&openid=" + openId;

        HttpEngine.get(path, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                showToast("请求微信登录失败");
                hideBgLoading();
                finish();
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(String arg0) {
                Log.v(TAG, "uid: " + arg0);
                try {
                    JSONObject json = new JSONObject(arg0);
                    String unionid = json.getString("unionid");
                    String openid = json.getString("openid");
                    SPUtils.save(DRMUtil.KEY_WECHAT_UNIONID, unionid);
                    SPUtils.save(DRMUtil.KEY_WECHAT_OPENID, openid);
                    // TODO：后台接口交互
                    postWXLogin(unionid, openid, json);
                } catch (Exception e) {
                    showToast("微信登录失败!");
                    hideBgLoading();
                    finish();
                }
            }
        });
    }

    protected void postWXLogin(final String unionid, final String openid, JSONObject json) {
        String nickname = json.optString("nickname");
        String sex = json.optString("sex");
        String province = json.optString("province");
        String city = json.optString("city");
        String country = json.optString("country");

        final Bundle bundle = new Bundle();
        bundle.putString("openid", openid); // 必传
        bundle.putString("unionid", unionid); // 必传
        bundle.putString("nickname", nickname);
        bundle.putString("sex", sex);
        bundle.putString("province", province);
        bundle.putString("city", city);
        bundle.putString("country", country);
        mWXCancelable = HttpEngine.post(APIUtil.getWXLoginUrl(), bundle, new Callback
                .CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                ToastShow.getToast().showFail(getApplicationContext(), "请求微信登录失败");
                hideBgLoading();
                finish();
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(String arg0) {
                DRMLog.d("login-ok : " + arg0);
                LoginModel o = JSON.parseObject(arg0, LoginModel.class);
                if (o != null && o.isSuccess()) {
                    LoginInfo oo = o.getData();
                    loginSuccess(oo, unionid);
                } else {
                    ToastShow.getToast().showFail(getApplicationContext(), "微信登录失败~");
                    hideBgLoading();
                    finish();
                }
            }
        });
    }

    private void loginSuccess(LoginInfo o, String unionid) {
        Constant.setAccountId(o.getAccountId());
        Constant.setName(o.getUsername());
        Constant.setToken(o.getToken());

        SPUtils.save(DRMUtil.KEY_VISIT_NAME, o.getUsername());
        SPUtils.save(DRMUtil.KEY_VISIT_PWD, unionid);

        //SPUtils.save(DRMUtil.KEY_REMEMBER_NAME, o.getUsername());
        //SPUtils.save(DRMUtil.KEY_REMEMBER_PWD, unionid);

        // 创建数据库和文件保存目录
        DRMDBHelper.destoryDBHelper();
        DRMDBHelper drmDB = new DRMDBHelper(this.getApplicationContext());
        drmDB.createDBTable();
        PathUtil.createFilePath();

        // 成功，注册设备信息
        OtherLoginUtil loginUtil = new OtherLoginUtil(this);
        if (!TextUtils.isEmpty((String) SPUtils.get("temp_webBuy_url", ""))) {
            //购买时点击登录
            loginUtil.registerDeviceInfo2((String) SPUtils.get("temp_webBuy_url", ""));
            SPUtils.remove("temp_webBuy_url");
        } else {
            loginUtil.registerDeviceInfo(null);
        }
    }

    // 1.若access_token已超时，那么进行refresh_token会获取一个新的access_token，新的超时时间；
    // 2.若access_token未超时，那么进行refresh_token不会改变access_token，
    // 但超时时间会刷新，相当于续期access_token。
    private void refeshToken(String refresh_token) {
        String path = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=" + SZConfig
                .WEIXIN_APPID + "&grant_type=refresh_token&refresh_token=" + refresh_token;

        HttpEngine.get(path, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                Toast.makeText(getApplicationContext(), "请求失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinished() {
            }

            @Override
            public void onSuccess(String arg0) {
                Log.i(TAG, "ref-tok: " + arg0);
                try {
                    JSONObject json = new JSONObject(arg0);
                    String openid = json.getString("openid").toString().trim();
                    String access_token = json.getString("access_token").toString().trim();
                    String expires_in = json.getString("expires_in").toString().trim();
                    String refresh_token2 = json.getString("refresh_token").toString().trim();

                    SPUtils.save(DRMUtil.KEY_WECHAT_ACCESS_TOKEN, access_token);
                    SPUtils.save(DRMUtil.KEY_WECHAT_EXPIRES_IN, expires_in);
                    SPUtils.save(DRMUtil.KEY_WECHAT_REFRESH_TOKEN, refresh_token2);
                    SPUtils.save(DRMUtil.KEY_WECHAT_OPENID, openid);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "请求认证失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HttpEngine.cancelHttp(mWXCancelable);
    }

}
