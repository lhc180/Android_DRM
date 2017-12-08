package tv.danmaku.ijk.media.widget;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 存储播放位置sp,
 * 
 * @author hudq
 * 
 */
public class SPManager
{

	private Context context;

	private SharedPreferences.Editor editor;
	private SharedPreferences prefs;
	private String PREFS_NAME = "config";

	public SPManager(Context context)
	{
		this.context = context;
		prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		editor = prefs.edit();
	}
	
	/**
	 * 清除保存
	 */
	public void clear()
	{
		editor.clear();
		editor.apply();
	}
	
	public void remove(String key)
	{
		editor.remove(key);
		editor.apply();
	}

	/**
	 * 保存键值
	 * 
	 * @param key
	 * @param value
	 */
	public void putInt(String key, int value)
	{
		editor.putInt(key, value);
		editor.apply();
	}

	/**
	 * 获取数值
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public int getInt(String key, int defValue)
	{
		return prefs.getInt(key, defValue);
	}
	
	/**
	 * 保存键值
	 * 
	 * @param key
	 * @param value
	 */
	public void putString(String key, String value)
	{
		editor.putString(key, value);
		editor.apply();
	}
	
	/**
	 * 获取数值
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public String getString(String key, String defValue)
	{
		return prefs.getString(key, defValue);
	}
	
}
