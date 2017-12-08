package cn.com.pyc.drm.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.adapter.SearchRcvAdapter;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.ConductUIEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.ProductInfo;
import cn.com.pyc.drm.model.SearchResultModel;
import cn.com.pyc.drm.model.SearchResultModel.MyProduct;
import cn.com.pyc.drm.model.SearchResultModel.RecommendProduct;
import cn.com.pyc.drm.model.SearchResultModel.SearchInfo;
import cn.com.pyc.drm.model.SearchResultModel.SearchProduct;
import cn.com.pyc.drm.model.SearchResultModel.SearchResult;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.UIHelper;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.widget.SearchEditText;
import cn.com.pyc.drm.widget.SplashTextView;
import cn.com.pyc.drm.widget.impl.OnRecyclerViewItemClickListener;
import cn.com.pyc.drm.widget.xrecyclerview.XRecyclerView;
import de.greenrobot.event.EventBus;

/**
 * 搜索（我的内容）
 *
 * @author hudq
 */
public class SearchProductActivity extends BaseActivity implements
        OnClickListener {
    private static final String TAG = "SearchProductUI";
    //private SwipeToLoadLayout mSwipeToLoadLayout;
    private XRecyclerView mRecyclerView;
    private SearchEditText searchEditText;
    private TextView searchCancelText;
    private SplashTextView searchTipsText;
    private TextView searchEmptyTipsText;
    //private ImageView backImg;

    private boolean isSearching = false;
    private int currentPage = 1;
    private int totalPageNum = 1;
    private String accessLogId;
    private List<SearchResult> tempMyLists;
    private String keyWords;

    private InputMethodManager imManager;
    private Cancelable searchCancelable;
    private SearchRcvAdapter adapter;

    //通知切换到‘我的内容’（BrowserUI发送通知消息），从搜索进入，则结束当前页面
    public void onEventMainThread(ConductUIEvent event) {
        if (event.getType() == BaseEvent.Type.UI_HOME_TAB_1) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);
        getValue();
        initView();
        loadData();
    }

    @Override
    protected void initView() {
        UIHelper.showTintStatusBar(this, getResources().getDrawable(R.drawable.bg));
        //((TextView) findViewById(R.id.title_tv)).setText(getString(R.string.search_title));
        searchEditText = (SearchEditText) findViewById(R.id.search_edittext);
        searchCancelText = (TextView) findViewById(R.id.search_canceltext);
        searchEditText.requestFocus();
        searchEditText.setCancelTextView(searchCancelText);
        searchTipsText = (SplashTextView) findViewById(R.id.search_tips);
        searchEmptyTipsText = (TextView) findViewById(R.id.search_empty_tip);

        //mSwipeToLoadLayout = (SwipeToLoadLayout) findViewById(R.id.swipeToLoadLayout);
        mRecyclerView = (XRecyclerView) findViewById(R.id.swipe_target);
//        StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(2,
//                StaggeredGridLayoutManager.VERTICAL);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setPullRefreshEnabled(false);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        searchCancelText.setOnClickListener(this);
        //backImg = (ImageView) findViewById(R.id.back_img);
        //backImg.setOnClickListener(this);

        initListener();
    }

    private void initListener() {
//        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            boolean isSlidingToLast = false;
//
//            private int getMaxElem(int[] arr) {
//                int size = arr.length;
//                int maxVal = Integer.MIN_VALUE;
//                for (int i = 0; i < size; i++) {
//                    if (arr[i] > maxVal) maxVal = arr[i];
//                }
//                return maxVal;
//            }
//
//            private void startLoadMore(RecyclerView recyclerView,
//                                       int lastVisiblePos, int totalItemCount) {
//                DRMLog.d(TAG, "lastVisiblePos = " + lastVisiblePos);
//                DRMLog.d(TAG, "totalItemCount = " + totalItemCount);
//
//                // 判断是否滚动到底部
//                if (lastVisiblePos == (totalItemCount - 1) && isSlidingToLast) // 加载更多功能的代码
//                {
//                    DRMLog.v("loadMoreData!!!");
//                    recyclerView.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mSwipeToLoadLayout.setLoadMoreEnabled(true);
//                            mSwipeToLoadLayout.setLoadingMore(true);
//                        }
//                    }, 10);
//                }
//            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView,
//                                             int newState) {
//
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager)
//                            recyclerView.getLayoutManager();
//                    if (manager == null) manager = mLayoutManager;
//                    try {
//                        // 获取最后一个完全显示的ItemPosition
//                        int[] lastPos = manager.findLastVisibleItemPositions(new
//                                int[manager.getSpanCount()]);
//                        int lastVisiblePos = getMaxElem(lastPos);
//                        int totalItemCount = manager.getItemCount();
//                        startLoadMore(recyclerView, lastVisiblePos, totalItemCount);
//                    } catch (NullPointerException e) {
//                        e.printStackTrace();
//                        // workaround for RecyclerView bug. assume not laid out, so no items
// visible
//                    }
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                // dx用来判断横向滑动方向，dy用来判断纵向滑动方向,大于0表示，正在向下滚动,小于等于0 表示停止或向上滚动
//                isSlidingToLast = dy > 0;
//            }
//        });
        searchEditText.setOnSearchClickListener(new SearchEditText.OnSearchClickListener() {
            @Override
            public void onSearchClick(View view) {
                keyWords = ((EditText) view).getText().toString().trim();
                if (TextUtils.isEmpty(keyWords)) {
                    showToast(getString(R.string.search_input_keyword));
                    return;
                }
                if (adapter != null) {
                    adapter.getContends().clear();
                    adapter.notifyDataSetChanged();
                    adapter = null;
                }
                ViewUtil.inVisibleWidget(mRecyclerView);
                search(keyWords, currentPage = 1);
            }
        });
