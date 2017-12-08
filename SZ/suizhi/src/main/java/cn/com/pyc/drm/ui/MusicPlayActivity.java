package cn.com.pyc.drm.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.pyc.drm.R;
import cn.com.pyc.drm.adapter.MusicViewPagerAdapter;
import cn.com.pyc.drm.adapter.MusicViewPagerUnconnectAdapter;
import cn.com.pyc.drm.bean.DrmFile;
import cn.com.pyc.drm.bean.event._ColorPixEvent;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.common.MusicMode;
import cn.com.pyc.drm.fragment.BaseFragment;
import cn.com.pyc.drm.model.FileData;
import cn.com.pyc.drm.receiver.MusicProgressReceiver;
import cn.com.pyc.drm.receiver.MusicTimerReceiver;
import cn.com.pyc.drm.service.MusicTimerService;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.FormatUtil;
import cn.com.pyc.drm.utils.SPUtils;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.BitmapPixHelp;
import cn.com.pyc.drm.utils.help.ImageBlurHelp;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.help.LoadDataHelp;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.com.pyc.drm.utils.help.ProgressHelp;
import cn.com.pyc.drm.utils.help.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.LrcEngine;
import cn.com.pyc.drm.utils.manager.MusicUIShowManager;
import cn.com.pyc.drm.utils.manager.ShareMomentEngine;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.RoundCornerIndicaor;
import de.greenrobot.event.EventBus;

/**
 * desc:音乐播放界面
 * author: hudq
 * update at 2017/6/23 15:45
 */
public class MusicPlayActivity extends BaseFragmentActivity implements OnClickListener {

    private static final String TAG = "MusicPlayUI";

