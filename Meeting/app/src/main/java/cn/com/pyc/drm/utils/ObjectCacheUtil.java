package cn.com.pyc.drm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

	private static String SAVE_OBJ_FILE = "object.ser";

	private static ObjectCacheUtil INSTANCE;

	public static ObjectCacheUtil init(Class<?> clazz)
	{
		if (null == INSTANCE)
		{
			INSTANCE = new ObjectCacheUtil();
		}
		// 初始化序列化对象的文件名
		SAVE_OBJ_FILE = clazz.getSimpleName() + ".ser";
		return INSTANCE;
	}

	/**
	 * 序列化存储对象
	 * 
	 * @param ctx
	 * @param obj
	 */
	public void saveObjToCache(Context ctx, Object obj)
	{
		File file = new File(ctx.getCacheDir(), SAVE_OBJ_FILE);
		try
		{
			DRMLog.e(TAG, "write obj to cache: " + SAVE_OBJ_FILE);
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
			os.writeObject(obj);

			os.close();
		} catch (Exception e)
		{
			e.printStackTrace();
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
		File file = new File(ctx.getCacheDir(), SAVE_OBJ_FILE);
		Object obj = null;
		if (file != null && file.exists())
		{
			try
			{
				DRMLog.e(TAG, "read obj from cache: " + SAVE_OBJ_FILE);
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
				obj = is.readObject();

				is.close();
			} catch (Exception e)
			{
				e.printStackTrace();
				file.delete();
			}
		}
		return obj;
	}

}
