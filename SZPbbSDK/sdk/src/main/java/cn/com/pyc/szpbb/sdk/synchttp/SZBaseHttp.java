package cn.com.pyc.szpbb.sdk.synchttp;

import cn.com.pyc.szpbb.sdk.callback.SZCommonCallbackEngine;
import cn.com.pyc.szpbb.sdk.manager.SZRequestManager;

class SZBaseHttp {
    static {
        sCommonBack = SZCommonCallbackEngine.getInstance();
        sRequestManager = SZRequestManager.getInstance();
    }

    static final SZCommonCallbackEngine sCommonBack;    //回调管理类
    static final SZRequestManager sRequestManager; //请求管理类

}
