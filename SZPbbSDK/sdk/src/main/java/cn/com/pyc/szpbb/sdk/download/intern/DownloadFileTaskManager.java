package cn.com.pyc.szpbb.sdk.download.intern;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.com.pyc.szpbb.common.Actions;
import cn.com.pyc.szpbb.common.Fields;
import cn.com.pyc.szpbb.common.K;
import cn.com.pyc.szpbb.sdk.SZInitInterface;
import cn.com.pyc.szpbb.sdk.bean.DownData2;
import cn.com.pyc.szpbb.sdk.db.DownData2DBManager;
import cn.com.pyc.szpbb.sdk.manager.FTPManager;
import cn.com.pyc.szpbb.sdk.manager.ParserManager;
import cn.com.pyc.szpbb.sdk.models.SZFileData;
import cn.com.pyc.szpbb.util.FileUtil;
import cn.com.pyc.szpbb.util.FormatUtil;
import cn.com.pyc.szpbb.util.PathUtil;
import cn.com.pyc.szpbb.util.SZLog;

/**
 * 下载文件任务管理类
 *
 * @author hudq
 */
public class DownloadFileTaskManager {
    /**
     * 标示线程是否暂停
     */
    public volatile boolean isPause = false;
    public volatile boolean isBreak = false;
    private Context mContext;
    private SZFileData mFileData;
    private FTPManager mFtpManager = new FTPManager();
    private static ExecutorService mFixedExecutor;
    private static final int POOL_SIZE = 1;

    public DownloadFileTaskManager(Context mContext, SZFileData data) {
        this.mContext = mContext;
        this.mFileData = data;
        if (mFixedExecutor == null) {
            mFixedExecutor = Executors.newFixedThreadPool(POOL_SIZE);
        }
    }

