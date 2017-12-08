package cn.com.pyc.drm.utils.help;

import android.graphics.Bitmap;
import android.graphics.Color;

import cn.com.pyc.drm.bean.event._ColorPixEvent;
import cn.com.pyc.drm.utils.DRMLog;
import de.greenrobot.event.EventBus;

/**
 * Created by hudaqiang on 2017/10/31.
 */

public class BitmapPixHelp {


    /**
     * 是否是浅色
     */
    public static boolean isLightColor(_Color color) {
        return isLightColor(color.R, color.G, color.B);
    }

    public static boolean isLightColor(int r, int g, int b) {
        //  //RGB 模式转换成 YUV 模式，而 Y 是明亮度（灰阶）
        double y = r * 0.299 + g * 0.587 + b * 0.114;
        DRMLog.d("--------------LightY--------------- " + y);
        //y >= 192:浅色背景,否则就为深色背景
        return y >= 192;
    }

    /**
     * RGB值转化为十六进制颜色值，eg: #CC02FA
     */
    public static String rgbToColor(_Color color) {
        return "#" + toBrowserHexValue(color.R)
                + toBrowserHexValue(color.G)
                + toBrowserHexValue(color.B);
    }

    /**
     * 获取bitmap每一像素上的RGB
     */
    public static _Color calculateColorPix(Bitmap bitmap) {
        long r = 0;
        long g = 0;
        long b = 0;
        long count = 0;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for (int i = 0; i < width; i += 10) {
            for (int j = 0; (j < height / 10 || (height - height / 5) < j && j < height) ; j += 10) {
                int rgbPix = bitmap.getPixel(i, j);
                int red = Color.red(rgbPix);
                int green = Color.green(rgbPix);
                int blue = Color.blue(rgbPix);

                r += red;
                g += green;
                b += blue;

                count++;
            }
        }

        _Color color = new _Color();
        color.R = (int) (r / count);
        color.G = (int) (g / count);
        color.B = (int) (b / count);

        //发送通知，获取到了背景的颜色平均值了
        EventBus.getDefault().postSticky(new _ColorPixEvent(color));

        return color;
    }

    private static String toBrowserHexValue(int number) {
        StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
        while (builder.length() < 2) {
            builder.append("0");
        }
        return builder.toString().toUpperCase();
    }

    public static class _Color {
        public int R;
        public int G;
        public int B;
    }

}
