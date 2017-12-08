package cn.com.pyc.szpbb.common;

import android.content.Context;

import cn.com.pyc.szpbb.util.DeviceUtil;
import cn.com.pyc.szpbb.util.SecurityUtil;

/**
 * 存储一下临时常量或值<br/>
 * <br/>
 * <br/>
 * 在APP的onCreate()开始时调用
 */
public final class Constant {

    /**
     * 设备imei号（如果imei取值为空，则会取值mac地址）
     */
    static String DEVICEID;
    private static String clipherKey = "";

    /***
     * 初始化一些值，在APP的onCreate()开始时调用
     *
     * @param context getApplicationContext();
     */
    public static void init(Context context) {
        String imei = DeviceUtil.getIMEI(context);
        DEVICEID = (imei != null && imei.length() > 0) ? SecurityUtil.encryptBASE64(imei) :
                DeviceUtil.getLocalMacAddress(context).replace(":", "");
        clipherKey = "CE93A89B3DB2EF4E";
    }

    public static String getClipherKey() {
        if (clipherKey == null || clipherKey.length() == 0) {
            return "E93A89B3DB2EF4E";
        }
        return clipherKey.substring(1);
    }
}
