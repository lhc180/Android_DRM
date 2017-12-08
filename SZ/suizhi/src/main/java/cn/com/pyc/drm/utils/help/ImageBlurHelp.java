package cn.com.pyc.drm.utils.help;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.lhkj.blurdemo.jni.ImageBlurManager;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.lang.ref.WeakReference;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.FileUtils;
import cn.com.pyc.drm.utils.ImageUtils;
import cn.com.pyc.drm.utils.PathUtil;
import cn.com.pyc.drm.utils.manager.ExecutorManager;

/**
 * 高斯模糊图片处理
 * <p>
 * Created by hudaqiang on 2017/10/30.
 */

public class ImageBlurHelp {

    private Context mContext;
    private String mPictureUrl;
    private View mTargetView;

    private String mFileName;
    private String mFilePath;
    private ExecHandler mHandler = new ExecHandler(this);

    /**
     * 开始处理高斯模糊图片
     */
    public void work() {
        //判断本地源文件图片是否存在
        if (FileUtils.checkFilePathExists(mFilePath)) {
            ExecutorManager.getInstance().execute(new BlurRunnable(this));
        } else {
            //不存在，先下载原图片，下载完成然后处理高斯模糊效果
            downloadImage();
        }
    }

    private ImageBlurHelp(Context context, String url, View targetView) {
        mContext = context;
        mPictureUrl = url;
        mTargetView = targetView;

        //根据图片url，获取图片的名称，eg: abc.jpg
        mFileName = FileUtils.getNameFromFilePath(mPictureUrl);
        ////原始图片保存的路径，eg: Android/data/cn.com.pyc.drm/img_fuzzy/abc.jpg
        mFilePath = PathUtil.DEF_FUZZY_PATH + "/" + mFileName;
        DRMLog.d("original image: " + mFilePath);
    }

    private static class ExecHandler extends Handler {
        private WeakReference<ImageBlurHelp> reference;

        private ExecHandler(ImageBlurHelp activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what != 100) return;
            ImageBlurHelp help = reference.get();
            if (help == null) return;

            String path = ((String) msg.obj);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Drawable drawable = new BitmapDrawable(help.mContext.getResources(), bitmap);
            BitmapPixHelp.calculateColorPix(bitmap);

