package cn.com.pyc.drm.ui;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.bean.UpdateVersionBean;
import cn.com.pyc.drm.bean.event.SettingActivityCloseEvent;
import cn.com.pyc.drm.bean.event.UpdateBarEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.model.MeetingNameModel;
import cn.com.pyc.drm.model.MeetingNameModel.MeetingName;
import cn.com.pyc.drm.service.AppUpdateService;
import cn.com.pyc.drm.service.MediaService;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMDBHelper;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.FileUtils;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.PathFileUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.ViewUtil.DialogCallBack;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.ExecutorManager;
import cn.com.pyc.drm.utils.manager.RequestHttpManager;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.utils.manager.ScanHistoryDBManager;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager.OnCheckResultListener;
import cn.com.pyc.drm.widget.ProgressWheel;
import cn.com.pyc.drm.widget.ToastShow;

import com.alibaba.fastjson.JSON;
import com.google.zxing.client.android.CaptureActivity;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-8-24 下午4:50:19
 * @version V1.0
 */
public class SettingActivity extends BaseActivity implements OnClickListener {

	private static final int MSG_GETCACHE = 0x33;
	private static final int MSG_CLEARCACHE = 0x35;

	private View mCheckUpdate;
	private View mClearLayout;
	private TextView tv_cache;
	private TextView tv_version;
	private ProgressWheel mProgressWheel;
	private TextView tv_account;
	private ImageView imgDot;

	// private String AnalyticalFilePath;
	// private String downloadFilePath;

	private RelativeLayout sweep_RL;
	private RelativeLayout history_RL;
	private String local_cache_path;

