package org.ultramine.server.data;

import gnu.trove.set.TIntSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ultramine.commands.basic.FastWarpCommand;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.data.player.PlayerDataExtension;
import org.ultramine.server.data.player.PlayerDataExtensionInfo;
import org.ultramine.server.util.TwoStepsExecutor;
import org.ultramine.server.util.WarpLocation;

import com.google.common.base.Function;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.event.ForgeEventFactory;

public class ServerDataLoader
{
	private static final boolean isClient = FMLCommonHandler.instance().getSide().isClient();
	private final TwoStepsExecutor executor = isClient ? null : new TwoStepsExecutor("PlayerData loader #%d");
	private final ServerConfigurationManager mgr;
	private final IDataProvider dataProvider;
	private final List<PlayerDataExtensionInfo> dataExtinfos = new ArrayList<PlayerDataExtensionInfo>();
	private final Map<UUID, PlayerData> playerDataCache = new HashMap<UUID, PlayerData>();
	private final Map<String, PlayerData> namedPlayerDataCache = new HashMap<String, PlayerData>();
	private final Map<String, WarpLocation> warps = new HashMap<String, WarpLocation>();
	private final List<String> fastWarps = new ArrayList<String>();
	
	public ServerDataLoader(ServerConfigurationManager mgr)
	{
		this.mgr = mgr;
		dataProvider = isClient || !ConfigurationHandler.getServerConfig().settings.inSQLServerStorage.enabled ? new NBTFileDataProvider(mgr) : new JDBCDataProvider(mgr);
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
		return namedPlayerDataCache.get(username);
	}
	
	public WarpLocation getWarp(String name)
	{
		return warps.get(name);
	}
	
	public void setWarp(String name, WarpLocation warp)
	{
		warps.put(name, warp);
		dataProvider.saveWarp(name, warp);
	}
	
	public boolean removeWarp(String name)
	{
		if(warps.remove(name) != null)
		{
			dataProvider.removeWarp(name);
			return true;
		}
		return false;
	}
	
	public Map<String, WarpLocation> getWarps()
	{
		return warps;
	}
	
	public void addFastWarp(String name)
	{
		if(fastWarps.add(name))
		{
			dataProvider.saveFastWarp(name);
			((CommandHandler)mgr.getServerInstance().getCommandManager()).getRegistry().registerCommand(new FastWarpCommand(name));
		}
	}
	
	public boolean removeFastWarp(String name)
	{
		if(fastWarps.remove(name))
		{
			dataProvider.removeFastWarp(name);
			((CommandHandler)mgr.getServerInstance().getCommandManager()).getRegistry().getCommandMap().remove(name);
			return true;
		}
		return false;
	}
	
	public List<String> getFastWarps()
	{
		return fastWarps;
	}
	
	public void loadCache()
	{
		dataProvider.init();
		if(!isClient) executor.register();
		
		for(PlayerData data : dataProvider.loadAllPlayerData())
		{
			playerDataCache.put(data.getProfile().getId(), data);
			namedPlayerDataCache.put(data.getProfile().getName(), data);
		}
		warps.putAll(dataProvider.loadWarps());
		fastWarps.addAll(dataProvider.loadFastWarps());
	}
	
	public void addDefaultWarps()
	{
		if(!warps.containsKey("spawn"))
		{
			WorldInfo wi = mgr.getServerInstance().getMultiWorld().getWorldByID(0).getWorldInfo();
			setWarp("spawn", new WarpLocation(0, wi.getSpawnX(), wi.getSpawnY(), wi.getSpawnZ(), 0, 0, 20));
		}
		if(!fastWarps.contains("spawn"))
		{
			fastWarps.add("spawn");
			dataProvider.saveFastWarp("spawn");
		}
		if(!isClient)
		{
			String firstSpawn = ConfigurationHandler.getServerConfig().settings.spawnLocations.firstSpawn;
			String deathSpawn = ConfigurationHandler.getServerConfig().settings.spawnLocations.deathSpawn;
			if(!warps.containsKey(firstSpawn)) setWarp(firstSpawn, getWarp("spawn"));
			if(!warps.containsKey(deathSpawn)) setWarp(deathSpawn, getWarp("spawn"));
		}
	}
	
