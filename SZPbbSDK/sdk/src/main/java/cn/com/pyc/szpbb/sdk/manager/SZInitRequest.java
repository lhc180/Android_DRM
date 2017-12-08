package cn.com.pyc.szpbb.sdk.manager;

import java.util.HashMap;

import cn.com.pyc.szpbb.sdk.response.SZResponse;
import cn.com.pyc.szpbb.sdk.response.SZResponseBindShare;
import cn.com.pyc.szpbb.sdk.response.SZResponseDownloadCheck;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetAllReceiveShare;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetShareFile;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetShareInfo;
import cn.com.pyc.szpbb.sdk.response.SZResponseGetVerifyCode;
import cn.com.pyc.szpbb.sdk.response.SZResponseLogin;
import cn.com.pyc.szpbb.sdk.response.SZResponseLoginout;
import cn.com.pyc.szpbb.sdk.response.SZResponseReceiveShare;
import cn.com.pyc.szpbb.sdk.response.SZResponseRegister;
import cn.com.pyc.szpbb.sdk.response.SZResponseVerifyCodeLogin;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestBindShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestDownloadCheck;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetAllReceiveShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetShareFile;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetShareInfo;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestGetVerifyCode;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestLogin;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestLoginout;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestReceiveShare;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestRegister;
import cn.com.pyc.szpbb.sdk.response.request.SZRequestVerifyCodeLogin;
import cn.com.pyc.szpbb.util.SZLog;

public class SZInitRequest
{
	// key=请求类名； value=响应类字节
	private static HashMap<String, Class<?>> hashRequestClass = null;

	/**
	 * TODO:添加请求响应，request和response一一对应：
	 */
	public static void initRequestClass()
	{
		hashRequestClass = new HashMap<String, Class<?>>();

		// 登录
		add(SZRequestLogin.class, SZResponseLogin.class);

		// 验证码登录
		add(SZRequestVerifyCodeLogin.class, SZResponseVerifyCodeLogin.class);

		// 获取验证码
		add(SZRequestGetVerifyCode.class, SZResponseGetVerifyCode.class);

		// 注册
		add(SZRequestRegister.class, SZResponseRegister.class);

		// 登出
		add(SZRequestLoginout.class, SZResponseLoginout.class);

		// 绑定设备
		add(SZRequestBindShare.class, SZResponseBindShare.class);

		// 下载校验
		add(SZRequestDownloadCheck.class, SZResponseDownloadCheck.class);

		// 接收手机号校验
		add(SZRequestReceiveShare.class, SZResponseReceiveShare.class);

		// 获取我的分享信息
		add(SZRequestGetShareInfo.class, SZResponseGetShareInfo.class);

		// 获取我的分享文件
		add(SZRequestGetShareFile.class, SZResponseGetShareFile.class);

		// 获取所有接收的分享
		add(SZRequestGetAllReceiveShare.class, SZResponseGetAllReceiveShare.class);

	}

	public static Class<?> getResponseClass(String requestName)
	{
		if (hashRequestClass == null) initRequestClass();

		if (hashRequestClass.containsKey(requestName))
		{
			SZLog.d("clazz", requestName);
			return hashRequestClass.get(requestName);
		}

		return SZResponse.class;
	}

	/**
	 * 添加key-value数据。
	 * 
	 * @param key
	 * @param value
	 */
	private static void add(Class<?> key, Class<?> value)
	{
		hashRequestClass.put(key.getSimpleName(), value);
	}

}
