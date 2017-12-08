package cn.com.pyc.szpbb.sdk.download;

import android.content.Context;
import android.content.Intent;

import cn.com.pyc.szpbb.common.Actions;
import cn.com.pyc.szpbb.common.DownloadState;
import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.SZDownloadInterface;
import cn.com.pyc.szpbb.sdk.models.SZFileData;
import cn.com.pyc.szpbb.util.SZLog;

public abstract class SZDownloadFileSubscriber extends BaseSubscriber<SZFileData> {

    public SZDownloadFileSubscriber() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null) return;
        if (!intent.hasExtra(K.TAG_FILEDATA))
            throw new IllegalArgumentException(
                    "hasExtra, 'cn.com.pyc.szpbb.sdk.SZFileData' must be required.");

        SZFileData o = intent.getParcelableExtra(K.TAG_FILEDATA);
        if (null == intent.getAction()) return;
        SZLog.d("dr2", "position = " + o.getPosition());

        switch (intent.getAction()) {
            case Actions.ACTION_PROGRESS: {
                long currentSize = intent.getLongExtra(K.TAG_CURRENTSIZE, 0L);
                int progress = intent.getIntExtra(K.TAG_PROGRESS, 0);
                boolean isLast = intent.getBooleanExtra(K.TAG_LASTUPDATE, false);
                int state = isLast ? DownloadState.PAUSE : DownloadState.DOWNLOADING;
                o.setTaskState(state);
                o.setProgress(progress);
                downloadProgress(o, progress, currentSize);
            }
            break;
            case Actions.ACTION_WAITING: {
                o.setTaskState(DownloadState.WAITING);
                waiting(o);
            }
            break;
            case Actions.ACTION_CONNECTING: {
                o.setTaskState(DownloadState.CONNECTING);
                connecting(o);
            }
            break;
            case Actions.ACTION_DOWNLOAD_ERROR: {
                SZDownloadInterface.stopTask(context, o); //停止该任务
                o.setTaskState(DownloadState.DOWNLOAD_ERROR);
                downloadFailed(o);
            }
            break;
            case Actions.ACTION_FINISHED: {
                //SZDownloadInterface.stopTask(context, o); //停止该任务
                o.setTaskState(DownloadState.FINISHED);
                downloadFinished(o);
            }
            break;
            default:
                break;
        }
    }

}
