package cn.com.pyc.szpbb.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtil {

    /**
     * 格式时间，n秒--->00:10, 04:21
     *
     * @param seconds 单位秒
     * @return String
     */
    public static String formatTimeSeconds(int seconds) {
        int temp = 0;
        StringBuilder sb = new StringBuilder();
        temp = seconds / 60;
        sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");
        temp = seconds % 60;
        sb.append((temp < 10) ? "0" + temp : "" + temp);
        return sb.toString();
    }

    /**
     * 格式时间：02:00，12:21
     *
     * @param mills 单位毫秒
     * @return String
     */
    public static String formatTimeMills(long mills) {
        // 毫秒--->秒。
        int totalSeconds = (int) (mills / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours,
                    minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes,
                    seconds);
        }
    }

    /**
     * 格式化显示文件大小 <br/>
     * eg:2G;2.3M（小数位不为0显示一位小数，小数位为0不显示小数位）
     *
     * @param size Long,长度大小
     * @return String
     */
    public static String formatSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        long _10gb = 10 * gb;

        if (size > _10gb) {
            return "大于10G";
        } else if (size >= gb) {
            return String.format(Locale.getDefault(), "%.1fG", (float) size
                    / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(Locale.getDefault(), f > 100 ? "%.0fM"
                    : "%.1fM", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(Locale.getDefault(), f > 100 ? "%.0fKB"
                    : "%.1fKB", f);
        } else
            return String.format(Locale.getDefault(), "%dB", size);
    }

    /**
     * 格式化时间显示,（权限剩余可用时间） <br/>
     *
     * @param availableMills 长度
     * @return 固定格式：<br/>
     * (1)  -1: 永久有效 <br/>
     * (2)   0: 已经过期 <br/>
     * (3)   xx天xx小时: 剩余有效期天数
     */
    public static String getLeftAvailableTime(long availableMills) {
        int temp = 0;
        StringBuilder sb = new StringBuilder();
        if (availableMills == -1) {
            // 永久有效
            return "-1";
        }
        temp = (int) (availableMills / (60 * 60 * 24 * 1000));
        sb.append((temp < 10) ? "0" + temp + "天" : temp + "天");

        temp = (int) (availableMills % (60 * 60 * 24 * 1000) / (60 * 60 * 1000));
        sb.append((temp < 10) ? "0" + temp + "小时" : temp + "小时");

        if ("00天00小时".equals(sb.toString())) {
            // 已经过期
            return "0";
        }
        return sb.toString();
    }

    /**
     * 获取权限到期的结束时间
     * <p>
     * <p>
     * eg: 有效期至：2016-12-30 23:59:59
     *
     * @param odd_endTime_mills Long,长度
     * @return String
     */
    public static String getToOddEndTime(long odd_endTime_mills) {
        // long mills = Double.valueOf(odd_endTime_mills).longValue();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        if (odd_endTime_mills == 0) // 文件非永久，没有过期，如果权限到期时间为0，返回0000（权限出错）
            return "-1";

        return sdf.format(new Date(odd_endTime_mills));
    }
}
