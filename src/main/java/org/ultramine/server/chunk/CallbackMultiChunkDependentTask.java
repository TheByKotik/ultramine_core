package org.ultramine.server.chunk;

import net.minecraft.world.chunk.Chunk;

public class CallbackMultiChunkDependentTask implements IChunkLoadCallback, IChunkDependency
{
	private int numChunksForLoad;
	private final Runnable task;
	
	public CallbackMultiChunkDependentTask(int numChunksForLoad, Runnable task)
	{
		this.numChunksForLoad = numChunksForLoad;
		this.task = task;
	}

	@Override
	public void onChunkLoaded(Chunk chunk)
	{
		chunk.addDependency(this);
		if(numChunksForLoad == 1)
			task.run();
		
		numChunksForLoad--;
	}

	@Override
	public boolean isDependent(Chunk chunk)
	{
		return numChunksForLoad != 0;
	}
}
