package cn.com.pyc.drm.service;

import java.io.File;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.IBinder;
import android.util.Log;
import cn.com.pyc.drm.bean.event.MusicEndEvent;
import cn.com.pyc.drm.bean.event.MusicSwitchNameEvent;
import cn.com.pyc.drm.bean.event.MusicUpdateStatusEvent;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.ui.MusicHomeActivity;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.ObjectCacheUtil;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.widget.ToastShow;

import com.conowen.libmad.MusicPlayer;

import de.greenrobot.event.EventBus;

/**
 * 媒体声音播放
 * 
 * @author admin
 * 
 */
public class MediaService extends Service implements OnErrorListener, OnCompletionListener, OnSeekCompleteListener
{

	private final String TAG = MediaService.class.getSimpleName();
	private Timer timer;
	private MusicPlayer musicPlayer = null;
	private MediaUtils mediaUtils = MediaUtils.getInstance();
	private ToastShow tos = ToastShow.getInstances_();

	public void onEventMainThread(MusicEndEvent event)
	{
		DRMLog.e(TAG, "playMusicFinish");
		playMusicFinish();
	}

	/**
	 * 只调用一次
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();

		EventBus.getDefault().register(this);
	}

	/**
	 * 每次播放完毕检查数据，如果有空的，就从序列化的数据中读取
	 */
	private void checkData()
	{
		DRMLog.d("check field");
		if (DRMUtil.Music_Album_Playing == null)
		{
			DRMUtil.Music_Album_Playing = (Album) ObjectCacheUtil.init(Album.class).readObjFromCache(this);

			if (MusicHomeActivity.sMyProductID == null)
			{
				MusicHomeActivity.sMyProductID = DRMUtil.Music_Album_Playing.getMyproduct_id();
			}
		}

		if (MediaUtils.MUSIC_CURRENTPOS == -1)
		{
			int index = SaveIndexDBManager.Builder(this).findIndexByMyProId(MusicHomeActivity.sMyProductID);
			MediaUtils.MUSIC_CURRENTPOS = index;
		}
	}

	/**
	 * 当前音乐播放结束
	 */
	private void playMusicFinish()
	{
		if (CommonUtil.isFastDoubleClick(600))
			return;

		checkData();

		switch (MediaUtils.playMode)
		{
		case ConstantValue.SINGLE_R: // 单曲循环
		{
			if (hasPermitted())
			{
				String key = getKey();
				String musicPath = getMusicPath();
				if (MediaUtils.playState != ConstantValue.OPTION_STOP)
				{
					startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
				}
				startMediaService(musicPath, key, ConstantValue.OPTION_PLAY);// 播放
			} else
			{
				// 没有播放权限
				MediaUtils.playState = ConstantValue.OPTION_STOP;
				startMediaService(null, null, ConstantValue.RELEASE);// 释放资源

				tos.showBusy(getApplicationContext(), "没有播放权限");

				EventBus.getDefault().post(new MusicUpdateStatusEvent());
			}
			// 单曲循环，不需要更新notificaiton和musicname
			// Intent intent = new
			// Intent(DRMUtil.BROADCASTRECEIVER_MUSIC_Status_title);
			// sendBroadcast(intent);
		}
			break;
		case ConstantValue.RANDOM: // 随机播放
		{
			MediaUtils.MUSIC_CURRENTPOS = new Random().nextInt(mediaUtils.getMediaList().size()); // [0-size())
			String musicName = getMusicName();
			boolean hasPermitted = false;
			if (hasPermitted())
			{
				hasPermitted = true;
				String key = getKey();
				String musicPath = getMusicPath();
				if (MediaUtils.playState != ConstantValue.OPTION_STOP)
				{
					startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
				}
				startMediaService(musicPath, key, ConstantValue.OPTION_PLAY);// 播放
			} else
			{
				hasPermitted = false;
				MediaUtils.playState = ConstantValue.OPTION_STOP;
				startMediaService(null, null, ConstantValue.RELEASE);// 释放资源

				tos.showBusy(getApplicationContext(), "没有播放权限");

				EventBus.getDefault().post(new MusicUpdateStatusEvent());

//				openCloseRoatate();
			}
			EventBus.getDefault().post(new MusicSwitchNameEvent(musicName, hasPermitted));
		}
			break;
		case ConstantValue.CIRCLE:// 列表循环
		{
			if (mediaUtils.getMediaList() != null)
			{
				if (MediaUtils.MUSIC_CURRENTPOS == mediaUtils.getMediaList().size() - 1)
				{
					MediaUtils.MUSIC_CURRENTPOS = 0;
				} else
				{
					MediaUtils.MUSIC_CURRENTPOS++;
				}
			}
			String musicName = getMusicName();
			boolean hasPermitted = false;
			if (hasPermitted())
			{
				hasPermitted = true;
				String key = getKey();
				String musicPath = getMusicPath();
				if (MediaUtils.playState != ConstantValue.OPTION_STOP)
				{
					startMediaService(null, null, ConstantValue.RELEASE);// 释放资源
				}
				startMediaService(musicPath, key, ConstantValue.OPTION_PLAY);// 播放
			} else
			{
				hasPermitted = false;
				MediaUtils.playState = ConstantValue.OPTION_STOP;
				startMediaService(null, null, ConstantValue.RELEASE);// 释放资源

				tos.showBusy(getApplicationContext(), "没有播放权限");

				EventBus.getDefault().post(new MusicUpdateStatusEvent());

//				openCloseRoatate();
			}
			EventBus.getDefault().post(new MusicSwitchNameEvent(musicName, hasPermitted));
		}
			break;
		}
	}

