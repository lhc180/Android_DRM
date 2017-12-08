package cn.com.pyc.drm.utils;

import android.content.Context;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.com.meeting.drm.R;

/**
 * date时间相关类
 * 
 * @author hudaqiang
 * 
 */
public class TimeUtil
{

	public static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 从日期字符串得到日期<br/>
	 * 
	 * @param dateString
	 *            默认 yyyy-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	public static Date getDateFromDateString(String dateString)
	{
		if (TextUtils.isEmpty(dateString))
		{
			return null;
		}
		try
		{
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_STRING,Locale.getDefault());
			Date date = format.parse(dateString);
			return date;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 从日期字符串得到日期
	 * 
	 * @param dateString
	 * @param formatStr
	 *            自定义时间格式
	 * @return
	 */
	public static Date getDateFromDateString(String dateString, String formatStr)
	{
		if (TextUtils.isEmpty(dateString))
		{
			return null;
		}
		try
		{
			SimpleDateFormat format = new SimpleDateFormat(formatStr,Locale.getDefault());
			Date date = format.parse(dateString);
			return date;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 得到日期字符串 <br/>
	 * 使用程序默认的格式yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateString(Date date)
	{
		return getDateString(date, DATE_FORMAT_STRING);
	}

	/**
	 * 得到日期字符串<br/>
	 * 使用格式formatStr
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateString(Date date, String formatStr)
	{
		if (date == null)
		{
			return null;
		}
		try
		{
			SimpleDateFormat format = new SimpleDateFormat(formatStr,Locale.getDefault());
			String dateString = format.format(date);
			return dateString;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据当前时间来显示<br/>
	 * 当天：上午HH:mm、下午HH:mm，其他时间：yyyy-MM-dd
	 */
	public static String getAddDateString(Date date)
	{
		Calendar cal = Calendar.getInstance();
		int currDay = cal.get(Calendar.DAY_OF_MONTH);
		int currMonth = cal.get(Calendar.MONTH) + 1;
		int currYear = cal.get(Calendar.YEAR);
		cal.setTime(date);
		if (currYear == cal.get(Calendar.YEAR))
		{
			if (currMonth == cal.get(Calendar.MONTH) + 1)
			{
				if (currDay == cal.get(Calendar.DAY_OF_MONTH))
				{
					if (cal.get(Calendar.HOUR_OF_DAY) > 12)
					{
						return "下午 " + getDateString(date, "h:mm");
					} else
					{
						return "上午 " + getDateString(date, "h:mm");
					}
				}
			}
		}

		return getDateString(date, "yyyy-MM-dd");

	}

	/**
	 * 昨天，今天
	 * 
	 * @param context
	 * @param dstDate
	 * @return
	 */
	public static String getTimeString(Context context, Date dstDate)
	{
		Calendar dstCal = Calendar.getInstance();
		dstCal.setTime(dstDate);
		Calendar srcCal = Calendar.getInstance();

		if (isYesterday(dstCal, srcCal))
		{
			return context.getString(R.string.yesterday) + " " + new SimpleDateFormat("HH:mm",Locale.getDefault()).format(dstDate);
		} else
		{
			int dstYear = dstCal.get(Calendar.YEAR);
			int srcYear = srcCal.get(Calendar.YEAR);
			if (dstYear == srcYear)
			{
				int dstMonth = dstCal.get(Calendar.MONTH);
				int srcMonth = srcCal.get(Calendar.MONTH);
				if (dstMonth == srcMonth)
				{
					int dstDay = dstCal.get(Calendar.DAY_OF_MONTH);
					int srcDay = srcCal.get(Calendar.DAY_OF_MONTH);
					if (dstDay == srcDay)
					{
						return context.getString(R.string.today) + " " + new SimpleDateFormat("HH:mm",Locale.getDefault()).format(dstDate);
					} else
					{
						return new SimpleDateFormat("MM-dd HH:mm",Locale.getDefault()).format(dstDate);
					}
				} else
				{
					return new SimpleDateFormat("MM-dd HH:mm",Locale.getDefault()).format(dstDate);
				}
			} else
			{
				return new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(dstDate);
			}
		}

	}

	public static boolean isYesterday(Calendar dstCal, Calendar srcCal)
	{
		boolean flag = false;
		dstCal.add(Calendar.DAY_OF_MONTH, 1);
		flag = isSameDay(dstCal, srcCal);
		dstCal.add(Calendar.DAY_OF_MONTH, -1);
		return flag;
	}

	public static boolean isSameDay(Calendar dstCal, Calendar srcCal)
	{
		int dstYear = dstCal.get(Calendar.YEAR);
		int srcYear = srcCal.get(Calendar.YEAR);
		if (dstYear == srcYear)
		{
			int dstMonth = dstCal.get(Calendar.MONTH);
			int srcMonth = srcCal.get(Calendar.MONTH);
			if (dstMonth == srcMonth)
			{
				int dstDay = dstCal.get(Calendar.DAY_OF_MONTH);
				int srcDay = srcCal.get(Calendar.DAY_OF_MONTH);
				if (dstDay == srcDay)
				{
					return true;
				} else
				{
					return false;
				}
			} else
			{
				return false;
			}
		} else
		{
			return false;
		}
	}

	/**
	 * 几秒以前，几分钟以前， 刚刚
	 * 
	 * @param context
	 * @param date
	 * @return
	 */
	public static String getTimeLine(Context context, Date date)
	{
		if (date == null)
		{
			return "null";
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar curCal = Calendar.getInstance();

		if (cal.after(curCal))
		{
			cal = Calendar.getInstance();
		}

		cal.add(Calendar.HOUR_OF_DAY, 1);
		if (cal.after(curCal))
		{
			// 一小时内
			cal.add(Calendar.HOUR_OF_DAY, -1);
			cal.add(Calendar.SECOND, 60);
			if (cal.after(curCal))
			{
				// 一分钟内
				cal.add(Calendar.SECOND, -60);
				return context.getString(R.string.timeline_less_than_a_minute);
			} else
			{
				// 几分钟以前
				cal.add(Calendar.SECOND, -60);
				if (curCal.get(Calendar.HOUR) == cal.get(Calendar.HOUR))
				{
					return context.getString(R.string.timeline_minutes_ago, curCal.get(Calendar.MINUTE) - cal.get(Calendar.MINUTE));
				} else
				{
					return context.getString(R.string.timeline_minutes_ago, curCal.get(Calendar.MINUTE) + 60 - cal.get(Calendar.MINUTE));
				}
			}
		} else
		{
			cal.add(Calendar.HOUR_OF_DAY, -1);
			int curYear = curCal.get(Calendar.YEAR);
			int curMonth = curCal.get(Calendar.MONTH) + 1;
			int curDay = curCal.get(Calendar.DAY_OF_MONTH);

			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);

			if (curYear == year)
			{
				// 同年
				if (curMonth == month)
				{
					// 同月
					if (curDay == day)
					{
						// 同天
						return context.getString(R.string.timeline_today_time, hour, minute);
					} else
					{
						// 不同天
						if (curDay == day + 1)
						{
							// 昨天
							return context.getString(R.string.timeline_yestoday_time, hour, minute);
						} else
						{
							// 昨天以前
							return context.getString(R.string.timeline_this_year, month, day, hour, minute);
						}
					}
				} else
				{
					// 不同月
					return context.getString(R.string.timeline_this_year, month, day, hour, minute);
				}
			} else
			{
				// 不同年
				if (curYear == year + 1 && month == 12 && curMonth == 1 && day == 31 && curDay == 1)
				{
					// 昨天
					return context.getString(R.string.timeline_yestoday_time, hour, minute);
				} else
				{
					return context.getString(R.string.timeline_before_year, year, month, day, hour, minute);
				}
			}
		}
	}

	/**
	 * 获取当前时间， 格式 yyyy/MM/dd HH:mm:ss
	 * 
	 * @return
	 */
	public static String getCurrentTime()
	{
		return getCurrentTime("yyyy/MM/dd HH:mm:ss");
	}

	/**
	 * 获取当前时间
	 * 
	 * @param formatStr
	 *            自定义时间显示格式
	 * @return
	 */
	public static String getCurrentTime(String formatStr)
	{
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr,Locale.getDefault());// 可以方便地修改日期格式
		String CurrentTime = dateFormat.format(date);
		return CurrentTime;
	}

	/**
	 * 获取指定时间日期 yyyy-MM-dd，（格式化权限时间使用）
	 * 
	 * @param oddtime
	 * @return
	 */
	public static String getStringByLongMills(double oddtime)
	{

		long mills = new Double(oddtime).longValue();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()); // yyyy-MM-dd
																	// HH:mm:ss
		String dateStr = sdf.format(new Date(mills));
		return dateStr;
	}
	/**
	 * 获取指定时间日期 yyyy-MM-dd HH:mm:ss，（格式化权限时间使用）
	 * 
	 * @param oddtime
	 * @return
	 */
	public static String getStringByLongMillsEveryminute(double oddtime)
	{

		long mills = new Double(oddtime).longValue();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()); // yyyy-MM-dd
																	// HH:mm:ss
		String dateStr = sdf.format(new Date(mills));
		return dateStr;
	}

	/**
	 * 日期转换成时间戳
	 * @param timeString
	 * @return
	 * @throws ParseException
     */
	public static long getDateToTimestamp(String timeString) {
		String timeStamp = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date d;
		try {
			d = sdf.parse(timeString);
			long l = d.getTime();
			return l;
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}

	}

	/**
	* @Description: (用来睡眠的方法)
	* @author 李巷阳
	* @date 2016-10-27 下午2:25:08
	 */
	public static void sleep_time(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
