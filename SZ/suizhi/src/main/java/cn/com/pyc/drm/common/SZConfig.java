package cn.com.pyc.drm.common;

import cn.com.pyc.drm.BuildConfig;

/**
 * 配置设置类
 *
 * @author hudq
 */
public final class SZConfig {
    /**
     * 开发模式<br/>
     * <p>
     * 调试模式=true；发布线上版本=false关闭日志打印记录
     */
    public static final boolean DEVELOPER_MODE = BuildConfig.LOG_DEBUG;

    public static final String WEIXIN_APPID = "wx13032c3eb10bdafe";
    public static final String WEIXIN_APPSECRET = "130d6023d7cd25a30b4434ba967f7e86";
    public static final String WEIXIN_SCOPE = "snsapi_userinfo"; // snsapi_base;snsapi_userinfo

    // 测试：101316083；QQ_KEY = 66a195dae7e4aed8896885858be624d8
    // 正式：101320602，QQ_KEY = f0a6d4ad15f70b552a9d89fc609f88a6
    public static final String QQ_APPID = "101320602";
    public static final String QQ_SCOPE = "all"; // “get_user_info,add_t”；所有权限用“all”

    //微博
    public static final String WEIBO_APPID = "2482136874";
    //public static final String WEIBO_APPSECRET = "3ff1b7c0280e517522a06d298e0590f9";
    //https://api.weibo.com/oauth2/default.html";
    public static final String WEIBO_REDIRECT_URL = "http://www.suizhi.com";
    public static final String WEIBO_SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog,invitation_write";

    //xml证书加密秘钥
    public static final String XML_SECRET = "819A4D283457A9DFB704C1B4F11CB512";
    //文件秘钥加密秘钥
    public static final String FILE_KEY_SECRET = "CB28646F6674C796BFAA44FC686992AD";
    //albumInfo加密秘钥
    public static final String ALBUMINFO_SECRET = "80F008F8C906098FCE93A89B3DB2EF4E";
    //authkey2使用盐值
    public static final String LOGIN_SALT = "A7070C3015B81AB0E5C1CF5F94D84BF7";


    //下载最新版apk地址
    public static final String APK_NEW_URL = "http://114.112.104.137:8080/android/suizhi.apk";

    /**
     * 登录方式(区分账号登录和扫码登录)
     *
     * @author hudq
     */
    public static class LoginConfig {
        // 默认账号密码登录
        public static int type = DrmPat.LOGIN_GENERAL;
    }

}
