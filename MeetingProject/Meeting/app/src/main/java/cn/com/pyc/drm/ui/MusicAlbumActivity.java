package cn.com.pyc.drm.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.MusicAlbumListAdapter;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.right.ContentRight;
import cn.com.pyc.drm.service.FloatViewService;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.impl.OnBackGestureListener;

/**
 * 音乐列表
 */
public class MusicAlbumActivity extends BaseActivity
{

	private Album album;
	public static String sMyProductID;
	private String judging_jump;
	private GestureDetector mGestureDetector;
	private ListView mListsListView;

	/**
	 * 标示比较：是否从音乐主界面跳转
	 * 
	 * @param judgStr
	 * @return
	 */
	public static boolean isFromMusicHomeUI(String judgStr)
	{
		return TextUtils.equals(judgStr, DRMUtil.from_MusicHome);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_menu_list);
		ActicityManager.getInstance().add(this);
		// 设置沉浸式状态栏
		UIHelper.showTintStatusBar(this, getResources().getColor(R.color.title_bar_bg_color));
		init_Value();
		init_View();
		load_Data();
		FloatViewService.closeFloatView(this);
	}

	@Override
	protected void init_View()
	{
//		// 音乐列表背景
//		RelativeLayout popupbackground = (RelativeLayout) findViewById(R.id.music_menu_bg);
//		// 设置默认背景
//		popupbackground.setBackground(getResources().getDrawable(R.drawable.music_default_bg));

//		ImageUtils.getGaussambiguity(this, album.getPicture(), popupbackground, null);

		((TextView) findViewById(R.id.title_tv)).setText(getString(R.string.title_music));
		mListsListView = (ListView) findViewById(R.id.music_ablum_listview);
		ImageView iv = (ImageView) findViewById(R.id.back_img);
		HighlightImageView Center_back=(HighlightImageView) findViewById(R.id.iv_music_list_back);
		
		// 判断是主界面跳转过来的，还是音乐主界面跳转过来的。
		if (MuPDFAlbumActivity.isFromMainUI(judging_jump))
		{
			iv.setVisibility(View.VISIBLE);
			Center_back.setVisibility(View.INVISIBLE);
		} else if (isFromMusicHomeUI(judging_jump))
		{
			iv.setVisibility(View.GONE);
			Center_back.setVisibility(View.VISIBLE);
		}
		mListsListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				List<ContentRight> rights = MediaUtils.getInstance().getMediaRight();
				if (rights != null && rights.size() > 0) {
					String available = CommonUtil.getLeftChangeTime(MusicAlbumActivity.this,rights.get(position).availableTime);
					// 已经过期
					if ("00天00小时".equals(available)) {
						tos.show(getApplicationContext(), 2, "文件已经过期！");
					} else if(!rights.get(position).IsEffectivetime){
						tos.show(getApplicationContext(), 2, "对不起，该资料未到生效时间。");
					}else{
						// MediaUtils.MUSIC_CURRENTPOS = position;
						SaveIndexDBManager.Builder(getApplicationContext()).saveDb(position, album.getMyproduct_id(), DrmPat.MUSIC);
						Bundle bundle = new Bundle();
						bundle.putSerializable("Album", album);
						// 判断如果点击的位置和正在播放的位置相同的话，则不再初始化音乐的操作。不传递Judging_jump，则不初始化。（在音乐主界面做了判断。）
						if (MediaUtils.MUSIC_CURRENTPOS != position || MediaUtils.playState == ConstantValue.OPTION_STOP)
						{
							MediaUtils.MUSIC_CURRENTPOS = position;
							bundle.putString("Judging_jump", DRMUtil.from_MusicAlbum);
						}
						// 判断是主界面跳转过来的，还是列表跳转过来的。如果是专辑主界面跳转过来的，则做回掉操作。
						if (isFromMusicHomeUI(judging_jump))
						{
							setResult(Activity.RESULT_OK, null);
						}
						// 打开音乐主页面
						openActivity(MusicHomeActivity.class, bundle);
						finish();
						overridePendingTransition(R.anim.activity_music_open, R.anim.fade_out_scale);
					}
				}
			}
		});
		iv.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				cancelBack();
			}
		});

		mGestureDetector = new GestureDetector(this, new OnBackGestureListener()
		{

			@Override
			public void onFlingRight()
			{
				cancelBack();
			}
		});

		/**
		 * 此处让listview执行手势事件,避免和activity的OnTouchEvent()冲突
		 */
		mListsListView.setOnTouchListener(new OnTouchListener()
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
	protected void load_Data()
	{
		List<AlbumContent> contents = MediaUtils.getInstance().getMediaList();
		List<ContentRight> rights = MediaUtils.getInstance().getMediaRight();

		MusicAlbumListAdapter music_Adapter = new MusicAlbumListAdapter(this, contents, rights);
		mListsListView.setAdapter(music_Adapter);

		mListsListView.setSelection(MediaUtils.MUSIC_CURRENTPOS);
	}

	@Override
	protected void init_Value()
	{
		album = (Album) getIntent().getSerializableExtra("Album");
		judging_jump = getIntent().getStringExtra("Judging_jump");// 判断是主界面跳转过来的，还是list界面跳转过来的。

		MediaUtils.getInstance().initMedia(getApplicationContext(), album.getId());
		sMyProductID = album.getMyproduct_id();

		// 上次播放文件的位置
		MediaUtils.MUSIC_CURRENTPOS = SaveIndexDBManager.Builder(this).findIndexByMyProId(album.getMyproduct_id());
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		ActicityManager.getInstance().remove(this);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		cancelBack();
	}

	// 手势右滑返回：动画
	private void cancelBack()
	{
		finish();
		UIHelper.finishOutAnim(this);
	}

}
