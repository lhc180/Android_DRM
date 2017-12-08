package cn.com.pyc.drm.utils.manager;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import cn.com.pyc.drm.common.App;
import cn.com.pyc.drm.common.AppException;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.FileUtils;
import cn.com.pyc.drm.utils.HttpUtil;
import cn.com.pyc.drm.utils.ImageUtils;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

/**
 * 异步线程加载图片工具类
 */
public class BitmapManager
{

	public static int DEFAULT_BITMAP_WIDTH = 90;
	public static int DEFAULT_BITMAP_HEIGHT = 90;

	private static HashMap<String, SoftReference<Bitmap>> cache;
	private static Map<ImageView, String> imageViews;
	private static ExecutorService pool;

	static
	{
		cache = new HashMap<String, SoftReference<Bitmap>>();
		pool = Executors.newFixedThreadPool(5); // 固定线程池
		imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	}

	/**
	 * 加载图片-可指定显示图片的高宽
	 * 
	 * @param url
	 * @param imageView
	 */
	public static void loadBitmap(App context, String url, ImageView imageView, Bitmap defaultBmp, ImageDownListener listener1)
	{

		imageViews.put(imageView, url);
		Bitmap bitmap = getBitmapFromCache(url);

		if (bitmap != null)
		{
			// 显示缓存图片
			imageView.setImageBitmap(bitmap);
		} else
		{
			// 加载SD卡中的图片缓存
			String filename = FileUtils.getNameFromFilePath(url);
			String filepath = DRMUtil.DEFAULT_SAVE_IMAGE_PATH + File.separator + filename;
			File file = new File(filepath);
			if (file.exists())
			{
				// 显示SD卡中的图片缓存
				Bitmap bmp = ImageUtils.getBitmap(file);
				imageView.setImageBitmap(bmp);
			} else
			{
				// 线程加载网络图片
				imageView.setImageBitmap(defaultBmp);
				queueJob(context, url, imageView, defaultBmp, 0, 0, 0);
			}
		}
	}

	/**
	 * 加载图片-可指定显示图片的高宽
	 * 
	 * @param url
	 * @param imageView
	 */
	public static void loadMusicBitmap(App context, String url, ImageView imageView, Bitmap defaultBmp, ImageDownListener listener1)
	{

		imageViews.put(imageView, url);
		Bitmap bitmap = getBitmapFromCache(url);

		if (bitmap != null)
		{
			// 显示缓存图片
			imageView.setImageBitmap(bitmap);
		} else
		{
			// 加载SD卡中的图片缓存
			String filename = FileUtils.getNameFromFilePath(url);
			String filepath = DRMUtil.DEFAULT_HIGH_SPEED_FUZZY_PATH + File.separator + filename;
			File file = new File(filepath);
			if (file.exists())
			{
				// 显示SD卡中的图片缓存
				Bitmap bmp = ImageUtils.getBitmap(file);
				imageView.setImageBitmap(bmp);
			} else
			{
				// 线程加载网络图片
				imageView.setImageBitmap(defaultBmp);
				queueJob(context, url, imageView, defaultBmp, 0, 0, 1);
			}
		}
	}

	/**
	 * 加载图片-指定音乐图片背景图片
	 * 
	 */
	public static void loadMusicBackGround(String url)
	{
		// 加载SD卡中的图片缓存
		String filename = FileUtils.getNameFromFilePath(url);
		String filepath = DRMUtil.DEFAULT_HIGH_SPEED_FUZZY_PATH + File.separator + filename;
		File file = new File(filepath);
		if (file.exists())
		{
			return;
		} else
		{
			// 线程加载网络图片
			HttpUtils http = new HttpUtils();
			http.download(url, filepath, true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
					true, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
					new RequestCallBack<File>()
					{
						public void onStart()
						{
						}

						public void onLoading(long total, long current, boolean isUploading)
						{
						}

						public void onSuccess(ResponseInfo<File> responseInfo)
						{
						}

						public void onFailure(HttpException error, String msg)
						{
						}
					});

		}
	}

	/**
	 * 从缓存中获取图片
	 * 
	 * @param url
	 */
	public static Bitmap getBitmapFromCache(String url)
	{
		Bitmap bitmap = null;
		if (cache.containsKey(url))
		{
			bitmap = cache.get(url).get();
		}
		return bitmap;
	}

