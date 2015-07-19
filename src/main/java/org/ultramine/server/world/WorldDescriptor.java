package org.ultramine.server.world;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.Teleporter;
import org.ultramine.server.WorldsConfig.WorldConfig;
import org.ultramine.server.WorldsConfig.WorldConfig.MobSpawn.MobSpawnEngine;
import org.ultramine.server.WorldsConfig.WorldConfig.Settings.WorldTime;
import org.ultramine.server.util.BasicTypeParser;
import org.ultramine.server.util.WarpLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WorldDescriptor
{
	private static final Logger log = LogManager.getLogger();
	
	private final MinecraftServer server;
	private final MultiWorld mw;
	private final boolean splitWorldDirs;
	private final int dimension;
	
	private String name;
	private File directory;
	private WorldConfig config;
	private WorldState state = WorldState.UNREGISTERED;
	private WorldServer world;
	
	public WorldDescriptor(MinecraftServer server, MultiWorld mw, boolean splitWorldDirs, int dimension, String name)
	{
		this.server = server;
		this.mw = mw;
		this.splitWorldDirs = splitWorldDirs;
		this.dimension = dimension;
		this.name = name;
		this.directory = new File(server.getWorldsDir(), name);
	}
	
	public int getDimension()
	{
		return dimension;
	}
	
	public String getName()
	{
		return name;
	}
	
	void setName(String name)
	{
		if(!this.name.equals(name))
		{
			this.mw.transitDescName(this, this.name, name);
			this.name = name;
			this.directory = new File(server.getWorldsDir(), this.name);
		}
	}
	
	public File getDirectory()
	{
		return this.directory;
	}
	
	public WorldConfig getConfig()
	{
		return config;
	}
	
	void setConfig(WorldConfig config)
	{
		this.config = config;
		if(state.isLoaded())
			applyConfig();
	}
	
	public WorldState getState()
	{
		return state;
	}
	
	void setState(WorldState state)
	{
		this.state = state;
	}
	
	public WorldServer getWorld()
	{
		return world;
	}
	
	void setWorld(WorldServer world)
	{
		this.world = world;
		this.directory = world.getSaveHandler().getWorldDirectory();
		if(dimension != 0 && !splitWorldDirs)
			this.directory = new File(this.directory, world.provider.getSaveFolder());
	}
	
	public WorldServer getOrLoadWorld()
	{
		if(state != WorldState.LOADED)
			weakLoad();
		return world;
	}
	
	public void register()
	{
		if(state != WorldState.UNREGISTERED)
			throw new IllegalStateException("Dimension "+dimension+" already registered");
		if(config == null)
			throw new IllegalStateException("Can not register dimension "+dimension+": world config == null!");
		if(DimensionManager.isDimensionRegistered(dimension))
			DimensionManager.unregisterDimension(dimension);
		DimensionManager.registerDimension(dimension, config.generation.providerID);
		setState(WorldState.UNLOADED);
		mw.sendDimensionToAll(dimension, config.generation.providerID);
	}
	
	@SideOnly(Side.SERVER)
	public void forceLoad()
	{
		if(state == WorldState.UNREGISTERED)
			register();
		load();
	}
	
	@SideOnly(Side.SERVER)
	public void weakLoad()
	{
		if(state == WorldState.HELD || state == WorldState.UNREGISTERED)
			return;
		
		load();
	}
	
	@SideOnly(Side.SERVER)
	private void load()
	{
		if(state.isLoaded())
			throw new RuntimeException("Dimension ["+dimension+"] is already loaded");
		
		ISaveFormat format = server.getActiveAnvilConverter();
		if(config == null)
		{
			log.warn("World with dimension id:{} was loaded bypass worlds configuration. Using global config", dimension);
			config = ConfigurationHandler.getWorldsConfig().global;
		}
		
		WorldServer world;
		if(dimension == 0)
		{
			ISaveHandler mainSaveHandler = format.getSaveLoader(name, true);
			WorldInfo mainWorldInfo = mainSaveHandler.loadWorldInfo();
			WorldSettings mainSettings = makeSettings(mainWorldInfo, config);
			
			world = new WorldServer(server, mainSaveHandler, name, dimension, mainSettings, server.theProfiler);
		}
		else if(splitWorldDirs)
		{
			ISaveHandler save = format.getSaveLoader(name, false);
			((AnvilSaveHandler)save).setSingleStorage();
			world = new WorldServer(server, save, name, dimension, makeSettings(save.loadWorldInfo(), config), server.theProfiler);
		}
		else
		{
			WorldServer mainWorld = mw.getWorldByID(0);
			ISaveHandler mainSaveHandler = mainWorld.getSaveHandler();
			WorldInfo mainWorldInfo = mainWorld.getWorldInfo();
			world = new WorldServerMulti(server, mainSaveHandler, mainWorldInfo.getWorldName(), dimension, makeSettings(mainWorldInfo, config), mainWorld, server.theProfiler);
		}
		
		setWorld(world);
		initWorld();
	}
	
	@SideOnly(Side.SERVER)
	private WorldSettings makeSettings(WorldInfo wi, WorldConfig conf)
	{
		WorldSettings mainSettings;

		if (wi == null)
		{
			mainSettings = new WorldSettings(toSeed(conf.generation.seed), server.getGameType(), conf.generation.generateStructures,
					server.isHardcore(), WorldType.parseWorldType(conf.generation.levelType));
			mainSettings.func_82750_a(conf.generation.generatorSettings);
		}
		else
		{
			mainSettings = new WorldSettings(wi);
		}
		
		return mainSettings;
	}
	
	private static long toSeed(String seedstr)
	{
		try
		{
			return Long.parseLong(seedstr);
		}
		catch (NumberFormatException e)
		{
			return seedstr.hashCode();
		}
	}
	
	@SideOnly(Side.SERVER)
	private void initWorld()
	{
		world.addWorldAccess(new WorldManager(server, world));
		world.getWorldInfo().setGameType(server.getGameType());
		applyConfig();
		setState(WorldState.LOADED);
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
	}
	
	@SideOnly(Side.SERVER)
	private void applyConfig()
	{
		world.difficultySetting = BasicTypeParser.parseDifficulty(config.settings.difficulty);
		world.setAllowedSpawnTypes(config.mobSpawn.spawnMonsters, config.mobSpawn.spawnAnimals);
		world.getGameRules().setOrCreateGameRule("doDaylightCycle", Boolean.toString(config.settings.time != WorldTime.FIXED));
		world.getGameRules().setOrCreateGameRule("doMobSpawning", Boolean.toString(config.mobSpawn.spawnEngine != MobSpawnEngine.NONE));
		world.setConfig(config);
	}
	
	void onUnload()
	{
		((WorldServer)world).theChunkProviderServer.setWorldUnloaded();
		world = null;
		if(getState().isLoaded())
			setState(WorldState.UNLOADED);
	}
	
	public List<EntityPlayerMP> extractPlayer()
	{
		if(!state.isLoaded())
			return Collections.emptyList();
		
		@SuppressWarnings("unchecked")
		List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>(world.playerEntities);
		for(EntityPlayerMP player : players)
		{
			world.removePlayerEntityDangerously(player);
			player.isDead = false;
			world.getEntityTracker().removePlayerFromTrackers(player);
			world.getPlayerManager().removePlayer(player);
			player.getChunkMgr().setWorldDestroyed();
			player.setWorld(null);
			player.theItemInWorldManager.setWorld(null);
		}
		world.playerEntities.clear();
		
		return players;
	}
	
	@SideOnly(Side.SERVER)
	public void hold()
	{
		if(getState().isLoaded())
			unload();
		setState(WorldState.HELD);
	}
	
	@SideOnly(Side.SERVER)
	public void unload()
	{
		if(!getState().isLoaded())
			return;
		if(!world.playerEntities.isEmpty())
			movePlayersOut();
		
		DimensionManager.unloadWorld(dimension);
	}
	
	@SuppressWarnings("unchecked")
	public void destroyWorld()
	{
		if(!getState().isLoaded())
			return;
		if(!world.playerEntities.isEmpty())
			movePlayersOut();
		
		WorldServer world = this.world;
		if(world.provider.dimensionId == 0)
			for(ScorePlayerTeam team : new ArrayList<ScorePlayerTeam>(world.getScoreboard().getTeams()))
				world.getScoreboard().removeTeam(team);
		
		world.theChunkProviderServer.setWorldUnloaded();
		world.theChunkProviderServer.unloadAll(false);
		world.forceUnloadTileEntities();
		
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(world));
		DimensionManager.setWorld(world.provider.dimensionId, null);
		world.theChunkProviderServer.free();
		for(Object o : world.loadedTileEntityList)
			((TileEntity)o).setWorldObj(null);
		world.loadedTileEntityList.clear();
		dispose();
	}
	
	@SideOnly(Side.SERVER)
	public void deleteWorld()
	{
		if(state.isLoaded())
			destroyWorld();
		else
			dispose();
		
		try
		{
			FileUtils.cleanDirectory(getDirectory());
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void dispose()
	{
		try
		{
			ThreadedFileIOBase.threadedIOInstance.waitForFinish();
		} catch (InterruptedException ignored){}

		RegionFileCache.clearRegionFileReferences();
	}
	
	private void movePlayersOut()
	{
		WarpLocation spawn = server.getConfigurationManager().getDataLoader().getWarp("spawn");
		@SuppressWarnings("unchecked")
		List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>(world.playerEntities);
		for(EntityPlayerMP player : players)
		{
			if(player.dimension == spawn.dimension)
			{
				player.playerNetServerHandler.kickPlayerFromServer("The world has been unloaded");
				world.removePlayerEntityDangerously(player);
				player.isDead = false;
				world.getEntityTracker().removePlayerFromTrackers(player);
				world.getPlayerManager().removePlayer(player);
			}
			else
			{
				Teleporter.tpNow(player, spawn);
			}
		}
	}
}
