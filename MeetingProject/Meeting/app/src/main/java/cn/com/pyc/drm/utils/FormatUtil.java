package cn.com.pyc.drm.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtil
{

	/**
	 * 格式化显示文件大小 eg:2G;2.3M（小数位不为0显示一位小数，小数位为0不显示小数位）
	 * 
	 * @param size
	 * @return
	 */
	public static String formatSize(long size)
	{
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;
		long _10gb = 10 * gb;

		if (size > _10gb)
		{
			return "大于10G";
		} else if (size >= gb)
		{
			return String.format(Locale.getDefault(), "%.1fG", (float) size
					/ gb);
		} else if (size >= mb)
		{
			float f = (float) size / mb;
			return String.format(Locale.getDefault(), f > 100 ? "%.0fM"
					: "%.1fM", f);
		} else if (size >= kb)
		{
			float f = (float) size / kb;
			return String.format(Locale.getDefault(), f > 100 ? "%.0fKB"
					: "%.1fKB", f);
		} else
			return String.format(Locale.getDefault(), "%dB", size);
	}

	/**
	 * 规范播放时间：02:00，12:21
	 * 
	 * @param mills
	 * @return
	 */
	public static String formatTime(long mills)
	{
		// 毫秒--->秒。
		int totalSeconds = (int) (mills / 1000);

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		if (hours > 0)
		{
			return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
					seconds).toString();
		} else
		{
			return String.format(Locale.US, "%02d:%02d", minutes, seconds)
					.toString();
		}
	}

	/**
	 * 秒--->00:10
	 * 
	 * @param seconds
	 *            (音乐时间)
	 * @return
	 */
	public static String change2Time(int seconds)
	{
		int temp = 0;
		StringBuffer sb = new StringBuffer();
		temp = seconds / 60;
		sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");
		temp = seconds % 60;
		sb.append((temp < 10) ? "0" + temp : "" + temp);
		return sb.toString();
	}

	/**
	 * 显示时间：eg 2分20秒
	 * 
	 * @param millseconds
	 * @return
	 */
	public static String toTime(long millseconds)
	{
		if (millseconds <= 0) { return "0秒"; }
		millseconds /= 1000;
		int min = (int) (millseconds / 60);
		int sec = (int) (millseconds % 60);
		String str = "";
		if (min > 0)
		{
			str += min + "分";
		}
		if (sec > 0)
		{
			str += sec + "秒";
		} else
		{
			str += "钟";
		}
		return str;
	}

	/**
	 * 格式化时间显示,（权限剩余时间） <br/>
	 * 固定显示格式。<br/>
	 * -1: 永久有效 <br/>
	 * xx天xx小时
	 * 
	 * @param availableMills
	 * @return
	 */
	public static String getLeftAvailableTime(long availableMills)
	{
		int temp = 0;
		StringBuffer sb = new StringBuffer();
		if (availableMills == -1)
		{
			// 永久有效
			// sb.append(context.getString(R.string.forever_time));
			sb.append("-1");
			return sb.toString();
		}
		temp = (int) (availableMills / (60 * 60 * 24 * 1000));
		sb.append((temp < 10) ? "0" + temp + "天" : "" + temp + "天");

		temp = (int) (availableMills % (60 * 60 * 24 * 1000) / (60 * 60 * 1000));
		sb.append((temp < 10) ? "0" + temp + "小时" : "" + temp + "小时");

		return sb.toString();
	}

	/**
	 * 获取指定时间日期 yyyy-MM-dd，（格式化权限时间使用）
	 * <p>
	 * 
	 * eg: 2016-12-26 23:59:59
	 * 
	 * @param odd_endTime_mills
	 * @return
	 */
	public static String getToOddEndTime(long odd_endTime_mills)
	{
		// long mills = Double.valueOf(odd_endTime_mills).longValue();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		if (odd_endTime_mills == 0) //文件非永久，没有过期，如果权限到期时间为0，返回当前日期（权限出错）
			return sdf.format(new Date());

		return sdf.format(new Date(odd_endTime_mills));
	}

	/**
	 * 格式化时间，将毫秒转换为分:秒格式.01:20;11:02
	 * 
	 * @param time
	 * @return
	 */
	// public static String formatN_NTime(long time)
	// {
	// String min = time / (1000 * 60) + "";
	// String sec = time % (1000 * 60) + "";
	// if (min.length() < 2)
	// {
	// min = "0" + time / (1000 * 60) + "";
	// } else
	// {
	// min = time / (1000 * 60) + "";
	// }
	// if (sec.length() == 4)
	// {
	// sec = "0" + (time % (1000 * 60)) + "";
	// } else if (sec.length() == 3)
	// {
	// sec = "00" + (time % (1000 * 60)) + "";
	// } else if (sec.length() == 2)
	// {
	// sec = "000" + (time % (1000 * 60)) + "";
	// } else if (sec.length() == 1)
	// {
	// sec = "0000" + (time % (1000 * 60)) + "";
	// }
	// return min + ":" + sec.trim().substring(0, 2);
	// }

}
