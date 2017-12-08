package cn.com.pyc.drm.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.VideoAlbumListAdapter;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.db.bean.Asset;
import cn.com.pyc.drm.model.right.ContentRight;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.TimeUtil;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.widget.impl.OnBackGestureListener;
import tv.danmaku.ijk.media.widget.DrmFile;

/**
 * 视频列表
 * @author hudq
 */
public class VideoAlbumActivity extends BaseActivity implements OnClickListener
{

	private Album album;
	private String albumId;
	/** 专辑myProId */
	private String myProductId;
	private List<DrmFile> drmFiles;

	private ListView mListView;

	private GestureDetector mGestureDetector;
	private VideoAlbumListAdapter adapter;

	/**
	 * 打开播放
	 * 
	 * @param curPos
	 * @param drmFiles
	 */
	private void openVideoPlayer(int curPos, List<DrmFile> drmFiles)
	{
		Intent intent = new Intent(this, VideoPlayerActivity.class);
		intent.putExtra("curPos", curPos);
		intent.putParcelableArrayListExtra("drmFiles", (ArrayList<? extends Parcelable>) drmFiles);
		startActivity(intent);
		// finish();
		UIHelper.startInAnim(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		init_Value();
		init_View();
		load_Data();
	}

	@Override
	protected void init_View()
	{
		setContentView(R.layout.activity_video_menu_list);
		ActicityManager.getInstance().add(this);
		UIHelper.showTintStatusBar(this, getResources().getColor(R.color.title_bar_bg_color));
		((TextView) findViewById(R.id.title_tv)).setText(getString(R.string.title_video));
		findViewById(R.id.back_img).setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.video_album_listview);

		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
					DrmFile df = drmFiles.get(position);
					String available = df.getValidity();
					// 已经过期
					if ("00天00小时".equals(available)) {
						tos.show(getApplicationContext(), 2, "文件已经过期！");
					} else if(!df.IsEffectivetime){
						tos.show(getApplicationContext(), 2, "对不起，该资料未到生效时间。");
					}else {
						Object obj = mListView.getItemAtPosition(position);
						if (obj instanceof DrmFile)
						{
							DrmFile drmFile = (DrmFile) obj;
							// 保存视频专辑索引
							SaveIndexDBManager.Builder(VideoAlbumActivity.this).saveDb(position, drmFile.getMyProId(), DrmPat.VIDEO);

							adapter.setCurPosition(position);

							openVideoPlayer(position, drmFiles);
						}
				}
			
			}
		});

		mGestureDetector = new GestureDetector(this, new OnBackGestureListener()
		{

			@Override
			public void onFlingRight()
			{
				flingRightBack();
			}
		});

		/**
		 * 此处让listview执行手势事件,避免和activity的OnTouchEvent()冲突
		 */
		mListView.setOnTouchListener(new OnTouchListener()
		{

			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event)
			{
				mGestureDetector.onTouchEvent(event);
				return false;
			}
		});
	}

	@Override
	protected void init_Value()
	{
		drmFiles = getParmasDatas();
		ActicityManager.getInstance().add(this);
	}

	@Override
	protected void load_Data()
	{
		adapter = new VideoAlbumListAdapter(this, drmFiles);
		mListView.setAdapter(adapter);

		int index = SaveIndexDBManager.Builder(this).findIndexByMyProId(myProductId);
		if (index > -1)
		{
			mListView.setSelection(index);
			adapter.setCurPosition(index);
		}
	}

	private List<DrmFile> getParmasDatas()
	{
		album = (Album) getIntent().getSerializableExtra("Album");
		albumId = album.getId();
		myProductId = album.getMyproduct_id();

		List<DrmFile> files = new ArrayList<DrmFile>();
		MediaUtils.getInstance().initMedia(getApplicationContext(), albumId);
		int count = MediaUtils.getInstance().getMediaList().size();
		for (int i = 0; i < count; i++)
		{
			ContentRight mRight = MediaUtils.getInstance().getMediaRight().get(i);
			AlbumContent mAlbum = MediaUtils.getInstance().getMediaList().get(i);
			Asset mAsset = MediaUtils.getInstance().getAssetList().get(i);

			String key = "";
			if (mRight.permitted)
			{
				key = mAsset.getCek_cipher_value();
			}
			String contentId = mAlbum.getContent_id();
			// String path = DRMUtil.DEFAULT_SAVE_FILE_PATH + File.separator +
			// myProductId + File.separator + contentId + DrmPat._MP4;
			String path = new StringBuffer().append(DRMUtil.DEFAULT_SAVE_FILE_PATH).append(File.separator).append(myProductId).append(File.separator)
					.append(contentId).append(DrmPat._MP4).toString();
			path = path.replaceAll("\"", "");
			String validityTime = CommonUtil.getLeftChangeTime(this, mRight.availableTime);
			String odd_datetime_start = TimeUtil.getStringByLongMillsEveryminute(mRight.odd_datetime_start);
			String odd_datetime_end = TimeUtil.getStringByLongMillsEveryminute(mRight.odd_datetime_end);
			String name = mAlbum.getName();
			name = (name != null) ? name.replaceAll("\"", "") : "";
			String asset_id = mAsset.getId();
			DrmFile drmFile = new DrmFile(path, null, key);
			drmFile.setIsEffectivetime(mRight.IsEffectivetime);
			drmFile.setOdd_datetime_start(odd_datetime_start);
			drmFile.setOdd_datetime_end(odd_datetime_end);
			drmFile.setMyProId(myProductId);
			drmFile.setValidity(validityTime);
			drmFile.setTitle(name);
			drmFile.setAsset_id(asset_id);
			files.add(drmFile);
		}
		return files;
	}

	/**
	 * 右滑返回
	 */
	private void flingRightBack()
	{
		setResult(Activity.RESULT_CANCELED, null);
		UIHelper.finishActivity(this);
	}

	public void onBackPressed()
	{
		super.onBackPressed();
		flingRightBack();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		ActicityManager.getInstance().remove(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.back_img:
			flingRightBack();
			break;
		default:
			break;
		}
	}

}
