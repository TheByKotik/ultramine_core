package org.ultramine.server.chunk;

import gnu.trove.TCollections;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.ultramine.server.util.BlockFace;
import org.ultramine.server.util.ChunkCoordComparator;
import org.ultramine.server.util.TIntArrayListImpl;

import com.google.common.collect.Queues;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkWatchEvent;

public class ChunkSendManager
{
	private static final ExecutorService executor = Executors.newFixedThreadPool(1);
	private static final int MAX_QUEUE_SIZE = 8;
	private static final int DEFAULT_RATE = 3;
	private static final double MIN_RATE = 0.2d;
	private static final double MAX_RATE = 8d;
	
	private final EntityPlayerMP player;
	private PlayerManager manager;
	private BlockFace lastFace;
	private int lastViewDistance;
	
	private final TIntArrayListImpl toSend = new TIntArrayListImpl(441);
	private final TIntSet sending = TCollections.synchronizedSet(new TIntHashSet());
	private final TIntSet sended = new TIntHashSet();
	private final Queue<Chunk> toUpdate = Queues.newConcurrentLinkedQueue();
	private final Queue<Chunk> toUnload = Queues.newConcurrentLinkedQueue();
	private final AtomicInteger sendingQueueSize = new AtomicInteger();
	
	private int lastQueueSize;
	private double rate = DEFAULT_RATE;
	private int intervalCounter = 1;
	
	public ChunkSendManager(EntityPlayerMP player)
	{
		this.player = player;
	}
	
	private int getViewDistance()
	{
		return Math.min(manager.getWorldServer().getConfig().chunkLoading.viewDistance, player.getRenderDistance());
	}
	
	private void sortSendQueue()
	{
		int cx = MathHelper.floor_double(player.posX) >> 4;
		int cz = MathHelper.floor_double(player.posZ) >> 4;
		toSend.sort(ChunkCoordComparator.get(lastFace = BlockFace.yawToFace(player.rotationYaw), cx, cz));
	}
	
	private void checkDistance()
	{
		int curView = getViewDistance();
		if(curView != lastViewDistance)
		{
			int cx = MathHelper.floor_double(player.posX) >> 4;
			int cz = MathHelper.floor_double(player.posZ) >> 4;
			
			if(curView < lastViewDistance)
			{
				for(TIntIterator it = toSend.iterator(); it.hasNext();)
				{
					int key = it.next();
					if(!overlaps(cx, cz, ChunkHash.keyToX(key), ChunkHash.keyToZ(key), curView))
						it.remove();
				}
				
				for(TIntIterator it = sending.iterator(); it.hasNext();)
				{
					int key = it.next();
					if(!overlaps(cx, cz, ChunkHash.keyToX(key), ChunkHash.keyToZ(key), curView))
						it.remove();
				}
				
				for(TIntIterator it = sended.iterator(); it.hasNext();)
				{
					int key = it.next();
					if(!overlaps(cx, cz, ChunkHash.keyToX(key), ChunkHash.keyToZ(key), curView))
					{
						PlayerManager.PlayerInstance pi = manager.getOrCreateChunkWatcher(ChunkHash.keyToX(key), ChunkHash.keyToZ(key), false);
						if(pi != null) pi.removePlayer(player);
						it.remove();
					}
				}
			}
			else
			{
				for (int x = cx - curView; x <= cx + curView; ++x)
				{
					for (int z = cz - curView; z <= cz + curView; ++z)
					{
						int key = ChunkHash.chunkToKey(x, z);
						if(!toSend.contains(key) && !sended.contains(key) && !sending.contains(key))
						{
							toSend.add(key);
						}
					}
				}
			}
			
			lastViewDistance = curView;
			sortSendQueue();
		}
	}
	
