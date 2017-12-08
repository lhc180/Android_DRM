package cn.com.pyc.szpbb.sdk.callback;

import cn.com.pyc.szpbb.sdk.response.SZResponse;

/**
 * 访问请求回调接口
 * 
 * @author hudq
 * 
 * @param <T>
 */
public interface ISZCallBack<T extends SZResponse>
{

	/**
	 * 成功
	 * 
	 * @param response
	 */
	void onSuccess(T response);

	/**
	 * 失败
	 * 
	 * @param throwable
	 */
	void onFailed(String throwable);

	/**
	 * 结束，（无论成功或失败，该方法都会执行）
	 */
	void onFinished();

}
