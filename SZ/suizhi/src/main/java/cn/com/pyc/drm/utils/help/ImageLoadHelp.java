package cn.com.pyc.drm.utils.help;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import cn.com.pyc.drm.R;

/**
 * Created by hudaqiang on 2017/7/11.
 */

public class ImageLoadHelp {

    private static ImageOptions imageOptions;

    static {
        imageOptions = new ImageOptions.Builder()
                // 加载中或错误图片的ScaleType
                //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
                // 默认自动适应大小
                .setSize(480, 800)
                .setFailureDrawableId(R.drawable.transparent)
                .setLoadingDrawableId(R.drawable.transparent)
                .setIgnoreGif(false)
                // 如果使用本地文件url, 添加这个设置可以在本地文件更新后刷新立即生效.
                .setUseMemCache(true)
                .setFadeIn(true)
                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .build();
    }

    public static void loadImage(ImageView imageView, String url) {
        x.image().bind(imageView, url, imageOptions);
    }

    public static void loadDrawable(String url, Callback.CommonCallback<Drawable> callback) {
        x.image().loadDrawable(url, imageOptions, callback);
    }

    public static void clearCacheFiles() {
        x.image().clearCacheFiles();
    }

    public static void clearMemCache() {
        x.image().clearMemCache();
    }

    public static void clearCache() {
        clearCacheFiles();
        clearMemCache();
    }
}