	public void addTo(PlayerManager manager)
	{
		if(this.manager != null) throw new IllegalStateException("PlayerManager already set");
		this.manager = manager;
		
		player.managedPosX = player.posX;
		player.managedPosZ = player.posZ;
		
		int cx = MathHelper.floor_double(player.posX) >> 4;
		int cz = MathHelper.floor_double(player.posZ) >> 4;
		int viewRadius = lastViewDistance = getViewDistance();
		
		for (int x = cx - viewRadius; x <= cx + viewRadius; ++x)
		{
			for (int z = cz - viewRadius; z <= cz + viewRadius; ++z)
			{
				toSend.add(ChunkHash.chunkToKey(x, z));
			}
		}
		
		sortSendQueue();
		
		sendChunks(Math.max(1, (int)rate));
	}
	
	public void removeFrom(PlayerManager manager)
	{
		if(this.manager == null) return;
		if(this.manager != manager) throw new IllegalStateException();
		
		toSend.clear();
		sending.clear();
		
		for(TIntIterator it = sended.iterator(); it.hasNext();)
		{
			int key = it.next();
			PlayerManager.PlayerInstance pi = manager.getOrCreateChunkWatcher(ChunkHash.keyToX(key), ChunkHash.keyToZ(key), false);
			if (pi != null) pi.removePlayer(player);
		}
		
		sended.clear();
		this.manager = null;
	}
	
	public void update()
	{
		if(!toSend.isEmpty())
		{
			int queueSize = sendingQueueSize.get();
			
			if(queueSize == 0)
			{
				rate += 0.14;
			}
			else if(queueSize < DEFAULT_RATE)
			{
				rate += 0.07;
			}
			else if(queueSize > lastQueueSize)
			{
				if(queueSize > MAX_QUEUE_SIZE)
					rate -= 0.14;
				else
					rate -= 0.07;
			}
			if(rate < MIN_RATE) rate = MIN_RATE;
			else if(rate > MAX_RATE) rate = MAX_RATE;
			
			if(queueSize == 0 || (queueSize != lastQueueSize && queueSize <= MAX_QUEUE_SIZE))
			{
				lastQueueSize = queueSize;
			
				if(rate >= 1)
				{
					sendChunks((int)rate);
				}
				else
				{
					int interval = Math.max(1, (int)(1/rate));
					if(intervalCounter++ >= interval)
					{
						intervalCounter = 1;
						sendChunks(1);
					}
				}
				
			}
		}
		
		while(!toUpdate.isEmpty())
		{
			Chunk chunk = toUpdate.poll();
			int key = ChunkHash.chunkToKey(chunk.xPosition, chunk.zPosition);
			
			if(sending.contains(key))
			{
				manager.getOrCreateChunkWatcher(chunk.xPosition, chunk.zPosition, true).addPlayer(player);
				
				List<?> tes = manager.getWorldServer().func_147486_a(chunk.xPosition * 16, 0, chunk.zPosition * 16, chunk.xPosition * 16 + 15, 256, chunk.zPosition * 16 + 15);
				for(Object o : tes)
				{
					TileEntity te = (TileEntity)o;
					Packet packet = te.getDescriptionPacket();

					if (packet != null)
					{
						player.playerNetServerHandler.sendPacket(packet);
					}
				}
				
				manager.getWorldServer().getEntityTracker().func_85172_a(player, chunk);
				MinecraftForge.EVENT_BUS.post(new ChunkWatchEvent.Watch(chunk.getChunkCoordIntPair(), player));
				
				sended.add(key);
				sending.remove(key);
			}
			else
			{
				player.playerNetServerHandler.sendPacket(S21PacketChunkData.makeForUnload(chunk));
				
				PlayerManager.PlayerInstance pi = manager.getOrCreateChunkWatcher(chunk.xPosition, chunk.zPosition, false);
				if (pi == null)
					((WorldServer)chunk.worldObj).theChunkProviderServer.unbindChunk(chunk);
			}
		}
		
		while(!toUnload.isEmpty())
		{
			Chunk chunk = toUnload.poll();
			
			PlayerManager.PlayerInstance pi = manager.getOrCreateChunkWatcher(chunk.xPosition, chunk.zPosition, false);
			if (pi == null)
				((WorldServer)chunk.worldObj).theChunkProviderServer.unbindChunk(chunk);
		}
	}
	
