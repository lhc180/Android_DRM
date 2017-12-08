package cn.com.pyc.drm.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

public class GlobalLruCache extends LruCache<String, Bitmap>
{
	private static GlobalLruCache globalLruCache;

	public static GlobalLruCache getGLC()
	{
		if (globalLruCache == null)
		{
			globalLruCache = new GlobalLruCache((int) (Runtime.getRuntime()
					.maxMemory() / 8));
		}
		return globalLruCache;
	}

	private GlobalLruCache(int maxSize)
	{
		super(maxSize);
	}

	@Override
	protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
			Bitmap newValue)
	{
		super.entryRemoved(evicted, key, oldValue, newValue);
	}

	// 一定要重写此方法，这样才不会OOM
	@Override
	protected int sizeOf(String key, Bitmap value)
	{
		return value.getByteCount();	// 最主要是这里，返回缩略图的大小，存储的每一个缩略图的总和不会超过maxSize
	}

}
