package cn.com.pyc.drm.http;

import android.content.Context;

import cn.com.pyc.drm.ui.BaseActivity;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2017/1/20 11:44
 */
public abstract class SpotsCallBack<T> extends BaseCallBack<T> {

    private Context mContext;


    public SpotsCallBack(Context mContext) {
        this.mContext = mContext;
    }

    public void showDialog(BaseActivity mActivity) {
        mActivity.showBgLoading("正在加载...");
    }

    public void dismissDialog(BaseActivity mActivity) {
        mActivity.hideBgLoading();
    }

    // 调用接口前 弹出对话框 在此设置
    @Override
    public void onBeforeRequest() {

        //        showDialog();
    }

    // 在此设置关闭对话框
    @Override
    public void onFailure(Exception e) {
        //        dismissDialog();
    }

    // 再次设置关闭对话框
    @Override
    public void onResponse() {
        //        dismissDialog();
    }


}
