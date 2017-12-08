/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2012 YIXIA.COM
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.widget;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.pragma.DebugLog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import cn.com.pyc.drm.ui.VideoPlayerActivity.MsgHandler;

import static android.R.attr.duration;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting. VideoView also
 * provide many wrapper methods for {@link io.vov.vitamio.MediaPlayer}, such as
 * {@link #getVideoWidth()}, {@link #setSubShown(boolean)}
 */
public class VideoView extends SurfaceView
{

	private String TAG = "VideoView";
	public IjkMediaPlayer mediaPlayer;
	/**
	 * 当前播放的文件是否有权限
	 */
	public boolean hasPrivatePower = false;
	private Context context;
	private DrmFile curPlayFile; // 当前播放文件
	private SPManager spm;
	private Handler mHandler = new Handler();
	private SurfaceHolder mSurfaceHolder;
	private int Current_progress;
	private MsgHandler msgHandler;

	private SurfaceHolder.Callback callback = new SurfaceHolder.Callback()
	{
		/**
		 * 当Surface的状态（大小和格式）发生变化的时候会调用该函数，在surfaceCreated调用后该函数至少会被调用一次
		 * holder:更新surface的SurfaceHolder format:新的图形格式 width:新的宽度 height：新的高度
		 */
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
		{
			DebugLog.d(TAG, "surfaceChanged");
			if (mediaPlayer != null)
			{
				mediaPlayer.setDisplay(holder);
			}
		}

		/**
		 * 当Surface第一次创建后会立即调用该函数。程序可以在该函数中做些和绘制界面相关的初始化工作
		 */
		public void surfaceCreated(SurfaceHolder holder)
		{
			DebugLog.d(TAG, "surfaceCreated");
			if (mediaPlayer == null && curPlayFile != null)
			{
				start(curPlayFile);
			}
		}

		/**
		 * 当Surface被摧毁前会调用该函数，该函数被调用后就不能继续使用Surface了，一般在该函数中来清理使用的资源
		 * holder为所要被删除的surface的SurfaceHolder
		 */
		public void surfaceDestroyed(SurfaceHolder holder)
		{
			DebugLog.d(TAG, "surfaceDestory");
			curPlayFile.setCurPos(Current_progress);
			release();
		}
	};

	/**
	 * 创建一个带有attrs属性的VideoView实例 attrs 用于视图的XML标签属性集合
	 */
	public VideoView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.context = context;
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(callback);
	}

	/**
	 * 当前播放进度
	 *
	 * @return
	 */
	public long getCurrentProgress()
	{
		return Current_progress;
	}

	/**
	 * 文件时长
	 *
	 * @return
	 */
	public long getDuration()
	{
		return duration;
	}

	public void setContext(Context context)
	{
		if (this.context == null)
			this.context = context;
	}

	public boolean isValid()
	{
		return (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid());
	}

	public void setMsgHandler(MsgHandler msgHandler, SPManager spm)
	{
		this.spm = spm;
		this.msgHandler = msgHandler;
	}

	public boolean isPlaying()
	{
		return mediaPlayer != null && mediaPlayer.isPlaying();
	}
	/**
	 * 当前播放进度
	 * 
	 * @return
	 */
	public int getPosition()
	{
		return Current_progress;
	}

	/**
	 * 当前播放的文件DrmFile
	 * 
	 * @return
	 */
	public DrmFile getCurPlayFile()
	{
		return curPlayFile;
	}

	/**
	 * 初始化MediaPlayer
	 */
	public void initPlayer()
	{
		DebugLog.d(TAG, "initPlayer");
		mediaPlayer = new IjkMediaPlayer();

		mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
		mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 12);

		mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
		// mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,
		// "user_agent", mUserAgent);
		mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

		mediaPlayer.setDisplay(getHolder());
		// mediaPlayer.setAvOption(AvFormatOption_HttpDetectRangeSupport.Disable);
		// mediaPlayer.setOverlayFormat(AvFourCC.SDL_FCC_RV32);
		mediaPlayer.setScreenOnWhilePlaying(true);

		mediaPlayer.setOnPreparedListener(preparedListener);
		mediaPlayer.setOnCompletionListener(onCompletionListener);
		mediaPlayer.setOnErrorListener(errorListener);
		// 预加载
		mediaPlayer.setOnInfoListener(mInfoListener);
		// mediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
		// mediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
		// mediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);

		mHandler.post(progressRunnable);
	}

	/**
	 * 开始播放
	 * 
	 * @param drmFile
	 */
	public void start(DrmFile drmFile)
	{
		DebugLog.d(TAG, "start play: " + drmFile.toString());
		curPlayFile = drmFile;

		//DRMLog.e(TAG, "drmfile: " + drmFile.toString());

		// 停止背景音乐等
		// Intent i = new Intent("com.android.music.musicservicecommand");
		// i.putExtra("command", "pause");
		// context.sendBroadcast(i);

		release();
		if (curPlayFile != null)
		{
			if (TextUtils.isEmpty(curPlayFile.getPrivateKey()))
			{
				// 没有权限
				hasPrivatePower = false;
				sendMsg(MsgHandler.STATE_NO_PLAY_PERMISSION, 0, 0, curPlayFile);
				return;
			}
			hasPrivatePower = true;
		}
		try
		{
			initPlayer();
			String filepath=curPlayFile.getFilePath();
			String privatekey=curPlayFile.getPrivateKey();
			mediaPlayer.setDataSource(filepath, privatekey);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		mediaPlayer.seekTo(Current_progress);
		mediaPlayer.prepareAsync();
	}

	/**
	 * 开始或暂停
	 */
	public void startOrPause()
	{
		if (mediaPlayer == null)
			return;
		if (mediaPlayer.isPlaying())
		{
			mediaPlayer.pause();
			sendMsg(MsgHandler.STATE_PLAY_PAUSE, MsgHandler.NEGATIVE, 0, null);
		} else
		{
			mediaPlayer.start();
			sendMsg(MsgHandler.STATE_PLAY_PAUSE, MsgHandler.POSITIVE, 0, null);
		}
	}
	/**
	 * 开始或暂停
	 */
	public void start()
	{
		if (mediaPlayer == null)
			return;
		
			mediaPlayer.start();
			sendMsg(MsgHandler.STATE_PLAY_PAUSE, MsgHandler.POSITIVE, 0, null);
	}
	/**
	 * 暂停
	 */
	public void pause()
	{
		if (mediaPlayer == null)
			return;
		mediaPlayer.pause();
		// sendMsg(MsgHandler.STATE_PLAY_PAUSE,MsgHandler.NEGATIVE, 0, null);
	}

	/**
	 * 指定播放位置
	 * 
	 * @param pos
	 */
	public void seek(int pos)
	{
		if (mediaPlayer != null)
		{
			mediaPlayer.seekTo(pos);
		}
	}

	/**
	 * 释放资源
	 */
	private void release()
	{
		DebugLog.d(TAG, "release");
		mHandler.removeCallbacks(progressRunnable);
		if (mediaPlayer != null)
		{
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	private OnInfoListener mInfoListener = new OnInfoListener()
	{
		@Override
		public boolean onInfo(IMediaPlayer mp, int what, int extra)
		{
			switch (what)
			{
			case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
				sendMsg(MsgHandler.STATE_BUFFERING, MsgHandler.POSITIVE, 0, null);
				break;

			case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
				sendMsg(MsgHandler.STATE_BUFFERING, MsgHandler.NEGATIVE, 0, null);
				break;

			default:
				break;
			}
			return false;
		}
	};

	private OnPreparedListener preparedListener = new OnPreparedListener()
	{
		@Override
		public void onPrepared(IMediaPlayer mp)
		{
			DebugLog.d(TAG, "onPrepared");
			curPlayFile.setDuration((int) mp.getDuration());
			sendMsg(MsgHandler.STATE_ANOTHER_PLAY, 0, 0, curPlayFile);
			if (mediaPlayer != null)
			{
				// 当前播放进度
				Current_progress = 0;
				sendMsg(MsgHandler.STATE_PROGRESS, (int) Current_progress, 0, null);
				mediaPlayer.seekTo(Current_progress);
			}
		}
	};

	/**
	 * 在设置或播放过程中发生错误时调用的回调函数。
	 * */
	private OnErrorListener errorListener = new OnErrorListener()
	{
		@Override
		public boolean onError(IMediaPlayer mp, int what, int extra)
		{
			release();
			sendMsg(MsgHandler.STATE_ERROR, 0, 0, curPlayFile.getTitle());
			return false;
		}
	};

	// private OnVideoSizeChangedListener mSizeChangedListener = new
	// OnVideoSizeChangedListener() {
	// @Override
	// public void onVideoSizeChanged(IMediaPlayer mp, int width, int height,
	// int sarNum, int sarDen) {
	// DebugLog.e(TAG, "onVideoSizeChanged");
	// int mVideoWidth = mp.getVideoWidth();
	// int mVideoHeight = mp.getVideoHeight();
	//
	// DRMLog.e(TAG, "width = " + width);
	// DRMLog.e(TAG, "height = " + height);
	// DRMLog.e(TAG, "mVideoWidth = " + mVideoWidth);
	// mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
	// }
	// };

	private OnCompletionListener onCompletionListener;

	/**
	 * 注册在媒体文件加载完毕，可以播放时调用的回调函数
	 */
	public void setOnCompleteListener(OnCompletionListener onCompletionListener)
	{
		this.onCompletionListener = onCompletionListener;
	}

	private Runnable progressRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Current_progress = (int) mediaPlayer.getCurrentPosition();
			if (Current_progress > mediaPlayer.getDuration() && onCompletionListener != null)
			{
				// 如果是有问题的视频，则可能会出现此情况
				onCompletionListener.onCompletion(mediaPlayer);
			}
			sendMsg(MsgHandler.STATE_PROGRESS, (int) Current_progress, 0, null);
			final int playState = mediaPlayer.isPlaying() ? MsgHandler.POSITIVE : MsgHandler.NEGATIVE;
			sendMsg(MsgHandler.STATE_PLAY_PAUSE, playState, 0, null);
			mHandler.postDelayed(progressRunnable, 1000);
		}
	};

	/**
	 * msgHandler sendMsg
	 * 
	 * @param what
	 * @param arg1
	 * @param arg2
	 * @param obj
	 */

	private void sendMsg(int what, int arg1, int arg2, Object obj)
	{
		Message msg = Message.obtain();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		msg.obj = obj;
		msgHandler.sendMessage(msg);
	}
}
