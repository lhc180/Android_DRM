package cn.com.pyc.drm.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.karics.library.zxing.android.CaptureActivity;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.DrmFile;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.FileData;
import cn.com.pyc.drm.model.ProductInfo;
import cn.com.pyc.drm.ui.BrowserActivity;
import cn.com.pyc.drm.ui.ListFileActivity;
import cn.com.pyc.drm.ui.MuPDFActivity;
import cn.com.pyc.drm.ui.MusicPlayActivity;
import cn.com.pyc.drm.ui.VideoPlayerActivity;
import cn.com.pyc.drm.utils.help.KeyHelp;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.com.pyc.drm.utils.help.UIHelper;

/**
 * 管理打开其他de页面的工具类
 *
 * @author hudq
 */
public class OpenPageUtil {

    /**
     * 开启页面
     *
     * @param ctx
     * @param cls
     */
    public static void openActivity(Context ctx, Class<?> cls) {
        openActivity(ctx, cls, null);
    }

    /**
     * 通过类名启动Activity，并且含有Bundle数据
     *
     * @param ctx
     * @param cls
     * @param bundle 传递的bundle数据
     */
    public static void openActivity(Context ctx, Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(ctx, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        ctx.startActivity(intent);
    }

    // //////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////

    /**
     * 打开系统拨号界面
     *
     * @param context
     * @param phoneNumber
     */
    public static void openSystemDialPage(Context context, String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(context, "号码为空~", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            context.startActivity(Intent.createChooser(intent, null));
        }
    }

    /**
     * 打开系统浏览器
     *
     * @param context
     * @param url
     */
    public static void openBrowserOfSystem(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(Intent.createChooser(intent, null));
    }

    /**
     * 打开app的浏览器
     */
    public static void openBrowserOfApp(Context context, String url) {
        Intent intent = new Intent(context, BrowserActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    //发现页打开app浏览器，去购买
    public static void openBrowserOfApp2Buy(Context context, String url) {
        Intent intent = new Intent(context, BrowserActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("discover_buy", true);
        context.startActivity(intent);
    }

    /**
     * 系统分享
     */
    public static void sharedMore(Context ctx, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        ctx.startActivity(Intent.createChooser(intent, "选择分享"));
    }

    /**
     * 打开系统短信界面
     */
    public static void sharedSendMsg(Context ctx, String text) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        // sendIntent.setData(Uri.parse("smsto:"));
        sendIntent.setType("vnd.android-dir/mms-sms");
        sendIntent.putExtra("sms_body", text);
        ctx.startActivity(Intent.createChooser(sendIntent, null));
    }

    /**
     * 打开网络设置界面
     */
    public static void openWifiSetting(Context ctx) {
        Intent wifiSettingsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
        ctx.startActivity(wifiSettingsIntent);
    }

    // //////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////

    /**
     * 扫一扫
     *
     * @param activity
     */
    public static void openZXingCode(Activity activity) {
        Intent ZXIntent = new Intent(activity, CaptureActivity.class);
        activity.startActivityForResult(ZXIntent, CaptureActivity.REQUEST_CODE_SCAN);
    }


    /**
     * 打开播放
     *
     * @param curFileId 当前文件id
     * @param drmFiles
     */
    @Deprecated
    public static void openVideoPlayer(Activity activity, String curFileId, List<DrmFile>
            drmFiles) {
        Intent intent = new Intent(activity, VideoPlayerActivity.class);
        intent.putExtra(KeyHelp.KEY_FILE_ID, curFileId);
        intent.putParcelableArrayListExtra("drmFiles", (ArrayList<? extends Parcelable>) drmFiles);
        activity.startActivity(intent);
        UIHelper.startInAnim(activity);
    }

    /**
     * 打开文件列表视图(在线)
     *
     * @param activity
     * @param o        ProductInfo
     */
    public static void openFileListPage(Activity activity, ProductInfo o) {
        Intent intent = new Intent(activity, ListFileActivity.class);
        intent.putExtra("ProductInfo", o);
        activity.startActivity(intent);
        UIHelper.startInAnim(activity);
    }

    /**
     * 打开文件列表视图（离线可用）
     * 2017-02-15 修改
     *
     * @param activity
     * @param myProId
     * @param albumName
     * @param albumCategory
     * @param albumPic
     */
    public static void openFileListPage(Activity activity, String myProId, String
            albumName, String albumCategory, String albumPic, String author, String ratio) {

        //构造一个Product,给其中的字段赋值
        ProductInfo o = new ProductInfo();
        o.setMyProId(myProId);
        o.setProductName(albumName);
        o.setPicture_url(albumPic);
        o.setCategory(albumCategory);
        o.setAuthors(author);
        o.setPicture_ratio(ratio);

        Intent intent = new Intent(activity, ListFileActivity.class);
        intent.putExtra("ProductInfo", o);
        //intent.putExtra("category", albumCategory);
        activity.startActivity(intent);
        UIHelper.startInAnim(activity);

        //		Intent intent = new Intent(activity, ListFilesActivity2.class);
        //		intent.putExtra("myProId", myProId);
        //		intent.putExtra("productName", albumName);
        //		intent.putExtra("productUrl", albumPic);
        //		intent.putExtra("albumType", albumCategory);
        //		activity.startActivity(intent);
        //		UIHelper.startInAnim(activity);
    }

    /**
     * 打开并下载
     *
     * @param activity
     * @param o
     * @param fileId
     */
    public static void openFileListPageToDownload(Activity activity, ProductInfo o, String fileId) {
        Intent intent = new Intent(activity, ListFileActivity.class);
        intent.putExtra("ProductInfo", o);
        intent.putExtra("download_fileId", fileId);
        activity.startActivity(intent);
        UIHelper.startInAnim(activity);
    }

    /**
     * 打开对应的播放器
     *
     * @param aty
     * @param category
     * @param myProductId
     * @param productName
     * @param productUrl
     * @param fileId
     * @param lrcId
     * @param cacheData   //增加参数文件列表数据
     */
    public static void openPage(Activity aty,
                                String category,
                                String myProductId,
                                String productName,
                                String productUrl,
                                String fileId,
                                String lrcId,
                                List<FileData> cacheData) {
        Bundle bundle = new Bundle();
        bundle.putString(KeyHelp.KEY_MYPRO_ID, myProductId); //必传
        bundle.putString(KeyHelp.KEY_PRO_NAME, productName);
        bundle.putString(KeyHelp.KEY_PRO_URL, productUrl);
        bundle.putString(KeyHelp.KEY_FILE_ID, fileId);
        bundle.putString(KeyHelp.KEY_LRC_ID, lrcId);
        if (cacheData != null) { //增加参数文件列表数据
            bundle.putParcelableArrayList(KeyHelp.KEY_FILE_LIST,
                    (ArrayList<? extends Parcelable>) cacheData);
        }
        openUI(aty, category, bundle);
    }

    //分享此刻打开播放器页面
    public static void openPageFromCheck(Activity aty,
                                         String category,
                                         String myProductId,
                                         String productName,
                                         String productUrl,
                                         String fileId,
                                         String lrcId) {
        Bundle bundle = new Bundle();
        bundle.putString(KeyHelp.KEY_MYPRO_ID, myProductId); //必传
        bundle.putString(KeyHelp.KEY_PRO_NAME, productName);
        bundle.putString(KeyHelp.KEY_PRO_URL, productUrl);
        bundle.putString(KeyHelp.KEY_FILE_ID, fileId);
        bundle.putString(KeyHelp.KEY_LRC_ID, lrcId);
        bundle.putBoolean(KeyHelp.KEY_FROM_CHECK, true); //分享此刻检测到分享进入播放器
        openUI(aty, category, bundle);
    }

    private static void openUI(Activity aty, String category, Bundle bundle) {
        switch (category) {
            case DrmPat.MUSIC: {
                OpenPageUtil.openActivity(aty, MusicPlayActivity.class, bundle);
                aty.overridePendingTransition(R.anim.activity_music_open, R.anim.fade_out_scale);
            }
            break;
            case DrmPat.BOOK: {
                OpenPageUtil.openActivity(aty, MuPDFActivity.class, bundle);
                UIHelper.startInAnim(aty);
            }
            break;
            case DrmPat.VIDEO: {
                MusicHelp.release(aty);
                OpenPageUtil.openActivity(aty, VideoPlayerActivity.class, bundle);
            }
            break;
            default:
                Log.e("", "error category. Album category mistake.");
                break;
        }
    }

}
