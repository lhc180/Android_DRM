package cn.com.pyc.drm.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;


/**
 * desc:   网络连接与否切换广播接收       <br/>
 * author: hudaqiang       <br/>
 * create at 2017/6/30 15:18
 */
public abstract class NetworkChangedReceiver extends BroadcastReceiver {

    //注册
    public NetworkChangedReceiver(Activity activity) {
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        activity.registerReceiver(this, netFilter);
    }

    //反注册
    public void abortReceiver(Activity activity) {
        activity.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean result = CommonUtil.isNetConnect(context);
        DRMLog.i("NetworkChanged: " + result);
        if (result) {
            Constant.initDeviceId(context);
        }
        if (intent != null && ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            onNetChange(result);
        }
    }

    protected abstract void onNetChange(boolean isConnect);
}
