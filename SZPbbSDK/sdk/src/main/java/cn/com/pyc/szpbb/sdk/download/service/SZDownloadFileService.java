package cn.com.pyc.szpbb.sdk.download.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.com.pyc.szpbb.common.Actions;
import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.download.intern.DownloadFileTaskManager;
import cn.com.pyc.szpbb.sdk.models.SZFileData;
import cn.com.pyc.szpbb.util.SZLog;

/**
 * 下载文件服务
 *
 * @author hudq
 */
public class SZDownloadFileService extends Service {
    private static final String TAG = "SZDownloadFileService";
    private Context context;
    // private final int count = Runtime.getRuntime().availableProcessors() * 3;

    /**
     * 下载任务的集合(key=文件id，value=任务)
     */
    private static Map<String, DownloadFileTaskManager> sTasks;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        if (sTasks == null) {
            sTasks = new LinkedHashMap<String, DownloadFileTaskManager>();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SZLog.v(TAG, "onDestroy");
        if (sTasks != null) {
            sTasks.clear();
            sTasks = null;
        }
        DownloadFileTaskManager.shutdownTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return super.onStartCommand(intent, flags, startId);
        SZFileData data = (SZFileData) intent.getParcelableExtra(K.TAG_FILEDATA);
        if (data != null) {
            startTask(data);
        }
        // action
        actionService(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void actionService(Intent intent) {
        String action = intent.getAction();
        if (Actions.ACTION_STOP.equalsIgnoreCase(action)) {
            if (!intent.hasExtra(K.TAG_TASKID))
                throw new IllegalArgumentException(
                        "hasExtra,'cn.com.pyc.szpbb.sdk.taskId' must be required.");

            // 通过taskId，取出下载任务，暂停下载
            String id = intent.getStringExtra(K.TAG_TASKID);
            DownloadFileTaskManager task = sTasks.get(id);
            if (task != null) {
                task.isPause = true;
                task.isBreak = true;
            }
        } else if (Actions.ACTION_ALL_STOP.equalsIgnoreCase(action)) {
            // 停止所有下载
            SZLog.i("taskCount = " + sTasks.size());
            Iterator<String> itor = sTasks.keySet().iterator();
            while (itor.hasNext()) {
                DownloadFileTaskManager task = sTasks.get(itor.next());
                if (task != null) {
                    SZLog.d(TAG, "stop all download task");
                    task.isPause = true;
                    task.isBreak = true;
                }
            }
            stopSelf();
        }
    }

    /**
     * 启动下载任务
     *
     * @param o
     */
    private void startTask(SZFileData o) {
        SZLog.i("1.satrt download : " + o.getName());
        DownloadFileTaskManager task = new DownloadFileTaskManager(context, o);
        sTasks.put(o.getFiles_id(), task);
        task.download();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
