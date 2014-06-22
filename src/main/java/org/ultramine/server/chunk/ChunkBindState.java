package org.ultramine.server.chunk;

public enum ChunkBindState
{
	/**
	 * Чанк ничем не занят и может быть выгружен в любой момент. Стандартное
	 * значение при асинхронной загрузке чанка.
	 */
	NONE,
	/**
	 * Чанк занят игроком/игроками. Как только все игроки выйдут из радиуса
	 * прогрузки, значение сменится на <code>NONE</code>.
	 */
	PLAYER,
	/**
	 * Чанк был загружен синхронно, в обход менеджера загрузки чанков. По логике
	 * ванильного майна это является утечкой памяти - чанк не будет выгружен из
	 * памяти до тех пор, пока не будет помечен к отгрузке вручную (например, в
	 * него зайдет и выйдет игрок). Но у нас чанк будет выгружен через некоторое
	 * время.
	 */
	LEAK,
	/**
	 * Чанку запрещено выгружаться или изменять состояние бинда. Чанк не будет
	 * выгружен никогда.
	 */
	ETERNAL;

	public boolean canUnload()
	{
		return this == NONE;
	}

	public boolean canChangeState()
	{
		return this != ETERNAL;
	}
	
	public boolean isLeak()
	{
		return this == LEAK;
	}
}
