package cn.com.pyc.szpbb.demo;

import cn.com.pyc.szpbb.common.SZApplication;
import cn.com.pyc.szpbb.sdk.SZInitInterface;

public class App extends SZApplication
{
	// 继承SZApplication，并初始化
	
	@Override
	public void onCreate()
	{
		super.onCreate();

		SZInitInterface.setDebugMode(BuildConfig.DEBUG);
		SZInitInterface.init(this);
	}

}
