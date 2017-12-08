package cn.com.pyc.drm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.Util_;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.widget.ProgressWebView;
import de.greenrobot.event.EventBus;

import static cn.com.pyc.drm.common.Constant.IMEI;

/**
 * desc:   发现       <br/>
 * author: hudaqiang       <br/>
 * create at 2017/6/29 18:51
 */
public class DiscoverActivity extends BaseAssistActivity {

    private ProgressWebView mWebView;
    private SwipeToLoadLayout mSwipeToLoadLayout;
    private View mEmptyView;
    private TextView mEmptyTextView;

    private boolean hasOpenStore = false;
    private String overrideUrl;

    //一键开店
    private static final String URL_OFFSET_OPEN_STORE =
            "/DRM/mobile/account/registerAndLogin?state=buyerOpenStore";
    //店铺
    //private static final String URL_OFFSET_SELLER = "/DRM/proQuery/sellerStore/showStoreDetails/";
    //产品详情
    //private static final String URL_OFFSET_PRODUCT = "/DRM/proQuery/productShow/proDetails/";
    //分类搜索
    //private static final String URL_OFFSET_CATEGORY = "/DRM/mobile/search/showSearch/";


    //登录（非web页购买时跳转后的登录）/注销后通知更新加载发现页
    public void onEventMainThread(ConductUIEvent event) {
        if (mWebView != null && event.getType() == BaseEvent.Type.UPDATE_DISCOVER) {
            mWebView.clearCookies(this, new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    DRMLog.e("onReceiveValue:" + value);
                    mWebView.clearCache(DiscoverActivity.this);
                    loadWebUI();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        initView();
        if (CommonUtil.isNetConnect(this)) {
            loadWebUI();
        } else {
            loadOffUI();
        }
    }

    private void initView() {
        EventBus.getDefault().register(this);
        mSwipeToLoadLayout = ((SwipeToLoadLayout) findViewById(R.id.swipeToLoadLayout));
        mWebView = ((ProgressWebView) findViewById(R.id.swipe_target));
        mEmptyView = findViewById(R.id.empty_layout);
        mEmptyTextView = (TextView) findViewById(R.id.empty_text);
        findViewById(R.id.dc_search_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity(SearchProductActivity2.class);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        mSwipeToLoadLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (CommonUtil.isNetConnect(DiscoverActivity.this)) {
                    loadWebUI();
                } else {
                    loadOffUI();
                }
                mSwipeToLoadLayout.setRefreshing(false);
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                DRMLog.d("url: " + url);
                overrideUrl = url;
                //打开的是去一键开店的url,则返回后需要刷新页面不显示一键开店按钮(未登录没有一键开店按钮)
                hasOpenStore = Util_.checkLogin() && url.contains(URL_OFFSET_OPEN_STORE);
                OpenPageUtil.openBrowserOfApp2Buy(DiscoverActivity.this, url);

//                if (url.contains(URL_OFFSET_PRODUCT)
//                        || url.contains(URL_OFFSET_SELLER)
//                        || Util_.checkLogin()) {
//                    //产品和店铺的详情,或者已登录过（一键开店）
//                    OpenPageUtil.openBrowserOfApp2Buy(DiscoverActivity.this, url);
//                    //打开的是去一键开店的url
//                    hasOpenStore = url.contains(URL_OFFSET_OPEN_STORE);
//                }
                return true;
            }
        });
    }

    private void loadOffUI() {
        ViewUtil.hideWidget(mWebView);
        ViewUtil.showWidget(mEmptyView);
        mEmptyTextView.setText(getString(R.string.net_unavailable_refresh));
    }

    //详情：proQuery/productShow/proDetails/
    //店铺：proQuery/sellerStore/showStoreDetails/
    private void loadWebUI() {
        ViewUtil.showWidget(mWebView);
        ViewUtil.hideWidget(mEmptyView);
        mWebView.loadUrl(APIUtil.getDiscoverUrl(1));
        DRMLog.d("loadUrl: " + APIUtil.getDiscoverUrl(1));
        mWebView.addJavascriptInterface(new JsClassHookInterface(), "androidJsControl");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroyed();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasOpenStore) {
            //一键开店结束，刷新当前webView
            mSwipeToLoadLayout.setRefreshing(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        ViewUtil.showContentDialog(this);
    }

    // 定义JS需要调用的方法
    // 被JS调用的方法必须加入@JavascriptInterface注解
    public final class JsClassHookInterface {

        //去开店：没登录去登录页面
        @JavascriptInterface
        public void gotoLoginView_Android() {
            startActivityForResult(new Intent(DiscoverActivity.this, LoginActivity.class)
                    .putExtra("web_login", true).putExtra("web_url", overrideUrl), 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (overrideUrl == null) return;
        if (resultCode != Activity.RESULT_OK) return;
        if (data == null) return;
        String url_ = data.getStringExtra("web_url");
        DRMLog.d("onActivityResult: " + url_);
        String url = getFullUrlByToken(url_);
        if (!TextUtils.isEmpty(url)) {
            mWebView.loadUrl(url);
        }
    }

    //通过前置存在的url获取整个带token的url
    public static String getFullUrlByToken(String prefixUrl) {
        String userName = Constant.getName(), token = Constant.getToken();
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(token)) {
            boolean hasChar = prefixUrl.contains("?"); //是否存在‘？’符号。
            String url = prefixUrl
                    + (hasChar ? "&username=" : "?username=") + userName
                    + "&token=" + token
                    + "&application_name=" + DrmPat.APP_FULLNAME
                    + "&app_version=" + CommonUtil.getAppVersionName(App.getInstance())
                    + "&IMEI=" + IMEI;
            DRMLog.e("full url: " + url);
            return url;
        }
        return null;
    }

}
