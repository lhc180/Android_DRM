package cn.com.pyc.drm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.MusicLrcEvent;
import cn.com.pyc.drm.bean.event._ColorPixEvent;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.help.BitmapPixHelp;
import cn.com.pyc.drm.utils.manager.LrcEngine;
import cn.com.pyc.drm.utils.manager.MusicUIShowManager;
import cn.com.pyc.drm.widget.lrc.LrcView;

/**
 * 歌词的展现界面
 *
 * @author hudaqiang
 */
public class FragmentMusicLrc extends BaseFragment {

    private LrcView mLrcView;
    //private WaveView mWaveView;

    private String myProId;
    private String lrcId;

    private String lrcPath;
    private boolean hasLoadLrc = false;

    /**
     * 歌词处理
     */
    public void onEventMainThread(MusicLrcEvent event) {
        MusicLrcEvent.Way way = event.getWay();
        Object obj = event.getObj();
        if (way == MusicLrcEvent.Way.LRC_LOAD) {                //加载歌词
            lrcPath = ((String) obj);
            loadLrc(lrcPath);
        } else if (way == MusicLrcEvent.Way.LRC_UPDATE) {       //更新歌词
            long time = ((Long) obj);
            mLrcView.updateTime(time);
        } else if (way == MusicLrcEvent.Way.LRC_CHANGE) {       //切换歌词
            String lrcId = ((String) obj);
            //if (!TextUtils.equals(lrcId, this.lrcId)) { //同一首歌歌词需要重新播放，不能判重
            this.lrcId = lrcId;
            resetLrc();
            LrcEngine.getLyric(myProId, lrcId);
            //}
        }
    }

    //获取到了背景的颜色值的通知
    public void onEventMainThread(_ColorPixEvent event) {
        BitmapPixHelp._Color _color = event.getColor();
        boolean isLightBg = BitmapPixHelp.isLightColor(_color);
        if (mLrcView != null) {
            mLrcView.setNormalColor(MusicUIShowManager.getInstance()._1_textColor(isLightBg));
        }
        DRMLog.d("emt", "fg lrc light is " + isLightBg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.myProId = bundle.getString(BaseFragment.MUSIC_MYPRO_ID);
            this.lrcId = bundle.getString(BaseFragment.MUSIC_LRC_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_lyc, container, false);
        //mWaveView = (WaveView) rootView.findViewById(R.id.lrc_wave_view);
        mLrcView = (LrcView) rootView.findViewById(R.id.lrc_view);
        //initLrcView(mLrcView);
        if (LrcEngine.existLyric(myProId, lrcId)) {
            lrcPath = LrcEngine.getLyricPath(myProId, lrcId);
            loadLrc(lrcPath);
        }
        super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mLrcView != null) {
            if (!hasLoadLrc) {
                hasLoadLrc = true;
                lrcPath = LrcEngine.getLyricPath(myProId, lrcId);
                mLrcView.loadLrc(new File(lrcPath));
            }
            //ViewUtil.showWidget(mWaveView);
        }
    }

//    private void initLrcView(LyricView mLrcView) {
//        mLrcView.setHighLightTextColor(getResources().getColor(R.color.brilliant_blue));
//        mLrcView.setLineSpace(16f);
//        mLrcView.setTextSize(16f);
//    }

    private void loadLrc(String lrcPath) {
        if (mLrcView != null && !hasLoadLrc) {
            hasLoadLrc = true;
            mLrcView.loadLrc(new File(lrcPath));
            DRMLog.d("loadLrc: " + lrcPath);
        }
    }

    private void resetLrc() {
        if (hasLoadLrc) {
            hasLoadLrc = false;
        }
    }

}
