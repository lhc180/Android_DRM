package cn.com.pyc.drm.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.xutils.common.Callback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.pyc.drm.R;
import cn.com.pyc.drm.adapter.ListFileAdapter;
import cn.com.pyc.drm.adapter.ListFileLocalAdapter;
import cn.com.pyc.drm.bean.DrmFile;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.FileHandleEvent;
import cn.com.pyc.drm.bean.event.FileProgressStateEvent;
import cn.com.pyc.drm.bean.event.MusicCurrentPlayEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.common.MusicMode;
import cn.com.pyc.drm.db.manager.DownDataPatDBManager;
import cn.com.pyc.drm.model.FileData;
import cn.com.pyc.drm.model.FileVersionModel;
import cn.com.pyc.drm.model.FilesDataModel;
import cn.com.pyc.drm.model.ProductInfo;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.db.practice.AlbumContentDAOImpl;
import cn.com.pyc.drm.model.db.practice.AlbumDAOImpl;
import cn.com.pyc.drm.model.right.SZContent;
import cn.com.pyc.drm.receiver.DownloadPatReceiver;
import cn.com.pyc.drm.service.DownloadPatService;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.StringUtil;
import cn.com.pyc.drm.utils.SwipUtil;
import cn.com.pyc.drm.utils.Util_;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.DRMDBHelper;
import cn.com.pyc.drm.utils.help.DRMFileHelp;
import cn.com.pyc.drm.utils.help.DownloadHelp;
import cn.com.pyc.drm.utils.help.MusicHelp;
import cn.com.pyc.drm.utils.help.ProgressHelp;
import cn.com.pyc.drm.utils.help.UIHelper;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.com.pyc.drm.utils.manager.DownloadTaskManagerPat;
import cn.com.pyc.drm.utils.manager.ExecutorManager;
import cn.com.pyc.drm.utils.manager.HttpEngine;
import cn.com.pyc.drm.utils.manager.LrcEngine;
import cn.com.pyc.drm.utils.manager.PromptMsgManager;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.PullListView;
import cn.com.pyc.drm.widget.ToastShow;
import de.greenrobot.event.EventBus;

/**
 * 隐藏批量操作的功能
 * desc:   文件列表（新）       <br/>
 * author: hudaqiang       <br/>
 * create at 2017/7/13 11:40
 */
public class ListFileActivity extends BaseAssistActivity implements View.OnClickListener {


    @BindView(R.id.alf_back_img)
    HighlightImageView mBackImg;
    @BindView(R.id.alf_title_tv)
    TextView mTitleTv;
    @BindView(R.id.alf_fun_tv)
    TextView mFunTv;
    //@BindView(R.id.empty_image)
    //HighlightImageView mEmptyImage;
    @BindView(R.id.empty_text)
    TextView emptyTextView;
    @BindView(R.id.empty_layout)
    LinearLayout emptyView;
    @BindView(R.id.alf_files_listview)
    PullListView mListView;
    @BindView(R.id.alf_local_files_listview)
    SwipeMenuListView mLocalListView;
    @BindView(R.id.alf_batchDownload_tv)
    TextView mBatchDownloadTv;
    @BindView(R.id.alf_batch_tv)
    TextView mBatchTv;
    @BindView(R.id.alf_batchDelete_tv)
    TextView mBatchDeleteTv;
    @BindView(R.id.alf_bottom_layout)
    RelativeLayout mBottomLayout;

    private static final int DEFAULT_ITEM = 6;
    private static final String KEY_LOCAL_CONTENT = "local_contents";
    private static final int MSG_UPLOAD_ITEM = 0x301;
    private static final int MSG_LOAD_LOCAL = 0x303;
    private static final int MSG_CLEAR_ITEM = 0x305;
    private static final int MSG_CLEAR_LOCAL_ITEM = 0x307;
    private static final int MSG_DELETE_FILE_DATA = 0x309;

    private AlbumContentDAOImpl daoAcImpl;
    private MyOwnHandler handler = new MyOwnHandler(this);
    private volatile String myProductId;
    private String productName;
    private String category;
    private String productPic;
    private String productAuthor;
    private String productPicRatio;
    private String downloadFileId; //分享此刻需要下载的文件id;
    private boolean isValid;       //是否当前端有效

    private boolean onStop = false;
    private boolean isLoading = false;
    private boolean isMobileVNO = false;
    private int totalItemCount = 0;
    private int downloadItemCount = 0;
    private ListFileAdapter adapter;
    private ListFileLocalAdapter localAdapter;
    private Callback.Cancelable dataCancelable;
    private List<FileData> cacheDataList;   //加载的列表缓存数据

    private Set<String> sAllFileId = new LinkedHashSet<>(); // 存储文件id
    private Set<String> sDownloadSet = new LinkedHashSet<>(); // 存储已下载的文件id
    public static Set<String> sTaskIdSet; // 存储下载任务的id
    private LocalBroadcastManager mLocalBroadcastManager;


    private static class MyOwnHandler extends Handler {
        private WeakReference<ListFileActivity> reference;

        private MyOwnHandler(ListFileActivity context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (reference == null) return;
            ListFileActivity activity = reference.get();
            if (activity == null) return;
            switch (msg.what) {
                case MSG_LOAD_LOCAL: {          //加载本地数据
                    Bundle bundle = msg.getData();
                    List<DrmFile> drmFiles = bundle.getParcelableArrayList(KEY_LOCAL_CONTENT);
                    if (drmFiles == null || drmFiles.isEmpty()) {
                        activity.loadFailedSet(activity.getString(R.string.offline_data_empty));
                    } else {
                        activity.localAdapter = new ListFileLocalAdapter(activity, drmFiles);
                        activity.mLocalListView.setAdapter(activity.localAdapter);
                    }
                    activity.hideLoading();
                }
                break;
                case MSG_CLEAR_ITEM: {          //清除网络下item数据缓存
                    String fileId = (String) msg.obj;
                    activity.clearItem(fileId);
                    activity.againPlaying(true); //检查续播文件id是否存在，不存在不显示续播按钮
                }
                break;
                case MSG_CLEAR_LOCAL_ITEM: {    //清除本地item数据缓存
                    int position = (int) msg.obj;
                    activity.clearLocalItem(position);
                    activity.againPlaying(true);
                }
                break;
                case MSG_UPLOAD_ITEM: {         //更新下载文件
                    FileData data = (FileData) msg.obj;
                    activity.hideBgLoading();
                    activity.downloadFile(data);
                    activity.againPlaying(true);
                }
                break;
                case MSG_DELETE_FILE_DATA: {    //清除所选文件数据
                    activity.adapter.setShowCheckBox(false, false);
                    //activity.adapter.setList(activity.cacheDataList);
                    activity.adapter.setContentId(null);
                    activity.adapter.notifyDataSetChanged();
                    activity.showBatchCenterText(false, null);
                    activity.showBatchDownloadText(true);
                    activity.againPlaying(true);
                    activity.hideBgLoading();
                    ToastShow.getToast().showOk(activity, activity.getString(R.string
                            .delete_localfile_success));
                    if ((--activity.downloadItemCount) > 0) {
                        activity.showBatchDeleteText(true);
                    }
                }
                break;
            }
        }
    }

