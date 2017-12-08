package cn.com.pyc.szpbb.util;

public final class APIUtil
{

	/**
	 * 部署环境
	 * 
	 * @return
	 */
	private static String getDeployServer()
	{
		return "http://124.205.155.154:8656";
	}

	/**
	 * 获取验证码<br/>
	 * 
	 * 
	 * @return
	 */
	public static final String getVerifyCodeUrl()
	{
		return getDeployServer()
				+ "/PBBOnline/client/security/registerSendValidateCode";
	}

	/**
	 * 注册<br/>
	 * 
	 * @return
	 */
	public static final String getRegisterUrl()
	{
		return getDeployServer() + "/PBBOnline/client/security/register";
	}

	/**
	 * 登录<br/>
	 * 
	 * @return
	 */
	public static final String getLoginUrl()
	{
		return getDeployServer() + "/PBBOnline/client/security/login";
	}

	/**
	 * 退出登录<br/>
	 * 
	 * @return
	 */
	public static final String getExitLoginUrl()
	{
		return getDeployServer() + "/PBBOnline/client/security/logout";
	}

	/**
	 * 短信验证码登录<br/>
	 * 
	 * @return
	 */
	public static final String getVerifyCodeLoginUrl()
	{
		return getDeployServer() + "/PBBOnline/client/security/checkLogin";
	}

	/**
	 * 获取分享信息
	 * 
	 * @return
	 */
	public static final String getShareInfoUrl()
	{
		return getDeployServer() + "/PBBOnline/client/content/getShareInfo";
	}

	/**
	 * 获取分享信息2
	 * 
	 * @return
	 */
	public static final String getShareInfo2Url()
	{
		return getDeployServer() + "/PBBOnline/client/content/getShareInfo2";
	}

	/**
	 * 下载校验
	 * 
	 * @return
	 */
	public static final String getDownloadCheckUrl()
	{
		return getDeployServer() + "/PBBOnline/client/content/downloadCheck";
	}

	/**
	 * 绑定设备
	 * 
	 * @return
	 */
	public static final String getBindDevicesUrl()
	{
		return getDeployServer() + "/PBBOnline/client/content/bindDevice";
	}

	/**
	 * 检验手机号是否是被分享者
	 * 
	 * @return
	 */
	public static final String getReceiverByPhoneUrl()
	{
		return getDeployServer() + "/PBBOnline/client/content/receiveByPhone";
	}

	/**
	 * 获取所有我接收的分享
	 * 
	 * @return
	 */
	public static final String getAllReceiveSharesUrl()
	{
		return getDeployServer()
				+ "/PBBOnline/client/content/getAllReceiveShares";
	}

	/**
	 * 获取分享下的文件
	 * 
	 * @return
	 */
	public static final String getShareFileUrl()
	{
		return getDeployServer() + "/PBBOnline/client/content/getShareFile";
	}

	/**
	 * 通过短码获取分享信息
	 * 
	 * @return
	 */
	public static final String getShareInfoByCodeUrl()
	{
		return getDeployServer()
				+ "/PBBOnline/client/content/getShareInfoByCode";
	}

}
