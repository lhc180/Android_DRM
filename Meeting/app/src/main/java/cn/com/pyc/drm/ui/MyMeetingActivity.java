package cn.com.pyc.drm.ui;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.MymeetingHistoryAdapter;
import cn.com.pyc.drm.bean.MechanismBean;
import cn.com.pyc.drm.bean.ReturnMeetingRecordDataBean;
import cn.com.pyc.drm.bean.ReturnMeetingRecordDataBean.databean;
import cn.com.pyc.drm.bean.UpdateVersionBean;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.dialog.TVAnimDialog;
import cn.com.pyc.drm.dialog.TVAnimDialog.OnTVAnimDialogClickListener;
import cn.com.pyc.drm.service.DownloadService;
import cn.com.pyc.drm.service.MediaService;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.TimeUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.NotificationPatManager;
import cn.com.pyc.drm.utils.manager.RxAndroid;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager.OnCheckResultListener;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.RecyclerViewClickListener;

import com.google.zxing.client.android.CaptureActivity;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.jcodecraeer.xrecyclerview.XRecyclerView.LoadingListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * @Description: (我的会议)
 * @author 李巷阳
 * @date 2016-8-16 上午11:35:27
 * @version V1.0
 */
public class MyMeetingActivity extends BaseActivity implements OnClickListener {

	@ViewInject(R.id.title_tv)
	private TextView mtv_title_tv;

	@ViewInject(R.id.tv_servername)
	private TextView mtv_meetingname;

	@ViewInject(R.id.lv_scanhistory)
	private XRecyclerView mlv_scanhistory;

	@ViewInject(R.id.tv_hintsinfo)
	private TextView mtv_hintsinfo;

	@ViewInject(R.id.view)
	private View mview;

	@ViewInject(R.id.rl_prompt)
	private RelativeLayout mrl_prompt;

	@ViewInject(R.id.iv_seting)
	private HighlightImageView mhiv_seting;

	@ViewInject(R.id.opt_img)
	private HighlightImageView mhiv_img;

	@ViewInject(R.id.iv_info_dot)
	private View mv_infodot;

	private MymeetingHistoryAdapter mAdapter;

	private List<databean> mDataList;

	private AsyncTask<String, String, String> mLoginTask;

	private String LoginPhone = "";

	private boolean isStop = false;

	
	
	/**
	 * @author 李巷阳
	 * @date 2016-8-16 上午11:36:30
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mymeeting_history);
		init_Value();
		init_View();
		initListener();
		showloading_load_data();
		checkVersion();
		registerBroadcast();
	}

	/**
	 * @author 李巷阳
	 * @date 2016-8-15 下午5:35:54
	 */
	@Override
	protected void init_Value() {
		ViewUtils.inject(this);
		ActicityManager.getInstance().add(this);
		UIHelper.showTintStatusBar(this); // 自定义状态栏
		LoginPhone = (String) SPUtils.get(DRMUtil.KEY_PHONE_NUMBER, "");
		mmechanismbean = (MechanismBean) getIntent().getSerializableExtra("MechanismData");
	}

	/**
	 * @author 李巷阳
	 * @date 2016-8-16 上午11:36:23
	 */
	@Override
	protected void init_View() {
		mtv_meetingname.setText(mmechanismbean.getServerName());
		mtv_title_tv.setText(getResources().getString(R.string.my_meeting));
		mhiv_img.setVisibility(View.VISIBLE);

		LinearLayoutManager LinearManager = new LinearLayoutManager(this);
		LinearManager.setOrientation(LinearLayoutManager.VERTICAL);
		mlv_scanhistory.setLayoutManager(LinearManager);

		mlv_scanhistory.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
		mlv_scanhistory.setLaodingMoreProgressStyle(ProgressStyle.BallRotate);
		mlv_scanhistory.setArrowImageView(R.drawable.iconfont_downgrey);

	}

