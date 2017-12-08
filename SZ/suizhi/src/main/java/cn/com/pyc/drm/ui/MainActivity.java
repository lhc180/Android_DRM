package cn.com.pyc.drm.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.maxwin.view.XListView;
import com.android.maxwin.view.XListView.IXListViewListener;
import com.android.maxwin.view.internal.PLA_AdapterView;
import com.android.maxwin.view.internal.PLA_AdapterView.OnItemClickListener;

import org.xutils.common.Callback;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.adapter.MainListAdapter;
import cn.com.pyc.drm.adapter.MainLocalAdapter;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.ProductInfo;
import cn.com.pyc.drm.model.ProductListModel;
import cn.com.pyc.drm.model.ProductListModel.ProductListInfo;
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.model.db.practice.AlbumDAOImpl;
import cn.com.pyc.drm.receiver.NetworkChangedReceiver;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.Util_;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.ExecutorManager;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.utils.manager.ShareMomentEngine;
import cn.com.pyc.drm.widget.HighlightImageView;

/**
 * 主界面-2
 *
 * @author hudq
 */
@Deprecated
public class MainActivity extends BaseActivity implements OnClickListener,
        IXListViewListener {

    private static String TAG = MainActivity.class.getSimpleName();
    private static final int MSG_LOCAL_DATA = 0xaaa; //本地数据
    private static final int MSG_NET_CHANGE = 0xaab; //网络切换
    private static final int MSG_LOAD_DATA = 0xaac;  //加载数据
    //private String TITLE_ONLINE;
    //private String TITLE_OFFLINE;
    private final Handler mHandler = new MyOwnHandler(this);

    private XListView mPullToListView;
    private XListView mLocalListView;
    private View mNetWarning;
    private View emptyView;
    private ImageView emptyImage;
    private TextView emptyText;
    private HighlightImageView netClose;
    //private TextView tvTitle;
    //private HighlightImageView userImg;
    //private View dotView;

    private NetworkChangedReceiver netReceiver;
    private MainListAdapter adapter;
    private MainLocalAdapter localAdapter;
    private int refreshCount = 0;
    private long lastLoadTime = 0L;
    private int totalPageNum = 1;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isFinding = false;// 本地正在加载
    private Callback.Cancelable mDataCancelable;


    private static class MyOwnHandler extends Handler {
        private WeakReference<MainActivity> reference;

        private MyOwnHandler(MainActivity context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (reference == null) return;
            MainActivity activity = reference.get();
            if (activity == null) return;
            switch (msg.what) {
                case MSG_LOCAL_DATA: {
                    activity.isFinding = false;
                    Bundle bundle = msg.getData();
                    @SuppressWarnings("unchecked")
                    List<Album> albumsLocals = (List<Album>) bundle.getSerializable("album_list");
                    activity.hideLoading();
                    if (albumsLocals == null || albumsLocals.isEmpty()) {
                        ViewUtil.hideWidget(activity.mLocalListView);
                        activity.showEmptyView(activity
                                .getString(R.string.offline_data_empty));
                    } else {
                        activity.localAdapter = new MainLocalAdapter(activity, albumsLocals);
                        activity.mLocalListView.setAdapter(activity.localAdapter);
                    }
                }
                break;
                case MSG_NET_CHANGE:
                    if (!activity.mHandler.hasMessages(MSG_LOAD_DATA)) {
                        activity.changeNetwork();
                    }
                    //activity.forceUpdate();
                    break;
                case MSG_LOAD_DATA:
                    if (CommonUtil.isNetConnect(activity)) {
                        activity.requestData(true);
                    } else {
                        activity.loadLocalData();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static class LoadLocalDataThread implements Runnable {
        public WeakReference<MainActivity> reference;

        private LoadLocalDataThread(MainActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            if (reference == null) return;
            MainActivity activity = reference.get();
            if (activity == null) return;

            if (activity.localAdapter != null) {
                activity.localAdapter.getAlbums().clear();
                activity.localAdapter = null;
            }
            // 获取本地所有的专辑.
            List<Album> albums = AlbumDAOImpl.getInstance().findAll(Album.class, "DESC");
            // 分集下载文件后，保存的专辑有重复.去重复。
            List<Album> albums_ = Util_.wipeRepeatAlbumData(albums);
            Message msg = Message.obtain();
            msg.what = MSG_LOCAL_DATA;
            Bundle data = new Bundle();
            data.putSerializable("album_list", (Serializable) albums_);
            msg.setData(data);
            activity.mHandler.sendMessageDelayed(msg, 500);
        }
    }

    private BroadcastReceiver clearReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            switch (intent.getAction()) {
                case DRMUtil.BROADCAST_CLOSE_ACTIVITY: // 注销，关闭页面
                    loginOut4CloseActivity();
                    break;
                case DRMUtil.BROADCAST_CLOSE_ACTIVITY2: // 注销，关闭页面
                    loginOut5CloseActivity();
                    break;
                case DRMUtil.BROADCAST_CLEAR_DOWNLOADED: // 清理缓存后，重新加载数据
                    loadingAfterClear();
                    break;
                case DRMUtil.BROADCAST_CLEAR_DOWNLOADED_ALBUM: // 删除单个专辑
                    if (mHandler.hasMessages(MSG_NET_CHANGE)) {
                        mHandler.removeMessages(MSG_NET_CHANGE);
                        return;
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_NET_CHANGE, 500);
                    break;
                default:
                    break;
            }
        }
    };

    // TODO:
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getValue();
        initView();
        loadData();
        registerBroadcast();
        //checkVersion();
    }

    /**
     * 初始化一些基本数据
     */
    @Override
    protected void getValue() {
        //createIfNot();// 非绥知账号登录时，1.数据库可能需要重新创建
        //UIHelper.showTintStatusBar(this);
        ActicityManager.getInstance().add(this);
        initWidget();
    }

    private void initWidget() {
        //TITLE_ONLINE = getString(R.string.title_online);
        //TITLE_OFFLINE = getString(R.string.title_offline);

        mPullToListView = (XListView) findViewById(R.id.main_listview);
        mLocalListView = (XListView) findViewById(R.id.main_local_listview);
        mNetWarning = findViewById(R.id.un_net_layout);
        netClose = (HighlightImageView) findViewById(R.id.un_image_close);
        emptyView = findViewById(R.id.empty_layout);
        emptyText = (TextView) findViewById(R.id.empty_text);
        emptyImage = (HighlightImageView) findViewById(R.id.empty_image);
        //tvTitle = (TextView) findViewById(R.id.tv_head_title);
        //userImg = (HighlightImageView) findViewById(R.id.iv_head_user);
        //dotView = findViewById(R.id.iv_head_dot);
        findViewById(R.id.main_search_text).setOnClickListener(this);

        ViewUtil.hideWidget(mNetWarning);// 隐藏无网络提示
        ViewUtil.hideWidget(emptyView);// 隐藏暂无数据提示
        ViewUtil.hideWidget(mLocalListView);// 隐藏显示本地数据的listview
    }

    @Override
    protected void initView() {
        hideEmptyView();
        mNetWarning.setOnClickListener(this);
        //userImg.setOnClickListener(this);
        netClose.setOnClickListener(this);
        mPullToListView.setXListViewListener(this);
        mPullToListView.setPullRefreshEnable(true);
        mPullToListView.setSelector(R.drawable.transparent);
        // mPullToListView.setFlingLoadImage(false);

        mLocalListView.setPullRefreshEnable(false);
        mLocalListView.setPullLoadEnable(false);
        mLocalListView.setSelector(R.drawable.transparent);
        // 覆盖XListView中onScroll方法，mTotalItemCount=0,使XListView滑动受阻
        mLocalListView.setOnScrollListener(null);
        mLocalListView.setXListViewListener(null);
        mLocalListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(PLA_AdapterView<?> parent, View view,
                                    int position, long id) {
                Object object = mLocalListView.getItemAtPosition(position);
                if (object != null && object instanceof Album) {
                    // 打开已经下载的内容
                    Album a = (Album) object;
                    OpenPageUtil.openFileListPage(MainActivity.this,
                            a.getMyproduct_id(), a.getName(), a.getCategory(),
                            a.getPicture(), a.getAuthor(), a.getPicture_ratio());

                }
            }
        });
        mPullToListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(PLA_AdapterView<?> parent, View view,
                                    final int position, long id) {
                if (mPullToListView.ismPullRefreshing()
                        || mPullToListView.ismPullLoading()) {
                    return;
                }
                Object obj = mPullToListView.getItemAtPosition(position);
                if (obj != null && obj instanceof ProductInfo) {
                    ProductInfo o = (ProductInfo) obj;
                    OpenPageUtil.openFileListPage(MainActivity.this, o);
                    //当前选择的专辑进入
                    ShareMomentEngine.setSelectProId(o.getProId());
                }
            }
        });
    }

    @Override
    protected void loadData() {
        //预防网络切换时重复加载数据
        mHandler.sendEmptyMessage(MSG_LOAD_DATA);
    }

    // 注销用户，停止所有下载并且关闭当前主页面。
    private void loginOut4CloseActivity() {
        if (adapter != null) {
            adapter.getInfos().clear();
            adapter = null;
        }
        finish();
    }

    // 注销用户，停止所有下载并且不关闭当前主页面。
    private void loginOut5CloseActivity() {
        if (adapter != null) {
            adapter.getInfos().clear();
            adapter = null;
        }
//        finish();
    }
    //清理缓存数据后，重新加载
    private void loadingAfterClear() {
        hideEmptyView();
        ViewUtil.hideWidget(mPullToListView);
        ViewUtil.hideWidget(mLocalListView);
        ViewUtil.hideWidget(mNetWarning);
        //String titleStr = CommonUtil.isNetConnect(this) ? TITLE_ONLINE : TITLE_OFFLINE;
        //tvTitle.setText(titleStr);

        boolean isNetLoad = CommonUtil.isNetConnect(this) ? requestData(false) : loadLocalData();
        DRMLog.d(" net " + isNetLoad);
    }

    private void changeNetwork() {
//        if (tvTitle.getAnimation() == null) {
//            Animation animation = AnimationUtils.loadAnimation(this, R.anim
//                    .slide_alpha_in_from_top);
//            tvTitle.startAnimation(animation);
//        } else {
//            tvTitle.getAnimation().start();
//        }
        if (CommonUtil.isNetConnect(this)) {
            hasNetwork();
        } else {
            noneNetwork();
        }
    }


    //非绥知账号登录时，数据库可能需要重新创建,备用，实际登录成功时已经添加创建代码
