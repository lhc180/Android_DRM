package cn.com.pyc.drm.common;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import org.xutils.common.Callback;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import cn.com.pyc.drm.model.DataModel;
import cn.com.pyc.drm.utils.AESUtil;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.SecurityUtil;
import cn.com.pyc.drm.utils.help.DownloadHelp;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.loger.intern.ExtraParams;
import cn.com.pyc.loger.intern.LoginType;
import cn.com.pyc.loger.intern._LogHttp;

/**
 * 关于日志记相关
 * <p>
 * Created by hudq on 2016/11/29.
 */

public class LogConfig {

    /**
     * 反射调用，设置请求的URL。(一般在APP的onCreate中设置)
     *
     * @param url 设置url
     */
    public static void setLogHttpUrl(String url) {
        setField(_LogHttp.create(), "sUrl", url);
    }

    /**
     * 获取基本的额外参数，包括用户名、密码、登录方式
     *
     * @return ExtraParams
     */
    public static ExtraParams getBaseExtraParams() {
        ExtraParams extraParams = new ExtraParams();
        extraParams.account_password = AESUtil.encrypt(Constant.getLoginPwd());
        extraParams.account_name = Constant.getName();
        extraParams.login_type = getLoginType();
        return extraParams;
    }

    /**
     * 获取登录类型
     *
     * @return @link LoginType
     */
    public static String getLoginType() {
        if (SZConfig.LoginConfig.type == DrmPat.LOGIN_GENERAL) {
            return LoginType.ACCOUNT.toString();
        } else if (SZConfig.LoginConfig.type == DrmPat.LOGIN_WECHAT) {
            return LoginType.WECHAT.toString();
        } else if (SZConfig.LoginConfig.type == DrmPat.LOGIN_QQ) {
            return LoginType.QQ.toString();
        }
        return LoginType.NO.toString();
    }

    private static void setField(Object owner, String fieldName, Object value) {
        try {
            Class<?> ownerClass = owner.getClass();
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 阅读日志记录接口
     * <p>
     * (参数顺序不可变)
     *
     * @param begin 是否开始记录标志
     */
    public static void fileReadLog(String myProductId, String collectionId,
                                   final String itemId, final boolean begin) {
        if (TextUtils.isEmpty(myProductId) || TextUtils.isEmpty(itemId))
            return;

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("application_name", DrmPat.APP_FULLNAME);
        map.put("app_version", CommonUtil.getAppVersionName(App.getInstance()));
        map.put("IMEI", Constant.IMEI);
        map.put("username", Constant.getName());
        map.put("token", Constant.getToken());

        map.put("myProductId", myProductId);
        map.put("collectionId", collectionId);
        map.put("itemId", itemId);
        map.put("state", begin ? "begin" : "end");
        map.put("logId", begin ? "" : getLogId(itemId));
        String authKey = SecurityUtil.encodeMD5BySalt(DownloadHelp.getMapParamsString(map),
                SZConfig.LOGIN_SALT);
        map.put("authKey", authKey);
        HttpEngine.post(APIUtil.fileReadLog(), map, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                DRMLog.d("fileReadLog", begin ? " begin :" + result : "end: " + result);
                DataModel dataModel = JSON.parseObject(result, DataModel.class);
                if (dataModel.isYes(Code.SUCCESS) && dataModel.getData() != null) {
                    SPUtils.save("log_" + itemId, dataModel.getData().getLogId());
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                DRMLog.d("fileReadLog", "onError:" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private static String getLogId(String fileId) {
        String logId = (String) SPUtils.get("log_" + fileId, "");
        SPUtils.remove("log_" + fileId);
        return logId;
    }
}