	/**
	 * @author 李巷阳
	 * @date 2016-8-16 下午12:13:29
	 */
	private void initListener() {
		mrl_prompt.setOnClickListener(this);
		mhiv_seting.setOnClickListener(this);
		mhiv_img.setOnClickListener(this);
		mlv_scanhistory.setLoadingListener(new LoadingListener() {

			@Override
			public void onRefresh() {
				load_Data();
			}

			@Override
			public void onLoadMore() {
			}
		});

		mlv_scanhistory.addOnItemTouchListener(new RecyclerViewClickListener(this, mlv_scanhistory, new RecyclerViewClickListener.onItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				final databean mData = mDataList.get(position - 1);
				OpenPageUtil.openDownloadMainByMyMeeting(MyMeetingActivity.this, mData, mmechanismbean);
			}
		}));
	}

	/**
	 * 获取机构下得所有会议
	 * 
	 * @author 李巷阳
	 * @date 2016-8-16 上午11:36:23
	 */
	@Override
	protected void load_Data() {
		Observable<ReturnMeetingRecordDataBean> observableservice = RxAndroid.getAllMeeting(LoginPhone, mmechanismbean);
		Subscriber subscriber = new Subscriber<ReturnMeetingRecordDataBean>() {
			public void onError(Throwable arg0) {
				UIHelper.showToast(getApplicationContext(), getResources().getString(R.string.visit_error));
				Load_error();
				mlv_scanhistory.refreshComplete();
			}

			@Override
			public void onNext(ReturnMeetingRecordDataBean Rdata) {
				if (Constant.return_true.equals(Rdata.getResult())) {
					mDataList = Rdata.getData();
					setAdapter(mDataList);
				} else if (Constant.return_false.equals(Rdata.getResult())) {
					UIHelper.showToast(getApplicationContext(), Rdata.getMsg());
				}
				
			}

			@Override
			public void onCompleted() {
				Load_success();
				mlv_scanhistory.refreshComplete();
			}
		};
		observableservice.subscribe(subscriber);

	}

	private void setAdapter(List<databean> meetingRecordData) {
		if (mAdapter == null) {
			mAdapter = new MymeetingHistoryAdapter(getApplicationContext(), meetingRecordData);
			mlv_scanhistory.setAdapter(mAdapter);
		} else {
			mAdapter.setData(meetingRecordData);
		}
	}

	/**
	 * @author 李巷阳
	 * @date 2016-8-17 下午3:04:35
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_prompt:
			OpenPageUtil.openMyMeetingbyMechanism(MyMeetingActivity.this);
			break;
		case R.id.iv_seting:
			openActivity(SettingActivity.class);
			UIHelper.startInAnim(MyMeetingActivity.this);
			break;
		case R.id.opt_img:
			SPUtils.remove(DRMUtil.KEY_LOGIN_TOKEN);
			OpenPageUtil.openZXingCode(MyMeetingActivity.this);
			UIHelper.startInAnim(MyMeetingActivity.this);
			break;
		default:
			break;
		}
	}

	/**
	 * startActivityForResult回调
	 * 
	 * @author 李巷阳
	 * @date 2016-9-9 下午3:13:31
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constant.REQUEST_CODE_SCAN: {
			// 扫码获取机构信息
			if (resultCode == CaptureActivity.RESULT_OK && data != null) {
				Bundle bundle = data.getExtras();
				String DataSource = bundle.getString(Constant.DECODED_CONTENT_KEY);
				saveScanResult(DataSource);
			}
		}
			break;
		// 扫描二维码后关闭此界面
		case Constant.SCANHISTORY_CODE: {
			if (resultCode == Activity.RESULT_OK) {
				MyMeetingActivity.this.finish();
			}
		}
			break;
		// 从机构跳转，获取机构信息，刷新会议界面。
		case DRMUtil.RefreshMyMeetingActivity: {
			if (data != null) {
				mmechanismbean = (MechanismBean) data.getSerializableExtra("MechanismData");
				mtv_meetingname.setText(mmechanismbean.getServerName());
				showloading_load_data();
			}
		}
			break;
		}
	}

	private void showloading_load_data() {
		setInvisible();
		showBgLoading(getResources().getString(R.string.load_ing));
		load_Data();
	}

	/**
	 * 扫描二维码后触发
	 * 
	 * 1.通过机构码获取机构信息 2.添加机构信息到服务器 3.查询添加机构下面所有的会议
	 * 
	 * @param result
	 */
	private void saveScanResult(final String DataSource) {
		// 判断是否是机构二维码
		if (!isMechanismQRcode(DataSource))
			return;
		showBgLoading(getResources().getString(R.string.load_ing));
		Observable<ReturnMeetingRecordDataBean> addMechanismAndGetMetting = RxAndroid.addMechanismAndGetMetting(mtv_meetingname, LoginPhone, DataSource);
		Subscriber subscriber = new Subscriber<ReturnMeetingRecordDataBean>() {
			public void onCompleted() {
				Load_success();
			}

			public void onError(Throwable arg0) {
				UIHelper.showToast(getApplicationContext(), arg0.getMessage());
				Load_error();
			}

			@Override
			public void onNext(ReturnMeetingRecordDataBean Rdata) {
				if (Constant.return_true.equals(Rdata.getResult())) {
					mDataList = Rdata.getData();
					setAdapter(mDataList);
				} else if (Constant.return_false.equals(Rdata.getResult())) {
					UIHelper.showToast(getApplicationContext(), Rdata.getMsg());
				}
			}
		};
		addMechanismAndGetMetting.subscribe(subscriber);

	}

	/**
	 * @Description: (获取数据成功)
	 * @author 李巷阳
	 * @date 2016-9-9 下午3:16:13
	 */
	private void Load_success() {
		hideBgLoading();
		setVisible();
	}

	/**
	 * @Description: (获取数据失败)
	 * @author 李巷阳
	 * @date 2016-9-9 下午3:16:13
	 */
	private void Load_error() {
		hideBgLoading();
		setInvisible();
	}

	/**
	 * 
	 * @Description: (判断是否是机构二维码)
	 * @author 李巷阳
	 * @date 2016-9-9 上午11:51:39
	 */
	private boolean isMechanismQRcode(final String DataSource) {
		final String GetCompany = StringUtil.getStringByColonHalf(DataSource, "?op", "=");
		if (TextUtils.isEmpty(GetCompany) && !"GetCompany".equals(GetCompany)) {
			showToast(getString(R.string.scan_qr_code_error));
			return false;
		}
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		isStop = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		isStop = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(netReceive);
	}

	/**
	 * 
	 * @Description: (检测版本号是否更新)
	 * @author 李巷阳
	 * @date 2016-9-9 下午3:53:43
	 */
	private void checkVersion() {
		if (!hasNet())
			return;

		if (isStop)
			return;

		XMLParserVersionManager.getInstance().checkUpdate(this, new OnCheckResultListener() {
			@Override
			public void onResult(boolean hasNewVersion, Object result) {
				if (hasNewVersion) {
					final UpdateVersionBean o = (UpdateVersionBean) result;
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (!isStop)
								ViewUtil.showUpdateDialog(MyMeetingActivity.this, o, !isStop);
						}
					}, 2000);
					mv_infodot.setVisibility(View.VISIBLE);

				} else {
					mv_infodot.setVisibility(View.GONE);
				}
			}
		});
	}

	/**
	 * @author 李巷阳
	 * @date 2016-8-25 下午3:10:12
	 */
	private void registerBroadcast() {
		// 只要开启此界面，就要关闭登陆界面。
		Intent intent = new Intent(DRMUtil.BROADCAST_CLEAR_LOGIN_ACTIVITY);
		this.sendBroadcast(intent);

		// 进入下载界面，关闭登陆界面
		IntentFilter netFilter = new IntentFilter();
		netFilter.addAction(DRMUtil.BROADCAST_CLEAR_MYMEETING_ACTIVITY);
		registerReceiver(netReceive, netFilter);
	}

	private BroadcastReceiver netReceive = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			String action = intent.getAction();
			if (action == null)
				return;
			switch (action) {
			case DRMUtil.BROADCAST_CLEAR_MYMEETING_ACTIVITY:
				MyMeetingActivity.this.finish();
				break;
			}
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		};
	};

	private MechanismBean mmechanismbean;

	private boolean hasNet() {
		return CommonUtil.isNetConnect(this);
	}

	@Override
	public void onBackPressed() {
		showExitDialog();
	}

	private void showExitDialog() {
		ViewUtil.showContentDialog(this, "", new OnTVAnimDialogClickListener() {
			@Override
			public void onClick(int dialogId) {
				if (dialogId == TVAnimDialog.DIALOG_CONFIRM) {
					DRMLog.i("Exit App");
					// 停止所有任务
					sendBroadcastStopAllTask();
					stopMediaService();
					ActicityManager.getInstance().exit();
					System.exit(0);
				}
			}
		});
	}

	/**
	 * 停止媒体服务
	 */
	private void stopMediaService() {
		MediaUtils.MUSIC_CURRENTPOS = -1; // 0;
		Intent intent = new Intent(this, MediaService.class);
		intent.putExtra("option", ConstantValue.OPTION_STOP);
		startService(intent);
		DRMUtil.positionOld = -1;
		MediaUtils.current_MusicAlbum_Id = "-1";
		// 清除音乐notification
		delMusicNotify();
	}

	private void delMusicNotify() {
		NotificationPatManager patManager = new NotificationPatManager(this);
		patManager.cancelNotification();
	}

	/**
	 * 发送广播，通知停止所有下载任务
	 */
	private void sendBroadcastStopAllTask() {
		Intent intent = new Intent(this, DownloadService.class);
		intent.setAction(DownloadService.ACTION_ALL_STOP);
		startService(intent);
	}

	/**
	 * @author 李巷阳
	 * @date 2016-8-16 上午11:20:54
	 */
	private void setVisible() {
		mrl_prompt.setVisibility(View.VISIBLE);
		mlv_scanhistory.setVisibility(View.VISIBLE);
		mview.setVisibility(View.VISIBLE);
		mtv_hintsinfo.setVisibility(View.GONE);
	}

	/**
	 * @author 李巷阳
	 * @date 2016-8-16 上午11:19:15
	 */
	private void setInvisible() {
		mrl_prompt.setVisibility(View.GONE);
		mlv_scanhistory.setVisibility(View.GONE);
		mview.setVisibility(View.GONE);
		// mtv_hintsinfo.setVisibility(View.VISIBLE);
	}

}
