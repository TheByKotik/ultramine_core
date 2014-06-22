package net.minecraft.world.gen;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.chunk.ChunkBindState;
import org.ultramine.server.chunk.ChunkGC;
import org.ultramine.server.chunk.ChunkHash;
import org.ultramine.server.chunk.ChunkMap;
import org.ultramine.server.chunk.IChunkLoadCallback;

public class ChunkProviderServer implements IChunkProvider
{
	private static final Logger logger = LogManager.getLogger();
	public TIntSet chunksToUnload = new TIntHashSet();
	private Chunk defaultEmptyChunk;
	public IChunkProvider currentChunkProvider;
	public IChunkLoader currentChunkLoader;
	public boolean loadChunkOnProvideRequest = true;
	public ChunkMap loadedChunkHashMap = new ChunkMap();
	public WorldServer worldObj;
	private static final String __OBFID = "CL_00001436";

	public ChunkProviderServer(WorldServer par1WorldServer, IChunkLoader par2IChunkLoader, IChunkProvider par3IChunkProvider)
	{
		this.defaultEmptyChunk = new EmptyChunk(par1WorldServer, 0, 0);
		this.worldObj = par1WorldServer;
		this.currentChunkLoader = par2IChunkLoader;
		this.currentChunkProvider = par3IChunkProvider;
		
		if(isServer)
			chunkGC = new ChunkGC(this);
	}

	public boolean chunkExists(int par1, int par2)
	{
		return this.loadedChunkHashMap.contains(par1, par2);
	}

	public void unloadChunksIfNotNearSpawn(int par1, int par2)
	{
		this.chunksToUnload.add(ChunkHash.chunkToKey(par1, par2));
	}

	public void unloadAllChunks()
	{
		//Iterator iterator = this.loadedChunks.iterator();

		//while (iterator.hasNext())
		//{
		//	Chunk chunk = (Chunk)iterator.next();
		//	this.unloadChunksIfNotNearSpawn(chunk.xPosition, chunk.zPosition);
		//}
	}

	public Chunk loadChunk(int par1, int par2)
	{
		return loadChunk(par1, par2, null);
	}

	public Chunk loadChunk(int par1, int par2, Runnable runnable)
	{
		this.chunksToUnload.remove(ChunkHash.chunkToKey(par1, par2));
		Chunk chunk = loadedChunkHashMap.get(par1, par2);
		AnvilChunkLoader loader = null;

		if (this.currentChunkLoader instanceof AnvilChunkLoader)
		{
			loader = (AnvilChunkLoader) this.currentChunkLoader;
		}

		// We can only use the queue for already generated chunks
		if (chunk == null && loader != null && loader.chunkExists(this.worldObj, par1, par2))
		{
			if (runnable != null)
			{
				ChunkIOExecutor.queueChunkLoad(this.worldObj, loader, this, par1, par2, runnable);
				return null;
			}
			else
			{
				chunk = ChunkIOExecutor.syncChunkLoad(this.worldObj, loader, this, par1, par2);
				chunk.setBindState(ChunkBindState.LEAK);
				logger.warn("The chunk("+par1+", "+par2+") was loaded sync", new Throwable());
			}
		}
		else if (chunk == null)
		{
			chunk = this.originalLoadChunk(par1, par2);
		}

		// If we didn't load the chunk async and have a callback run it now
		if (runnable != null)
		{
			runnable.run();
		}

		return chunk;
	}

