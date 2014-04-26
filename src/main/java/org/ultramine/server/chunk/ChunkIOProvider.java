package org.ultramine.server.chunk;

import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;

class ChunkIOProvider implements AsynchronousExecutor.CallBackProvider<QueuedChunk, Chunk, Runnable, RuntimeException>
{
	private final AtomicInteger threadNumber = new AtomicInteger(1);

	// async stuff
	public Chunk callStage1(QueuedChunk queuedChunk) throws RuntimeException
	{
		AnvilChunkLoader loader = queuedChunk.loader;
		Object[] data = loader.loadChunk__Async_CB(queuedChunk.world, ChunkHash.keyToX(queuedChunk.coords), ChunkHash.keyToZ(queuedChunk.coords));

		if(data != null)
		{
			queuedChunk.compound = (net.minecraft.nbt.NBTTagCompound) data[1];
			return (Chunk) data[0];
		}

		return null;
	}

	// sync stuff
	public void callStage2(QueuedChunk queuedChunk, Chunk chunk) throws RuntimeException
	{
		if(chunk == null)
		{
			// If the chunk loading failed just do it synchronously (may
			// generate)
			queuedChunk.provider.loadChunk(ChunkHash.keyToX(queuedChunk.coords), ChunkHash.keyToZ(queuedChunk.coords));
			return;
		}

		int x = ChunkHash.keyToX(queuedChunk.coords);
		int z = ChunkHash.keyToZ(queuedChunk.coords);

		// See if someone already loaded this chunk while we were working on it
		// (API, etc)
		if(queuedChunk.provider.loadedChunkHashMap.containsKey(queuedChunk.coords))
		{
			// Make sure it isn't queued for unload, we need it
			queuedChunk.provider.chunksToUnload.remove(queuedChunk.coords); // Spigot
			return;
		}

		queuedChunk.loader.loadEntities(chunk, queuedChunk.compound.getCompoundTag("Level"), queuedChunk.world);
		MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunk, queuedChunk.compound)); // MCPC+
																								// -
																								// Don't
																								// call
																								// ChunkDataEvent.Load
																								// async
		chunk.lastSaveTime = queuedChunk.provider.worldObj.getTotalWorldTime();
		queuedChunk.provider.loadedChunkHashMap.put(queuedChunk.coords, chunk);
		chunk.onChunkLoad();

		if(queuedChunk.provider.currentChunkProvider != null)
		{
			queuedChunk.provider.currentChunkProvider.recreateStructures(x, z);
		}

		chunk.populateChunk(queuedChunk.provider, queuedChunk.provider, x, z);
		chunk.func_150804_b(false);
	}

	public void callStage3(QueuedChunk queuedChunk, Chunk chunk, Runnable runnable) throws RuntimeException
	{
		runnable.run();
	}

	public Thread newThread(Runnable runnable)
	{
		Thread thread = new Thread(runnable, "Chunk I/O Executor Thread-" + threadNumber.getAndIncrement());
		thread.setDaemon(true);
		return thread;
	}
}