//    private void createIfNot() {
//        if (SZConfig.LoginConfig.type != DrmPat.LOGIN_GENERAL) {
//            // 非绥知账号登录（扫码，微信，QQ）
//            DRMDBHelper.destoryDBHelper();
//            DRMDBHelper drmDB = new DRMDBHelper(this.getApplicationContext());
//            drmDB.createDBTable();
//            PathUtil.createFilePath();
//        }
//    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DRMUtil.BROADCAST_CLOSE_ACTIVITY);
        filter.addAction(DRMUtil.BROADCAST_CLOSE_ACTIVITY2);
        filter.addAction(DRMUtil.BROADCAST_CLEAR_DOWNLOADED);
        filter.addAction(DRMUtil.BROADCAST_CLEAR_DOWNLOADED_ALBUM);
        registerReceiver(clearReceive, filter);

        netReceiver = new NetworkChangedReceiver(this) {
            @Override
            protected void onNetChange(boolean isConnect) {
                if (mHandler.hasMessages(MSG_NET_CHANGE)) {
                    mHandler.removeMessages(MSG_NET_CHANGE);
                    return;
                }
                mHandler.sendEmptyMessage(MSG_NET_CHANGE);
            }
        };
    }

    //网络连接
    private void hasNetwork() {
        hideEmptyView();
        ViewUtil.hideWidget(mLocalListView);
        ViewUtil.hideWidget(mNetWarning);
        //tvTitle.setText(TITLE_ONLINE);
        adapter = null;
        requestData(true);
    }

    //无网络
    private void noneNetwork() {
        hideEmptyView();
        ViewUtil.hideWidget(mPullToListView);
        ViewUtil.showWidget(mNetWarning);
        //tvTitle.setText(TITLE_OFFLINE);
        loadLocalData();
    }

    @Override
    public void onBackPressed() {
        ViewUtil.showContentDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshCount = 0;
        lastLoadTime = 0L;
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideLoading();
        mHandler.removeMessages(MSG_NET_CHANGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(clearReceive);
        netReceiver.abortReceiver(this);
        HttpEngine.cancelHttp(mDataCancelable);
        ExecutorManager.getInstance().shutdownNow();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.iv_head_user:
//                openActivity(SettingActivity.class);
//                UIHelper.startInAnim(this);
//                break;
            case R.id.un_net_layout:
                OpenPageUtil.openWifiSetting(this);
                break;
            case R.id.un_image_close:
                mNetWarning.startAnimation(AnimationUtils.loadAnimation(this,
                        android.R.anim.fade_out));
                ViewUtil.hideWidget(mNetWarning);
                break;
            case R.id.main_search_text:
                openActivity(SearchProductActivity.class);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            default:
                break;
        }
    }

    /**
     * load本地数据
     */
    private boolean loadLocalData() {
        if (isFinding) return false;
        isFinding = true;

        currentPage = 1;
        showLoading();
        ViewUtil.hideWidget(mPullToListView);
        ViewUtil.showWidget(mLocalListView);
        ExecutorManager.getInstance().execute(new LoadLocalDataThread(this));
        return false;
    }

    private Bundle createParamsForData() {
        Bundle bundle = new Bundle();
        bundle.putInt("category", 0);
        bundle.putInt("page", currentPage);
        bundle.putString("token", Constant.getToken());
        bundle.putString("username", Constant.getName());
        bundle.putString("application_name", DrmPat.APP_FULLNAME);
        bundle.putString("app_version", CommonUtil.getAppVersionName(this));
        return bundle;
    }

    /**
     * 加载网络数据，从currentPage = 1开始
     *
     * @param showLoading 是否显示加载进度框
     */
    private boolean requestData(boolean showLoading) {
        if (isLoading) return true;
        isLoading = true;

        if (showLoading) showLoading();
        hideEmptyView();
        ViewUtil.showWidget(mPullToListView);
        ViewUtil.hideWidget(mLocalListView);
        Bundle bundle = createParamsForData();
        mDataCancelable = HttpEngine.post(APIUtil.getProductsListUrl(), bundle,
                new Callback.CommonCallback<String>() {
                    @Override
                    public void onCancelled(CancelledException arg0) {
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        DRMLog.d(TAG, "failJSON: " + arg0.getMessage());
                        getDataFailed(R.string.load_server_failed_again);
                    }

                    @Override
                    public void onFinished() {
                        isLoading = false;
                        mPullToListView.stopRefresh();
                        mPullToListView.stopLoadMore();
                        mHandler.removeMessages(MSG_LOAD_DATA);
                        hideLoading();
                    }

                    @Override
                    public void onSuccess(String mJsonString) {
                        DRMLog.d(TAG, "requestJSON:" + mJsonString);
                        ProductListModel o = JSON.parseObject(mJsonString, ProductListModel.class);
                        if (o.isSuccess()) {
                            getDataSuccess(o);
                        } else {
                            if (o.getMsg() != null && o.getMsg().contains("验证")) {
                                MusicHelp.release(MainActivity.this);
                                // 发送广播通知主界面，停止下载，清除数据，关闭主界面。
                                sendBroadcast(new Intent(DRMUtil.BROADCAST_CLOSE_ACTIVITY));
                                //reInitValue();
                                Util_.repeatLogin(MainActivity.this);
                            }
                            getDataFailed(R.string.getdata_failed);
                        }
                    }
                });
        return true;
    }

    private void getDataSuccess(ProductListModel o) {
        hideEmptyView();
        if (o.getData() == null) {
            // 隐藏netListView,避免netListView接收滑动崩溃
            ViewUtil.hideWidget(mPullToListView);
            showEmptyView(getString(R.string.getdata_failed));
            return;
        }
        ProductListInfo oo = o.getData();
        List<ProductInfo> oNetList = oo.getItems();
        totalPageNum = oo.getTotalPageNum();
        DRMLog.d(TAG, "totalPage = " + totalPageNum);
        if (oNetList == null || oNetList.isEmpty()) {
            ViewUtil.hideWidget(mPullToListView);
            showEmptyView(getString(R.string.load_data_null_again));
            emptyImage.setOnClickListener(againClick);
            return;
        }
        DRMLog.d(TAG, "dataSize = " + oNetList.size());

        //判断卖家是否禁用，如已禁用且未下载，清除此条数据不显示。
        Iterator<ProductInfo> Pro_it = oNetList.iterator();
        while (Pro_it.hasNext()) {
            ProductInfo proInfo = Pro_it.next();
            Album album = AlbumDAOImpl.getInstance().findAlbumByMyProId(proInfo.getMyProId());
            if (!proInfo.isValid() && album == null) {
                Pro_it.remove();
            }
        }
        if (adapter == null || currentPage == 1) {
            // 刷新或者第一次加载
            adapter = new MainListAdapter(this, oNetList);
            mPullToListView.setAdapter(adapter);
        } else {
            adapter.addLastInfos(oNetList);
            adapter.notifyDataSetChanged();
        }
        mPullToListView.setPullLoadEnable(currentPage < totalPageNum);
        ViewUtil.showWidget(mPullToListView);// 显示网络listView

        //cacheList = oNetList;
        //checkShare();
    }

    private void getDataFailed(int strId) {
        // 隐藏netListView,避免netListView接收滑动崩溃
        ViewUtil.hideWidget(mPullToListView);
        MusicHelp.release(this);
        showEmptyView(getString(strId));
        emptyImage.setOnClickListener(againClick);
    }

    OnClickListener againClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideEmptyView();
            requestData(true);
        }
    };

    /**
     * 加载更多数据
     */
    private boolean loadMoreData() {
        if (currentPage < totalPageNum) {
            currentPage++;
            requestData(false);
        } else {
            mPullToListView.stopLoadMore();
            mPullToListView.setPullLoadEnable(false);
            showToastLong(getApplicationContext().getString(
                    R.string.the_last_page));
        }
        return false;
    }

    @Override
    public void onRefresh() {
        refreshCount++;
        if (refreshCount > 5) {
            mPullToListView.stopRefresh();
            showToast(getString(R.string.refresh_must_later));
            refreshCount = 0;
            return;
        }
        currentPage = 1;
        dataWorker(true);
    }

    @Override
    public void onLoadMore() {
        long curTime = System.currentTimeMillis();
        if (curTime - lastLoadTime < 1000) {
            mPullToListView.stopLoadMore();
            showToast(getString(R.string.loadmore_must_later));
            return;
        }
        lastLoadTime = curTime;
        dataWorker(false);
    }

    /**
     * 加载数据data
     *
     * @param onRefresh 是否刷新
     */
    private void dataWorker(boolean onRefresh) {
        boolean result = onRefresh ? requestData(false) : loadMoreData();
        DRMLog.i("refresh is " + result);
    }

    private void showEmptyView(String tips) {
        ViewUtil.showWidget(emptyView);
        if (!TextUtils.isEmpty(tips)) emptyText.setText(tips);
    }

    private void hideEmptyView() {
        ViewUtil.hideWidget(emptyView);
    }

