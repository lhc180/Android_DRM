package cn.com.pyc.drm.utils;

import android.util.Log;
import cn.com.pyc.drm.common.SZConfig;

/**
 * 日志打印管理类
 * 
 * @author qd
 * 
 */
public class DRMLog
{

	private static final boolean flag = SZConfig.DEVELOPER_MODE;
	private static final String TAG = "DRM";
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
			if (st.getClassName().equals(DRMLog.class.getName()))
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

	/**
	 * 打印日志时获取当前的程序文件名、行号、方法名 输出格式为：[FileName | LineNumber | MethodName]
	 * 
	 * @return
	 */
	protected static String getFileLineMethod()
	{
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		StringBuffer toStringBuffer = new StringBuffer("[")
				.append(traceElement.getFileName()).append(" | ")
				.append(traceElement.getLineNumber()).append(" | ")
				.append(traceElement.getMethodName()).append("]");
		return toStringBuffer.toString();
	}

	// 当前文件名
	protected static String _FILE_()
	{
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		return traceElement.getFileName();
	}

	// 当前方法名
	protected static String _FUNC_()
	{
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		return traceElement.getMethodName();
	}

	// 当前行号
	protected static int _LINE_()
	{
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		return traceElement.getLineNumber();
	}

}
