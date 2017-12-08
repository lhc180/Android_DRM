package cn.com.pyc.szpbb.sdk.callback;

import java.util.List;

import cn.com.pyc.szpbb.sdk.response.SZResponse;

/**
 * @Description: (用一句话描述该文件做什么)
 * @author 李巷阳
 * @date 2016-9-20 下午3:58:48
 * @version V1.0
 * @param <T>
 */
public interface ISZCallBackArray<T extends SZResponse>
{

	/**
	 * 成功
	 * 
	 * @param response
	 */
	void onSuccess(List<T> response);

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
