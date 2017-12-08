package cn.com.pyc.drm.common;

import android.app.Application;

public class BaseApplication extends Application
{

	/**
	 * 配置模式<br/>
	 * 
	 * 开发调试模式下=true；发布线上版本=false
	 * 
	 * @author hudq
	 */
	public static class Config
	{
		public static final boolean DEVELOPER_MODE = true;
	}

	/**
	 * 渠道号
	 * 
	 * @author hudq
	 * 
	 */
	public static class Channel
	{
		public static final int LOCAL_SERVER = 100;
		public static final int _360_MARKET = 101;
		public static final int WANDOUJIA = 102;
		public static final int XIAOMI_MARKET = 103;
		public static final int ANDROID_MARKET = 104;
		public static final int YINGYONGBAO = 105;
		public static final int YINGYONGHUI = 106;
		public static final int ANZHI_MARKET = 107;
		public static final int BAIDU_MARKET = 108;
	}

}
