package cn.com.pyc.szpbb.common;

import cn.com.pyc.szpbb.sdk.bean.FILEFormat;

/**
 * 字段设置
 */
public class Fields {
    // 常量类型字符串
    /**
     * 设备imei，如果没有则取值是mac地址
     */
    public static final String IMEI = Constant.DEVICEID;
    /**
     * permission:odd_individual，即绑定的授权用户
     **/
    public static final String INDIVIDUAL = "guestpbb";
    //public static final String _LINE = "_";
    //public static final String DOT = ".";
    //public static final String UTF_8 = "UTF-8";
    //public static final String platform = "Android";
    //public static final String sZipFileMimeType = "application/zip";

    // 扩展名
    public static final String _DRM = ".drm";
    public static final String _MP4 = ".mp4";
    public static final String _MP3 = ".mp3";
    public static final String _PDF = ".pdf";

    // 文件夹类别
    //public static final String VIDEO = "VIDEO";
    //public static final String MUSIC = "MUSIC";
    //public static final String BOOK = "BOOK";

    // 文件类型
    public static final String PDF = FILEFormat.PDF.name();
    public static final String MP3 = FILEFormat.MP3.name();
    public static final String MP4 = FILEFormat.MP4.name();

}
