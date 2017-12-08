package cn.com.pyc.drm.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtil {
    /**
     * 监视剪贴板内容
     *
     * @param context
     * @return
     */
    public static String eyesClipboard(Context context) {
        final ClipboardManager cm = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null)
            return "";
        ClipData data = cm.getPrimaryClip();
        if (data == null)
            return "";
        ClipData.Item item = data.getItemAt(0);
        if (item == null)
            return "";
        if (item.getText() == null)
            return "";
        String clipContent = item.getText().toString();
        DRMLog.e("clipboard: " + clipContent);
        return clipContent;
    }

    /**
     * 实现文本复制功能
     *
     * @param context
     * @param copyStr
     */
    public static void copyText(Context context, String copyStr) {
        android.content.ClipboardManager cm = (android.content.ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData clip = ClipData.newPlainText("copy_data", copyStr);
            cm.setPrimaryClip(clip);
        }
    }

    /**
     * 清空剪贴板内容
     *
     * @param context
     */
    public static void clearClipboard(Context context) {
        copyText(context, "");
    }
}
