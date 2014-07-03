package org.ultramine.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.data.player.PlayerDataExtension;
import org.ultramine.server.data.player.PlayerDataExtensionInfo;
import org.ultramine.server.data.player.io.PlayerDataIOExecutor;
import org.ultramine.server.util.GlobalExecutors;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.event.ForgeEventFactory;

public class ServerDataLoader
{
	private static final boolean isClient = FMLCommonHandler.instance().getSide().isClient();
	private final ServerConfigurationManager mgr;
	private final IDataProvider dataProvider;
	private final List<PlayerDataExtensionInfo> dataExtinfos = new ArrayList<PlayerDataExtensionInfo>();
	private final Map<UUID, PlayerData> playerDataCache = new HashMap<UUID, PlayerData>();
	
	public ServerDataLoader(ServerConfigurationManager mgr)
	{
		this.mgr = mgr;
		dataProvider = new NBTFileDataProvider(mgr);
	}
	
	public IDataProvider getDataProvider()
	{
		return dataProvider;
	}
	
	public PlayerData getPlayerData(GameProfile profile)
	{
		return playerDataCache.get(profile.getId());
	}
	
	public PlayerData getPlayerData(UUID id)
	{
		return playerDataCache.get(id);
	}
	
	public PlayerData getPlayerData(String username)
	{
		return playerDataCache.get(mgr.getServerInstance().func_152358_ax().func_152655_a(username));
	}
	
	public void loadCache()
	{
		for(PlayerData data : dataProvider.loadAllPlayerData())
			playerDataCache.put(data.getProfile().getId(), data);
	}
	
	public void initializeConnectionToPlayer(NetworkManager network, EntityPlayerMP player, NetHandlerPlayServer nethandler)
	{
		if(isClient)
		{
			NBTTagCompound nbt = mgr.readPlayerDataFromFile(player);
			player.setData(getDataProvider().loadPlayerData(player.getGameProfile()));
			mgr.initializeConnectionToPlayer_body(network, player, nethandler, nbt);
		}
		else
		{
			PlayerDataIOExecutor.requestData(getDataProvider(), network, player, nethandler, this, !playerDataCache.containsKey(player.getGameProfile().getId()));
		}
	}
	
	public void plyaerLoadCallback(NetworkManager network, EntityPlayerMP player, NetHandlerPlayServer nethandler, NBTTagCompound nbt, PlayerData data)
	{
		if(data != null)
		{
			player.setData(data);
			playerDataCache.put(data.getProfile().getId(), data);
		}
		else
		{
			player.setData(playerDataCache.get(player.getGameProfile().getId()));
		}
		ForgeEventFactory.firePlayerLoadingEvent(player, ((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir(), player.getUniqueID().toString());
		mgr.initializeConnectionToPlayer_body(network, player, nethandler, nbt);
	}
	
	public void savePlayer(final EntityPlayerMP player)
	{
		ForgeEventFactory.firePlayerSavingEvent(player, ((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir(), player.getUniqueID().toString());
		final NBTTagCompound nbt = new NBTTagCompound();
		player.writeToNBT(nbt);
		GlobalExecutors.writingIOExecutor().execute(new Runnable()
		{
			@Override
			public void run()
			{
				getDataProvider().savePlayer(player.getGameProfile(), nbt);
				getDataProvider().savePlayerData(player.getData());
			}
		});
	}
	
	public void registerPlayerDataExt(Class<? extends PlayerDataExtension> clazz, String nbtTagName)
	{
		dataExtinfos.add(new PlayerDataExtensionInfo(clazz, nbtTagName));
	}
	
	public List<PlayerDataExtensionInfo> getDataExtProviders()
	{
		return dataExtinfos;
	}
}
