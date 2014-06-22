package org.ultramine.server.chunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

@SideOnly(Side.SERVER)
public class ChunkGC
{
	private static final int MIN_GC_INTERVAL = 100;
	private static final int MAX_UNLOAD_QUEUE_SIZE = 128;
	private static final int MAX_CHUNKS_PER_OP = 1024;
	private static final int _10_MINUTES = 20*60*10;
	
	private final ChunkProviderServer provider;
	private final WorldServer world;
	
	private int lastGCTime;
	private int lastChunkCount;
	private int minChunkDiff;
	
	public ChunkGC(ChunkProviderServer provider)
	{
		this.provider = provider;
		this.world = provider.worldObj;
	}
	
	public void onTick()
	{
		int confCacheSize = world.getConfig().chunkLoading.chunkCacheSize;
		int chunksPerPlayer = (int)Math.pow(world.getConfig().chunkLoading.viewDistance*2 + 1, 2);
		int boundChunks = world.playerEntities.size()*chunksPerPlayer + world.getPersistentChunks().size();
		int chunkLimit = boundChunks + confCacheSize + MAX_CHUNKS_PER_OP;
		
		int curTime = world.func_73046_m().getTickCounter();
		int unloadQueueSize = provider.chunksToUnload.size();
		int chunkCount = provider.loadedChunkHashMap.size() - unloadQueueSize;
		int timePassed = curTime - lastGCTime;
		int chunkDiff = chunkCount - lastChunkCount;
		
		if(chunkCount > chunkLimit && timePassed > MIN_GC_INTERVAL && unloadQueueSize < MAX_UNLOAD_QUEUE_SIZE && (minChunkDiff == 0 || chunkDiff > minChunkDiff))
		{
			Collection<Chunk> all = provider.loadedChunkHashMap.valueCollection();
			List<Chunk> unbound = new ArrayList<Chunk>(all.size() - boundChunks);
			for(Chunk chunk : all)
			{
				if(chunk.getBindState().canUnload())
				{
					unbound.add(chunk);
				}
				else if(chunk.getBindState().isLeak() && curTime - chunk.getUnbindTime() > _10_MINUTES)
				{
					chunk.unbind();
					unbound.add(chunk);
				}
			}
			
			int unboundLimit = confCacheSize + MAX_CHUNKS_PER_OP + unloadQueueSize;
			
			if(unbound.size() > unboundLimit)
			{
				//performing GC
				Collections.sort(unbound, new ChunkComparator(curTime));
				
				for(int i = 0, s = Math.min(unbound.size(), MAX_CHUNKS_PER_OP); i < s; i++)
				{
					Chunk chunk = unbound.get(i);
					provider.chunksToUnload.add(ChunkHash.chunkToKey(chunk.xPosition, chunk.zPosition));
				}
				
				if(unbound.size() - Math.min(unbound.size(), MAX_CHUNKS_PER_OP) > unboundLimit)
					minChunkDiff = 0;
				else
					minChunkDiff = unboundLimit - unbound.size();
			}
			else
			{
				minChunkDiff = unboundLimit - unbound.size();
			}
			
			lastGCTime = curTime;
			lastChunkCount = provider.loadedChunkHashMap.size() - provider.chunksToUnload.size();
		}
	}
	
	private static class ChunkComparator implements Comparator<Chunk>
	{
		private int curTime;
		
		public ChunkComparator(int curTime)
		{
			this.curTime = curTime;
		}
		
		@Override
		public int compare(Chunk c1, Chunk c2)
		{
			float c = (float)Math.max(curTime - c1.getLoadTime(), _10_MINUTES)/(float)Math.max(curTime - c1.getUnbindTime(), 1)
					- (float)Math.max(curTime - c2.getLoadTime(), _10_MINUTES)/(float)Math.max(curTime - c2.getUnbindTime(), 1);
			return c == 0 ? 0 : c < 0 ? -1 : 1;
		}
	};
}
