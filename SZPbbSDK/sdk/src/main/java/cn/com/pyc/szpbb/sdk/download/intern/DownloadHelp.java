package cn.com.pyc.szpbb.sdk.download.intern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;

import java.util.LinkedHashSet;
import java.util.Set;

import cn.com.pyc.szpbb.common.Actions;
import cn.com.pyc.szpbb.common.DownloadState;
import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.download.service.SZDownloadFileService;
import cn.com.pyc.szpbb.sdk.download.service.SZDownloadFolderService;
import cn.com.pyc.szpbb.sdk.models.SZFileData;
import cn.com.pyc.szpbb.sdk.models.SZFolderInfo;

public class DownloadHelp {

    private static volatile Set<String> sTaskIdSet = new LinkedHashSet<>();

    /*
     * 注册下载广播信息
     *
     * @param context
     *
     * @param receiver
     *
     * @return The first sticky intent found that matches filter, or null if
     * there are none.
     */
    public static Intent registerReceiver(Context context,
                                          BroadcastReceiver receiver) {
        if (receiver == null)
            throw new IllegalArgumentException("receiver must be init.");

        IntentFilter downloadFilter = new IntentFilter();
        downloadFilter.addAction(Actions.ACTION_WAITING);
        downloadFilter.addAction(Actions.ACTION_CONNECTING);
        downloadFilter.addAction(Actions.ACTION_DOWNLOAD_ERROR);
        downloadFilter.addAction(Actions.ACTION_PROGRESS);
        downloadFilter.addAction(Actions.ACTION_FINISHED);
        return context.registerReceiver(receiver, downloadFilter);
    }

    /*
     * 反注册下载广播信息
     *
     * @param context
     *
     * @param receiver
     */
    public static String unRegisterReceiver(Context context,
                                            BroadcastReceiver receiver) {
        if (receiver == null)
            throw new IllegalArgumentException("receiver must be init.");

        String receiverName = receiver.getClass().getName();
        context.unregisterReceiver(receiver);
        return receiverName;
    }

    /*
     * 是否添加了此任务
     * @param data
     * @return
     */
    public static boolean isAdd(Parcelable data) {
        if (data == null) {
            throw new IllegalArgumentException("args'data'not allow null");
        }
        String taskId = null;
        if (data instanceof SZFileData) {
            taskId = ((SZFileData) data).getFiles_id();
        } else if (data instanceof SZFolderInfo) {
            taskId = ((SZFolderInfo) data).getMyProId();
        } else {
            throw new IllegalArgumentException("args'data' must instanceof SZFileData,or " +
                    "SZFolderInfo");
        }
        return sTaskIdSet != null && sTaskIdSet.contains(taskId);
    }

    private static void remove(String taskId) {
        if (sTaskIdSet != null) {
            sTaskIdSet.remove(taskId);
        }
    }

    private static void add(String taskId) {
        if (sTaskIdSet != null) {
            sTaskIdSet.add(taskId);
        }
    }

    private static void clear() {
        if (sTaskIdSet != null) {
            sTaskIdSet.clear();
        }
    }

    /*
     * 添加下载任务
     *
     * @param ctx
     * @param data
     */
    public static void addTask(Context ctx, Parcelable data) {
        if (data == null) {
            throw new IllegalArgumentException("args'data'not allow null");
        }

        if (data instanceof SZFileData) { //文件下载
            SZFileData o = ((SZFileData) data);
            o.setTaskState(DownloadState.WAITING);
            ctx.sendBroadcast(new Intent(Actions.ACTION_WAITING)
                    .putExtra(K.TAG_FILEDATA, o));

            add(o.getFiles_id());
            Intent intent = new Intent(ctx, SZDownloadFileService.class);
            intent.putExtra(K.TAG_FILEDATA, o);
            ctx.startService(intent);
        } else if (data instanceof SZFolderInfo) { //文件夹下载
            SZFolderInfo o = ((SZFolderInfo) data);
            o.setTaskState(DownloadState.WAITING);
            ctx.sendBroadcast(new Intent(Actions.ACTION_WAITING)
                    .putExtra(K.TAG_FOLDERINFO, o));

            add(o.getMyProId());
            Intent intent = new Intent(ctx, SZDownloadFolderService.class);
            intent.putExtra(K.TAG_FOLDERINFO, o);
            ctx.startService(intent);
        } else {
            throw new IllegalArgumentException("args'data' must instanceof SZFileData,or " +
                    "SZFolderInfo");
        }
    }

    /*
     * 取消下载任务
     *
     * @param ctx
     * @param data
     */
    public static void removeTask(Context ctx, Parcelable data) {
        if (data == null) {
            throw new IllegalArgumentException("args'data'not allow null");
        }

        String taskId = null;
        Intent intent = null;
        if (data instanceof SZFileData) {
            SZFileData o = ((SZFileData) data);
            o.setTaskState(DownloadState.PAUSE);
            taskId = o.getFiles_id();
            intent = new Intent(ctx, SZDownloadFileService.class);
        } else if (data instanceof SZFolderInfo) {
            SZFolderInfo o = ((SZFolderInfo) data);
            o.setTaskState(DownloadState.PAUSE);
            taskId = o.getMyProId();
            intent = new Intent(ctx, SZDownloadFolderService.class);
        }

        if (intent == null || taskId == null) {
            throw new IllegalArgumentException("args'data' must instanceof SZFileData,or " +
                    "SZFolderInfo");
        }

        remove(taskId);
        intent.setAction(Actions.ACTION_STOP);
        intent.putExtra(K.TAG_TASKID, taskId);
        ctx.startService(intent);
    }

    /**
     * 停止所有文件下载任务
     *
     * @param ctx android.content.Context
     */
    public static void stopFileAllTask(Context ctx) {
        Intent intent = new Intent(ctx, SZDownloadFileService.class);
        intent.setAction(Actions.ACTION_ALL_STOP);
        ctx.startService(intent);
        clear();
    }

    /**
     * 停止所有文件夹的下载任务
     *
     * @param ctx android.content.Context
     */
    public static void stopFolderAllTask(Context ctx) {
        Intent intent = new Intent(ctx, SZDownloadFolderService.class);
        intent.setAction(Actions.ACTION_ALL_STOP);
        ctx.startService(intent);
        clear();
    }

}