	/**
	 * 
	 * @param path
	 * @param key
	 * @param option
	 */
	private void startMediaService(String path, String key, int option)
	{
		startMediaService(path, key, option, -1);
	}

	private void startMediaService(String path, String key, int option, int process)
	{
		Intent intent = new Intent(getApplicationContext(), MediaService.class);
		intent.putExtra("path", path);
		intent.putExtra("key", key);
		intent.putExtra("option", option);
		intent.putExtra("process", process);
		startService(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent == null)
			return super.onStartCommand(intent, flags, startId);
		int option = intent.getIntExtra("option", -1);
		switch (option)
		{
		case ConstantValue.OPTION_PLAY:// 播放
		{
			String path = intent.getStringExtra("path");
			String key = intent.getStringExtra("key");
			if (musicPlayer == null)
				musicPlayer = new MusicPlayer(path, key);
			play();
			MediaUtils.playState = ConstantValue.OPTION_PLAY;

//			openStartRoatate();
		}
			break;
		case ConstantValue.OPTION_PAUSE:// 暂停
		{
			if (musicPlayer != null)
			{
				musicPlayer.pause();
			}
			MediaUtils.playState = ConstantValue.OPTION_PAUSE;// 修改当前播放状态

//			openCloseRoatate();
		}
			break;
		case ConstantValue.RELEASE:// 释放资源
		{
			if (musicPlayer != null)
			{
				musicPlayer.release();
				musicPlayer = null;
			}
			if (timer != null)
			{
				timer.cancel();
				timer = null;
			}

//			openCloseRoatate();
		}
			break;
		case ConstantValue.OPTION_CONTINUE:
		{
			if (musicPlayer != null)
			{
				musicPlayer.play();
			}
			MediaUtils.playState = ConstantValue.OPTION_CONTINUE;
//			openStartRoatate();
		}
			break;

		case ConstantValue.OPTION_CHANGE:
		{
			// 拖动进度改变
			int postion = intent.getIntExtra("process", -1);
			if (musicPlayer != null)
			{
				musicPlayer.seekTo(postion);
			}
		}
			break;
		case ConstantValue.OPTION_STOP:
		{
			DRMUtil.Music_Album_Playing = null;

			if (musicPlayer != null)
			{
				musicPlayer.release();
				musicPlayer = null;
			}
			MediaUtils.playState = ConstantValue.OPTION_STOP;
			if (timer != null)
			{
				timer.cancel();
				timer = null;
			}

			closeFloatView();

			stopSelf();
		}
			break;
		case ConstantValue.OBTAIN_TIME:
		{
			String path = intent.getStringExtra("path");
			String key = intent.getStringExtra("key");
			if (musicPlayer == null)
			{
				musicPlayer = new MusicPlayer(path, key);
			}
			int CurrentPosition = musicPlayer.getCurrentPosition();
			int Duration = musicPlayer.getDuration();
			if (CurrentPosition < ((Duration - 1)))
			{
				String action = DRMUtil.BROADCAST_MUSIC_OBTAIN_TIME;
				sendProgressBroadcast(action, CurrentPosition, Duration);
			}
		}
			break;
		default:
			break;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private boolean isDisplay = true;

	private void play()
	{
		try
		{
			musicPlayer.play();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (timer == null)
		{
			timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					if ((MediaUtils.playState == ConstantValue.OPTION_PAUSE))
					{
						return;
					}
					if (musicPlayer != null)
					{
						int CurrentPosition = musicPlayer.getCurrentPosition();
						int Duration = musicPlayer.getDuration();
						if (CurrentPosition < ((Duration - 1)))
						{
							// 发送进度
							String action = DRMUtil.BROADCAST_MUSIC_PROGRESS;
							sendProgressBroadcast(action, CurrentPosition, Duration);

						} else
						{
							// 根据不同的播放模式 切换不同的歌曲
							EventBus.getDefault().post(new MusicEndEvent());
							isDisplay = !isDisplay;
						}
					}
				}
			}, 5, 700);
		}
		// 加载歌词
		// loadLRC();
	}

