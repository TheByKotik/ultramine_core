package org.ultramine.server.chunk;

import net.minecraft.world.chunk.Chunk;

public interface IChunkLoadCallback
{
	public void onChunkLoaded(Chunk chunk);
}
