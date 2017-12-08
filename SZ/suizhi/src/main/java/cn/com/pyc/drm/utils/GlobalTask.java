package cn.com.pyc.drm.utils;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*-
 * 建议所有联网操作都通过此类完成
 * 异步任务也可以用此类来完成
 * 只是进一步封装了new Thread(r).start()而已
 */
public class GlobalTask
{
	private static final ExecutorService EXECUTOR_SERVICE = Executors
			.newFixedThreadPool(4);		// 4个常驻线程

	public static void execute(Runnable task)
	{
		EXECUTOR_SERVICE.execute(task);
	}

	public static Future<?> submit(Runnable task)
	{
		return EXECUTOR_SERVICE.submit(task);
	}

	/*-
	 * 以下是联网的多个重载方法
	 */

	// 后台异步执行业务时（不带进度条）
	public static void executeTask(Context context, final Runnable netTask)
	{
		executeTask(context, netTask, false);
	}

	// 联网时
	public static void executeNetTask(Context context, final Runnable netTask)
	{
		executeTask(context, netTask, true);
	}

	// 前台异步执行业务时（带进度条）
	public static void executeNormalTask(Context context, final Runnable netTask)
	{
		executeTask(context, netTask, true);
	}

	// 自定义
	public static void executeTask(Context context, final Runnable netTask,
			boolean showDialog)
	{
		execute(new Runnable()
		{
			@Override
			public void run()
			{
				if (netTask != null)
				{
					netTask.run();
				}
			}
		});
	}

}
