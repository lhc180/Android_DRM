package cn.com.pyc.drm.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.Tencent;

import java.io.UnsupportedEncodingException;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.pay.PayUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.help.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.ShareManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.ProgressWebView;
import de.greenrobot.event.EventBus;

import static android.R.id.message;
import static com.alipay.sdk.app.statistic.c.v;

/**
 * app内置浏览网页
 */
@SuppressLint("SetJavaScriptEnabled")
public class BrowserActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = BrowserActivity.class.getSimpleName();
    public static ProgressWebView mWebView;
    private String url;
    private boolean gotoLogin = false;
    private boolean discoverBuy;            //发现页进入购买
    private String overrideUrl;             //点击页面中元素链接，回调返回的url
    private String returnUrl;               //立即购买，未登录状态登录成功后，回调返回的url
    private ShareManager mShareManager;     //分享管理类
    private PayUtil payUtil;

    private Handler mHandler = new Handler();

    //注销后，通知清除cookie和cache
    public void onEventMainThread(ConductUIEvent event) {
        if (event.getType() == BaseEvent.Type.UI_BROWSER_CLEAR) {
            if (mWebView != null) {
                mWebView.clearCookies(this, null);
                mWebView.clearCache(this);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        ActicityManager.getInstance().add(this);
        UIHelper.showTintStatusBar(this);
        EventBus.getDefault().register(this);
        getValue();
        initView();
        //mShareManager = new ShareManager(this);
        payUtil = new PayUtil(this, mWebView);
        loadData();
    }


    @Override
    protected void getValue() {
        url = getIntent().getStringExtra("url");
        discoverBuy = getIntent().getBooleanExtra("discover_buy", false);
        DRMLog.d(TAG, "url = " + url + ", discover_buy = " + discoverBuy);
    }

    @Override
    protected void initView() {
        mWebView = (ProgressWebView) findViewById(R.id.ab_content_webview);
        mWebView.setTitleTextView((TextView) findViewById(R.id.ab_title_tv));
        findViewById(R.id.ab_back_image).setOnClickListener(this);
        findViewById(R.id.ab_share_image).setOnClickListener(this);

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new ProgressWebView.WebViewChromeClient(mWebView) {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                DRMLog.d(TAG, "js message: " + message);
                result.confirm();
                if (message != null && message.startsWith("http://")) {
                    share(message);
                }
                return true;
            }
        });
    }

    @Override
    protected void loadData() {
        overrideUrl = url;
        loadUrl(url);
    }

    // 定义JS需要调用的方法
    // 被JS调用的方法加入@JavascriptInterface注解
    public final class JsClassHookInterface {

        //支付完成去我的内容
        @JavascriptInterface
        public void gotoContentView_Android() {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConductUIEvent conductUIEvent = new ConductUIEvent(BaseEvent.Type
                            .UI_HOME_TAB_1);
                    conductUIEvent.setNet(true);
                    EventBus.getDefault().post(conductUIEvent);
                    finish();
                }
            }, 500);
        }

        //支付完成去发现页
        @JavascriptInterface
        public void gotoDiscoveryView_Android() {

        }

        //没登录去登录页面
        @JavascriptInterface
        public void gotoLoginView_Android() {
//            startActivity(new Intent(BrowserActivity.this, RegisterActivity.class)
//                    .putExtra("web_login", true));
            gotoLogin = true;
            startActivityForResult(new Intent(BrowserActivity.this, LoginActivity.class)
                    .putExtra("web_login", true).putExtra("web_url", overrideUrl), 10);
        }

        /**
         * 微信支付&支付宝支付
         *
         * @param array 传递的调用参数
         */
        @JavascriptInterface
        public void gotoPay_Android(final String[] array) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    payUtil.pay(array);
                }
            }, 500);
        }
    }

    private void loadUrl(String url) {
        if (url == null) {
            url = "";
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        mWebView.loadUrl(url);
        mWebView.addJavascriptInterface(new JsClassHookInterface(), "androidJsControl");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ab_back_image:
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                    return;
                }
                finishUI();
                break;
            case R.id.ab_share_image:
                shareTo();
                break;
            default:
                break;
        }
    }

    private void shareTo() {
        if (mShareManager == null) {
            mShareManager = new ShareManager(this);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            //使用alert提示返回的信息，在onJsAlert中回调message
            mWebView.loadUrl("javascript:alert(getShareParameter_Android())");
            return;
        }
        mWebView.evaluateJavascript("javascript:getShareParameter_Android()", new
                ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //此处为 js 返回的结果
                        DRMLog.d(TAG, "js Value: " + value);
                        share(value);
                    }
                });
    }

    //分享操作
    private void share(String value) {
        ShareManager.ShareData result = mShareManager.getShareData(value);
        if (result != null) {
            mShareManager.setShareData(result);
            mShareManager.showShareDialog();
        } else {
            showToast(getString(R.string.share_to_params_illegal));
        }
    }

    @Override
    public void onBackPressed() {
        finishUI();
    }

    private void finishUI() {
        UIHelper.finishActivity(this);
        if (gotoLogin && returnUrl != null) {
            //点击了立即购买，没登录去登录完毕后，通知发现页更新
            EventBus.getDefault().post(new ConductUIEvent(BaseEvent.Type.UPDATE_DISCOVER));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!gotoLogin) return;
        if (mWebView != null && overrideUrl != null && !overrideUrl.startsWith("mqqwpa://")) {
            final String url = DiscoverActivity.getFullUrlByToken(overrideUrl);
            if (!TextUtils.isEmpty(url)) {
                loadUrl(url);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActicityManager.getInstance().remove(this);
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        if (!discoverBuy) {
            //发现页，去购买，则不清除cookie
            mWebView.destroyed();
            //mWebView.clearCookies(this, null);
            //mWebView.clearCache(this);
        }
    }

    //H5转Native支付
//    private void fetchOrderFromH5Url(String url) {
//        final PayTask task = new PayTask(BrowserActivity.this);
//        final String ex = task.fetchOrderInfoFromH5PayUrl(url);
//        if (!TextUtils.isEmpty(ex)) {
//            DRMLog.e("paytask::" + ex);
//            new Thread(new Runnable() {
//                public void run() {
//                    final H5PayResultModel result = task.h5Pay(ex, true);
//                    final String returnUrl = result.getReturnUrl();
//                    DRMLog.e("支付—return:" + returnUrl + "|支付—code: " + result.getResultCode());
//                    if (!TextUtils.isEmpty(returnUrl)) {
//                        BrowserActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mWebView.loadUrl(returnUrl);
//                            }
//                        });
//                    }
//                }
//            }).start();
//        } else {
//            mWebView.loadUrl(url);
//        }
//    }

    // webView重载设置。方法根据需要选取重载。
    private final class WebViewClient extends android.webkit.WebViewClient {

        // 在点击请求的是链接是才会调用，重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。
        // 这个函数我们可以做很多操作，比如我们读取到某些特殊的URL，于是就可以不打开地址，取消这个操作，进行预先定义的其他操作，这对一个程序是非常必要的。
        // 在当前WebView处理链接。
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            DRMLog.i("Override::: " + url);
            overrideUrl = url;
            if (url.startsWith("mqqwpa://")) {
                //例如：mqqwpa://im/chat?chat_type=wpa&uin=1002164327&version=1
                //String qq = StringUtil.getString4Result(url, "uin=");
                if (CommonUtil.isQQClientAvailable(BrowserActivity.this)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    showToast("没有安装QQ客户端");
                }
            } else {
                //fetchOrderFromH5Url(url);
                mWebView.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            // 重写此方法可以让webview处理https请求。
            super.onReceivedSslError(view, handler, error);
            handler.proceed(); // 表示等待证书响应
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //sina weibo
        if (mShareManager != null && mShareManager.getWeiboManager() != null) {
            mShareManager.getWeiboManager().onNewIntent(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            returnUrl = data.getStringExtra("web_url"); //立即购买后，未登录状态去登录成功后返回
            DRMLog.i("onActivityResult: " + returnUrl);
        }

        //sina weibo
        if (mShareManager != null && mShareManager.getWeiboManager() != null) {
            mShareManager.getWeiboManager().onActivityResult(requestCode, resultCode, data);
        }
        //QQ
        if (mShareManager != null) {
            Tencent.onActivityResultData(requestCode, resultCode, data, mShareManager.mIUiListener);
            if (requestCode == Constants.REQUEST_API) {
                if (resultCode == Constants.REQUEST_QQ_SHARE || resultCode == Constants
                        .REQUEST_QZONE_SHARE || resultCode == Constants.REQUEST_OLD_SHARE) {
                    Tencent.handleResultData(data, mShareManager.mIUiListener);
                }
            }
        }
    }

}
