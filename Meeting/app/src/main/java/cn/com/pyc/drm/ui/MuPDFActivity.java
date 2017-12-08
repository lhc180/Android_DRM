package cn.com.pyc.drm.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import tv.danmaku.ijk.media.widget.Util.AnimationUtil;
import tv.danmaku.ijk.media.widget.Util.AnimationUtil.Location;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;
import cn.com.meeting.drm.R;
import cn.com.pyc.drm.bean.event.MuPDFBookMarkJPageEvent;
import cn.com.pyc.drm.bean.event.MuPDFBookMarkUploadEvent;
import cn.com.pyc.drm.common.ConstantValue;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.bean.Bookmark;
import cn.com.pyc.drm.model.db.practice.BookmarkDAOImpl;
import cn.com.pyc.drm.service.FloatViewService;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.MediaUtils;
import cn.com.pyc.drm.utils.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.SaveIndexDBManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.MarqueeTextView;
import cn.com.pyc.drm.widget.ToastShow;

import com.artifex.mupdfdemo.Hit;
import com.artifex.mupdfdemo.MuPDFAlert;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFReflowAdapter;
import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.OutlineItem;
import com.artifex.mupdfdemo.SearchTask;
import com.artifex.mupdfdemo.SearchTaskResult;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

/**
 * pdf阅读界面
 * 
 */
public class MuPDFActivity extends BaseActivity implements View.OnClickListener {

	private static final String TAG = "MuPDFActivity";
	// 竖屏和横屏时缩放倍数
	public static final float sScaleVertical = 1.0f;
	public static final float sScaleHorizontal = 1.1002f;// 2.3224998f;
	private static final int OUTLINE_REQUEST = 10;
	private static final int ALBUM_RELOAD_REQUEST = 12;
	private MuPDFCore core;
	private String mFileName;
	private MuPDFReaderView mDocView;
	private View mButtonsView;
	private boolean mButtonsVisible;
	private TextView mPageCurrentView; // 当前页数
	private TextView mPageTotalView; // 总页数
	private SeekBar mPageSlider;
	private TextView mOutlineText;
	private HighlightImageView mPdfFileInfoButton;
	private ViewAnimator mTopBarSwitcher;
	private TextView tvFloatPage;
	private View bottomBarMain;

	private boolean mStartTouchSeekBar = false;
	private boolean mAlertsActive = false;
	private boolean mReflow = false;
	private int mPageSliderRes;
	private String myProID;
	private Album album;

	private TopBarMode mTopBarMode = TopBarMode.Main;
	private View mTitleBack;
	private SearchTask mSearchTask;
	private AlertDialog mAlertDialog;
	private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
	private PopupWindow pwInfo;
	// private String PASSWORD = "E24DB22E5BD621885D30CB9495F9EFD2";
	/** 最外层父类layout */
	private RelativeLayout mParentLayout;
	private RelativeLayout mupdf_situation;
	private RelativeLayout mTitleBar;
	private TextView amc_text_count;
	private ImageView makebook;
	private int page_few;
	private String asset_id;
	private PdfPageSharedPreference preference;
	public OutlineItem[] outlines;
	private ToastShow ts = ToastShow.getInstances_();
	private MediaUtils mediaUtils = MediaUtils.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preference = new PdfPageSharedPreference(this);
		ImageLoader.getInstance().clearMemoryCache();
		ActicityManager.getInstance().add(this);
		// 获取传递过来的值，并取得 路径和 密钥
		init_Value();
		initCore(savedInstanceState);

		EventBus.getDefault().register(this);

