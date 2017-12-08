package cn.com.pyc.szpbb.sdk.manager;

import java.lang.ref.SoftReference;
import java.util.WeakHashMap;

import org.xutils.common.Callback.Cancelable;

/**
 * 请求管理类
 * 
 * @author hudq
 * 
 */
public class SZRequestManager
{
	private volatile static WeakHashMap<String, SoftReference<Cancelable>> https = new WeakHashMap<>();
	private volatile static SZRequestManager sInstance;

	private SZRequestManager()
	{
		https.clear();
	}

	public static SZRequestManager getInstance()
	{
		if (null == sInstance)
		{
			synchronized (SZRequestManager.class)
			{
				if (null == sInstance) sInstance = new SZRequestManager();
			}
		}
		return sInstance;
	}

	/**
	 * 添加某个请求
	 * 
	 * @param request
	 * @return
	 */
	public final void put(Class<?> request, Cancelable cancelable)
	{
		if (cancelable == null) return;
		if (https != null && !https.containsKey(request))
		{
			SoftReference<Cancelable> reference = new SoftReference<Cancelable>(
					cancelable);
			https.put(request.getSimpleName(), reference);
		}
	}

	/**
	 * 取出某个请求
	 * 
	 * @param request
	 * @return
	 */
	public final Cancelable get(Class<?> request)
	{
		if (https != null && https.containsKey(request))
		{
			SoftReference<Cancelable> reference = https.get(request
					.getSimpleName());
			if (reference != null) { return reference.get(); }
		}
		return null;
	}

	/**
	 * 获取所有请求
	 * 
	 * @return
	 */
	public WeakHashMap<String, SoftReference<Cancelable>> getAllHttps()
	{
		return https;
	}

}
