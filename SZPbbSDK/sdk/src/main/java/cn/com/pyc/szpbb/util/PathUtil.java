package cn.com.pyc.szpbb.util;

import android.os.Environment;

import cn.com.pyc.szpbb.common.K;

public class PathUtil {
    /**
     * 文件解析保存路径username初始值为null，登陆后需重新初始化,注销后置为null <br/>
     * eg:sdcard/Android/data/SZOnline/name/file
     */
    public volatile static String DEF_FILE_PATH = null;

    /**
     * 文件下载保存路径 username初始值为null，登陆后需重新初始化 ,注销后置为null <br/>
     * eg:sdcard/Android/data/SZOnline/name/download
     */
    public volatile static String DEF_DOWNLOAD_PATH = null;

    /**
     * 手机SD卡根目录：sdCard/
     *
     * @return
     */
    private static final String getSDcardRoot() {
        return Environment.getExternalStorageDirectory().toString() + "/";
    }

    /**
     * 文件存储位置路径偏移
     *
     * @return offsetPathName <br/>
     * default value: Android/data/PBB.Guest/
     */
    private static final String getSZOffset() {
        String offsetName = (String) SPUtil.get(K.FIELDS_BASE_PATH, "");
        if (offsetName == null || offsetName.length() == 0 || "null".equals(offsetName)) {
            return "Android/data/PBB.Guest/";
        }
        return offsetName + "/";
    }

    /**
     * 创建保存下载文件的目录； 登录之后创建以name命名的目录
     *
     * @param name 名称
     */
    public static void createSaveFilePath(String name) {
        if ("".equals(name))
            throw new IllegalArgumentException("name is not allow empty string");

        destorySaveFilePath();
        if (DEF_FILE_PATH == null) {
            DEF_FILE_PATH = getFilePath(name);
        }

        if (DEF_DOWNLOAD_PATH == null) {
            DEF_DOWNLOAD_PATH = getDRMPath(name);
        }

        FileUtil.createDirectory(DEF_FILE_PATH);
        FileUtil.createDirectory(DEF_DOWNLOAD_PATH);

        //SZLog.v("create fileDirs success: " + DEF_FILE_PATH);
        //SZLog.v("create downloadDirs success: " + DEF_DOWNLOAD_PATH);
    }

    /**
     * 下载前检查路径
     */
    public static void checkFilePath(String name) {
        if ("".equals(name))
            throw new IllegalArgumentException("name is not allow empty string");
        if (DEF_FILE_PATH == null) {
            DEF_FILE_PATH = getFilePath(name);
        }
        FileUtil.createDirectory(DEF_FILE_PATH);

        if (DEF_DOWNLOAD_PATH == null) {
            DEF_DOWNLOAD_PATH = getDRMPath(name);
        }
        FileUtil.createDirectory(DEF_DOWNLOAD_PATH);
    }

    /**
     * 保存下载文件的目录； 用户名切换后，对应的用户目录重新初始化
     */
    public static void destorySaveFilePath() {
        DEF_FILE_PATH = null;
        DEF_DOWNLOAD_PATH = null;
    }


    /**
     * 下载DRM包保存路径 <br/>
     * eg:sdCard/Android/data/PBB.Pyc/name/download
     *
     * @param name 名称
     * @return
     */
    private static String getDRMPath(String name) {
        final StringBuilder mDownloadSb = new StringBuilder();
        return mDownloadSb
                .append(getSDcardRoot())
                .append(getSZOffset())
                .append(name)
                .append("/download")
                .toString();
    }

    /**
     * 文件存储路径 <br/>
     * eg:sdCard/Android/data/PBB.Pyc/name/file
     *
     * @param name 名称
     * @return
     */
    private static String getFilePath(String name) {
        final StringBuilder mFileSb = new StringBuilder();
        return mFileSb
                .append(getSDcardRoot())
                .append(getSZOffset())
                .append(name)
                .append("/file")
                .toString();
    }

}
