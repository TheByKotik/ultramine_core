package org.ultramine.server.data.player.io;

import org.ultramine.server.data.IDataProvider;
import org.ultramine.server.data.ServerDataLoader;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.util.AsynchronousExecutor;

public class PlayerDataIOExecutor
{
	private static final AsynchronousExecutor<QueuedPlayer, LoadedDataStruct, ServerDataLoader, RuntimeException> instance
		= new AsynchronousExecutor<QueuedPlayer, LoadedDataStruct, ServerDataLoader, RuntimeException>(new PlayerDataIOProvider(), 1);
	
	public static void requestData(IDataProvider provider, NetworkManager network, EntityPlayerMP player, NetHandlerPlayServer nethandler,
			ServerDataLoader callback, boolean loadData)
	{
		instance.add(new QueuedPlayer(provider, network, player, nethandler, loadData), callback);
	}
	
	public static void tick()
	{
		instance.finishActive();
	}
}
