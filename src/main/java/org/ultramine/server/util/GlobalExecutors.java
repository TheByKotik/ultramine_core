package org.ultramine.server.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalExecutors
{
	private static final ExecutorService io = Executors.newSingleThreadExecutor();

	/**
	 * Обрабатывает задачи на сохранение чего-либо на диск/в БД. Используется
	 * единственный поток, т.к. при сохранениее не требуется наискорейшее
	 * выполнение задачи.
	 */
	public static ExecutorService writingIOExecutor()
	{
		return io;
	}
}