	public void initializeConnectionToPlayer(final NetworkManager network, final EntityPlayerMP player, final NetHandlerPlayServer nethandler)
	{
		if(isClient)
		{
			NBTTagCompound nbt = mgr.readPlayerDataFromFile(player);
			player.setData(getDataProvider().loadPlayerData(player.getGameProfile()));
			StatisticsFile existsStats = mgr.func_152602_a(player);
			StatisticsFile stats = existsStats != null ? existsStats : mgr.loadStatisticsFile_Async(player.getGameProfile());
			mgr.addStatFile(player.getGameProfile(), stats);
			player.setStatisticsFile(stats);
			mgr.initializeConnectionToPlayer_body(network, player, nethandler, nbt);
		}
		else
		{
			final GameProfile profile = player.getGameProfile();
			final boolean loadData = !playerDataCache.containsKey(player.getGameProfile().getId());
			final StatisticsFile loadedStats = mgr.func_152602_a(player);
			final TIntSet isolatedDataDims = mgr.getServerInstance().getMultiWorld().getIsolatedDataDims();
			executor.execute(new Function<Void, LoadedDataStruct>()
			{
				@Override
				public LoadedDataStruct apply(Void input) //async
				{
					NBTTagCompound nbt = getDataProvider().loadPlayer(profile);
					if(nbt != null)
					{
						int dim = nbt.getInteger("Dimension");
						if(dim != 0 && isolatedDataDims.contains(dim))
							nbt = getDataProvider().loadPlayer(dim, profile);
					}
					PlayerData data = loadData ? getDataProvider().loadPlayerData(profile) : null;
					StatisticsFile stats = loadedStats != null ? loadedStats : mgr.loadStatisticsFile_Async(profile);
					return new LoadedDataStruct(nbt, data, stats);
				}
			}, new Function<LoadedDataStruct, Void>()
			{
				@Override
				public Void apply(LoadedDataStruct data) //sync
				{
					if(data.getNBT() != null)
						player.readFromNBT(data.getNBT());
					playerLoadCallback(network, player, nethandler, data.getNBT(), data.getPlayerData(), data.getStats());
					
					return null;
				}
			});
		}
	}
	
