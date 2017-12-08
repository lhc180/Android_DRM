package cn.com.pyc.drm.utils.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.AppException;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.DownloadInfo;
import cn.com.pyc.drm.model.db.bean.Downdata;
import cn.com.pyc.drm.model.db.practice.AlbumDAOImpl;
import cn.com.pyc.drm.model.db.practice.DowndataDAOImpl;
import cn.com.pyc.drm.service.DownloadService;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.FileUtils;
import cn.com.pyc.drm.utils.SPUtils;

/**
 * 下载任务管理类
 * 
 * @author hudq
 * 
 */
public class DownloadTaskManager {

	// 开始，默认
	public static final int INIT = 0;
	// 等待
	public static final int WAITING = 1;
	// 连接
	public static final int CONNECTING = 2;
	// 暂停
	public static final int PAUSE = 3;
	// 下载中,更新进度中
	public static final int DOWNLOADING = 4;
	// 完成，开始解析
	public static final int COMPLETE_PARSER = 5;
	// 下载异常，ftpPath关闭
	public static final int DOWNLOAD_ERROR = 6;
	// 连接异常，一般是服务端文件异常
	public static final int CONNECT_ERROR = 7;

	private Context context;

	/** 标示线程是否暂停 */
	public boolean isPause = false;

	private FTPManager ftpManager = new FTPManager();

	private static ExecutorService mFixedExecutor;
	// 线程池大小
	private static final int POOL_SIZE = 1;

	private DownloadInfo info;

	public DownloadTaskManager(Context context, DownloadInfo info) {
		this.context = context;
		this.info = info;
		if (mFixedExecutor == null) {
			mFixedExecutor = Executors.newFixedThreadPool(POOL_SIZE);
		}
	}

	/**
	 * 关闭
	 */
	public static void closeExecutorService() {
		if (mFixedExecutor != null && !mFixedExecutor.isShutdown()) {
			mFixedExecutor.shutdownNow();
			mFixedExecutor = null;
			DRMLog.i("shutdown downloadthreads pool");
		}
	}

	/**
	 * 下载信息,开始下载
	 * 
	 */
	public synchronized void downloadInfo() {

		DownloadThread thread = new DownloadThread(this.context, this.info);
		mFixedExecutor.execute(thread);
	}

	/**
	 * 下载线程类
	 * 
	 * @author qd
	 * 
	 */
	private class DownloadThread extends Thread {
		private String TAG = DownloadThread.class.getSimpleName();
		private Context context;

		// 标示线程是否在下载中
		private boolean isThreadDownloading = false;
		private DownloadInfo o;

		public DownloadThread(Context context, DownloadInfo o) {
			this.context = context;
			this.o = o;
		}

