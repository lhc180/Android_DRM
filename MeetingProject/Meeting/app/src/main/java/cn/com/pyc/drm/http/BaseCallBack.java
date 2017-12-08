package cn.com.pyc.drm.http;

import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.com.pyc.drm.ui.BaseActivity;

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @date 2017/1/20 11:35
 */
public abstract class BaseCallBack<T> {

    public Type mType;

    static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }
    // 把调用的泛型 赋值给mType,后期解析gson获取。
    public BaseCallBack() {
        mType = getSuperclassTypeParameter(getClass());
    }


    /**
     * 打开加载框
     *
     * @param request
     */
    public abstract void showDialog(BaseActivity mActivity);


    /**
     * 关闭加载框
     *
     * @param request
     */
    public abstract void dismissDialog(BaseActivity mActivity);


    /**
     * 调用网络接口前 调用此接口 用于弹出加载对话框提示
     *
     * @param request
     */
    public abstract void onBeforeRequest();


    /**
     * 请求网络失败   比如无法连接网络
     *
     * @param request
     * @param e
     */
    public abstract void onFailure(Exception e);


    /**
     * 请求成功时调用此方法
     *
     * @param response
     */
    public abstract void onResponse();

    /**
     * 状态码大于200，小于300 时调用此方法
     *
     * @param response
     * @param t
     * @throws IOException
     */
    public abstract void onSuccess(T t);

    /**
     * 状态码400，404，403，500等时调用此方法
     * 300 代表解析失败。
     *
     * @param response
     * @param code
     * @param e
     */
    public abstract void onError(String error_str, Exception e);



}
