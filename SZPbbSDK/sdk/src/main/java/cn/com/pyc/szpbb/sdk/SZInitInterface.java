package cn.com.pyc.szpbb.sdk;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;
import android.view.WindowManager;

import org.xutils.x;

import java.io.File;

import cn.com.pyc.szpbb.common.Constant;
import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.bean.SZBuildParams;
import cn.com.pyc.szpbb.sdk.manager.SZInitRequest;
import cn.com.pyc.szpbb.util.FileUtil;
import cn.com.pyc.szpbb.util.PathUtil;
import cn.com.pyc.szpbb.util.SPUtil;
import cn.com.pyc.szpbb.util.SZLog;
import cn.com.pyc.szpbb.util.SecurityUtil;

/**
 * 初始化工具类
 */
public abstract class SZInitInterface {
    public static boolean isDebugMode = false;

    /**
     * 设置是否调试状态，调试状态可能会打印一些log信息
     *
     * @param isDebug 是否是调试状态
     */
    public static final void setDebugMode(boolean isDebug) {
        SZInitInterface.isDebugMode = isDebug;
    }

    /**
     * 初始化一些操作
     *
     * @param context Android上下文环境参数
     */
    public static final void init(Application context) {
        x.Ext.init(context);
        x.Ext.setDebug(false);
        Constant.init(context.getApplicationContext());
        SZInitRequest.initRequestClass();
    }

    /**
     * 设置初始化参数
     *
     * @param params 参数 {@link SZBuildParams}
     */
    public static final void setParams(SZBuildParams params) {
        params.init();
        SZLog.d("init", "name: " + getUserName(false));
    }

    /**
     * 获取分享id。
     */
    public static String getShareId() {
        return (String) SPUtil.get(K.FIELDS_SHARE, "");
    }


    /**
     * 获取用户唯一名称 <br/>
     *
     * @param onlyUserName 是否只获取用户名<br/>
     *                     true，则只获取用户名,
     *                     false，则如果分享shareId不为空，则返回 userName_shareId；
     *                     如果分享shareId为空，则同样只返回用户名。
     * @return String
     */
    public static String getUserName(boolean onlyUserName) {
        String userName = (String) SPUtil.get(K.FIELDS_NAME, "");
        return onlyUserName ? userName : getName(userName);
    }

    /*
     * 获取唯一名称<br/>
     * <p>
     * 如果分享shareId不为空，则返回 userName_shareId；<br/>
     * 如果分享shareId为空，则返回用户名
     */
    private static String getName(String userName) {
        String shareId = getShareId();
        return TextUtils.isEmpty(shareId) ? userName : userName + "_" + shareId;
    }

    /**
     * 获取用户令牌token
     *
     * @return String
     */
    public static String getToken() {
        return (String) SPUtil.get(K.FIELDS_TOKEN, "");
    }

    /**
     * 获取密码 <br/>
     * <p>
     * 读取保存的密码值 。
     *
     * @return String
     */
    @Deprecated
    public static String getPassWord() {
        String cliperPwd = (String) SPUtil.get(K.FIELDS_PWD, "");
        return SecurityUtil.decryptBASE64(cliperPwd);
    }

    /**
     * 保存到sp
     *
     * @param key
     * @param obj
     * @return
     */
    public static final boolean saveSp(String key, Object obj) {
        return SPUtil.save(key, obj);
    }

    /**
     * 从sp获取
     *
     * @param key        key值
     * @param defaultObj 默认value值
     * @return
     */
    public static final Object getSp(String key, Object defaultObj) {
        return SPUtil.get(key, defaultObj);
    }

    /**
     * 清除所有保存在sp中的数据
     */
    public static final boolean clearSP() {
        return SPUtil.clear();
    }

    /**
     * 清除指定key的value值
     *
     * @param key
     */
    public static final boolean removeSP(String key) {
        return SPUtil.remove(key);
    }


    /*
     * 创建文件保存目录<br/>
     * <p>
     * 登录成功后调用（目录路径需要使用用户名，必须实例化name后调用）
     */
//    public static final void createFilePath() {
//        String userName = getUserName(false);
//        if (userName == null || userName.length() == 0) {
//            throw new IllegalArgumentException("userName not allow null or empty.");
//        }
//        PathUtil.createSaveFilePath(userName);
//    }

    /**
     * 检查文件目录，路径为null则初始化，没有则创建。
     */
    public static final void checkFilePath() {
        PathUtil.checkFilePath(getUserName(false));
    }

    /**
     * 获取缓存文件的大小，单位byte <br/>
     * 建议放在子线程中执行
     *
     * @return long
     */
    public static final long getCacheFileSize() {
        String userName = getUserName(false);
        if (userName == null || userName.length() == 0) {
            throw new IllegalArgumentException("userName not allow null or empty.");
        }
        PathUtil.checkFilePath(userName);
        File fileDire = new File(PathUtil.DEF_FILE_PATH);
        File downloadDire = new File(PathUtil.DEF_DOWNLOAD_PATH);
        return FileUtil.getDirSize(fileDire) + FileUtil.getDirSize(downloadDire);
    }

    /**
     * 删除除本地缓存文件<br/>
     * 建议放在子线程中执行
     */
    public static final void deleteCacheFile() {
        String userName = getUserName(false);
        if (userName == null || userName.length() == 0) {
            throw new IllegalArgumentException("userName not allow null or empty.");
        }
        PathUtil.checkFilePath(userName);
        FileUtil.deleteAllFile(PathUtil.DEF_FILE_PATH + "/");
        FileUtil.deleteAllFile(PathUtil.DEF_DOWNLOAD_PATH + "/");
    }

    /**
     * 截屏功能不可用
     *
     * @param acty Activity
     */
    public static final void setSecureShot(Activity acty) {
        if (acty.getWindow() != null)
            acty.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }


}
