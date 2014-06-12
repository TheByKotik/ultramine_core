package net.minecraft.server.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ultramine.server.chunk.ChunkIOExecutor;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkWatchEvent;

public class PlayerManager
{
	private final WorldServer theWorldServer;
	private final List players = new ArrayList();
	private final LongHashMap playerInstances = new LongHashMap();
	private final List chunkWatcherWithPlayers = new ArrayList();
	private final List playerInstanceList = new ArrayList();
	private final int playerViewRadius;
	private long previousTotalWorldTime;
	private final int[][] xzDirectionsConst = new int[][] {{1, 0}, {0, 1}, { -1, 0}, {0, -1}};
	private static final String __OBFID = "CL_00001434";

	public PlayerManager(WorldServer par1WorldServer, int par2)
	{
		if (par2 > 15)
		{
			throw new IllegalArgumentException("Too big view radius!");
		}
		else if (par2 < 3)
		{
			throw new IllegalArgumentException("Too small view radius!");
		}
		else
		{
			this.playerViewRadius = par2;
			this.theWorldServer = par1WorldServer;
		}
	}

	public WorldServer getWorldServer()
	{
		return this.theWorldServer;
	}

	public void updatePlayerInstances()
	{
		long i = this.theWorldServer.getTotalWorldTime();
		int j;
		PlayerManager.PlayerInstance playerinstance;

		if (i - this.previousTotalWorldTime > 8000L)
		{
			this.previousTotalWorldTime = i;

			for (j = 0; j < this.playerInstanceList.size(); ++j)
			{
				playerinstance = (PlayerManager.PlayerInstance)this.playerInstanceList.get(j);
				playerinstance.sendChunkUpdate();
				playerinstance.processChunk();
			}
		}
		else
		{
			for (j = 0; j < this.chunkWatcherWithPlayers.size(); ++j)
			{
				playerinstance = (PlayerManager.PlayerInstance)this.chunkWatcherWithPlayers.get(j);
				playerinstance.sendChunkUpdate();
			}
		}

		this.chunkWatcherWithPlayers.clear();

		if (this.players.isEmpty())
		{
			WorldProvider worldprovider = this.theWorldServer.provider;

			if (!worldprovider.canRespawnHere())
			{
				this.theWorldServer.theChunkProviderServer.unloadAllChunks();
			}
		}
	}

	public PlayerManager.PlayerInstance getOrCreateChunkWatcher(int par1, int par2, boolean par3)
	{
		long k = (long)par1 + 2147483647L | (long)par2 + 2147483647L << 32;
		PlayerManager.PlayerInstance playerinstance = (PlayerManager.PlayerInstance)this.playerInstances.getValueByKey(k);

		if (playerinstance == null && par3)
		{
			playerinstance = new PlayerManager.PlayerInstance(par1, par2);
			this.playerInstances.add(k, playerinstance);
			this.playerInstanceList.add(playerinstance);
		}

		return playerinstance;
	}

	public void markBlockForUpdate(int p_151250_1_, int p_151250_2_, int p_151250_3_)
	{
		int l = p_151250_1_ >> 4;
		int i1 = p_151250_3_ >> 4;
		PlayerManager.PlayerInstance playerinstance = this.getOrCreateChunkWatcher(l, i1, false);

		if (playerinstance != null)
		{
			playerinstance.flagChunkForUpdate(p_151250_1_ & 15, p_151250_2_, p_151250_3_ & 15);
		}
	}

	public void addPlayer(EntityPlayerMP par1EntityPlayerMP)
	{
		par1EntityPlayerMP.getChunkMgr().addTo(this);
		players.add(par1EntityPlayerMP);
	}

	public void filterChunkLoadQueue(EntityPlayerMP par1EntityPlayerMP)
	{
		ArrayList arraylist = new ArrayList(par1EntityPlayerMP.loadedChunks);
		int i = 0;
		int j = this.playerViewRadius;
		int k = (int)par1EntityPlayerMP.posX >> 4;
		int l = (int)par1EntityPlayerMP.posZ >> 4;
		int i1 = 0;
		int j1 = 0;
		ChunkCoordIntPair chunkcoordintpair = this.getOrCreateChunkWatcher(k, l, true).chunkLocation;
		par1EntityPlayerMP.loadedChunks.clear();

		if (arraylist.contains(chunkcoordintpair))
		{
			par1EntityPlayerMP.loadedChunks.add(chunkcoordintpair);
		}

		int k1;

		for (k1 = 1; k1 <= j * 2; ++k1)
		{
			for (int l1 = 0; l1 < 2; ++l1)
			{
				int[] aint = this.xzDirectionsConst[i++ % 4];

				for (int i2 = 0; i2 < k1; ++i2)
				{
					i1 += aint[0];
					j1 += aint[1];
					chunkcoordintpair = this.getOrCreateChunkWatcher(k + i1, l + j1, true).chunkLocation;

					if (arraylist.contains(chunkcoordintpair))
					{
						par1EntityPlayerMP.loadedChunks.add(chunkcoordintpair);
					}
				}
			}
		}

		i %= 4;

		for (k1 = 0; k1 < j * 2; ++k1)
		{
			i1 += this.xzDirectionsConst[i][0];
			j1 += this.xzDirectionsConst[i][1];
			chunkcoordintpair = this.getOrCreateChunkWatcher(k + i1, l + j1, true).chunkLocation;

			if (arraylist.contains(chunkcoordintpair))
			{
				par1EntityPlayerMP.loadedChunks.add(chunkcoordintpair);
			}
		}
	}

