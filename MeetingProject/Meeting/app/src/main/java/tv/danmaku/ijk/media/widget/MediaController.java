/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2012 YIXIA.COM
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
 *
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

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import android.app.Activity;
import android.content.Context;
import android.view.View;

/**
 * A view containing controls for a MediaPlayer. Typically contains the buttons
 * like "Play/Pause" and a progress slider. It takes care of synchronizing the
 * controls with the state of the MediaPlayer.
 * <p>
 * The way to use this class is to a) instantiate it programatically or b)
 * create it in your xml layout. a) The MediaController will create a default
 * set of controls and put them in a window floating above your application.
 * Specifically, the controls will float above the view specified with
 * setAnchorView(). By default, the window will disappear if left idle for three
 * seconds and reappear when the user touches the anchor view. To customize the
 * MediaController's style, layout and controls you should extend
 * MediaController and override the {#link {@link #makeControllerView()} method.
 * b) The MediaController is a FrameLayout, you can put it in your layout xml
 * and get it through {@link #findViewById(int)}. NOTES: In each way, if you
 * want customize the MediaController, the SeekBar's id must be
 * mediacontroller_progress, the Play/Pause's must be mediacontroller_pause,
 * current time's must be mediacontroller_time_current, total time's must be
 * mediacontroller_time_total, file name's must be mediacontroller_file_name.
 * And your resources must have a pause_button drawable and a play_button
 * drawable.
 * <p>
 * Functions like show() and hide() have no effect when MediaController is
 * created in an xml layout.
 */
public class MediaController
{
	private VideoView videoView;
	private int curOne;
	// private SharedPreferences sp;
	private SPManager spm;
	private List<DrmFile> drmFiles;

	private int curMode;
	public static final int MODE_SINGLE = 0; // 单个循环
	public static final int MODE_CYCLE = 1; // 列表循环
	public static final int MODE_LIST = 2; // 顺序播放
	public static final int MODE_RANDOM = 3; // 随机播放

	public MediaController(Context context, VideoView videoView, List<DrmFile> drmFiles, int curOne, int curMode)
	{
		spm = new SPManager(context);

		this.videoView = videoView;
		this.drmFiles = drmFiles;
		this.curOne = curOne;
		this.curMode = curMode;
		setListeners(videoView);

		start();
	}

	public void setMode(int mode)
	{
		curMode = mode;
	}

	private void setListeners(VideoView vv)
	{
		vv.setOnCompleteListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(IMediaPlayer mp)
			{
				spm.putInt(drmFiles.get(curOne).getAsset_id(), 0);

				seek(0);// 默认单个视频循环
				videoView.mediaPlayer.pause();
			}
		});
	}

	public void control(View v, Activity atv)
	{
		switch (v.getId())
		{
		case R.id.amc_imb_next:
		{
			if (!CommonUtil.isFastDoubleClick(600))
			{
				spm.putInt(drmFiles.get(curOne).getAsset_id(), videoView.getPosition());

				next();
				// 保存视频专辑索引
				SaveIndexDBManager.Builder(atv).saveDb(curOne, drmFiles.get(curOne).getMyProId(), DrmPat.VIDEO);
			}
		}
			break;

		case R.id.amc_imb_start_pause:
			startOrPause();
			break;

		case R.id.amc_imb_previous:
		{
			if (!CommonUtil.isFastDoubleClick(600))
			{
				spm.putInt(drmFiles.get(curOne).getAsset_id(), videoView.getPosition());
				previous();
				// 保存视频专辑索引
				SaveIndexDBManager.Builder(atv).saveDb(curOne, drmFiles.get(curOne).getMyProId(), DrmPat.VIDEO);
			}
		}
			break;

		default:
			break;
		}
	}

	public void seek(int pos)
	{
		videoView.seek(pos);
	}

	public void start(int pos)
	{
		curOne = pos;
		start();
	}

	public void next()
	{
		curOne = curOne + 1 < drmFiles.size() ? curOne + 1 : 0;
		start();
	}

	public void previous()
	{
		curOne = curOne - 1 < 0 ? drmFiles.size() - 1 : curOne - 1;
		start();
	}

	public void start()
	{
		videoView.start(drmFiles.get(curOne));
	}

	private void startOrPause()
	{
		videoView.startOrPause();
	}
	
	public void Pause()
	{
		videoView.pause();
	}
	public void starts()
	{
		videoView.start();
	}

}
