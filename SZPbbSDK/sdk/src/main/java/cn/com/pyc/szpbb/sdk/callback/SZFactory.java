package cn.com.pyc.szpbb.sdk.callback;

import cn.com.pyc.szpbb.sdk.synchttp.SZBindDeviceHttp;
import cn.com.pyc.szpbb.sdk.synchttp.SZCheckReceiveHttp;
import cn.com.pyc.szpbb.sdk.synchttp.SZGetSharedHttp;
import cn.com.pyc.szpbb.sdk.synchttp.SZGetVerifyCodeHttp;
import cn.com.pyc.szpbb.sdk.synchttp.SZUserAccountHttp;

/**
 * abstract Factory
 * 
 * @author hudq
 * 
 */
abstract class SZFactory
{
	/** 登录,登出,注册 */
	public abstract SZUserAccountHttp getUserHttp();

	/** 获取验证码 */
	public abstract SZGetVerifyCodeHttp getVerifyCodeHttp();

	/** 下载校验，接收手机号校验 */
	public abstract SZCheckReceiveHttp getCheckReceiveHttp();

	/** 绑定设备 */
	public abstract SZBindDeviceHttp getBindDeviceHttp();

	/** 获取分享信息 */
	public abstract SZGetSharedHttp getShareInfoHttp();

}
