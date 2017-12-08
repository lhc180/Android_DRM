package cn.com.pyc.szpbb.sdk.synchttp;

import android.os.Bundle;
import android.text.TextUtils;

import org.xutils.common.Callback.Cancelable;

import java.util.HashMap;
import java.util.Map;

import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBack;
import cn.com.pyc.szpbb.sdk.callback.ISZCallBackArray;
import cn.com.pyc.szpbb.sdk.manager.SZHttpEngine;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetAllReceiveShare;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetShareFile;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetShareInfo;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetAllReceiveShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetShareFile;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetShareInfo;
import cn.com.pyc.szpbb.util.APIUtil;

/**
 * 获取分享,分享信息,分享文件
 *
 * @author hudq
 */
public class SZGetSharedHttp extends SZBaseHttp {

    /**
     * 获取分享信息接口(有分享receiveId,使用二次分享获取接口)
     *
     * @param r
     * @param callBack
     */
    public void getShareInfo(SZRequestGetShareInfo r,
                             final ISZCallBack<SZResponseGetShareInfo> callBack) {
        Bundle params = new Bundle();
        params.putString("shareId", r.shareId);
        params.putString("myToken", r.token);
        // ******** url根据receiveId设置*************
        String actionUrl = APIUtil.getShareInfoUrl();
        if (!TextUtils.isEmpty(r.receiveId)) {
            params.putString("receiveId", r.receiveId);
            actionUrl = APIUtil.getShareInfo2Url();
        }

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        Cancelable c = SZHttpEngine.get(actionUrl,
                params,
                header,
                sCommonBack.getCommonCallback(SZRequestGetShareInfo.class, callBack));

        sRequestManager.put(SZRequestGetShareInfo.class, c);
    }

    /**
     * @param r
     * @param callBack
     * @Description: (根据分享文件夹ID获取分享文件)
     * @author 李巷阳
     * @date 2016-9-12 下午4:19:06
     * @version V1.0
     * <p>
     * 根据分享文件夹ID获取分享文件
     * <p>
     * shareFolderId：不可为空
     * </p>
     */

    public void getShareFile(SZRequestGetShareFile r,
                             final ISZCallBack<SZResponseGetShareFile> callBack) {
        Bundle params = new Bundle();
        params.putString("shareFolderId", r.shareFolderId);
        params.putString("myToken", r.token);

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        Cancelable c = SZHttpEngine.post(APIUtil.getShareFileUrl(),
                params,
                header,
                sCommonBack.getCommonCallback(SZRequestGetShareFile.class, callBack));

        sRequestManager.put(SZRequestGetShareFile.class, c);
    }

    /**
     * @param r
     * @param callBack
     * @Description: (登录成功根据token获取所有创建的分享)
     * @author 李巷阳
     * @date 2016-9-12 下午4:19:06
     * @version V1.0
     * <p>
     * 登录成功根据token获取所有创建的分享。
     * <p>
     * token：不可为空
     * </p>
     */

    public void getAllReceiveShare(SZRequestGetAllReceiveShare r, final
    ISZCallBackArray<SZResponseGetAllReceiveShare> callBack) {
        Bundle params = new Bundle();
        params.putString("myToken", r.token);

        Map<String, String> header = new HashMap<>();
        header.put(K.HEADER_KEY, K.HEADER_VALUE);

        Cancelable c = SZHttpEngine.post(APIUtil.getAllReceiveSharesUrl(),
                params,
                header,
                sCommonBack.getCommonCallback(SZRequestGetAllReceiveShare.class, callBack));

        sRequestManager.put(SZRequestGetShareFile.class, c);
    }

}
