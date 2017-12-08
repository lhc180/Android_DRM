package cn.com.pyc.drm.bean.event;

import cn.com.pyc.drm.utils.help.BitmapPixHelp;

/**
 * Created by hudaqiang on 2017/10/31.
 */

public class _ColorPixEvent extends BaseEvent {

    public BitmapPixHelp._Color mColor;

    public BitmapPixHelp._Color getColor() {
        return mColor;
    }

    public _ColorPixEvent(BitmapPixHelp._Color mColor) {
        this.mColor = mColor;
    }
}
