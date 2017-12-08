package cn.com.pyc.drm.adapter;

import android.content.Context;

import java.util.List;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2017/1/18 11:52
 */
public abstract class Rv_SimpleAdapter<T> extends Rv_BaseAdapter<T,Rv_BaseViewHolder> {
    /**
     * 构造函数
     *
     * @param context     上下文
     * @param layoutResId 布局layoutId
     * @param datas       数据源
     */
    public Rv_SimpleAdapter(Context context, int layoutResId, List<T> datas) {
        super(context, layoutResId, datas);
    }


}
