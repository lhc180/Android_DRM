package cn.com.pyc.drm.utils;

public class ClearKeyUtil {

    /**
     * 登录方式所有的值的key
     */
    public static void removeKey() {
        removeUserKey();
        removeWXKey();
        removeQQKey();
    }

    /**
     * 账号密码登录保存值key
     */
    public static void removeUserKey() {
        //SPUtils.remove(DRMUtil.KEY_REMEMBER_NAME);
        //SPUtils.remove(DRMUtil.KEY_REMEMBER_PWD);
        SPUtils.remove(DRMUtil.KEY_ACCOUNT_ID);
        SPUtils.remove(DRMUtil.KEY_SUPER_TOKEN);
        SPUtils.remove(DRMUtil.KEY_SUPER_NAME);
        SPUtils.remove(DRMUtil.KEY_SELECT_PRO_ID);
    }

    /**
     * 微信登录保存值key
     */
    public static void removeWXKey() {
        SPUtils.remove(DRMUtil.KEY_WECHAT_ACCESS_TOKEN);
        SPUtils.remove(DRMUtil.KEY_WECHAT_EXPIRES_IN);
        SPUtils.remove(DRMUtil.KEY_WECHAT_OPENID);
        SPUtils.remove(DRMUtil.KEY_WECHAT_REFRESH_TOKEN);
        SPUtils.remove(DRMUtil.KEY_WECHAT_ACCESS_TOKEN);
        SPUtils.remove(DRMUtil.KEY_WECHAT_UNIONID);
    }

    /**
     * qq登录值key
     */
    public static void removeQQKey() {
        SPUtils.remove(DRMUtil.KEY_QQ_ACCESS_TOKEN);
        SPUtils.remove(DRMUtil.KEY_QQ_OPENID);
        SPUtils.remove(DRMUtil.KEY_QQ_EXPIRES_IN);
    }

}
