package cn.com.pyc.drm.adapter;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.pyc.drm.R;
import cn.com.pyc.drm.bean.DownDataPat;
import cn.com.pyc.drm.bean.event.BaseEvent;
import cn.com.pyc.drm.bean.event.FileHandleEvent;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.db.manager.DownDataPatDBManager;
import cn.com.pyc.drm.model.FileData;
import cn.com.pyc.drm.model.db.bean.AlbumContent;
import cn.com.pyc.drm.model.db.practice.AlbumContentDAOImpl;
import cn.com.pyc.drm.model.right.SZContent;
import cn.com.pyc.drm.ui.BaseAssistActivity;
import cn.com.pyc.drm.ui.ListFileActivity;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.FormatUtil;
import cn.com.pyc.drm.utils.Util_;
import cn.com.pyc.drm.utils.ValidDateUtil;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.DownloadHelp;
import cn.com.pyc.drm.utils.manager.DownloadTaskManagerPat;
import cn.com.pyc.drm.widget.HighlightImageView;
import cn.com.pyc.drm.widget.swipe.SimpleSwipeListener;
import cn.com.pyc.drm.widget.swipe.SwipeLayout;
import de.greenrobot.event.EventBus;

public class ListFileAdapter extends BaseAdapter {
    private static final String TAG = "ListFileAdapter";
    private BaseAssistActivity mContext;
    private List<FileData> mList;
    private ListView listView;
    private String contentId;

    private Random random = new Random();
    private ArrayList<SwipeLayout> alSwipeLayout = new ArrayList<>();
    private OnSwipeClickListener mListener;

    private boolean showCheckBox = false; //显示checkBox
    private boolean download = false;    //下载操作（true下载、false删除）
    private Map<String, Boolean> mSelectState = new HashMap<>(); //存儲選中的狀態
    private Map<String, FileData> sSelectFile = new HashMap<>(); //存储选中的元素。

    private int selectColorId;
    private int waitingColorId;
    private int unSelectNameColorId;
    private int unSelectTimeColorId;
    private int expiredColorId;


    private String waitingStr;
    private String connectStr;
    private String packagingStr;
    private String parseringStr;
    private String pauseDownloadStr;
    private String downloadedStr;
    private String unDownloadedStr;

    private DownDataPatDBManager mDBManager;
    private AlbumContentDAOImpl mDaoImpl;

    /**
     * 设置checkbox
     *
     * @param showCheckBox 是否显示CheckBox
     * @param download     是否是下载操作
     */
    public void setShowCheckBox(boolean showCheckBox, boolean download) {
        this.showCheckBox = showCheckBox;
        this.download = download;
    }

//    public void setShowCheckBox(boolean showCheckBox, boolean download, int type) {
//        this.showCheckBox = showCheckBox;
//        this.download = download;
//        this.type = type;
//    }

