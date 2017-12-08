package cn.com.pyc.szpbb.sdk.synchttp;

import android.os.Bundle;

import org.xutils.common.Callback.Cancelable;

import java.util.HashMap;
import java.util.Map;

import cn.com.pyc.szpbb.common.Fields;
import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBack;
import cn.com.pyc.szpbb.sdk.manager.SZHttpEngine;
import cn.com.pyc.szpbb.sdk.response.SZResponseBindShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestBindShare;
import cn.com.pyc.szpbb.util.APIUtil;

public class SZBindDeviceHttp extends SZBaseHttp {

    /**
     * 绑定设备
     *
     * @param r
     * @param callBack
     */
    public String bindDevice(SZRequestBindShare r,
                             final ISZCallBack<SZResponseBindShare> callBack) {
        Bundle params = new Bundle();
        params.putString("shareId", r.shareId);
        params.putString("shareFolderId", r.shareFolderId);
        params.putString("fileId", r.fileId);
        params.putString("deviceIdentifier", Fields.IMEI);
        params.putString("myToken", r.token);
        params.putString("receiveID", r.receiveId);// 绑定按身份创建的分享必传，绑定按设备创建的分享不必传

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        String result = null;
        if (callBack == null) {
            result = SZHttpEngine.getSyncString(APIUtil.getBindDevicesUrl(), params, header);
        } else {
            Cancelable c = SZHttpEngine.post(
                    APIUtil.getBindDevicesUrl(),
                    params,
                    header,
                    sCommonBack.getCommonCallback(SZRequestBindShare.class, callBack));

            sRequestManager.put(SZRequestBindShare.class, c);
        }
        return result;
    }

}