    @BindView(R.id.music_back)
    HighlightImageView backImg;
    @BindView(R.id.music_close)
    HighlightImageView closeImg;
    @BindView(R.id.prompt_btn)
    HighlightImageView promptBtn;           // 播放模式
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.indicator_square)
    RoundCornerIndicaor indicaor;
    @BindView(R.id.currTimeTextView)
    TextView currTimeTextView;              // 当前播放时间
    @BindView(R.id.totalTimeTextView)
    TextView totalTimeTextView;             // 歌曲总时间
    @BindView(R.id.share_layout)
    View floatView;                         //分享此刻悬浮框
    @BindView(R.id.share_text_pos)
    TextView floatPosText;                  //分享此刻时间刻度提示
    @BindView(R.id.share_image_btn)
    HighlightImageView floatSharePosImg;    //分享此刻按钮
    @BindView(R.id.progressSeekBar)
    SeekBar progressSeekBar;
    @BindView(R.id.play_pause_btn)
    HighlightImageView pausePlayBtn;           // 播放/暂停
    @BindView(R.id.prev_btn)
    HighlightImageView preBtn;                  //上一首
    @BindView(R.id.next_btn)
    HighlightImageView nextBtn;                 //下一首
    @BindView(R.id.timer_text)
    TextView tvCountDown;                       //定时关闭-倒计时文字显示
    @BindView(R.id.timer_btn)
    HighlightImageView countDownImg;            //右下角定时菜单


    private boolean isLightBg;
    private boolean onStart;
    private String myProId;
    private String lrcId;
    private String productName;
    private String curFileId;
    private String albumPic;
    private List<FileData> dataList;  //加载的文件列表数据
    private List<DrmFile> contentFiles;
    private IntentFilter progressFilter;
    private LocalBroadcastManager mLocalBroadcastManager;
    private MusicUIShowManager mMusicUIManager;
    private int timerOption = 0;//定时选择标志
    private long recLen;
    private PopupWindow popupWindow;
    private Drawable playDrawable, pauseDrawable, bgDrawable;

    //获取到了背景的颜色值的通知
    public void onEventMainThread(_ColorPixEvent event) {
        BitmapPixHelp._Color _color = event.getColor();
        isLightBg = BitmapPixHelp.isLightColor(_color);
        if (mMusicUIManager == null) return;
        backImg.setImageDrawable(mMusicUIManager.back(isLightBg));
        closeImg.setImageDrawable(mMusicUIManager.close(isLightBg));
        countDownImg.setImageDrawable(mMusicUIManager.timer(isLightBg));
        setPlayMode(false);
        currTimeTextView.setTextColor(mMusicUIManager._1_textColor(isLightBg));
        totalTimeTextView.setTextColor(mMusicUIManager._1_textColor(isLightBg));
        tvCountDown.setTextColor(mMusicUIManager._1_textColor(isLightBg));
        indicaor.setUnselectColor(mMusicUIManager._1_textColor(isLightBg));
    }

    private MusicProgressReceiver mProgressReceiver = new MusicProgressReceiver() {
        @Override
        protected void progressTime(int currentPosition, int duration) {
            if (onStart) {
                currTimeTextView.setText(FormatUtil.change2Time(currentPosition));
                progressSeekBar.setProgress(currentPosition);
                String formatTime = FormatUtil.change2Time(duration);
                if (!totalTimeTextView.getText().toString().equals(formatTime)) {
                    totalTimeTextView.setText(formatTime);
                }
                progressSeekBar.setMax(duration);
            }
        }

        @Override
        protected void obtainTime(int currentPosition, int duration) {
            DRMLog.i("duration：" + duration);
            if (onStart) {
                totalTimeTextView.setText(FormatUtil.change2Time(duration));
                progressSeekBar.setMax(duration);
            }
        }
    };

    private MusicTimerReceiver mTimerReceiver = new MusicTimerReceiver() {
        @Override
        protected void onTick(long current) {
            if (onStart) {
                tvCountDown.setText(FormatUtil.formatTime(current));
            }
        }

        @Override
        protected void onFinish(String string) {
            if (onStart) {
                tvCountDown.setText(string);
                pausePlayBtn.setImageDrawable(pauseDrawable);
                finishUI(true);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        initConfig();
        getParams();
        initView();
        initData();
        initCurrentLrc();
    }

    private void initConfig() {
        ButterKnife.bind(this);
        ActicityManager.getInstance().add(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        EventBus.getDefault().registerSticky(this);
        UIHelper.showTintStatusBar(this, getResources().getColor(R.color.transparent));
        playDrawable = getResources().getDrawable(R.drawable.ic_play);
        pauseDrawable = getResources().getDrawable(R.drawable.ic_pause);
        bgDrawable = getResources().getDrawable(R.drawable.music_default_bg);
        if (getWindow() != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        contentFiles = new ArrayList<>();
        mMusicUIManager = MusicUIShowManager.getInstance();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        // 进度条广播
        progressFilter = new IntentFilter();
        progressFilter.addAction(DRMUtil.BROADCAST_MUSIC_PROGRESS);
        progressFilter.addAction(DRMUtil.BROADCAST_MUSIC_OBTAIN_TIME);
        mLocalBroadcastManager.registerReceiver(mProgressReceiver, progressFilter);

        //定时器
        IntentFilter timerFilter = new IntentFilter();
        timerFilter.addAction(DRMUtil.BROADCAST_MUSIC_TIMER);
        timerFilter.addAction(DRMUtil.BROADCAST_MUSIC_TIMER_END);
        mLocalBroadcastManager.registerReceiver(mTimerReceiver, timerFilter);

    }

    private void getParams() {
        Intent intent = getIntent();
        myProId = intent.getStringExtra(KeyHelp.KEY_MYPRO_ID);                //专辑id
        productName = intent.getStringExtra(KeyHelp.KEY_PRO_NAME);            //专辑名
        albumPic = intent.getStringExtra(KeyHelp.KEY_PRO_URL);                //专辑图片url
        curFileId = intent.getStringExtra(KeyHelp.KEY_FILE_ID);               //当前文件id
        lrcId = intent.getStringExtra(KeyHelp.KEY_LRC_ID);                    //当前文件歌词id
        dataList = intent.getParcelableArrayListExtra(KeyHelp.KEY_FILE_LIST); //列表数据

//        List<AlbumContent> contents = AlbumContentDAOImpl.getInstance().findAlbumContentByMyProId
//                (myProId);
//        contentFiles = DRMFileHelp.convert2DrmFileList(contents, albumPic);
//        DRMLog.d(TAG, "count = " + contentFiles.size() + "; " + contentFiles.toString());
    }

    private void initData() {
        RelativeLayout musicUIBg = (RelativeLayout) findViewById(R.id.musicRL);
        musicUIBg.setBackgroundDrawable(bgDrawable);
        //测试使用的url: http://192.168.81.87
        //albumPic = "http://192.168.81.87/" + (new Random().nextInt(15) + 1) + ".jpg";
        if (!StringUtil.isEmptyOrNull(albumPic) &&
                (albumPic.startsWith("http://") || albumPic.startsWith("https://"))) {
            DRMLog.e("albumPic: " + albumPic);
            //处理背景模糊效果
            ImageBlurHelp blur = new ImageBlurHelp.Helper()
                    .setContext(this)
                    .setPictureUrl(albumPic)
                    .setTargetView(musicUIBg)
                    .create();
            blur.work();
        }
        //加载数据
        UIHelper.setEnableUI(this, false);
        final Activity _this = MusicPlayActivity.this;
        LoadDataHelp help = new LoadDataHelp.Builder()
                .setMyProductId(myProId)
                .setCurrentFileId(curFileId)
                .setMyProductUrl(albumPic)
                .setDateList(dataList)
                .init();
        help.load(new LoadDataHelp.OnLoadDataListener() {
            @Override
            public void onLoadSuccess(List<DrmFile> drmFiles, int currentPosition) {
                UIHelper.setEnableUI(MusicPlayActivity.this, true);
                //contentFiles = drmFiles;
                contentFiles.addAll(drmFiles);
                //进入此界面就隐藏关闭悬浮图标
                MusicHelp.removeMusicView(_this);
                //启动音乐播放的Service.
                MusicHelp.initMusicService(_this, myProId, albumPic, curFileId, contentFiles);
                //启动悬浮图标Service.
                MusicHelp.initMusicView(_this, productName, myProId, albumPic, dataList);
                // 初始化fragment
                initTabPoint();
                // 播放
                playMusic();
            }
        });
    }

    private void initCurrentLrc() {
        if (StringUtil.isEmptyOrNull(lrcId)) return;
        //判断本地歌词是否存在
        if (LrcEngine.existLyric(myProId, lrcId)) {
            //判断歌词是否有更新
            if (!LrcEngine.getLrcIdByPath(LrcEngine.getLyricPath(myProId, lrcId)).equals(lrcId)) {
                //有更新，删除本地旧歌词
                LrcEngine.deleteLyric(myProId, lrcId);
                //下载新歌词
                LrcEngine.getLyric(myProId, lrcId);
            }
        } else {
            //下载新歌词
            LrcEngine.getLyric(myProId, lrcId);
        }
    }

    private void initView() {
        floatSharePosImg.setOnClickListener(this);// 分享此刻
        backImg.setOnClickListener(this);// 后退
        closeImg.setOnClickListener(this);//关闭
        countDownImg.setOnClickListener(this); //右下角菜单
        preBtn.setOnClickListener(this);// 播放上一曲
        nextBtn.setOnClickListener(this); // 下一曲
        promptBtn.setOnClickListener(this);// 播放模式
        pausePlayBtn.setOnClickListener(this);// 播放
        tvCountDown.setOnClickListener(this);

        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    String progressText = FormatUtil.change2Time(progress);
                    currTimeTextView.setText(progressText);
                    floatPosText.setText(progressText);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                floatView.clearAnimation();
                ViewUtil.showWidget(floatView);
                mLocalBroadcastManager.unregisterReceiver(mProgressReceiver);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mLocalBroadcastManager.registerReceiver(mProgressReceiver, progressFilter);
                int progress = seekBar.getProgress();
                MusicHelp.progressPlay(MusicPlayActivity.this, progress);
                ViewUtil.hideWidgetAnimator(floatView);

                //倒计时-播完当前开启时候，进度条拖动时，更新倒计时数据
                long leftRec = (seekBar.getMax()) * 1000L - progress * 1000L;

                if (timerOption == 9) {
                    sendTimerData2Service(leftRec - 2000);
                }
            }
        });
    }

    private void playMusic() {
        if (MusicHelp.isSameMusic(curFileId)) {
            //同一首歌曲，不处理
        } else {
            MusicHelp.stop(this);
        }
        MusicHelp.play(this);
        pausePlayBtn.setImageDrawable(pauseDrawable);
        //去分享此刻开始播放（存在的话）
        String sharePosition = ShareMomentEngine.getSharePosition(DrmPat.MUSIC);
        if (!TextUtils.isEmpty(sharePosition)) {
            MusicHelp.progressPlay(this, ConvertToUtils.toInt(sharePosition));
        } else {
            //是否存在播放进度
            int sProgress = (int) ProgressHelp.getProgress(curFileId, 0);
            if (sProgress > 0) {
                MusicHelp.progressPlay(this, sProgress);
                ProgressHelp.removeProgress(curFileId);
            }
        }
    }

    private void initTabPoint() {
        Bundle data = new Bundle();
        data.putString(BaseFragment.MUSIC_ALBUM_NAME, productName);
        data.putString(BaseFragment.MUSIC_IMAGE_URL, albumPic);
        data.putString(BaseFragment.MUSIC_MYPRO_ID, myProId);
        data.putString(BaseFragment.MUSIC_FILE_ID, curFileId);
        data.putString(BaseFragment.MUSIC_LRC_ID, lrcId);
        data.putParcelableArrayList(BaseFragment.MUSIC_CONTENT_LIST, (ArrayList<? extends
                Parcelable>) contentFiles);

        mViewPager.setOffscreenPageLimit(3);
        FragmentPagerAdapter adapter = null;
        //离线情况下不显示音频简介
        if (CommonUtil.isNetConnect(MusicPlayActivity.this)) {
            adapter = new MusicViewPagerAdapter(getSupportFragmentManager(),
                    data);
            mViewPager.setAdapter(adapter);
            indicaor.setViewPager(mViewPager, MusicViewPagerAdapter.FRAGMENT_NUM);
        } else {
            adapter = new MusicViewPagerUnconnectAdapter(getSupportFragmentManager(), data);
            mViewPager.setAdapter(adapter);
            indicaor.setViewPager(mViewPager, MusicViewPagerUnconnectAdapter.FRAGMENT_NUM);
        }
    }

    /**
     * 分享此刻
     */
    private void sharedMoment(String sharePosition) {
        if (!CommonUtil.isNetConnect(MusicPlayActivity.this)) {
            return;
        }
        ViewUtil.hideWidget(floatView);
        MusicHelp.pause(MusicPlayActivity.this);
        pausePlayBtn.setImageDrawable(playDrawable);
        ShareMomentEngine engine = new ShareMomentEngine.Intern()
                .setProId(ShareMomentEngine.getSelectProId())
                .setMyProId(this.myProId)
                .setItemId(this.curFileId)
                .setCategory(DrmPat.MUSIC)
                .setSharePosition(sharePosition)
                .launch();
        engine.work(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        onStart = true;
        setPlayMode(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPlayState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        onStart = false;
    }

    private void setPlayState() {
        switch (MusicMode.STATUS) {
            case MusicMode.Status.STOP:     // 状态为停止 按钮显示播放
            case MusicMode.Status.PAUSE:    // 状态是暂停 按钮显示播放
            case MusicMode.Status.RELEASE:  // 状态是释放 按钮显示播放
                pausePlayBtn.setImageDrawable(playDrawable);
                break;
            case MusicMode.Status.PLAY:     // 当前状是播放，按钮显示暂停
            case MusicMode.Status.CONTINUE:
            case MusicMode.Status.PROGRESS:
                pausePlayBtn.setImageDrawable(pauseDrawable);
                break;
            default:
                break;
        }
    }

    /**
     * 循环-单曲-随机
     *
     * @param click 是否点击
     */
    private void setPlayMode(boolean click) {
        if (click && CommonUtil.isFastDoubleClick(600)) return;
        if (promptBtn == null) return;
        switch (MusicHelp.getPlayMode()) {
            case MusicMode.RANDOM: {
                MusicHelp.setPlayMode(click ? MusicMode.CIRCLE : MusicMode.RANDOM);
//                promptBtn.setImageDrawable(getResources().getDrawable(click ? R.drawable
//                        .ic_sequence : R.drawable.ic_random));
                promptBtn.setImageDrawable(click ? mMusicUIManager.sequence(isLightBg) :
                        mMusicUIManager.random(isLightBg));
            }
            break;
            case MusicMode.SINGLE: {
                MusicHelp.setPlayMode(click ? MusicMode.RANDOM : MusicMode.SINGLE);
//                promptBtn.setImageDrawable(getResources().getDrawable(click ? R.drawable
//                        .ic_random : R.drawable.ic_single));
                promptBtn.setImageDrawable(click ? mMusicUIManager.random(isLightBg)
                        : mMusicUIManager.single(isLightBg));
            }
            break;
            case MusicMode.CIRCLE:
            default: {
                MusicHelp.setPlayMode(click ? MusicMode.SINGLE : MusicMode.CIRCLE);
//                promptBtn.setImageDrawable(getResources().getDrawable(click ? R.drawable
//                        .ic_single : R.drawable.ic_sequence));
                promptBtn.setImageDrawable(click ? mMusicUIManager.single(isLightBg)
                        : mMusicUIManager.sequence(isLightBg));
            }
            break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contentFiles.clear();
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        mLocalBroadcastManager.unregisterReceiver(mProgressReceiver);
        mLocalBroadcastManager.unregisterReceiver(mTimerReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_back:
                finishUI(false);
                break;
            case R.id.music_close:
                closeMusic();
                break;
            case R.id.timer_btn:
            case R.id.timer_text:
                showBottomPopupWindow(v);
                break;
            case R.id.prompt_btn:
            case R.id.prompt_text:
                setPlayMode(true);
                break;
            case R.id.play_pause_btn:
                setPauseOrPlay();
                break;
            case R.id.prev_btn:
                setPrevious();
                break;
            case R.id.next_btn:
                setNext();
                break;
            case R.id.share_image_btn:
                sharedMoment(String.valueOf(progressSeekBar.getProgress()));
                break;
            default:
                break;
        }
    }

    private void sendTimerData2Service(long s) {
        Intent intent = new Intent(this, MusicTimerService.class);
        intent.putExtra(MusicHelp.TIMER_COUNTDOWN, s);
        startService(intent);
    }

    /**
     * 底部弹出PopupWindow
     * <p>
     * 点击PopupWindow以外部分或点击返回键时,PopupWindow 会 消失
     *
     * @param view parent view
     */
    public void showBottomPopupWindow(View view) {
        //取出倒计时标识，设置默认时间的标识
        timerOption = (int) SPUtils.get(MusicHelp.TIMER_KEY, 0);
        //自定义PopupWindow的布局
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_timer_layout, null);
        final ImageView icUnopen, icFinishCurrent, ic10, ic20, ic30, ic60, ic90;
        icUnopen = (ImageView) contentView.findViewById(R.id.ic_timer_unopen);
        icFinishCurrent = (ImageView) contentView.findViewById(R.id.ic_timer_finish_current);
        ic10 = (ImageView) contentView.findViewById(R.id.ic_timer_10);
        ic20 = (ImageView) contentView.findViewById(R.id.ic_timer_20);
        ic30 = (ImageView) contentView.findViewById(R.id.ic_timer_30);
        ic60 = (ImageView) contentView.findViewById(R.id.ic_timer_60);
        ic90 = (ImageView) contentView.findViewById(R.id.ic_timer_90);

        if (timerOption == 0) {
            icUnopen.setVisibility(View.VISIBLE);
            icFinishCurrent.setVisibility(View.INVISIBLE);
            ic10.setVisibility(View.INVISIBLE);
            ic20.setVisibility(View.INVISIBLE);
            ic30.setVisibility(View.INVISIBLE);
            ic60.setVisibility(View.INVISIBLE);
            ic90.setVisibility(View.INVISIBLE);
        } else if (timerOption == 9) {
            icUnopen.setVisibility(View.INVISIBLE);
            icFinishCurrent.setVisibility(View.VISIBLE);
            ic10.setVisibility(View.INVISIBLE);
            ic20.setVisibility(View.INVISIBLE);
            ic30.setVisibility(View.INVISIBLE);
            ic60.setVisibility(View.INVISIBLE);
            ic90.setVisibility(View.INVISIBLE);
        } else if (timerOption == 10) {
            icUnopen.setVisibility(View.INVISIBLE);
            icFinishCurrent.setVisibility(View.INVISIBLE);
            ic10.setVisibility(View.VISIBLE);
            ic20.setVisibility(View.INVISIBLE);
            ic30.setVisibility(View.INVISIBLE);
            ic60.setVisibility(View.INVISIBLE);
            ic90.setVisibility(View.INVISIBLE);
        } else if (timerOption == 20) {
            icUnopen.setVisibility(View.INVISIBLE);
            icFinishCurrent.setVisibility(View.INVISIBLE);
            ic10.setVisibility(View.INVISIBLE);
            ic20.setVisibility(View.VISIBLE);
            ic30.setVisibility(View.INVISIBLE);
            ic60.setVisibility(View.INVISIBLE);
            ic90.setVisibility(View.INVISIBLE);
        } else if (timerOption == 30) {
            icUnopen.setVisibility(View.INVISIBLE);
            icFinishCurrent.setVisibility(View.INVISIBLE);
            ic10.setVisibility(View.INVISIBLE);
            ic20.setVisibility(View.INVISIBLE);
            ic30.setVisibility(View.VISIBLE);
            ic60.setVisibility(View.INVISIBLE);
            ic90.setVisibility(View.INVISIBLE);
        } else if (timerOption == 60) {
            icUnopen.setVisibility(View.INVISIBLE);
            icFinishCurrent.setVisibility(View.INVISIBLE);
            ic10.setVisibility(View.INVISIBLE);
            ic20.setVisibility(View.INVISIBLE);
            ic30.setVisibility(View.INVISIBLE);
            ic60.setVisibility(View.VISIBLE);
            ic90.setVisibility(View.INVISIBLE);
        } else if (timerOption == 90) {
            icUnopen.setVisibility(View.INVISIBLE);
            icFinishCurrent.setVisibility(View.INVISIBLE);
            ic10.setVisibility(View.INVISIBLE);
            ic20.setVisibility(View.INVISIBLE);
            ic30.setVisibility(View.INVISIBLE);
            ic60.setVisibility(View.INVISIBLE);
            ic90.setVisibility(View.VISIBLE);
        }

        //初始化PopupWindow,并为其设置布局文件
        popupWindow = new PopupWindow(contentView);

        //点击事件-不开启
        contentView.findViewById(R.id.timer_unopen).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                timerOption = 0;
                stopService(new Intent(MusicPlayActivity.this, MusicTimerService.class));
                tvCountDown.setText(" ");
                popupWindow.dismiss();
                popupWindow = null;
            }
        });
        //点击事件-播完当前
        contentView.findViewById(R.id.timer_finish_current).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                timerOption = 9;
                long timer_Max = progressSeekBar.getMax() * 1000L;
                long timer_Progress = progressSeekBar.getProgress() * 1000L;
                recLen = timer_Max - timer_Progress - 1000L;
                sendTimerData2Service(recLen);
                popupWindow.dismiss();
                popupWindow = null;
            }
        });
        //10
        contentView.findViewById(R.id.timer_10_later).setOnClickListener(new OnClickListener
                () {
            @Override
            public void onClick(View v) {
                timerOption = 10;
                recLen = 10 * 60 * 1000L;
                sendTimerData2Service(recLen);
                popupWindow.dismiss();
                popupWindow = null;
            }
        });
        //20
        contentView.findViewById(R.id.timer_20_later).setOnClickListener(new OnClickListener
                () {
            @Override
            public void onClick(View v) {
                timerOption = 20;
                recLen = 20 * 60 * 1000L;
                sendTimerData2Service(recLen);
                popupWindow.dismiss();
                popupWindow = null;
            }
        });
        //30
        contentView.findViewById(R.id.timer_30_later).setOnClickListener(new OnClickListener
                () {
            @Override
            public void onClick(View v) {
                timerOption = 30;
                recLen = 30 * 60 * 1000L;
                //开启服务
                sendTimerData2Service(recLen);

                popupWindow.dismiss();
                popupWindow = null;
            }
        });
        //60
        contentView.findViewById(R.id.timer_60_later).setOnClickListener(new OnClickListener
                () {
            @Override
            public void onClick(View v) {
                timerOption = 60;
                recLen = 60 * 60 * 1000L;
                sendTimerData2Service(recLen);
                popupWindow.dismiss();
                popupWindow = null;
            }
        });
        //90
        contentView.findViewById(R.id.timer_90_later).setOnClickListener(new OnClickListener
                () {
            @Override
            public void onClick(View v) {
                timerOption = 90;
                recLen = 90 * 60 * 1000L;
                sendTimerData2Service(recLen);
                popupWindow.dismiss();
                popupWindow = null;
            }
        });
        //设置PopupWindow的宽和高,必须设置,否则不显示内容(也可用PopupWindow的构造方法设置宽高)
        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        //当需要点击返回键,或者点击空白时,需要设置下面两句代码.
        //如果有背景，则会在contentView外面包一层PopupViewContainer之后作为mPopupView，如果没有背景，则直接用contentView
        // 作为mPopupView。
        //而这个PopupViewContainer是一个内部私有类，它继承了FrameLayout，在其中重写了Key和Touch事件的分发处理
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));   //为PopupWindow设置透明背景.
        popupWindow.setOutsideTouchable(false);
        //设置PopupWindow进入和退出动画
        popupWindow.setAnimationStyle(R.style.anim_popup_bottombar);
        //设置PopupWindow显示的位置
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        UIHelper.setScreenAlpha(this, 0.5f);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                SPUtils.save(MusicHelp.TIMER_KEY, timerOption);
                UIHelper.setScreenAlpha(MusicPlayActivity.this, 1f);
            }
        });
    }