            //Drawable drawable = ((Drawable) msg.obj);
            if (help.mTargetView.getBackground() != null)
                help.mTargetView.setBackgroundDrawable(null);
            help.mTargetView.setBackgroundDrawable(drawable);
        }
    }

    public static class Helper {
        private Context mContext;
        private String mPictureUrl;
        private View mTargetView;

        public Helper setContext(Context context) {
            mContext = context;
            return this;
        }

        public Helper setPictureUrl(String pictureUrl) {
            mPictureUrl = pictureUrl;
            return this;
        }

        public Helper setTargetView(View targetView) {
            mTargetView = targetView;
            return this;
        }

        public ImageBlurHelp create() {
            return new ImageBlurHelp(mContext, mPictureUrl, mTargetView);
        }
    }


    // 高斯模糊图片路径名，eg: Android/data/cn.com.pyc.drm/img_fuzzy/abc_BLUR.jpg
    private String getBlurFilePath() {
        //abc.jpg; 忽略其他格式的图片
        String fileName = mFileName;
        if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".bmp")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        } else if (fileName.endsWith(".jpeg")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        return PathUtil.DEF_FUZZY_PATH + "/" + fileName + "_BLUR.jpg";
    }


    private static class BlurRunnable implements Runnable {

        private WeakReference<ImageBlurHelp> mWeakReference;

        private BlurRunnable(ImageBlurHelp ibh) {
            mWeakReference = new WeakReference<>(ibh);
        }

        @Override
        public void run() {
            ImageBlurHelp help = mWeakReference.get();
            if (help == null) return;
            help.blurImage();
        }
    }

    //高斯迷糊处理(非主线程中运行)
    private void blurImage() {
        String filePathFuzzy = getBlurFilePath();
        File file = new File(filePathFuzzy);
        //Drawable drawable;
        if (file.exists()) {
            DRMLog.w("blur image exist: " + filePathFuzzy);
            //高斯模糊处理的图片存在，读取缓存图片
            //Bitmap bitmap = BitmapFactory.decodeFile(filePathFuzzy);
            //drawable = new BitmapDrawable(mContext.getResources(), bitmap);
            //BitmapPixHelp.calculateColorPix(bitmap);
        } else {
            //进行高斯模糊处理...
            DRMLog.w("blur image not exist.");
            Bitmap decodeFile = BitmapFactory.decodeFile(mFilePath);
            if (decodeFile == null) {
                decodeFile = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.music_default_bg); //默认的图片背景
            }
            //int brightness = -80; // 改变亮度
            //ColorMatrix cMatrix = new ColorMatrix();
            //cMatrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0,
            //        0, brightness, 0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});

            float scaleFactor = 1.4f;
            // 设置告诉模糊的图片
            Bitmap bmp = Bitmap.createBitmap(
                    (int) (decodeFile.getWidth() / scaleFactor),
                    (int) (decodeFile.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.translate(-mTargetView.getLeft() / scaleFactor,
                    -mTargetView.getTop() / scaleFactor);
            canvas.scale(1 / scaleFactor, 1 / scaleFactor);
            Paint paint = new Paint();
            //paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));
            paint.setFlags(Paint.FILTER_BITMAP_FLAG);
            canvas.drawBitmap(decodeFile, 0, 0, paint);
            Bitmap blurBitmap = ImageBlurManager.doBlur(bmp, 70, false);

            decodeFile.recycle();
            bmp.recycle();

            // 保存文件到sd卡
            ImageUtils.saveBitmap(FileUtils.getNameFromFilePath(filePathFuzzy), blurBitmap, false);

            //获取到drawable.
            //drawable = new BitmapDrawable(mContext.getResources(), blurBitmap);

            //BitmapPixHelp.calculateColorPix(blurBitmap);
        }

        Message message = Message.obtain(mHandler, 100, filePathFuzzy);
        message.sendToTarget();
    }

    //下载原始图片
    private void downloadImage() {
        DRMLog.d("picture file not exist, download.");
        RequestParams params = new RequestParams(mPictureUrl);
        params.setUseCookie(false);
        params.setConnectTimeout(30 * 1000);
        params.setAutoResume(true); // 设置断点续传
        params.setAutoRename(true);
        params.setSaveFilePath(mFilePath);

        x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {
            }

            @Override
            public void onStarted() {
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
            }

            @Override
            public void onSuccess(File result) {
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
                //下载文件图片完毕
                ExecutorManager.getInstance().execute(new BlurRunnable(ImageBlurHelp.this));
            }
        });
    }


    /*
     * 获取设置高斯模糊图片
     */
//    public void getGaussambiguity(String pictureUrl, final View musicBg) {
//        // 加载SD卡中的图片缓存
//        final String fileName = FileUtils.getNameFromFilePath(pictureUrl);
//        //eg: Android/data/cn.com.pyc.drm/img_fuzzy/abc.jpg
//        String filePath = PathUtil.DEF_FUZZY_PATH + File.separator + fileName;
//        if (FileUtils.checkFilePathExists(filePath)) {
//            DRMLog.i("picture file exist: " + filePath);
//            setMusicBlurBackground(fileName, musicBg);
//        } else {
//            // 缓存图片不存在本地，加载网络专辑图片，存储路径filePath
//            DRMLog.i("picture file not exist.");
//            RequestParams params = new RequestParams(pictureUrl);
//            params.setUseCookie(false);
//            params.setConnectTimeout(30 * 1000);
//            params.setAutoResume(true); // 设置断点续传
//            params.setAutoRename(true);
//            params.setSaveFilePath(filePath);
//            x.http().get(params, new Callback.ProgressCallback<File>() {
//
//                @Override
//                public void onWaiting() {
//                }
//
//                @Override
//                public void onStarted() {
//                }
//
//                @Override
//                public void onLoading(long l, long l1, boolean b) {
//                }
//
//                @Override
//                public void onCancelled(CancelledException arg0) {
//                }
//
//                @Override
//                public void onError(Throwable arg0, boolean arg1) {
//                }
//
//                @Override
//                public void onFinished() {
//                }
//
//                @Override
//                public void onSuccess(File arg0) {
//                    setMusicBlurBackground(fileName, musicBg);
//                }
//            });
//        }
//    }

    /*
     * 设置高斯模糊背景图片
     *
     * @param fileName
     * @param musicBg
     */
