package org.ultramine.server.world.load;

import org.ultramine.server.world.WorldDescriptor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;

public class SplittedWorldLoader extends AbstractWorldLoader
{
	public SplittedWorldLoader(WorldDescriptor desc, MinecraftServer server)
	{
		super(desc, server);
	}

	@Override
	public WorldServer doLoad()
	{
		ISaveHandler save = getSaveHandler();
		((AnvilSaveHandler)save).setSingleStorage();
		return new WorldServer(server, save, desc.getName(), desc.getDimension(), makeSettings(save.loadWorldInfo(), desc.getConfig()), server.theProfiler);
	}
	
	protected ISaveHandler getSaveHandler()
	{
		return server.getActiveAnvilConverter().getSaveLoader(desc.getName(), false);
	}
}