	public void removePlayer(EntityPlayerMP par1EntityPlayerMP)
	{
		par1EntityPlayerMP.getChunkMgr().removeFrom(this);
		this.players.remove(par1EntityPlayerMP);
	}

	private boolean overlaps(int par1, int par2, int par3, int par4, int par5)
	{
		int j1 = par1 - par3;
		int k1 = par2 - par4;
		return j1 >= -par5 && j1 <= par5 ? k1 >= -par5 && k1 <= par5 : false;
	}

	public void updatePlayerPertinentChunks(EntityPlayerMP par1EntityPlayerMP)
	{
		par1EntityPlayerMP.getChunkMgr().updatePlayerPertinentChunks();
	}

	public boolean isPlayerWatchingChunk(EntityPlayerMP par1EntityPlayerMP, int par2, int par3)
	{
		PlayerManager.PlayerInstance playerinstance = this.getOrCreateChunkWatcher(par2, par3, false);
		return playerinstance == null ? false : playerinstance.playersWatchingChunk.contains(par1EntityPlayerMP) && !par1EntityPlayerMP.loadedChunks.contains(playerinstance.chunkLocation);
	}

	public static int getFurthestViewableBlock(int par0)
	{
		return par0 * 16 - 16;
	}

	public class PlayerInstance
	{
		private final List playersWatchingChunk = new ArrayList();
		private final ChunkCoordIntPair chunkLocation;
		private short[] locationOfBlockChange = new short[64];
		private int numberOfTilesToUpdate;
		private int flagsYAreasToUpdate;
		private long previousWorldTime;
		private static final String __OBFID = "CL_00001435";

		public PlayerInstance(int par2, int par3)
		{
			this.chunkLocation = new ChunkCoordIntPair(par2, par3);
//			getWorldServer().theChunkProviderServer.loadAsync(par2, par3, this.loadedRunnable);
		}

		public void addPlayer(EntityPlayerMP par1EntityPlayerMP)
		{
			if (this.playersWatchingChunk.contains(par1EntityPlayerMP))
			{
				throw new IllegalStateException("Failed to add player. " + par1EntityPlayerMP + " already is in chunk " + this.chunkLocation.chunkXPos + ", " + this.chunkLocation.chunkZPos);
			}
			else
			{
				if (this.playersWatchingChunk.isEmpty())
				{
					this.previousWorldTime = PlayerManager.this.theWorldServer.getTotalWorldTime();
				}

				this.playersWatchingChunk.add(par1EntityPlayerMP);
			}
		}

		public void removePlayer(EntityPlayerMP par1EntityPlayerMP)
		{
			if (this.playersWatchingChunk.contains(par1EntityPlayerMP))
			{
				Chunk chunk = PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos);

//				if (chunk.func_150802_k())
				{
					par1EntityPlayerMP.playerNetServerHandler.sendPacket(S21PacketChunkData.makeForUnload(chunk));
				}

				this.playersWatchingChunk.remove(par1EntityPlayerMP);
//				par1EntityPlayerMP.loadedChunks.remove(this.chunkLocation);

				MinecraftForge.EVENT_BUS.post(new ChunkWatchEvent.UnWatch(chunkLocation, par1EntityPlayerMP));

				if (this.playersWatchingChunk.isEmpty())
				{
					long i = (long)this.chunkLocation.chunkXPos + 2147483647L | (long)this.chunkLocation.chunkZPos + 2147483647L << 32;
					this.increaseInhabitedTime(chunk);
					PlayerManager.this.playerInstances.remove(i);
					PlayerManager.this.playerInstanceList.remove(this);

					if (this.numberOfTilesToUpdate > 0)
					{
						PlayerManager.this.chunkWatcherWithPlayers.remove(this);
					}

					PlayerManager.this.getWorldServer().theChunkProviderServer.unloadChunksIfNotNearSpawn(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos);
				}
			}
		}

