package cn.com.pyc.drm.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import org.xutils.common.Callback;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.pyc.drm.R;
import cn.com.pyc.drm.adapter.MyContentAdapter;
import cn.com.pyc.drm.adapter.MyNetContentAdapter;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.ProductInfo;
import cn.com.pyc.drm.model.ProductListModel;
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
import cn.com.pyc.drm.widget.IconCenterTextView;
import cn.com.pyc.drm.widget.impl.OnRecyclerViewItemClickListener;
import cn.com.pyc.drm.widget.xrecyclerview.XRecyclerView;
import de.greenrobot.event.EventBus;

/**
 * 我的内容
 *
 * @author hudaqiang, update 20170818
 */
public class MyContentActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.amc_search_text)
    IconCenterTextView mSearchText;
    @BindView(R.id.un_image_close)
    HighlightImageView mUnImageClose;
    //@BindView(R.id.un_image_arrow)
    //ImageView mUnImageArrow;
    @BindView(R.id.un_net_layout)
    RelativeLayout mUnNetLayout;
    @BindView(R.id.amc_top_menu_all_img)
    ImageView mTopMenuAllImg;
    @BindView(R.id.amc_top_menu_all_text)
    TextView mTopMenuAllText;
    @BindView(R.id.amc_top_menu_all)
    RelativeLayout mTopMenuAll;
    @BindView(R.id.amc_top_menu_doc_img)
    ImageView mTopMenuDocImg;
    @BindView(R.id.amc_top_menu_doc_text)
    TextView mTopMenuDocText;
    @BindView(R.id.amc_top_menu_doc)
    RelativeLayout mTopMenuDoc;
    @BindView(R.id.amc_top_menu_audio_img)
    ImageView mTopMenuAudioImg;
    @BindView(R.id.amc_top_menu_audio_text)
    TextView mTopMenuAudioText;
    @BindView(R.id.amc_top_menu_audio)
    RelativeLayout mTopMenuAudio;
    @BindView(R.id.amc_top_menu_video_img)
    ImageView mTopMenuVideoImg;
    @BindView(R.id.amc_top_menu_video_text)
    TextView mTopMenuVideoText;
    @BindView(R.id.amc_top_menu_video)
    RelativeLayout mTopMenuVideo;
    @BindView(R.id.amc_lin_top_menu)
    LinearLayout mTopMenu;
    @BindView(R.id.empty_image)
    HighlightImageView mEmptyImage;
    @BindView(R.id.empty_text)
    TextView mEmptyText;
    @BindView(R.id.empty_layout)
    LinearLayout mEmptyLayout;
    @BindView(R.id.amc_xrv_my_local_content)
    XRecyclerView mLocalRecyclerView;
    @BindView(R.id.amc_xrv_my_net_content)
    XRecyclerView mNetRecyclerView;

    private static final String TAG = "MyContent";
    private static final String ALL = "0";
    private static final String DOC = "1";
    private static final String AUDIO = "2";
    private static final String VIDEO = "3";

    private static final int MSG_LOCAL_DATA = 0x01; //本地数据
    private static final int MSG_NET_CHANGE = 0x02; //网络切换
    private static final int MSG_LOAD_DATA = 0x03;  //加载数据

    private boolean isLoading = false;// 正在请求网络
    private boolean isFinding = false;// 本地正在加载
    private MyNetContentAdapter mNetAdapter;
    private MyContentAdapter mLocalAdapter;
    private Callback.Cancelable mDataCancelable;
    private NetworkChangedReceiver mNetReceiver;
    private ExecHandler mHandler = new ExecHandler(this);


    //0=全部；1=文档；2=音频；3=视频
    private String category;
    private int currentPage = 1;
    private int totalPageNum = 1;
    private int DOC_SUM = 0;     //记录文档数据
    private int AUDIO_SUM = 0;    //记录音频数据
    private int VIDEO_SUM = 0;    //记录视频数据


    private static class ExecHandler extends Handler {
        private WeakReference<MyContentActivity> reference;

        private ExecHandler(MyContentActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MyContentActivity activity = reference.get();
            if (activity == null) return;
            switch (msg.what) {
                case MSG_NET_CHANGE:
                    if (!activity.mHandler.hasMessages(MSG_LOAD_DATA)) {
                        activity.changeNetwork();
                    }
                    break;
                case MSG_LOAD_DATA:
                    if (CommonUtil.isNetConnect(activity)) {
                        activity.requestData(true);
                    } else {
                        activity.requestLocalData(true);
                    }
                    break;
                case MSG_LOCAL_DATA:
                    activity.isFinding = false;
                    Bundle bundle = msg.getData();
                    @SuppressWarnings("unchecked")
                    List<Album> albumsLocals = (List<Album>) bundle.getSerializable("album_list");
                    activity.hideLoading();
                    activity.initLocalData(albumsLocals);
                    break;
            }
        }
    }

    private void initLocalData(List<Album> albumList) {
        if (mLocalAdapter != null) {
            mLocalAdapter.getAlbums().clear();
            mLocalAdapter = null;
        }
        if (albumList == null || albumList.isEmpty()) {
            ViewUtil.hideWidget(mLocalRecyclerView);
            ViewUtil.hideWidget(mTopMenu);
            showEmptyView(getString(R.string.offline_data_empty));
        } else {
            mLocalAdapter = new MyContentAdapter(this, albumList);
            mLocalRecyclerView.setAdapter(mLocalAdapter);
            mLocalAdapter.setOnRecyclerViewItemClickListener(new OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Album a = mLocalAdapter.getAlbum(position);
                    // 打开已经下载的内容
                    OpenPageUtil.openFileListPage(MyContentActivity.this,
                            a.getMyproduct_id(), a.getName(), a.getCategory(),
                            a.getPicture(), a.getAuthor(), a.getPicture_ratio());
                }
            });
        }
        mLocalRecyclerView.refreshComplete();
    }

    private static class LoadLocalDataThread implements Runnable {
        public WeakReference<MyContentActivity> reference;

        private LoadLocalDataThread(MyContentActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            if (reference == null) return;
            MyContentActivity activity = reference.get();
            if (activity == null) return;
            // 获取本地所有的专辑.
            List<Album> albums = AlbumDAOImpl.getInstance().findAll(Album.class, "DESC");
            // 分集下载文件后，保存的专辑可能有重复，去重复。
            List<Album> albums_ = Util_.wipeRepeatAlbumData(albums);
            List<Album> target = activity.wipeAlbum(albums_);
            Message msg = Message.obtain();
            msg.what = MSG_LOCAL_DATA;
            Bundle data = new Bundle();
            data.putSerializable("album_list", (Serializable) target);
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

    //清理缓存数据后，重新加载
    private void loadingAfterClear() {
        hideEmptyView();
        ViewUtil.hideWidget(mLocalRecyclerView);
        ViewUtil.hideWidget(mNetRecyclerView);
        ViewUtil.hideWidget(mUnNetLayout);

        if (CommonUtil.isNetConnect(this)) {
            requestData(false);
        } else {
            requestLocalData(false);
        }
    }


    // 注销用户，停止所有下载并且关闭当前主页面。
    private void loginOut4CloseActivity() {
        if (mNetAdapter != null && mNetAdapter.getInfos() != null) {
            mNetAdapter.getInfos().clear();
            mNetAdapter = null;
        }
        finish();
    }

    public void onEventMainThread(ConductUIEvent event) {
        if (event.getType() == BaseEvent.Type.UI_HOME_TAB_1) {
            if (event.isNet() && mNetRecyclerView != null) {
                //requestData(false);
                mNetRecyclerView.refresh();
            }
        }
    }


    //TODO:
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_content);
        ButterKnife.bind(this);
        getValue();
        initView();
        loadData();
        registerBroadcast();
    }

    @Override
    protected void getValue() {
        ActicityManager.getInstance().add(this);
        EventBus.getDefault().register(this);
        category = ALL;
        mTopMenuAllImg.setSelected(true);
        mTopMenuAllText.setSelected(true);
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DRMUtil.BROADCAST_CLOSE_ACTIVITY);
        filter.addAction(DRMUtil.BROADCAST_CLEAR_DOWNLOADED);
        filter.addAction(DRMUtil.BROADCAST_CLEAR_DOWNLOADED_ALBUM);
        registerReceiver(clearReceive, filter);

        mNetReceiver = new NetworkChangedReceiver(this) {
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

    @Override
    protected void initView() {
        ViewUtil.hideWidget(mUnNetLayout);// 隐藏无网络提示
        ViewUtil.hideWidget(mEmptyLayout);// 隐藏暂无数据提示
        ViewUtil.hideWidget(mLocalRecyclerView);//隐藏本地List

        mUnNetLayout.setOnClickListener(this);
        mUnImageClose.setOnClickListener(this);
        mSearchText.setOnClickListener(this);
        mTopMenuAll.setOnClickListener(this);
        mTopMenuAudio.setOnClickListener(this);
        mTopMenuDoc.setOnClickListener(this);
        mTopMenuVideo.setOnClickListener(this);

        //本地
        mLocalRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, false));
        mLocalRecyclerView.setPullRefreshEnabled(true);
        mLocalRecyclerView.setLoadingMoreEnabled(false);
        mLocalRecyclerView.setHasFixedSize(true);
        mLocalRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //网络
        mNetRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, false));
        mNetRecyclerView.setPullRefreshEnabled(true);
        mNetRecyclerView.setLoadingMoreEnabled(true);
        mNetRecyclerView.setHasFixedSize(true);
        mNetRecyclerView.setActivated(true);
        mNetRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mNetRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                resetData(false);
                requestData(false);
            }

            @Override
            public void onLoadMore() {
                if (currentPage < totalPageNum) {
                    currentPage++;
                    requestData(false);
                } else {
                    mNetRecyclerView.setNoMore(true);
                    //showToast(getApplicationContext().getString(
                    //        R.string.the_last_page));
                }
            }
        });
        mLocalRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                resetData(false);
                requestLocalData(false);
            }

            @Override
            public void onLoadMore() {
            }
        });
    }

    @Override
    protected void loadData() {
        //预防网络切换时重复加载数据
        mHandler.sendEmptyMessage(MSG_LOAD_DATA);
    }

    private void showEmptyView(String tips) {
        ViewUtil.showWidget(mEmptyLayout);
        if (!TextUtils.isEmpty(tips)) {
            mEmptyText.setText(tips);
        }
    }

    private void hideEmptyView() {
        ViewUtil.hideWidget(mEmptyLayout);
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
        EventBus.getDefault().unregister(this);
        ActicityManager.getInstance().remove(this);
        ExecutorManager.getInstance().shutdownNow();
        mHandler.removeCallbacksAndMessages(null);
        mNetReceiver.abortReceiver(this);
        unregisterReceiver(clearReceive);
        HttpEngine.cancelHttp(mDataCancelable);
    }

    @Override
    public void onBackPressed() {
        ViewUtil.showContentDialog(this);
    }

    private void changeNetwork() {
        resetData(true);
        if (CommonUtil.isNetConnect(this)) {
            hideEmptyView();
            ViewUtil.hideWidget(mLocalRecyclerView);
            ViewUtil.hideWidget(mUnNetLayout);
            requestData(true);
        } else {
            hideEmptyView();
            ViewUtil.hideWidget(mNetRecyclerView);
            ViewUtil.showWidget(mUnNetLayout);
            requestLocalData(true);
        }
    }

    private void resetData(boolean clearData) {
        currentPage = 1;
        DOC_SUM = 0;
        AUDIO_SUM = 0;
        VIDEO_SUM = 0;
        if (clearData) {
            if (mNetAdapter != null && mNetAdapter.getInfos() != null) {
                mNetAdapter.getInfos().clear();
                mNetAdapter.notifyDataSetChanged();
                mNetAdapter = null;
            }
            if (mLocalAdapter != null && mLocalAdapter.getAlbums() != null) {
                mLocalAdapter.getAlbums().clear();
                mLocalAdapter.notifyDataSetChanged();
                mLocalAdapter = null;
            }
        }
    }

    /**
     * 本地数据
     */
    private boolean requestLocalData(boolean showLoading) {
        if (isFinding) return false;
        isFinding = true;
        ViewUtil.hideWidget(mNetRecyclerView);
        ViewUtil.showWidget(mLocalRecyclerView);
        if (showLoading) showLoading();
        ExecutorManager.getInstance().execute(new LoadLocalDataThread(this));
        return false;
    }


    private void resetTopMenuStatus() {
        mTopMenuAllImg.setSelected(false);
        mTopMenuAllText.setSelected(false);

        mTopMenuDocImg.setSelected(false);
        mTopMenuDocText.setSelected(false);

        mTopMenuAudioImg.setSelected(false);
        mTopMenuAudioText.setSelected(false);

        mTopMenuVideoImg.setSelected(false);
        mTopMenuVideoText.setSelected(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.un_net_layout) {
            OpenPageUtil.openWifiSetting(this);
        } else if (id == R.id.un_image_close) {
            mUnNetLayout.startAnimation(AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_out));
            ViewUtil.hideWidget(mUnNetLayout);
        } else if (id == R.id.amc_search_text) {
            openActivity(SearchProductActivity.class);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            resetTopMenuStatus();
            if (id == R.id.amc_top_menu_all) {
                mTopMenuAllImg.setSelected(true);
                mTopMenuAllText.setSelected(true);
                if (ALL.equals(category)) return;
                category = ALL;
            } else if (id == R.id.amc_top_menu_doc) {
                mTopMenuDocImg.setSelected(true);
                mTopMenuDocText.setSelected(true);
                if (DOC.equals(category)) return;
                category = DOC;
            } else if (id == R.id.amc_top_menu_audio) {
                mTopMenuAudioImg.setSelected(true);
                mTopMenuAudioText.setSelected(true);
                if (AUDIO.equals(category)) return;
                category = AUDIO;
            } else if (id == R.id.amc_top_menu_video) {
                mTopMenuVideoImg.setSelected(true);
                mTopMenuVideoText.setSelected(true);
                if (VIDEO.equals(category)) return;
                category = VIDEO;
            }
            if (CommonUtil.isNetConnect(this)) {
                mNetRecyclerView.refresh();
            } else {
                mLocalRecyclerView.refresh();
            }
        }
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
        ViewUtil.showWidget(mNetRecyclerView);
        ViewUtil.hideWidget(mLocalRecyclerView);
        Bundle bundle = new Bundle();
        bundle.putString("category", category);
        bundle.putInt("page", currentPage);
        bundle.putString("token", Constant.getToken());
        bundle.putString("username", Constant.getName());
        bundle.putString("application_name", DrmPat.APP_FULLNAME);
        bundle.putString("app_version", CommonUtil.getAppVersionName(this));
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
                        hideLoading();
                        mNetRecyclerView.loadMoreComplete();
                        mNetRecyclerView.refreshComplete();
                        mHandler.removeMessages(MSG_LOAD_DATA);
                    }

                    @Override
                    public void onSuccess(String mJsonString) {
                        DRMLog.d(TAG, "requestJSON:" + mJsonString);
                        ProductListModel o = JSON.parseObject(mJsonString, ProductListModel.class);
                        if (o.isSuccess()) {
                            getDataSuccess(o);
                        } else {
                            if (o.getMsg() != null && o.getMsg().contains("验证")) {
                                // 发送广播通知主界面，停止下载，清除数据，关闭主界面。
                                sendBroadcast(new Intent(DRMUtil.BROADCAST_CLOSE_ACTIVITY));
                                Util_.repeatLogin(MyContentActivity.this);
                            }
                            getDataFailed(R.string.getdata_failed);
                        }
                    }
                });
        return true;
    }

    private void getDataSuccess(ProductListModel o) {
        if (o.getData() == null) {
            ViewUtil.hideWidget(mNetRecyclerView);
            showEmptyView(getString(R.string.getdata_failed));
            return;
        }
        ProductListModel.ProductListInfo oo = o.getData();
        List<ProductInfo> oNetList = oo.getItems();
        totalPageNum = oo.getTotalPageNum();
        if (oNetList == null || oNetList.isEmpty()) {
            ViewUtil.hideWidget(mNetRecyclerView);
            showEmptyView(getString(R.string.load_data_null_again));
            mEmptyImage.setOnClickListener(againClick);
            return;
        }
        DRMLog.d(TAG, "totalPage = " + totalPageNum);
        DRMLog.d(TAG, "dataSize = " + oNetList.size());
        List<ProductInfo> target = wipeCategory(oNetList);
        if (target.isEmpty()) {
            showEmptyView(null);
            return;
        }
        //page=1,刷新页面, 清空之前数据。
        if (currentPage == 1 && mNetAdapter != null) {
            mNetAdapter.getInfos().clear();
            mNetAdapter.notifyDataSetChanged();
            mNetAdapter = null;
        }
        if (mNetAdapter == null) {
            mNetAdapter = new MyNetContentAdapter(this, target);
            mNetRecyclerView.setAdapter(mNetAdapter);
        } else {
            int posStart = mNetAdapter.getInfos().size();
            mNetAdapter.addLastInfos(target);
            mNetAdapter.notifyItemRangeChanged((posStart + 1), target.size());
        }
        ViewUtil.showWidget(mNetRecyclerView);
        //mNetRecyclerView.setLoadingMoreEnabled(currentPage < totalPageNum);
        initNetRecyclerViewListener();
    }

    private void initNetRecyclerViewListener() {
        if (mNetAdapter == null) return;
        mNetAdapter.setOnRecyclerViewItemClickListener(new OnRecyclerViewItemClickListener
                () {
            @Override
            public void onItemClick(View view, int position) {
                ProductInfo productInfo = mNetAdapter.getProduct(position);
                //打开列表
                OpenPageUtil.openFileListPage(MyContentActivity.this, productInfo);
                //当前选择的专辑进入
                ShareMomentEngine.setSelectProId(productInfo.getProId());
            }
        });
    }

    //根据列表数据，计算类别
    private List<ProductInfo> wipeCategory(List<ProductInfo> oNetList) {
        //判断卖家是否禁用，如已禁用且未下载，清除此条数据不显示。
        Iterator<ProductInfo> Pro_it = oNetList.iterator();
        while (Pro_it.hasNext()) {
            ProductInfo proInfo = Pro_it.next();
            Album album = AlbumDAOImpl.getInstance().findAlbumByMyProId(proInfo.getMyProId());
            if (!proInfo.isValid() && album == null) {
                Pro_it.remove();
            }
            //根据种类区分不同数据类型，放入不同集合中
            String category_ = proInfo.getCategory();
            if (DrmPat.BOOK.equals(category_)) {
                DOC_SUM++;
            } else if (DrmPat.MUSIC.equals(category_)) {
                AUDIO_SUM++;
            } else if (DrmPat.VIDEO.equals(category_)) {
                VIDEO_SUM++;
            }
        }
        showTopMenuByNum();
        return oNetList;
    }

    //根据本地列表数据，计算类别
    private List<Album> wipeAlbum(List<Album> albumList) {
        Iterator<Album> album_it = albumList.iterator();
        List<Album> docList = new ArrayList<>();
        List<Album> audioList = new ArrayList<>();
        List<Album> videoList = new ArrayList<>();
        while (album_it.hasNext()) {
            Album album = album_it.next();
            //根据category分类数据，放到不同集合中
            String category_ = album.getCategory();
            if (DrmPat.BOOK.equals(category_)) {
                DOC_SUM++;
                docList.add(album);
            } else if (DrmPat.MUSIC.equals(category_)) {
                AUDIO_SUM++;
                audioList.add(album);
            } else if (DrmPat.VIDEO.equals(category_)) {
                VIDEO_SUM++;
                videoList.add(album);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTopMenuByNum();
            }
        });
        if (DOC.equals(category)) {
            return docList;
        }
        if (AUDIO.equals(category)) {
            return audioList;
        }
        if (VIDEO.equals(category)) {
            return videoList;
        }
        return albumList;
    }

    //根据各类别文件个数，是否显示按钮
    private void showTopMenuByNum() {
        DRMLog.e("doc=" + DOC_SUM + "; audio=" + AUDIO_SUM + ";video=" + VIDEO_SUM);
        if (ALL.equals(category)) { //全部类型时计算
            // 5 = 3 + 0 + 2;
            int sum = DOC_SUM + AUDIO_SUM + VIDEO_SUM;
            if (sum == DOC_SUM || sum == AUDIO_SUM || sum == VIDEO_SUM) {
                //只有一种类型，不显示menu
                ViewUtil.hideWidget(mTopMenu);
            } else {
                // 有多种类型,显示menu，且显示全部按钮
                ViewUtil.showWidget(mTopMenu);
                ViewUtil.showWidget(mTopMenuAll);
                //文档
                mTopMenuDoc.setVisibility(DOC_SUM > 0 ? View.VISIBLE : View.GONE);
                //音频
                mTopMenuAudio.setVisibility(AUDIO_SUM > 0 ? View.VISIBLE : View.GONE);
                //视频
                mTopMenuVideo.setVisibility(VIDEO_SUM > 0 ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void getDataFailed(int strId) {
        //隐藏netListView,避免netListView接收滑动崩溃
        ViewUtil.hideWidget(mNetRecyclerView);
        MusicHelp.release(this);
        showEmptyView(getString(strId));
        mEmptyImage.setOnClickListener(againClick);
    }

    View.OnClickListener againClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideEmptyView();
            requestData(true);
        }
    };


}

