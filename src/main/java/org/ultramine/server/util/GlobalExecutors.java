package org.ultramine.server.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.ultramine.permission.internal.SyncServerExecutorImpl;

public class GlobalExecutors
{
	private static final ExecutorService writing = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("UM IO writing #%d").setDaemon(true).build());
	private static final ExecutorService cached = new ThreadPoolExecutor(
			2, Integer.MAX_VALUE,
			60L, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>(),
			new ThreadFactoryBuilder().setNameFormat("UM IO cached  #%d").setDaemon(true).build());
	private static final SyncServerExecutor sync = new SyncServerExecutorImpl();

	/**
	 * Обрабатывает задачи на сохранение чего-либо на диск/в БД. Используется
	 * единственный поток, т.к. при сохранениее не требуется наискорейшее
	 * выполнение задачи.
	 */
	public static ExecutorService writingIO()
	{
		return writing;
	}
	
	@Deprecated
	public static ExecutorService writingIOExecutor()
	{
		return writing;
	}

	/**
	 * Обрабатывает задачи, требующие наискорейшего выполнения. Создает любое
	 * количество потоков по мере необходимости. При остановке сервер не ожидает
	 * окончания выполнения задач
	 */
	public static ExecutorService cachedIO()
	{
		return cached;
	}

	@Deprecated
	public static ExecutorService cachedExecutor()
	{
		return cached;
	}
	
	/**
	 * Выполняет задачи в основном потоке сервера, на следующем тике
	 */
	public static SyncServerExecutor nextTick()
	{
		return sync;
	}
}
