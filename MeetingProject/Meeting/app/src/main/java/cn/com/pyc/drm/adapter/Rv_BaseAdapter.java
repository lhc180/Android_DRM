package cn.com.pyc.drm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2017/1/19 16:09
 */
public abstract class Rv_BaseAdapter<T,H extends Rv_BaseViewHolder> extends RecyclerView.Adapter<Rv_BaseViewHolder> {
    protected static final String TAG = BaseAdapter.class.getSimpleName();

    protected final Context context;

    protected int layoutResId;

    protected List<T> datas;

    private OnItemClickListener mOnItemClickListener = null;

    /**
     * 构造函数
     * @param context  上下文
     * @param datas 数据源
     */
    public Rv_BaseAdapter(Context context, List<T> datas) {
        this.datas = datas == null ? new ArrayList<T>() : datas;
        this.context = context;
    }

    /**
     * 构造函数
     * @param context  上下文
     * @param layoutResId 布局layoutId
     * @param datas 数据源
     */
    public Rv_BaseAdapter(Context context, int layoutResId, List<T> datas) {
        this.datas = datas == null ? new ArrayList<T>() : datas;
        this.context = context;
        this.layoutResId = layoutResId;
    }

    // 相当于getView
    @Override
    public Rv_BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutResId, viewGroup, false);
        Rv_BaseViewHolder vh = new Rv_BaseViewHolder(view,mOnItemClickListener);
        return vh;
    }
    // 绑定数据
    @Override
    public void onBindViewHolder(Rv_BaseViewHolder viewHoder, int position) {
        T item = getItem(position);
        // 把viewhoder和数据传递过去，通过抽象方法让子类实现。
        convert((H)viewHoder, item);
    }

    // 设置adapter的显示数量
    @Override
    public int getItemCount() {
        if(datas==null || datas.size()<=0)
            return 0;

        return datas.size();
    }

    // 通过position获取对应的数据
    public T getItem(int position) {
        if (position >= datas.size()) return null;
        return datas.get(position);
    }

    // 清空数据源
    public void clearData(){
        int itemCount = datas.size();
        datas.clear();
        this.notifyItemRangeRemoved(0,itemCount);
    }
    // 获取数据源
    public List<T> getDatas(){

        return  datas;
    }
    // 下拉刷新，添加数据
    public void addData(List<T> datas){

        addData(0,datas);
    }
    // 在指定position位置，添加位置
    public void addData(int position,List<T> datas){
        if(datas !=null && datas.size()>0) {

            this.datas.addAll(datas);
            this.notifyItemRangeChanged(position, datas.size());
        }
    }
    // 刷新数据
    public void refreshData(List<T> listSH){
        this.datas=listSH;
        notifyDataSetChanged();
    }



    /**
     * 子类实现绑定数据
     */
    protected abstract void convert(H viewHoder, T item);


    // item点击事件的回调
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public  interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