//        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(
//                this, mRecyclerView,
//                new RecyclerViewClickListener.onItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        DRMLog.d(TAG, "position: " + position);
////                        if (mSwipeToLoadLayout.isLoadingMore()) return;
//                        if (adapter == null) return;
//                        if (position < 2) return; //XRecyclerView至少2个header
//                        openPage(adapter.getResult(position - 1));
//                    }
//                }));

//        mSwipeToLoadLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
//            @Override
//            public void onLoadMore() {
//                if (currentPage < totalPageNum) {
//                    search(keyWords, ++currentPage);
//                } else {
//                    mSwipeToLoadLayout.setLoadingMore(false);
//                    mSwipeToLoadLayout.setLoadMoreEnabled(false);
//                    showToast(getApplicationContext().getString(
//                            R.string.the_last_page));
//                }
//            }
//        });
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadMore() {
                if (currentPage < totalPageNum) {
                    search(keyWords, ++currentPage);
                } else {
//                    mSwipeToLoadLayout.setLoadingMore(false);
//                    mSwipeToLoadLayout.setLoadMoreEnabled(false);
                    mRecyclerView.setNoMore(true);
                    showToast(getApplicationContext().getString(
                            R.string.the_last_page));
                }
            }
        });
    }

    /**
     * 搜索接口
     *
     * @param keyWord 关键字
     * @param page    页数
     */
    protected void search(String keyWord, int page) {
        if (isSearching) return;
        isSearching = true;
        showTipsText(getString(R.string.searching_));
        Bundle bundle = new Bundle();
        bundle.putString("keyword", keyWord);
        bundle.putInt("page", page);
        bundle.putString("token", Constant.getToken());
        bundle.putString("username", Constant.getName());
        bundle.putString("IMEI", Constant.IMEI);
        bundle.putString("application_name", DrmPat.APP_FULLNAME);

        searchCancelable = HttpEngine.post(APIUtil.getProductSearch(), bundle,
                new CommonCallback<String>() {
                    @Override
                    public void onSuccess(String arg0) {
                        DRMLog.d(TAG, "onSuccess: " + arg0);
                        parserData(arg0);
                    }

                    @Override
                    public void onFinished() {
                        isSearching = false;
//                        searchEditText.setText(null);
//                        mSwipeToLoadLayout.setLoadingMore(false);
                        mRecyclerView.loadMoreComplete();
                    }

                    @Override
                    public void onError(Throwable arg0, boolean arg1) {
                        DRMLog.e(arg0.getMessage());
                        showTipsText(getString(R.string.connect_server_failed));
                    }

                    @Override
                    public void onCancelled(CancelledException arg0) {
                        DRMLog.d(TAG, "onCancelled");
                    }
                });
    }

    private void parserData(String arg0) {
        SearchResultModel model = JSON.parseObject(arg0,
                SearchResultModel.class);
        if (model.isSuccess()) {
            SearchInfo info = model.getData();
            if (info == null) {
                showTipsText(getString(R.string.load_server_failed));
                return;
            }
            MyProduct myProduct = info.getMyProducts();
            SearchProduct searchProduct = info.getSearchProducts();
            RecommendProduct recommendProduct = info.getRecommendProducts();
            accessLogId = info.getAccessLogId();

            if (myProduct == null || searchProduct == null
                    || recommendProduct == null) {
                showTipsText(getString(R.string.search_empty));
                return;
            }

            final List<SearchResult> myList = myProduct.getItems();
            List<SearchResult> searchList = searchProduct.getItems();
            List<SearchResult> recommendList = recommendProduct.getItems();
            final boolean myEmpty = (myList == null || myList.isEmpty());
            boolean searchEmpty = (searchList == null || searchList.isEmpty());
            boolean recommendEmpty = (recommendList == null || recommendList
                    .isEmpty());
            if (myEmpty && searchEmpty) {
                showTipsText(getString(R.string.search_empty));
                searchEmptyTipsText.setVisibility(View.VISIBLE);
                //  return;
            }

            DRMLog.v("buy：" + myList.size());
            DRMLog.v("search：" + searchList.size());
            DRMLog.v("recommend：" + recommendList.size());
            totalPageNum = searchProduct.getTotalPageNum();
            if (currentPage == 1) tempMyLists = myList;
            if (searchEmpty) {
                totalPageNum = recommendProduct.getTotalPageNum();
                searchList = recommendList;
            }
            searchList = wipeOffRepeatData(searchList, tempMyLists);
            DRMLog.v("temp mylist：" + tempMyLists.size());
            DRMLog.v("wipe search：" + searchList.size());
            if (adapter == null || currentPage == 1) {
                DRMLog.d(TAG, "newData.");
                adapter = new SearchRcvAdapter(this, myList, searchList);
                mRecyclerView.setAdapter(adapter);
            } else {
                DRMLog.d(TAG, "addData.");
                int posStart = adapter.getContends().size();
                adapter.addData(searchList);
                adapter.notifyItemRangeChanged(myEmpty ? (posStart + 1) : (posStart + 2),
                        searchList.size());
            }
            initClickItemListener();
            hideTipsText();
            ViewUtil.showWidget(mRecyclerView);
//            mSwipeToLoadLayout.setLoadingMore(false);
            DRMLog.i("currentPage = " + currentPage);
            DRMLog.i("totalPageNum = " + totalPageNum);
        } else {
            showTipsText(model.getMsg());
        }
    }

    //添加item点击事件
    private void initClickItemListener() {
        if (adapter == null) return;
        adapter.setOnRecyclerViewItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                DRMLog.d(TAG, "position: " + position);
                if (adapter == null) return;
                if (position < 1) return; //XRecyclerView至少1个header
                openPage(adapter.getResult(position));
            }
        });
    }

    private void openPage(SearchResult result) {
        if (result == null) return;
        DRMLog.w("product name: " + result.getProductName());
        DRMLog.w("search source: " + result.getSource());
        if (result.getSource() == DrmPat.BUYED) {
            ProductInfo o = new ProductInfo();
            o.setMyProId(result.getMyProId());
            o.setProductName(result.getProductName());
            o.setPicture_url(result.getPicture_url());
            o.setPicture_ratio(result.getPicture_ratio());
            o.setAuthors(result.getAuthors());
            o.setCategory(result.getCategory());
            OpenPageUtil.openFileListPage(this, o);
        } else {
            OpenPageUtil.openBrowserOfApp(this,
                    APIUtil.getProductDetailUrl(result.getProId(), accessLogId));
        }
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected void getValue() {
        EventBus.getDefault().register(this);
        imManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        finishUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.back_img:
//                hideKeyboard(backImg);
//                finishUI();
//                break;
            case R.id.search_canceltext:
                searchEditText.setText(null);
                hideKeyboard(searchCancelText);
                finishUI();
                break;
            default:
                break;
        }
    }

    private void finishUI() {
        HttpEngine.cancelHttp(searchCancelable);
//        if (mSwipeToLoadLayout.isLoadingMore())
//            mSwipeToLoadLayout.setLoadingMore(false);
        if (tempMyLists != null) tempMyLists.clear();
        tempMyLists = null;
        this.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * loading第一页时候显示提示
     *
     * @param tips
     */
    private void showTipsText(String tips) {
        if (currentPage > 1) return;

        ViewUtil.showWidget(searchTipsText);
        if (tips == null) tips = getString(R.string.load_data_null);
        searchTipsText.setText(tips);
    }

    private void hideTipsText() {
        searchTipsText.setText(null);
        searchTipsText.setAnimating(false);
        ViewUtil.hideWidget(searchTipsText);
    }

    private void hideKeyboard(View v) {
        if (imManager != null && imManager.isActive()) {
            imManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    /**
     * 去除搜索中已购买的重复数据
     *
     * @param targets 搜索源数据
     * @param sources 已购买源数据
     * @return
     */
    private List<SearchResult> wipeOffRepeatData(List<SearchResult> targets,
                                                 List<SearchResult> sources) {
        if (sources == null || sources.isEmpty()) return targets;

        List<SearchResult> results = new ArrayList<>(targets);
        final int size = sources.size();
        final int count = targets.size();
        for (int i = 0; i < size; i++) // 已购买
        {
            String srcId = sources.get(i).getProId();
            for (int j = 0; j < count; j++) // 搜索结果
            {
                SearchResult t = targets.get(j);
                if (srcId.equals(t.getProId())) // 相等则重复数据
                {
                    if (results.contains(t)) {
                        results.remove(t);
                    }
                }
            }
        }
        return results;
    }
}
