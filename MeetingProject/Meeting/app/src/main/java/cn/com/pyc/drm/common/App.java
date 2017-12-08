package cn.com.pyc.drm.common;

import java.io.File;

import android.content.ComponentCallbacks2;
import android.content.Context;
import cn.com.pyc.drm.utils.DRMUtil;
import cn.com.pyc.drm.utils.manager.ActicityManager;
import cn.jpush.android.api.JPushInterface;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * application <br/>
 * <br/>
 * <br/>
 * warning：applicaiton中尽量不要存储初始化后的临时数据
 */
public class App extends BaseApplication
{

	// Application对象是全局的，单例
	private static App instance;

	public static App getInstance()
	{
		return instance;
	}

	@Override
	public void onTerminate()
	{
		super.onTerminate();
		ActicityManager.getInstance().exit();
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		System.err.print("onLowMemory");
		ImageLoader.getInstance().clearMemoryCache();
	}

	@Override
	public void onTrimMemory(int level)
	{
		super.onTrimMemory(level);
		if (level > ComponentCallbacks2.TRIM_MEMORY_BACKGROUND)
		{
			ImageLoader.getInstance().clearMemoryCache();
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
		Constant.init(getApplicationContext());
		initImageLoader(getApplicationContext());
		// 检查应用可能存在的警告或问题，供开发者发现问题隐患
		// if (Config.DEVELOPER_MODE
		// && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
		// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		// .detectAll().penaltyLog().build());
		// StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		// .detectAll().penaltyDeath().penaltyLog().build());
		// }
		if (!Config.DEVELOPER_MODE)
		{
			CrashHandler crashHandler = CrashHandler.getInstance();
			crashHandler.init(getApplicationContext());
		}
		// 设置开启日志,发布时请关闭日志
		JPushInterface.setDebugMode(Config.DEVELOPER_MODE);
		JPushInterface.init(this);
	}

	private void initImageLoader(Context context)
	{
		File cacheDir = StorageUtils.getOwnCacheDirectory(context, DRMUtil.getDefaultImageCachePath());
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).diskCacheExtraOptions(480, 800, null).threadPoolSize(3)
				// default
				.threadPriority(Thread.NORM_PRIORITY - 2)
				// default
				.tasksProcessingOrder(QueueProcessingType.FIFO)
				// default
				.denyCacheImageMultipleSizesInMemory().memoryCache(new LruMemoryCache(2 * 1024 * 1024)).memoryCacheSize(2 * 1024 * 1024)
				.memoryCacheSizePercentage(13)
				// default
				.diskCache(new UnlimitedDiskCache(cacheDir))
				// default
				.diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(100).diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
				.imageDownloader(new BaseImageDownloader(context)) // default
				.imageDecoder(new BaseImageDecoder(false)) // default
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
				.build();
		ImageLoader.getInstance().init(config);
	}

}
