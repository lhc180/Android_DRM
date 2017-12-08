package cn.com.pyc.drm.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.ViewUtil;

public class ProgressWebView extends WebView {
    private ProgressBar mProgressBar;
    private TextView mTitleText;
//    private Activity mActivity;
//    //视频全屏参数
//    private static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout
//            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
// .MATCH_PARENT);
//    private View customView;
//    private FrameLayout fullscreenContainer;
//    private WebChromeClient.CustomViewCallback customViewCallback;


    /**
     * 设置标题textView
     */
    public void setTitleTextView(TextView textView) {
        mTitleText = textView;
    }

    /*
     * 设置挂载的activity
     */
//    public void setActivity(Activity activity) {
//        mActivity = activity;
//    }

//    public boolean isFullScreen() {
//        return customView != null;
//    }

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) return;

        mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        int h = CommonUtil.dip2px(context, 3.0f);
        mProgressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, h, 0, 0));
        Drawable drawable = context.getResources().getDrawable(R.drawable.xml_progressbar_states);
        mProgressBar.setProgressDrawable(drawable);
        addView(mProgressBar);

        WebSettings settings = getSettings();
        //settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //settings.setMinimumFontSize(settings.getMinimumFontSize() + 8);
        //settings.setTextSize(WebSettings.TextSize.NORMAL);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setJavaScriptEnabled(true); // 支持js
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(true); // 支持缩放
        settings.setUseWideViewPort(true); // 支持调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);// 排版适应屏幕
//        settings.setDomStorageEnabled(true);
//        settings.setDatabaseEnabled(true);
//        settings.setGeolocationEnabled(true);

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 关闭webview中缓存
        settings.setLoadsImagesAutomatically(true); // 支持自动加载图片
        settings.setAllowFileAccess(true); // 设置可以访问文件
        settings.supportMultipleWindows(); // 支持多窗口
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 支持通过JS打开新窗口

        // 如果webView中需要用户手动输入用户名、密码或其他，则webview必须设置支持获取手势焦点。
        requestFocusFromTouch();
        setVerticalScrollbarOverlay(true);

        setWebChromeClient(new WebViewChromeClient(this));
    }

    public static class WebViewChromeClient extends android.webkit.WebChromeClient {
        private ProgressWebView mWebView;

        public WebViewChromeClient(ProgressWebView webView) {
            mWebView = webView;
        }


//        @Override
//        public View getVideoLoadingProgressView() {
//            FrameLayout frameLayout = new FrameLayout(getContext());
//            frameLayout.setLayoutParams(COVER_SCREEN_PARAMS);
//            return frameLayout;
//        }
//
//        @Override
//        public void onShowCustomView(View view, CustomViewCallback callback) {
//            super.onShowCustomView(view, callback);
//            //showCustomView(mActivity, view, callback);
//        }
//
//        @Override
//        public void onHideCustomView() {
//            super.onHideCustomView();
//            //hideCustomView(mActivity);
//        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (mWebView.mProgressBar != null) {
                if (newProgress == 100) {
                    mWebView.mProgressBar.setVisibility(GONE);
                } else {
                    if (mWebView.mProgressBar.getVisibility() == GONE)
                        mWebView.mProgressBar.setVisibility(VISIBLE);
                    mWebView.mProgressBar.setProgress(newProgress);
                }
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (mWebView.mTitleText != null) {
                mWebView.mTitleText.setText(TextUtils.isEmpty(title) ? "浏览信息" : title);
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) mProgressBar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        mProgressBar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    //destroy webView
    public void destroyed() {
        clearCache(getContext());
        clearCookies(getContext(), null);
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
        destroy();
        ViewUtil.hideWidget(this);
    }

    //清除缓存数据
    public void clearCache(Context ctx) {
        //ctx.deleteDatabase("webview.db");
        //ctx.deleteDatabase("webviewCache.db");
        clearCache(true);
        clearSslPreferences();
        clearFormData();
        clearHistory();
        //releaseAllWebViewCallback();
    }

    //清除cookie
    public void clearCookies(Context ctx, ValueCallback<Boolean> vc) {
        @SuppressWarnings("unused")
        CookieSyncManager cookieSyncMng = CookieSyncManager.createInstance(ctx);
        CookieManager cookieManager = CookieManager.getInstance();
        //cookieManager.removeAllCookie();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(vc);
            //cookieManager.removeSessionCookies(vc);
        } else {
            cookieManager.removeAllCookie();
            //cookieManager.removeSessionCookie();
            if (vc != null) {
                vc.onReceiveValue(false);
            }
        }
    }

    /*
     * 同步一下指定url的cookie
     */
//    public static void synCookies(Context context, String url, String cookie) {
//        //CookieSyncManager.createInstance(context);
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        cookieManager.removeSessionCookie();//移除
//        cookieManager.setCookie(url, cookie);//指定要修改的cookies
//        cookieManager.flush();
//        //CookieSyncManager.getInstance().sync();
//    }

    /*
     * 尽可能防止webview内存泄露
     */
//    private void releaseAllWebViewCallback() {
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//            try {
//                Field field = WebView.class.getDeclaredField("mWebViewCore");
//                field = field.getType().getDeclaredField("mBrowserFrame");
//                field = field.getType().getDeclaredField("sConfigCallback");
//                field.setAccessible(true);
//                field.set(null, null);
//            } catch (NoSuchFieldException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        } else {
//            try {
//                Field sConfigCallback = Class.forName(
//                        "android.webkit.BrowserFrame").getDeclaredField(
//                        "sConfigCallback");
//                if (sConfigCallback != null) {
//                    sConfigCallback.setAccessible(true);
//                    sConfigCallback.set(null, null);
//                }
//            } catch (NoSuchFieldException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /*
     * 视频播放全屏
     **/
//    public void showCustomView(Activity activity, View view, CustomViewCallback callback) {
//        if (mActivity == null) return;
//        // if a view already exists then immediately terminate the new one
//        if (customView != null) {
//            callback.onCustomViewHidden();
//            return;
//        }
//        //activity.getWindow().getDecorView();
//        FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
//        fullscreenContainer = new FullscreenHolder(activity);
//        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
//        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
//        customView = view;
//        setStatusBarVisibility(activity, false);
//        customViewCallback = callback;
//    }

    /*
     * 隐藏视频全屏
     */
//    public void hideCustomView(Activity activity) {
//        if (mActivity == null) return;
//        if (customView == null) {
//            return;
//        }
//        setStatusBarVisibility(activity, true);
//        FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
//        decor.removeView(fullscreenContainer);
//        fullscreenContainer = null;
//        customView = null;
//        customViewCallback.onCustomViewHidden();
//        setVisibility(View.VISIBLE);
//    }

    /*
     * 全屏容器界面
     */
//    private static class FullscreenHolder extends FrameLayout {
//
//        public FullscreenHolder(Context ctx) {
//            super(ctx);
//            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
//        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent evt) {
//            return true;
//        }
//    }
//
//    public void setStatusBarVisibility(Activity activity, boolean visible) {
//        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
//        activity.getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//    }


}