		@Override
		public void run() {
			super.run();
			final String myProId = o.getMyProId();
			final int position = o.getPosition();
			// DRMLog.d(TAG, "o.getMyProId() = " + myProId);
			// DRMLog.d(TAG, "o.getPosition() = " + position);
			String Album_Id = AlbumDAOImpl.getInstance().findAlbumId(myProId); // 获取专辑ID
			if (!TextUtils.isEmpty(Album_Id))
				delAllFile(myProId);
			if (!CommonUtil.isSdCardCanUsed())
				return;

			// 正在连接ftp服务器
			Intent in = new Intent(DownloadService.ACTION_CONNECTING);
			in.putExtra("position", position);
			this.context.sendBroadcast(in);

			// final boolean isScaning = Constant.LoginConfig.type ==
			// DrmPat.LOGIN_SCANING;
			final String httpPath = DRMUtil.getDownloadUrlByScan();
			// 通过http路径获取ftp下载路径
			Bundle bundle = new Bundle();
			bundle.putString("username", (String) SPUtils.get(DRMUtil.KEY_VISIT_NAME, ""));
			bundle.putString("id", myProId);// 专辑ID
			bundle.putString("device", "Android");// 设备
			bundle.putString("memberId", (String) SPUtils.get(DRMUtil.KEY_PHONE_NUMBER, "")); // 手机号
			bundle.putString("dId", (String) SPUtils.get(DRMUtil.KEY_MEETINGID, ""));// 会议ID
//			bundle.putString("dId", (String) SPUtils.get(DRMUtil.KEY_SUIZHI_CODE, ""));// 会议ID

			String ftpPath = RequestHttpManager.init().postDataSync(httpPath, bundle);
			if (!ftpPath.contains("ftp:")) {
				try {
					new Thread().sleep(1000 * 10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ftpPath = RequestHttpManager.init().postDataSync(httpPath, bundle);
			}

			// 开始下载
			DownloadFTPFile(myProId, position, true, ftpPath);
		}

		// 下载文件
		private void DownloadFTPFile(String myProId, int position, boolean isScaning, String ftpPath) {
			DRMLog.w(TAG, "ftpPath = " + ftpPath);
			// ftp路径不合法！！！！
			boolean illegalsFtpPath = (ftpPath == null || !ftpPath.startsWith("ftp://") || (AppException.TYPE_HTTP_ERROR + "").equals(ftpPath) || "-1".equals(ftpPath) || "shutdown".equals(ftpPath));
			if (illegalsFtpPath) {
				ftpPathError(position, myProId);
				return;
			}
			String filePath_ = FileUtils.getNameFromFilePath(ftpPath);
			DRMLog.d(TAG, "filePathName: " + filePath_);
			String host;
			int port;
			try {
				String[] result = formatHostPort(isScaning, ftpPath);
				host = result[0];
				if (!TextUtils.isDigitsOnly(result[1])) {
					Toast.makeText(context, "非法端口号(" + result[1] + ")", Toast.LENGTH_SHORT).show();
					return;
				}
				port = ConvertToUtils.toInt(result[1]);
			} catch (IndexOutOfBoundsException e) {
				Log.v(TAG, "" + e.getMessage());
				ftpPathError(position, myProId);
				return;
			}
			// 连接ftp服务器
			FTPClient ftpClient = new FTPClient();
			DRMLog.e(TAG, "2.connect ftp server");
			boolean hasConnectServer = (ftpManager.connect(ftpClient, host, port, this.context.getResources().getString(R.string.ftp_name), ""));
			if (!hasConnectServer) {
				connectError(position, myProId);
				return;
			}
			FTPFile[] files = null;
			long fileSize = 0;
			try {
				files = ftpClient.listFiles(filePath_);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (files != null && files.length > 0) {
				FTPFile file = files[0];
				fileSize = file.getSize(); // 文件大小
			}
			if (fileSize == 0) {
				connectError(position, myProId);
				return;
			}
			StringBuffer strbuffer = new StringBuffer();
			final String localPath = strbuffer.append(DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH).append(File.separator).append(myProId).append(DrmPat._DRM).toString();
			// 1.判断数据库中有没有保存下载信息
			// 2.有，继续上次下载；没有创建线程进行下载
			// 3.如果下载成功了，不继续下载
			Downdata mSaveData = DowndataDAOImpl.getInstance().findDowndataById(myProId);
			if (mSaveData == null) {
				DRMLog.i("first download");
				mSaveData = new Downdata();
				mSaveData.setId(String.valueOf(System.currentTimeMillis()));
				mSaveData.setFileOffsetstr(0 + "M"); // 本地已下载文件大小,格式化后的eg：22M
				mSaveData.setIsDownload("0");
				mSaveData.setCompleteSize(0); // 下载完成大小size
				mSaveData.setLocalpath(localPath);
				mSaveData.setMyProduct_id(myProId);
				mSaveData.setTotalSize(FileUtils.convertStorage(fileSize)); // 总大小，eg:23.2M
				mSaveData.setFtpPath(ftpPath);
			}

			startDownloader(ftpClient, mSaveData, fileSize, filePath_);
		}

		/**
		 * 获得host和端口号port
		 * 
		 * @param isScaning
		 * @param ftpPath
		 * @return result[0] = host; result[1] = port;
		 */
		private String[] formatHostPort(boolean isScaning, String ftpPath) {
			String host;
			String port;
			String[] result = new String[2];
			final int offset = "ftp://".length();
			Log.v(TAG, "offset = " + offset);
			if (offset > ftpPath.length()) {
				throw new IndexOutOfBoundsException("ftp path is illegals，please checking");
			}
			String subFtpPath = ftpPath.substring(offset);
			Log.v(TAG, "subFtpPath = " + subFtpPath);
			// 判断是否是扫码登录，改变host
			if (isScaning) {
				String[] mOffsetScan = subFtpPath.split(":");
				host = mOffsetScan[0];
				port = mOffsetScan[1].split("\\/")[0];
			} else {
				host = subFtpPath.split("\\/")[0];
				port = "21";
			}
			result[0] = host;
			result[1] = port;

			return result;
		}

		/**
		 * ftp路径非法
		 * 
		 * @param position
		 * @param myProId
		 */
		private void ftpPathError(int position, String myProId) {
			DRMLog.e(TAG, "ftpPath is illegals");
			Intent intent = new Intent(DownloadService.ACTION_ERROR);
			intent.putExtra("position", position);
			intent.putExtra("myProId", myProId);
			this.context.sendBroadcast(intent);
		}

		/**
		 * 连接server失败
		 * 
		 * @param position
		 * @param myProId
		 */
		private void connectError(int position, String myProId) {
			DRMLog.e(TAG, "connect ftp failed");
			Intent intent = new Intent(DownloadService.ACTION_CONNECT_ERROR);
			intent.putExtra("myProId", myProId);
			intent.putExtra("position", position);
			this.context.sendBroadcast(intent);
		}

		/**
		 * 开始下载了
		 * 
		 * @param ftpClient
		 * @param data
		 * @param fileSize
		 * @param filePath_
		 */
		private void startDownloader(FTPClient ftpClient, Downdata data, long fileSize, String filePath_) {
			DRMLog.e(TAG, "3.connect success,start download task");
			isThreadDownloading = true;
			String myProId = data.getMyProduct_id();
			String ftpPath = data.getFtpPath();
			String localPath = data.getLocalpath();

			// DRMLog.d(TAG, "ftpPath = " + ftpPath);
			RandomAccessFile rAcFile = null;
			InputStream stream = null;
			try {
				long localSize = 0;
				// 判断要下载的文件是否已经存在本地
				if (FileUtils.checkFilePathExists(localPath)) {
					localSize = FileUtils.getFileSize(localPath);
				}
				DRMLog.d(TAG, "localSize = " + FileUtils.convertStorage(localSize));
				// 创建本地目录文件
				File tmpFile = FileUtils.createFile(localPath);
				// 得到ftp文件path,eg:drgfd43tdg.drm
				// String filePath_ = FileUtils.getPathlastFilePath(ftpPath);
				// DRMLog.w("filePathName:" + filePath_);
				// 把文件大小转换成M形式,eg:20MB
				String formatTotalSize = FileUtils.convertStorage(fileSize);
				DRMLog.d(TAG, "formatTotalSize = " + formatTotalSize);

				// 得到写入的文件
				rAcFile = new RandomAccessFile(tmpFile, "rw");
				// 起始位置
				long offsetSize = data.getCompleteSize();
				// 已经完成进度位置
				DRMLog.d(TAG, "offsetSize = " + offsetSize);

				rAcFile.seek(offsetSize);
				long mCurrentSize = offsetSize;

				ftpClient.setRestartOffset(offsetSize);
				stream = ftpClient.retrieveFileStream(filePath_);

				byte[] buffer = new byte[2048];
				int length = -1;
				DRMLog.e(TAG, "4.start write files");
				long time = 0;
				int mTempPercent = 0;
				try {
					while (isThreadDownloading && (length = stream.read(buffer)) != -1) {
						// 写入文件
						rAcFile.write(buffer, 0, length);
						// 累加文件完成进度
						mCurrentSize += length;
						int mPercentage = (int) (mCurrentSize * 100 / fileSize);
						// 暂停,保存数据库
						if (isPause) {
							DRMLog.e(TAG, "暂停下载任务");
							isThreadDownloading = false;
							DowndataDAOImpl.getInstance().updateSaveDownData(myProId, mPercentage, formatTotalSize, mCurrentSize, localPath, ftpPath);
							// 发送最后一次的进度更新,防止界面显示进度误差
							sendProgress(mPercentage, mCurrentSize, fileSize, true);
							return;
						}
						// 通知ui更新进度
//						if (System.currentTimeMillis() - time > 700) {
							DRMLog.e(TAG, this.getName() + "progress = " + mPercentage + "%;downloaded：" + FileUtils.convertStorage(mCurrentSize));
							if (mPercentage > mTempPercent) {
								sendProgress(mPercentage, mCurrentSize, fileSize, false);
							}
							time = System.currentTimeMillis();
							mTempPercent = mPercentage;
//						}
					}

					// 正在下载，判断是否下载完成,完成通知更新ui
					if (isThreadDownloading && mCurrentSize >= fileSize) {
						DRMLog.e(TAG, "5.download complete，mCurrentSize = " + mCurrentSize + ";fileSize = " + fileSize);
						Intent intent = new Intent(DownloadService.ACTION_FINISH);
						intent.putExtra("myProId", myProId);
						intent.putExtra("position", this.o.getPosition());
						intent.putExtra("author", this.o.getAuthors());
						intent.putExtra("picture_ratio", this.o.getPicture_ratio());
						intent.putExtra("publish_date", this.o.getPublishDate());
						this.context.sendBroadcast(intent);
					}
				} catch (IOException e) {
					e.printStackTrace();
					// 下载过程中，读取流异常，保存进度
					DowndataDAOImpl.getInstance().updateSaveDownData(myProId, mTempPercent, formatTotalSize, mCurrentSize, localPath, ftpPath);
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
		 * @param fileSize
		 * @param isLastSaveProgress
		 *            是否是最后一次保存的进度
		 */
		private void sendProgress(int mPercentage, long mCurrentSize, long fileSize, boolean isLastSaveProgress) {
			Intent intent = new Intent(DownloadService.ACTION_UPDATE);
			intent.putExtra("position", this.o.getPosition());
			intent.putExtra("progress", mPercentage);
			intent.putExtra("currentSize", mCurrentSize);
			intent.putExtra("totalSize", fileSize);
			intent.putExtra("isLastSaveProgress", isLastSaveProgress);
			this.context.sendBroadcast(intent);
		}

		/**
		 * 要下载的专辑已经存在或部分存在，删除
		 * 
		 * @param myProid
		 */
		protected void delAllFile(final String myProid) {
			String decodePath = DRMUtil.DEFAULT_SAVE_FILE_PATH + File.separator + myProid;
			FileUtils.delFolder(decodePath);
			Thread clearThread = new Thread(new Runnable() {

				@Override
				public void run() {
					String Album_Id = AlbumDAOImpl.getInstance().findAlbumId(myProid);// 获取专辑ID
					if (Album_Id != null) {
						AlbumDAOImpl.getInstance().DeleteAlbum(Album_Id);
						String albumContentIdList = AlbumDAOImpl.getInstance().findAlbumContentId(Album_Id);
						if (albumContentIdList != null) {
							AlbumDAOImpl.getInstance().DeleteAlbumContent(Album_Id);
						}
						String RightContentIdList = AlbumDAOImpl.getInstance().findRightId(Album_Id);
						if (RightContentIdList != null) {
							AlbumDAOImpl.getInstance().DeleteRight(Album_Id);
						}
						ArrayList<String> assetIdList = AlbumDAOImpl.getInstance().findAssetId(Album_Id);
						if (assetIdList != null && assetIdList.size() > 0) {
							AlbumDAOImpl.getInstance().DeleteAsset(Album_Id);
							for (int i = 0; i < assetIdList.size(); i++) {
								String assetId = assetIdList.get(i);
								String PermissionId = AlbumDAOImpl.getInstance().findPermissionId(assetId);
								if (PermissionId != null) {
									AlbumDAOImpl.getInstance().DeletePermission(assetId);
									ArrayList<String> perconstraintIdList = AlbumDAOImpl.getInstance().findPerconstraintId(PermissionId);
									if (perconstraintIdList != null) {
										AlbumDAOImpl.getInstance().DeletePerconstraint(PermissionId);
									}
								}
							}
						}
					}
				}
			});
			ExecutorManager.getInstance().execute(clearThread);
		}
	}

}
