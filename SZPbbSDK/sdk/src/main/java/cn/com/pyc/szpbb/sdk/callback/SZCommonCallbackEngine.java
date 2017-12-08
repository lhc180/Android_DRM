package cn.com.pyc.szpbb.sdk.callback;

import com.alibaba.fastjson.JSON;

import org.xutils.common.Callback;

import java.util.List;

import cn.com.pyc.szpbb.sdk.manager.SZInitRequest;
import cn.com.pyc.szpbb.sdk.response.SZResponse;
import cn.com.pyc.szpbb.util.SZLog;

public class SZCommonCallbackEngine {
    private static class SZCommonCallbackSingleton {
        private static final SZCommonCallbackEngine sInstance = new SZCommonCallbackEngine();
    }

    private SZCommonCallbackEngine() {
    }

    public static SZCommonCallbackEngine getInstance() {
        return SZCommonCallbackSingleton.sInstance;
    }

    /**
     * @param requestClazz
     * @param callBack
     * @return
     */
    public final Callback.CommonCallback<String> getCommonCallback(
            final Class<?> requestClazz, final ISZCallBack callBack) {
        return new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String arg0) {
                SZLog.d("callback", arg0 + "");
                if (callBack != null)
                    callBack.onSuccess(parserObject(arg0, requestClazz));
            }

            @Override
            public void onFinished() {
                if (callBack != null) callBack.onFinished();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                if (callBack != null) callBack.onFailed(arg0.getMessage());
            }

            @Override
            public void onCancelled(CancelledException arg0) {
            }
        };
    }

    public final Callback.CommonCallback<String> getCommonCallback(
            final Class<?> requestClazz, final ISZCallBackArray callBack) {
        return new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String arg0) {
                SZLog.d("callback", arg0 + "");
                if (callBack != null)
                    callBack.onSuccess(parserArray(arg0, requestClazz));
            }

            @Override
            public void onFinished() {
                if (callBack != null) callBack.onFinished();
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                if (callBack != null) callBack.onFailed(arg0.getMessage());
            }

            @Override
            public void onCancelled(CancelledException arg0) {
            }
        };
    }

    private SZResponse parserObject(String result, Class<?> request) {
        String requestName = request.getSimpleName();
        Class<?> clazz = SZInitRequest.getResponseClass(requestName);
        return (SZResponse) JSON.parseObject(result, clazz);
    }

    @SuppressWarnings("unchecked")
    private List<SZResponse> parserArray(String result, Class<?> request) {
        String requestName = request.getSimpleName();
        Class<?> clazz = SZInitRequest.getResponseClass(requestName);
        return (List<SZResponse>) JSON.parseArray(result, clazz);
    }

}
