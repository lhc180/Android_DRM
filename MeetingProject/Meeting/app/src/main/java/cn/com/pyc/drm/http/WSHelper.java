package cn.com.pyc.drm.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import cn.com.meeting.drm.R;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.ui.BaseActivity;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.UIHelper;

import static cn.com.pyc.drm.common.Constant.SERVICE_NAME_SPACE;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (webservice访问网络工具类)
 * @date 2017/1/20 11:05
 */
public class WSHelper {

    private Handler mHandler;


    public WSHelper() {
        mHandler = new Handler(Looper.getMainLooper());
        requestData(new RequestCallBack(builder.mContext));
    }


    private static Builder builder;

    public static Builder newBuilder() {

        builder = new Builder();
        return builder;
    }


    public static class Builder {
        private BaseActivity mContext;
        private String mUrl;// webservice url
        private String MethodName;// 方法名
        private OnLoadDataListener callback;// 访问成功回调
        // 获取参数类型
        private Type mType;

        private HashMap<String, String> params = new HashMap<String, String>(10);


        public Builder setCallback(OnLoadDataListener callback) {
            builder.callback = callback;
            return builder;
        }

        public Builder setmUrl(String mUrl) {
            builder.mUrl = mUrl;
            return builder;
        }

        public Builder setMethodName(String methodName) {
            builder.MethodName = methodName;
            return builder;
        }

        public Builder putParam(String key, String value) {
            params.put(key, value);
            return builder;
        }


        public WSHelper build(BaseActivity context, Type type) {
            this.mContext = context;
            this.mType = type;// 返回值类型
            if (!CommonUtil.isNetConnect(mContext)) {
                UIHelper.showToast(mContext, mContext.getResources().getString(R.string.net_disconnected));
                return null;
            }
            return new WSHelper();

        }


    }


    public void requestData(final RequestCallBack Requestcallback) {


        Requestcallback.showDialog(builder.mContext);// 打开加载对话框
        new Thread(new Runnable() {
            @Override
            public void run() {
                final HttpTransportSE htse = new HttpTransportSE(builder.mUrl, 15000);
                htse.debug = true;
                final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                SoapObject soapObject = SoapObjectFormData(builder.params, builder.MethodName);// 赋值参数到SoapObject里面。
                envelope.bodyOut = soapObject;
                envelope.dotNet = true;
                FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
                    public String call() throws Exception {
                        htse.call(SERVICE_NAME_SPACE + builder.MethodName, envelope);
                        Requestcallback.dismissDialog(builder.mContext);// 关闭加载对话框
                        if (envelope.getResponse() != null) {
                            try {
                                SoapObject results = (SoapObject) envelope.bodyIn;
                                String str = results.getProperty(0).toString();
                                Object obj = JSON.parseObject(str, Requestcallback.mType);
                                if (obj != null) {
                                    callbackSuccess(Requestcallback, obj);
                                } else {
                                    callbackError(Requestcallback, Constant.connect_error, builder.mContext.getResources().getString(R.string.connect_error), null);// 连接失败
                                }
                            } catch (Exception ex) {
                                callbackError(Requestcallback, Constant.analysis_error, builder.mContext.getResources().getString(R.string.analysis_error), ex);// 解析失败
                            }
                            return null;
                        }
                        callbackError(Requestcallback, Constant.connect_error, builder.mContext.getResources().getString(R.string.connect_error), null);// 连接失败
                        return null;
                    }
                });
                new Thread(task).start();
                try {
                    task.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    callbackError(Requestcallback, Constant.connect_error, builder.mContext.getResources().getString(R.string.connect_error), e);// 连接失败
                    Requestcallback.dismissDialog(builder.mContext);
                }

            }
        }).start();
    }


    private void callbackSuccess(final BaseCallBack callback, final Object obj) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(obj);
            }
        });
    }


    private void callbackError(final BaseCallBack callback, final int code, final String error_str, final Exception e) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(error_str, e);
            }
        });
    }


    private SoapObject SoapObjectFormData(Map<String, String> params, String MethodName) {
        SoapObject mSoapObject = new SoapObject(SERVICE_NAME_SPACE, MethodName);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                mSoapObject.addProperty(entry.getKey(), entry.getValue());
            }
        }
        return mSoapObject;
    }


    // 访问网络的类，继承之前做的类，然后把返回值回调给调用者
    class RequestCallBack<T> extends SpotsCallBack<T> {

        public RequestCallBack(Context context) {
            super(context);
            super.mType = builder.mType;
        }


        @Override
        public void onSuccess(T t) {
            builder.callback.onSuccess(t);
        }

        @Override
        public void onError(String error_str, Exception e) {
            builder.callback.onError(error_str,e);
        }
    }


    public interface OnLoadDataListener<T> {


        void onSuccess(T t);


        void onError(String error_str, Exception e);


    }

}
