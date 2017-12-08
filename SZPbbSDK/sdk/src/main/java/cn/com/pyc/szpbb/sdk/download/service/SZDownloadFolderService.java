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
import cn.com.pyc.szpbb.sdk.download.intern.DownloadFolderTaskManager;
import cn.com.pyc.szpbb.sdk.models.SZFolderInfo;
import cn.com.pyc.szpbb.util.SZLog;

/**
 * 文件夹下载任务服务service <br/>
 * <p>
 * manifest.xml文件中注册服务
 *
 * @author hudq
 */
@Deprecated
public class SZDownloadFolderService extends Service {
    private static final String TAG = "DownloadService";
    private Context context;

    // private final int count = Runtime.getRuntime().availableProcessors() * 3;

    // 下载任务的集合
    private static Map<String, DownloadFolderTaskManager> sTasks;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        if (sTasks == null) {
            sTasks = new LinkedHashMap<String, DownloadFolderTaskManager>();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SZLog.d(TAG, "onDestroy");
        if (sTasks != null) {
            sTasks.clear();
            sTasks = null;
        }
        DownloadFolderTaskManager.shutdownTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags,
                    startId);
        }

        SZFolderInfo info = (SZFolderInfo) intent.getParcelableExtra(K.TAG_FOLDERINFO);
        if (info != null) {
            startTask(info);
        }

        if (Actions.ACTION_STOP.equals(intent.getAction())) {
            if (!intent.hasExtra(K.TAG_TASKID))
                throw new IllegalArgumentException(
                        "hasExtra, extra 'taskId' must be required.");
            // 通过myProId，取出下载任务，暂停下载
            String myProId = intent.getStringExtra(K.TAG_TASKID);
            DownloadFolderTaskManager task = sTasks.get(myProId);
            if (task != null) {
                task.isPause = true;
                task.isBreak = true;
            }
        } else if (Actions.ACTION_ALL_STOP.equals(intent.getAction())) {
            // 停止所有下载
            SZLog.i("stop task ,size = " + sTasks.size());
            Iterator<String> itor = sTasks.keySet().iterator();
            while (itor.hasNext()) {
                DownloadFolderTaskManager task = sTasks.get(itor.next());
                if (task != null && !task.isPause) {
                    SZLog.d(TAG, "stop all task");
                    task.isPause = true;
                    task.isBreak = true;
                }
            }
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 启动下载任务
     *
     * @param info
     */
    private void startTask(SZFolderInfo info) {
        SZLog.d(TAG, "1.satrt download : " + info.getProductName());
        DownloadFolderTaskManager task = new DownloadFolderTaskManager(context, info);
        task.download();
        sTasks.put(info.getMyProId(), task); // 将下载任务加到map集合中
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