    public void setOnSwipeClickListener(OnSwipeClickListener listener) {
        mListener = listener;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    public ListFileAdapter(BaseAssistActivity context, List<FileData> list) {
        mContext = context;
        mList = list;

        mDaoImpl = AlbumContentDAOImpl.getInstance();
        mDBManager = DownDataPatDBManager.Builder();
//        initCheckBoxState(mList);

        Resources resources = mContext.getResources();
        waitingColorId = resources.getColor(R.color.title_bg_color);
        selectColorId = resources.getColor(R.color.brilliant_blue);
        unSelectNameColorId = resources.getColor(R.color.black_aa);
        unSelectTimeColorId = resources.getColor(R.color.gray);
        expiredColorId = resources.getColor(R.color.tomato);
        //grayLineColorId = resources.getColor(R.color.line_color);
        //blueLineColorId = resources.getColor(R.color.brilliant_tint_blue);
        //this.selectDrawable = resources.getDrawable(R.drawable.ic_validate_time_select);
        //this.unSelectDrawable = resources.getDrawable(R.drawable.ic_validate_time_nor);

        waitingStr = resources.getString(R.string.Waiting);
        connectStr = resources.getString(R.string.Connecting);
        packagingStr = resources.getString(R.string.Packaging);
        parseringStr = resources.getString(R.string.Parsering);
        pauseDownloadStr = resources.getString(R.string.downloaditem_pause);
        downloadedStr = resources.getString(R.string.downloaditem_download);
        unDownloadedStr = resources.getString(R.string.downloaditem_undownload);
    }

    //初始化checkbox状态，默认false
//    private void initCheckBoxState(List<FileData> dataList) {
//        sSelectFile.clear();
//        mSelectState.clear();
//        for (FileData data : dataList) {
//            mSelectState.put(data.getItemId(), false);
//        }
//    }

    public Map<String, FileData> getSelectFile() {
        return sSelectFile;
    }

    //清除对应的item checkbox的状态
    public void clearItemCheckState(String itemId) {
        mSelectState.remove(itemId);
        sSelectFile.remove(itemId);
    }

    //清除所有的item checkbox的状态
    public void clearItemCheckState() {
        mSelectState.clear();
        sSelectFile.clear();
    }

    public List<FileData> getList() {
        return mList;
    }

    public void setList(List<FileData> list) {
//        initCheckBoxState(list);
        mList = list;
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public FileData getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final FileData fileData = mList.get(position);
        convertView = View.inflate(mContext, R.layout.item_list_file, null);
        convertView.setTag(fileData.getItemId());
        ViewHolder holder = new ViewHolder(convertView);
        //名称和大小
        holder.progressBar.setSecondaryProgress(0);
        holder.nameText.setText(fileData.getContent_name());
        holder.sizeText.setText(FormatUtil.formatSize(fileData.getContent_size()));
        AlbumContent content = null;
        if (fileData.isOverdue()) { //文件已过期
            holder.timeText.setTextColor(expiredColorId);
            holder.timeText.setText(mContext.getString(R.string.over_time));
        } else {
            unSelectorItem(holder);
            content = mDaoImpl.findAlbumContentByCollectionId(fileData.getCollectionId());
            setItemView(fileData, content, holder);
        }
//        setCheckBox(holder, fileData, content != null);
        if (fileData.getContent_size() == 0) //出现错误数据，不显示下载按钮,一般是服务器数据出错
            ViewUtil.hideWidget(holder.downloadImage);
        initSwipeClickListener(holder, position);
        return convertView;
    }

    //设置item view显示
    private void setItemView(FileData fileData, AlbumContent content, ViewHolder holder) {
        if (content == null) {  //没有下载
            DRMLog.d(TAG, "Net data name is " + fileData.getContent_name());
            ViewUtil.showWidget(holder.downloadImage);
            ViewUtil.hideWidget(holder.timeLayout);
            ViewUtil.hideWidget(holder.downloadStatusText);
            ViewUtil.hideWidget(holder.updateBtn);
            holder.progressBar.setSecondaryProgress(0);
            holder.swipeLayout.setSwipeEnabled(false);
            initState(fileData, holder);
            downloadFileByClick(fileData, holder.downloadImage);
        } else { //本地已下载
            DRMLog.d(TAG, "Local data name is " + content.getName());
            ViewUtil.inVisibleWidget(holder.downloadImage);
            ViewUtil.hideWidget(holder.downloadStatusText);
            ViewUtil.showWidget(holder.timeLayout);
            holder.progressBar.setSecondaryProgress(100);
            holder.swipeLayout.setSwipeEnabled(true);

            initTime(content.getAsset_id(), holder.timeText);
            setItemSelector(content.getContent_id(), holder);
            checkFileUpdate(content.getContent_id(), fileData, holder);
        }
    }

    //设置CheckBox的状态
//    private void setCheckBox(final ViewHolder holder, final FileData o, boolean hasDownload) {
//        if (download) {  //下载操作
//            if (showCheckBox && !hasDownload) {
//                //需要显示复选框，且没有下载数据
//                ViewUtil.inVisibleWidget(holder.downloadImage);
//                ViewUtil.hideWidget(holder.downloadStatusText);
//            }
//            if (showCheckBox) { //需要显示复选框
//                if (holder.checkBox.isChecked())
//                    holder.checkBox.setChecked(false);
//                ViewUtil.showWidget(holder.checkBox); //显示checkBox
//                if (hasDownload) {
//                    //有下载,则不显示checkBox,显示“已下载”文字
//                    ViewUtil.hideWidget(holder.downloadImage);
//                    ViewUtil.hideWidget(holder.checkBox);
//                    ViewUtil.showWidget(holder.downloadStatusText);
//                    holder.downloadStatusText.setTextColor(waitingColorId);
//                    holder.downloadStatusText.setText(downloadedStr);
//                }
//            } else {
//                if (holder.checkBox.isChecked())
//                    holder.checkBox.setChecked(false);
//                ViewUtil.hideWidget(holder.checkBox);
//                ViewUtil.hideWidget(holder.downloadStatusText);
//            }
//        } else { //删除操作
//            if (showCheckBox) {
//                //需要显示复选框
//                ViewUtil.inVisibleWidget(holder.downloadImage);
//                ViewUtil.hideWidget(holder.downloadStatusText);
//            }
//            if (showCheckBox) { //需要显示复选框
//                if (holder.checkBox.isChecked())
//                    holder.checkBox.setChecked(false);
//                ViewUtil.showWidget(holder.checkBox); //显示checkBox
//                if (!hasDownload) { //没有下载则不显示checkBox
//                    ViewUtil.inVisibleWidget(holder.checkBox);
//                    ViewUtil.showWidget(holder.downloadStatusText);
//                    holder.downloadStatusText.setTextColor(waitingColorId);
//                    holder.downloadStatusText.setText(unDownloadedStr);
//                }
//            } else {
//                ViewUtil.hideWidget(holder.checkBox);
//                ViewUtil.hideWidget(holder.downloadStatusText);
//            }
//        }
//        final String key = o.getItemId();
//        holder.checkBox.setChecked(mSelectState.get(key) == null ? false : mSelectState.get(key));
//        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                DRMLog.i("OnCheckedChangeListener...");
//                if (isChecked) {
//                    mSelectState.put(key, true);
//                    if (!sSelectFile.containsKey(key)) {
//                        sSelectFile.put(key, o);
//                        EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type
//                                .FILE_SELECT, o));
//                    }
//                } else {
//                    mSelectState.remove(key);
//                    if (sSelectFile.containsKey(key)) {
//                        sSelectFile.remove(key);
//                        EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type
//                                .FILE_UN_SELECT, o));
//                    }
//                }
//                holder.checkBox.setChecked(isChecked);
//            }
//        });
//    }

    //全选(反选)
    public void selectAll(List<FileData> data, boolean selected) {
        for (FileData d : data) {
            String key = d.getItemId();
            if (selected) {
                mSelectState.put(key, true);
                sSelectFile.put(key, d);
            } else {
                mSelectState.remove(key);
                sSelectFile.clear();
            }
        }
        notifyDataSetChanged();
    }

    //下载文件
    private void downloadFileByClick(final FileData data, View downloadView) {
        downloadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.isOverdue()) {
                    mContext.showToast(mContext.getString(R.string.file_expired_2));
                    return;
                }
                int taskState = data.getTaskState();
                if (taskState == DownloadTaskManagerPat.CONNECTING) return;
                if (taskState == DownloadTaskManagerPat.PARSERING) return;
                if (taskState == DownloadTaskManagerPat.PACKAGING) {
                    mContext.showToast(mContext.getString(R.string.downloaditem_packaging));
                    return;
                }
                EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type.FILE_DOWNLOAD, data));
            }
        });
    }

    //设置权限时间
    private void initTime(String assetId, TextView timeText) {
        SZContent szCont = new SZContent(assetId);
        if (szCont.isInEffective()) {
            timeText.setText(mContext.getString(R.string.file_ineffective));
        } else {
            timeText.setText(ValidDateUtil.getValidTime(mContext, szCont
                    .getAvailbaleTime(), szCont.getOdd_datetime_end()));
        }
    }

    //初始化下载各种状态显示
    private void initState(FileData o, ViewHolder holder) {
        //下载退出后再进入文件列表，可能该列表中对应文件刚好下载完正处在解析状态
        if (!TextUtils.isEmpty(DownloadHelp.findParserId(o.getItemId()))) {
            //o.setTaskState(DownloadTaskManagerPat.PARSERING);
            //updateItemState(o.getItemId(), DownloadTaskManagerPat.PARSERING);
        }
        //下载过程退出后再进入文件列表，可能该列表中对应文件正好也在下载
//        int tempProgress = DownloadHelp.findFileProgress(o.getItemId());
//        if (tempProgress > 0 && o.getTaskState() != DownloadTaskManagerPat.DOWNLOADING) {
//            o.setTaskState(DownloadTaskManagerPat.DOWNLOADING);
//            o.setProgress(tempProgress);
//            holder.progressBar.setProgress(tempProgress);
//            holder.checkBox.setChecked(true);
//            EventBus.getDefault().post(new FileProgressStateEvent(o.getItemId()));
//        }
        // 下载后删除数据，不执行finished()；滚动列表过程中。
        if (o.getTaskState() != DownloadTaskManagerPat.FINISHED) {
            changeView(o, holder);
        }
        //存在下载记录，并且正在连接状态
        DownDataPat data = mDBManager.findByFileId(o.getItemId());
        if (data == null) {
            holder.progressBar.setProgress(0);
        } else {
            holder.progressBar.setProgress(data.getProgress());
        }
    }


    private void setItemSelector(String contentId, ViewHolder holder) {
        if (TextUtils.equals(this.contentId, contentId)) {
            holder.nameText.setTextColor(selectColorId);
        } else {
            unSelectorItem(holder);
        }
    }

    private void unSelectorItem(ViewHolder holder) {
        holder.timeText.setTextColor(unSelectTimeColorId);
        holder.nameText.setTextColor(unSelectNameColorId);
    }

    // 检查已下载的文件是否有更新(保存的id和最新的id不同就提示更新)
    private void checkFileUpdate(String dbContentId, final FileData o, final ViewHolder holder) {
        String newItemId = o.getLatestItemId();
        DRMLog.d(TAG, "dbContentId  = " + dbContentId);
        DRMLog.d(TAG, "latestItemId = " + newItemId);
        ViewUtil.hideWidget(holder.updateBtn);
        if (TextUtils.equals(newItemId, dbContentId))
            return;
        ViewUtil.hideWidget(holder.downloadImage);
        ViewUtil.showWidget(holder.updateBtn);
        holder.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ListFileActivity.sTaskIdSet != null) {
                    int taskSize = ListFileActivity.sTaskIdSet.size();
                    if (taskSize >= Constant.sTaskCount) {
                        mContext.showToast(mContext.getString(R.string
                                .please_waiting_n_update_download, taskSize));
                        return;
                    }
                }
                EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type.FILE_UPDATE, o));
                ViewUtil.hideWidget(holder.updateBtn);
            }
        });
    }

    /**
     * 下载任务开始，单个item状态变化更新
     *
     * @param o FileData
     */
    public void updateItemView(FileData o) {
        View itemView = listView.findViewWithTag(o.getItemId());
        if (itemView == null) {
            return;
        }
        DRMLog.d(TAG, "updateItemView : " + o.getContent_name());

        itemView.setTag(o.getItemId());
        ViewHolder holder = new ViewHolder(itemView);

        changeView(o, holder);
        updateItemState(o);
    }

    /**
     * 设置item对象状态
     */
    private void updateItemState(FileData data) {
        if (mList == null) return;
        String itemId = data.getItemId();
        int location = Util_.getFileIndex(itemId, mList);
        if (location != -1) {
            FileData o = mList.get(location);
            int taskState = data.getTaskState();
            if (o.getTaskState() == taskState) {
                return;
            }
            o.setTaskState(taskState);
        }
    }

