package cn.com.pyc.drm.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (所有RecyclerView的ViewHolder基类)
 * @date 2017/1/18 11:34
 */
public class Rv_BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    // 声明一个数据,保存view
    private SparseArray<View> views;
    // item 点击事件的回调。
    private Rv_BaseAdapter.OnItemClickListener mOnItemClickListener ;

    public Rv_BaseViewHolder(View itemView, Rv_BaseAdapter.OnItemClickListener onItemClickListener) {
        super(itemView);
        itemView.setOnClickListener(this);// 设置item的点击事件
        this.mOnItemClickListener=onItemClickListener;
        this.views = new SparseArray<View>();
    }
    // 通过ID,获取布局中的textview
    public TextView getTextView(int viewId) {
        return retrieveView(viewId);
    }
    // 通过ID,获取布局中的Button
    public Button getButton(int viewId) {
        return retrieveView(viewId);
    }
    // 通过ID,获取布局中的ImageView
    public ImageView getImageView(int viewId) {
        return retrieveView(viewId);
    }
    // 通过ID,获取布局中的View
    public View getView(int viewId) {
        return retrieveView(viewId);
    }

    // 相当于FindViewById.把数据加入到views数组里
    protected <T extends View> T retrieveView(int viewId) {
        View view = views.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            views.put(viewId, view);
        }
        return (T) view;
    }

    @Override
    public void onClick(View v) {
        // 点击事件回调
        if(mOnItemClickListener!=null){
            mOnItemClickListener.onItemClick(v,getLayoutPosition());
        }
    }
}
