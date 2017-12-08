package cn.com.pyc.drm.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.xutils.common.Callback;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.MusicCurrentPlayEvent;
import cn.com.pyc.drm.bean.event._ColorPixEvent;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.help.BitmapPixHelp;
import cn.com.pyc.drm.utils.manager.HttpEngine;

/**
 * 音乐的简介页面
 *
 * @author hudaqiang
 */
public class FragmentMusicPreview extends BaseFragment {

    private static final String TAG = "FragmentMusicPreview";
    private WebView mWebView;

    private Callback.Cancelable cancelable;
    private String fileId;
    private String color;

    private boolean hasLoadPreview = false;

    //当前歌曲切换了
    public void onEventMainThread(MusicCurrentPlayEvent event) {
        if (!TextUtils.equals(fileId, event.getFileId())) {
            if (mWebView == null) return;
            if (color == null) return;
            getMusicPreview(event.getFileId(), color);
            fileId = event.getFileId();
        }
    }

    //获取到了背景的颜色值的通知
    public void onEventMainThread(_ColorPixEvent event) {
        BitmapPixHelp._Color _color = event.getColor();
        color = BitmapPixHelp.rgbToColor(_color);
        if (!hasLoadPreview && fileId != null) {
            hasLoadPreview = true;
            getMusicPreview(fileId, color);
        }
        DRMLog.d("emt", "fg preview color " + color);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            fileId = bundle.getString(BaseFragment.MUSIC_FILE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_preview, container, false);
        mWebView = ((WebView) rootView.findViewById(R.id.music_preview));
        initWebView();
        super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    private void initWebView() {
        mWebView.getBackground().setAlpha(0);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // maybe remove code
        if (isVisibleToUser && mWebView != null) {
            if (!hasLoadPreview && color != null) {
                hasLoadPreview = true;
                getMusicPreview(fileId, color);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hasLoadPreview = false;
        mWebView.clearCache(true);
        mWebView.destroy();
        HttpEngine.cancelHttp(cancelable);
    }

    //loadDataWithBaseURL和loadData两个方法加载的HTML代码片段的不同点在于，
    // loadData()中的html data中不能包含'#', '%', '\', '?'四中特殊字符，
    // 在平时测试你的数据时，你的数据里含有这些字符，但不会出问题，当出问题时，你可以替换下。
    //%，会报找不到页面错误，页面全是乱码。乱码样式见符件。
    //#，会让你的goBack失效，但canGoBAck是可以使用的。于是就会产生返回按钮生效，但不能返回的情况。
    //loadData方法显示的HTML都是居左对齐，不管align的值是left|center|right，结果都是居左对齐，
    // 并且显示中文的时候必须使用UrlEncoder进行编码
    private void getMusicPreview(String fileId, String color) {
        cancelable = HttpEngine.get(APIUtil.getMusicPreview(fileId, color.substring(1)),
                new Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        mWebView.setVisibility(View.VISIBLE);
                        mWebView.loadDataWithBaseURL(null, result, "text/html", "utf-8", null);
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        DRMLog.d("onError: " + ex.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
    }


}
