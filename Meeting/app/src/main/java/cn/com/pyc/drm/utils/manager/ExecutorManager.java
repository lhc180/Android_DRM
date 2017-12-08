package cn.com.pyc.drm.utils.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorManager
{

	private static ExecutorService sExecutors;

	public static ExecutorService getInstance()
	{

		if (sExecutors == null) sExecutors = Executors.newCachedThreadPool();

		return sExecutors;
	}

	public static void closeExecutor()
	{
		if (sExecutors == null) return;

		try
		{
			sExecutors.shutdown();
			sExecutors.awaitTermination(3 * 1000, TimeUnit.SECONDS);
		} catch (Exception e)
		{
		} finally
		{
			sExecutors.shutdownNow();
			sExecutors = null;
		}
	}

}
