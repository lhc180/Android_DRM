/*
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.com.pyc.drm.ui;

import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.widget.DrmFile;
import tv.danmaku.ijk.media.widget.MediaController;
import tv.danmaku.ijk.media.widget.SPManager;
import tv.danmaku.ijk.media.widget.Util;
import tv.danmaku.ijk.media.widget.Util.AnimationUtil;
import tv.danmaku.ijk.media.widget.Util.AnimationUtil.Location;
import tv.danmaku.ijk.media.widget.Util.ViewUtil;
import tv.danmaku.ijk.media.widget.VideoView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.VideoListAdapter;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.MarqueeTextView;
import cn.com.pyc.drm.widget.ToastShow;

/**
 * 视频播放
 * 
 */
public class VideoPlayerActivity extends Activity
{

	private static final String TAG = VideoPlayerActivity.class.getSimpleName();
	private static final int CONTROL_DURATION = 6000;
	private MediaController mController;
	private IjkMediaPlayer mMediaPlayer;
	private VideoView mVideoView;
	/** DRMFile */
	private DrmFile curPlay;
	/** List DrmFile */
	private List<DrmFile> mDrmFiles;
	private Handler selfHandler = new Handler();// 本类自己使用

	private TextView g_txtCount;
	private TextView g_txtTitle;
	private TextView g_txtCurPos;
	private TextView g_txtDuration;
	private SeekBar g_skbProgress;
	private HighlightImageView g_imbInfo;
	private View g_lytControlTop;
	private View g_lytControlBottom;
	private ImageButton g_imbPlayPause;
	private View g_lytList;
	private ProgressBar g_pbBuffering;

	private SPManager spm;
	private PopupWindow pwInfo;
	private ToastShow ts = ToastShow.getInstances_();

	private boolean needInit = true;
	private VideoListAdapter videoAdapter = null;

	
	private HighlightImageView pervious;
	private HighlightImageView next;

	// handler Msg
	public class MsgHandler extends Handler
	{
		// 这两个只不能设为0（arg1默认是0）
		public static final int POSITIVE = 1; // 播放、显示buffer
		public static final int NEGATIVE = -1; // 暂停、隐藏buffer