//    private void setMusicBlurBackground(final String fileName, final View musicBg) {
//
//        //final String imageName = FileUtils.getNameFromFilePath(pictureUrl);
//        final String filePath = PathUtil.DEF_FUZZY_PATH + File.separator + fileName;
//        // 高斯模糊图片路径名
//        final String filePathFuzzy = filePath + "BLUR" + ".jpg";
//        File files = new File(filePathFuzzy);
//        if (!files.exists()) {
//            // 不存在，创建高斯模糊图片背景
//            //final int getLeft = musicBg.getLeft();
//            //final int getTop = musicBg.getTop();
//            new AsyncTask<String, String, BitmapDrawable>() {
//                @Override
//                protected BitmapDrawable doInBackground(String... params) {
//                    if (isCancelled()) return null;
//                    Bitmap decodeFile = BitmapFactory.decodeFile(filePath);
//                    if (decodeFile == null) {
//                        decodeFile = BitmapFactory.decodeResource(mContext.getResources(),
//                                R.drawable.music_default_bg);
//                    }
//                    int brightness = -80;
//                    ColorMatrix cMatrix = new ColorMatrix();
//                    cMatrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0,
//                            0, brightness,// 改变亮度
//                            0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
//
//                    float scaleFactor = 2f;
//                    // float radius = 20;
//                    // 设置告诉模糊的图片
//                    Bitmap bmp = Bitmap.createBitmap(
//                            (int) (decodeFile.getWidth() / scaleFactor),
//                            (int) (decodeFile.getHeight() / scaleFactor),
//                            Bitmap.Config.ARGB_8888);
//                    Canvas canvas = new Canvas(bmp);
//                    canvas.translate(-musicBg.getLeft() / scaleFactor,
//                            -musicBg.getTop() / scaleFactor);
//                    //canvas.translate(-getLeft / scaleFactor, -getTop / scaleFactor);
//                    canvas.scale(1 / scaleFactor, 1 / scaleFactor);
//                    Paint paint = new Paint();
//                    paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));
//                    paint.setFlags(Paint.FILTER_BITMAP_FLAG);
//                    canvas.drawBitmap(decodeFile, 0, 0, paint);
//                    Bitmap blurBitmap = ImageBlurManager.doBlur(bmp, 70, false);
//
//                    decodeFile.recycle();
//                    //decodeFile = null;
//                    bmp.recycle();
//                    //bmp = null;
//                    //获取到drawable
//                    BitmapDrawable background = new BitmapDrawable(mContext.getResources(),
//                            blurBitmap);
//                    // 保存文件到sd卡
//                    ImageUtils.saveBitmap(FileUtils.getNameFromFilePath(filePathFuzzy),
//                            blurBitmap, false);
//
//                    DRMLog.e("width: " + background.getIntrinsicWidth());
//                    DRMLog.e("height: " + background.getIntrinsicHeight());
//
//                    isLightColor(getColorPix(blurBitmap));
//
//                    return background;
//                }
//
//                @Override
//                protected void onPostExecute(BitmapDrawable result) {
//                    if (result == null) return;
//                    // 移除原背景
//                    if (musicBg.getBackground() != null)
//                        musicBg.setBackgroundDrawable(null);
//                    musicBg.setBackgroundDrawable(result);
//                    cancel(true);
//                }
//            }.execute();
//        } else {
//            // 高斯模糊图片已存在本地
//            BitmapDrawable background = new BitmapDrawable(mContext.getResources(), ImageUtils
//                    .getBitmap(files));
//            // 移除原背景
//            if (musicBg.getBackground() != null)
//                musicBg.setBackgroundDrawable(null);
//            musicBg.setBackgroundDrawable(background);
//        }
//    }

}
