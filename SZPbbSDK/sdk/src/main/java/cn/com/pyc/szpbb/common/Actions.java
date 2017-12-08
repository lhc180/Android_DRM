package cn.com.pyc.szpbb.common;

/**
 * 下载广播状态
 */
public final class Actions {

    // 停止某一个下载任务
    public static final String ACTION_STOP = "cn.com.pyc.szpbb.sdk.Stop_Task";
    // 停止所有下载(退出应用主页面时)
    public static final String ACTION_ALL_STOP = "cn.com.pyc.szpbb.sdk.Stop_All_Task";

    // /////------------------------下载------------------------------///////
    // 等待
    public static final String ACTION_WAITING = "cn.com.pyc.szpbb.sdk.Action_Waiting";
    // 连接
    public static final String ACTION_CONNECTING = "cn.com.pyc.szpbb.sdk.Action_Connecting";
    // 下载异常
    public static final String ACTION_DOWNLOAD_ERROR = "cn.com.pyc.szpbb.sdk.Action_Download_Error";
    // 更新
    public static final String ACTION_PROGRESS = "cn.com.pyc.szpbb.sdk.Action_Progress";
    // 完毕
    public static final String ACTION_FINISHED = "cn.com.pyc.szpbb.sdk.Action_Finished";

    // /////------------------------下载------------------------------///////

}