	public Chunk originalLoadChunk(int par1, int par2)
	{
		int k = ChunkHash.chunkToKey(par1, par2);
		this.chunksToUnload.remove(k);
		Chunk chunk = (Chunk)this.loadedChunkHashMap.get(par1, par2);

		if (chunk == null)
		{
//			chunk = ForgeChunkManager.fetchDormantChunk(k, this.worldObj);
//			if (chunk == null)
			{
				chunk = this.safeLoadChunk(par1, par2);
			}

			if (chunk == null)
			{
				if (this.currentChunkProvider == null)
				{
					chunk = this.defaultEmptyChunk;
				}
				else
				{
					try
					{
						chunk = this.currentChunkProvider.provideChunk(par1, par2);
					}
					catch (Throwable throwable)
					{
						CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
						CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
						crashreportcategory.addCrashSection("Location", String.format("%d,%d", new Object[] {Integer.valueOf(par1), Integer.valueOf(par2)}));
						crashreportcategory.addCrashSection("Position hash", Long.valueOf(k));
						crashreportcategory.addCrashSection("Generator", this.currentChunkProvider.makeString());
						throw new ReportedException(crashreport);
					}
				}
			}

			this.loadedChunkHashMap.put(par1, par2, chunk);
			chunk.onChunkLoad();
			chunk.populateChunk(this, this, par1, par2);
			chunk.func_150804_b(false);
		}

		return chunk;
	}

	public Chunk provideChunk(int par1, int par2)
	{
		Chunk chunk = this.loadedChunkHashMap.get(par1, par2);
		return chunk == null ? (!this.worldObj.findingSpawnPoint && !this.loadChunkOnProvideRequest ? this.defaultEmptyChunk : this.loadChunk(par1, par2)) : chunk;
	}

	private Chunk safeLoadChunk(int par1, int par2)
	{
		if (this.currentChunkLoader == null)
		{
			return null;
		}
		else
		{
			try
			{
				Chunk chunk = this.currentChunkLoader.loadChunk(this.worldObj, par1, par2);

				if (chunk != null)
				{
					chunk.lastSaveTime = this.worldObj.getTotalWorldTime();

					if (this.currentChunkProvider != null)
					{
						this.currentChunkProvider.recreateStructures(par1, par2);
					}
				}

				return chunk;
			}
			catch (Exception exception)
			{
				logger.error("Couldn\'t load chunk", exception);
				return null;
			}
		}
	}

	private void safeSaveExtraChunkData(Chunk par1Chunk)
	{
		if (this.currentChunkLoader != null)
		{
			try
			{
				this.currentChunkLoader.saveExtraChunkData(this.worldObj, par1Chunk);
			}
			catch (Exception exception)
			{
				logger.error("Couldn\'t save entities", exception);
			}
		}
	}

	private void safeSaveChunk(Chunk par1Chunk)
	{
		if (this.currentChunkLoader != null)
		{
			try
			{
				par1Chunk.lastSaveTime = this.worldObj.getTotalWorldTime();
				this.currentChunkLoader.saveChunk(this.worldObj, par1Chunk);
			}
			catch (IOException ioexception)
			{
				logger.error("Couldn\'t save chunk", ioexception);
			}
			catch (MinecraftException minecraftexception)
			{
				logger.error("Couldn\'t save chunk; already in use by another instance of Minecraft?", minecraftexception);
			}
		}
	}

	public void populate(IChunkProvider par1IChunkProvider, int par2, int par3)
	{
		Chunk chunk = this.provideChunk(par2, par3);

		if (!chunk.isTerrainPopulated)
		{
			chunk.func_150809_p();

			if (this.currentChunkProvider != null)
			{
				this.currentChunkProvider.populate(par1IChunkProvider, par2, par3);
				GameRegistry.generateWorld(par2, par3, worldObj, currentChunkProvider, par1IChunkProvider);
				chunk.setChunkModified();
			}
		}
	}

	public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
	{
		int i = 0;

		for (Chunk chunk : loadedChunkHashMap.valueCollection())
		{
			if (par1)
			{
				this.safeSaveExtraChunkData(chunk);
			}

			if (chunk.needsSaving(par1))
			{
				this.safeSaveChunk(chunk);
				chunk.isModified = false;
				++i;

				if (i == 24 && !par1)
				{
					return false;
				}
			}
		}

		return true;
	}

	public void saveExtraData()
	{
		if (this.currentChunkLoader != null)
		{
			this.currentChunkLoader.saveExtraData();
		}
	}