    /**
     * 关闭
     */
    public static void shutdownTask() {
        if (mFixedExecutor != null && !mFixedExecutor.isShutdown()) {
            try {
                mFixedExecutor.shutdown();
                mFixedExecutor.awaitTermination(3 * 1000, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mFixedExecutor.shutdownNow();
                mFixedExecutor = null;
            }
            SZLog.i("shutdownNow download threads pool");
        }
    }

    /**
     * 下载信息,开始下载
     */
    public synchronized void download() {
        DownloadFileThread thread = new DownloadFileThread(this.mContext,
                this.mFileData);
        mFixedExecutor.execute(thread);
    }

    /**
     * 下载线程类2
     */
    private class DownloadFileThread extends Thread {
        private static final String TAG = "dt2";
        private final int offset = "ftp://".length();
        private Context context;
        private SZFileData o;
        private boolean isThreadDownloading = false; // 标示线程是否在下载中

        private DownloadFileThread(Context context, SZFileData data) {
            this.context = context;
            this.o = data;
        }

        @Override
        public void run() {
            super.run();
            if (isInterrupted() || isBreak) {
                isThreadDownloading = false;
                return;
            }
            try {
                // 一、正在连接，获取下载路径
                this.context.sendBroadcast(new Intent(Actions.ACTION_CONNECTING)
                        .putExtra(K.TAG_FILEDATA, o));

                // 获取下载路径
                String ftpPath = o.getFtpUrl();
                SZLog.d(TAG, "ftp: " + ftpPath);
                boolean illegalsFtpPath = (ftpPath == null
                        || !ftpPath.startsWith("ftp://")
                        || offset > ftpPath.length());
                if (illegalsFtpPath) // ftp路径不合法
                {
                    downloadError();
                    return;
                }
                String[] ftpOffset = ftpPath.substring(offset).split(":");
                String host = ftpOffset[0];
                int port = Integer.valueOf(ftpOffset[1].split("\\/")[0]);
                // 二、连接ftp服务器
                SZLog.i("2.connect ftp server");
                FTPClient ftpClient = new FTPClient();
                boolean hasConnectServer = (mFtpManager.connect(ftpClient, host, port));
                if (isBreak) {
                    SZLog.v("isBreak!");
                    mFtpManager.disconnect(ftpClient);
                    downloadError();
                    return;
                }
                if (!hasConnectServer) {
                    SZLog.v("connect server failed!");
                    downloadError();
                    return;
                }
                String filePath_ = FileUtil.getNameFromFilePath(ftpPath);
                FTPFile[] files = ftpClient.listFiles(filePath_);
                long fileSize = 0L;
                if (files != null && files.length > 0) {
                    FTPFile file = files[0];
                    fileSize = file.getSize(); // 文件大小
                }
                if (fileSize == 0L) {
                    SZLog.v("connect server failed,fileSize = 0");
                    downloadError();
                    return;
                }
                SZInitInterface.checkFilePath();
                StringBuilder sb = new StringBuilder();
                final String localPath = sb
                        .append(PathUtil.DEF_DOWNLOAD_PATH)
                        .append(File.separator)
                        .append(o.getFiles_id())
                        .append(Fields._DRM).toString();
                // 1.判断数据库中有没有保存下载信息
                // 2.有，继续上次下载；没有创建线程进行下载
                // 3.如果下载成功了，不继续下载
                DownData2 data2 = DownData2DBManager.Builder().findByFileId(o.getFiles_id());
                if (data2 == null) {
                    Log.v(TAG, "first download.");
                    data2 = new DownData2();
                    data2.setFileId(o.getFiles_id());
                    data2.setFileName(o.getName());
                    data2.setFileSize(fileSize);
                    data2.setFtpPath(ftpPath);
                    data2.setLocalPath(localPath);
                    data2.setFolderId(o.getSharefolder_id());
                }
                // 三、开始下载。
                startDownload(ftpClient, data2, filePath_);
            } catch (Exception e) {
                e.printStackTrace();
                downloadError();
            }
        }

        /**
         * 开始下载了
         *
         * @param ftpClient
         * @param data2
         * @param remoteFileName
         */
        private void startDownload(FTPClient ftpClient, DownData2 data2,
                                   String remoteFileName) {
            SZLog.i("3.connect success,start download task");
            isThreadDownloading = true;
            String localPath = data2.getLocalPath();
            long fileSize = data2.getFileSize();

            RandomAccessFile rAcFile = null;
            InputStream stream = null;
            try {
                // 创建本地目录文件
                File tmpFile = FileUtil.createFile(localPath);
                rAcFile = new RandomAccessFile(tmpFile, "rw");
                long offsetSize = data2.getCompleteSize(); // 文件完成大小，即是下载的 起始位置
                rAcFile.seek(offsetSize);
                long mCurrentSize = offsetSize;
                ftpClient.setRestartOffset(offsetSize);
                stream = ftpClient.retrieveFileStream(remoteFileName);
                SZLog.d(TAG,
                        "offsetSize = " + FormatUtil.formatSize(offsetSize));

                SZLog.i("4.start write files");
                long time = 0;
                byte[] buffer = new byte[2048];
                int length = -1, mTempPercent = 0;
                try {
                    while (isThreadDownloading
                            && (length = stream.read(buffer)) != -1) {
                        rAcFile.write(buffer, 0, length);
                        mCurrentSize += length;
                        int mPercentage = (int) (mCurrentSize * 100 / fileSize);
                        if (isPause) // 暂停,保存数据库
                        {
                            SZLog.v("pause!");
                            isThreadDownloading = false;
                            data2.setCompleteSize(mCurrentSize);
                            data2.setProgress(mPercentage);
                            DownData2DBManager.Builder().saveOrUpdate(data2);
                            sendProgress(mPercentage, mCurrentSize, true); // 发送最后一次的进度更新,防止界面显示进度误差
                            return;
                        }
                        // 通知ui更新进度
                        if (System.currentTimeMillis() - time > 600) {
                            SZLog.d(this.getName() + "progress = "
                                    + mPercentage + "%;downloaded："
                                    + FormatUtil.formatSize(mCurrentSize));
                            if (mPercentage > mTempPercent) {
                                sendProgress(mPercentage, mCurrentSize, false);
                            }
                            time = System.currentTimeMillis();
                            mTempPercent = mPercentage;
                        }
                    }

                    // 正在下载，判断是否下载完成,完成通知更新ui
                    if (isThreadDownloading && mCurrentSize >= fileSize) {
                        SZLog.i("5.download complete，mCurrentSize = "
                                + mCurrentSize + ";fileSize = " + fileSize);
                        //开始解析
                        ParserManager.getInstance().parserFile(this.context, this.o);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // 下载过程中，读取流异常，保存进度
                    data2.setCompleteSize(mCurrentSize);
                    data2.setProgress(mTempPercent);
                    DownData2DBManager.Builder().saveOrUpdate(data2);
                    downloadError();
                }
            } catch (Exception e) {
                e.printStackTrace();
                downloadError();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (rAcFile != null) {
                    try {
                        rAcFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ftpClient != null && ftpClient.isConnected()) {
                    try {
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 下载异常
         */
        private void downloadError() {
            Intent intent = new Intent(Actions.ACTION_DOWNLOAD_ERROR);
            intent.putExtra(K.TAG_FILEDATA, this.o);
            this.context.sendBroadcast(intent);
        }

        /**
         * 发送进度
         *
         * @param mPercentage
         * @param mCurrentSize
         * @param isLastProgress 是否是最后一次保存的进度
         */
        private void sendProgress(int mPercentage, long mCurrentSize,
                                  boolean isLastProgress) {
            Intent intent = new Intent(Actions.ACTION_PROGRESS);
            intent.putExtra(K.TAG_FILEDATA, this.o);
            intent.putExtra(K.TAG_PROGRESS, mPercentage);
            intent.putExtra(K.TAG_CURRENTSIZE, mCurrentSize);
            intent.putExtra(K.TAG_LASTUPDATE, isLastProgress);
            this.context.sendBroadcast(intent);
        }
    }

}
