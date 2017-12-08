package cn.com.pyc.drm.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class MyLruCache extends LruCache<String, Bitmap>{
	public MyLruCache(int maxSize)
	{
		super(maxSize);
	}

	@Override
	protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
			Bitmap newValue)
	{
		super.entryRemoved(evicted, key, oldValue, newValue);
	}

	@Override
	protected int sizeOf(String key, Bitmap value)
	{
		return value.getByteCount();	
	}
}