//    private void updateItemState(String fileId, int taskState) {
//        if (mList == null) return;
//        int location = Util_.getFileIndex(fileId, mList);
//        if (location != -1) {
//            FileData o = mList.get(location);
//            o.setTaskState(taskState);
//        }
//    }

    /**
     * 修改view状态
     */
    private void changeView(FileData o, ViewHolder holder) {
        int taskState = o.getTaskState();
        switch (taskState) {
            case DownloadTaskManagerPat.INIT:
                init(o, holder);
                break;
            case DownloadTaskManagerPat.WAITING:
                waiting(o, holder);
                break;
            case DownloadTaskManagerPat.CONNECTING:
                connecting(o, holder);
                break;
            case DownloadTaskManagerPat.PAUSE:
                pause(o, holder);
                break;
            case DownloadTaskManagerPat.PARSERING:
                parsering(o, holder);
                break;
            case DownloadTaskManagerPat.DOWNLOAD_ERROR:
                downloadError(o, holder);
                break;
            case DownloadTaskManagerPat.DOWNLOADING:
                downloading(o, holder);
                break;
            case DownloadTaskManagerPat.FINISHED:
                finished(o, holder);
                break;
            case DownloadTaskManagerPat.PACKAGING:
                packaging(o, holder);
                break;
            default:
                Log.e(TAG, "download task state error.");
                break;
        }
    }

    /**
     * 开始状态
     */
    private void init(FileData o, ViewHolder holder) {
        ViewUtil.showWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.hideWidget(holder.downloadStatusText);
        holder.progressBar.setProgress(0);
        holder.downloadStatusText.setTag(null);
        holder.sizeText.setText(FormatUtil.formatSize(o.getContent_size()));
    }

    /**
     * 等待中
     */
    private void waiting(FileData o, ViewHolder holder) {
        ViewUtil.inVisibleWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        holder.downloadStatusText.setTextColor(waitingColorId);
        holder.downloadStatusText.setText(waitingStr);
        holder.progressBar.setProgress(0);
        //holder.sizeText.setText(FormatUtil.formatSize(o.getContent_size()));
    }

    /**
     * 连接中
     */
    private void connecting(FileData o, ViewHolder holder) {
        ViewUtil.inVisibleWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        holder.downloadStatusText.setTag(null);
        holder.downloadStatusText.setTextColor(unSelectTimeColorId);
        holder.downloadStatusText.setText(connectStr);
        holder.progressBar.setProgress(0);
        //holder.sizeText.setText(FormatUtil.formatSize(o.getContent_size()));
    }

    /**
     * 正在打包
     */
    private void packaging(FileData o, ViewHolder holder) {
        ViewUtil.inVisibleWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        holder.downloadStatusText.setTextColor(unSelectTimeColorId);
        holder.downloadStatusText.setText(packagingStr);
        holder.progressBar.setProgress(0);
//        if (holder.checkBox.isChecked()) {
//            sSelectFile.remove(o.getItemId());
//            holder.checkBox.setChecked(false);
//        }
        //相当于回到初始状态，文件不选择
//        EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type.FILE_UN_SELECT, o));
    }

    /**
     * 下载
     */
    private void downloading(final FileData o, final ViewHolder holder) {
        ViewUtil.inVisibleWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        ViewUtil.showWidget(holder.progressBar);
//        if (!holder.checkBox.isChecked()) {
//            //正在下载的，如果没有勾选，设置勾选
//            sSelectFile.put(o.getItemId(), o);
//            holder.checkBox.setChecked(true);
//        }
        int progress = o.getProgress();
        holder.progressBar.setProgress(progress);
        long currentSize = (o.getContent_size() * progress) / 100;
        holder.sizeText.setText(FormatUtil.formatSize2(currentSize) + "/" + FormatUtil
                .formatSize(o.getContent_size()));
        holder.downloadStatusText.setTextColor(selectColorId);
        holder.downloadStatusText.setText(pauseDownloadStr);
        if (holder.downloadStatusText.getTag() == null) {
            holder.downloadStatusText.setTag(o.getItemId());
            holder.downloadStatusText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //holder.checkBox.setChecked(false);
                    //下载---》 暂停
                    EventBus.getDefault().post(new FileHandleEvent(
                            BaseEvent.Type.FILE_CANCEL, o));
                }
            });
        }
    }

    /**
     * 暂停中
     */
    private void pause(FileData o, ViewHolder holder) {
        ViewUtil.showWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.inVisibleWidget(holder.downloadStatusText);
        holder.progressBar.setProgress(o.getProgress());
        holder.downloadStatusText.setOnClickListener(null);
        holder.downloadStatusText.setTag(null);
        holder.sizeText.setText(FormatUtil.formatSize(o.getContent_size()));
    }

    /**
     * 解析文件
     */
    private void parsering(FileData o, ViewHolder holder) {
        ViewUtil.hideWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.showWidget(holder.downloadStatusText);
        holder.downloadStatusText.setTextColor(unSelectTimeColorId);
        holder.downloadStatusText.setText(parseringStr);
        int progress = 98 + random.nextInt(3);
        int curProgress = holder.progressBar.getProgress();
        if (curProgress < progress) {
            curProgress = progress;
        }
        holder.progressBar.setProgress(curProgress);
        holder.downloadStatusText.setOnClickListener(null);
        holder.downloadStatusText.setTag(null);
        holder.sizeText.setText(FormatUtil.formatSize(o.getContent_size()));
    }

    /**
     * 下载异常，ftpPath: shutdown,404,-1
     */
    private void downloadError(FileData o, ViewHolder holder) {
        ViewUtil.showWidget(holder.downloadImage);
        ViewUtil.hideWidget(holder.timeLayout);
        ViewUtil.hideWidget(holder.downloadStatusText);
//        if (holder.checkBox.isChecked()) {
//            sSelectFile.remove(o.getItemId());
//            holder.checkBox.setChecked(false);
//        }
        holder.progressBar.setProgress(0);
        holder.downloadStatusText.setOnClickListener(null);
        holder.downloadStatusText.setTag(null);
        holder.sizeText.setText(FormatUtil.formatSize(o.getContent_size()));
        //相当于回到初始状态，文件不选择
//        EventBus.getDefault().post(new FileHandleEvent(BaseEvent.Type.FILE_UN_SELECT, o));
    }

    /**
     * 下载完成
     */
    private void finished(FileData o, ViewHolder holder) {
        // 查询存储的数值
        AlbumContent content = mDaoImpl.findAlbumContentByCollectionId(o.getCollectionId());
        if (content == null){
            holder.swipeLayout.setSwipeEnabled(false);
            return;
        }
        holder.swipeLayout.setSwipeEnabled(true);
        ViewUtil.hideWidget(holder.downloadStatusText);
        ViewUtil.hideWidget(holder.downloadImage);
//        if (holder.checkBox.isChecked()) {
//            sSelectFile.remove(o.getItemId());
//            holder.checkBox.setChecked(false);
//        }
//        ViewUtil.hideWidget(holder.checkBox);
        ViewUtil.showWidget(holder.timeLayout);
        SZContent szContent = new SZContent(content.getAsset_id());
        holder.timeText.setText(ValidDateUtil.getValidTime(mContext,
                szContent.getAvailbaleTime(), szContent.getOdd_datetime_end()));
        holder.nameText.setText(TextUtils.isEmpty(content.getName()) ? o.getContent_name() :
                content.getName());
        holder.progressBar.setSecondaryProgress(100); //第二进度当做蓝色线条
        holder.progressBar.setProgress(0);
        holder.sizeText.setText(FormatUtil.formatSize(o.getContent_size())); //更新size
    }

    static class ViewHolder {
        @BindView(R.id.item_lf_swipe)
        SwipeLayout swipeLayout;
        @BindView(R.id.item_lf_del_layout)
        View delLayout;
        //@BindView(R.id.item_lf_name_checkbox)
        //CheckBox checkBox;
        @BindView(R.id.item_lf_name_tv)
        TextView nameText;
        @BindView(R.id.item_lf_status_tv)
        TextView downloadStatusText;
        @BindView(R.id.item_lf_download_img)
        HighlightImageView downloadImage;
        @BindView(R.id.item_lf_time_tv)
        TextView timeText;
        @BindView(R.id.item_lf_size_tv)
        TextView sizeText;
        @BindView(R.id.item_lf_progress)
        ProgressBar progressBar;
        @BindView(R.id.item_lf_time_layout)
        View timeLayout;
        @BindView(R.id.item_lf_update_btn)
        Button updateBtn;

        private ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }

    /**
     * 初始化左滑控件事件。
     */
    private void initSwipeClickListener(final ViewHolder holder, final int position) {
        holder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                if (alSwipeLayout.size() > 0) {
                    ArrayList<SwipeLayout> sl_al = new ArrayList<>(alSwipeLayout);
                    for (SwipeLayout sw : sl_al) {
                        if (sw != null && !layout.equals(sw)) {
                            sw.close();
                            alSwipeLayout.remove(sw);
                        }
                    }
                }
                if (!alSwipeLayout.contains(holder.swipeLayout)) {
                    alSwipeLayout.add(holder.swipeLayout);
                }
            }
        });
        holder.delLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(v, position);
                }
                holder.swipeLayout.close();
            }
        });
    }

    // 左滑删除控件回调事件。
    public interface OnSwipeClickListener {
        void onClick(View view, int position);
    }

}