	private void loadLRC()
	{
		AlbumContent music = MediaUtils.getInstance().getMediaList().get(MediaUtils.MUSIC_CURRENTPOS);
		String path = "/storage/sdcard0/My Heart Will Go On.mp3";
		String name = path.substring(0, path.lastIndexOf("."));
		File lrcFile = new File(name + ".lrc");
		if (lrcFile == null || !lrcFile.exists())
		{
			lrcFile = new File(name + ".txt");
		}
		// MusicActivity01.util.ReadLRC(lrcFile);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		DRMLog.d(TAG + " onDestroy");
		DRMUtil.Music_Album_Playing = null;

		closeFloatView();

		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		Log.i(TAG, "complete");
	}

	// 当音乐资源出现问题
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra)
	{
		tos.showFail(getApplicationContext(), "资源出问题了");
		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp)
	{
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/**
	 * 发送进度通知
	 * 
	 * @param action
	 * @param currentPos
	 * @param duration
	 */
	private void sendProgressBroadcast(String action, int currentPos, int duration)
	{
		Intent intent = new Intent(action);
		intent.putExtra("CurrentPositionInt", currentPos);
		intent.putExtra("DurationInt", duration - 5);
		intent.putExtra("CurrentPositionStr", changeTime(currentPos));
		intent.putExtra("DurationStr", changeTime(duration));
		sendBroadcast(intent);
	}

	private String getMusicName()
	{
		String musicName = mediaUtils.getMediaList().get(MediaUtils.MUSIC_CURRENTPOS).getName();
		musicName = musicName != null ? musicName : "";
		return musicName.replaceAll("\"", "");
	}

	/**
	 * 是否有权限
	 */
	private boolean hasPermitted()
	{
		return mediaUtils.getMediaRight().get(MediaUtils.MUSIC_CURRENTPOS).permitted;
	}

	/**
	 * 秘钥
	 * 
	 * @return
	 */
	private String getKey()
	{
		return mediaUtils.getAssetList().get(MediaUtils.MUSIC_CURRENTPOS).getCek_cipher_value();
	}

	/**
	 * 音乐文件路径
	 * 
	 * @param potion
	 */
	private String getMusicPath()
	{
		// String name = mediaUtils.getMediaList().get(potion).getName();
		String musicId = mediaUtils.getMediaList().get(MediaUtils.MUSIC_CURRENTPOS).getContent_id();
		try
		{
			musicId = musicId.substring(1, musicId.length() - 1);
		} catch (IndexOutOfBoundsException e)
		{
		}
		String filePath = DRMUtil.DEFAULT_SAVE_FILE_PATH + File.separator + MusicHomeActivity.sMyProductID + File.separator + musicId + DrmPat._MP3;
		return filePath;
	}

//	/**
//	 * 打开悬浮图标/关闭旋转动画
//	 * 
//	 * @param context
//	 */
//	private void openCloseRoatate()
//	{
//		FloatViewService.openFloatView(getApplicationContext(), false);
//	}

	/**
	 * 打开并动画旋转
	 * 
	 * @param context
	 */
	private void openStartRoatate()
	{
		FloatViewService.openFloatView(getApplicationContext(), true);
	}

	/**
	 * 关闭悬浮
	 */
	private void closeFloatView()
	{
		FloatViewService.closeFloatView(getApplicationContext());
	}

	/**
	 * 秒--->00:10
	 * 
	 * @param seconds
	 * @return
	 */
	public static String changeTime(int seconds)
	{
		int temp = 0;
		StringBuffer sb = new StringBuffer();
		temp = seconds / 60;
		sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");
		temp = seconds % 60;
		sb.append((temp < 10) ? "0" + temp : "" + temp);
		return sb.toString();
	}

}
