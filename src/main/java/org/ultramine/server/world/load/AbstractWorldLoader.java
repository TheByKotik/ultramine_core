package org.ultramine.server.world.load;

import org.ultramine.server.WorldsConfig.WorldConfig;
import org.ultramine.server.world.WorldDescriptor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.WorldInfo;

public abstract class AbstractWorldLoader implements IWorldLoader
{
	protected final WorldDescriptor desc;
	protected final MinecraftServer server;
	
	protected AbstractWorldLoader(WorldDescriptor desc, MinecraftServer server)
	{
		this.desc = desc;
		this.server = server;
	}
	
	@Override
	public boolean hasAsyncLoadPhase()
	{
		return false;
	}
	
	@Override
	public void doAsyncLoadPhase()
	{
		
	}
	
	@Override
	public void dispose()
	{
		RegionFileCache.clearRegionFileReferences();
	}
	
	@SideOnly(Side.SERVER)
	protected WorldSettings makeSettings(WorldInfo wi, WorldConfig conf)
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
	
	protected static long toSeed(String seedstr)
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
}
