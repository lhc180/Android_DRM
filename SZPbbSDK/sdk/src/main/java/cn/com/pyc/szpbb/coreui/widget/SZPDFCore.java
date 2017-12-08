package cn.com.pyc.szpbb.coreui.widget;

import android.content.Context;

import com.artifex.mupdfdemo.MuPDFCore;

/**
 * Created by hudq on 2016/12/8.
 */

public class SZPDFCore extends MuPDFCore {
    public SZPDFCore(Context context, String filename) throws Exception {
        super(context, filename);
    }

    public SZPDFCore(Context context, byte[] buffer, String magic) throws Exception {
        super(context, buffer, magic);
    }
}
