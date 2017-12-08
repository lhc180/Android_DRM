package cn.com.pyc.szpbb.sdk.synchttp;

import android.os.Bundle;

import org.xutils.common.Callback.Cancelable;

import java.util.HashMap;
import java.util.Map;

import cn.com.pyc.szpbb.common.Fields;
import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBack;
import cn.com.pyc.szpbb.sdk.manager.SZHttpEngine;
import cn.com.pyc.szpbb.sdk.response.SZResponseDownloadCheck;
import cn.com.pyc.szpbb.sdk.response.SZResponseReceiveShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestDownloadCheck;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestReceiveShare;
import cn.com.pyc.szpbb.util.APIUtil;

import static cn.com.pyc.szpbb.sdk.manager.SZHttpEngine.getSyncString;

/**
 * 下载校验，接收手机号校验
 *
 * @author hudq
 */
public class SZCheckReceiveHttp extends SZBaseHttp {

    /**
     * 下载校验
     *
     * @param r        SZRequestDownloadCheck
     * @param callBack ISZCallBack<T>
     */
    public String downloadCheck(SZRequestDownloadCheck r,
                                final ISZCallBack<SZResponseDownloadCheck> callBack) {
        Bundle params = new Bundle();
        params.putString("shareFolderId", r.shareFolderId);
        params.putString("shareId", r.shareId);
        params.putString("fileId", r.fileId);
        params.putString("myToken", r.token);
        params.putString("receiveID", r.receiveId);

        params.putString("deviceIdentifier", Fields.IMEI);
        params.putString("deviceType", "Android");

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        String result = null;
        if (callBack == null) {
            result = SZHttpEngine.getSyncString(APIUtil.getDownloadCheckUrl(), params, header);
        } else {
            Cancelable c = SZHttpEngine.post(
                    APIUtil.getDownloadCheckUrl(),
                    params,
                    header,
                    sCommonBack.getCommonCallback(SZRequestDownloadCheck.class, callBack));

            sRequestManager.put(SZRequestDownloadCheck.class, c);
        }
        return result;
    }

    /**
     * 检验手机号,并接收分享
     *
     * @param r
     * @param callBack
     * @return
     */
    public String receiveByPhone(SZRequestReceiveShare r,
                                 final ISZCallBack<SZResponseReceiveShare> callBack) {
        Bundle params = new Bundle();
        params.putString("phone", r.phoneNumber);
        params.putString("shareId", r.shareId);
        params.putString("myToken", r.token);
        params.putString("deviceIdentifier", Fields.IMEI);

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        String result = null;
        if (callBack == null) {
            result = getSyncString(APIUtil.getReceiverByPhoneUrl(), params, header);
        } else {

            Cancelable c = SZHttpEngine.post(
                    APIUtil.getReceiverByPhoneUrl(),
                    params,
                    header,
                    sCommonBack.getCommonCallback(SZRequestReceiveShare.class, callBack));

            sRequestManager.put(SZRequestReceiveShare.class, c);
        }
        return result;
    }
}
