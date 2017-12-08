package cn.com.pyc.szpbb.coreui;

import android.content.Context;
import android.view.View;

import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.ReaderView;

import cn.com.pyc.szpbb.coreui.widget.SZPDFCore;
import cn.com.pyc.szpbb.coreui.widget.SZPDFDocView;
import cn.com.pyc.szpbb.util.SZLog;

/**
 * Created by hudq on 2016/12/7.
 */

public class SZPDFInterface {

    /**
     * 初始化PDFCore
     *
     * @param ctx
     * @param filePath
     * @return
     * @throws Exception
     */
    public static final SZPDFCore init(Context ctx, String filePath) throws Exception {
        SZLog.i("Trying to open " + filePath);
        SZPDFCore core = new SZPDFCore(ctx, filePath);
        // New file: drop the old outline data
        OutlineActivityData.set(null);
        return core;
    }

    /**
     * 释放资源,core and pageView
     *
     * @param core
     * @param mDocView
     */
    public static final void destroy(SZPDFCore core, SZPDFDocView mDocView) {
        if (mDocView != null) {
            mDocView.applyToChildren(new ReaderView.ViewMapper() {
                @Override
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }
        if (core != null) {
            core.onDestroy();
            core = null;
        }
    }

    /**
     * 当前解析core包是否有效
     *
     * @param core Pdf核心core类
     * @return true有效，反之false。
     */
    public static final boolean isValid(SZPDFCore core) {
        return core != null;
    }

    /**
     * 鉴定pdf文件的秘钥是否通过
     *
     * @param core Pdf核心core类
     * @param key  秘钥
     * @return
     */
    public static final boolean authenticate(SZPDFCore core, String key) {
        return core.needsPassword() && core.authenticatePassword(key);
    }


}
