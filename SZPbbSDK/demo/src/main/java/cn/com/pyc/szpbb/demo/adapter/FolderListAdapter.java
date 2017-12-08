package cn.com.pyc.szpbb.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.com.pyc.szpbb.demo.R;
import cn.com.pyc.szpbb.sdk.models.SZFolderInfo;
import cn.com.pyc.szpbb.util.FormatUtil;

public class FolderListAdapter extends BaseAdapter {
    private List<SZFolderInfo> mList;
    private Context mContext;
    private ListView listView;

    public FolderListAdapter(Context ctx, List<SZFolderInfo> mList) {
        this.mContext = ctx;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public SZFolderInfo getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView)
            convertView = View.inflate(mContext, R.layout.item_data_list, null);

        SZFolderInfo data = mList.get(position);
        convertView.setTag(position);

        TextView name = (TextView) convertView.findViewById(R.id.item_tv_name);
        TextView size = (TextView) convertView.findViewById(R.id.item_tv_size);
        //TextView power = (TextView) convertView.findViewById(R.id.item_tv_power);
        TextView state = (TextView) convertView.findViewById(R.id.item_tv_download);
        name.setText("(" + (data.getPosition() + 1) + ")" + data.getProductName());
        size.setText("大小: " + FormatUtil.formatSize(data.getFolderSize()));
        state.setVisibility(View.GONE);
        return convertView;
    }


}
