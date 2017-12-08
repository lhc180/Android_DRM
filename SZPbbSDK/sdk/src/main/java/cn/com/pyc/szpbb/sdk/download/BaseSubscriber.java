package cn.com.pyc.szpbb.sdk.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

public abstract class BaseSubscriber<T extends Parcelable> extends BroadcastReceiver {
    public BaseSubscriber() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    }

    /**
     * 进度更新
     *
     * @param data        T: Parcelable
     * @param progress    进度
     * @param currentSize 当前下载大小
     */
    protected abstract void downloadProgress(T data, int progress, long currentSize);

    /**
     * 下载失败
     *
     * @param data T: Parcelable
     */
    protected abstract void downloadFailed(T data);

    /*
     * 连接失败
     *
     * @param data T: Parcelable
     */
    //protected abstract void connectError(T data);

    /**
     * 等待中
     *
     * @param data T: Parcelable
     */
    protected abstract void waiting(T data);

    /**
     * 正在连接中
     *
     * @param data T: Parcelable
     */
    protected abstract void connecting(T data);

    /*
     * 解析中
     *
     * @param data T: Parcelable
     */
    //protected abstract void parsering(T data);

    /**
     * 下载结束
     *
     * @param data T: Parcelable
     */
    protected abstract void downloadFinished(T data);
}