	/**
	 * 从网络中加载图片
	 * 
	 * @param url
	 * @param imageView
	 * @param defaultBmp
	 * @param width
	 * @param height
	 */
	public static void queueJob(final App context, final String url, final ImageView imageView, Bitmap defaultBmp, final int width, final int height,
			final int type)
	{
		/* Create handler in UI thread. */
		final Handler handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				String tag = imageViews.get(imageView);
				if (tag != null && tag.equals(url))
				{
					if (msg.obj != null)
					{
						// 向SD卡中写入图片缓存
						ImageUtils.saveImage(FileUtils.getNameFromFilePath(url), (Bitmap) msg.obj, type);
					}
				}
			}
		};

		pool.execute(new Runnable()
		{
			public void run()
			{
				Message message = Message.obtain();
				message.obj = downloadBitmap(context, url, width, height);
				handler.sendMessage(message);
			}
		});
	}

	/**
	 * 从网络中加载图片
	 * 
	 * @param url
	 * @param imageView
	 * @param defaultBmp
	 * @param width
	 * @param height
	 */
	public static void queueJobMusicBackGround(final App context, final String url, final int width, final int height, final int type)
	{
		pool.execute(new Runnable()
		{
			public void run()
			{
				downloadBitmap(context, url, width, height);
			}
		});
	}

	private static Map<String, SoftReference<Bitmap>> cacheBitmaps = new HashMap<String, SoftReference<Bitmap>>();

	/**
	 * 计算图片的最小缩放比例
	 * 
	 * @param options
	 *            图片option
	 * @param reqWidth
	 *            图片的宽度
	 * @param reqHeight
	 *            图片的高度
	 * @return 缩放比例
	 */

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;// 默认缩放为1，即为不缩放
		if (height > reqHeight || width > reqWidth)
		{
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth)
			{
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	/**
	 * 从资源文件加载缩放后的Bitmap
	 * 
	 * @param res
	 * @param resId
	 *            资源图片的ID
	 * @param reqWidth
	 *            图片的宽度
	 * @param reqHeight
	 *            图片的高度
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight)
	{
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * 从文件加载缩放后的Bitmap
	 * 
	 * @param pathName
	 *            图片所在路径
	 * @param reqWidth
	 *            图片的宽度
	 * @param reqHeight
	 *            图片的高度
	 * @return
	 */
	public static Bitmap decodeSampledBitmap(String pathName, int reqWidth, int reqHeight)
	{
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	/**
	 * 从文件加载缩放后的Bitmap
	 * 
	 * @param pathName
	 *            图片所在路径
	 * @return
	 */
	public static Bitmap decodeSampledBitmap(String pathName)
	{
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, DEFAULT_BITMAP_WIDTH, DEFAULT_BITMAP_HEIGHT);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	/**
	 * 获得Bitamp对象
	 * 
	 * @param pic_path
	 * @return
	 */
	public static Bitmap getBigBitmap(String pic_path, int width, int height)
	{
		Bitmap bitmap = null;
		if (cacheBitmaps.containsKey(pic_path))
		{
			bitmap = cacheBitmaps.get(pic_path).get();
		} else
		{
			bitmap = decodeSampledBitmap(pic_path, width, height);
			if (bitmap != null)
			{
				cacheBitmaps.put(pic_path, new SoftReference<Bitmap>(bitmap));
			}
		}

		return bitmap;
	}

	/**
	 * 下载图片-可指定显示图片的高宽
	 * 
	 * @param url
	 * @param width
	 * @param height
	 */
	private static Bitmap downloadBitmap(App context, String url, int width, int height)
	{
		Bitmap bitmap = null;
		try
		{
			// http加载图片
			bitmap = HttpUtil.getNetBitmap(url);
			if (width > 0 && height > 0)
			{
				// 指定显示图片的高宽
				bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
			}
			bitmap = ImageUtils.getRoundedCornerBitmap(bitmap, 50);
			// 放入缓存
			cache.put(url, new SoftReference<Bitmap>(bitmap));
		} catch (AppException e)
		{
			e.printStackTrace();
		}
		return bitmap;
	}

	public interface ImageDownListener
	{
		void onComplete(Bitmap bitmap);
	}
}