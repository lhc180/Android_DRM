package cn.com.pyc.szpbb.sdk.callback;

import cn.com.pyc.szpbb.sdk.synchttp.SZBindDeviceHttp;
import cn.com.pyc.szpbb.sdk.synchttp.SZCheckReceiveHttp;
import cn.com.pyc.szpbb.sdk.synchttp.SZGetSharedHttp;
import cn.com.pyc.szpbb.sdk.synchttp.SZGetVerifyCodeHttp;
import cn.com.pyc.szpbb.sdk.synchttp.SZUserAccountHttp;

public class SZCreateFactory extends SZFactory
{

	public static SZCreateFactory create()
	{
		return SZCreateFactoryInstance.sInstance;
	}

	private SZCreateFactory()
	{
	}

	private static class SZCreateFactoryInstance
	{
		private static final SZCreateFactory sInstance = new SZCreateFactory();
	}

	@Override
	public SZUserAccountHttp getUserHttp()
	{
		// TODO Auto-generated method stub
		return new SZUserAccountHttp();
	}

	@Override
	public SZGetVerifyCodeHttp getVerifyCodeHttp()
	{
		// TODO Auto-generated method stub
		return new SZGetVerifyCodeHttp();
	}

	@Override
	public SZCheckReceiveHttp getCheckReceiveHttp()
	{
		// TODO Auto-generated method stub
		return new SZCheckReceiveHttp();
	}

	@Override
	public SZBindDeviceHttp getBindDeviceHttp()
	{
		// TODO Auto-generated method stub
		return new SZBindDeviceHttp();
	}

	@Override
	public SZGetSharedHttp getShareInfoHttp()
	{
		// TODO Auto-generated method stub
		return new SZGetSharedHttp();
	}

}
