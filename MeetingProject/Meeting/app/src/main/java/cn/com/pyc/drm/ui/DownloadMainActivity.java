package cn.com.pyc.drm.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.maxwin.view.XListView;
import com.android.maxwin.view.XListView.IXListViewListener;
import com.android.maxwin.view.internal.PLA_AdapterView;
import com.android.maxwin.view.internal.PLA_AdapterView.OnItemClickListener;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.DownloadLocalAdapter;
import cn.com.pyc.drm.adapter.DownloadMainAdapter;
import cn.com.pyc.drm.bean.ReturnDownloadStaicDataBean;
import cn.com.pyc.drm.bean.ScanHistory;
import cn.com.pyc.drm.bean.event.AlbumUpdateEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.dialog.NormalDialog;
import cn.com.pyc.drm.dialog.NormalDialog.NormalDialogCallBack;
import cn.com.pyc.drm.dialog.TVAnimDialog;
import cn.com.pyc.drm.dialog.TVAnimDialog.OnTVAnimDialogClickListener;
import cn.com.pyc.drm.http.WSHelper;
import cn.com.pyc.drm.model.DownloadInfo;
import cn.com.pyc.drm.model.ProductListInfo;
import cn.com.pyc.drm.model.ProductListModel;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.practice.AlbumDAOImpl;
import cn.com.pyc.drm.model.db.practice.DowndataDAOImpl;
import cn.com.pyc.drm.service.BGOCommandService;
import cn.com.pyc.drm.service.DownloadService;
import cn.com.pyc.drm.service.FloatViewService;
import cn.com.pyc.drm.service.MediaService;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMDBHelper;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.DeviceUtils;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.PathFileUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.DownloadTaskManager;
import cn.com.pyc.drm.utils.manager.ExecutorManager;
import cn.com.pyc.drm.utils.manager.NotificationPatManager;
import cn.com.pyc.drm.utils.manager.RequestHttpManager;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager;
import cn.com.pyc.drm.utils.manager.XMLParserVersionManager.OnCheckDownloadDataListener;
import cn.com.pyc.drm.widget.HighlightImageView;
import de.greenrobot.event.EventBus;

/**
 * 下载主界面
 * 
 */
public class DownloadMainActivity extends BaseActivity implements OnClickListener, IXListViewListener {

	private String TAG = DownloadMainActivity.class.getSimpleName();

	@ViewInject(R.id.download_listview)
	private XListView mPullToListView;

	@ViewInject(R.id.local_listview)
	private XListView mLocalListView;

	@ViewInject(R.id.un_net_layout)
	private View mNetWarning;

	@ViewInject(R.id.empty_layout)
	private View emptyView;

	@ViewInject(R.id.empty_text)
	private TextView emptyText;

	@ViewInject(R.id.tv_teb)
	private ImageView tvTitle;

	@ViewInject(R.id.empty_image)
	private HighlightImageView emptyimage;

	@ViewInject(R.id.txv_bottom_vote)
	private TextView txtVote; // 投票按钮

	@ViewInject(R.id.bottom_menu_webview)
	private WebView mBottomWebView; // 底部菜单

	@ViewInject(R.id.title_meeting_webview)
	private WebView title_meeting_webview; // 标题菜单

	@ViewInject(R.id.fl_title_meeting_webview)
	private FrameLayout fl_title_meeting_webview; // 标题菜单

	@ViewInject(R.id.fl_title_meeting_webview)
	private FrameLayout fl_bottom_menu_webview; // 底部菜单

	@ViewInject(R.id.rl_layout)
	private RelativeLayout rl_layout;

	@ViewInject(R.id.tv_close)
	private TextView tv_close;

	/** 无网络状态 */
	private static final int MSG_NET_NONE = 0x10;
	private int totalPageNum = 0;
	private int currentPage = 1;
	private DownloadMainAdapter adapter;
	private DownloadLocalAdapter localAdapter;
	/** 存储下载的任务id */
	public static Set<String> sTaskIdSet;

	private AlertDialog clearDlg;