//    private void backGroundAlpha(float bgAlpha) {
//        if (getWindow() != null) {
//            WindowManager.LayoutParams params = getWindow().getAttributes();
//            params.alpha = bgAlpha;
//            getWindow().setAttributes(params);
//        }
//    }

    private void dismissPop() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    /**
     * 关闭音乐
     */
    private void closeMusic() {
        MusicHelp.release(this);
        MusicHelp.stopMusicView(this);
        finishUI(true);
        stopService(new Intent(this, MusicTimerService.class));
    }


    /**
     * 关闭当前activity
     */
    private void finishUI(boolean close) {
        ActicityManager.getInstance().remove(this);
        dismissPop();
        finish();
        overridePendingTransition(R.anim.fade_in_scale, R.anim.activity_music_close);
        if (!close) {
            MusicHelp.showFloatView(this);
        }
    }

    // 下一首
    private void setNext() {
        if (CommonUtil.isFastDoubleClick(800)) return;
        MusicHelp.next(this);
        pausePlayBtn.setImageDrawable(pauseDrawable);
    }

    // 上一首
    private void setPrevious() {
        if (CommonUtil.isFastDoubleClick(800)) return;
        MusicHelp.previous(this);
        pausePlayBtn.setImageDrawable(pauseDrawable);
    }

    // 播放、暂停
    private void setPauseOrPlay() {
        if (CommonUtil.isFastDoubleClick(800)) return;
        if (MusicMode.STATUS == MusicMode.Status.PLAY
                || MusicMode.STATUS == MusicMode.Status.CONTINUE
                || MusicMode.STATUS == MusicMode.Status.PROGRESS) {
            MusicHelp.pause(this);
            pausePlayBtn.setImageDrawable(playDrawable);
        } else if (MusicMode.STATUS == MusicMode.Status.PAUSE) {
            MusicHelp.continuePlay(this);
            pausePlayBtn.setImageDrawable(pauseDrawable);
        } else if (MusicMode.STATUS == MusicMode.Status.STOP
                || MusicMode.STATUS == MusicMode.Status.RELEASE) {
            MusicHelp.play(this);
            pausePlayBtn.setImageDrawable(pauseDrawable);
        }
    }

    @Override
    public void onBackPressed() {
        finishUI(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) return;
        //检测到分享此刻的信息，进入的播放器页面
        if (intent.getBooleanExtra(KeyHelp.KEY_FROM_CHECK, false)) {
            getParams();
            initData();
            initCurrentLrc();
        }
    }
}