    private void clearItem(String fileId) {
        if (adapter != null) {
            adapter.clearItemCheckState(fileId);
            adapter.setContentId(null);
            adapter.notifyDataSetChanged();
            if (downloadItemCount > 0) {
                if ((--downloadItemCount) == 0) {
                    //全部清除，则也删除专辑
                    showBatchDownloadText(true);
                    showBatchDeleteText(false);
                    AlbumDAOImpl.getInstance().deleteAlbumByMyProId(myProductId);
                }
                sDownloadSet.remove(fileId);
            }
        }
        hideBgLoading();
        ToastShow.getToast().showOk(this, getString(R.string.delete_localfile_success));
    }

    private void clearLocalItem(int position) {
        if (localAdapter != null && (position) != -1) {
            localAdapter.notifyDataSetChanged();
            if (localAdapter.getList().isEmpty()) {
                Util_.setEmptyViews(mLocalListView, emptyView, getString(R.string
                        .connect_net_download));
                //缓存全部清理完，则清除专辑
                AlbumDAOImpl.getInstance().deleteAlbumByMyProId(myProductId);
                sendBroadcast(new Intent(DRMUtil.BROADCAST_CLEAR_DOWNLOADED_ALBUM));
            }
        }
        hideBgLoading();
        ToastShow.getToast().showOk(this, getString(R.string.delete_localfile_success));
    }

    // 清除Item本地资源文件
    private static class ClearItemDataThread implements Runnable {
        private WeakReference<ListFileActivity> reference;
        private String folderId;
        private String fileId;
        private String collectionId;
        private String lrcId;
        private int position;

        private ClearItemDataThread(ListFileActivity activity, String MyProId,
                                    String ItemId, String CollectionId,
                                    String LrcId, int Position) {
            reference = new WeakReference<>(activity);
            this.folderId = MyProId;
            this.fileId = ItemId;
            this.collectionId = CollectionId;
            this.lrcId = LrcId;
            this.position = Position;
        }

        @Override
        public void run() {
            if (reference.get() == null) return;
            ListFileActivity activity = reference.get();
            activity.deleteFileData(folderId, fileId, collectionId, lrcId);
            if (MusicMode.STATUS != MusicMode.Status.STOP && MusicHelp.isSameMusic(fileId)) {
                MusicHelp.release(activity);
            }
            Message message = Message.obtain();
            //position=-1:网络下的删除本地; 反之离线下的删除本地
            message.what = (position == -1) ? MSG_CLEAR_ITEM : MSG_CLEAR_LOCAL_ITEM;
            message.obj = (position == -1) ? fileId : position;
            activity.handler.sendMessageDelayed(message, 300);
        }
    }

    //清除对应文件的资源
    @Deprecated
    private static class ClearListFileDataThread implements Runnable {
        private WeakReference<ListFileActivity> reference;
        private List<FileData> data;

        private ClearListFileDataThread(ListFileActivity activity, List<FileData> data) {
            reference = new WeakReference<>(activity);
            this.data = data;
        }

