package org.ultramine.server.data.player.io;

import java.util.concurrent.atomic.AtomicInteger;

import org.ultramine.server.data.ServerDataLoader;
import org.ultramine.server.data.player.PlayerData;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.AsynchronousExecutor;

public class PlayerDataIOProvider implements AsynchronousExecutor.CallBackProvider<QueuedPlayer, LoadedDataStruct, ServerDataLoader, RuntimeException>
{
	private final AtomicInteger threadNumber = new AtomicInteger(1);

	@Override
	public Thread newThread(Runnable runnable)
	{
		Thread thread = new Thread(runnable, "PlayerData I/O Executor Thread-" + threadNumber.getAndIncrement());
		thread.setDaemon(true);
		return thread;
	}

	@Override
	public LoadedDataStruct callStage1(QueuedPlayer param) throws RuntimeException
	{
		NBTTagCompound nbt =  param.getDataProvider().loadPlayer(param.getPlayer().getGameProfile());
		PlayerData data = param.shouldLoadData() ? param.getDataProvider().loadPlayerData(param.getPlayer().getGameProfile()) : null;
		
		return new LoadedDataStruct(nbt, data);
	}

	@Override
	public void callStage2(QueuedPlayer param, LoadedDataStruct data) throws RuntimeException
	{
		if(data.getNBT() != null)
			param.getPlayer().readFromNBT(data.getNBT());
	}

	@Override
	public void callStage3(QueuedPlayer param, LoadedDataStruct data, ServerDataLoader callback) throws RuntimeException
	{
		callback.plyaerLoadCallback(param.getNetwork(), param.getPlayer(), param.getNethandler(), data.getNBT(), data.getPlayerData());
	}
}
