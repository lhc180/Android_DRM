package cn.com.pyc.drm.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片操作工具包
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ImageUtils {

    public static final String SDCARD_MNT = "/mnt/sdcard";
    public static final String SDCARD = "/sdcard";

    /**
     * 图片的八个位置*
     */
    private static final int TOP = 0; // 上
    private static final int BOTTOM = 1; // 下
    private static final int LEFT = 2; // 左
    private static final int RIGHT = 3; // 右
    private static final int LEFT_TOP = 4; // 左上
    private static final int LEFT_BOTTOM = 5; // 左下
    private static final int RIGHT_TOP = 6; // 右上
    private static final int RIGHT_BOTTOM = 7; // 右下

    /**
     * 图像的放大缩小方法
     *
     * @param src    源位图对象
     * @param scaleX 宽度比例系数
     * @param scaleY 高度比例系数
     * @return 返回位图对象
     */
    public static Bitmap zoomBitmap(Bitmap src, float scaleX, float scaleY) {
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    /**
     * 图像放大缩小--根据宽度和高度
     *
     * @param src    源位图对象
     * @param width  宽
     * @param height 高
     * @return 返回位图对象
     */
    public static Bitmap zoomBimtap(Bitmap src, int width, int height) {
        return Bitmap.createScaledBitmap(src, width, height, true);
    }

    /**
     * Bitmap转byte[]
     *
     * @param bitmap
     * @return
     */
    public static byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return out.toByteArray();
    }

    /**
     * byte[]转Bitmap
     *
     * @param data
     * @return
     */
    public static Bitmap byteToBitmap(byte[] data) {
        if (data.length != 0) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    /**
     * 绘制带圆角的图像
     *
     * @param src
     * @param radius
     * @return
     */
    public static Bitmap createRoundedCornerBitmap(Bitmap src, int radius) {
        final int w = src.getWidth();
        final int h = src.getHeight();
        // 高清量32位图
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff424242);
        // 防止边缘的锯齿
        paint.setFilterBitmap(true);
        Rect rect = new Rect(0, 0, w, h);
        RectF rectf = new RectF(rect);
        // 绘制带圆角的矩形
        canvas.drawRoundRect(rectf, radius, radius, paint);

        // 取两层绘制交集，显示上层
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // 绘制图像
        canvas.drawBitmap(src, rect, rect, paint);
        return bitmap;
    }

    /**
     * 创建选中带提示图片
     *
     * @param context
     * @param srcId
     * @param tipId
     * @return
     */
    public static Drawable createSelectedTip(Context context, int srcId, int tipId) {
        Bitmap src = BitmapFactory
                .decodeResource(context.getResources(), srcId);
        Bitmap tip = BitmapFactory
                .decodeResource(context.getResources(), tipId);
        final int w = src.getWidth();
        final int h = src.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        // 绘制原图
        canvas.drawBitmap(src, 0, 0, paint);
        // 绘制提示图片
        canvas.drawBitmap(tip, (w - tip.getWidth()), 0, paint);
        return bitmapToDrawable(bitmap);
    }

    /**
     * 带倒影的图像
     *
     * @param src
     * @return
     */
    public static Bitmap createReflectionBitmap(Bitmap src) {
        // 两个图像间的空隙
        final int spacing = 4;
        final int w = src.getWidth();
        final int h = src.getHeight();
        // 绘制高质量32位图
        Bitmap bitmap = Bitmap.createBitmap(w, h + h / 2 + spacing,
                Config.ARGB_8888);
        // 创建燕X轴的倒影图像
        Matrix m = new Matrix();
        m.setScale(1, -1);
        Bitmap t_bitmap = Bitmap.createBitmap(src, 0, h / 2, w, h / 2, m, true);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        // 绘制原图像
        canvas.drawBitmap(src, 0, 0, paint);
        // 绘制倒影图像
        canvas.drawBitmap(t_bitmap, 0, h + spacing, paint);
        // 线性渲染-沿Y轴高到低渲染
        Shader shader = new LinearGradient(0, h + spacing, 0, h + spacing + h
                / 2, 0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);
        paint.setShader(shader);
        // 取两层绘制交集，显示下层。
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // 绘制渲染倒影的矩形
        canvas.drawRect(0, h + spacing, w, h + h / 2 + spacing, paint);
        return bitmap;
    }

    /**
     * 独立的倒影图像
     *
     * @param src
     * @return
     */
    public static Bitmap createReflectionBitmapForSingle(Bitmap src) {
        final int w = src.getWidth();
        final int h = src.getHeight();
        // 绘制高质量32位图
        Bitmap bitmap = Bitmap.createBitmap(w, h / 2, Config.ARGB_8888);
        // 创建沿X轴的倒影图像
        Matrix m = new Matrix();
        m.setScale(1, -1);
        Bitmap t_bitmap = Bitmap.createBitmap(src, 0, h / 2, w, h / 2, m, true);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        // 绘制倒影图像
        canvas.drawBitmap(t_bitmap, 0, 0, paint);
        // 线性渲染-沿Y轴高到低渲染
        Shader shader = new LinearGradient(0, 0, 0, h / 2, 0x70ffffff,
                0x00ffffff, Shader.TileMode.MIRROR);
        paint.setShader(shader);
        // 取两层绘制交集。显示下层。
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // 绘制渲染倒影的矩形
        canvas.drawRect(0, 0, w, h / 2, paint);
        return bitmap;
    }

    public static Bitmap createGreyBitmap(Bitmap src) {
        final int w = src.getWidth();
        final int h = src.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        // 颜色变换的矩阵
        ColorMatrix matrix = new ColorMatrix();
        // saturation 饱和度值，最小可设为0，此时对应的是灰度图；为1表示饱和度不变，设置大于1，就显示过饱和
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    /*
     * 保存图片
     *
     * @param src
     * @param filepath
     * @param format   :[Bitmap.CompressFormat.PNG,Bitmap.CompressFormat.JPEG]
     * @return
     */
//    public static boolean saveImage(Bitmap src, String filepath, CompressFormat format) {
//        boolean rs = false;
//        File file = new File(filepath);
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            if (src.compress(format, 100, out)) {
//                out.flush(); // 写入流
//            }
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return rs;
//    }

    /**
     * 添加水印效果
     *
     * @param src       源位图
     * @param watermark 水印
     * @param direction 方向
     * @param spacing   间距
     * @return
     */
    public static Bitmap createWatermark(Bitmap src, Bitmap watermark,
                                         int direction, int spacing) {
        final int w = src.getWidth();
        final int h = src.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(src, 0, 0, null);
        if (direction == LEFT_TOP) {
            canvas.drawBitmap(watermark, spacing, spacing, null);
        } else if (direction == LEFT_BOTTOM) {
            canvas.drawBitmap(watermark, spacing, h - watermark.getHeight()
                    - spacing, null);
        } else if (direction == RIGHT_TOP) {
            canvas.drawBitmap(watermark, w - watermark.getWidth() - spacing,
                    spacing, null);
        } else if (direction == RIGHT_BOTTOM) {
            canvas.drawBitmap(watermark, w - watermark.getWidth() - spacing, h
                    - watermark.getHeight() - spacing, null);
        }
        return bitmap;
    }

    /**
     * 合成图像
     *
     * @param direction
     * @param bitmaps
     * @return
     */
    public static Bitmap composeBitmap(int direction, Bitmap... bitmaps) {
        if (bitmaps.length < 2) {
            return null;
        }
        Bitmap firstBitmap = bitmaps[0];
        for (int i = 0; i < bitmaps.length; i++) {
            firstBitmap = composeBitmap(firstBitmap, bitmaps[i], direction);
        }
        return firstBitmap;
    }

    /**
     * 合成两张图像
     *
     * @param firstBitmap
     * @param secondBitmap
     * @param direction    ImageUtils(TOP,BOTTOM,LEFT,RIGHT)
     * @return
     */
    private static Bitmap composeBitmap(Bitmap firstBitmap,
                                        Bitmap secondBitmap, int direction) {
        if (firstBitmap == null) {
            return null;
        }
        if (secondBitmap == null) {
            return firstBitmap;
        }
        final int fw = firstBitmap.getWidth();
        final int fh = firstBitmap.getHeight();
        final int sw = secondBitmap.getWidth();
        final int sh = secondBitmap.getHeight();
        Bitmap bitmap = null;
        Canvas canvas = null;
        if (direction == TOP) {
            bitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh,
                    Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawBitmap(secondBitmap, 0, 0, null);
            canvas.drawBitmap(firstBitmap, 0, sh, null);
        } else if (direction == BOTTOM) {
            bitmap = Bitmap.createBitmap(fw > sw ? fw : sw, fh + sh,
                    Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawBitmap(firstBitmap, 0, 0, null);
            canvas.drawBitmap(secondBitmap, 0, fh, null);
        } else if (direction == LEFT) {
            bitmap = Bitmap.createBitmap(fw + sw, sh > fh ? sh : fh,
                    Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawBitmap(secondBitmap, 0, 0, null);
            canvas.drawBitmap(firstBitmap, sw, 0, null);
        } else if (direction == RIGHT) {
            bitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh,
                    Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawBitmap(firstBitmap, 0, 0, null);
            canvas.drawBitmap(secondBitmap, fw, 0, null);
        }
        return bitmap;
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename  The full path of the file to decode
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect
     * ratio and dimensions that are equal to or greater than the
     * requested width and height
     */
    public static synchronized Bitmap decodeSampledBitmapFromFile(
            String filename, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Calculate an inSampleSize for use in a
     * {@link android.graphics.BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This
     * implementation calculates the closest inSampleSize that will result in
     * the final decoded bitmap having a width and height equal to or larger
     * than the requested width and height. This implementation does not ensure
     * a power of 2 is returned for inSampleSize which can be faster when
     * decoding but results in a larger bitmap which isn't as useful for caching
     * purposes.
     *
     * @param options   An options object with out* params already populated (run
     *                  through a decode* method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * 保存图片到文件
     */
    public static boolean saveBitmap(String path, Bitmap bitmap) {
        return saveBitmap(new File(path), bitmap);
    }

    /**
     * 保存图片到文件
     */
    public static boolean saveBitmap(File f, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return false;

        FileOutputStream fOut = null;
        try {
            if (f.exists()) f.createNewFile();

            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

            fOut.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

//    public static Bitmap decodeUriAsBitmap(Context ctx, Uri uri) {
//        Bitmap bitmap = null;
//        try {
//            final BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            // options.outWidth = reqWidth;
//            // options.outHeight = reqHeight;
//            BitmapFactory.decodeStream(ctx.getContentResolver()
//                    .openInputStream(uri), null, options);
//            int be = (int) (options.outHeight / (float) 350);
//            if (be <= 0) be = 1;
//            options.inSampleSize = be;// calculateInSampleSize(options,
//            // reqWidth, reqHeight);
//            options.inJustDecodeBounds = false;
//            bitmap = BitmapFactory.decodeStream(ctx.getContentResolver()
//                    .openInputStream(uri), null, options);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (OutOfMemoryError e) {
//            e.printStackTrace();
//        }
//        return bitmap;
//    }

    /**
     * 转行Drawable为Bitmap对象
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
                .getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 缩放图片
     *
     * @param src       缩放原图
     * @param dstWidth  缩放后宽
     * @param dstHeight 缩放后高
     * @return
     */
    public static Bitmap scaledBitmap(Bitmap src, int dstWidth, int dstHeight) {
        // 原图不能为空也不能已经被回收掉了
        Bitmap result = null;
        if (src != null && !src.isRecycled()) {
            if (src.getWidth() == dstWidth && src.getHeight() == dstHeight) {
                result = src;
            } else {
                result = Bitmap.createScaledBitmap(src, dstWidth, dstHeight,
                        true);
            }
        }
        // ThumbnailUtils.extractThumbnail(source, width, height)
        return result;
    }

    /**
     * 按比例缩放图片
     *
     * @param src
     * @param scale 例如2 就是二分之一
     * @return
     */
    public static Bitmap scaledBitmap(Bitmap src, int scale) {
        if (src == null || src.isRecycled()) {
            return null;
        }
        int dstWidth = src.getWidth() / scale;
        int dstHeight = src.getHeight() / scale;
        return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, true);
    }

    /*
     * 将图片转换成字节数组
     *
     * @param bitmap
     * @return
     */
//    public static byte[] bitmapToBytes(Bitmap bitmap) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//        return outputStream.toByteArray();
//    }

    /**
     * 写图片文件 在Android系统sd卡中<br/>
     * 文件保存在SD卡/Android/DRM/...,
     * <p>
     * cachePath: true:  /image_cache; fasle: /fuzzy
     */
    public static void saveBitmap(String fileName, Bitmap bitmap, boolean cachePath) {
        if (bitmap == null || fileName == null) return;
        String cacheImgPath = cachePath ? PathUtil.DEF_IMAGE_PATH : PathUtil.DEF_FUZZY_PATH;
        FileOutputStream fos = null;
        if (FileUtils.createDirectory(cacheImgPath)) {
            try {
                fos = new FileOutputStream(cacheImgPath + File.separator + fileName);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.JPEG, 100, stream);
                byte[] bytes = stream.toByteArray();
                fos.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null)
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    /**
     * 写图片文件到SD卡
     *
     * @throws IOException
     */
    public static void saveImageToSD(Context ctx, String filePath,
                                     Bitmap bitmap, int quality) throws IOException {
        if (bitmap != null) {
            File file = new File(filePath.substring(0,
                    filePath.lastIndexOf(File.separator)));
            if (!file.exists()) {
                file.mkdirs();
            }
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(filePath));
            bitmap.compress(CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();
            if (ctx != null) {
                scanPhoto(ctx, filePath);
            }
        }
    }

//    public static void saveBackgroundImage(Context ctx, String filePath,
//                                           Bitmap bitmap, int quality) throws IOException {
//        if (bitmap != null) {
//            File file = new File(filePath.substring(0,
//                    filePath.lastIndexOf(File.separator)));
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//            BufferedOutputStream bos = new BufferedOutputStream(
//                    new FileOutputStream(filePath));
//            bitmap.compress(CompressFormat.PNG, quality, bos);
//            bos.flush();
//            bos.close();
//            if (ctx != null) {
//                scanPhoto(ctx, filePath);
//            }
//        }
//    }

    /**
     * 让Gallery上能马上看到该图片
     */
    private static void scanPhoto(Context ctx, String imgFileName) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(imgFileName);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        ctx.sendBroadcast(mediaScanIntent);
    }

    /**
     * 获取bitmap
     *
     * @param cacheImg
     * @return
     */
    public static Bitmap getBitmap(File cacheImg) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(cacheImg);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 获取bitmap
     *
     * @param filePath
     * @return
     */
    public static Bitmap getBitmapByPath(String filePath) {
        return getBitmapByPath(filePath, null);
    }

    public static Bitmap getBitmapByPath(String filePath, BitmapFactory.Options opts) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis, null, opts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return bitmap;
    }

    /**
     * 获取bitmap
     *
     * @param file
     * @return
     */
    public static Bitmap getBitmapByFile(File file) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return bitmap;
    }

    /*
     * 使用当前时间戳拼接一个唯一的文件名
     */
//    public static String getTempFileName() {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SS");
//        String fileName = format.format(new Timestamp(System
//                .currentTimeMillis()));
//        return fileName;
//    }

    /*
     * 获取照相机使用的目录
     *
     * @return
     */
//    public static String getCamerPath() {
//        return Environment.getExternalStorageDirectory() + File.separator
//                + "FounderNews" + File.separator;
//    }

    /*
     * 判断当前Url是否标准的content://样式，如果不是，则返回绝对路径
     *
     * @param mUri
     * @return
     */
//    public static String getAbsolutePathFromNoStandardUri(Uri mUri) {
//        String filePath = null;
//
//        String mUriString = mUri.toString();
//        mUriString = Uri.decode(mUriString);
//
//        String pre1 = "file://" + SDCARD + File.separator;
//        String pre2 = "file://" + SDCARD_MNT + File.separator;
//
//        if (mUriString.startsWith(pre1)) {
//            filePath = Environment.getExternalStorageDirectory().getPath()
//                    + File.separator + mUriString.substring(pre1.length());
//        } else if (mUriString.startsWith(pre2)) {
//            filePath = Environment.getExternalStorageDirectory().getPath()
//                    + File.separator + mUriString.substring(pre2.length());
//        }
//        return filePath;
//    }

    /*
     * 通过uri获取文件的绝对路径
     *
     * @param uri
     * @return
     */
//    @SuppressWarnings("deprecation")
//    public static String getAbsoluteImagePath(Activity context, Uri uri) {
//        String imagePath = "";
//        String[] proj = {MediaStore.Images.Media.DATA};
//        Cursor cursor = context.managedQuery(uri, proj, // Which columns to
//                // return
//                null, // WHERE clause; which rows to return (all rows)
//                null, // WHERE clause selection arguments (none)
//                null); // Order-by clause (ascending by name)
//
//        if (cursor != null) {
//            int column_index = cursor
//                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
//                imagePath = cursor.getString(column_index);
//            }
//        }
//
//        return imagePath;
//    }

    /*
     * 获取图片缩略图 只有Android2.1以上版本支持
     *
     * @param imgName
     * @param kind    MediaStore.Images.Thumbnails.MICRO_KIND
     * @return
     */
//    @SuppressWarnings("deprecation")
//    public static Bitmap loadImgThumbnail(Activity context, String imgName,
//                                          int kind) {
//        Bitmap bitmap = null;
//
//        String[] proj = {MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.DISPLAY_NAME};
//
//        Cursor cursor = context.managedQuery(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
//                MediaStore.Images.Media.DISPLAY_NAME + "='" + imgName + "'",
//                null, null);
//
//        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
//            ContentResolver crThumb = context.getContentResolver();
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 1;
//            bitmap = getThumbnail(crThumb, cursor.getInt(0), kind, options);
//        }
//        return bitmap;
//    }

//    public static Bitmap getThumbnail(ContentResolver cr, long origId,
//                                      int kind, Options options) {
//        return MediaStore.Images.Thumbnails.getThumbnail(cr, origId, kind,
//                options);
//    }

//    public static Bitmap loadImgThumbnail(String filePath, int w, int h) {
//        Bitmap bitmap = getBitmapByPath(filePath);
//        return zoomBitmap(bitmap, w, h);
//    }

    /*
     * 获取SD卡中最新图片路径
     *
     * @return
     */
//    public static String getLatestImage(Activity context) {
//        String latestImage = null;
//        String[] items = {MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.DATA};
//        Cursor cursor = context.managedQuery(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, items, null,
//                null, MediaStore.Images.Media._ID + " desc");
//
//        if (cursor != null && cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
//                latestImage = cursor.getString(1);
//                break;
//            }
//        }
//
//        return latestImage;
//    }

    /**
     * 计算缩放图片的宽高
     *
     * @param img_size
     * @param square_size
     * @return
     */
    public static int[] scaleImageSize(int[] img_size, int square_size) {
        if (img_size[0] <= square_size && img_size[1] <= square_size) {
            return img_size;
        }
        double ratio = square_size / (double) Math.max(img_size[0], img_size[1]);
        return new int[]{(int) (img_size[0] * ratio), (int) (img_size[1] * ratio)};
    }

    /**
     * 创建缩略图
     *
     * @param largeImagePath 原始大图路径
     * @param thumbfilePath  输出缩略图路径
     * @param square_size    输出图片宽度
     * @param quality        输出图片质量
     * @throws IOException
     */
    public static void createImageThumbnail(String largeImagePath, String thumbfilePath,
                                            int square_size, int quality) throws IOException {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 1;
        // 原始图片bitmap
        Bitmap cur_bitmap = getBitmapByPath(largeImagePath, opts);

        if (cur_bitmap == null) return;

        // 原始图片的高宽
        int[] cur_img_size = new int[]{cur_bitmap.getWidth(),
                cur_bitmap.getHeight()};
        // 计算原始图片缩放后的宽高
        int[] new_img_size = scaleImageSize(cur_img_size, square_size);
        // 生成缩放后的bitmap
        Bitmap thb_bitmap = zoomBitmap(cur_bitmap, new_img_size[0],
                new_img_size[1]);
        // 生成缩放后的图片文件
        saveImageToSD(null, thumbfilePath, thb_bitmap, quality);
    }

    /**
     * 放大缩小图片
     *
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        Bitmap newbmp = null;
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidht = ((float) w / width);
            float scaleHeight = ((float) h / height);
            matrix.postScale(scaleWidht, scaleHeight);
            newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                    true);
        }
        return newbmp;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap) {
        // 获取这个图片的宽和高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 定义预转换成的图片的宽度和高度
        int newWidth = 200;
        int newHeight = 200;
        // 计算缩放率，新尺寸除原始尺寸
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        // 旋转图片 动作
        // matrix.postRotate(45);
        // 创建新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * (缩放)重绘图片
     *
     * @param context Activity
     * @param bitmap
     * @return
     */
    public static Bitmap reDrawBitmap(Activity context, Bitmap bitmap) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int rWidth = dm.widthPixels;
        int width = bitmap.getWidth();
        float zoomScale;
        /** 方式3 **/
        if (width >= rWidth)
            zoomScale = ((float) rWidth) / width;
        else
            zoomScale = 1.0f;
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 缩放图片动作
        matrix.postScale(zoomScale, zoomScale);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /*
     * 将Drawable转化为Bitmap
     *
     * @param drawable
     * @return
     */
//    public static Bitmap drawableToBitmap(Drawable drawable) {
//        int width = drawable.getIntrinsicWidth();
//        int height = drawable.getIntrinsicHeight();
//        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
//                .getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
//                : Bitmap.Config.RGB_565);
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, width, height);
//        drawable.draw(canvas);
//        return bitmap;
//
//    }

    /**
     * 获得圆角图片的方法
     *
     * @param bitmap
     * @param roundPx 一般设成14
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * 获得带倒影的图片方法
     *
     * @param bitmap
     * @return
     */
    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
        final int reflectionGap = 4;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
                width, height / 2, matrix, false);

        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                (height + height / 2), Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint deafalutPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);

        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
                0x00ffffff, TileMode.CLAMP);
        paint.setShader(shader);
        // Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
                + reflectionGap, paint);

        return bitmapWithReflection;
    }

    /**
     * 将bitmap转化为drawable
     *
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    /*
     * 获取图片类型
     *
     * @param file
     * @return
     */
//    public static String getImageType(File file) {
//        if (file == null || !file.exists()) {
//            return null;
//        }
//        InputStream in = null;
//        try {
//            in = new FileInputStream(file);
//            String type = getImageType(in);
//            return type;
//        } catch (IOException e) {
//            return null;
//        } finally {
//            try {
//                if (in != null) {
//                    in.close();
//                }
//            } catch (IOException e) {
//            }
//        }
//    }

    /*
     * 获取图片的类型信息
     *
     * @param in
     * @return
     * @see #getImageType(byte[])
     */
//    public static String getImageType(InputStream in) {
//        if (in == null) {
//            return null;
//        }
//        try {
//            byte[] bytes = new byte[8];
//            in.read(bytes);
//            return getImageType(bytes);
//        } catch (IOException e) {
//            return null;
//        }
//    }

    /**
     * 获取图片的类型信息
     *
     * @param bytes 2~8 byte at beginning of the image file
     * @return image mimetype or null if the file is not image
     */
    public static String getImageType(byte[] bytes) {
        if (isJPEG(bytes)) {
            return "image/jpeg";
        }
        if (isGIF(bytes)) {
            return "image/gif";
        }
        if (isPNG(bytes)) {
            return "image/png";
        }
        if (isBMP(bytes)) {
            return "application/x-bmp";
        }
        return null;
    }

    private static boolean isJPEG(byte[] b) {
        if (b.length < 2) {
            return false;
        }
        return (b[0] == (byte) 0xFF) && (b[1] == (byte) 0xD8);
    }

    private static boolean isGIF(byte[] b) {
        if (b.length < 6) {
            return false;
        }
        return b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private static boolean isPNG(byte[] b) {
        if (b.length < 8) {
            return false;
        }
        return (b[0] == (byte) 137 && b[1] == (byte) 80 && b[2] == (byte) 78
                && b[3] == (byte) 71 && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    private static boolean isBMP(byte[] b) {
        if (b.length < 2) {
            return false;
        }
        return (b[0] == 0x42) && (b[1] == 0x4d);
    }
}
