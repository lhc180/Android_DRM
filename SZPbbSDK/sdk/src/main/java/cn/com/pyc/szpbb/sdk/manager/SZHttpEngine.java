package cn.com.pyc.szpbb.sdk.manager;

import android.os.Bundle;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Map;
import java.util.Map.Entry;

import cn.com.pyc.szpbb.util.SZLog;

/**
 * http 请求类
 *
 * @author hudq
 */
public class SZHttpEngine {

    private static final String TAG = "http";

    /**
     * post请求,不添加请求头
     *
     * @param url
     * @param params
     * @param callback
     * @return
     */
    public static Callback.Cancelable post(String url, Bundle params,
                                           Callback.CommonCallback<String> callback) {
        return post(url, params, null, callback);
    }

    /**
     * post请求
     *
     * @param url
     * @param params
     * @param header   请求头
     * @param callback
     * @return
     */
    public static Callback.Cancelable post(String url, Bundle params,
                                           Map<String, String> header, Callback
                                                   .CommonCallback<String> callback) {
        RequestParams arg0 = createPostParamsUrl(url, params, header);
        return x.http().post(arg0, callback);
    }

    /**
     * get请求,不添加请求头header
     *
     * @param url
     * @param params
     * @param callback
     * @return
     */
    public static Callback.Cancelable get(String url, Bundle params,
                                          Callback.CommonCallback<String> callback) {
        return get(url, params, null, callback);
    }

    /**
     * get请求
     *
     * @param url
     * @param params
     * @param header   请求头
     * @param callback
     * @return
     */
    public static Callback.Cancelable get(String url, Bundle params,
                                          Map<String, String> header, Callback
                                                  .CommonCallback<String> callback) {
        RequestParams arg0 = createGetParamsUrl(url, params, header);
        return x.http().get(arg0, callback);
    }

    /**
     * Post: Params
     *
     * @param url
     * @param params
     * @param header 请求头，Map集合，为空则不传
     * @return
     */
    private static RequestParams createPostParamsUrl(String url, Bundle params,
                                                     Map<String, String> header) {
        SZLog.d(TAG, "POST: " + url);
        RequestParams requestParams = new RequestParams(url);
        requestParams.setMethod(HttpMethod.POST);
        requestParams.setConnectTimeout(30 * 1000);
        requestParams.setUseCookie(false);

        if (null != header) {
            for (Entry<String, String> e : header.entrySet()) {
                requestParams.addHeader(e.getKey(), e.getValue());
                SZLog.d("header", e.getKey() + " = " + e.getValue());
            }
        }
        if (null != params) {
            for (String key : params.keySet()) {
                Object obj = params.get(key);
                // 添加到请求body体的参数, 只有POST, PUT, PATCH, DELETE请求支持.
                requestParams.addBodyParameter(key,
                        String.valueOf(params.get(key)));
                SZLog.d("params", key + " = " + obj);
            }
        }
        SZLog.d(TAG, "RequestParams: "
                + requestParams.getBodyParams().toString());
        return requestParams;
    }

    /**
     * Get：Params
     *
     * @param url
     * @param params
     * @param header 请求头，Map集合，为空则不传
     * @return
     */
    private static RequestParams createGetParamsUrl(String url, Bundle params,
                                                    Map<String, String> header) {
        SZLog.d(TAG, "GET: " + url);
        RequestParams requestParams = new RequestParams(url);
        requestParams.setMethod(HttpMethod.GET);
        requestParams.setConnectTimeout(30 * 1000);
        requestParams.setUseCookie(false);
        if (null != header) {
            for (Entry<String, String> e : header.entrySet()) {
                requestParams.addHeader(e.getKey(), e.getValue());
                SZLog.d("header", e.getKey() + " = " + e.getValue());
            }
        }
        if (null != params) {
            for (String key : params.keySet()) {
                Object obj = params.get(key);
                // 加到url里的参数, http://xxxx/s?key=value&key2=value2
                requestParams.addQueryStringParameter(key,
                        String.valueOf(params.get(key)));
                SZLog.d("params", key + " = " + obj);
            }
        }
        SZLog.d(TAG, "RequestParams: " + requestParams.toString());
        return requestParams;
    }

    /**
     * 同步get请求
     *
     * @param url
     * @param params
     * @return
     */
    public static String getSyncString(String url, Bundle params) {
        return getSyncString(url, params, null);
    }

    /**
     * 同步get请求
     *
     * @param url
     * @param params
     * @param header
     * @return
     */
    public static String getSyncString(String url, Bundle params, Map<String, String> header) {
        return getSync(url, params, header, String.class);
    }

    /**
     * get请求，同步方式
     *
     * @param url
     * @param params
     * @param resultType
     * @return
     */
    private static <T> T getSync(String url, Bundle params, Map<String, String> header, Class<T>
            resultType) {
        RequestParams arg0 = createGetParamsUrl(url, params, header);
        try {
            return x.http().getSync(arg0, resultType);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 取消对应的http请求
     *
     * @param cancelable
     */
    public static void cancelHttp(Callback.Cancelable cancelable) {
        if (cancelable != null && !cancelable.isCancelled()) {
            SZLog.i("cancel http.");
            cancelable.cancel();
            cancelable = null;
        }
    }
}
