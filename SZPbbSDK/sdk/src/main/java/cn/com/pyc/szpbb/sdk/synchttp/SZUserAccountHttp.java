package cn.com.pyc.szpbb.sdk.synchttp;

import android.os.Bundle;

import org.xutils.common.Callback.Cancelable;

import java.util.HashMap;
import java.util.Map;

import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBack;
import cn.com.pyc.szpbb.sdk.manager.SZHttpEngine;
import cn.com.pyc.szpbb.sdk.response.SZResponseLogin;
import cn.com.pyc.szpbb.sdk.response.SZResponseLoginout;
import cn.com.pyc.szpbb.sdk.response.SZResponseRegister;
import cn.com.pyc.szpbb.sdk.response.SZResponseVerifyCodeLogin;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestLogin;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestLoginout;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestRegister;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestVerifyCodeLogin;
import cn.com.pyc.szpbb.util.APIUtil;

/**
 * http请求类，登录,登出，注册
 *
 * @author hudq
 */
public class SZUserAccountHttp extends SZBaseHttp {

    /**
     * 登录
     *
     * @param login
     * @param callBack
     */
    public void login(SZRequestLogin login,
                      final ISZCallBack<SZResponseLogin> callBack) {
        Bundle params = new Bundle();
        params.putString("username", login.username);
        params.putString("password", login.password);

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        Cancelable cancelable = SZHttpEngine.post(APIUtil.getLoginUrl(),
                params,
                header,
                sCommonBack.getCommonCallback(SZRequestLogin.class, callBack));

        sRequestManager.put(SZRequestLogin.class, cancelable);
    }

    /**
     * 验证码登录
     *
     * @param vcLogin
     * @param callBack
     */
    public void verifyCodeLogin(SZRequestVerifyCodeLogin vcLogin,
                                final ISZCallBack<SZResponseVerifyCodeLogin> callBack) {
        Bundle params = new Bundle();
        params.putString("username", vcLogin.phone);
        params.putString("validateCode", vcLogin.verifyCode);

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        Cancelable c = SZHttpEngine.post(APIUtil.getVerifyCodeLoginUrl(),
                params,
                header,
                sCommonBack.getCommonCallback(SZRequestVerifyCodeLogin.class, callBack));

        sRequestManager.put(SZRequestVerifyCodeLogin.class, c);
    }

    /**
     * @Description: (注册接口)
     * @author 李巷阳
     * @date 2016-9-12 下午6:24:29
     * @version V1.0
     */
    public void register(SZRequestRegister r,
                         final ISZCallBack<SZResponseRegister> callBack) {
        Bundle params = new Bundle();
        params.putString("username", r.username);
        params.putString("password", r.password);
        params.putString("validateCode", r.verifyCode);

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        Cancelable c = SZHttpEngine.post(APIUtil.getRegisterUrl(),
                params,
                header,
                sCommonBack.getCommonCallback(SZRequestRegister.class, callBack));

        sRequestManager.put(SZRequestRegister.class, c);

    }

    /**
     * 登出 账号
     *
     * @param r
     * @param callBack
     */
    public void loginout(SZRequestLoginout r,
                         ISZCallBack<SZResponseLoginout> callBack) {

        Bundle params = new Bundle();
        params.putString("username", r.username);
        params.putString("token", r.token);

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        Cancelable c = SZHttpEngine.post(APIUtil.getExitLoginUrl(),
                params,
                header,
                sCommonBack.getCommonCallback(SZRequestLoginout.class, callBack));

        sRequestManager.put(SZRequestLoginout.class, c);

    }

}