	@SideOnly(Side.SERVER)
	private void playerLoadCallback(NetworkManager network, EntityPlayerMP player, NetHandlerPlayServer nethandler, NBTTagCompound nbt, PlayerData data, StatisticsFile stats)
	{
		if(data != null)
		{
			player.setData(data);
			playerDataCache.put(data.getProfile().getId(), data);
			namedPlayerDataCache.put(data.getProfile().getName(), data);
		}
		else
		{
			player.setData(playerDataCache.get(player.getGameProfile().getId()));
		}
		mgr.addStatFile(player.getGameProfile(), stats);
		player.setStatisticsFile(stats);
		WarpLocation spawn = null;
		if(nbt == null) //first login
		{
			WarpLocation spawnWarp = getWarp(isClient ? "spawn" : ConfigurationHandler.getServerConfig().settings.spawnLocations.firstSpawn);
			spawn = (spawnWarp != null ? spawnWarp : getWarp("spawn"));
		}
		else
		{
			String warpName = mgr.getServerInstance().getMultiWorld().getConfigByID(player.dimension).settings.reconnectOnWarp;
			if(warpName != null)
				spawn = getWarp(warpName);
		}
		if(spawn != null)
		{
			spawn = spawn.randomize();
			player.setLocationAndAngles(spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch);
		}
		ForgeEventFactory.firePlayerLoadingEvent(player, ((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir(), player.getUniqueID().toString());
		mgr.initializeConnectionToPlayer_body(network, player, nethandler, nbt);
	}
	
	public void savePlayer(EntityPlayerMP player)
	{
		ForgeEventFactory.firePlayerSavingEvent(player, ((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir(), player.getUniqueID().toString());
		NBTTagCompound nbt = new NBTTagCompound();
		player.writeToNBT(nbt);
		
		if(player.getServerForPlayer().getConfig().settings.useIsolatedPlayerData)
			getDataProvider().savePlayer(player.worldObj.provider.dimensionId, player.getGameProfile(), nbt);
		else
			getDataProvider().savePlayer(player.getGameProfile(), nbt);
		getDataProvider().savePlayerData(player.getData());
	}
	
	public void handlePlayerDimensionChange(EntityPlayerMP player, int fromDim, int toDim)
	{
		WorldServer from = mgr.getServerInstance().getMultiWorld().getWorldByID(fromDim);
		WorldServer to = mgr.getServerInstance().getMultiWorld().getWorldByID(toDim);

		boolean fromIs = from.getConfig().settings.useIsolatedPlayerData;
		boolean toIs = to.getConfig().settings.useIsolatedPlayerData;

		if(fromIs || toIs)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			player.writeToNBT(nbt);
			if(fromIs)
				dataProvider.savePlayer(fromDim, player.getGameProfile(), nbt);
			else// if(toIs)
				dataProvider.savePlayer(player.getGameProfile(), nbt);

			loadIsolatedData(player, toDim, toIs, true);
		}
	}

	public void handleRespawn(EntityPlayerMP dead, EntityPlayerMP created, int oldDim, int newDim)
	{
		WorldServer from = mgr.getServerInstance().getMultiWorld().getWorldByID(oldDim);
		WorldServer to = mgr.getServerInstance().getMultiWorld().getWorldByID(newDim);

		boolean fromIs = from.getConfig().settings.useIsolatedPlayerData;
		boolean toIs = to.getConfig().settings.useIsolatedPlayerData;

		if(fromIs || toIs)
		{
			if(fromIs)
				dataProvider.savePlayer(oldDim, dead.getGameProfile(), new NBTTagCompound());

			loadIsolatedData(created, newDim, toIs, false);
		}
	}
	
	private void loadIsolatedData(final EntityPlayerMP player, final int toDim, final boolean toIs, boolean replaceToNull)
	{
		final GameProfile profile = player.getGameProfile();
		if(isClient)
		{
			NBTTagCompound nbt;
			if(toIs)
				nbt = dataProvider.loadPlayer(toDim, profile);
			else// if(fromIs)
				nbt = dataProvider.loadPlayer(profile);
			applyIsolatedData(player, nbt);
		}
		else
		{
			if(replaceToNull)
				applyIsolatedData(player, null);

			executor.execute(new Function<Void, NBTTagCompound>()
			{
				@Override
				public NBTTagCompound apply(Void input) //async
				{
					if(toIs)
						return dataProvider.loadPlayer(toDim, profile);
					else// if(fromIs)
						return dataProvider.loadPlayer(profile);
				}
			}, new Function<NBTTagCompound, Void>()
			{
				@Override
				public Void apply(NBTTagCompound nbt) //sync
				{
					player.inventory.dropAllItems();
					applyIsolatedData(player, nbt);
					return null;
				}
			});
		}
	}

	private void applyIsolatedData(EntityPlayerMP player, NBTTagCompound nbt)
	{
		double x = player.posX;
		double y = player.posY;
		double z = player.posZ;
		float yaw = player.rotationYaw;
		float pitch = player.rotationPitch;

		player.readFromNBT(nbt != null ? nbt : new NBTTagCompound());

		player.dimension = player.worldObj.provider.dimensionId;
		player.prevPosX = player.lastTickPosX = player.posX = x;
		player.prevPosY = player.lastTickPosY = player.posY = y;
		player.prevPosZ = player.lastTickPosZ = player.posZ = z;
		player.prevRotationYaw = player.rotationYaw = yaw;
		player.prevRotationPitch = player.rotationPitch = pitch;
	}

	public void registerPlayerDataExt(Class<? extends PlayerDataExtension> clazz, String nbtTagName)
	{
		dataExtinfos.add(new PlayerDataExtensionInfo(clazz, nbtTagName));
	}
	
	public List<PlayerDataExtensionInfo> getDataExtProviders()
	{
		return dataExtinfos;
	}
	
	private static class LoadedDataStruct
	{
		private final NBTTagCompound nbt;
		private final PlayerData data;
		private final StatisticsFile stats;

		public LoadedDataStruct(NBTTagCompound nbt, PlayerData data, StatisticsFile stats)
		{
			this.nbt = nbt;
			this.data = data;
			this.stats = stats;
		}

		public NBTTagCompound getNBT()
		{
			return nbt;
		}

		public PlayerData getPlayerData()
		{
			return data;
		}
		
		public StatisticsFile getStats()
		{
			return stats;
		}
	}
}
