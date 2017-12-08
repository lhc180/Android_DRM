package cn.com.pyc.szpbb.sdk.synchttp;

import android.os.Bundle;

import org.xutils.common.Callback.Cancelable;

import java.util.HashMap;
import java.util.Map;

import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBack;
import cn.com.pyc.szpbb.sdk.manager.SZHttpEngine;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetVerifyCode;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetVerifyCode;
import cn.com.pyc.szpbb.util.APIUtil;

/**
 * 获取验证码
 *
 * @author hudq
 */
public class SZGetVerifyCodeHttp extends SZBaseHttp {

    /**
     * 获取验证码
     * <p>
     * 验证码登录时，type="login";
     * 其他时候设置为空或不设置
     *
     * @param r
     * @param callBack
     */
    public void getVerifyCode(SZRequestGetVerifyCode r,
                              final ISZCallBack<SZResponseGetVerifyCode> callBack) {
        Bundle params = new Bundle();
        params.putString("username", r.phone);
        params.putString("login", r.type);

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        Cancelable c = SZHttpEngine.get(APIUtil.getVerifyCodeUrl(),
                params,
                header,
                sCommonBack.getCommonCallback(SZRequestGetVerifyCode.class, callBack));

        sRequestManager.put(SZRequestGetVerifyCode.class, c);
    }
}
