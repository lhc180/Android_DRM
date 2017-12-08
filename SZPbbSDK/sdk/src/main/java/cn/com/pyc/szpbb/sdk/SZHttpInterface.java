package cn.com.pyc.szpbb.sdk;

import org.xutils.common.Callback.Cancelable;

import cn.com.pyc.szpbb.sdk.callback.ISZCallBack;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBackArray;
import cn.com.pyc.szpbb.sdk.callback.SZCreateFactory;
import cn.com.pyc.szpbb.sdk.manager.SZHttpEngine;
import cn.com.pyc.szpbb.sdk.manager.SZRequestManager;
import cn.com.pyc.szpbb.sdk.response.SZResponseBindShare;
import cn.com.pyc.szpbb.sdk.response.SZResponseDownloadCheck;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetAllReceiveShare;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetShareFile;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetShareInfo;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetVerifyCode;
import cn.com.pyc.szpbb.sdk.response.SZResponseLogin;
import cn.com.pyc.szpbb.sdk.response.SZResponseLoginout;
import cn.com.pyc.szpbb.sdk.response.SZResponseReceiveShare;
import cn.com.pyc.szpbb.sdk.response.SZResponseRegister;
import cn.com.pyc.szpbb.sdk.response.SZResponseVerifyCodeLogin;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestBindShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestDownloadCheck;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetAllReceiveShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetShareFile;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetShareInfo;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetVerifyCode;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestLogin;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestLoginout;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestReceiveShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestRegister;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestVerifyCodeLogin;

/**
 * http请求接口
 *
 * @author hudq
 */
public final class SZHttpInterface {
    static {
        factory = SZCreateFactory.create();
    }

    private static final SZCreateFactory factory;

    /**
     * 取消某个http请求
     *
     * @param request Class
     */
    public static void cancelHttp(Class<?> request) {
        Cancelable c = SZRequestManager.getInstance().get(request);
        SZHttpEngine.cancelHttp(c);
    }

    /**
     * 登录
     *
     * @param params   SZRequestLogin
     * @param callBack ISZCallBack
     */
    public static void login(SZRequestLogin params,
                             ISZCallBack<SZResponseLogin> callBack) {
        factory.getUserHttp().login(params, callBack);
    }

    /**
     * 获取验证码
     *
     * @param params   SZRequestGetVerifyCode
     * @param callBack ISZCallBack
     */
    public static void getVerifyCode(SZRequestGetVerifyCode params,
                                     ISZCallBack<SZResponseGetVerifyCode> callBack) {
        factory.getVerifyCodeHttp().getVerifyCode(params, callBack);
    }

    /**
     * 手机验证码登录
     *
     * @param params   SZRequestVerifyCodeLogin
     * @param callBack ISZCallBack
     */
    public static void verifyCodeLogin(SZRequestVerifyCodeLogin params,
                                       ISZCallBack<SZResponseVerifyCodeLogin> callBack) {
        factory.getUserHttp().verifyCodeLogin(params, callBack);
    }

    /**
     * 注册
     *
     * @param params   SZRequestRegister
     * @param callBack ISZCallBack
     */
    @Deprecated
    public static void register(SZRequestRegister params,
                                ISZCallBack<SZResponseRegister> callBack) {
        factory.getUserHttp().register(params, callBack);
    }

    /**
     * 注销，退出账号
     *
     * @param params   SZRequestLoginout
     * @param callBack ISZCallBack
     */
    public static void loginout(SZRequestLoginout params,
                                ISZCallBack<SZResponseLoginout> callBack) {
        factory.getUserHttp().loginout(params, callBack);
    }

    /**
     * 下载校验
     *
     * @param params   SZRequestDownloadCheck
     * @param callBack ISZCallBack ,callBack==null 则表示同步请求访问
     */
    public static String downloadCheck(SZRequestDownloadCheck params,
                                       ISZCallBack<SZResponseDownloadCheck> callBack) {
        return factory.getCheckReceiveHttp().downloadCheck(params, callBack);
    }

    /**
     * 接收分享
     *
     * @param params   SZRequestReceiveShare
     * @param callBack ISZCallBack ,callBack==null 则表示同步请求访问
     */
    public static String receiveShare(SZRequestReceiveShare params,
                                      ISZCallBack<SZResponseReceiveShare> callBack) {
        return factory.getCheckReceiveHttp().receiveByPhone(params, callBack);
    }

    /**
     * 绑定分享设备
     *
     * @param params   SZRequestBindShare
     * @param callBack ISZCallBack ,callBack==null 则表示同步请求访问
     */
    public static String bindShare(SZRequestBindShare params,
                                   ISZCallBack<SZResponseBindShare> callBack) {
        return factory.getBindDeviceHttp().bindDevice(params, callBack);
    }

    /**
     * 获取分享详情信息
     *
     * @param params   SZRequestGetShareInfo
     * @param callBack ISZCallBack
     */
    public static void getShareInfo(SZRequestGetShareInfo params,
                                    ISZCallBack<SZResponseGetShareInfo> callBack) {
        factory.getShareInfoHttp().getShareInfo(params, callBack);
    }

    /**
     * 获取分享详情下的文件
     *
     * @param params   SZRequestGetShareFile
     * @param callBack ISZCallBack
     */
    public static void getShareFile(SZRequestGetShareFile params,
                                    ISZCallBack<SZResponseGetShareFile> callBack) {
        factory.getShareInfoHttp().getShareFile(params, callBack);
    }

    /**
     * 获取所有接收的分享
     *
     * @param params   SZRequestGetAllReceiveShare
     * @param callBack ISZCallBackArray
     */
    public static void getAllShare(SZRequestGetAllReceiveShare params,
                                          ISZCallBackArray<SZResponseGetAllReceiveShare> callBack) {
        factory.getShareInfoHttp().getAllReceiveShare(params, callBack);
    }

}
