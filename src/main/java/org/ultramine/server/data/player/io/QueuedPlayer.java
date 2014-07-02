package org.ultramine.server.data.player.io;

import org.ultramine.server.data.IDataProvider;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;

public class QueuedPlayer
{
	private final IDataProvider provider;
	private final NetworkManager network;
	private final EntityPlayerMP player;
	private final NetHandlerPlayServer nethandler;
	private final boolean loadData;

	public QueuedPlayer(IDataProvider provider, NetworkManager network, EntityPlayerMP player, NetHandlerPlayServer nethandler, boolean loadData)
	{
		this.provider = provider;
		this.network = network;
		this.player = player;
		this.nethandler = nethandler;
		this.loadData = loadData;
	}

	public IDataProvider getDataProvider()
	{
		return provider;
	}

	public NetworkManager getNetwork()
	{
		return network;
	}

	public EntityPlayerMP getPlayer()
	{
		return player;
	}

	public NetHandlerPlayServer getNethandler()
	{
		return nethandler;
	}

	public boolean shouldLoadData()
	{
		return loadData;
	}
}
