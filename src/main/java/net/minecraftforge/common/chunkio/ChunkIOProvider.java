package net.minecraftforge.common.chunkio;


import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.AsynchronousExecutor;
import net.minecraftforge.event.world.ChunkDataEvent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.ultramine.server.chunk.ChunkHash;
import org.ultramine.server.chunk.IChunkLoadCallback;

class ChunkIOProvider implements AsynchronousExecutor.CallBackProvider<QueuedChunk, net.minecraft.world.chunk.Chunk, IChunkLoadCallback, RuntimeException> {
	private final AtomicInteger threadNumber = new AtomicInteger(1);

	// async stuff
	public net.minecraft.world.chunk.Chunk callStage1(QueuedChunk queuedChunk) throws RuntimeException {
		if(queuedChunk.provider.isWorldUnloaded())
			return null;
		net.minecraft.world.chunk.storage.AnvilChunkLoader loader = queuedChunk.loader;
		Object[] data = null;
//		try {
			data = loader.loadChunk__Async(queuedChunk.world, queuedChunk.x, queuedChunk.z);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		if (data != null) {
			queuedChunk.compound = (net.minecraft.nbt.NBTTagCompound) data[1];
			return (net.minecraft.world.chunk.Chunk) data[0];
		}

		return null;
	}

	// sync stuff
	public void callStage2(QueuedChunk queuedChunk, net.minecraft.world.chunk.Chunk chunk) throws RuntimeException {
		if(queuedChunk.provider.isWorldUnloaded())
			return;
		if(chunk == null) {
			// If the chunk loading failed just do it synchronously (may generate)
			queuedChunk.provider.originalLoadChunk(queuedChunk.x, queuedChunk.z);
			return;
		}

		queuedChunk.loader.loadEntities(queuedChunk.world, queuedChunk.compound.getCompoundTag("Level"), chunk);
		MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunk, queuedChunk.compound)); // Don't call ChunkDataEvent.Load async
		chunk.lastSaveTime = queuedChunk.provider.worldObj.getTotalWorldTime();
		queuedChunk.provider.chunkMap.put(ChunkHash.chunkToKey(queuedChunk.x, queuedChunk.z), chunk);
		chunk.onChunkLoad();

		if (queuedChunk.provider.currentChunkProvider != null) {
			queuedChunk.provider.currentChunkProvider.recreateStructures(queuedChunk.x, queuedChunk.z);
		}

		chunk.populateChunk(queuedChunk.provider, queuedChunk.provider, queuedChunk.x, queuedChunk.z);
	}

	public void callStage3(QueuedChunk queuedChunk, net.minecraft.world.chunk.Chunk chunk, IChunkLoadCallback callback) throws RuntimeException {
		if(queuedChunk.provider.isWorldUnloaded())
			return;
		callback.onChunkLoaded(chunk != null ? chunk : queuedChunk.provider.getChunkIfExists(queuedChunk.x, queuedChunk.z));
	}

	public Thread newThread(Runnable runnable) {
		Thread thread = new Thread(runnable, "Chunk I/O Executor Thread-" + threadNumber.getAndIncrement());
		thread.setDaemon(true);
		return thread;
	}
}
