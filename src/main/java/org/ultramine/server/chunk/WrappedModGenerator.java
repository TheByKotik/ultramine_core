package org.ultramine.server.chunk;

import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.ModContainer;

public class WrappedModGenerator implements IWorldGenerator
{
	private final IWorldGenerator wrapped;
	private final ModContainer owner;
	
	public WrappedModGenerator(IWorldGenerator wrapped, ModContainer owner)
	{
		this.wrapped = wrapped;
		this.owner = owner;
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		List<String> modGenerationBlackList = ((WorldServer)world).getConfig().generation.modGenerationBlackList;
		if(modGenerationBlackList == null || owner == null || !modGenerationBlackList.contains(owner.getModId()))
			wrapped.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
	}

	public int hashCode()
	{
		return wrapped.hashCode();
	}
	
	public boolean equals(Object o)
	{
		return o instanceof WrappedModGenerator && ((WrappedModGenerator)o).wrapped.equals(wrapped) || wrapped.equals(o);
	}
}
