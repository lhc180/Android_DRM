package cn.com.pyc.szpbb.coreui.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.artifex.mupdfdemo.MuPDFReaderView;

/**
 * Created by hudq on 2016/12/8.
 */

public abstract class SZPDFDocView extends MuPDFReaderView {
    public SZPDFDocView(Context context) {
        super(context);
    }

    public SZPDFDocView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMoveToChild(int i) {
        super.onMoveToChild(i);
        onPageChanged(i);
    }

    @Override
    protected void onTapMainDocArea() {
        super.onTapMainDocArea();
        onTapMainArea();
    }


    /**
     * 页数切换
     *
     * @param page
     */
    protected abstract void onPageChanged(int page);

    /**
     * 点击中心区域
     */
    protected abstract void onTapMainArea();
}
