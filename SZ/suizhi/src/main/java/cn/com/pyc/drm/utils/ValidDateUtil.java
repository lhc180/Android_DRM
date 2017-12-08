package cn.com.pyc.drm.utils;

import android.content.Context;

import cn.com.pyc.drm.R;

public class ValidDateUtil
{

	private static final String TAG = "ValidDate";

	/**
	 * 获取权限时间
	 * <p>
	 * eg：<br/>
	 * -1 永久有效<br/>
	 * 0 已经过期<br/>
	 * 2016-12-12 23:59:59 到期
	 * 
	 * @param context
	 * @param availableTime
	 *            可用时间
	 * @param endOddTime
	 *            截止时间。可用时间如果是永久，则截止时间为0
	 * @return
	 */
	public static String getValidTime(Context context, long availableTime, long endOddTime)
	{
		DRMLog.i("---------------");
		//DRMLog.d(TAG, "availableTime: " + availableTime);
		//DRMLog.d(TAG, "endOddTime: " + endOddTime);
		String available = FormatUtil.getLeftAvailableTime(availableTime);
		String deadTime = "";
		DRMLog.d(TAG, "available: " + available);
		if ("00天00小时".equals(available))
		{
			// 已经过期
			deadTime = context.getString(R.string.over_time);
		} else if ("-1".equals(available))
		{
			// 永久
			deadTime = context.getString(R.string.forever_time);
		} else
		{
			String odd_datetime_end = FormatUtil.getToOddEndTime(endOddTime);
			deadTime = context.getString(R.string.deadline_time, odd_datetime_end);
		}
		DRMLog.d(TAG, "time: " + deadTime);
		DRMLog.i("---------------");
		return deadTime;
	}

	/**
	 * 获取权限时间
	 * <p>
	 * eg：<br/>
	 * -1 永久有效<br/>
	 * 0 已经过期<br/>
	 * 2016-12-12 23:59:59 到期
	 * 
	 * @param context
	 * @param available_time
	 * @param odd_endTime
	 *            截止时间。可用时间如果是永久，则截止时间为0，会默认返回当前时间
	 * @return
	 */
	public static String getValidTime(Context context, String available_time, String odd_endTime)
	{
		DRMLog.i("---------------");
		DRMLog.d(TAG, "available_time: " + available_time);
		String deadTime = "";
		if ("00天00小时".equals(available_time))
		{
			// 已经过期
			deadTime = context.getString(R.string.over_time);
		} else if ("-1".equals(available_time))
		{
			// 永久
			deadTime = context.getString(R.string.forever_time);
		} else
		{
			DRMLog.d(TAG, "odd_endTime: " + odd_endTime);
			deadTime = context.getString(R.string.deadline_time, odd_endTime);
		}
		DRMLog.d(TAG, "time: " + deadTime);
		DRMLog.i("---------------");
		return deadTime;
	}
}
