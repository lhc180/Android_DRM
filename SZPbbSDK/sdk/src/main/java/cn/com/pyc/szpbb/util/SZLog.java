package cn.com.pyc.szpbb.util;

import cn.com.pyc.szpbb.sdk.SZInitInterface;
import android.util.Log;

/**
 * 日志打印管理类
 * 
 * @author hudq
 * 
 */
public class SZLog
{

	private static final boolean flag = SZInitInterface.isDebugMode;
	private static final String TAG = "pyc";
	private static final String CODER_NAME = "";

	// eg： hudq[Test.java|86|printlog]:打印一条日志
	private static String getFunctionName()
	{
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) { return null; }
		for (StackTraceElement st : sts)
		{
			if (st.isNativeMethod())
			{
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName()))
			{
				continue;
			}
			if (st.getClassName().equals(SZLog.class.getName()))
			{
				continue;
			}
			return CODER_NAME + "[" + st.getFileName() + "|"
					+ st.getLineNumber() + "|" + st.getMethodName() + "]";
		}
		return null;
	}

	public static void i(String msg)
	{
		if (flag)
		{
			String name = getFunctionName();
			if (name != null)
			{
				Log.i(TAG, name + ":" + msg);
			} else
			{
				Log.i(TAG, msg);
			}
		}
	}

	public static void d(String msg)
	{
		if (flag)
		{
			String name = getFunctionName();
			if (name != null)
			{
				Log.d(TAG, name + ":" + msg);
			} else
			{
				Log.d(TAG, msg);
			}
		}
	}

	public static void d(String tag, String msg)
	{
		if (flag)
		{
			Log.d(tag, msg);
		}
	}

	public static void e(String msg)
	{
		if (flag)
		{
			String name = getFunctionName();
			if (name != null)
			{
				Log.e(TAG, name + ":" + msg);
			} else
			{
				Log.e(TAG, msg);
			}
		}
	}

	public static void w(String msg)
	{
		if (flag)
		{
			String name = getFunctionName();
			if (name != null)
			{
				Log.w(TAG, name + ":" + msg);
			} else
			{
				Log.w(TAG, msg);
			}
		}
	}

	public static void v(String msg)
	{
		if (flag)
		{
			String name = getFunctionName();
			if (name != null)
			{
				Log.v(TAG, name + ":" + msg);
			} else
			{
				Log.v(TAG, msg);
			}
		}
	}

	public static void v(String tag, String msg)
	{
		if (flag)
		{
			Log.v(tag, msg);
		}
	}

}
