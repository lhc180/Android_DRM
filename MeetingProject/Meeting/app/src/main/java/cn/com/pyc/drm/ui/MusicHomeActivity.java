package cn.com.pyc.drm.ui;

import java.io.File;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.MusicViewPagerAdaper;
import cn.com.pyc.drm.bean.event.MusicSwitchNameEvent;
import cn.com.pyc.drm.bean.event.MusicUpdateStatusEvent;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.fragment.FragmentMusicImg;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.service.FloatViewService;
import cn.com.pyc.drm.service.MediaService;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.ImageUtils;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.ObjectCacheUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.NotificationPatManager;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.ToastShow;
import cn.com.pyc.drm.widget.waveview.WaveView;
import de.greenrobot.event.EventBus;

/**
 * 音乐主界面
 * 
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN) public class MusicHomeActivity extends FragmentActivity implements OnClickListener
{

	private static final String TAG = "MusicHomeActivity";
	
	public static int bgId = 0;
	private static final int[] drawableBgIds = new int[]
	{ R.drawable.music_bg, R.drawable.music_bg_2, R.drawable.music_bg_3 };
	
	private ImageButton prompt;
	private ViewPager mViewPager;
	private LinearLayout llPointGroup;
	private TextView currTimeTextView;
	private TextView totalTimeTextView;
	private SeekBar progressSeekBar;
	private ImageButton pausebtn;
	private RelativeLayout musicbackground;
	private TextView miniLyricShow; // gone
	private WaveView waveView;

	private MusicViewPagerAdaper adapter;
	private Album album;
	private String mAlbumImageUrl;;
	private NotificationPatManager notifyPatManager;
	private ToastShow ts = ToastShow.getInstances_();
	private MediaUtils mediaUtils = MediaUtils.getInstance();
	private static final int ALBUM_MUSIC_REQUEST = 12;
	private String judging_jump;

	// public LyrcUtil lyrcUtil;
	public Handler handler = new Handler();
	public static MusicHomeActivity sMusicActivity;
	public static String sMyProductID;

	private boolean isActivityStart;

	private BroadcastReceiver receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent == null)
				return;
			String action = intent.getAction();
			if (action == null)
				return;

			int currentPosint = intent.getIntExtra("CurrentPositionInt", -1);
			int Durationint = intent.getIntExtra("DurationInt", -1);
			String currentPosintStr = intent.getStringExtra("CurrentPositionStr");
			String DurationintStr = intent.getStringExtra("DurationStr");

			switch (action)
			{
			case DRMUtil.BROADCAST_MUSIC_PROGRESS:
			{
				// 进度
				if (MediaUtils.playState == ConstantValue.OPTION_STOP)
				{
					if (isActivityStart)
						initProgress();
					return;
				}
				// activity可见时更新ui
				if (isActivityStart)
				{
					totalTimeTextView.setText(DurationintStr);
					currTimeTextView.setText(currentPosintStr);
					progressSeekBar.setMax(Durationint);
					progressSeekBar.setProgress(currentPosint);
				}
			}
				break;

			case DRMUtil.BROADCAST_MUSIC_OBTAIN_TIME:
			{
				// 获取时间
				totalTimeTextView.setText(DurationintStr);
				currTimeTextView.setText(currentPosintStr);
				progressSeekBar.setMax(Durationint);
				progressSeekBar.setProgress(currentPosint);
			}
				break;

			case DRMUtil.BROADCAST_MUSIC_STATUSBAR:
			{
				// 通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
				int buttonId = intent.getIntExtra(MediaUtils.NOTIFY_BUTTONID_TAG, 0);
				switch (buttonId)
				{
				case MediaUtils.BUTTON_CLOSE_ID:
					delMusicNotify();
					DRMLog.d(TAG, "close notification");
					break;
				}
			}
				break;
			case DRMUtil.BROADCAST_MUSIC_Close_Music:
				CloseMusic();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		ActicityManager.getInstance().add(this);
//		if (getWindow() != null)
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		sMusicActivity = this;
		EventBus.getDefault().register(this);

		initData();
		initView();
		registerBroadcast();
		// 如果是专辑列表界面跳转过来的，就初始化音乐播放。如果是在主界面或者pdf界面点击音乐图标，则不用做初始化操作。
		if (TextUtils.equals(judging_jump, DRMUtil.from_MusicAlbum))
		{
			initState();
		}

		// 当前音乐主界面，去掉悬浮窗
		if (CommonUtil.isTopActivy(this, MusicHomeActivity.class.getName()))
		{
			FloatViewService.closeFloatView(this);
		}
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		setIntent(intent);

		checkData();
	}

	private void initData()
	{
		checkData();
		
		int index = new Random().nextInt(drawableBgIds.length);
		if (bgId == 0)
			bgId = drawableBgIds[index];
		
		mediaUtils.initMedia(getApplicationContext(), album.getId());
		sMyProductID = album.getMyproduct_id();
		mAlbumImageUrl = album.getPicture();

		notifyPatManager = new NotificationPatManager(this);
		notifyPatManager.setAlbum(album);

		getObtainTime();

	}

	/**
	 * 检查数据，如果有空的，就从序列化的数据中读取
	 */
	private void checkData()
	{
		album = (Album) getIntent().getSerializableExtra("Album");
		judging_jump = getIntent().getStringExtra("Judging_jump");// 判断是主界面跳转过来的，还是pdf界面跳转过来的。

		if (album != null)
		{
			ObjectCacheUtil.init(Album.class).saveObjToCache(this, album);
		} else
		{
			album = (Album) ObjectCacheUtil.init(Album.class).readObjFromCache(this);
		}

		if (MediaUtils.MUSIC_CURRENTPOS == -1)
		{
			SaveIndexDBManager saveDB = SaveIndexDBManager.Builder(this);
			MediaUtils.MUSIC_CURRENTPOS = saveDB.findIndexByMyProId(album.getMyproduct_id());
		}

		DRMUtil.Music_Album_Playing = album;
	}

	private void initView()
	{
		setContentView(R.layout.activity_music_play);
		// 状态栏设置颜色透明。
		UIHelper.showTintStatusBar(this, getResources().getColor(R.color.touming));
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		musicbackground = (RelativeLayout) findViewById(R.id.musicRL);
		// 设置默认背景
		musicbackground.setBackground(getResources().getDrawable(bgId));
		HighlightImageView back = (HighlightImageView) findViewById(R.id.back);// 后退
		TextView close = (TextView) findViewById(R.id.Close);
		TextView musicname = (TextView) findViewById(R.id.focused_tv);// 歌曲名字
		musicname.setText(album != null ? album.getName() : "");
		currTimeTextView = (TextView) findViewById(R.id.currTimeTextView);// 当前播放时间
		totalTimeTextView = (TextView) findViewById(R.id.totalTimeTextView);// 歌曲总时间
		progressSeekBar = (SeekBar) findViewById(R.id.progressSeekBar);// 播放进度条。
		pausebtn = (ImageButton) findViewById(R.id.pausebtn);// 播放prevbtn
		HighlightImageView prevbtn = (HighlightImageView) findViewById(R.id.prevbtn);// 播放上一首
		HighlightImageView nextbtn = (HighlightImageView) findViewById(R.id.nextbtn);// 播放下一首
		prompt = (ImageButton) findViewById(R.id.Prompt);// 播放模式
		HighlightImageView menu = (HighlightImageView) findViewById(R.id.menu);// 右下角菜单按钮
		mViewPager = (ViewPager) findViewById(R.id.viewpager);// fragment模块
		llPointGroup = (LinearLayout) findViewById(R.id.ll_point_group);// 左右滑动的点。
		llPointGroup.removeAllViews();
		waveView = (WaveView) findViewById(R.id.wave_view);
		waveView.setVisibility(View.INVISIBLE);
		miniLyricShow = (TextView) findViewById(R.id.MiniLyricShow);// mini歌词
		TextView tv_number = (TextView) findViewById(R.id.iv_number);// 显示子专辑个数。

		if (mediaUtils.getMediaList() != null && mediaUtils.getMediaList().size() > 0)
		{
			tv_number.setVisibility(View.VISIBLE);
			tv_number.setText((mediaUtils.getMediaList().size() + ""));
		}
		back.setOnClickListener(this);
		close.setOnClickListener(this);
		menu.setOnClickListener(this);
		prompt.setOnClickListener(this);// 播放模式
		pausebtn.setOnClickListener(this);// 播放
		prevbtn.setOnClickListener(this);// 播放上一曲
		nextbtn.setOnClickListener(this); // 下一曲
		// lyrcUtil = new LyrcUtil(this);
		setTabPoint();
		setMusicName(true);
		// 设置背景
		FragmentMusicImg fg = (FragmentMusicImg) adapter.getItem(0);
		ImageUtils.getGaussambiguity(this, album.getPicture(), musicbackground, fg);

		// 监听电话状态, 如果接通了电话, 暂停
		TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void registerBroadcast()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(DRMUtil.BROADCAST_MUSIC_PROGRESS);// 进度条广播
		filter.addAction(DRMUtil.BROADCAST_MUSIC_OBTAIN_TIME);// 第一次进入显示总时间
		filter.addAction(DRMUtil.BROADCAST_MUSIC_STATUSBAR);//
		filter.addAction(DRMUtil.BROADCAST_MUSIC_Close_Music);
		// 音乐状态栏显示
		registerReceiver(receiver, filter);
	}

	private void initState()
	{
		if (MediaUtils.MUSIC_CURRENTPOS == -1 && MediaUtils.playState == ConstantValue.OPTION_STOP)
		{
			startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
		}
		if (MediaUtils.playState != ConstantValue.OPTION_STOP)
		{
			startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
		}
		if (hasPermitted())
		{
			String key = getKey();
			String musicPath = getPath();
			if (MediaUtils.playState != ConstantValue.OPTION_STOP)
			{
				startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
			}
			startMediaService(musicPath, key, ConstantValue.OPTION_PLAY);// 播放

			// notification
			showMusicNotify(getMusicName(), true);
		} else
		{
			releaseResoureWhenHasnotPermit();
		}
		setMusicName(false);// 设置歌曲名称。
	}

	/**
	 * onEventMainThread:如果使用onEventMainThread作为订阅函数，那么不论事件是在哪个线程中发布出来的，
	 * onEventMainThread都会在UI线程中执行
	 * ，接收事件就会在UI线程中运行，这个在Android中是非常有用的，因为在Android中只能在UI线程中跟新UI
	 * ，所以在onEvnetMainThread方法中是不能执行耗时操作的。
	 * 
	 * 名称切换
	 * 
	 * @param event
	 */
	public void onEventMainThread(MusicSwitchNameEvent event)
	{
		DRMLog.e(TAG, "switch music name");

		if (isActivityStart)
		{
			setMusicName(true);// 设置歌曲名称。
		} else
		{
			// 不可见只保存位置
			SaveIndexDBManager sm = SaveIndexDBManager.Builder(this);
			sm.saveDb(MediaUtils.MUSIC_CURRENTPOS, sMyProductID, DrmPat.MUSIC);
		}

		// 切换notification
		showMusicNotify(event.getMusicName(), event.isHasPermitted());
	}

	/**
	 * 状态切换
	 * 
	 * @param event
	 */
	public void onEventMainThread(MusicUpdateStatusEvent event)
	{
		DRMLog.e(TAG, "update music status");

		if (!isActivityStart)
			return;

		pausebtn.setBackgroundResource(R.drawable.music_play);
		initProgress();

		// showMusicNotify();
	}

	private void setTabPoint()
	{
		final int mDotsCount = 2;
		int iconSize = CommonUtil.dip2px(MusicHomeActivity.this, 5f);
		LayoutParams params = new LayoutParams(iconSize, iconSize);
		params.leftMargin = CommonUtil.dip2px(MusicHomeActivity.this, 5f);
		for (int i = 0; i < mDotsCount; i++)
		{
			// 每循环一次, 添加一个点到Linearlayout中
			View dotView = new View(this);
			dotView.setBackgroundResource(R.drawable.point_background);
			dotView.setEnabled(i == 0);
			dotView.setLayoutParams(params);
			llPointGroup.addView(dotView); // 向线性布局中添加"点"
		}
		adapter = new MusicViewPagerAdaper(this.getSupportFragmentManager(), album.getPicture(), mediaUtils.getMediaList().get(MediaUtils.MUSIC_CURRENTPOS)
				.getName());
		mViewPager.setAdapter(adapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			public void onPageSelected(int position)
			{
				for (int i = 0; i < llPointGroup.getChildCount(); i++)
				{
					llPointGroup.getChildAt(i).setEnabled(i == position);
				}
			}

			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
			}

			public void onPageScrollStateChanged(int arg0)
			{
			}
		});

		progressSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				if (!CommonUtil.isFastDoubleClick(600))
				{
					startMediaService(null, null, ConstantValue.OPTION_CHANGE, progressSeekBar.getProgress());
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				currTimeTextView.setText(MediaService.changeTime(progress));
			}
		});
	}

	public void setLRCText(String lrcString)
	{
		miniLyricShow.setText(lrcString);
	}

	private String getMusicPath(int potion)
	{
		String musicID = mediaUtils.getMediaList().get(potion).getContent_id();
		try
		{
			musicID = musicID.substring(1, musicID.length() - 1);
		} catch (IndexOutOfBoundsException e)
		{
		}
		String filePath = DRMUtil.DEFAULT_SAVE_FILE_PATH + File.separator + sMyProductID + File.separator + musicID + DrmPat._MP3;
		return filePath;
	}

	/**
	 * 是否有权限
	 */
	private boolean hasPermitted()
	{
		return mediaUtils.getMediaRight().get(MediaUtils.MUSIC_CURRENTPOS).permitted;
	}

	private String getKey()
	{
		return mediaUtils.getAssetList().get(MediaUtils.MUSIC_CURRENTPOS).getCek_cipher_value();
	}

	private String getPath()
	{
		return getMusicPath(MediaUtils.MUSIC_CURRENTPOS);
	}

	private String getMusicName()
	{
		String musicName = mediaUtils.getMediaList().get(MediaUtils.MUSIC_CURRENTPOS).getName();
		musicName = musicName != null ? musicName : "";
		return musicName.replaceAll("\"", "");
	}

	private void getObtainTime()
	{
		if (hasPermitted())
		{
			// 有播放权限
			startMediaService(getPath(), getKey(), ConstantValue.OBTAIN_TIME);// 播放
		} else
		{
			// 本用户没有在本设备播放该音乐的权限
			ts.showBusy(getApplicationContext(), getString(R.string.play_miss_private_key));
		}
	}

	private void setMusicName(boolean isChange)
	{
		String name = getMusicName();// mediaUtils.getMediaList().get(MediaUtils.MUSIC_CURRENTPOS).getName();
		Fragment fragment = adapter.getItem(0);
		if (fragment instanceof FragmentMusicImg)
		{
			FragmentMusicImg fm = (FragmentMusicImg) fragment;
			// 设置音乐名称
			fm.switchMusicName(name);
		}
		// 用来记录此专辑子专辑的播放的位置。
		if (isChange)
		{
			SaveIndexDBManager sm = SaveIndexDBManager.Builder(this);
			sm.saveDb(MediaUtils.MUSIC_CURRENTPOS, sMyProductID, DrmPat.MUSIC);
		}
	}

	// public static class MusicInfo
	// {
	// public String path;
	// public String title;
	// }

	@Override
	protected void onStart()
	{
		super.onStart();

		isActivityStart = true;
		setMusicName(false);// 设置歌曲名称。

		switch (MediaUtils.playState)
		{
		case ConstantValue.OPTION_STOP:// 之前状态为停止 应该处理播放
			pausebtn.setBackgroundResource(R.drawable.music_play);
			break;
		case ConstantValue.OPTION_PAUSE: // 当前状态是暂停 应该继续播放
			pausebtn.setBackgroundResource(R.drawable.music_play);
			break;
		case ConstantValue.OPTION_PLAY:// 当前状态如果是播放的情况 处理暂停
		case ConstantValue.OPTION_CONTINUE:
			pausebtn.setBackgroundResource(R.drawable.music_pause);
			break;
		}

		switch (MediaUtils.playMode)
		{
		// 顺序播放--随机播放--单曲播放 -- 顺序播放
		case ConstantValue.RANDOM:
			/*** 随机播放 */
			prompt.setBackgroundResource(R.drawable.sequence);// 随机播放
			break;
		case ConstantValue.SINGLE_R:
			/*** 单曲循环 */
			prompt.setBackgroundResource(R.drawable.single);// 单曲
			break;
		case ConstantValue.CIRCLE:
			/*** 列表循环 */
			prompt.setBackgroundResource(R.drawable.shunxu);// 列表循环
			break;
		default:
			break;
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		isActivityStart = false;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		unregisterReceiver(receiver);

		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.back:
		{
			finishUI();
		}
			break;
		case R.id.Close:
		{
			CloseMusic();
		}
			break;
		case R.id.menu:
			openMusicAlbumList();
			break;
		case R.id.Prompt:
			setPlayModel();
			break;
		case R.id.pausebtn:// 播放按钮
			setPausebtn();
			break;
		case R.id.prevbtn:
			setPrevbtn();
			break;
		case R.id.nextbtn:
			setNextbtn();
			break;
		}
	}

	private void CloseMusic() {
		stopMediaService();
		delMusicNotify();
		finishUI();
	}

	/**
	 * 关闭当前activity
	 */
	private void finishUI()
	{
		ActicityManager.getInstance().remove(this);
		finish();
		overridePendingTransition(R.anim.fade_in_scale, R.anim.activity_music_close);
		if (MediaUtils.playState == ConstantValue.OPTION_PLAY || MediaUtils.playState == ConstantValue.OPTION_CONTINUE)
		{
			// 播放或者继续播放状态,显示悬浮窗并开启旋转动画
			FloatViewService.openFloatView(getApplicationContext(), true);
		}

		if (MediaUtils.playState == ConstantValue.OPTION_PAUSE)
		{
			// 暂停,显示悬浮窗，关闭动画
			FloatViewService.openFloatView(getApplicationContext(), false);
		}
	}

	private void setPlayModel()
	{
		if (CommonUtil.isFastDoubleClick(600))
		{
			return;
		}
		switch (MediaUtils.playMode)
		{
		// 顺序播放--随机播放--单曲播放 -- 顺序播放
		case ConstantValue.CIRCLE:
		{
			/*** 随机播放 */
			MediaUtils.playMode = ConstantValue.RANDOM;
			prompt.setBackgroundResource(R.drawable.sequence);// 随机播放
			ts.showOk(getApplicationContext(), getString(R.string.play_random));
		}
			break;
		case ConstantValue.RANDOM:
		{
			/*** 单曲循环 */
			MediaUtils.playMode = ConstantValue.SINGLE_R;
			prompt.setBackgroundResource(R.drawable.single);// 单曲
			ts.showOk(getApplicationContext(), getString(R.string.play_single_cycle));
		}
			break;
		case ConstantValue.SINGLE_R:
		{
			/*** 列表循环 */
			MediaUtils.playMode = ConstantValue.CIRCLE;
			prompt.setBackgroundResource(R.drawable.shunxu);// 列表循环
			ts.showOk(getApplicationContext(), getString(R.string.play_list_cycle));
		}
			break;
		default:
			break;
		}
	}

	// 下一首
	private void setNextbtn()
	{
		if (CommonUtil.isFastDoubleClick(600))
		{
			return;
		}
		if (MediaUtils.MUSIC_CURRENTPOS < mediaUtils.getMediaList().size() - 1)
		{
			MediaUtils.MUSIC_CURRENTPOS++;// 修改位置
		} else
		{
			MediaUtils.MUSIC_CURRENTPOS = 0;
		}
		if (hasPermitted())
		{
			String key = getKey();
			String musicPath = getPath();
			if (MediaUtils.playState != ConstantValue.OPTION_STOP)
			{
				startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
			}
			if (MediaUtils.MUSIC_CURRENTPOS == 1 && MediaUtils.playState == ConstantValue.OPTION_STOP)
			{
				startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
			}
			startMediaService(musicPath, key, ConstantValue.OPTION_PLAY);// 播放
			// pausebtn.setBackgroundResource(R.drawable.music_pause);
		} else
		{
			releaseResoureWhenHasnotPermit();
		}
		setMusicName(true);// 设置歌曲名称。

		showMusicNotify(getMusicName(), hasPermitted());
	}

	// 上一首
	private void setPrevbtn()
	{
		if (CommonUtil.isFastDoubleClick(600))
		{
			return;
		}
		if (MediaUtils.MUSIC_CURRENTPOS >= 1)
		{
			MediaUtils.MUSIC_CURRENTPOS--;// 修改位置
		} else
		{
			MediaUtils.MUSIC_CURRENTPOS = (mediaUtils.getAssetList().size() - 1);
		}
		if (hasPermitted())
		{
			String key = getKey();
			String musicPath = getPath();
			if (MediaUtils.playState != ConstantValue.OPTION_STOP)
			{
				startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
			}
			if (MediaUtils.MUSIC_CURRENTPOS == (mediaUtils.getAssetList().size() - 1) && MediaUtils.playState == ConstantValue.OPTION_STOP)
			{
				startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
			}
			startMediaService(musicPath, key, ConstantValue.OPTION_PLAY);// 播放
			// pausebtn.setBackgroundResource(R.drawable.music_pause);
		} else
		{
			releaseResoureWhenHasnotPermit();
		}
		setMusicName(true);// 设置歌曲名称。

		showMusicNotify(getMusicName(), hasPermitted());
	}

	/**
	 * 无权限，释放资源
	 */
	private void releaseResoureWhenHasnotPermit()
	{
		MediaUtils.playState = ConstantValue.OPTION_STOP;
		startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
		initProgress();
		pausebtn.setBackgroundResource(R.drawable.music_play);
		hideWaveVIew();
		ts.showBusy(getApplicationContext(), getString(R.string.play_miss_private_key));
	}

	// 播放按钮
	private void setPausebtn()
	{
		if (CommonUtil.isFastDoubleClick(600))
		{
			return;
		}
		switch (MediaUtils.playState)
		{
		case ConstantValue.OPTION_STOP:// 之前状态为停止 应该处理播放
		{
			if (hasPermitted())
			{
				// 有播放权限
				String key = getKey();
				String musicPath = getPath();
				startMediaService(musicPath, key, ConstantValue.OPTION_PLAY);// 播放
				showWaveView();

				showMusicNotify(getMusicName(), true);

			} else
			{ // 本用户没有在本设备播放该音乐的权限
				ts.showBusy(getApplicationContext(), getString(R.string.play_miss_private_key));
			}
			pausebtn.setBackgroundResource(R.drawable.music_pause);
		}
			break;
		case ConstantValue.OPTION_PAUSE: // 当前状态是暂停 应该继续播放
		{
			startMediaService(null, null, ConstantValue.OPTION_CONTINUE);
			pausebtn.setBackgroundResource(R.drawable.music_pause);
			showWaveView();

			showMusicNotify(getMusicName(), true);
		}
			break;
		case ConstantValue.OPTION_PLAY:// 当前状态如果是播放的情况 处理暂停
		case ConstantValue.OPTION_CONTINUE:
		{
			startMediaService(null, null, ConstantValue.OPTION_PAUSE);
			pausebtn.setBackgroundResource(R.drawable.music_play);
			hideWaveVIew();

			delMusicNotify();
		}
			break;
		}
	}

	private void showWaveView()
	{
		// 显示waveview
		if (waveView.getVisibility() == View.INVISIBLE)
		{
			waveView.setVisibility(View.VISIBLE);
		}
	}

	private void hideWaveVIew()
	{
		if (waveView.getVisibility() == View.VISIBLE)
		{
			waveView.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 初始显示进度、时间
	 */
	private void initProgress()
	{
		totalTimeTextView.setText("00:00");
		currTimeTextView.setText("00:00");
		progressSeekBar.setMax(0);
		progressSeekBar.setProgress(0);
	}

	private void stopMediaService()
	{
		MediaUtils.MUSIC_CURRENTPOS = -1; // 0
		Intent intent = new Intent(this, MediaService.class);
		intent.putExtra("option", ConstantValue.OPTION_STOP);
		startService(intent);
		DRMUtil.positionOld = -1;
		MediaUtils.current_MusicAlbum_Id = "-1";
	}

	/**
	 * 
	 * @param path
	 * @param key
	 * @param option
	 */
	private void startMediaService(String path, String key, int option)
	{
		if (ConstantValue.OPTION_PLAY == option)
		{
			pausebtn.setBackgroundResource(R.drawable.music_pause);
			waveView.setVisibility(View.VISIBLE);
			MediaUtils.playState = ConstantValue.OPTION_PLAY;

		}
		startMediaService(path, key, option, -1);
	}

	/**
	 * 
	 * @param path
	 * @param key
	 * @param option
	 * @param process
	 */
	private void startMediaService(String path, String key, int option, int process)
	{
		Intent intent = new Intent(getApplicationContext(), MediaService.class);
		intent.putExtra("path", path);
		intent.putExtra("key", key);
		intent.putExtra("option", option);
		intent.putExtra("process", process);
		startService(intent);
	}

	/**
	 * 音乐文件列表
	 */
	private void openMusicAlbumList()
	{
		Bundle b = new Bundle();
		b.putSerializable("Album", album);
		b.putString("Judging_jump", DRMUtil.from_MusicHome);// 标识从音乐主界面跳转
		Intent i = new Intent(this, MusicAlbumActivity.class);
		i.putExtras(b);
		startActivityForResult(i, ALBUM_MUSIC_REQUEST);
		UIHelper.startInAnim(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode)
		{
		case ALBUM_MUSIC_REQUEST:
		{
			ActicityManager.getInstance().remove(MusicHomeActivity.this);
			MusicHomeActivity.this.finish();
		}
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 显示带按钮的通知栏
	 */
	private void showMusicNotify(String musicName, boolean hasPermitted)
	{
		notifyPatManager.updateNotification(musicName, mAlbumImageUrl, hasPermitted);
	}

	private class MyPhoneStateListener extends PhoneStateListener
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch (state)
			{
			case TelephonyManager.CALL_STATE_RINGING: // 震铃
				switch (MediaUtils.playState)
				{
				case ConstantValue.OPTION_PLAY:// 当前状态如果是播放的情况 处理暂停
				case ConstantValue.OPTION_CONTINUE:
					startMediaService(null, null, ConstantValue.OPTION_PAUSE);
					pausebtn.setBackgroundResource(R.drawable.music_play);
					break;
				}
				// DRMLog.d(TAG, "振铃");
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// DRMLog.d(TAG, "通话");
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				// DRMLog.d(TAG, "空闲");
				break;
			}
		}
	}

	/**
	 * 关闭音乐的notification
	 */
	private void delMusicNotify()
	{
		notifyPatManager.cancelNotification();
	}

	@Override
	public void onBackPressed()
	{
		finishUI();
	}

}