//    private void checkVersion() {
//        if (!CommonUtil.isNetConnect(this)) return;
//
//        XMLParserVersionManager.getInstance().checkUpdate(this,
//                new OnCheckResultListener() {
//                    @Override
//                    public void onResult(boolean hasNewVersion, Object result) {
//                        if (hasNewVersion) {
//                            final VersionBean o = (VersionBean) result;
//                            mHandler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ViewUtil.showUpdateDialog(
//                                            MainActivity.this, o, !isStop);
//                                }
//                            }, 2000);
//                            //dotView.setVisibility(View.VISIBLE);
//                        }
//                    }
//                });
//    }

    /*
     * 强制更新检测，验证App版本号
     */
//    private void forceUpdate() {
//        if (!CommonUtil.isNetConnect(this))
//            return;
//        if (isForceUpdate)
//            return;
//        isForceUpdate = true;
//        Bundle params = new Bundle();
//        params.putString("application_name", DrmPat.APP_FULLNAME);
//        params.putString("app_version", CommonUtil.getAppVersionName(this));
//        HttpEngine.post(APIUtil.getForceUpdateUrl(), params, new Callback
// .CommonCallback<String>() {
//            @Override
//            public void onSuccess(String s) {
//                DRMLog.d("forceUpdate: " + s);
//                if (isStop) return;
//                BaseModel model = JSON.parseObject(s, BaseModel.class);
//                if (model != null && Code._16002.equals(model.getCode())) {
//                    ViewUtil.DialogCallBackPat callBack = new ViewUtil.DialogCallBackPat() {
//                        @Override
//                        public void onCancel() {
//                            android.os.Process.killProcess(android.os.Process.myPid());
//                            System.exit(1);
//                        }
//
//                        @Override
//                        public void onConfirm() {
//                            ActicityManager.getInstance().remove(MainActivity.this);
//                            MainActivity.this.finish();
//                            AppUpdateService.openAppUpdateService(MainActivity.this, SZConfig
//                                    .APK_NEW_URL, "-new");
//                        }
//                    };
//                    PromptMsgManager proMsg = new PromptMsgManager.Builder()
//                            .setPositiveText(getString(R.string.update_app_now))
//                            .setNegativeText(getString(R.string.menu_exit))
//                            .setDialogCallback(callBack)
//                            .create();
//                    proMsg.show(MainActivity.this, Code._16002);
//                }
//            }
//
//            @Override
//            public void onError(Throwable throwable, boolean b) {
//                DRMLog.e("onError: " + throwable.getMessage());
//            }
//
//            @Override
//            public void onCancelled(CancelledException e) {
//            }
//
//            @Override
//            public void onFinished() {
//                isForceUpdate = false;
//            }
//        });
//    }


}
