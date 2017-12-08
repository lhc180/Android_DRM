package cn.com.pyc.drm.utils.manager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.App;

/**
 * Created by hudaqiang on 2017/11/2.
 */

public class MusicUIShowManager {

    private Resources mResources;
    private static MusicUIShowManager INSTANCE = new MusicUIShowManager();

//    protected static int ICON_BACK_LIGHT;           //图标最小化，浅色
//    protected static int ICON_BACK_DEEP;            //图标最小化，深色
//
//    protected static int ICON_CLOSE_LIGHT;          //关闭
//    protected static int ICON_CLOSE_DEEP;
//
//    protected static int ICON_TIMER_LIGHT;          //定时
//    protected static int ICON_TIMER_DEEP;
//
//    protected static int ICON_RANDOM_LIGHT;         //随机播放
//    protected static int ICON_RANDOM_DEEP;
//    protected static int ICON_CIRCLE_LIGHT;         //循环播放
//    protected static int ICON_CIRCLE_DEEP;
//    protected static int ICON_SINGLE_LIGHT;         //单曲播放
//    protected static int ICON_SINGLE_DEEP;

    private static int TEXT_COLOR_LIGHT;          //浅色一级文字颜色
    private static int TEXT_COLOR_SUB_LIGHT;      //浅色二级文字颜色
    private static int TEXT_COLOR_DEEP;           //深色一级文字颜色
    private static int TEXT_COLOR_SUB_DEEP;       //深色二级文字颜色

    static {
        TEXT_COLOR_LIGHT = Color.parseColor("#f6f6f6");
        TEXT_COLOR_SUB_LIGHT = Color.parseColor("#eaeaea");
        TEXT_COLOR_DEEP = Color.parseColor("#201e1f");
        TEXT_COLOR_SUB_DEEP = Color.parseColor("#312d33");
    }

    private MusicUIShowManager() {
        Context context = App.getInstance();
        mResources = context.getResources();
    }

    public static MusicUIShowManager getInstance() {
        if (INSTANCE == null) {
            return new MusicUIShowManager();
        }
        return INSTANCE;
    }

    //最小化
    public Drawable back(boolean light) {
        return mResources.getDrawable(light ? R.drawable.xml_music_back2 : R.drawable
                .xml_music_back);
    }

    //关闭
    public Drawable close(boolean light) {
        return mResources.getDrawable(light ? R.drawable.ic_close_deep : R.drawable.ic_close);
    }

    //定时
    public Drawable timer(boolean light) {
        return mResources.getDrawable(light ? R.drawable.ic_timer_deep : R.drawable.ic_timer);
    }

    //随机
    public Drawable random(boolean light) {
        return mResources.getDrawable(light ? R.drawable.ic_random_deep : R.drawable.ic_random);
    }

    //单曲
    public Drawable single(boolean light) {
        return mResources.getDrawable(light ? R.drawable.ic_single_deep : R.drawable.ic_single);
    }

    //顺序
    public Drawable sequence(boolean light) {
        return mResources.getDrawable(light ? R.drawable.ic_sequence_deep : R.drawable.ic_sequence);
    }

    //一级文字颜色
    public int _1_textColor(boolean light) {
        return light ? TEXT_COLOR_DEEP : TEXT_COLOR_LIGHT;
    }

    //2级文字颜色
    public int _2_textColor(boolean light) {
        return light ? TEXT_COLOR_SUB_DEEP : TEXT_COLOR_SUB_LIGHT;
    }


}
