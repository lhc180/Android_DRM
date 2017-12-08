package cn.com.pyc.szpbb.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import cn.com.pyc.szpbb.sdk.download.BaseSubscriber;
import cn.com.pyc.szpbb.sdk.download.intern.DownloadHelp;
import cn.com.pyc.szpbb.sdk.models.SZFileData;
import cn.com.pyc.szpbb.sdk.models.SZFolderInfo;

/**
 * 下载文件处理
 */
public abstract class SZDownloadInterface {

    /**
     * 注册下载广播
     *
     * @param ctx android.content.Context
     * @param s   BaseSubscriber
     * @return The first sticky intent found that matches filter, or null if
     * there are none.
     */
    public static Intent register(Context ctx, BaseSubscriber s) {
        return DownloadHelp.registerReceiver(ctx, s);
    }

    /**
     * 反注册下载广播
     *
     * @param ctx android.content.Context
     * @param s   BaseSubscriber
     * @return sName
     */
    public static String unregister(Context ctx, BaseSubscriber s) {
        return DownloadHelp.unRegisterReceiver(ctx, s);
    }

    /**
     * 开启下载
     *
     * @param ctx  android.content.Context
     * @param data {@link SZFileData} or {@link SZFolderInfo}
     */
    public static void startTask(Context ctx, Parcelable data) {
        DownloadHelp.addTask(ctx, data);
    }

    /**
     * 停止其中一个下载任务
     *
     * @param ctx  android.content.Context
     * @param data {@link SZFileData} or {@link SZFolderInfo}
     */
    public static void stopTask(Context ctx, Parcelable data) {
        DownloadHelp.removeTask(ctx, data);
    }

    /**
     * 停止所有文件下载任务
     *
     * @param ctx android.content.Context
     */
    public static void stopFileTask(Context ctx) {
        DownloadHelp.stopFileAllTask(ctx);
    }

    /**
     * 停止所有文件夹的下载任务
     *
     * @param ctx android.content.Context
     */
    public static void stopFolderTask(Context ctx) {
        DownloadHelp.stopFolderAllTask(ctx);
    }

    /**
     * 下载任务是否开启
     *
     * @param data {@link SZFileData} or {@link SZFolderInfo}
     * @return boolean
     */
    public static boolean isStartTask(Parcelable data) {
        return DownloadHelp.isAdd(data);
    }

}