	private void sendChunks(int count)
	{
		count = Math.min(count, toSend.size());
		for(int i = 0; i < count; i++)
		{
			int key = toSend.get(i);
			sending.add(key);
			sendingQueueSize.incrementAndGet();
			int ncx = ChunkHash.keyToX(key);
			int ncz = ChunkHash.keyToZ(key);
			manager.getWorldServer().theChunkProviderServer.loadAsync(ncx, ncz, chunkLoadCallback);
		}
		toSend.remove(0, count);
	}
	
	public void updatePlayerPertinentChunks()
	{
		checkDistance();
		
		int cx = MathHelper.floor_double(player.posX) >> 4;
		int cz = MathHelper.floor_double(player.posZ) >> 4;
		double d0 = player.managedPosX - player.posX;
		double d1 = player.managedPosZ - player.posZ;
		double square = d0 * d0 + d1 * d1;

		boolean sorted = false;
		
		if (square >= 64.0D)
		{
			int lastX = MathHelper.floor_double(player.managedPosX) >> 4;
			int lastZ = MathHelper.floor_double(player.managedPosZ) >> 4;
			int view = getViewDistance();
			int movX = cx - lastX;
			int movZ = cz - lastZ;

			if (movX != 0 || movZ != 0)
			{
				for (int x = cx - view; x <= cx + view; ++x)
				{
					for (int z = cz - view; z <= cz + view; ++z)
					{
						if (!overlaps(x, z, lastX, lastZ, view))
						{
							toSend.add(ChunkHash.chunkToKey(x, z));
						}

						if (!overlaps(x - movX, z - movZ, cx, cz, view))
						{
							int key = ChunkHash.chunkToKey(x - movX, z - movZ);
							if(!toSend.remove(key))
							{
								if(sended.contains(key))
								{
									PlayerManager.PlayerInstance pi = manager.getOrCreateChunkWatcher(x - movX, z - movZ, false);
									if(pi != null) pi.removePlayer(player);
									sended.remove(key);
								}
								else
								{
									sending.remove(key);
								}
							}
						}
					}
				}

				sortSendQueue();
				sorted = true;
				player.managedPosX = player.posX;
				player.managedPosZ = player.posZ;
			}
		}
		
		if(!sorted)
		{
			BlockFace face = BlockFace.yawToFace(player.rotationYaw);
			if(face != lastFace)
			{
				sortSendQueue();
			}
		}
	}
	
	private boolean overlaps(int x, int z, int lastX, int lastZ, int radius)
	{
		int movX = x - lastX;
		int movZ = z - lastZ;
		return movX >= -radius && movX <= radius ? movZ >= -radius && movZ <= radius : false;
	}
	
	
	
	
	private IChunkLoadCallback chunkLoadCallback = new IChunkLoadCallback()
	{
		@Override
		public void onChunkLoaded(Chunk chunk)
		{
			chunk.setBindState(ChunkBindState.PLAYER);
			executor.execute(new CompressAndSendChunkTask(chunk));
		}
	};
	
	private class CompressAndSendChunkTask implements Runnable
	{
		private final Chunk chunk;
		
		public CompressAndSendChunkTask(Chunk chunk)
		{
			this.chunk = chunk;
		}
		
		@Override
		public void run()
		{
			if(sending.contains(ChunkHash.chunkToKey(chunk.xPosition, chunk.zPosition)))
			{
				player.playerNetServerHandler.netManager.scheduleOutboundPacket(S21PacketChunkData.makeDeflated(chunk),
					new GenericFutureListener<Future<Void>>()
					{
						@Override
						public void operationComplete(Future<Void> future) throws Exception
						{
							sendingQueueSize.decrementAndGet();
						}
					});
				
				toUpdate.add(chunk);
			}
			else
			{
				sendingQueueSize.decrementAndGet();
				toUnload.add(chunk);
			}
		}
	}
}
