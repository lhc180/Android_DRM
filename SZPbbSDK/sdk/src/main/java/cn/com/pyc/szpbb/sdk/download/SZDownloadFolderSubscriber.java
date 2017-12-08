package cn.com.pyc.szpbb.sdk.download;

import android.content.Context;
import android.content.Intent;

import cn.com.pyc.szpbb.common.Actions;
import cn.com.pyc.szpbb.common.DownloadState;
import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.models.SZFolderInfo;

@Deprecated
public abstract class SZDownloadFolderSubscriber extends BaseSubscriber<SZFolderInfo> {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null) return;
        if (!intent.hasExtra(K.TAG_FOLDERINFO))
            throw new IllegalArgumentException(
                    "hasExtra, extra must be required.");

        SZFolderInfo o = intent.getParcelableExtra(K.TAG_FOLDERINFO);
        if (null == intent.getAction()) return;

        switch (intent.getAction()) {
            case Actions.ACTION_PROGRESS: {
                if (!intent.hasExtra(K.TAG_PROGRESS))
                    throw new IllegalArgumentException(
                            "hasExtra, 'progress' must be required.");
                int progress = intent.getIntExtra(K.TAG_PROGRESS, 0);
                long currentSize = intent.getLongExtra(K.TAG_CURRENTSIZE, 0);
                boolean isLast = intent.getBooleanExtra(K.TAG_LASTUPDATE, false);
                int state = isLast ? DownloadState.PAUSE : DownloadState.DOWNLOADING;
                o.setTaskState(state);
                downloadProgress(o, progress, currentSize);
            }
            break;
            case Actions.ACTION_DOWNLOAD_ERROR: {
                o.setTaskState(DownloadState.DOWNLOAD_ERROR);
                downloadFailed(o);
            }
            break;
//            case Actions.ACTION_CONNECT_ERROR: {
//                o.setTaskState(DownloadState.CONNECT_ERROR);
//                connectError(o);
//            }
//            break;
            case Actions.ACTION_CONNECTING: {
                o.setTaskState(DownloadState.CONNECTING);
                connecting(o);
            }
            break;
//            case Actions.ACTION_PARSERING: // 解析drm文件
//            {
//                o.setTaskState(DownloadState.PARSERING);
//                int result = DownDataDBManager.Builder().deleteByFolderId(
//                        o.getMyProId());
//                SZLog.i("result: " + result);
//                parsering(o);
//            }
//            break;
            case Actions.ACTION_FINISHED: {
                o.setTaskState(DownloadState.FINISHED);
                downloadFinished(o);
            }
            break;
            default:
                break;
        }
    }

//    /**
//     * 进度更新
//     *
//     * @param data           {@link SZFolderInfo}
//     * @param progress       进度
//     * @param currentSize    当前下载大小
//     * @param isLastProgress 是否是最后一次发送进度
//     */
//    protected abstract void downloadProgress(SZFolderInfo data, int progress,
//                                           long currentSize, boolean isLastProgress);
//
//    /**
//     * 下载失败
//     *
//     * @param data {@link SZFolderInfo}
//     */
//    protected abstract void downloadError(SZFolderInfo data);
//
//    /*
//     * 连接失败
//     *
//     * @param data {@link SZFolderInfo}
//     */
//    //protected abstract void connectError(SZFolderInfo data);
//
//    /**
//     * 正在连接中
//     *
//     * @param data {@link SZFolderInfo}
//     */
//    protected abstract void connecting(SZFolderInfo data);
//
//    /**
//     * 解析完成,操作通知ui更新
//     *
//     * @param data {@link SZFolderInfo}
//     */
//    protected abstract void downloadFinished(SZFolderInfo data);
//
//    /*
//     * 正在解析.....
//     *
//     * @param data {@link SZFolderInfo}
//     */
//    //protected abstract void parsering(SZFolderInfo data);
//

}
