package org.ultramine.server.world.load;

import org.ultramine.server.world.WorldDescriptor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class NotSplittedWorldLoader extends AbstractWorldLoader
{
	public NotSplittedWorldLoader(WorldDescriptor desc, MinecraftServer server)
	{
		super(desc, server);
	}

	@Override
	public WorldServer doLoad()
	{
		WorldServer mainWorld = server.getMultiWorld().getWorldByID(0);
		ISaveHandler mainSaveHandler = mainWorld.getSaveHandler();
		WorldInfo mainWorldInfo = mainWorld.getWorldInfo();
		return new WorldServerMulti(
				server,
				mainSaveHandler,
				mainWorldInfo.getWorldName(),
				desc.getDimension(),
				makeSettings(mainWorldInfo, desc.getConfig()),
				mainWorld,
				server.theProfiler
			);
	}
}
