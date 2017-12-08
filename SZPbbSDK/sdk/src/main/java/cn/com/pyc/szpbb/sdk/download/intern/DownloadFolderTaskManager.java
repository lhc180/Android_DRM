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
import cn.com.pyc.szpbb.sdk.bean.DownData;
import cn.com.pyc.szpbb.sdk.db.DownDataDBManager;
import cn.com.pyc.szpbb.sdk.manager.FTPManager;
import cn.com.pyc.szpbb.sdk.manager.ParserManager;
import cn.com.pyc.szpbb.sdk.models.SZFolderInfo;
import cn.com.pyc.szpbb.util.FileUtil;
import cn.com.pyc.szpbb.util.FormatUtil;
import cn.com.pyc.szpbb.util.PathUtil;
import cn.com.pyc.szpbb.util.SZLog;

/**
 * 下载文件夹任务管理类
 *
 * @author hudq
 */
@Deprecated
public class DownloadFolderTaskManager {
    private Context mContext;
    private SZFolderInfo mData;

    /**
     * 标示线程是否暂停
     */
    public boolean isPause = false;
    public boolean isBreak = false;

    private FTPManager mFtpManager = new FTPManager();
    private static ExecutorService mFixedExecutor;
    private static final int POOL_SIZE = 1;

    public DownloadFolderTaskManager(Context context, SZFolderInfo data) {
        this.mContext = context;
        this.mData = data;
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
        }
    }

    /**
     * 下载信息,开始下载
     */
    public synchronized void download() {
        DownloadThread thread = new DownloadThread(this.mContext, this.mData);
        mFixedExecutor.execute(thread);
    }

    /**
     * 下载线程类
     */
    private class DownloadThread extends Thread {
        private static final String TAG = "dt";
        private final int offset = "ftp://".length();
        private Context context;
        private SZFolderInfo o;
        private boolean isThreadDownloading = false; // 标示线程是否在下载中

        private DownloadThread(Context context, SZFolderInfo o) {
            this.context = context;
            this.o = o;
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
                        .putExtra(K.TAG_FOLDERINFO, o));

                // 获取下载路径
                String myProId = o.getMyProId();
                String ftpPath = o.getFtpUrl();
                SZLog.d("ftpPath", "" + ftpPath);
                boolean illegalsFtpPath = (ftpPath == null
                        || !ftpPath.startsWith("ftp://")
                        || offset > ftpPath.length());
                if (illegalsFtpPath) // ftp路径不合法
                {
                    downloadError();
                    return;
                }
                String host = ftpPath.substring(offset).split("\\/")[0];
                final int port = 21;
                // 二、连接ftp服务器
                SZLog.i("2.connect ftp server");
                FTPClient ftpClient = new FTPClient();
                boolean hasConnectServer = (mFtpManager.connect(ftpClient, host, port));
                if (isBreak) {
                    //isPause = true;
                    SZLog.i("isBreak!");
                    mFtpManager.disconnect(ftpClient);
                    downloadError();
                    return;
                }
                if (!hasConnectServer) {
                    downloadError();
                    SZLog.i("connect server failed!");
                    return;
                }
                String filePath_ = FileUtil.getNameFromFilePath(ftpPath);
                FTPFile[] files = ftpClient.listFiles(filePath_);
                long fileSize = 0;
                if (files != null && files.length > 0) {
                    FTPFile file = files[0];
                    fileSize = file.getSize(); // 文件大小
                }
                if (fileSize == 0L) {
                    downloadError();
                    SZLog.i("connect server failed, fileSize = 0");
                    return;
                }
                SZInitInterface.checkFilePath();
                StringBuilder sb = new StringBuilder();
                final String localPath = sb.append(PathUtil.DEF_DOWNLOAD_PATH)
                        .append("/")
                        .append(myProId)
                        .append(Fields._DRM).toString();
                // 1.判断数据库中有没有保存下载信息
                // 2.有，继续上次下载；没有创建线程进行下载
                // 3.如果下载成功了，不继续下载
                DownData data = DownDataDBManager.Builder().findByFolderId(
                        myProId);
                if (data == null) {
                    Log.v(TAG, "first download.");
                    data = new DownData();
                    data.setFolderName(o.getProductName());
                    data.setTotalSize(fileSize);
                    data.setFtpPath(ftpPath);
                    data.setLocalPath(localPath);
                    data.setFolderId(myProId);
                }
                // 三、开始下载。
                startDownload(ftpClient, data, filePath_);
            } catch (Exception e) {
                e.printStackTrace();
                downloadError();
            }
        }

        /**
         * 开始下载了
         *
         * @param ftpClient
         * @param data
         * @param remoteFileName
         */
        private void startDownload(FTPClient ftpClient, DownData data,
                                   String remoteFileName) {
            SZLog.i("3.connect success,start download task");
            isThreadDownloading = true;
            String localPath = data.getLocalPath();
            long fileSize = data.getTotalSize();

            RandomAccessFile rAcFile = null;
            InputStream stream = null;
            try {
                File tmpFile = FileUtil.createFile(localPath);
                rAcFile = new RandomAccessFile(tmpFile, "rw");
                long offsetSize = data.getCompleteSize(); // 文件完成大小，即是下载的 起始位置
                rAcFile.seek(offsetSize);
                long mCurrentSize = offsetSize;
                ftpClient.setRestartOffset(offsetSize);
                stream = ftpClient.retrieveFileStream(remoteFileName);
                SZLog.d(TAG, "offsetSize = " + FormatUtil.formatSize(offsetSize));

                byte[] buffer = new byte[2048];
                int length = -1;
                SZLog.i("4.start write files");
                long time = 0;
                int mTempPercent = 0;
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
                            data.setCompleteSize(mCurrentSize);
                            data.setProgress(mPercentage);
                            DownDataDBManager.Builder().saveOrUpdate(data);
                            // 发送最后一次的进度更新,防止界面显示进度误差
                            sendProgress(mPercentage, mCurrentSize, true);
                            return;
                        }
                        // 通知ui更新进度
                        if (System.currentTimeMillis() - time > 700) {
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
                        // 开始解析
                        ParserManager.getInstance().parserFolder(this.context, this.o);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // 下载过程中，读取流异常，保存进度
                    data.setCompleteSize(mCurrentSize);
                    data.setProgress(mTempPercent);
                    DownDataDBManager.Builder().saveOrUpdate(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
         * 发送进度
         *
         * @param mPercentage
         * @param mCurrentSize
         * @param isLastProgress 是否是最后一次保存的进度
         */
        private void sendProgress(int mPercentage, long mCurrentSize,
                                  boolean isLastProgress) {
            Intent intent = new Intent(Actions.ACTION_PROGRESS);
            intent.putExtra(K.TAG_FOLDERINFO, this.o);
            intent.putExtra(K.TAG_PROGRESS, mPercentage);
            intent.putExtra(K.TAG_CURRENTSIZE, mCurrentSize);
            intent.putExtra(K.TAG_LASTUPDATE, isLastProgress);
            this.context.sendBroadcast(intent);
        }

        /**
         * ftp路径非法
         */
        private void downloadError() {
            SZLog.d("ftpPath is illegals");
            Intent intent = new Intent(Actions.ACTION_DOWNLOAD_ERROR);
            intent.putExtra(K.TAG_FOLDERINFO, this.o);
            this.context.sendBroadcast(intent);
        }

        /*
         * 连接server失败
         */
//        private void connectError() {
//            SZLog.d("connect ftp failed");
//            Intent intent = new Intent(Actions.ACTION_CONNECT_ERROR);
//            intent.putExtra(K.TAG_FOLDERINFO, this.o);
//            this.context.sendBroadcast(intent);
//        }
    }

}