        @Override
        public void run() {
            if (reference.get() == null) return;
            final ListFileActivity activity = reference.get();
            for (FileData o : data) {
                if (!activity.daoAcImpl.existAlbumContentById(o.getCollectionId())) {
                    DownDataPatDBManager.Builder().deleteByFileId(o.getItemId());
                    o.setTaskState(DownloadTaskManagerPat.INIT);
                    final FileData o_ = o;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //activity.adapter.notifyDataSetChanged();
                            activity.adapter.updateItemView(o_);
                        }
                    });
                    continue;
                }
                activity.adapter.clearItemCheckState(o.getItemId());
                activity.deleteFileData(o.getMyProId(), o.getItemId(), o.getCollectionId(), o
                        .getMusicLyric_id());
                if (MusicMode.STATUS != MusicMode.Status.STOP && MusicHelp.isSameMusic(o
                        .getItemId())) {
                    MusicHelp.release(activity);
                }
                if (activity.downloadItemCount > 0) {
                    if ((--activity.downloadItemCount) == 0) {
                        //全部清除，则也删除专辑
                        AlbumDAOImpl.getInstance().deleteAlbumByMyProId(activity.myProductId);
                    }
                }
                activity.sDownloadSet.remove(o.getItemId());
            }
            activity.handler.sendEmptyMessageDelayed(MSG_DELETE_FILE_DATA, 300);
        }
    }

    private static class LoadLocalContentThread implements Runnable {
        private WeakReference<ListFileActivity> reference;
        private String myProId;

        private LoadLocalContentThread(ListFileActivity activity, String myProId) {
            reference = new WeakReference<>(activity);
            this.myProId = myProId;
        }

        @Override
        public void run() {
            if (reference == null) return;
            ListFileActivity activity = reference.get();
            if (activity == null) return;
            List<AlbumContent> as = activity.daoAcImpl.findAlbumContentByMyProId(myProId);
            List<DrmFile> drmFiles = DRMFileHelp.convert2DrmFileList(as, activity.productPic);
            Message msg = Message.obtain();
            msg.what = MSG_LOAD_LOCAL;
            Bundle data = new Bundle();
            data.putParcelableArrayList(KEY_LOCAL_CONTENT, (ArrayList<? extends Parcelable>)
                    drmFiles);
            msg.setData(data);
            activity.handler.sendMessageDelayed(msg, 400);
        }
    }

    /**
     * 文件的更新下载
     */
    private static class UploadFileDataThread implements Runnable {
        private WeakReference<ListFileActivity> reference;
        private FileData data;

        private UploadFileDataThread(ListFileActivity activity, FileData data) {
            reference = new WeakReference<>(activity);
            this.data = data;
        }

        @Override
        public void run() {
            if (reference.get() == null) return;
            ListFileActivity activity = reference.get();
            AlbumContent ac = activity.daoAcImpl.findAlbumContentByCollectionId(data
                    .getCollectionId());
            if (ac != null) {      // 删除相关
                activity.deleteFileData(ac.getMyProId(), ac.getContent_id(),
                        ac.getCollectionId(), ac.getMusicLrcId());
            }
            //重新更新下载
            Message message = Message.obtain();
            message.obj = data;
            message.what = MSG_UPLOAD_ITEM;
            activity.handler.sendMessage(message);
        }
    }

    private DownloadPatReceiver receiver = new DownloadPatReceiver() {
        private Context mContext = ListFileActivity.this;

        @Override
        protected void updateProgress(FileData data, int progress,
                                      long currentSize, boolean isLastProgress) {
            if (adapter == null || sAllFileId == null) return;
            if (data.getTaskState() == DownloadTaskManagerPat.DOWNLOADING
                    && sAllFileId.contains(data.getItemId())) {
                //存在已下载的
                addTaskId(data.getItemId()); //进入UI,下载加入对应的taskId;
            }
            adapter.updateItemView(data);
        }

        @Override
        protected void parsering(FileData data) {
            if (adapter == null) return;
            //////removeTaskId(data.getItemId());
            adapter.updateItemView(data);
        }

        @Override
        protected void connecting(FileData data) {
            if (adapter == null) return;
            adapter.updateItemView(data);
        }

        @Override
        protected void downloadError(FileData data) {
            if (adapter == null) return;
            removeTaskId(data.getItemId());
            adapter.updateItemView(data);

            DownloadHelp.stopDownload(mContext, data.getItemId());
            showToast(getString(R.string.connect_server_error));
        }

        @Override
        protected void downloadFinished(FileData data) {
            if (adapter == null) return;
            removeTaskId(data.getItemId());
            adapter.updateItemView(data);

            endDownloadProcess(data);
            showToast(getString(R.string.download_n_data_complete,
                    data.getContent_name()));
            checkSharePosFile(data);
        }

        @Override
        protected void packaging(final FileData data) {
            if (adapter == null) return;
            adapter.updateItemView(data);
            showToast(getString(R.string.downloaditem_packaging));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeTaskId(data.getItemId());
                    data.setTaskState(DownloadTaskManagerPat.INIT);
                    adapter.updateItemView(data);
                }
            }, 2000);
        }

        @Override
        protected void requestError(FileData data, String code) {
            if (adapter == null) return;
            removeTaskId(data.getItemId());
            adapter.updateItemView(data);
            DownloadHelp.stopDownload(mContext, data.getItemId());
            PromptMsgManager.showToast(mContext, code);
        }
    };


    //TODO: onCreate UI Start...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_file);
        ButterKnife.bind(this);
        initConfigure();
        getValue();
        initView();
        loadData();
        registerBroadcast();
    }

    private void registerBroadcast() {
        IntentFilter downloadFilter = new IntentFilter();
        downloadFilter.addAction(DownloadPatService.ACTION_FINISHED);
        downloadFilter.addAction(DownloadPatService.ACTION_UPDATE);
        downloadFilter.addAction(DownloadPatService.ACTION_ERROR);
        downloadFilter.addAction(DownloadPatService.ACTION_CONNECTING);
        downloadFilter.addAction(DownloadPatService.ACTION_PARSERING);
        downloadFilter.addAction(DownloadPatService.ACTION_PACKAGING);
        downloadFilter.addAction(DownloadPatService.ACTION_REQUEST_ERROR);
        mLocalBroadcastManager.registerReceiver(receiver, downloadFilter);
    }

    private void initConfigure() {
        onStop = false;
        ActicityManager.getInstance().add(this);
        EventBus.getDefault().register(this);
        UIHelper.showTintStatusBar(this);
        daoAcImpl = AlbumContentDAOImpl.getInstance();
        if (sTaskIdSet == null) sTaskIdSet = new LinkedHashSet<>();
        sTaskIdSet.clear();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    private void getValue() {
        Intent intent = getIntent();
        ProductInfo o = intent.getParcelableExtra("ProductInfo");
        if (intent.hasExtra("download_fileId")) {
            downloadFileId = intent.getStringExtra("download_fileId");
        }
        if (o != null) {
            isValid = o.isValid();
            myProductId = o.getMyProId();
            productName = o.getProductName();
            productPic = o.getPicture_url();
            category = o.getCategory();
            productAuthor = o.getAuthors();
            productPicRatio = o.getPicture_ratio();
        }
        checkMyProId(myProductId);// 检查缺省值
        checkCategory(myProductId);// 检查专辑类型
    }

    private void checkMyProId(String myProId) {
        if (TextUtils.isEmpty(myProId)) {
            ViewUtil.showSingleCommonDialog(this, null, "参数缺失(id可能为空或出现错误)",
                    "返回", new ViewUtil.DialogCallBack() {
                        @Override
                        public void onConfirm() {
                            UIHelper.finishActivity(ListFileActivity.this);
                        }
                    });
        }
    }

    /**
     * 初始化专辑类型category
     * <p>
     * VIDEO;MUSIC;BOOK
     */
    private String checkCategory(String myProId) {
        if (TextUtils.isEmpty(category)) {
            category = AlbumDAOImpl.getInstance().findAlbumCategoryByMyProId(myProId);
        }
        return category;
    }

    protected void initView() {
        mTitleTv.setText(productName);
        mBackImg.setOnClickListener(this);
        mBatchDownloadTv.setOnClickListener(this);
        mBatchDeleteTv.setOnClickListener(this);
        mBatchTv.setOnClickListener(this);


        mLocalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (localAdapter == null) return;
                DrmFile ac = localAdapter.getItem(position);
                if (ac == null) return;
                localAdapter.setContentId(ac.getFileId());
                localAdapter.notifyDataSetChanged();
                // 根据不同的类型，以及子专辑item，跳转不同的播放器。
                OpenPageUtil.openPage(ListFileActivity.this,
                        category,
                        myProductId,
                        productName,
                        productPic,
                        ac.getFileId(), ac.getLyricId(), null);
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //卖家禁用当前端时，已下载的文件给出相应提示
                if (!isValid) {
                    ViewUtil.showSingleCommonDialog(ListFileActivity.this, "",
                            getString(R.string.file_limit_device), getString(R.string.i_known),
                            null);
                    return;
                }
                FileData data = (FileData) mListView.getItemAtPosition(position);
                if (data == null) return;
                if (data.isOverdue()) {
                    showToast(getString(R.string.file_expired_2));
                    return;
                }
                int taskState = data.getTaskState();
                if (taskState == DownloadTaskManagerPat.CONNECTING) return;
                if (taskState == DownloadTaskManagerPat.PARSERING) return;
                if (taskState == DownloadTaskManagerPat.PACKAGING) {
                    showToast(getString(R.string.downloaditem_packaging));
                    return;
                }
                AlbumContent ac = daoAcImpl.findAlbumContentByCollectionId(data.getCollectionId());
                if (ac == null) {
                    //下载
                    downloadFile(data);
                } else {
                    SZContent szContent = new SZContent(ac.getAsset_id());
                    if (!szContent.checkOpen()) {
                        showToast(getString(R.string.file_expired));
                        return;
                    }
                    if (szContent.isInEffective()) {
                        showToast(getString(R.string.file_ineffective));
                        return;
                    }
                    adapter.setContentId(data.getItemId());
                    adapter.notifyDataSetChanged();
                    OpenPageUtil.openPage(ListFileActivity.this,
                            category,
                            myProductId,
                            productName,
                            productPic,
                            data.getItemId(),
                            data.getMusicLyric_id(),
                            cacheDataList);
                }
            }
        });
        mListView.setOnRefreshListener(new PullListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                if (sTaskIdSet.size() == 0) { //没有下载任务
//                    mBatchTv.setVisibility(View.GONE);
//                }
                if (sTaskIdSet != null && !sTaskIdSet.isEmpty()) {
                    showToast(getString(R.string.please_waiting_now_downloading));
                    mListView.refreshComplete();
                    return;
                }

                loadNetData(false);
            }
        });
    }

    protected void loadData() {
        ViewUtil.hideWidget(emptyView);
        if (CommonUtil.isNetConnect(this)) {
            loadNetData(true);
        } else {
            SwipeMenuListView.OnMenuItemClickListener offFileItemClickListener = new
                    SwipeMenuListView
                            .OnMenuItemClickListener() {
                        @Override
                        public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                            if (localAdapter == null) return;
                            if (index != 0) return;
                            DrmFile file = localAdapter.getItem(position);
                            if (file == null) return;
                            removeItemData(file.getMyProductId(), file.getFileId(), file
                                    .getCollectionId(), file.getLyricId(), position);
                        }
                    };

            ViewUtil.showWidget(mLocalListView);
            mListView.refreshComplete();
            ViewUtil.hideWidget(mListView);
            SwipUtil.initSwipItem(mLocalListView, offFileItemClickListener);
            showLoading();
            ExecutorManager.getInstance().execute(new LoadLocalContentThread(this, myProductId));
        }
    }

    private void loadNetData(boolean showLoading) {
        if (isLoading) {
            return;
        }
        isLoading = true;
        if (showLoading) showLoading();
        ViewUtil.showWidget(mListView);
        ViewUtil.hideWidget(mLocalListView);
        Bundle params = new Bundle();
        params.putString("username", Constant.getName());
        params.putString("token", Constant.getToken());
        params.putString("category", category);
        params.putString("myProductId", myProductId);
        dataCancelable = HttpEngine.post(APIUtil.getItemVersionList(),
                params, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        DRMLog.v("itemVersion: " + s);
                        FilesDataModel model = JSON.parseObject(s, FilesDataModel.class);
                        parserData(model);
                    }

                    @Override
                    public void onError(Throwable throwable, boolean b) {
                        loadFailedSet(getResources().getString(R.string.connect_server_failed));
                    }

                    @Override
                    public void onCancelled(CancelledException e) {
                    }

                    @Override
                    public void onFinished() {
                        isLoading = false;
                        mListView.refreshComplete();
                        hideLoading();
                    }
                });
    }

    private void parserData(FilesDataModel model) {
        List<FileData> mDataList = new ArrayList<>();
        if (model.isSuccess()) {
            List<FileData> fileDataList = model.getData();
            int size = fileDataList.size();
            for (int x = 0; x < size; x++) {
                FileData fileData = fileDataList.get(x);
                List<FileVersionModel> versionModelList = fileData.getItemList();
                String latestItemId = fileData.getLatestItemId();//最新的Id;
                int count = versionModelList.size();
                for (int z = 0; z < count; z++) {
                    FileVersionModel versionModel = versionModelList.get(z);
                    String itemId = versionModel.getItemId();
                    if (TextUtils.equals(latestItemId, itemId)) {
                        convertModel(fileData, versionModel);
                        break;
                    }
                }
                sAllFileId.add(fileData.getItemId());
                mDataList.add(fileData);
            }
            loadSuccess(mDataList);
        } else {
            if (model.getMsg().contains("验证")) {
                Util_.repeatLogin(this);
            }
            loadFailedSet(getString(R.string.getdata_failed));
        }
    }

    private FileData convertModel(FileData target, FileVersionModel src) {
        target.setContent_name(src.getContent_name());
        target.setContent_size(src.getContent_size());
        target.setContent_format(src.getContent_format());
        target.setPage_num(src.getPage_num());
        target.setLength(src.getLength());
        target.setVersion(src.getVersion());
        target.setVersionInfo(src.getVersionInfo());
        target.setItemId(src.getItemId());
        target.setPlay_progress(src.getPlay_progress());
        target.setMusicLyric_id(src.getMusicLyric_id());
        target.setMyProId(myProductId);
        target.setPicture_ratio(productPicRatio);
        target.setAuthors(productAuthor);
        return target;
    }


    private void loadFailedSet(String msg) {
        emptyTextView.setText(msg);
        mListView.setEmptyView(emptyView);
    }

    private void loadSuccess(List<FileData> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            emptyTextView.setText(getString(R.string.load_data_null));
            mListView.setEmptyView(emptyView);
            return;
        }
        cacheDataList = itemList;
        updateAlbumContentForCollectionId(itemList);

        totalItemCount = itemList.size();
        adapter = new ListFileAdapter(this, itemList);
        adapter.setListView(mListView);
        mListView.setAdapter(adapter);
        initOnSwipeClickListener();
        setBottomLayout();
        notifyItemView(MusicHelp.getCurrentPlayId());

        againPlaying(true);
        downloadShareFile(downloadFileId, itemList);
    }

    //先通过itemId查询到记录AlbumContent;
    //查询该itemId对应的collectionId;
    //将对应的collectionId更新到AlbumContent;
    private void updateAlbumContentForCollectionId(List<FileData> datas) {
        for (FileData data : datas) {
            List<FileVersionModel> models = data.getItemList();
            for (FileVersionModel model : models) {
                AlbumContent saveAc = daoAcImpl.findAlbumContentByContentId(model.getItemId());
                if (saveAc != null) {
                    //存在记录
                    if (StringUtil.isEmptyOrNull(saveAc.getCollectionId())
                            || StringUtil.isEmptyOrNull(saveAc.getMusicLrcId())
                            || StringUtil.isEmptyOrNull(saveAc.getMyProId())
                            || saveAc.getContentSize() == 0L) {
                        String collectionId = data.getCollectionId();
                        String lrcId = data.getMusicLyric_id();
                        long contentSize = data.getContent_size();
                        int result = daoAcImpl.updateAlbumContentByItemId(saveAc.getContent_id(),
                                collectionId, lrcId, myProductId, contentSize);
                        DRMLog.e("更新AlbumContent[" + saveAc.getName() + "] Column: " + result);
                    }
                }
            }
        }
    }

    // 初始化左滑删除事件回调
    private void initOnSwipeClickListener() {
        if (adapter == null) return;
        adapter.setOnSwipeClickListener(new ListFileAdapter.OnSwipeClickListener() {
            @Override
            public void onClick(View view, int position) {
                FileData data = adapter.getItem(position);
                if (data == null) return;
                //AlbumContent ac = daoAcImpl.findAlbumContentByCollectionId(data.getCollectionId
                // ());
                //if (ac == null) return;
                boolean hasSaveData = daoAcImpl.existAlbumContentById(data.getCollectionId());
                if (!hasSaveData) {
                    DownDataPatDBManager.Builder().deleteByFileId(data.getItemId());
                    return;
                }
                if (data.getTaskState() == DownloadTaskManagerPat.DOWNLOADING) return;
                if (data.getTaskState() == DownloadTaskManagerPat.PARSERING) {
                    showToast(getString(R.string.please_waiting_now_parser));
                    return;
                }
                removeItemData(data.getMyProId(), data.getItemId(), data.getCollectionId(), data
                        .getMusicLyric_id(), -1);
            }
        });
    }

    /**
     * 删除数据
     *
     * @param MyProId      我购买的专辑id
     * @param ItemId       文件id
     * @param CollectionId 文件集合id
     * @param LrcId        歌词id
     * @param Position     网络状态：position=-1,离线本地：position=position
     */
    protected void removeItemData(String MyProId, String ItemId, String CollectionId,
                                  String LrcId, int Position) {
        handler.removeCallbacksAndMessages(null);
        showBgLoading(getString(R.string.now_delete_item));
        ExecutorManager.getInstance().execute(new ClearItemDataThread(this, MyProId, ItemId,
                CollectionId, LrcId, Position));
    }

    /**
     * 加载数据成功，根据是否存在已下载数据，查询文件，显示按钮与否 <br/>
     * <p>
     * 1. 当列表文件数大于6个时，显示“批量下载”“批量删除”按钮 <br/>
     * 2. 如全部已下载则不显示“批量下载”按钮 <br/>
     * 3. 如全部未下载则不显示“批量删除”按钮；
     */
    private void setBottomLayout() {
//        if (totalItemCount <= DEFAULT_ITEM) {
//            ViewUtil.hideWidget(mBottomLayout);
//            return;
//        }
//        ViewUtil.showWidget(mBottomLayout);
        ViewUtil.hideWidget(mBottomLayout);
        List<String> localIds = daoAcImpl.findAlbumContentIdByMyProId(myProductId);
        sDownloadSet.addAll(localIds); //已下载数量
        DRMLog.i("已下载：" + sDownloadSet.size());
        downloadItemCount = sDownloadSet.size();
        showBatchDownloadText(!(downloadItemCount == totalItemCount));
        showBatchDeleteText(downloadItemCount > DEFAULT_ITEM);
    }

    /**
     * 是否显示‘批量下载’
     */
    private void showBatchDownloadText(boolean show) {
        //mBatchDownloadTv.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 是否显示‘批量删除’
     */
    private void showBatchDeleteText(boolean show) {
        //mBatchDeleteTv.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 是否显示‘全部删除/下载’
     */
    private void showBatchCenterText(boolean show, String text) {
//        if (show) {
//            if (mBatchTv.getVisibility() == View.GONE
//                    || mBatchTv.getVisibility() == View.INVISIBLE) {
//                mBatchTv.setVisibility(View.VISIBLE);
//            }
//            mBatchTv.setText(text);
//            mBatchTv.setOnClickListener(this);
//        } else {
//            mBatchTv.setText(null);
//            mBatchTv.setVisibility(View.GONE);
//        }
    }

    private void notifyItemView(String fileId) {
        if (adapter == null || TextUtils.isEmpty(fileId)) return;
        adapter.setContentId(fileId);
        adapter.notifyDataSetChanged();
    }

    /**
     * 续播
     *
     * @param check 是否检查存在续播文件id
     */
    private void againPlaying(boolean check) {
        //判断对应myProId保存的文件id是否存在
        String fileId = (String) ProgressHelp.getProgress("ap_" + myProductId, "");
        if (check) {
            if (!TextUtils.isEmpty(fileId)) {
                mFunTv.setVisibility(View.VISIBLE);
                mFunTv.setOnClickListener(this);
            } else {
                mFunTv.setVisibility(View.INVISIBLE);
            }
            return;
        }
        ProgressHelp.removeProgress("ap_" + myProductId); //移除，只需取到值fileId就可以；
        FileData fileData = null;
        for (FileData data : cacheDataList) {
            if (TextUtils.equals(data.getItemId(), fileId)) {
                fileData = data;
                break;
            }
        }
        String tempId = (fileData != null) ? fileData.getItemId() : fileId;
        notifyItemView(tempId);
        OpenPageUtil.openPage(ListFileActivity.this,
                category,
                myProductId,
                productName,
                productPic,
                tempId,
                fileData != null ? fileData.getMusicLyric_id() : "",
                cacheDataList);
    }

    /**
     * 下载分享此刻的文件（有权限前提下）
     */
    private void downloadShareFile(String downloadFileId, List<FileData> itemList) {
        if (TextUtils.isEmpty(downloadFileId)) return;
        int count = itemList.size();
        for (int i = 0; i < count; i++) {
            FileData data = itemList.get(i);
            if (downloadFileId.equals(data.getItemId())) {
                data.setMyProId(myProductId);
                data.setAuthors(productAuthor);
                data.setPicture_ratio(productPicRatio);
                downloadFile(data);
                break;
            }
        }
    }

    /**
     * 下载完成，检测分享此刻的文件并提示打开
     */
    private void checkSharePosFile(final FileData data) {
        if (TextUtils.isEmpty(downloadFileId)) return;
        ViewUtil.showCommonDialog(this, "",
                getString(R.string.share_moment_download_complete),
                getString(R.string.open), "",
                new ViewUtil.DialogCallBackPat() {
                    @Override
                    public void onConfirm() {
                        if (adapter != null) {
                            adapter.setContentId(data.getItemId());
                            adapter.notifyDataSetChanged();
                        }
                        OpenPageUtil.openPage(ListFileActivity.this,
                                category,
                                myProductId,
                                productName,
                                productPic,
                                data.getItemId(),
                                data.getMusicLyric_id(),
                                cacheDataList);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (onStop) { //页面从不可见到显示，更新item选中和续播按钮显示
            notifyItemView(MusicHelp.getCurrentPlayId());
            againPlaying(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        onStop = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    private void release() {
        if (sTaskIdSet != null) sTaskIdSet.clear();
        if (sDownloadSet != null) sDownloadSet.clear();
        if (sAllFileId != null) sAllFileId.clear();
        handler.removeCallbacksAndMessages(null);
        mLocalBroadcastManager.unregisterReceiver(receiver);
        //DownloadHelp.stopAllDownload(this);
        ActicityManager.getInstance().remove(this);
        EventBus.getDefault().unregister(this);
        HttpEngine.cancelHttp(dataCancelable);
    }

    @Override
    public void onBackPressed() {
        UIHelper.finishActivity(this);
    }

    @Override
    public void onClick(View v) {
        ///////if (CommonUtil.isFastDoubleClick(600)) return;
        int id = v.getId();
        if (id == R.id.alf_back_img) {
            UIHelper.finishActivity(this);
        } else if (id == R.id.alf_fun_tv) {
            //续播
            againPlaying(false);
        } else if (id == R.id.alf_batchDownload_tv) {
            //批量下载
            //batchDownloadClick();
        } else if (id == R.id.alf_batch_tv) {
            //全部下载/取消，删除全部/所选
            //batchCenterClick();
        } else if (id == R.id.alf_batchDelete_tv) {
            //批量删除
            //batchDeleteClick();
        }
    }

    private boolean isBatchDownloadClick = false;  //批量下载点击
    private boolean isBatchDeleteClick = false;  //批量删除点击
    private boolean isDownloadAll = false;  //下载全部
    private boolean isDeleteAll = false;  //删除全部
    private boolean isDownloadStart = false;  //已经点击开始下载了
    private boolean isDownloadPause = false;  //全部暂停

    /**
     * 进行下载功能
     */
    private boolean downloadClick() {
        return isBatchDownloadClick && !isBatchDeleteClick;
    }

    /**
     * 进行删除功能
     */
    private boolean deleteClick() {
        return isBatchDeleteClick && !isBatchDownloadClick;
    }

    //批量删除
    private void batchDeleteClick() {
        if (adapter == null) return;
        //List<FileData> data = adapter.getList();
        //adapter.selectAll(data, false);
        isBatchDeleteClick = true;
        isBatchDownloadClick = false;
        //设置checkbox显示
        adapter.clearItemCheckState();
        adapter.setShowCheckBox(true, false);
        //点击“批量删除”，列表隐藏未下载的
        //List<AlbumContent> contents = daoAcImpl.findAlbumContentByMyProId(myProductId);
        //List<FileData> data = Util_.wipeNotDownloadData(cacheDataList, contents);
        //adapter.setList(data);
        adapter.notifyDataSetChanged();
        //底部变成“删除全部”isDeleteAll=true
        showBatchDownloadText(false);
        showBatchDeleteText(false);
        showBatchCenterText(true, getString(R.string.downloaditem_all_delete));
        isDeleteAll = true;
    }

    //批量下载
    private void batchDownloadClick() {
        if (adapter == null) return;
        isBatchDownloadClick = true;
        isBatchDeleteClick = false;
        isDownloadStart = false; //点击批量下载，说明下载点击就是false
        isDownloadPause = false; //暂停必然变回false
        //adapter.getSelectFile().clear();
        //设置checkbox显示
        //adapter.setShowCheckBox(true, true, KeyHelp.SOME_DOWNLOAD);
        adapter.clearItemCheckState();
        adapter.setShowCheckBox(true, true);
        //点击“批量下载”，列表隐藏已下载的
        //List<AlbumContent> contents = daoAcImpl.findAlbumContentByMyProId(myProductId);
        //List<FileData> data = Util_.wipeDownloadData(cacheDataList, contents);
        //adapter.setList(data);
        adapter.notifyDataSetChanged();
        //底部变为“下载全部”，点击则直接全部开始下载isDownloadAll=true
        showBatchDownloadText(false);
        showBatchDeleteText(false);
        showBatchCenterText(true, getString(R.string.downloaditem_all_download));
        isDownloadAll = true;
    }

    //全部下载/取消,删除全部/所选
    private void batchCenterClick() {
        if (downloadClick()) {
            //全部下载/取消
            if (isDownloadStart) {
                allCancel();
            } else {
                allDownload();
            }
            isDownloadStart = !isDownloadStart;
        }
        if (deleteClick()) {
            //删除全部/所选
            allDelete();
        }
    }

    //全部删除（或者选中的删除）
    private void allDelete() {
        isBatchDownloadClick = false;
        isBatchDeleteClick = true;
        if (adapter == null) return;
        List<FileData> deleteFiles = new ArrayList<>(); //存储要删除的文件
        deleteFiles.clear();
        if (isDeleteAll) { //删除全部
            List<FileData> data = adapter.getList();
            adapter.selectAll(data, true);
            deleteFiles.addAll(data);
        } else { //删除所选
            Map<String, FileData> mSelectFile = adapter.getSelectFile();
            Collection<FileData> data = mSelectFile.values();
            deleteFiles.addAll(data);
        }
        DRMLog.e("删除文件数：" + deleteFiles.size());
        showBgLoading(getString(R.string.now_delete_item));
        ExecutorManager.getInstance().execute(new ClearListFileDataThread(this, deleteFiles));

    }

    //全部下载(或者选中的下载)
    private void allDownload() {
        if (adapter == null) return;
        List<FileData> downloadFiles = new ArrayList<>(); //存储要下载的文件
        downloadFiles.clear();
        if (isDownloadAll) { //下载全部
            List<FileData> data = adapter.getList();
            adapter.selectAll(data, true);
            downloadFiles.addAll(data);
        } else { //下载所选
            Map<String, FileData> mSelectFile = adapter.getSelectFile();
            Collection<FileData> data = mSelectFile.values();
            downloadFiles.addAll(data);
        }
        //开始下载后，底部中间按钮文字变为“全部暂停”
        showBatchCenterText(true, getString(R.string.downloaditem_all_pause));
        //adapter.setShowCheckBox(true, true, KeyHelp.ALL_PAUSE);
        //adapter.setShowCheckBox(true, true);
        //adapter.notifyDataSetChanged();
        DRMLog.e("下载文件数：" + downloadFiles.size());
        for (FileData data : downloadFiles) {
            if (daoAcImpl.existAlbumContentById(data.getCollectionId()))
                continue;
            if (data.getTaskState() == DownloadTaskManagerPat.INIT
                    || data.getTaskState() == DownloadTaskManagerPat.PAUSE
                    || data.getTaskState() == DownloadTaskManagerPat.DOWNLOAD_ERROR) {
                //downloadFile(data);
                //init,pause,error
                addTask(data);
            }
        }
    }

    //全部暂停
    private void allCancel() {
        ViewUtil.showCommonDialog(this, null, "确认要暂停所有下载吗？", null, null, new ViewUtil
                .DialogCallBackPat() {
            @Override
            public void onConfirm() {
                if (adapter == null) return;
                Map<String, FileData> mSelectFile = adapter.getSelectFile();
                Collection<FileData> data = mSelectFile.values();
                if (!data.isEmpty()) {
                    isDownloadPause = true;
                    if (sTaskIdSet != null) sTaskIdSet.clear();
                    for (FileData fileData : data) { //没有解析，没有结束，就暂停
                        if (fileData.getTaskState() != DownloadTaskManagerPat.PARSERING
                                || fileData.getTaskState() != DownloadTaskManagerPat.FINISHED) {
                            removeTask(fileData);
                        }
                    }
                }
                adapter.setShowCheckBox(false, true);
                //adapter.setList(cacheDataList);
                adapter.notifyDataSetChanged();
                showBatchCenterText(false, null);
                //mBatchTv.setVisibility(View.GONE);
                showBatchDownloadText(true);
                showBatchDeleteText(downloadItemCount > DEFAULT_ITEM);
                isBatchDeleteClick = false;
                isBatchDownloadClick = false;
            }

            @Override
            public void onCancel() {
            }
        });
    }

    /**
     * 下载，更新，(非)选择/删除 文件
     */
    public void onEventMainThread(FileHandleEvent event) {
        int type = event.getType();
        if (type == BaseEvent.Type.FILE_DOWNLOAD) {
            FileData data = event.getData();
            DRMLog.e("download file: " + data.getContent_name());
            downloadFile(data);
        } else if (type == BaseEvent.Type.FILE_UPDATE) {
            FileData data = event.getData();
            DRMLog.e("update file: " + data.getContent_name());
            //data.setMyProId(myProductId);
            data.setItemId(data.getLatestItemId());
            ExecutorManager.getInstance().execute(new UploadFileDataThread(this, data));
        } else if (type == BaseEvent.Type.FILE_SELECT) {  //暂不使用
            FileData data = event.getData();
            DRMLog.e("select file name: " + data.getContent_name());
            //存在选中的元素，下载：底部按钮变为“开始下载”；删除：底部按钮变为“删除所选”
            if (downloadClick()) {
                if (!isDownloadStart) { //没有点击开始下载，才切换文字
                    if (!isDownloadPause) { //没有点击暂停，才切换文字
                        showBatchCenterText(true, getString(R.string.downloaditem_start));
                    }
                    isDownloadAll = false;
                }
                if (data.getTaskState() != DownloadTaskManagerPat.FINISHED
                        && data.getTaskState() != DownloadTaskManagerPat.DOWNLOADING
                        && data.getTaskState() != DownloadTaskManagerPat.PARSERING
                        && isDownloadStart) {
                    //点击开始，文件状态没有下载，没有解析，没有结束，不管任何状态选中了都是添加下载任务
                    addTask(data);
                }
            } else if (deleteClick()) {
                showBatchCenterText(true, getString(R.string.downloaditem_select_delete));
                isDeleteAll = false;
            }
        } else if (type == BaseEvent.Type.FILE_UN_SELECT) { //暂不使用
            FileData data = event.getData();
            DRMLog.e("un select file name: " + data.getContent_name());
            if (adapter == null) return;
            int count = adapter.getSelectFile().size();
            DRMLog.e("select file count: " + count);
            //取消勾选后(count=0)，下载：再变回“下载全部”；删除：则变回“删除全部”
            if (downloadClick()) {
                if (!isDownloadStart) { //没有点击开始下载，才切换文字
                    if (!isDownloadPause) { //没有点击暂停，才切换文字
                        showBatchCenterText(true, getString(count == 0 ?
                                R.string.downloaditem_all_download : R.string.downloaditem_start));
                    }
                }
                isDownloadAll = (count == 0);
                if (data.getTaskState() != DownloadTaskManagerPat.FINISHED
                        && data.getTaskState() != DownloadTaskManagerPat.PARSERING
                        && isDownloadStart) {
                    //点击开始下载了，文件状态没有解析，没有结束，不管任何状态没有选中都是取消下载任务
                    removeTask(data);
                }
                //全部未选，则相当于没有开始下载
                if (count == 0) isDownloadStart = false;
            } else if (deleteClick()) {
                showBatchCenterText(true, getString((count == 0) ? R.string.downloaditem_all_delete
                        : R.string.downloaditem_select_delete));
                isDeleteAll = (count == 0);
            }
        } else if (type == BaseEvent.Type.FILE_CANCEL) {
            //取消下载
            FileData data = event.getData();
            if (containsTaskId(data.getItemId())) {
                removeTask(data);
            }
        }
    }

    //下载过程退出后再进入文件列表，可能该列表中对应文件正好也在下载(ListFileAdapter发送通知)
    @Deprecated
    public void onEventMainThread(FileProgressStateEvent event) {
        //setDownloadAllButtonShow(R.string.download_cancel, true);
        //进入UI,下载加入对应的taskId;
        addTaskId(event.getFileId());
    }

    // 播放音乐服务，发送当前的id
    public void onEventMainThread(MusicCurrentPlayEvent event) {
        notifyItemView(event.getFileId());
    }

    /**
     * 更新文件，删除相关数据
     * <p>
     * folderId 文件夹id,
     * itemId   文件id（文件有更新，会有历史id列表）,
     * collectionId   文件集合id,
     * lrcId   音乐歌词id
     */
    private void deleteFileData(String folderId, String itemId,
                                String collectionId, String lrcId) {
        DownDataPatDBManager.Builder().deleteByFileId(itemId);
        DRMDBHelper.deleteFile(folderId, itemId);
        daoAcImpl.deleteAlbumContentByCollectionId(collectionId);
        LrcEngine.deleteLyric(folderId, lrcId);
    }

    private synchronized void addTaskId(String fileId) {
        if (sTaskIdSet != null) {
            sTaskIdSet.add(fileId);
        }
    }

    private synchronized void removeTaskId(String fileId) {
        if (sTaskIdSet != null) {
            sTaskIdSet.remove(fileId);
        }
    }

//    private synchronized void clearTaskId() {
//        if (sTaskIdSet != null) {
//            sTaskIdSet.clear();
//        }
//    }

    private synchronized boolean containsTaskId(String fileId) {
        return sTaskIdSet != null && sTaskIdSet.contains(fileId);
    }

    /**
     * 整个下载流程结束，文件解析完毕并入库
     */
    private void endDownloadProcess(FileData data) {
        sDownloadSet.add(data.getItemId());
        downloadItemCount = sDownloadSet.size();
        DRMLog.e("已下载个数：" + downloadItemCount + ", 总数：" + totalItemCount);
        checkCategory(myProductId);
//        if (sTaskIdSet.size() == 0) {
//            if (downloadItemCount > DEFAULT_ITEM) {//下载总数和已下载的个数
//                //下载总数大于6时显示批量删除
//                isBatchDeleteClick = true;
//                showBatchDeleteText(true);
//            }
//            if (totalItemCount - downloadItemCount > 0 || downloadItemCount == 0) {
//                showBatchDownloadText(true);
//                isBatchDownloadClick = true;
//            }
//            showBatchCenterText(false, null);
//            adapter.setShowCheckBox(false, false);
//            adapter.notifyDataSetChanged();
//        } else {
//            showBatchDownloadText(false);
//            showBatchDeleteText(false);
//            //全部暂停？？？？
//            //showBatchCenterText(true, getString(R.string.downloaditem_all_pause));
//        }
//        if (downloadItemCount == totalItemCount) {
//            //已全部下载,显示批量删除,将全部暂停隐藏
//            isDownloadStart = false;
//            isBatchDeleteClick = true;
//            isBatchDownloadClick = false;
//            showBatchDownloadText(false);
//            showBatchCenterText(false, null);
//            adapter.setShowCheckBox(false, false);
//            adapter.notifyDataSetChanged();
//            if (downloadItemCount > DEFAULT_ITEM) {
//                showBatchDeleteText(true);
//            }
//        }
    }

    private void addTask(FileData o) {
        addTaskId(o.getItemId());
        o.setTaskState(DownloadTaskManagerPat.WAITING);
        adapter.updateItemView(o);
        DownloadHelp.startDownload(this, o);

        //存在下载任务，按钮显示取消
        //setDownloadAllButtonShow(R.string.download_cancel, true);
    }

    private void removeTask(FileData o) {
        removeTaskId(o.getItemId());
        o.setTaskState(DownloadTaskManagerPat.PAUSE);
        adapter.updateItemView(o);
        DownloadHelp.stopDownload(this, o.getItemId());
    }

    /**
     * 下载
     */
    private void downloadFile(final FileData o) {
        if (o == null) return;
        if (containsTaskId(o.getItemId())) {
            removeTask(o);
            return;
        }
        // 提示一次信息
        if (!CommonUtil.isWifi(this) && !isMobileVNO) {
            ViewUtil.showUserDialog(this, getString(R.string.download_tips),
                    getString(R.string.download_ask_ok), new ViewUtil.DialogCallBack() {
                        public void onConfirm() {
                            isMobileVNO = true;
                            addTask(o);
                        }
                    });
        } else {
            addTask(o);
        }
    }

}
