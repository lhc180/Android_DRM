package cn.com.pyc.drm.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.adapter.Mupdf_AlbumList_Adapter;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.db.practice.AlbumDAOImpl;
import cn.com.pyc.drm.model.right.ContentRight;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.impl.OnBackGestureListener;

/**
 * 图书列表
 */
public class MuPDFAlbumActivity extends BaseActivity implements OnClickListener {

	private Album album;
	private GestureDetector mGestureDetector;
	private String judging_jump;
	private ListView mListView;
	private Mupdf_AlbumList_Adapter adapter;
	private List<AlbumContent> contents;

	/**
	 * 标示比较：是否从主界面跳转
	 * 
	 * @param judgStr
	 * @return
	 */
	public static boolean isFromMainUI(String judgStr) {
		return TextUtils.equals(judgStr, DRMUtil.from_DownloadMain);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.print("1");
		System.out.print("2");
		System.out.print("3");
		setContentView(R.layout.activity_pdf_menu_list);
		ActicityManager.getInstance().add(this);
		init_Value();
		init_View();
		load_Data();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		int mCurrentOrientation = getResources().getConfiguration().orientation;
		if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
//			// 竖屏
//			ReaderView.mScale = MuPDFActivity.sScaleVertical;
//			ReaderView.HORIZONTAL_SCROLLING = true;

		} else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			// 横屏
//			ReaderView.mScale = MuPDFActivity.sScaleHorizontal;
//			ReaderView.HORIZONTAL_SCROLLING = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (adapter != null) {
			List<AlbumContent> contents = MediaUtils.getInstance()
					.getPdfmediaList();
			if (contents == null || contents.isEmpty())
				return;
			adapter.setCurrentPosition(MediaUtils.MPDF_CURRENTPOS);
			adapter.setAlbumContentList(contents);
			adapter.notifyDataSetChanged();
		}

		// ReaderView.mScale = MuPDFActivity.sScaleVertical;
		// ReaderView.HORIZONTAL_SCROLLING = true;
	}

	protected void init_View() {
		TextView tvTitle = ((TextView) findViewById(R.id.title_tv));
		findViewById(R.id.back_img).setOnClickListener(this);
//		boolean isScaning = (Constant.LoginConfig.type == DrmPat.LOGIN_SCANING);
		tvTitle.setText(album.getName());

		mListView = (ListView) findViewById(R.id.pdf_album_listview);
		HighlightImageView imgBack = (HighlightImageView) findViewById(R.id.iv_back);
		// 判断是主界面跳转过来的，还是pdf界面跳转过来的。
		if (isFromMainUI(judging_jump)) {
			imgBack.setVisibility(View.GONE);
		} else if (TextUtils.equals(judging_jump, DRMUtil.from_MuPDFActivity)) {
			// 全屏
			CommonUtil.fullScreen(true, this);
			imgBack.setVisibility(View.VISIBLE);
		}
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				List<ContentRight> rights = MediaUtils.getInstance()
						.getPdfmediaRight();
				if (rights != null && rights.size() > 0) {
					String available = CommonUtil.getLeftChangeTime(
							MuPDFAlbumActivity.this,
							rights.get(position).availableTime);
					// 已经过期
					if ("00天00小时".equals(available)) {
						tos.show(getApplicationContext(), 2, "对不起,该文件已过期。");
					} else if(!rights.get(position).IsEffectivetime){
						tos.show(getApplicationContext(), 2, "对不起，该资料未到生效时间。");
					}else{
						MediaUtils.MPDF_CURRENTPOS = position;
						// 保存当前index
						SaveIndexDBManager.Builder(getApplicationContext())
								.saveDb(position, album.getMyproduct_id(),
										DrmPat.PDF);
						// 返回album数据
						Intent data = new Intent();
						Bundle bundle = new Bundle();
						bundle.putSerializable("Album", album);
						data.putExtras(bundle);
						// 判断是主界面跳转过来的，还是pdf界面跳转过来的。
						if (isFromMainUI(judging_jump)) {
							// 直接打开pdf页面
							openActivity(MuPDFActivity.class, bundle);
						} else if (TextUtils.equals(judging_jump,
								DRMUtil.from_MuPDFActivity)) {
							setResult(Activity.RESULT_OK, data);
							finish();
						}
						adapter.setCurrentPosition(position);
						adapter.notifyDataSetChanged();
						UIHelper.startInAnim(MuPDFAlbumActivity.this);
					}
				}
			}
		});


		imgBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				flingRightBack();
			}
		});

		mGestureDetector = new GestureDetector(this,
				new OnBackGestureListener() {

					@Override
					public void onFlingRight() {
						flingRightBack();
					}
				});

		/**
		 * 此处让listview执行手势事件,避免和activity的OnTouchEvent()冲突
		 */
		mListView.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event) {
				mGestureDetector.onTouchEvent(event);
				return false;
			}
		});
	}



	@Override
	protected void load_Data() {
		contents = MediaUtils.getInstance().getPdfmediaList();
		if (adapter == null)
			adapter = new Mupdf_AlbumList_Adapter(this, contents);
		adapter.setCurrentPosition(MediaUtils.MPDF_CURRENTPOS);
		mListView.setAdapter(adapter);
		// 定位到当前选择阅读的位置
		mListView.setSelection(MediaUtils.MPDF_CURRENTPOS);
	}

	@Override
	protected void init_Value() {
		album = (Album) getIntent().getSerializableExtra("Album");
		judging_jump = getIntent().getStringExtra("Judging_jump");// 判断是主界面跳转过来的，还是pdf界面跳转过来的。
		Log.e("mupdfalbumactivity","1");
		// 第一次init
		MediaUtils.getInstance()
				.initPdf(getApplicationContext(), album.getId());
		Log.e("mupdfalbumactivity","2");
		// 获取用户上次阅读后的子专辑的位置。
		// SaveIndexDBManager就是存储点击专辑的索引位置
		MediaUtils.MPDF_CURRENTPOS = SaveIndexDBManager.Builder(this)
				.findIndexByMyProId(album.getMyproduct_id());
		Log.e("mupdfalbumactivity","3");
		if (isFromMainUI(judging_jump)) {
			// 从主界面跳转，则设置沉浸式状态栏
			UIHelper.showTintStatusBar(this,
					getResources().getColor(R.color.title_bar_bg_color));
		}
		Log.e("mupdfalbumactivity","4");

	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		flingRightBack();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActicityManager.getInstance().remove(MuPDFAlbumActivity.this);
	}

	/**
	 * 右滑返回
	 */
	private void flingRightBack() {
		setResult(Activity.RESULT_CANCELED, null);
		UIHelper.finishActivity(MuPDFAlbumActivity.this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_img:
			flingRightBack();
			break;
		default:
			break;
		}
	}
	//删除单个文件。暂时没用到。
	public void clearing(AlbumContent ac) {
		ArrayList<String> assetIdList = AlbumDAOImpl.getInstance().findAssetId(
				ac.getAlbum_id());
		if (assetIdList != null && assetIdList.size() > 0) {
			for (int i = 0; i < assetIdList.size(); i++) {
				String assetId = assetIdList.get(i);
				if (assetId.equals(ac.getAsset_id())) {
					 AlbumDAOImpl.getInstance().DeleteAlbumContentByasset_id(ac.getAsset_id());
					 String PermissionId = AlbumDAOImpl.getInstance()
							.findPermissionId(assetId);
					if (PermissionId != null) {
						AlbumDAOImpl.getInstance().DeletePermission(assetId);
						ArrayList<String> perconstraintIdList = AlbumDAOImpl
								.getInstance()
								.findPerconstraintId(PermissionId);
						if (perconstraintIdList != null) {
							AlbumDAOImpl.getInstance().DeletePerconstraint(
									PermissionId);
						}
					}
				}

			}
		}
	}
}