	public boolean unloadQueuedChunks()
	{
//		if (!this.worldObj.levelSaving)
		{
			if(isServer)
				chunkGC.onTick();
			
			for (ChunkCoordIntPair forced : this.worldObj.getPersistentChunks().keySet())
			{
				this.chunksToUnload.remove(ChunkHash.chunkToKey(forced.chunkXPos, forced.chunkZPos));
			}

			/*
			for (int i = 0; i < 100; ++i)
			{
				if (!this.chunksToUnload.isEmpty())
				{
					Long olong = (Long)this.chunksToUnload.iterator().next();
					Chunk chunk = (Chunk)this.loadedChunkHashMap.getValueByKey(olong.longValue());
					chunk.onChunkUnload();
					this.safeSaveChunk(chunk);
					this.safeSaveExtraChunkData(chunk);
					this.chunksToUnload.remove(olong);
					this.loadedChunkHashMap.remove(olong.longValue());
					this.loadedChunks.remove(chunk);
					ForgeChunkManager.putDormantChunk(ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition), chunk);
					if(loadedChunks.size() == 0 && ForgeChunkManager.getPersistentChunksFor(this.worldObj).size() == 0 && !DimensionManager.shouldLoadSpawn(this.worldObj.provider.dimensionId)){
						DimensionManager.unloadWorld(this.worldObj.provider.dimensionId);
						return currentChunkProvider.unloadQueuedChunks();
					}
				}
			}
			*/
			
			int processed = 0;
			for(TIntIterator it = chunksToUnload.iterator(); it.hasNext();)
			{
				if(processed >= 20) break;
				int hash = it.next();
				Chunk chunk = loadedChunkHashMap.get(hash);
				if(chunk != null)
				{
					if(chunk.getBindState().canUnload())
					{
						chunk.onChunkUnload();
						if(true/*chunk.shouldSaveOnUnload()*/)
						{
							processed++;
							safeSaveChunk(chunk);
						}
						this.safeSaveExtraChunkData(chunk);
						this.loadedChunkHashMap.remove(hash);
						//chunk.postChunkUnload();
					}
				}
				else
				{
					logger.warn("Not existing chunk was queued for unload (" + ChunkHash.keyToX(hash) + ", " + ChunkHash.keyToZ(hash) + ")");
				}
				
				it.remove();
			}

			if (this.currentChunkLoader != null)
			{
				this.currentChunkLoader.chunkTick();
			}
		}

		return this.currentChunkProvider.unloadQueuedChunks();
	}

	public boolean canSave()
	{
		return !this.worldObj.levelSaving;
	}

	public String makeString()
	{
		return "ServerChunkCache: " + this.loadedChunkHashMap.size() + " Drop: " + this.chunksToUnload.size();
	}

	public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
	{
		return this.currentChunkProvider.getPossibleCreatures(par1EnumCreatureType, par2, par3, par4);
	}

	public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_)
	{
		return this.currentChunkProvider.func_147416_a(p_147416_1_, p_147416_2_, p_147416_3_, p_147416_4_, p_147416_5_);
	}

	public int getLoadedChunkCount()
	{
		return this.loadedChunkHashMap.size();
	}

	public void recreateStructures(int par1, int par2) {}
	
	
	/* ======================================== ULTRAMINE START =====================================*/
	
	private static final boolean isServer = FMLCommonHandler.instance().getSide().isServer();
	
	@SideOnly(Side.SERVER)
	private ChunkGC chunkGC;
	
	public void loadAsync(int x, int z, IChunkLoadCallback callback)
	{
		Chunk chunk = loadedChunkHashMap.get(x, z);
		if(chunk != null)
		{
			callback.onChunkLoaded(chunk);
			return;
		}
		else
		{
			ChunkIOExecutor.queueChunkLoad(this.worldObj, (AnvilChunkLoader)currentChunkLoader, this, x, z, callback);
		}
	}
	
	public Chunk getChunkIfExists(int cx, int cz)
	{
		return loadedChunkHashMap.get(cx, cz);
	}
	
	public void unbindChunk(int cx, int cz)
	{
		Chunk chunk = loadedChunkHashMap.get(cx, cz);
		if(chunk != null)
			unbindChunk(chunk);
	}
	
	public void unbindChunk(Chunk chunk)
	{
		chunk.unbind();
		if(!isServer)
			unloadChunksIfNotNearSpawn(chunk.xPosition, chunk.zPosition);
	}
}