		public void processChunk()
		{
			this.increaseInhabitedTime(PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos));
		}

		private void increaseInhabitedTime(Chunk par1Chunk)
		{
			par1Chunk.inhabitedTime += PlayerManager.this.theWorldServer.getTotalWorldTime() - this.previousWorldTime;
			this.previousWorldTime = PlayerManager.this.theWorldServer.getTotalWorldTime();
		}

		public void flagChunkForUpdate(int p_151253_1_, int p_151253_2_, int p_151253_3_)
		{
			if (this.numberOfTilesToUpdate == 0)
			{
				PlayerManager.this.chunkWatcherWithPlayers.add(this);
			}

			this.flagsYAreasToUpdate |= 1 << (p_151253_2_ >> 4);

			//if (this.numberOfTilesToUpdate < 64) //Forge; Cache everything, so always run
			{
				short short1 = (short)(p_151253_1_ << 12 | p_151253_3_ << 8 | p_151253_2_);

				for (int l = 0; l < this.numberOfTilesToUpdate; ++l)
				{
					if (this.locationOfBlockChange[l] == short1)
					{
						return;
					}
				}

				if (numberOfTilesToUpdate == locationOfBlockChange.length)
				{
					locationOfBlockChange = Arrays.copyOf(locationOfBlockChange, locationOfBlockChange.length << 1);
				}
				this.locationOfBlockChange[this.numberOfTilesToUpdate++] = short1;
			}
		}

		public void sendToAllPlayersWatchingChunk(Packet p_151251_1_)
		{
			for (int i = 0; i < this.playersWatchingChunk.size(); ++i)
			{
				EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playersWatchingChunk.get(i);

				if (!entityplayermp.loadedChunks.contains(this.chunkLocation))
				{
					entityplayermp.playerNetServerHandler.sendPacket(p_151251_1_);
				}
			}
		}

		public void sendChunkUpdate()
		{
			if (this.numberOfTilesToUpdate != 0)
			{
				int i;
				int j;
				int k;

				if (this.numberOfTilesToUpdate == 1)
				{
					i = this.chunkLocation.chunkXPos * 16 + (this.locationOfBlockChange[0] >> 12 & 15);
					j = this.locationOfBlockChange[0] & 255;
					k = this.chunkLocation.chunkZPos * 16 + (this.locationOfBlockChange[0] >> 8 & 15);
					this.sendToAllPlayersWatchingChunk(new S23PacketBlockChange(i, j, k, PlayerManager.this.theWorldServer));

					if (PlayerManager.this.theWorldServer.getBlock(i, j, k).hasTileEntity(PlayerManager.this.theWorldServer.getBlockMetadata(i, j, k)))
					{
						this.sendTileToAllPlayersWatchingChunk(PlayerManager.this.theWorldServer.getTileEntity(i, j, k));
					}
				}
				else
				{
					int l;

					if (this.numberOfTilesToUpdate == ForgeModContainer.clumpingThreshold)
					{
						i = this.chunkLocation.chunkXPos * 16;
						j = this.chunkLocation.chunkZPos * 16;
						this.sendToAllPlayersWatchingChunk(new S21PacketChunkData(PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos), false, this.flagsYAreasToUpdate));

						// Forge: Grabs ALL tile entities is costly on a modded server, only send needed ones
						for (k = 0; false && k < 16; ++k)
						{
							if ((this.flagsYAreasToUpdate & 1 << k) != 0)
							{
								l = k << 4;
								List list = PlayerManager.this.theWorldServer.func_147486_a(i, l, j, i + 16, l + 16, j + 16);

								for (int i1 = 0; i1 < list.size(); ++i1)
								{
									this.sendTileToAllPlayersWatchingChunk((TileEntity)list.get(i1));
								}
							}
						}
					}
					else
					{
						this.sendToAllPlayersWatchingChunk(new S22PacketMultiBlockChange(this.numberOfTilesToUpdate, this.locationOfBlockChange, PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos)));
					}
					
					{ //Forge: Send only the tile entities that are updated, Adding this brace lets us keep the indent and the patch small
						WorldServer world = PlayerManager.this.theWorldServer;
						for (i = 0; i < this.numberOfTilesToUpdate; ++i)
						{
							j = this.chunkLocation.chunkXPos * 16 + (this.locationOfBlockChange[i] >> 12 & 15);
							k = this.locationOfBlockChange[i] & 255;
							l = this.chunkLocation.chunkZPos * 16 + (this.locationOfBlockChange[i] >> 8 & 15);

							if (world.getBlock(j, k, l).hasTileEntity(world.getBlockMetadata(j, k, l)))
							{
								this.sendTileToAllPlayersWatchingChunk(PlayerManager.this.theWorldServer.getTileEntity(j, k, l));
							}
						}
					}
				}

				this.numberOfTilesToUpdate = 0;
				this.flagsYAreasToUpdate = 0;
			}
		}

		private void sendTileToAllPlayersWatchingChunk(TileEntity p_151252_1_)
		{
			if (p_151252_1_ != null)
			{
				Packet packet = p_151252_1_.getDescriptionPacket();

				if (packet != null)
				{
					this.sendToAllPlayersWatchingChunk(packet);
				}
			}
		}
	}
}