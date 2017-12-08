package cn.com.pyc.drm.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Looper;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.manager.ActicityManager;

import org.apache.http.HttpException;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 应用程序异常类：用于捕获异常和提示错误信息
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class AppException extends Exception implements UncaughtExceptionHandler
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8348851006411302923L;

	private final static boolean Debug = false;// 是否保存错误日志

	/**
	 * 定义异常类型
	 */
	public final static byte TYPE_NETWORK = 0x01;
	public final static byte TYPE_SOCKET = 0x02;
	public final static byte TYPE_HTTP_CODE = 0x03;
	public final static byte TYPE_HTTP_ERROR = 0x04;
	public final static byte TYPE_XML = 0x05;
	public final static byte TYPE_IO = 0x06;
	public final static byte TYPE_RUN = 0x07;
	public final static byte TYPE_JSON = 0x08;
	public final static byte TYPE_FILE = 0x09;
	public final static byte TYPE_FTP_CODE = 10;
	public final static byte TYPE_CIPHER = 11;
	public final static byte TYPE_OOM_CODE = 12;

	private byte type;
	private int code;

	/**
	 * 系统默认的UncaughtException处理类
	 */
	private UncaughtExceptionHandler mDefaultHandler;

	private AppException()
	{
		this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	}

	private AppException(byte type, int code, Exception excp)
	{
		super(excp);
		this.type = type;
		this.code = code;
		if (Debug)
		{
			// 保存异常日志值sd卡
		}
	}

	public int getCode()
	{
		return this.code;
	}

	public int getType()
	{
		return this.type;
	}

	public static AppException oom(Error e)
	{
		return new AppException(TYPE_OOM_CODE, 0, null);
	}

	public static AppException ftp(Exception e)
	{
		base(e);
		return new AppException(TYPE_FTP_CODE, 0, null);
	}

	public static AppException http(int code)
	{
		return new AppException(TYPE_HTTP_CODE, code, null);
	}

	public static AppException http(Exception e)
	{
		base(e);
		return new AppException(TYPE_HTTP_ERROR, 0, e);
	}

	public static AppException socket(Exception e)
	{
		base(e);
		return new AppException(TYPE_SOCKET, 0, e);
	}

	public static AppException cipher(Exception e)
	{
		base(e);
		return new AppException(TYPE_CIPHER, 0, e);
	}

	public static AppException io(Exception e)
	{
		base(e);
		if (e instanceof UnknownHostException || e instanceof ConnectException)
		{
			return new AppException(TYPE_NETWORK, 0, e);
		} else if (e instanceof IOException)
		{
			return new AppException(TYPE_IO, 0, e);
		}
		return run(e);
	}

	public static AppException file(Exception e)
	{
		base(e);
		return new AppException(TYPE_FILE, 0, e);
	}

	public static AppException xml(Exception e)
	{
		base(e);
		return new AppException(TYPE_XML, 0, e);
	}

	public static AppException json(Exception e)
	{
		base(e);
		return new AppException(TYPE_JSON, 0, e);
	}

	public static AppException network(Exception e)
	{
		base(e);
		if (e instanceof UnknownHostException || e instanceof ConnectException)
		{
			return new AppException(TYPE_NETWORK, 0, e);
		} else if (e instanceof HttpException)
		{
			return http(e);
		} else if (e instanceof SocketException)
		{
			return socket(e);
		}
		return http(e);
	}

	public static AppException run(Exception e)
	{
		base(e);
		return new AppException(TYPE_RUN, 0, e);
	}

	public static void base(Exception e)
	{
		e.printStackTrace();
	}

	/**
	 * 获取APP异常崩溃处理对象
	 * 
	 * @return
	 */
	public static AppException getAppExceptionHandler()
	{
		return new AppException();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex)
	{

		if (!handleException(ex) && mDefaultHandler != null)
		{
			mDefaultHandler.uncaughtException(thread, ex);
		}

	}

	/**
	 * 自定义异常处理:收集错误信息&发送错误报告
	 * 
	 * @param ex
	 * @return true:处理了该异常信息;否则返回false
	 */
	private boolean handleException(Throwable ex)
	{
		if (ex == null)
		{
			return false;
		}

		final Context context = ActicityManager.getInstance().currentActivity();

		if (context == null)
		{
			return false;
		}

		final String crashReport = getCrashReport(context, ex);
		DRMLog.e("crashReport", crashReport);
		// 显示异常信息&发送报告
		new Thread()
		{
			public void run()
			{
				Looper.prepare();
				// UIHelper.sendAppCrashReport(context, crashReport);
				Looper.loop();
			}

		}.start();
		return true;
	}

	/**
	 * 获取APP崩溃异常报告
	 * 
	 * @param ex
	 * @return
	 */
	private String getCrashReport(Context context, Throwable ex)
	{
		PackageInfo pinfo = CommonUtil.getPackageInfo();
		StringBuffer exceptionStr = new StringBuffer();
		exceptionStr.append("Version: " + pinfo.versionName + "("
				+ pinfo.versionCode + ")\n");
		exceptionStr.append("Android: " + android.os.Build.VERSION.RELEASE
				+ "(" + android.os.Build.MODEL + ")\n");
		exceptionStr.append("Exception: " + ex.getMessage() + "\n");
		StackTraceElement[] elements = ex.getStackTrace();
		for (int i = 0; i < elements.length; i++)
		{
			exceptionStr.append(elements[i].toString() + "\n");
		}
		return exceptionStr.toString();
	}

}
