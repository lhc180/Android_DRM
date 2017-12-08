package cn.com.pyc.drm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;

/**
 * 序列化对象缓存工具类
 * 
 * @author hudq
 * 
 */
public class ObjectCacheUtil
{

	private final String TAG = "ObjectCacheUtil";

	private static String DEFAULT_SAVE_OBJ_FILE = "SZObject.ser";

	private static ObjectCacheUtil INSTANCE;

	public static ObjectCacheUtil init(Class<?> clazz)
	{
		if (null == INSTANCE)
		{
			INSTANCE = new ObjectCacheUtil();
		}
		// 初始化序列化对象的文件名
		String SAVE_OBJ_FILE = clazz.getSimpleName() + ".ser";
		DEFAULT_SAVE_OBJ_FILE = SAVE_OBJ_FILE;
		return INSTANCE;
	}

	public static ObjectCacheUtil init(String objName)
	{
		if (null == INSTANCE)
		{
			INSTANCE = new ObjectCacheUtil();
		}
		// 初始化序列化对象的文件名
		String SAVE_OBJ_FILE = objName + ".ser";
		DEFAULT_SAVE_OBJ_FILE = SAVE_OBJ_FILE;
		return INSTANCE;
	}

	private ObjectCacheUtil()
	{
	}

	/**
	 * 序列化存储对象
	 * 
	 * @param ctx
	 * @param obj
	 */
	public void saveObjToCache(Context ctx, Object obj)
	{
		File file = new File(ctx.getCacheDir(), DEFAULT_SAVE_OBJ_FILE);
		ObjectOutputStream os = null;
		try
		{
			DRMLog.i("write to cache: " + DEFAULT_SAVE_OBJ_FILE);
			os = new ObjectOutputStream(new FileOutputStream(file));
			os.writeObject(obj);
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (os != null)
			{
				try
				{
					os.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				os = null;
			}
		}
	}

	/**
	 * 读取序列化存储的object
	 * 
	 * @param ctx
	 * @return
	 */
	public Object readObjFromCache(Context ctx)
	{
		File file = new File(ctx.getCacheDir(), DEFAULT_SAVE_OBJ_FILE);
		Object obj = null;
		if (file != null && file.exists())
		{
			ObjectInputStream is = null;
			try
			{
				DRMLog.i("read from cache: " + DEFAULT_SAVE_OBJ_FILE);
				is = new ObjectInputStream(new FileInputStream(file));
				obj = is.readObject();
				DRMLog.i("readCache: " + (obj != null ? obj.toString() : -1));
			} catch (Exception e)
			{
				e.printStackTrace();
				file.delete();
			} finally
			{
				if (is != null)
				{
					try
					{
						is.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
					is = null;
				}
			}
		}
		return obj;
	}

}