	private AsyncTask<String, String, String> mLoginTask;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		UIHelper.showTintStatusBar(this);
		ActicityManager.getInstance().add(this);
		init_View();
		init_Value();
		load_Data();
		setlistener();
		loadVersion(false);
	}

	@Override
	protected void init_View() {
		((TextView) findViewById(R.id.title_tv)).setText(getString(R.string.user_me));
		tv_version = (TextView) findViewById(R.id.tv_version);// 版本号控制
		tv_cache = (TextView) findViewById(R.id.tv_cache);
		tv_account = (TextView) findViewById(R.id.tv_account);
		mProgressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
		imgDot = (ImageView) findViewById(R.id.iv_dot);
		imgDot.setVisibility(View.GONE);

		tv_account.setText(Constant.getKey_mobile()); // 显示当前账号
		mClearLayout = findViewById(R.id.clear_storager);
		mCheckUpdate = findViewById(R.id.check_update);
		sweep_RL = (RelativeLayout) findViewById(R.id.Sweep_RL);
		history_RL = (RelativeLayout) findViewById(R.id.history_RL);
	}

	@Override
	protected void init_Value() {
		EventBus.getDefault().register(this);
		if (DRMUtil.DEFAULT_SAVE_FILE_PATH == null || DRMUtil.DEFAULT_SAVE_FILE_DOWNLOAD_PATH == null || DRMUtil.DEFAULT_SAVE_FATHER_FILE_PATH == null) {
			PathFileUtil.createFilePath();
		}
		local_cache_path = DRMUtil.DEFAULT_SAVE_FATHER_FILE_PATH;

		String currentVersionName = CommonUtil.getAppVersionName(this);

		tv_version.setText(getString(R.string.cur_version_name, currentVersionName));

	}

	@Override
	protected void load_Data() {
		if (CommonUtil.isServiceRunning(this, AppUpdateService.class.getName())) {
			DRMLog.i("AppUpdateService running...");
			mProgressWheel.setVisibility(View.VISIBLE);
			mCheckUpdate.setEnabled(false);
		}
		getFileCacheSize();
	}

	/**
	 * @Description: (事件注入)
	 * @author 李巷阳
	 * @date 2016-6-17 下午12:14:15
	 */
	private void setlistener() {
		findViewById(R.id.back_img).setOnClickListener(this);
		mClearLayout.setOnClickListener(this);
		mClearLayout.setEnabled(true);
		findViewById(R.id.btn_loginout).setOnClickListener(this);
		mCheckUpdate.setOnClickListener(this);
		findViewById(R.id.about_us_page).setOnClickListener(this);
		sweep_RL.setOnClickListener(this);
		history_RL.setOnClickListener(this);
	}

	/**
	 * 获取缓存大小
	 */
	private void getFileCacheSize() {
		Thread mGetCacheThread = new Thread(new Runnable() {

			@Override
			public void run() {
				String fileSize = "0KB";
				File local_cachefilePath = new File(local_cache_path);
				long local_cachefilePathsize = FileUtils.getDirSize(local_cachefilePath);
				long dirsize = local_cachefilePathsize;
				if (dirsize > 0) {
					fileSize = FileUtils.convertStorage(dirsize);
				}
				Message message = Message.obtain();
				message.what = MSG_GETCACHE;
				message.obj = fileSize;
				mHandler.sendMessageDelayed(message, 200);
			}
		});
		mGetCacheThread.setPriority(Thread.NORM_PRIORITY - 1);
		ExecutorManager.getInstance().execute(mGetCacheThread);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_img:
			finishUI();
			break;
		case R.id.clear_storager:
			clearCache();
			break;
		case R.id.check_update: {
			if (CommonUtil.isFastDoubleClick(1000)) {
				showToast(getString(R.string.click_too_fast));
				return;
			}
			if (CommonUtil.isNetConnect(this))
				if (!XMLParserVersionManager.getInstance().isCheckUpdateTaskRunning()) {
					showToast(SettingActivity.this, getString(R.string.version_checking));
					loadVersion(true);
				}
		}
			break;
		case R.id.about_us_page: {
			openActivity(SZInfoActivity.class);
			UIHelper.startInAnim(this);
		}
			break;
		case R.id.btn_loginout:
			loginOut();
			break;
		case R.id.Sweep_RL:
			// 关闭音乐
			closeAudioSing();
			// 扫码登录
			SPUtils.remove(DRMUtil.KEY_LOGIN_TOKEN);
			OpenPageUtil.openZXingCode(SettingActivity.this);
			UIHelper.startInAnim(this);
			break;
		case R.id.history_RL:
			Intent intent = new Intent(SettingActivity.this, ScanHistoryActivity.class);
			startActivityForResult(intent, 0);
			UIHelper.startInAnim(this);
			break;

		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Constant.REQUEST_CODE_SCAN: {
			// 扫码登录获取产品信息
			if (resultCode == CaptureActivity.RESULT_OK && data != null) {
				Bundle bundle = data.getExtras();
				String DataSource = bundle.getString(Constant.DECODED_CONTENT_KEY);
				saveScanResult(DataSource);
			}
		}
			break;
		case Constant.SCANHISTORY_CODE: {
			// 扫码登录获取产品信息
			if (resultCode == Activity.RESULT_OK) {
				SettingActivity.this.finish();
			}
		}
			break;
		}
	}

	/**
	 * 保存扫描到的用户名称
	 * 
	 * @param result
	 */
	private void saveScanResult(final String DataSource) {
		if (TextUtils.isEmpty(DataSource)) {
			showToast(getString(R.string.scaning_empty));
			return;
		}
		if (TextUtils.isDigitsOnly(DataSource)) {
			showMsgDialog(DataSource);
			return;
		}
		if (!CommonUtil.isNetConnect(SettingActivity.this)) {
			showToast(getString(R.string.net_disconnected));
			return;
		}
		final String userName = StringUtil.getStringByResult(DataSource, "username=");
		final String id = StringUtil.getStringByResult(DataSource, "id=");
		final String systemType = StringUtil.getStringByResult(DataSource, "SystemType=");

		if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(id)) {
			showMsgDialog(DataSource);
			return;
		}
		cancelTask();
		mLoginTask = new AsyncTask<String, String, String>() {
			protected void onPreExecute() {
				showBgLoading("载入中...");
			};

			@Override
			protected String doInBackground(String... params) {
				if (isCancelled())
					return null;
				// 保存用户名
				SPUtils.save(DRMUtil.KEY_VISIT_NAME, userName);
				SPUtils.save(DRMUtil.KEY_MEETINGID, id);// 存储会议id
				// 根据返回的url获取主机和端口保存
				String[] hostAndPortString = StringUtil.getHostAndPortByResult(DataSource);
				if (hostAndPortString != null) {
					// 保存主机名。eg： video.suizhi.net
					SPUtils.save(DRMUtil.SCAN_FOR_HOST, hostAndPortString[0]);
					// 保存端口号。eg：8657
					SPUtils.save(DRMUtil.SCAN_FOR_PORT, hostAndPortString[1]);
				}
				return null;
			}

			protected void onPostExecute(String result) {
				getMeetingName(userName, id, systemType, DataSource);
			};
		};
		mLoginTask.execute();
	}

	private void getMeetingName(final String username, final String MeetingId, final String MeetingType, final String DataSource) {
		
		RequestHttpManager.init().postData(DRMUtil.getMeetingNameUrl(), username,MeetingId, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				MeetingNameModel model = JSON.parseObject(arg0.result, MeetingNameModel.class);
				if (model.isSuccess()) {
					MeetingName o = model.getData();
					if (o == null) {
						hideBgLoading();
						UIHelper.showToast(getApplicationContext(), getResources().getString(R.string.scan_qr_code_error));
						return;
					}
					if ("true".equals(o.getVerify()) || "false".equals(o.getVerify())) {
						hideBgLoading();
						showToast("对不起,您的二维码已经过期，请扫描最新二维码。");
						return;
					}
					// 暂停下载，暂停音乐等
					closeDownload();
					// 初始化数据库
					reInitData();

					ScanHistory sh = new ScanHistory();
					sh.setMeetingId(MeetingId);
					sh.setScanDataSource(DataSource);
					sh.setMeetingType(MeetingType);
					sh.setUsername(username);
					sh.setIsverifys(o.getVerify());
					sh.setVerify_url(o.getVerifyurl() + "&DevicesId=" + DeviceUtils.getIMEI(SettingActivity.this));
					sh.setVote_title(o.getTitle());
					sh.setMeetingName(o.getName());
					sh.setVote_url(o.getUrl());
					sh.setClient_url(o.getClient_url());
					sh.setCreateTime(o.getCreateTime());
					ScanHistoryDBManager.Builder(SettingActivity.this).saveScanHistory(sh);
					// verify判断是否需要第三方验证。
					if (!"0".equals(o.getVerify())) {
						Intent intent = new Intent(SettingActivity.this, ScanLoginVerificationActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("MeetingDetails", sh);
						intent.putExtras(bundle);
						startActivityForResult(intent, Constant.SCANHISTORY_CODE);
						hideBgLoading();
					} else {
						SPUtils.save(DRMUtil.VOTE_URL, o.getUrl());
						// 不需要第三方验证就把本地的手机号设置为"";
						SPUtils.save(DRMUtil.KEY_PHONE_NUMBER, "");
						OpenPageUtil.openDownloadMainByScaning2(SettingActivity.this, sh);
						hideBgLoading();
						finish();
					}
				} else {
					showToast("服务器异常");
					hideBgLoading();
				}

				cancelTask();
			}

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				hideBgLoading();
				showToast("连接服务器失败");
			}
		});
	}

	private void loadVersion(final boolean isShowDialog) {
		if (CommonUtil.isNetConnect(this)) {
			XMLParserVersionManager.getInstance().checkUpdate(this, new OnCheckResultListener() {
				@Override
				public void onResult(boolean hasNewVersion, Object result) {
					UpdateVersionBean o = (UpdateVersionBean) result;
					if (hasNewVersion) {
						imgDot.setVisibility(View.VISIBLE);
						ViewUtil.showUpdateDialog(SettingActivity.this, o, isShowDialog);
					} else {
						if (isShowDialog)
							showToast(SettingActivity.this, getString(R.string.cur_is_newclient));
					}
				}
			});
		}
	}

	/**
	 * 清除缓存空间
	 */
	private void clearCache() {
		// 判断有无正在下载的
		if (DownloadMainActivity.sTaskIdSet != null && DownloadMainActivity.sTaskIdSet.size() > 0) {
			ToastShow.getInstances_().showBusy(getApplicationContext(), getString(R.string.clear_must_pause));
			return;
		}
		AlertDialog.Builder builder = new Builder(this);
		View view = View.inflate(getApplicationContext(), R.layout.dialog_user, null);
		LinearLayout confirm = (LinearLayout) view.findViewById(R.id.confirm);// 确定
		LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);// 取消
		TextView tView = (TextView) view.findViewById(R.id.Cancellation);
		TextView tvSubView = (TextView) view.findViewById(R.id.subCancellation);
		tvSubView.setVisibility(View.VISIBLE);
		tView.setText(getString(R.string.ask_clear_local_content));
		tvSubView.setText(getString(R.string.clear_can_reloaded));
		builder.setView(view);
		final AlertDialog clearDlg = builder.create();
		clearDlg.show();
		confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (clearDlg != null && clearDlg.isShowing()) {
					clearDlg.dismiss();
				}
				clearing();
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (clearDlg != null)
					clearDlg.dismiss();
			}
		});
	}

	protected void clearing() {
		showBgLoading("正在清除");
		Thread mClear = new Thread(new Runnable() {
			@Override
			public void run() {
				close_underway_music();
				deleteCache();
				Message message = Message.obtain();
				message.what = MSG_CLEARCACHE;
				mHandler.sendMessageDelayed(message, 500);
			}
		});
		mClear.setPriority(Thread.NORM_PRIORITY - 1);
		ExecutorManager.getInstance().execute(mClear);
	}

	private void close_underway_music() {
		// 判断是否正在播放音乐
		if (MediaUtils.playState != ConstantValue.OPTION_STOP) {
			sendBroadcast(new Intent(DRMUtil.BROADCAST_MUSIC_Close_Music));
			try {
				Thread.currentThread().sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除存储的缓存
	 */
	private void deleteCache() {
		ArrayList<String> al_Subunit_file = FileUtils.getSubunitFileName(local_cache_path);
		for (String Subunit_file : al_Subunit_file) {
			if (Subunit_file.contains("_") && Subunit_file.length()>32) {
				String[] array = Subunit_file.split("_");
				DRMDBHelper.destoryDBHelper();
				SPUtils.save(DRMUtil.KEY_VISIT_NAME, array[0]);
				SPUtils.save(DRMUtil.KEY_MEETINGID, array[1]);
				DRMDBHelper helper = new DRMDBHelper(this);
				helper.createDBTable();
				DRMDBHelper.deleteTableData();
			}
		}
		FileUtils.delAllFile(local_cache_path + File.separator);
		ImageLoader.getInstance().clearMemoryCache();
		// // 位置索引数据库表删除
		SaveIndexDBManager.Builder(this).deleteAll();
	}

	/**
	 * 注销
	 */
	private void loginOut() {
		AlertDialog.Builder builder = new Builder(this);
		View view = View.inflate(getApplicationContext(), R.layout.dialog_user, null);
		LinearLayout confirm = (LinearLayout) view.findViewById(R.id.confirm);// 确定
		LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);// 取消
		builder.setView(view);
		final AlertDialog dialog = builder.create();
		dialog.show();
		confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loginOuting();
				// 注销关闭我的会议界面。
				clear_mymeeting_activity();
				String username = (String) SPUtils.get("username", "");
				Bundle bundle = new Bundle();
				bundle.putString("phone_number", username);
				openActivity(LoginActivity.class, bundle);
				finish();
				if (dialog != null) {
					dialog.dismiss();
				}
			}

			private void clear_mymeeting_activity() {
				Intent intent = new Intent(DRMUtil.BROADCAST_CLEAR_MYMEETING_ACTIVITY);
				SettingActivity.this.sendBroadcast(intent);
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialog != null)
					dialog.dismiss();
			}
		});
	}

	private void loginOuting() {
		closeAudioSing();
		closeDownload();
		reInitData();
	}

	private void closeDownload() {
		// 发送广播通知主界面，关闭下载，清除数据，关闭主界面。
		sendBroadcast(new Intent(DRMUtil.BROADCAST_CLOSE_ACTIVITY));
	}

	/**
	 * 关闭音乐相关
	 */
	private void closeAudioSing() {
		if (MusicHomeActivity.sMusicActivity != null || MediaUtils.playState != ConstantValue.OPTION_STOP) {
			Intent Closeintent = new Intent(this, MediaService.class);
			Closeintent.putExtra("option", ConstantValue.OPTION_STOP);
			startService(Closeintent);
		}
		MediaUtils.MUSIC_CURRENTPOS = -1;
	}

	/**
	 * 注销后，重新需要初始化的变量
	 */
	private void reInitData() {
		// 位置索引数据库表删除
		SaveIndexDBManager.Builder(this).dropTable();
		// 清除pdf保存的页数
		new MuPDFActivity.PdfPageSharedPreference(this).clearPdfPageData();

		DRMDBHelper.destoryDBHelper();
		Constant.setUserName(null);
		Constant.setPassWord(null);
		SPUtils.remove(DRMUtil.KEY_PWD);
		SPUtils.remove(DRMUtil.KEY_LOGIN_TOKEN);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		XMLParserVersionManager.getInstance().shutDownCheckTask();
		EventBus.getDefault().unregister(this);
		mHandler.removeMessages(MSG_GETCACHE);
		mHandler.removeMessages(MSG_CLEARCACHE);
		mHandler = null;
	}

	/**
	 * 捕捉后退键
	 */
	@Override
	public void onBackPressed() {
		finishUI();
	}

	private void finishUI() {
		UIHelper.finishActivity(this);
		ActicityManager.getInstance().remove(this);
	}

	/**
	 * eventbus 事件监听
	 * 
	 * @param event
	 */
	public void onEventMainThread(UpdateBarEvent event) {
		boolean ishow = event.isShowBar();
		if (mProgressWheel != null) {
			mCheckUpdate.setEnabled(!ishow);
			mProgressWheel.setVisibility(ishow ? View.VISIBLE : View.GONE);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GETCACHE:
				tv_cache.setText((String) msg.obj);
				break;
			case MSG_CLEARCACHE: {
				// 清除缓存
				hideBgLoading();
				// 清除缓存后，重发送广播，通知main页面重新加载内容
				sendBroadcast(new Intent(DRMUtil.BROADCAST_CLEAR_DOWNLOADED));
				getFileCacheSize();
				mClearLayout.setEnabled(false);
				// 清除pdf保存的页数
				new MuPDFActivity.PdfPageSharedPreference(SettingActivity.this).clearPdfPageData();
			}
				break;
			}
		};
	};

	/**
	 * 非自定义登录的url，显示结果对话框
	 * 
	 * @param result
	 */
	private void showMsgDialog(final String result) {
		ViewUtil.showCommonDialog(this, getString(R.string.scanning_result), result, getString(R.string.copy), new DialogCallBack() {

			@Override
			public void onConfirm() {
				CommonUtil.copyText(SettingActivity.this, result);
				showToast(getString(R.string.copy_to_clip));
			}
		});
	}

	// 取消任务
	private void cancelTask() {
		if (mLoginTask != null && (!mLoginTask.isCancelled() || mLoginTask.getStatus() == AsyncTask.Status.RUNNING)) {
			mLoginTask.cancel(true);
			mLoginTask = null;
		}
	}

	/**
	 * 接收关闭activity事件
	 * 
	 * @param event
	 */
	public void onEventMainThread(SettingActivityCloseEvent event) {
		loginOuting();
		SettingActivity.this.finish();
	}

}
