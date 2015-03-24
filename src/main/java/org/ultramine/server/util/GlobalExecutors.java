package org.ultramine.server.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class GlobalExecutors
{
	private static final ExecutorService io = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("UM IO #%d").setDaemon(true).build());
	private static final ExecutorService cached = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("UM cached  #%d").setDaemon(true).build());

	/**
	 * Обрабатывает задачи на сохранение чего-либо на диск/в БД. Используется
	 * единственный поток, т.к. при сохранениее не требуется наискорейшее
	 * выполнение задачи.
	 */
	public static ExecutorService writingIOExecutor()
	{
		return io;
	}

	/**
	 * Обрабатывает задачи, требующие наискорейшего выполнения. Создает любое
	 * количество потоков по мере необходимости. При остановке сервер не ожидает
	 * окончания выполнения задач
	 */
	public static ExecutorService cachedExecutor()
	{
		return cached;
	}
}
