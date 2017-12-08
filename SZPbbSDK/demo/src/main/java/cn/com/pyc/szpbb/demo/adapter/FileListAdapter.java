package cn.com.pyc.szpbb.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.com.pyc.szpbb.common.DownloadState;
import cn.com.pyc.szpbb.demo.R;
import cn.com.pyc.szpbb.sdk.SZFileInterface;
import cn.com.pyc.szpbb.sdk.models.SZFileData;
import cn.com.pyc.szpbb.util.FormatUtil;

public class FileListAdapter extends BaseAdapter {
    private List<SZFileData> mList;
    private Context mContext;
    private ListView listView;

    public FileListAdapter(ListView listView, List<SZFileData> mList) {
        this.listView = listView;
        this.mContext = this.listView.getContext();
        this.mList = mList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mList != null ? mList.size() : 0;
    }

    @Override
    public SZFileData getItem(int position) {
        // TODO Auto-generated method stub
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView)
            convertView = View.inflate(mContext, R.layout.item_data_list, null);

        SZFileData data = mList.get(position);
        convertView.setTag(position);

        TextView name = (TextView) convertView.findViewById(R.id.item_tv_name);
        TextView size = (TextView) convertView.findViewById(R.id.item_tv_size);
        //TextView power = (TextView) convertView.findViewById(R.id.item_tv_power);
        TextView state = (TextView) convertView.findViewById(R.id.item_tv_download);
        name.setText("(" + (data.getPosition() + 1) + ")" + data.getName());
        size.setText("大小: " + FormatUtil.formatSize(data.getFileSize()));
        if (SZFileInterface.existAlbumContent(data.getFiles_id())) {
            state.setText("已下载");
        } else {
            state.setText("下载");
        }

        return convertView;
    }

    public void updateItemView(SZFileData o) {

        if (listView == null) {
            throw new NullPointerException("listView not allow null");
        }

        int position = o.getPosition();
        int visiblePosition = listView.getFirstVisiblePosition();
        int offset = position - visiblePosition;
        if (offset < 0 && offset > 10) {
            return;
        }

        View itemView = listView.findViewWithTag(position);
        if (itemView == null) {
            return;
        }

        TextView state = (TextView) itemView.findViewById(R.id.item_tv_download);
        //TextView power = (TextView) itemView.findViewById(R.id.item_tv_power);

        changeView(o, state);
    }

    /**
     * 修改view状态
     *
     * @param o
     * @param state
     */
    private void changeView(SZFileData o, TextView state) {
        switch (o.getTaskState()) {
            case DownloadState.INIT:
                state.setText("下载");
                break;
            case DownloadState.WAITING:
                state.setText("等待下载");
                break;
            case DownloadState.CONNECTING:
                state.setText("连接中...");
                break;
            case DownloadState.PAUSE:
                state.setText("暂停下载");
                break;
            case DownloadState.DOWNLOAD_ERROR:
                state.setText("下载有误");
                break;
            case DownloadState.DOWNLOADING:
                state.setText(o.getProgress() + "%");
                break;
            case DownloadState.FINISHED:
                state.setText("已下载");
                break;
            default:
                break;
        }
    }

}
