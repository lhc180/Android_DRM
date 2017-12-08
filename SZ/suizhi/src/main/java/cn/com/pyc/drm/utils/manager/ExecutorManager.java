package cn.com.pyc.drm.utils.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorManager {

	private static ExecutorService sExecutor = null;
	private static ExecutorManager instance;

	private ExecutorManager() {
	}

	public static ExecutorManager getInstance() {
		createExecutor();
		if (instance == null)
			instance = new ExecutorManager();
		return instance;
	}

	private static ExecutorService createExecutor() {
		if (sExecutor == null)
			sExecutor = Executors.newCachedThreadPool();
		return sExecutor;
	}

	public void execute(Runnable r) {
		createExecutor();

		sExecutor.execute(r);
	}

	public void shutdownNow() {
		createExecutor();
		try {
			// shutdown方法：这个方法会平滑地关闭ExecutorService，当我们调用这个方法时，
			// ExecutorService停止接受任何新的任务且等待已经提交的任务执行完成
			// (已经提交的任务会分两类：一类是已经在执行的，另一类是还没有开始执行的)，当所有已经提交的任务执行完毕后将会关闭ExecutorService。
			sExecutor.shutdown();
			// awaitTermination方法：这个方法有两个参数，一个是timeout即超时时间，
			// 另一个是unit即时间单位。这个方法会使线程等待timeout时长，当超过timeout时间后，
			// 会监测ExecutorService是否已经关闭，若关闭则返回true，否则返回false。一般情况下会和shutdown方法组合使用。
			sExecutor.awaitTermination(3 * 1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			sExecutor.shutdownNow();
			sExecutor = null;
		}
	}

}