		public static final int STATE_ERROR = -1;
		public static final int STATE_PLAY_PAUSE = 0;
		public static final int STATE_PROGRESS = 1; // 当前进度
		public static final int STATE_ANOTHER_PLAY = 2; // 切换视频
		public static final int STATE_BUFFERING = 3;
		public static final int STATE_NO_PLAY_PERMISSION = 4;// 没有播放权限　
		private long duration;

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case STATE_ERROR:
				ts.show(getApplicationContext(), ToastShow.IMG_FAIL, "亲，视频播放出现问题了...");
				break;
			case STATE_PLAY_PAUSE:
				// 暂停/开始按钮更新
				g_imbPlayPause.setBackgroundResource(msg.arg1 == POSITIVE ? R.drawable.video_pause : R.drawable.video_play);
				break;
			case STATE_PROGRESS:
			{
				// 更新进度
				if (msg.arg1 <= duration)
				{
					g_txtCurPos.setText(Util.formateTime(msg.arg1));
					g_skbProgress.setProgress(msg.arg1);
				} else
				{
					g_txtCurPos.setText(Util.formateTime(duration));
					g_skbProgress.setProgress((int) duration);
				}
				if (msg.arg1 <= duration)
				{
					g_txtCurPos.setText(Util.formateTime(msg.arg1));
					g_skbProgress.setProgress(msg.arg1);
				} else
				{
					g_txtCurPos.setText(Util.formateTime(duration));
					g_txtCurPos.setText(Util.formateTime(duration));
				}
			}
				break;
			case STATE_ANOTHER_PLAY:
			{
				// 自动播放
				curPlay = (DrmFile) msg.obj;
				duration = curPlay.getDuration();
				duration = curPlay.getDuration();
				g_txtTitle.setText(curPlay.getTitle());
				g_txtDuration.setText(Util.formateTime(duration));
				g_skbProgress.setMax(curPlay.getDuration());
				g_txtDuration.setText(Util.formateTime(duration));
				g_skbProgress.setMax((int) duration);
			}
				break;
			case STATE_BUFFERING:
			{
				if (msg.arg1 == POSITIVE)
				{
					// 延迟显示，要不一点击进度条就闪现buffer
					selfHandler.postDelayed(bufferRunnable, 300);
				}
				if (msg.arg1 == NEGATIVE)
				{
					// 如果300ms内加载完毕，就取消buffer的显示
					selfHandler.removeCallbacks(bufferRunnable);
					ViewUtil.gone(g_pbBuffering);
				}
			}
				break;
			case STATE_NO_PLAY_PERMISSION:
			{
				curPlay = (DrmFile) msg.obj;
				duration = curPlay.getDuration();

				g_txtTitle.setText(curPlay.getTitle());
				g_txtCurPos.setText(Util.formateTime(0));
				// g_txtDuration.setText(formateTime(duration));
				g_skbProgress.setMax(curPlay.getDuration());
				g_txtDuration.setText(Util.formateTime(duration));
				g_skbProgress.setMax((int) duration);
				ts.show(getApplicationContext(), ToastShow.IMG_BUSY, "没有播放权限~");
				showList(true);
			}
				break;
			default:
				break;
			}
		}
	}

	// 电话状态监听器
	private class MyPhoneStateListener extends PhoneStateListener
	{
		boolean flag = false; // flag表示 是否因为来电引起了视频中断

		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			super.onCallStateChanged(state, incomingNumber);
			if(mMediaPlayer==null){
				return;
				}
			switch (state)
			{
			case TelephonyManager.CALL_STATE_RINGING: // 来电
				mController.Pause();
//				if (mMediaPlayer.isPlaying())
//				{
					mMediaPlayer.pause();
					flag = true; // 来电引起了视频暂停播放，设置标识flag
//				}
				break;
			case TelephonyManager.CALL_STATE_IDLE: // call state 是空闲状态
//				if (flag && !mMediaPlayer.isPlaying())
//				{
					mController.starts();
//					mMediaPlayer.pause();
//					mMediaPlayer.start(); // 恢复播放
//					flag = false;
//				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: // 电话已经接通
				break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getWindow() != null)
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
			// 让视频在播放期间系统不会自动休眠
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		// init library loader
		IjkMediaPlayer.loadLibrariesOnce(null);
		IjkMediaPlayer.native_profileBegin("libijkplayer.so");

		Intent intent = getIntent();
		// 当前播放文件的位置
		int mCurrentPos = intent.getIntExtra("curPos", 0);
		// 获取列表
		mDrmFiles = intent.getParcelableArrayListExtra("drmFiles");

		if (mDrmFiles == null)
		{
			ts.showFail(getApplicationContext(), "出错了...");
			finish();
			return;
		}
		setContentView(R.layout.activity_video_player);
		findViewAndSetListeners();
		if(mDrmFiles.size()==1){
			pervious.setVisibility(View.GONE);
			next.setVisibility(View.GONE);
		}
		// 设置播放器
		setPlayer(mCurrentPos);
		// 监听电话状态，来电，暂停视频播放,通话结束，视频恢复播放前状态
		TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void setPlayer(int mCurrentPos)
	{
		// 存储播放位置时长的preference
		spm = new SPManager(this);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mVideoView.setContext(this);
		mVideoView.requestFocus();
		mVideoView.setMsgHandler(new MsgHandler(), spm);
		mMediaPlayer = mVideoView.mediaPlayer;
		// 从记录的startPos开始播放，没有就是0
		// 从唯一myProId查询存储的专辑索引位置
		// int index =
		// SaveIndexDBManager.getInstance(this).findIndexByMyProId(myProductId);
		// int startPos = (index == -1) ? 0 : index;
		DRMLog.e(TAG, "播放第" + (mCurrentPos + 1) + "个视频文件");
		mController = new MediaController(this, mVideoView, mDrmFiles, mCurrentPos, MediaController.MODE_SINGLE);
		showControl(true);

		if (!mDrmFiles.isEmpty())
		{
			g_txtCount.setVisibility(View.VISIBLE);
			g_txtCount.setText(String.valueOf(mDrmFiles.size()));
		}
	}

	private void findViewAndSetListeners()
	{
		// 返回
		findViewById(R.id.amc_imb_back).setOnClickListener(dispatchClickListener);
		findViewById(R.id.amc_iv_back).setOnClickListener(dispatchClickListener);
		// 标题
		g_txtTitle = (TextView) findViewById(R.id.amc_txt_title);

		pervious = (HighlightImageView) findViewById(R.id.amc_imb_previous);
		pervious.setOnClickListener(dispatchClickListener);
		g_imbPlayPause = (ImageButton) findViewById(R.id.amc_imb_start_pause);
		g_imbPlayPause.setOnClickListener(dispatchClickListener);
		next = (HighlightImageView) findViewById(R.id.amc_imb_next);
		next.setOnClickListener(dispatchClickListener);

		// 时间
		g_txtCurPos = (TextView) findViewById(R.id.amc_txt_current);
		g_txtDuration = (TextView) findViewById(R.id.amc_txt_duration);

		// 进度条
		g_skbProgress = ((SeekBar) findViewById(R.id.amc_skb_progress));
		g_skbProgress.setOnSeekBarChangeListener(progressChangedListener);

		// 视频信息
		g_imbInfo = (HighlightImageView) findViewById(R.id.amc_imb_info);
		g_imbInfo.setOnClickListener(dispatchClickListener);

		// 列表文件数
		g_txtCount = (TextView) findViewById(R.id.amc_text_count);

		// 列表信息
		findViewById(R.id.amc_imb_list).setOnClickListener(dispatchClickListener);
		g_lytList = findViewById(R.id.amc_lyt_list);
		g_lytList.setOnClickListener(dispatchClickListener);
		// 控制界面
		// g_lytControl = findViewById(R.id.avp_lyt_control);
		g_lytControlTop = findViewById(R.id.amc_lyt_top);
		g_lytControlBottom = findViewById(R.id.amc_lyt_bottom);
		// 缓冲
		g_pbBuffering = (ProgressBar) findViewById(R.id.avp_pb_buffing);

		// videoview
		mVideoView = (VideoView) findViewById(R.id.avp_lyt_videoview);
	}

	private OnClickListener dispatchClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			int id = v.getId();
			if (id != R.id.amc_imb_back) // 点击activity_media_control的非amc_imb_back，
											// Control界面存在
			{
				showControl(true);
			}
			if (id != R.id.amc_imb_info) // 点击activity_media_control的非amc_imb_info，视频信息的popWindow消失
			{
				showInfo(false);
			}
			switch (id)
			{
			case R.id.amc_imb_back: // 返回 视频退出
			{
				if (mMediaPlayer != null && mMediaPlayer.isPlaying())
				{
					DRMLog.d(TAG, "mMediaPlayer stop playing");
					mMediaPlayer.stop();
				}
				finish();
				showList(false);
			}
				break;

			case R.id.amc_imb_info: // 显示视频信息
				showInfo();
				break;

			case R.id.amc_imb_list: // 显示视频列表
				showList();
				break;

			case R.id.amc_iv_back: // 视频列表信息消失
				showList(false);
				break;
			default:
				mController.control(v, VideoPlayerActivity.this);
				break;
			}

			if (id != R.id.amc_imb_list && id != R.id.amc_lyt_list)
			{
				// 点击非列表按钮和列表view,有权限，列表消失。否则显示
				// 点击amc_lyt_list，视频列表仍会存在
				if (mVideoView.hasPrivatePower)
					showList(false);
			}
		}
	};

	private OnSeekBarChangeListener progressChangedListener = new OnSeekBarChangeListener()
	{
		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			mController.seek(g_skbProgress.getProgress());
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			g_txtCurPos.setText(Util.formateTime(progress));
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			showInfo(false);
			if (mVideoView.hasPrivatePower)
				showList(false);
			if (isClickControllView(event))
			{
				showControl(true);
			} else
			{
				showControl();
			}
		}
		return super.onTouchEvent(event);
	}

	private boolean isClickControllView(MotionEvent event)
	{
		final int contollHeight = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
		final int y = (int) event.getY();
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		final int screenY = dm.heightPixels;
		return y < contollHeight || (screenY - y) < contollHeight;
	}

	/**
	 * 视频信息
	 */
	public void showInfo()
	{
		if (pwInfo == null)
		{
			showInfo(true);
		} else
		{
			showInfo(!pwInfo.isShowing());
		}
	}

	public void showInfo(boolean show)
	{
		if (curPlay == null)
		{
			ts.showBusy(getApplicationContext(), "视频信息为空~");
			return; // 视频还没加载完
		}
		// 将视频信息popwindow暂时隐藏
		if (show)
		{
			// 下一曲时信息会变，每次都要重新加载
			View infoView = getLayoutInflater().inflate(R.layout.dialog_video_info, null);
			DrmFile drmFile = curPlay;
			// String author = (!TextUtils.isEmpty(album.getAuthor())) ? album
			// .getAuthor().replace(";", "") : "DRM";
			// ((MarqueeTextView) infoView.findViewById(R.id.dvi_txt_1))
			// .setText(author);
			((MarqueeTextView) infoView.findViewById(R.id.dvi_txt_2)).setText(drmFile.getFormat());
			((MarqueeTextView) infoView.findViewById(R.id.dvi_txt_3)).setText(Util.toTime(drmFile.getDuration()));
			if (pwInfo == null)
			{
				pwInfo = new PopupWindow(infoView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				pwInfo.setAnimationStyle(R.style.PopupWindow_info_anim);
			}
			pwInfo.showAtLocation(g_imbInfo, Gravity.BOTTOM | Gravity.RIGHT, (g_imbInfo.getTop() + (int) (g_imbInfo.getWidth() * 1.9)),
					(g_imbInfo.getBottom() + 10));
		} else
		{
			if (pwInfo != null)
			{
				pwInfo.dismiss();
				pwInfo = null;
			}
		}
	}

	/**
	 * 列表信息
	 */
	public void showList()
	{
		if (!needInit)
		{
			showList(!ViewUtil.isShown(g_lytList));
		} else
		{
			showList(true);
		}
	}

	public void showList(boolean show)
	{
		if (null == mDrmFiles)
			return;

		if (show)
		{
			if (needInit)
			{
				ListView lv = (ListView) findViewById(R.id.amc_lv_list);
				videoAdapter = new VideoListAdapter(this, mDrmFiles);
				lv.setAdapter(videoAdapter);
				lv.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						DrmFile drmFile = mVideoView.getCurPlayFile();
						spm.putInt(drmFile.getAsset_id(), mVideoView.getPosition());

						if (!TextUtils.isEmpty(drmFile.getPrivateKey()))
						{
							// 有权限就消失列表
							showList(false);
						}
						showControl(true); // 重新计时
						// 保存视频专辑索引
						SaveIndexDBManager.Builder(VideoPlayerActivity.this).saveDb(position, drmFile.getMyProId(), DrmPat.VIDEO);
						mController.start(position);
					}
				});
				lv.setOnScrollListener(new OnScrollListener()
				{
					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState)
					{
						showControl(true); // 重新计时
					}

					@Override
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
					{
					}
				});
				needInit = false;
			}
			videoAdapter.setCurPosition(mDrmFiles.indexOf(curPlay));
			if (!ViewUtil.isShown(g_lytList))
			{
				Animation ta = AnimationUtil.translate(g_lytList, false, show, Location.Right);
				Animation aa = AnimationUtil.alpha(g_lytList, false, show);
				AnimationUtil.group(g_lytList, show, ta, aa);
			}
		} else
		{
			if (ViewUtil.isShown(g_lytList))
			{
				Animation ta = AnimationUtil.translate(g_lytList, false, show, Location.Right);
				Animation aa = AnimationUtil.alpha(g_lytList, false, show);
				AnimationUtil.group(g_lytList, show, ta, aa);
			}
		}
	}

	/**
	 * control view
	 */
	private void showControl()
	{
		showControl(!ViewUtil.isShown(g_lytControlTop));
	}

	private void showControl(boolean show)
	{
		selfHandler.removeCallbacks(controlShowRunnable);
		if (show)
		{
			if (!ViewUtil.isShown(g_lytControlTop))
			{
				AnimationUtil.translate(g_lytControlTop, true, show, Location.Top);
				AnimationUtil.translate(g_lytControlBottom, true, show, Location.Bottom);
			}
			selfHandler.postDelayed(controlShowRunnable, CONTROL_DURATION);
		} else
		{
			if (ViewUtil.isShown(g_lytControlTop))
			{
				// //这里不能直接gone，否则没有动画
				// 但如果不Gone，则其所在位置仍有焦点，所以放在AnimationListener中执行Gone操作
				// ViewUtil.gone(g_lytControlTop);
				AnimationUtil.translate(g_lytControlTop, true, show, Location.Top);
				AnimationUtil.translate(g_lytControlBottom, true, show, Location.Bottom);
			}
			showInfo(false);
			showList(false);
		}
	}

	private final Runnable controlShowRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			showControl(false);
		}
	};

	private final Runnable bufferRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			ViewUtil.visible(g_pbBuffering);
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		DRMLog.e(TAG, "onConfigurationChanged");
		// 暂停
		// mMediaPlayer.pause();
		if (mVideoView != null)
			mVideoView.pause();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if (curPlay != null)
		{
			spm.putInt(curPlay.getAsset_id(), curPlay.getCurPos());
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (pwInfo != null && pwInfo.isShowing())
		{
			pwInfo.dismiss();
			pwInfo = null;
		}
		if (mMediaPlayer != null)
		{
			if (mMediaPlayer.isPlaying())
			{
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
		}
		selfHandler.removeMessages(MsgHandler.STATE_ERROR);
		selfHandler.removeMessages(MsgHandler.STATE_ANOTHER_PLAY);
		selfHandler.removeMessages(MsgHandler.STATE_BUFFERING);
		selfHandler.removeMessages(MsgHandler.STATE_NO_PLAY_PERMISSION);
		selfHandler.removeMessages(MsgHandler.STATE_PLAY_PAUSE);
		selfHandler.removeMessages(MsgHandler.STATE_PROGRESS);
		IjkMediaPlayer.native_profileEnd();
	}
}
