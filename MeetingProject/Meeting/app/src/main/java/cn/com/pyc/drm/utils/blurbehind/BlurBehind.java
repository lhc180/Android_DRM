package cn.com.pyc.drm.utils.blurbehind;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;
import android.view.View;


public class BlurBehind {

    private static final String KEY_CACHE_BLURRED_BACKGROUND_IMAGE = "KEY_CACHE_BLURRED_BACKGROUND_IMAGE";
    private static final int CONSTANT_BLUR_RADIUS = 12;
    private static final int CONSTANT_DEFAULT_ALPHA = 100;

    private static final LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(1);
    //        private static CacheBlurBehindAndExecuteTask cacheBlurBehindAndExecuteTask;

    private int mAlpha = CONSTANT_DEFAULT_ALPHA;
    private int mFilterColor = -1;

    private enum State {
        READY,
        EXECUTING
    }

    private State mState = State.READY;

    private static BlurBehind mInstance;

    public static BlurBehind getInstance() {
        if (mInstance == null) {
            mInstance = new BlurBehind();
        }
        return mInstance;
    }

    public void execute(final Activity activity, final OnBlurCompleteListener onBlurCompleteListener) {
        if (mState.equals(State.READY)) {
            mState = State.EXECUTING;
            final View decorView = activity.getWindow().getDecorView();
            decorView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
            decorView.setDrawingCacheEnabled(true);
            decorView.buildDrawingCache();
            final Bitmap image = decorView.getDrawingCache();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap blurredBitmap = Blur.apply(activity, image, CONSTANT_BLUR_RADIUS);
                    mImageCache.put(KEY_CACHE_BLURRED_BACKGROUND_IMAGE, blurredBitmap);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            decorView.destroyDrawingCache();
                            decorView.setDrawingCacheEnabled(false);
                            onBlurCompleteListener.onBlurComplete();
                            mState = State.READY;
                        }
                    });
                }
            }).start();
            //                        cacheBlurBehindAndExecuteTask = new CacheBlurBehindAndExecuteTask(activity, onBlurCompleteListener);
            //                        cacheBlurBehindAndExecuteTask.execute();
        }
    }

    public BlurBehind withAlpha(int alpha) {
        this.mAlpha = alpha;
        return this;
    }

    public BlurBehind withFilterColor(int filterColor) {
        this.mFilterColor = filterColor;
        return this;
    }

    public void setBackground(Activity activity) {
        if (mImageCache.size() != 0) {
            BitmapDrawable bd = new BitmapDrawable(activity.getResources(), mImageCache.get(KEY_CACHE_BLURRED_BACKGROUND_IMAGE));
            bd.setAlpha(mAlpha);
            if (mFilterColor != -1) {
                bd.setColorFilter(mFilterColor, PorterDuff.Mode.DST_ATOP);
            }
            activity.getWindow().setBackgroundDrawable(bd);
            mImageCache.remove(KEY_CACHE_BLURRED_BACKGROUND_IMAGE);
            //cacheBlurBehindAndExecuteTask = null;
        }
    }
    //
    //        private class CacheBlurBehindAndExecuteTask extends AsyncTask<Void, Void, Void> {
    //            private Activity activity;
    //            private OnBlurCompleteListener onBlurCompleteListener;
    //
    //            private View decorView;
    //            private Bitmap image;
    //
    //            public CacheBlurBehindAndExecuteTask(Activity activity, OnBlurCompleteListener onBlurCompleteListener) {
    //                this.activity = activity;
    //                this.onBlurCompleteListener = onBlurCompleteListener;
    //            }
    //
    //            @Override
    //            protected void onPreExecute() {
    //                super.onPreExecute();
    //                Log.e("open","加载");
    //                decorView = activity.getWindow().getDecorView();
    //                decorView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
    //                decorView.setDrawingCacheEnabled(true);
    //                decorView.buildDrawingCache();
    //
    //                image = decorView.getDrawingCache();
    //                Log.e("open","加载完成");
    //            }
    //
    //            @Override
    //            protected Void doInBackground(Void... params) {
    //                Log.e("open","获取Bitmap");
    //                Bitmap blurredBitmapCache=mImageCache.get(KEY_CACHE_BLURRED_BACKGROUND_IMAGE);
    //                Log.e("open","判断是否为空");
    //                if(blurredBitmapCache==null){
    //                    Log.e("open","为空,进行创建");
    //                    Bitmap blurredBitmap = Blur.apply(activity, image, CONSTANT_BLUR_RADIUS);
    //                    mImageCache.put(KEY_CACHE_BLURRED_BACKGROUND_IMAGE, blurredBitmap);
    //                }
    //                return null;
    //            }
    //
    //            @Override
    //            protected void onPostExecute(Void aVoid) {
    //                super.onPostExecute(aVoid);
    //
    //                decorView.destroyDrawingCache();
    //                decorView.setDrawingCacheEnabled(false);
    //
    //                activity = null;
    //
    //                onBlurCompleteListener.onBlurComplete();
    //
    //                mState = State.READY;
    //            }
    //        }
}