	private boolean isStop = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_main);
		init_Value();
		init_View();
		init_Listener();
		load_Data();
		registerBroadcast();
		loadWebView();
	}

	/**
	 * 初始化参数
	 * 
	 * @author 李巷阳
	 * @date 2016-9-26 下午3:35:56
	 */
	@Override
	protected void init_Value() {
		EventBus.getDefault().register(this);
		ActicityManager.getInstance().add(this);
		MeetingDetails = (ScanHistory) getIntent().getSerializableExtra("MeetingDetails"); // 获取会议参数
		init_download_task();// 初始化下载任务
		createIfScaning(); // 扫码登录时，数据库需要重新创建
	}

	/**
	 * 初始化view
	 * @author 李巷阳
	 * @date 2016-9-26 下午3:36:13
	 */
	@Override
	protected void init_View() {
		ViewUtils.inject(this);
		UIHelper.showTintStatusBar(this); // 自定义状态栏
		ImageLoader.getInstance().displayImage(MeetingDetails.getClient_url() + "/resource/logo/logo.png", tvTitle, Constant.options, null);
		hideEmptyView();// "没有加载到数据" 置为不可见。
	}

	/**
	 * @Description: (初始化事件)
	 * @author 李巷阳
	 * @date 2016-9-26 下午3:29:25
	 */
	private void init_Listener() {
		mNetWarning.setOnClickListener(this);
		txtVote.setOnClickListener(this);
		tv_close.setOnClickListener(this);
		mPullToListView.setXListViewListener(this);
		mPullToListView.setPullRefreshEnable(true);
		mPullToListView.setSelector(R.drawable.transparent);
		mPullToListView.setFlingLoadImage(false);

		mLocalListView.setPullLoadEnable(false);
		mLocalListView.setPullRefreshEnable(false);
		mLocalListView.setSelector(R.drawable.transparent);
		mLocalListView.setOnScrollListener(null); // 覆盖XListView中onScroll方法，mTotalItemCount=0,使XListView滑动受阻
		mLocalListView.setXListViewListener(null); // 设置本地listview如果上下拉刷新，则不进行，逻辑处理。
		emptyimage.setOnClickListener(this);
//		mLocalLVOnItemClickListener();
		mPullToLVOnItemClickListener();
	}

	/**
	 * 
	 * @Description: (动态注册广播接收者)
	 * @author 李巷阳
	 * @date 2016-9-26 下午4:12:20
	 */
	private void registerBroadcast() {
		// 注册网络动态变化的广播
		IntentFilter netFilter = new IntentFilter();
//		netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);   李博说文件下载接口取消
		netFilter.addAction(DRMUtil.BROADCAST_RELOAD_HOMEITEM);
		netFilter.addAction(DRMUtil.BROADCAST_CLOSE_ACTIVITY);
		netFilter.addAction(DRMUtil.BROADCAST_CLEAR_DOWNLOADED);
		netFilter.addAction(DRMUtil.BROADCAST_MUSIC_STATUSBAR);
//		registerReceiver(netReceive, netFilter);
		// 定义下载的广播
		IntentFilter downloadFilter = new IntentFilter();
		downloadFilter.addAction(DownloadService.ACTION_FINISH);
		downloadFilter.addAction(DownloadService.ACTION_UPDATE);
		downloadFilter.addAction(DownloadService.ACTION_ERROR);
		downloadFilter.addAction(DownloadService.ACTION_CONNECT_ERROR);
		downloadFilter.addAction(DownloadService.ACTION_CONNECTING);
//		registerReceiver(downloadReceiver, downloadFilter);
		// 注册监听HOME键的广播
		registerReceiver(mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}

	/**
	 * 
	 * @Description: (初始化任务集合栈)
	 * @author 李巷阳
	 * @date 2016-9-26 下午4:27:15
	 */
	private void init_download_task() {
		// 存储下载的任务id
		if (sTaskIdSet == null)
			sTaskIdSet = new LinkedHashSet<String>();
		sTaskIdSet.clear();
	}

	/**
	 * @Description: (横竖屏切换，停止所有下载任务，横屏显示4列。)
	 * @author 李巷阳
	 * @date 2016-6-15 下午4:33:16
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (sTaskIdSet != null && sTaskIdSet.size() > 0) {
			sTaskIdSet.clear();
			hideEmptyView();
			hideNetListView();
			sendBroadcastStopAllTask(); // 停止所有的下载
			recreate();
		} else {
			recreate();
		}
	}

	/**
	 * 
	 * @Description: (加载头布局和底部布局)
	 * @author 李巷阳
	 * @date 2016-9-26 下午4:17:00
	 */
	private void loadWebView() {
		/**
		 * 本地的测试html中含有js和css超链接文件，涉及安全性和权限问题，估计加载不出来
		 * 都放本地测试。html中样式路径保持一致。测试完成删除！
		 */
		String titleHtml = MeetingDetails.getClient_url() + "/resource/meetingtitle/" + MeetingDetails.getMeetingId() + ".html";
		setWebViewCreate(titleHtml, title_meeting_webview);
		title_meeting_webview.getSettings().setDefaultTextEncodingName("gbk");
		title_meeting_webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				Log.d("", "href: " + url);
				return false;
			}
		});

		String vote_url = (String) SPUtils.get(DRMUtil.VOTE_URL, "");
		if (!"".equals(vote_url)) {
			setWebViewCreate(vote_url, mBottomWebView);
			mBottomWebView.addJavascriptInterface(new JavaScriptStartDownloadActivity(this), "StartDownloadActivity");
			//mBottomWebView.addJavascriptInterface(new JavaScriptStartDownloadActivity(this), "StartDownloadActivity");
			mBottomWebView.setWebViewClient(new WebViewClient() {
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
//					Log.d("", "href: " + url);
					return false;
				}
			});
		}
	}

	/**
	 * 
	 * @Description: (网络切换)
	 * @author 李巷阳
	 * @date 2016-9-26 下午4:37:34
	 */
	private void changeNetwork() {
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_top_alpha_anim);
		tvTitle.startAnimation(animation);
		String netStr = CommonUtil.getNetStateType(this);
		DRMLog.d(TAG, "网络类型：" + netStr);
		if (hasNet()) {
			hasNetwork();
			if (CommonUtil.is2G(this) || CommonUtil.is3G(this) || CommonUtil.is4G(this)) {
				showToast(getString(R.string.net_subType_s, netStr));
			}
			// 有网重新加载web页面。
			loadWebView();
		} else {
			noneNetwork();
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void setWebViewCreate(String vote_url, WebView webView) {
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setDefaultTextEncodingName("gb2312");
		webSettings.setAllowFileAccess(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webView.setSaveEnabled(false);
		webView.setVisibility(View.VISIBLE);
		DRMLog.e(TAG, "网页url: " + vote_url);
		//webview只是一个承载体，各种内容的渲染需要使用webviewChromClient去实现,网页自己i
		webView.setWebChromeClient(new WebChromeClient());
		webView.loadUrl(vote_url);
	}

	public class JavaScriptStartDownloadActivity {
		Context mContext;

		JavaScriptStartDownloadActivity(Context c) {
			mContext = c;
		}

		@JavascriptInterface
		public void VisitSuccess(final String url) {
			mHandler.post(new Runnable() {
				public void run() {
					Bundle bundle = new Bundle();
					bundle.putString("webview_url", url);
					openActivity(VoteActivity.class, bundle);
				}
			});
		}

		/**
		 * Created by songyumei on 2017/6/26 网页顶部返回按钮的调用
		 */
		@JavascriptInterface
		public void appGoBack() {
			mHandler.post(new Runnable() {
				public void run() {
					finish();
				}
			});
		}

		/**
		*@Params :
		*@Author :songyumei
		*@Date :2017/9/5
		*/
		@JavascriptInterface
		public void downLoad() {
			mHandler.post(new Runnable() {
				public void run() {
					Intent intent = new Intent(DownloadMainActivity.this, DownloadMainActivity2.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("MeetingDetails", MeetingDetails);
					intent.putExtras(bundle);
					startActivity(intent);
					UIHelper.startInAnim(DownloadMainActivity.this);
//					startActivity(new Intent(DownloadMainActivity.this,DownloadMainActivity2.class));
				}
			});
		}
	}

	/**
	 * 扫码登录时，1.数据库需要重新创建
	 */
	private void createIfScaning() {
		// update:10-29,创建之前销毁helper和db实例
		DRMDBHelper.destoryDBHelper();
		// 扫码登录，需要单独创建数据库。。
		DRMDBHelper helper = new DRMDBHelper(this);
		helper.createDBTable();
		// 创建应用文件保存目录
		PathFileUtil.createFilePath();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NET_NONE: {
				// 无网络，加载本地数据
				Bundle bundle = msg.getData();
				@SuppressWarnings("unchecked")
				List<Album> albumsLocals = (List<Album>) bundle.getSerializable("albumlist");
				hideLoadingIndicatorView();
				if (albumsLocals == null || albumsLocals.isEmpty()) {
					hideLocalListView();
					showEmptyView(getString(R.string.offline_data_empty));
					return;
				}
				localAdapter = new DownloadLocalAdapter(DownloadMainActivity.this, albumsLocals);
				mLocalListView.setAdapter(localAdapter);
			}
				break;
			}
		};
	};

	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			if (context == null)
				context = DownloadMainActivity.this;
			// 必传参数position
			int position = intent.getIntExtra("position", 0);
			DRMLog.d("position", "position = " + position);
			String action = intent.getAction();
			if (action == null)
				return;
			switch (action) {
			case DownloadService.ACTION_UPDATE: {
				long currentSize = intent.getLongExtra("currentSize", 0);
				long totalSize = intent.getLongExtra("totalSize", 0);
				int progress = intent.getIntExtra("progress", 0);
				boolean isLastSaveProgress = intent.getBooleanExtra("isLastSaveProgress", false);
				DRMLog.d("isLastSaveProgress = " + isLastSaveProgress);
				// 正在下载， 更新进度
				if (adapter != null && adapter.getInfos() != null) {
					DownloadInfo o = adapter.getInfos().get(position);
					o.setProgress(progress);
					o.setCurrentSize(currentSize);
					if (o.getTotalSize() == 0) {
						o.setTotalSize(totalSize);
					}
					// 最后一次进度只是用来显示，不刷新界面imageview控件
					if (isLastSaveProgress) {
						o.setTaskState(DownloadTaskManager.PAUSE);
						adapter.updateItemViewWhenDownload(context, position, o, mPullToListView);
					} else {
						o.setTaskState(DownloadTaskManager.DOWNLOADING);
						adapter.updateProgress(context, position, o, mPullToListView);
					}
				}

			}
				break;
			case DownloadService.ACTION_ERROR: {
				String myProId = intent.getStringExtra("myProId");
				// ftp路径异常或关闭，下载失败
				sTaskIdSet.remove(myProId);
				DownloadInfo o = adapter.getInfos().get(position);
				o.setTaskState(DownloadTaskManager.DOWNLOAD_ERROR);
				adapter.updateItemViewWhenDownload(context, position, o, mPullToListView);
				// 暂停下载
				sendBroadcastStopTask(myProId);
				showToast("链接错误,任务下载异常!");
				// :删除本地下载的部分文件，清除数据库进度记录
			}
				break;
			case DownloadService.ACTION_CONNECT_ERROR: {
				String myProId = intent.getStringExtra("myProId");
				DRMLog.e("", "connect error : " + myProId);
				// 连接异常，失败
				sTaskIdSet.remove(myProId);
				DownloadInfo o = adapter.getInfos().get(position);
				o.setTaskState(DownloadTaskManager.CONNECT_ERROR);
				adapter.updateItemViewWhenDownload(context, position, o, mPullToListView);
				// 暂停任务
				sendBroadcastStopTask(myProId);
				showToast("连接服务器失败！");
			}
				break;
			case DownloadService.ACTION_CONNECTING: {
				// 正在连接
				DownloadInfo o = adapter.getInfos().get(position);
				o.setTaskState(DownloadTaskManager.CONNECTING);
				adapter.updateItemViewWhenDownload(context, position, o, mPullToListView);
			}
				break;
			case DownloadService.ACTION_FINISH: {
				// 下载完成
				String myProId = intent.getStringExtra("myProId");
				String author = intent.getStringExtra("author");
				String picture_ratio = intent.getStringExtra("picture_ratio");
				String publishDate = intent.getStringExtra("publish_date");
				DRMLog.i("下载第" + position + "个位置的文件成功: " + myProId);
				DRMLog.w("publishDate: " + publishDate);

				sTaskIdSet.remove(myProId);
				DownloadInfo o = adapter.getInfos().get(position);
				o.setTaskState(DownloadTaskManager.COMPLETE_PARSER);
				adapter.updateItemViewWhenDownload(context, position, o, mPullToListView);
				// 删除记录
				DowndataDAOImpl.getInstance().deleteDowndataByMyProId(myProId);
				// 开始解析文件
				startAnalyticalService(myProId, position, author, picture_ratio, publishDate);
			}
				break;
			}
		}
	};
	// 下载统计接口
	private void CollDownloadStatisticsWS(String topicname) {
		String memberid = (String) SPUtils.get(DRMUtil.KEY_PHONE_NUMBER, "");
		String devicesid = DeviceUtils.getIMEI(DownloadMainActivity.this);

		WSHelper.newBuilder().setmUrl(Constant.getDownloadStatistics_URL()).setMethodName(Constant.DownloadStatistics_MethodName)
				.putParam("meetingid",MeetingDetails.getMeetingId())
				.putParam("telephone",memberid)
				.putParam("topicname",topicname)
				.putParam("devicesid",devicesid)
				.setCallback(new WSHelper.OnLoadDataListener<ReturnDownloadStaicDataBean>() {
					@Override
					public void onSuccess(ReturnDownloadStaicDataBean returnDownloadStaicDataBean) {
					}

					@Override
					public void onError(String error_str, Exception e) {
					}
				}).build(mBaseActivity,new TypeToken<ReturnDownloadStaicDataBean>() {}.getType());

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
			case ConnectivityManager.CONNECTIVITY_ACTION:
				changeNetwork();
				break;
			case DRMUtil.BROADCAST_CLOSE_ACTIVITY:
				// 注销，关闭页面
				logOutWithCloseActivity();
				break;
			case DRMUtil.BROADCAST_RELOAD_HOMEITEM: {
				// 解析文件完毕,通知ui更新
				int position = intent.getIntExtra("analytic_position", 0);
				DownloadInfo o = adapter.getInfos().get(position);
				adapter.updateItemViewAfterAnalysisFile(context, position, o, mPullToListView);
				showToast(getString(R.string.download_n_data_complete, o.getProductName()));
				// 文件下载成功后，调用服务器接口
//				setRecordsConsumption(o);
				CollDownloadStatisticsWS(o.getProductName());
			}
				break;
			case DRMUtil.BROADCAST_CLEAR_DOWNLOADED:
				// 清理缓存后，重新加载数据
				loadingAfterClear();
				break;
			case DRMUtil.BROADCAST_MUSIC_STATUSBAR:
				// 音乐通知栏关闭广播
				delMusicNotify();
				break;
			}
		}
	};

	/*@Override
	public void onBackPressed() {
		if (!"".equals(DRMUtil.Is_analytic)) {
			showToast(getString(R.string.download_being_parsed));
			return;
		}
		// 注销，关闭页面
		logOutWithCloseActivity();
	}*/

	/**
	 * Created by songyumei on 2017/6/22  返回键处理
	 * @param keyCode
	 * @param event
     * @return
     */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (mBottomWebView.canGoBack()) {
				mBottomWebView.goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private ScanHistory MeetingDetails;

	// 注销用户，停止所有下载并且关闭当前主页面。
	private void logOutWithCloseActivity() {
		// 停止所有的下载
		sendBroadcastStopAllTask();
		closeAudioSing();
		delMusicNotify();
		if (adapter != null) {
			adapter.getInfos().clear();
			adapter = null;
		}
		finish();
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

//	/**
//	 * 文件下载成功后，调用服务器接口
//	 */
//	protected void setRecordsConsumption(final DownloadInfo o) {
//		if (hasNet()) {
//			new AsyncTask<String, String, String>() {
//				protected String doInBackground(String... params) {
//					Album albm = DowndataDAOImpl.getInstance().findAlbumByMyproId(o.getMyProId());
//					String[] pid = { "album_id" };
//					String[] pidvalue = { albm.getId() };
//					@SuppressWarnings("unchecked")
//					List<AlbumContent> mediaList = (List<AlbumContent>) AlbumContentDAOImpl.getInstance().findByQuery(pid, pidvalue, AlbumContent.class);
//					JSONArray json = new JSONArray();
//					for (AlbumContent media : mediaList) {
//						JSONObject jo = new JSONObject();
//						try {
//							jo.put("fileid", media.getContent_id().replace("\"", ""));
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
//						json.put(jo);
//					}
//					String fileidjson = json.toString();
//					String meetingid = MeetingDetails.getMeetingId();
//					String devicesid = DeviceUtils.getIMEI(DownloadMainActivity.this);
//					String downloadname = Constant.getUserName();
//					String result = RequestHttpManager.setDownloadsuccess(meetingid, fileidjson, devicesid, downloadname);
//					return result;
//				}
//
//				@Override
//				protected void onPostExecute(String result) {
//				}
//			}.execute();
//		}
//	}

	private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
		String SYSTEM_REASON = "reason";
		String SYSTEM_HOME_KEY = "homekey";
		String SYSTEM_HOME_KEY_LONG = "recentapps";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
					if (hasNet()) {
						loadNetData(true);
					}
					// 表示按了home键,程序到了后台
				} else if (TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)) {
					// 表示长按home键,显示最近使用的程序列表
				}
			}
		}
	};

	/**
	 * 网络连接
	 */
	private void hasNetwork() {
		mNetWarning.setVisibility(View.GONE);
		hideLocalListView();
		loadNetData(true);
	}

	/**
	 * 无网络
	 */
	private void noneNetwork() {
		sTaskIdSet.clear();
		hideEmptyView();
		hideNetListView();
		mNetWarning.setVisibility(View.VISIBLE);

		showToast(
				R.string.network_is_not_available);
		// 停止所有的下载
		sendBroadcastStopAllTask();
		loadLocalData();
	}

	/**
	 * 清理缓存数据后，重新加载
	 */
	private void loadingAfterClear() {
		hideNetListView();
		hideLocalListView();
		hideEmptyView();
		mNetWarning.setVisibility(View.GONE);
		// 停止所有的下载
		sTaskIdSet.clear();
		sendBroadcastStopAllTask();
		stopMediaService();
		DRMDBHelper.deleteTableData();
		// 标题滑入动画
		tvTitle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_top_alpha_anim));
		if (hasNet()) {
			loadNetData(false);

		} else {
			loadLocalData();
		}
	}

	/**
	 * 
	 * @Description: (设置网络listview ，item点击事件。)
	 * @author 李巷阳
	 * @date 2016-9-26 下午3:33:40
	 */
	private void mPullToLVOnItemClickListener() {
		mPullToListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(PLA_AdapterView<?> parent, View view, int position, long id) {
				// sd卡，不可用
				if (!CommonUtil.isSdCardCanUsed()) {
					tos.showFail(getApplicationContext(), "SD卡不可用~");
					return;
				}
				// 正在刷新，点击不下载。
				if (mPullToListView.ismPullRefreshing() || mPullToListView.ismPullLoading()) {
					showToast("正在更新数据，请稍候~");
					return;
				}
				Object obj = mPullToListView.getItemAtPosition(position);
				// 减去headerView所占的位置
				int mPosition = position - 1;
				clickItemOperation(mPosition, obj, view);
			}
		});
	}

	/**
	 * 
	 * @Description: (设置本地listview，item点击事件处理。)
	 * @author 李巷阳
	 * @date 2016-9-26 下午3:33:25
	 */
	private void mLocalLVOnItemClickListener() {
		mLocalListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(PLA_AdapterView<?> parent, View view, int position, long id) {
				// sd卡，不可用
				if (!CommonUtil.isSdCardCanUsed()) {
					tos.showFail(getApplicationContext(), "SD卡不可用~");
					return;
				}
				// 如果正在刷新，则弹出Toast提示。
				if (mPullToListView.ismPullRefreshing()) {
					showToast("正在刷新，请稍候~");
					return;
				}
				// 获取用户点击的item对象的数据源。
				Object object = mLocalListView.getItemAtPosition(position);
				if (object != null && object instanceof Album) {
					// 打开已经下载的资源文件。
					Album albm = (Album) object;
					startMultiMedia(albm, position);
				} else {
					DRMLog.d("local", "Album is null");
				}
			}
		});
	}

	/**
	 * 网络状态下item点击
	 * 
	 * @param position
	 * @param obj
	 */
	private void clickItemOperation(int position, Object obj, View view) {
		if (obj != null && obj instanceof DownloadInfo) {
			DownloadInfo o = (DownloadInfo) obj;
			DRMLog.e(TAG, "o.getPosition() = " + o.getPosition());
			DRMLog.e(TAG, "o.getMyProId() = " + o.getMyProId());
			Album albm = DowndataDAOImpl.getInstance().findAlbumByMyproId(o.getMyProId());
			if (albm != null) {
				// 产品发布的时间
				String publishDate = o.getPublishDate();
				// 解析后保存的发布时间
				String savePublishDate = albm.getPublishDate();
				// 不需要更新操作，可以打开本地文件。
				if (TextUtils.isEmpty(publishDate) || TextUtils.equals(publishDate, savePublishDate)) {
					// 专辑不为空，打开
					startMultiMedia(albm, position);
				} else {
					// 发送通知：1.删除此前下载的专辑，2.删除权限，3.重新下载
					int taskSize = DownloadMainActivity.sTaskIdSet.size();
					if (taskSize >= Constant.sTaskCount) {
						showToast(DownloadMainActivity.this.getString(R.string.please_waiting_n_update_download, taskSize));
						return;
					}
					EventBus.getDefault().post(new AlbumUpdateEvent(o, o.getMyProId()));
					Button button_update = (Button) view.findViewById(R.id.button_update);
					button_update.setVisibility(View.GONE);
				}
			} else {
				// :后续功能延续扩展
				// DRMLog.d(TAG, "后续功能延续扩展");
				// 专辑为空，需要下载，解析
				if (!hasNet()) {
					tos.showBusy(getApplicationContext(), "亲，网络连接不可用。");
					return;
				}
				if (o.getTaskState() == DownloadTaskManager.COMPLETE_PARSER) {
					showToast(getString(R.string.please_waiting_now_parser));
					return;
				}
				// 需要下载
				needDownload(position, o);
			}
		}
	}

	private void needDownload(final int position, final DownloadInfo o) {
		if (adapter == null) {
			showToast(getString(R.string.load_data_null));
			return;
		}
		final String myProId = o.getMyProId();
		// 检查再下载队列里，如果有此任务，则说明正在下载，或者等待下载，则暂停任务。
		if (sTaskIdSet.contains(myProId)) {
			// 任务已存在，暂停下载任务
			sendBroadcastStopTask(myProId);
			o.setTaskState(DownloadTaskManager.PAUSE);
			// 在adapter修改暂停按钮。
			adapter.updateItemViewWhenDownload(this, position, o, mPullToListView);
			// 在下载任务中删除此proid.
			sTaskIdSet.remove(myProId);
		} else {
			if (!CommonUtil.isWifi(DownloadMainActivity.this)) {
				clearDlg = new NormalDialog().dialog(DownloadMainActivity.this, "您正在使用非WiFi网络。", "是否继续下载?", new NormalDialogCallBack() {
					public void getConfirm(View v) {
						start_download(position, o, myProId);
					}

					public void getCancel(View v) {
					}
				});
			} else {
				start_download(position, o, myProId);
			}
		}
	}

	private void start_download(int position, DownloadInfo o, String myProId) {
		// 如果任务超过4个子就限制任务个数。
		int taskSize = sTaskIdSet.size();
		if (taskSize >= Constant.sTaskCount) {
			// 限制任务个数
			showToast(getString(R.string.please_waiting_n_download, taskSize));
			return;
		}
		// 任务不存在,加入下载队列
		startDownloadService(o, position);
		o.setTaskState(DownloadTaskManager.WAITING);
		adapter.updateItemViewWhenDownload(this, position, o, mPullToListView);
		sTaskIdSet.add(myProId);
	}

	/**
	 * 开始下载任务
	 * 
	 * @param o
	 * @param position
	 */
	private void startDownloadService(DownloadInfo o, int position) {
		o.setPosition(position);
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("option", ConstantValue.START_DOWNLOAD);
		intent.putExtra("DownloadInfo", o);
		startService(intent);
	}

	/**
	 * 开启service,解析已下载文件
	 * 
	 * @param myProId
	 * @param position
	 * @param author
	 * @param picture_ratio
	 */
	private void startAnalyticalService(String myProId, int position, String author, String picture_ratio, String publishDate) {
		Intent in = new Intent(this, BGOCommandService.class);
		in.putExtra("option", ConstantValue.RESOLVE_DOWNLOAD);
		in.putExtra("myProId", myProId);
		in.putExtra("position", position);
		in.putExtra("author", author);
		in.putExtra("picture_ratio", picture_ratio);
		in.putExtra("publish_date", publishDate);
		startService(in);
	}

	/**
	 * 打开对应的资源文件
	 * 
	 * @param position
	 */
	private void startMultiMedia(Album album, int position) {
		if (album.getCategory() == null) {
			return;
		}
		OpenAlbum(album, position);
	}

	private void OpenAlbum(Album album, int position) {
		// 停止音乐
		stopMusicService(position, album);
		// 传递专辑对象
		Bundle bundle = new Bundle();
		// 专辑Serializable对象。
		bundle.putSerializable("Album", album);
		// 标识是从主界面跳转
		bundle.putString("Judging_jump", DRMUtil.from_DownloadMain);

		switch (album.getCategory()) {
		case "MUSIC": {
			openActivity(MusicAlbumActivity.class, bundle);
			UIHelper.startInAnim(this);
		}
			break;
		case "BOOK": {
			openActivity(MuPDFAlbumActivity.class, bundle);
			UIHelper.startInAnim(this);
		}
			break;
		case "VIDEO": {
			MediaUtils.MUSIC_CURRENTPOS = -1; // 0;
			openActivity(VideoAlbumActivity.class, bundle);
			UIHelper.startInAnim(this);
		}
			break;
		}
	}

	private void stopMusicService(int position, Album albm) {
		if (DRMUtil.positionOld == position) {
			return;
		}
		if (MediaUtils.current_MusicAlbum_Id.equals(albm.getMyproduct_id())) {
			return;
		}
		if (!"BOOK".equalsIgnoreCase(albm.getCategory())) {
			// 获取用户上次播放的子专辑的位置。
			MediaUtils.MUSIC_CURRENTPOS = SaveIndexDBManager.Builder(this).findIndexByMyProId(albm.getMyproduct_id());
			// 关闭音乐播放
			Intent intent = new Intent(this, MediaService.class);
			intent.putExtra("option", ConstantValue.OPTION_STOP);
			startService(intent);
			DRMUtil.positionOld = position;
			MediaUtils.current_MusicAlbum_Id = albm.getMyproduct_id();

			if ("VIDEO".equalsIgnoreCase(albm.getCategory())) {
				// 关闭音乐通知
				Intent close_music_bar = new Intent(DRMUtil.BROADCAST_MUSIC_STATUSBAR);
				close_music_bar.putExtra(MediaUtils.NOTIFY_BUTTONID_TAG, MediaUtils.BUTTON_CLOSE_ID);
				sendBroadcast(close_music_bar);
			}
		}
	}

	/**
	 * 发送停止下载的广播
	 * 
	 * @param myProId
	 */
	private void sendBroadcastStopTask(String myProId) {
		Intent intent = new Intent(this, DownloadService.class);
		intent.setAction(DownloadService.ACTION_STOP);
		intent.putExtra("myProId", myProId);
		startService(intent);
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

	// @Override
	// protected void load_Data() {
	// // 第一次进入app登陆成功后，进入主界面
	// if (!DRMUtil.isForFirstTime) {
	// if (hasNet()) {
	// loadNetData(true);
	// } else {
	// loadLocalData();
	// }
	// }
	// }

	protected void loadDataEmptyOnclick() {
		if (hasNet()) {
			loadNetData(true);
		} else {
			loadLocalData();
		}
	}

	private boolean hasNet() {
		return CommonUtil.isNetConnect(this);
	}

	// @Override
	// public void onBackPressed() {
	// showExitDialog();
	// }

	// private void showExitDialog() {
	// ViewUtil.showContentDialog(this, "", new OnTVAnimDialogClickListener() {
	//
	// @Override
	// public void onClick(int dialogId) {
	// if (dialogId == TVAnimDialog.DIALOG_CONFIRM) {
	// DRMLog.i("Exit App");
	// // 停止所有任务
	// sendBroadcastStopAllTask();
	// stopMediaService();
	// DownloadMainActivity.this.finish();
	// }
	// }
	// });
	// }

	@Override
	protected void onStart() {
		super.onStart();
		isStop = false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (MediaUtils.playState == ConstantValue.OPTION_PLAY || MediaUtils.playState == ConstantValue.OPTION_CONTINUE) {
			// 播放或者继续播放状态,显示悬浮窗并开启旋转动画
			FloatViewService.openFloatView(getApplicationContext(), true);
		}

		if (MediaUtils.playState == ConstantValue.OPTION_PAUSE) {
			// 暂停,显示悬浮窗，关闭动画
			FloatViewService.openFloatView(getApplicationContext(), false);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		isStop = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sTaskIdSet.clear();
		clearDlag();
		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
		WebViewClear();
		unregisterReceiver(mHomeKeyEventReceiver);
//		unregisterReceiver(netReceive);
//		unregisterReceiver(downloadReceiver);
		DownloadTaskManager.closeExecutorService();
		XMLParserVersionManager.getInstance().shutDownCheckTask();
		EventBus.getDefault().unregister(this);
		// System.exit(0);
	}

	private void clearDlag() {
		if (clearDlg != null)
			clearDlg.dismiss();
	}

	private void WebViewClear() {
		if (mBottomWebView != null) {
			ViewGroup parent = (ViewGroup) mBottomWebView.getParent();
			if (parent != null) {
				parent.removeView(mBottomWebView);
			}
			mBottomWebView.removeAllViews();
			mBottomWebView.destroy();
		}
		if (title_meeting_webview != null) {
			ViewGroup parent = (ViewGroup) title_meeting_webview.getParent();
			if (parent != null) {
				parent.removeView(title_meeting_webview);
			}
			title_meeting_webview.removeAllViews();
			title_meeting_webview.destroy();
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.tv_close: {
			// UIHelper.finishActivity(DownloadMainActivity.this);
			// finish();
			if (!"".equals(DRMUtil.Is_analytic)) {
				showToast(getString(R.string.download_being_parsed));
				return;
			}
			logOutWithCloseActivity();
		}
			break;
		case R.id.un_net_layout: {
			Intent wifiSettingsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
			startActivity(wifiSettingsIntent);
		}
			break;

		case R.id.txv_bottom_vote: {
			Intent intent = new Intent(DownloadMainActivity.this, VoteActivity.class);
			intent.putExtra("webview_url", MeetingDetails.getVote_url() + "mobile=" + SPUtils.get("mobile", "") + "&meetingid=" + MeetingDetails.getMeetingId());
			intent.putExtra("vote_title", MeetingDetails.getVote_title());
			startActivity(intent);
			UIHelper.startInAnim(DownloadMainActivity.this);
		}
			break;

		case R.id.empty_image: {
			loadDataEmptyOnclick();// 在没有数据时，显示没有加载到数据的图片，点击图片，访问网络，获取数据。
		}
			break;

		}
	}

	private void delMusicNotify() {
		NotificationPatManager patManager = new NotificationPatManager(this);
		patManager.cancelNotification();
	}

	/**
	 * load本地数据
	 */
	private void loadLocalData() {
		currentPage = 1;
		showIndicatorViewLoading();
		showLocalListView();
		Thread mLocal_t = new Thread(new Runnable() {
			@Override
			public void run() {
				if (localAdapter != null) {
					localAdapter.getAlbums().clear();
					localAdapter = null;
				}
				List<Album> albumlist = AlbumDAOImpl.getInstance().findAll(Album.class, "DESC");
				Message msg = Message.obtain();
				msg.what = MSG_NET_NONE;
				Bundle bl = new Bundle();
				bl.putSerializable("albumlist", (Serializable) albumlist);
				msg.setData(bl);
				mHandler.sendMessageDelayed(msg, 800);
			}
		});
		ExecutorManager.getInstance().execute(mLocal_t);
	}

	/**
	 * 加载网络数据，从currentpage = 1开始
	 * 
	 * @param showLoading
	 */
	private boolean loadNetData(boolean showLoading) {
		if (showLoading)
			showIndicatorViewLoading();
		currentPage = 1;
		if (adapter != null)
			adapter = null;
		requestData();
		return true;
	}

	/**
	 * 请求网络数据
	 * 
	 */
	private void requestData() {
		showNetListView(true);
		DRMLog.i("currentPage = " + currentPage);
		// 扫码登录
		if (TextUtils.isEmpty(MeetingDetails.getScanDataSource())) {
			// 网络切换频繁，resultUrlExtra值不在保有原来的引用值，可能被置成null了
			hideLoadingIndicatorView();
			showToast(getString(R.string.scaning_again));
			showEmptyView(getString(R.string.empty_scaning_again));
			return;
		}
		String actionUrl = MeetingDetails.getScanDataSource();
		String memberid = (String) SPUtils.get(DRMUtil.KEY_PHONE_NUMBER, "");
		getNetworkData(actionUrl, memberid);

	}

	private void getNetworkData(String actionUrl, String memberid) {

		Bundle bundle = new Bundle();
		bundle.putString("token", (DeviceUtils.getIMEI(this) != null) ? DeviceUtils.getIMEI(this) : DeviceUtils.getLocalMacAddress(this));
		bundle.putInt("category", 0);
		bundle.putInt("page", currentPage);
		if (!"".equals(memberid))
			bundle.putString("memberId", memberid);

		RequestHttpManager.init().getData(actionUrl, bundle, new RequestCallBack<String>() {

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				DRMLog.e(TAG, "mJsonString" + arg0.result);
				String mJsonString = arg0.result;
				final ProductListModel oldProductListModel = JSON.parseObject(mJsonString, ProductListModel.class);

				if (oldProductListModel.isSuccess()) {
					// 如果为null,则不是短信验证，然不用权限控制。
					getDataSuccess(oldProductListModel);
				} else {
					if (oldProductListModel.getMsg() != null && oldProductListModel.getMsg().contains("验证")) {
						// 重新登录，获取数据和token
						SPUtils.remove(DRMUtil.KEY_LOGIN_TOKEN);
					} else {
						getDataFailed(R.string.getdata_failed);
					}
				}
				mPullToListView.stopRefresh();
				mPullToListView.stopLoadMore();
				hideLoadingIndicatorView();

			}

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				mPullToListView.stopRefresh();
				mPullToListView.stopLoadMore();
				getDataFailed(R.string.getdata_failed);
				hideLoadingIndicatorView();

			}
		});
	}

	private void getDataSuccess(ProductListModel o) {
		hideLoadingIndicatorView();
		hideEmptyView();
		if (o.getData() == null) {
			hideNetListView(); // 隐藏netListview,避免netListview接收滑动崩溃
			showEmptyView(getString(R.string.getdata_failed));
			return;
		}
		ProductListInfo oo = o.getData();
		List<DownloadInfo> oNetList = oo.getItems();
		totalPageNum = oo.getTotalPageNum();
		if (oNetList == null || oNetList.isEmpty()) {
//			Toast.makeText(getApplicationContext(), "没有加载到数据", Toast.LENGTH_SHORT).show();
			hideNetListView();
			showEmptyView(null);
			return;
		}
		if (adapter == null || currentPage == 1) {
			// 刷新或者第一次加载
			adapter = new DownloadMainAdapter(this, oNetList);
			mPullToListView.setAdapter(adapter);
			// 检查数据表DownloadData中，是否有未下载完成的记录
			checkExistDownloadDataRecord(oNetList);
		} else {
			// 加载更多
			adapter.addLastInfos(oNetList);
			adapter.notifyDataSetChanged();
		}
		showNetListView(false);
		mPullToListView.setPullLoadEnable(currentPage < totalPageNum);

	}

	private void getDataFailed(int str) {
		hideLoadingIndicatorView();
		if (adapter == null || currentPage == 1) {
			// 刷新时,数据空
		} else {
			if (currentPage > 1)
				currentPage--;
		}
		// 隐藏netListview,避免netListview接收滑动崩溃
		hideNetListView();
		showEmptyView(getString(str));
	}

	/**
	 * 加载更多数据
	 */
	private boolean loadMoreData() {
		currentPage++;
		if (currentPage <= totalPageNum) {
			mPullToListView.setPullLoadEnable(true);
			requestData();
		} else {
			mHandler.postDelayed(new Runnable() {
				public void run() {
					currentPage--;
					mPullToListView.stopLoadMore();
					// 不显示加载更多
					mPullToListView.setPullLoadEnable(false);
					showToast(getResources().getString(R.string.the_last_page));
				}
			}, 500);
		}
		return true;
	}

	@Override
	public void onRefresh() {

		dataWorker(true);
	}

	@Override
	public void onLoadMore() {

		dataWorker(false);
	}

	/**
	 * 加载数据data
	 * 
	 * @param onRefresh
	 *            是否刷新
	 */
	private void dataWorker(final boolean onRefresh) {
		if (sTaskIdSet.isEmpty() && "".equals(DRMUtil.Is_analytic)) {
			boolean result = onRefresh ? loadNetData(false) : loadMoreData();
			DRMLog.i("dateWorker " + result);
		} else {
			// 提示刷新,防止下载时多次刷新页面
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (onRefresh) {
						mPullToListView.stopRefresh();
					} else {
						mPullToListView.stopLoadMore();
					}
				}
			}, 2000);
		}
	}

	/**
	 * 显示本地的listview,网络view不可见
	 */
	private void showLocalListView() {
		if (mLocalListView.getVisibility() == View.GONE)
			mLocalListView.setVisibility(View.VISIBLE);
		hideNetListView();
	}

	/**
	 * 显示网络和已下载的listview,本地View不可见
	 */
	private void showNetListView(boolean isHideLocal) {
		if (isHideLocal)
			hideLocalListView();
		if (mPullToListView.getVisibility() == View.GONE) {
			DRMLog.i("showNetListView");
			mPullToListView.setVisibility(View.VISIBLE);
		}
	}

	/***
	 * 显示emptyView
	 * 
	 * @param tips
	 *            提示信息
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void showEmptyView(String tips) {
		emptyView.setVisibility(View.VISIBLE);
		if (!TextUtils.isEmpty(tips))
			emptyText.setText(tips);
		emptyView.animate().alpha(1f).setDuration(500).setListener(null);
	}

	/**
	 * 如果‘没有加载数据图片’可见，则置为不可见。
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void hideEmptyView() {
		if (emptyView.getVisibility() == View.VISIBLE) {
			emptyView.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					emptyView.setVisibility(View.GONE);
				}
			});
		}
	}

	private void hideLocalListView() {
		if (mLocalListView.getVisibility() == View.VISIBLE) {
			mLocalListView.setVisibility(View.GONE);
		}
	}

	private void hideNetListView() {
		if (mPullToListView.getVisibility() == View.VISIBLE) {
			mPullToListView.setVisibility(View.GONE);
		}
	}

	private void hideNetWarning() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mNetWarning.setVisibility(View.GONE);
			}
		}, 4000);
	}

	// 对照返回的数据记录，检查保存的已下载记录。
	@Deprecated
	private void checkExistDownloadDataRecord(List<DownloadInfo> infos) {
		if (isStop)
			return;

		XMLParserVersionManager.getInstance().checkExistDownloadData(infos, new OnCheckDownloadDataListener() {

			@Override
			public void onExist(boolean isSingleData, final int position, Object obj) {

				if (isSingleData) {
					final DownloadInfo o = (DownloadInfo) obj;
					// 下载
					ViewUtil.showContentDialog(DownloadMainActivity.this, getString(R.string.download_1_task_to_handle, o.getProductName()), new OnTVAnimDialogClickListener() {

						@Override
						public void onClick(int dialogId) {
							if (dialogId == TVAnimDialog.DIALOG_CONFIRM) {
								needDownload(position, o);
							}
						}
					});
				} else {
					showToast(getString(R.string.download_n_task_to_handle, (Integer) obj));
				}
			}
		});
	}

	/**
	 * onEventMainThread:如果使用onEventMainThread作为订阅函数，那么不论事件是在哪个线程中发布出来的，
	 * onEventMainThread都会在UI线程中执行
	 * ，接收事件就会在UI线程中运行，这个在Android中是非常有用的，因为在Android中只能在UI线程中跟新UI
	 * ，所以在onEvnetMainThread方法中是不能执行耗时操作的。
	 * 
	 * 更新数据
	 * 
	 * @param event
	 */
	public void onEventMainThread(AlbumUpdateEvent event) {
		DownloadInfo o = event.getO();
		String myProId = event.getMyProId();

		DRMDBHelper.deleteAlbumAttachInfos(getApplicationContext(), myProId);

		int position = o.getPosition();
		DRMLog.e(TAG, "update album position is " + position + "，download album again");

		// 下载
		needDownload(position, o);
	}

	/**
	 * @author 李巷阳
	 * @date 2016-9-26 下午3:47:05
	 */
	@Override
	protected void load_Data() {

	}
}