		// 进来判断横竖屏
		// Point mSizePoint = DeviceUtils.getScreenSize(this);
		// ReaderView.HORIZONTAL_SCROLLING = (mSizePoint.x < mSizePoint.y);
	}

	/**
	 * 接收删除书签的事件
	 * 
	 * @param event
	 */
	public void onEventMainThread(MuPDFBookMarkUploadEvent event) {
		// 删除书签
		DRMLog.e(TAG, "delete mark");
		String contentId = asset_id.replaceAll("\"", "");
		Bookmark bookmark = BookmarkDAOImpl.getInstance().findBookmarkById(contentId + page_few);
		makebook.setSelected(bookmark != null);
	}

	/**
	 * 接收跳转页面的事件
	 * 
	 * @param event
	 */
	public void onEventMainThread(MuPDFBookMarkJPageEvent event) {
		mDocView.setDisplayedViewIndex(event.getPages());
	}

	private void makeButtonsView() {
		mButtonsView = getLayoutInflater().inflate(R.layout.activity_pdf_buttons, null);
		mupdf_situation = (RelativeLayout) mButtonsView.findViewById(R.id.mupdf_situation);
		amc_text_count = (TextView) mButtonsView.findViewById(R.id.amc_text_count);
		if (!mediaUtils.getPdfmediaList().isEmpty()) {
			amc_text_count.setVisibility(View.VISIBLE);
			amc_text_count.setText(mediaUtils.getPdfmediaList().size() + "");
		}
		MarqueeTextView pdfName = (MarqueeTextView) mButtonsView.findViewById(R.id.focused_tv);// PDF页面顶部名称
		makebook = (ImageView) mButtonsView.findViewById(R.id.makebook);
		mOutlineText = (TextView) mButtonsView.findViewById(R.id.outlineText);
		HighlightImageView mPdfListButton = (HighlightImageView) mButtonsView.findViewById(R.id.pdf_list_Button);
		mPdfFileInfoButton = (HighlightImageView) mButtonsView.findViewById(R.id.pdf_info_Button);
		mTopBarSwitcher = (ViewAnimator) mButtonsView.findViewById(R.id.switcher);
		mPageCurrentView = (TextView) mButtonsView.findViewById(R.id.currentPage_pdf_txt);
		mPageTotalView = (TextView) mButtonsView.findViewById(R.id.totalPage_pdf_txt);
		mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
		mTitleBack = mButtonsView.findViewById(R.id.back);
		mTitleBar = (RelativeLayout) mButtonsView.findViewById(R.id.rel_titlebar);
		tvFloatPage = (TextView) mButtonsView.findViewById(R.id.tv_float_page);
		bottomBarMain = mButtonsView.findViewById(R.id.bottomBar0Main);

		makebook.setOnClickListener(this);
		mTitleBack.setOnClickListener(this);
		mTitleBar.setOnClickListener(this);
		mTopBarSwitcher.setOnClickListener(this);
		mOutlineText.setOnClickListener(this);
		mPdfListButton.setOnClickListener(this);
		mPdfFileInfoButton.setOnClickListener(this);

		if (mediaUtils.getPdfmediaList().size() > 0) {
			String name = mediaUtils.getPdfmediaList().get(MediaUtils.MPDF_CURRENTPOS).getName();
			name = (name != null) ? name.replaceAll("\"", "") : "";
			pdfName.setText(name.replaceAll("\"", ""));
		}

		// 根据权限设置activity上的背景，此处不是pdf的背景
		if (hasPermit()) {
			mupdf_situation.setBackgroundColor(getResources().getColor(R.color.touming));
			mPageSlider.setEnabled(true);
		} else {
			mupdf_situation.setBackgroundColor(Color.parseColor("#8B8989"));
			mPageSlider.setEnabled(false);
		}
		// 获得assertId
		asset_id = mediaUtils.getPdfmediaList().get(MediaUtils.MPDF_CURRENTPOS).getAsset_id();
	}

	protected void init_Value() {
		album = (Album) getIntent().getSerializableExtra("Album");
		myProID = album.getMyproduct_id();
		// mediaUtils.pdfinitMedia(getApplicationContext(), album.getId());
		// 查询打开专辑的索引值
		int index = SaveIndexDBManager.Builder(this).findIndexByMyProId(myProID);
		DRMLog.e(TAG, "打开第" + (index + 1) + "个专辑文件");
		MediaUtils.MPDF_CURRENTPOS = index;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (mediaUtils.getPdfmediaList().isEmpty())
			mediaUtils.initPdf(getApplicationContext(), album.getId());
		myProID = album.getMyproduct_id();
	}

	/**
	 * core initial
	 * 
	 * @param savedInstanceState
	 */
	private void initCore(Bundle savedInstanceState) {
		if (core == null) {
			core = (MuPDFCore) getLastNonConfigurationInstance();
			if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
				mFileName = savedInstanceState.getString("FileName");
			}
		}
		if (core == null) {
			try {
				parserIntent(savedInstanceState);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void parserIntent(Bundle savedInstanceState) {
		String filePath = getMusicPath();
		core = openPdfFile(filePath);
		if (core != null && core.needsPassword()) {
			requestPassword(savedInstanceState);
			return;
		}
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(filePath);
			byte buffer[] = new byte[inputStream.available()];
			inputStream.read(buffer, 0, inputStream.available());
			core = openPdfBuffer(buffer);
			if (core != null && !core.hasOutline()) {
				mOutlineText.setVisibility(View.GONE);
			}
			if (core != null && core.needsPassword()) {
				ts.showBusy(getApplicationContext(), getString(R.string.need_password));
				requestPassword(savedInstanceState);
				return;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void requestPassword(Bundle savedInstanceState) {
		String key = mediaUtils.getPdfassetList().get(MediaUtils.MPDF_CURRENTPOS).getCek_cipher_value();
		if (core.authenticatePassword(key)) {
			createPdfUI(savedInstanceState);
		} else {
			makeButtonsView();
			mParentLayout = new RelativeLayout(this);
			mParentLayout.addView(mButtonsView);
			setContentView(mParentLayout);
			ts.show(getApplicationContext(), ToastShow.IMG_BUSY, getString(R.string.read_miss_private_key), Gravity.CENTER);
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.outlineText:
			// 点击显示目录
			if (hasPermit()) {
				if (pwInfo != null && pwInfo.isShowing()) {
					pwInfo.dismiss();
					pwInfo = null;
				}
				getLableMarkPopupWindow();
			} else {
				ts.showBusy(getApplicationContext(), getString(R.string.read_miss_private_key));
			}
			break;
		case R.id.pdf_list_Button: {
			Bundle b = new Bundle();
			b.putSerializable("Album", album);
			b.putString("Judging_jump", DRMUtil.from_MuPDFActivity);// 标识从pdf界面跳转
			Intent i = new Intent(this, MuPDFAlbumActivity.class);
			i.putExtras(b);
			startActivityForResult(i, ALBUM_RELOAD_REQUEST);
			UIHelper.startInAnim(this);
		}
			break;
		case R.id.back:
			UIHelper.finishActivity(this);
			break;
		case R.id.pdf_info_Button:// 图书信息
			showInfo();
			break;
		case R.id.makebook:
			makeBookMark();
			break;
		}
	}

	/**
	 * 设置书签
	 */
	private void makeBookMark() {
		if (hasPermit()) {
			Bundle bundle = new Bundle();
			bundle.putInt("page_few", page_few);
			bundle.putString("asset_id", asset_id.replaceAll("\"", ""));
			openActivity(MuPDFSaveNotes.class, bundle);
			UIHelper.startInAnim(this);
		}

		// Bookmark booknotes =
		// BookmarkDAOImpl.getInstance().findBookmarkById(asset_id.replaceAll("\"",
		// ""), page_few);
		//
		// if (booknotes == null)
		// {
		// openActivity(MuPDFSaveNotes.class, bundle);
		// makebook.setSelected(true);
		// String content = "";
		// TextWord[][] texwords = core.textLines(page_few);
		// for (int x = 1; x < texwords.length; x++)
		// {
		// TextWord[] textword = texwords[x];
		// for (int z = 0; z < textword.length; z++)
		// {
		// TextWord tw = textword[z];
		// String w = tw.getW();
		// content += w;
		// }
		// }
		// content = content.replace(" ", "").trim();
		// if ("".equals(content))
		// {
		// content = "[图片]";
		// } else
		// {
		// content = content.substring(0, content.length() < 60 ?
		// content.length() : 60);
		// }
		// Bookmark bm = new Bookmark();
		// bm.setId(System.currentTimeMillis() + "");
		// bm.setContent_ids(asset_id.replaceAll("\"", ""));
		// bm.setContent(content);
		// bm.setTime(currentdate);
		// bm.setPagefew(page_few + "");
		// BookmarkDAOImpl.getInstance().save(bm);
		// makebook.setSelected(true);
		// showToast(getString(R.string.add_bookmarks_success));
		// } else
		// {
		// bundle.putSerializable("booknotes", booknotes);
		// openActivity(MuPDFSaveNotes.class, bundle);
		// makebook.setSelected(true);

		// Bookmark bm = new Bookmark();
		// bm.setId(bookmark.getId());
		// bm.setContent_ids(bookmark.getContent_ids());
		// bm.setContent(bookmark.getContent());
		// bm.setTime(currentdate);
		// bm.setPagefew(bookmark.getPagefew());
		// BookmarkDAOImpl.getInstance().update(bm);
		// makebook.setSelected(true);
		// showToast(getString(R.string.bookmarks_exist));
		// }
		// } else
		// {
		// showToast(getString(R.string.bookmarks_er_because_unkey));
		// }
	}

	/**
	 * 打开目录
	 */
	private void getLableMarkPopupWindow() {

		OutlineItem[] outlines = core.getOutline();
		List<OutlineItem> outlineList = null;
		if (!"".equals(outlines) && outlines != null) {
			outlineList = Arrays.asList(outlines);
			// 当前页数
			String pagecurrentString = mPageCurrentView.getText().toString().trim();
			String pageCurrentSub = pagecurrentString.substring(1, pagecurrentString.length() - 1);
			int pageCurrent = ConvertToUtils.toInt(pageCurrentSub);
			for (int i = 0; i < outlines.length; i++) {
				DRMLog.e(TAG, outlines[i].toString());
				int page = outlines[i].page;
				if (page >= pageCurrent) {
					DRMUtil.OutlinePosition = i;
					break;
				}
				if (i == (outlines.length - 1)) {
					DRMUtil.OutlinePosition = i + 1;
					break;
				}
			}
		}
		Intent in = new Intent(MuPDFActivity.this, MuPDFOutlineActivity.class);
		if (outlineList != null) {
			// 目录
			Bundle bundle = new Bundle();
			bundle.putSerializable("outline", (Serializable) outlineList);
			in.putExtras(bundle);
		}
		MuPDFActivity.this.startActivityForResult(in, OUTLINE_REQUEST);
		hideBgLoading();
		UIHelper.startInAnim(MuPDFActivity.this);
		// }
		// }.execute();

	}

	/**
	 * 图书信息
	 */
	public void showInfo() {
		if (pwInfo == null) {
			showInfo(true);
		} else {
			showInfo(!pwInfo.isShowing());
		}
	}

	@SuppressLint("InflateParams")
	public void showInfo(boolean show) {
		if (show) {
			View infoView = getLayoutInflater().inflate(R.layout.dialog_pdf_infor, null);
			// DrmFile drmFile = curPlay;
			// String author = (!TextUtils.isEmpty(album.getAuthor())) ? album
			// .getAuthor().replace(";", "") : "DRM";
			((TextView) infoView.findViewById(R.id.dvi_txt_geshi)).setText("PDF");
			((TextView) infoView.findViewById(R.id.dvi_txt_yeshu)).setText(String.valueOf(core.countPages()));
			// ((TextView) infoView.findViewById(R.id.dvi_text_zuozhe))
			// .setText(author);
			if (pwInfo == null) {
				pwInfo = new PopupWindow(infoView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				pwInfo.setAnimationStyle(R.style.PopupWindow_info_anim);
			}
			pwInfo.showAtLocation(mPdfFileInfoButton, Gravity.LEFT | Gravity.BOTTOM, (mPdfFileInfoButton.getLeft() + mPdfFileInfoButton.getWidth() / 14), (mPdfFileInfoButton.getBottom() + 10));
		} else {
			if (pwInfo != null) {
				pwInfo.dismiss();
				pwInfo = null;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		switch (requestCode) {
		case OUTLINE_REQUEST:
			if (data != null) {
				mDocView.setDisplayedViewIndex(data.getIntExtra("page", 1));
			}
			break;

		case ALBUM_RELOAD_REQUEST: {
			if (data != null) {
				Album album = (Album) data.getSerializableExtra("Album");
				if (album != null) {
					MuPDFActivity.this.finish();
					Bundle bundle = new Bundle();
					bundle.putSerializable("Album", album);
					Intent intent = new Intent(this, MuPDFActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mFileName != null && mDocView != null) {
			outState.putString("FileName", mFileName);
			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded
			// Other info is needed only for screen-orientation change,
			// so it can go in the bundle
			// SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			// SharedPreferences.Editor edit = prefs.edit();
			// edit.putInt("page" + mFileName,
			// mDocView.getDisplayedViewIndex());
			// edit.commit();

			preference.putPageInt("page" + mFileName, mDocView.getDisplayedViewIndex());
		}

		if (!mButtonsVisible)
			outState.putBoolean("ButtonsHidden", true);

		if (mTopBarMode == TopBarMode.Search)
			outState.putBoolean("SearchMode", true);

		if (mReflow)
			outState.putBoolean("ReflowMode", true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSearchTask != null)
			mSearchTask.stop();

		if (mFileName != null && mDocView != null) {
			// SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			// SharedPreferences.Editor edit = prefs.edit();
			// edit.putInt("page" + mFileName,
			// mDocView.getDisplayedViewIndex());
			// edit.commit();

			preference.putPageInt("page" + mFileName, mDocView.getDisplayedViewIndex());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ActicityManager.getInstance().remove(this);
		if (core != null) {
			core.onDestroy();
			core = null;
		}
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		if (pwInfo != null && pwInfo.isShowing()) {
			pwInfo.dismiss();
			pwInfo = null;
		}
		EventBus.getDefault().unregister(this);
	}

	@Override
	public boolean onSearchRequested() {
		if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOn();
		}
		return super.onSearchRequested();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (core != null) {
			core.startAlerts();
			createAlertWaiter();
		}
		initialScreenOrientation();

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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initialScreenOrientation();
	}

	/**
	 * 设置横竖屏
	 */
	private void initialScreenOrientation() {
		int mCurrentOrientation = getResources().getConfiguration().orientation;
		if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
			// 竖屏
			// ReaderView.mScale = sScaleVertical;
			// ReaderView.HORIZONTAL_SCROLLING = true;
		} else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			// 横屏
			// ReaderView.mScale = sScaleHorizontal;
			// ReaderView.HORIZONTAL_SCROLLING = false;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (core != null) {
			destroyAlertWaiter();
			core.stopAlerts();
		}
		ts.cancelTos();
	}

	@Deprecated
	public void createAlertWaiter() {
		mAlertsActive = true;
		// All mupdf library calls are performed on asynchronous tasks to avoid
		// stalling
		// the UI. Some calls can lead to javascript-invoked requests to display
		// an
		// alert dialog and collect a reply from the user. The task has to be
		// blocked
		// until the user's reply is received. This method creates an
		// asynchronous task,
		// the purpose of which is to wait of these requests and produce the
		// dialog
		// in response, while leaving the core blocked. When the dialog receives
		// the
		// user's response, it is sent to the core via replyToAlert, unblocking
		// it.
		// Another alert-waiting task is then created to pick up the next alert.
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {
			@Override
			protected MuPDFAlert doInBackground(Void... arg0) {
				if (!mAlertsActive)
					return null;
				if (isCancelled())
					return null;

				return core.waitForAlert();
			}

			@Override
			protected void onPostExecute(final MuPDFAlert result) {
				// core.waitForAlert may return null when shutting down
				if (result == null)
					return;
				final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
				for (int i = 0; i < 3; i++)
					pressed[i] = MuPDFAlert.ButtonPressed.None;
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mAlertDialog = null;
						if (mAlertsActive) {
							int index = 0;
							switch (which) {
							case AlertDialog.BUTTON_POSITIVE:
								index = 0;
								break;
							case AlertDialog.BUTTON_NEGATIVE:
								index = 1;
								break;
							case AlertDialog.BUTTON_NEUTRAL:
								index = 2;
								break;
							}
							result.buttonPressed = pressed[index];
							// Send the user's response to the core, so that it
							// can
							// continue processing.
							core.replyToAlert(result);
							// Create another alert-waiter to pick up the next
							// alert.
							createAlertWaiter();
						}
					}
				};
				AlertDialog.Builder mAlertBuilder = new Builder(MuPDFActivity.this, android.R.style.Theme_Holo_Light);
				mAlertDialog = mAlertBuilder.create();
				mAlertDialog.setTitle(result.title);
				mAlertDialog.setMessage(result.message);
				switch (result.iconType) {
				case Error:
					break;
				case Warning:
					break;
				case Question:
					break;
				case Status:
					break;
				}
				switch (result.buttonGroupType) {
				case OkCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
				case Ok:
					mAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Ok;
					break;
				case YesNoCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.cancel), listener);
					pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
				case YesNo:
					mAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Yes;
					mAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.No;
					break;
				}
				mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						mAlertDialog = null;
						if (mAlertsActive) {
							result.buttonPressed = MuPDFAlert.ButtonPressed.None;
							core.replyToAlert(result);
							createAlertWaiter();
						}
					}
				});

				mAlertDialog.show();
			}
		};
		mAlertTask.execute();
	}

	@Deprecated
	public void destroyAlertWaiter() {
		mAlertsActive = false;
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
	}

	private MuPDFCore openPdfFile(String path) {
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1 ? path : path.substring(lastSlashPos + 1));
		DRMLog.i("Trying to open " + path);
		try {
			core = new MuPDFCore(this, path, null, 0, 0);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return core;
	}

	private MuPDFCore openPdfBuffer(byte buffer[]) {
		DRMLog.i("Trying to open byte buffer");
		try {
			core = new MuPDFCore(this, buffer, null);
			// core = new MuPDFCore(this, buffer);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return core;
	}

	public void createPdfUI(Bundle savedInstanceState) {
		if (core == null)
			return;

		// Now create the UI.
		// First create the document view
		mDocView = new MuPDFReaderView(this) {
			@Override
			protected void onMoveToChild(int i) {
				if (core == null)
					return;
				// mPageNumberView.setText(String.format("%d / %d", i + 1,
				// core.countPages()));
				page_few = i;
				// 当前页初始化
				mPageCurrentView.setText(getString(R.string.page_n, (i + 1)));
				// 总页数初始化
				mPageTotalView.setText(getString(R.string.page_total, core.countPages()));

				mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
				mPageSlider.setProgress(i * mPageSliderRes);
				// 书签
				Bookmark bookmark = BookmarkDAOImpl.getInstance().findBookmarkById(asset_id.replaceAll("\"", "") + page_few);
				makebook.setSelected(bookmark != null);
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {
				if (!mButtonsVisible) {
					showButtons();
				} else {
					if (mTopBarMode == TopBarMode.Main)
						hideButtons();
				}
			}

			@Override
			protected void onDocMotion() {
				hideButtons();
			}

			@Override
			protected void onHit(Hit item) {
				super.onHit(item);
				switch (mTopBarMode) {
				case Annot:
					if (item == Hit.Annotation) {
						showButtons();
						mTopBarMode = TopBarMode.Delete;
						mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					}
					break;
				case Delete:
					mTopBarMode = TopBarMode.Annot;
					mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					// fall through
				default:
					// Not in annotation editing mode, but the pageview will
					// still select and highlight hit annotations, so
					// deselect just in case.
					MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
					if (pageView != null)
						pageView.deselectAnnotation();
					break;
				}
			}
		};
		// mDocView.setAdapter(new MuPDFPageAdapter(this, core));
		MuPDFPageAdapter adapter = new MuPDFPageAdapter(this, null, core);
		mDocView.setAdapter(adapter);

		mSearchTask = new SearchTask(this, core) {
			@Override
			protected void onTextFound(SearchTaskResult result) {
				SearchTaskResult.set(result);
				// Ask the ReaderView to move to the resulting page
				mDocView.setDisplayedViewIndex(result.pageNumber);
				// Make the ReaderView act on the change to SearchTaskResult
				// via overridden onChildSetup method.
				mDocView.resetupChildren();
			}
		};

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// Set up the page slider
		int smax = Math.max(core.countPages() - 1, 1);
		mPageSliderRes = ((10 + smax - 1) / smax) * 2;

		// Activate the seekbar
		mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mStartTouchSeekBar = false;
				mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
				// 隐藏悬浮页数textview
				if (!hasPermit())
					return;
				(new Handler()).postDelayed(new Runnable() {

					@Override
					public void run() {
						tvFloatPage.startAnimation(AnimationUtils.loadAnimation(MuPDFActivity.this, android.R.anim.fade_out));
						tvFloatPage.setVisibility(View.GONE);
					}
				}, 600);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if (!hasPermit())
					return;
				mStartTouchSeekBar = true;
				int height = bottomBarMain.getHeight();
				height = height > 0 ? height : 200;
				RelativeLayout.LayoutParams params = (LayoutParams) tvFloatPage.getLayoutParams();
				params.bottomMargin = (int) (height * 1.8f);
				tvFloatPage.setLayoutParams(params);
				tvFloatPage.setVisibility(View.VISIBLE);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
				if (!hasPermit())
					return;
				// 滑动时view不可见了，则设置可见
				if (mStartTouchSeekBar && tvFloatPage.getVisibility() == View.GONE) {
					tvFloatPage.clearAnimation();
					tvFloatPage.setVisibility(View.VISIBLE);
				}
			}
		});

		// Reenstate last state if it was recorded
		// SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		int page = preference.getPageInt("page" + mFileName, 0);
		DRMLog.d(TAG, "currentPage = " + page);
		mDocView.setDisplayedViewIndex(page);

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
			showButtons();

		if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
			searchModeOn();

		if (savedInstanceState != null && savedInstanceState.getBoolean("ReflowMode", false))
			reflowModeSet(true);

		// Stick the document view and the buttons overlay into a parent view
		mParentLayout = new RelativeLayout(this);
		mParentLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		// mParentLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
		FrameLayout fLayout = new FrameLayout(this);
		fLayout.setBackgroundColor(getResources().getColor(R.color.white));
		mParentLayout.addView(fLayout);
		if (hasPermit()) {
			// 有权限
			fLayout.addView(mDocView);
		} else {
			mPageCurrentView.setText(getString(R.string.page_n, 0));
			mPageTotalView.setText(getString(R.string.page_total, 0));
			mPageSlider.setMax(0);
			mPageSlider.setProgress(0);
			ts.show(getApplicationContext(), ToastShow.IMG_BUSY, getString(R.string.read_miss_private_key), Gravity.CENTER);
		}
		fLayout.addView(mButtonsView);
		setContentView(mParentLayout);
	}

	/**
	 * 当前文件是否有权限
	 * 
	 * @return
	 */
	private boolean hasPermit() {
		return mediaUtils.getPdfmediaRight().get(MediaUtils.MPDF_CURRENTPOS).permitted;
	}

	private String getMusicPath() {
		String musicID = mediaUtils.getPdfmediaList().get(MediaUtils.MPDF_CURRENTPOS).getContent_id();
		final int length = musicID.length();
		if (length > 1) {
			musicID = musicID.substring(1, length - 1);
		}
		String filePath = DRMUtil.DEFAULT_SAVE_FILE_PATH + File.separator + myProID + File.separator + musicID + DrmPat._PDF;
		return filePath;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	private void reflowModeSet(boolean reflow) {
		mReflow = reflow;
		mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core) : new MuPDFPageAdapter(this, null, core));
		// setButtonEnabled(mSearchButton, !reflow);
		mDocView.refresh(mReflow);
	}

	// private void setButtonEnabled(ImageButton button, boolean enabled) {
	// button.setEnabled(enabled);
	// button.setColorFilter(enabled ? Color.argb(255, 255, 255, 255) : Color
	// .argb(255, 128, 128, 128));
	// }

	private void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			// 当前页
			updatePageNumView(index);
			// 总页数
			// mPageTotalView.setText(getString(R.string.page_total,
			// core.countPages()));

			// if (mTopBarMode == TopBarMode.Search)
			// {
			// mSearchText.requestFocus();
			// showKeyboard();
			// }

			// mPageSlider.setVisibility(View.VISIBLE);
			// // mPageNumberView.setVisibility(View.VISIBLE);
			// mPageCurrentView.setVisibility(View.VISIBLE);
			// mPageTotalView.setVisibility(View.VISIBLE);
			//
			// WindowManager.LayoutParams params = getWindow().getAttributes();
			// params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			// getWindow().setAttributes(params);
			// getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

			// if(layout!=null)
			// layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

			AnimationUtil.translate(mTitleBar, true, true, Location.Top);
			AnimationUtil.translate(mTopBarSwitcher, true, true, Location.Bottom);

			// mTitleBar.setVisibility(View.VISIBLE);
			// mTopBarSwitcher.setVisibility(View.VISIBLE);

		}
	}

	private void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			// hideKeyboard();
			// mPageCurrentView.setVisibility(View.GONE);
			// mPageTotalView.setVisibility(View.GONE);
			// mPageSlider.setVisibility(View.INVISIBLE);

			// WindowManager.LayoutParams params = getWindow().getAttributes();
			// params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			// getWindow().setAttributes(params);
			//
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			AnimationUtil.translate(mTitleBar, true, false, Location.Top);
			AnimationUtil.translate(mTopBarSwitcher, true, false, Location.Bottom);

			// mTitleBar.setVisibility(View.INVISIBLE);
			// mTopBarSwitcher.setVisibility(View.INVISIBLE);

			if (pwInfo != null) {
				showInfo(false);
			}
		}
	}

	@Deprecated
	private void searchModeOn() {
		if (mTopBarMode != TopBarMode.Search) {
			mTopBarMode = TopBarMode.Search;
			// Focus on EditTextWidget
			// mSearchText.requestFocus();
			// showKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		}
	}

	@Deprecated
	private void searchModeOff() {
		if (mTopBarMode == TopBarMode.Search) {
			mTopBarMode = TopBarMode.Main;
			// hideKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
			SearchTaskResult.set(null);
			// Make the ReaderView act on the change to mSearchTaskResult
			// via overridden onChildSetup method.
			mDocView.resetupChildren();
		}
	}

	/**
	 * 更新当前页数
	 * 
	 * @param index
	 */
	private void updatePageNumView(int index) {
		if (core == null)
			return;

		mPageCurrentView.setText(getString(R.string.page_n, (index + 1)));
		tvFloatPage.setText(getString(R.string.page_n, (index + 1)));
		// mPageTotalView.setText(getString(R.string.page_total,
		// core.countPages()));
	}

	// public void OnCancelSearchButtonClick(View v) {
	// searchModeOff();
	// }

	/** The core rendering instance */
	enum TopBarMode {
		Main, Search, Annot, Delete, More, Accept
	}

	@Override
	public void onBackPressed() {
		UIHelper.finishActivity(this);
	}

	/**
	 * 保存pdf页码的SharedPreferences
	 * 
	 * @author hudq
	 * 
	 */
	public static class PdfPageSharedPreference {

		private SharedPreferences.Editor editor;
		private SharedPreferences prefs;
		private String PREFS_NAME = "Pdf_Page_Preferences";

		public PdfPageSharedPreference(Context context) {
			prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			editor = prefs.edit();
		}

		/**
		 * 清除页数保存
		 */
		public void clearPdfPageData() {
			editor.clear().commit();
		}

		/**
		 * 保存键值
		 * 
		 * @param key
		 * @param value
		 */
		public void putPageInt(String key, int value) {
			editor.putInt(key, value);
			editor.commit();
		}

		/**
		 * 获取数值
		 * 
		 * @param key
		 * @param defValue
		 * @return
		 */
		public int getPageInt(String key, int defValue) {
			return prefs.getInt(key, defValue);
		}
	}

	/**
	 * @author 李巷阳
	 * @date 2016-9-8 上午11:28:53
	 */
	@Override
	protected void init_View() {
		// TODO Auto-generated method stub

	}

	/**
	 * @author 李巷阳
	 * @date 2016-9-8 上午11:28:53
	 */
	@Override
	protected void load_Data() {
		// TODO Auto-generated method stub

	}

}
