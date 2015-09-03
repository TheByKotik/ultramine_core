package org.ultramine.server;

import java.util.Random;

import org.ultramine.server.WorldsConfig.WorldConfig.LoadBalancer.Limits;
import org.ultramine.server.WorldsConfig.WorldConfig.LoadBalancer.Limits.PerChunkEntityLimits;
import org.ultramine.server.chunk.ChunkHash;

import cpw.mods.fml.common.FMLCommonHandler;
import gnu.trove.map.TIntByteMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class ServerLoadBalancer
{
	private static final boolean isClient = FMLCommonHandler.instance().getSide().isClient();
	private static final PerChunkEntityLimits clientLimits = new PerChunkEntityLimits();
	private static final PerChunkEntityLimits infinityLimits = new PerChunkEntityLimits();
	private static final Random rng = new Random();
	private final World world;
	private final TIntByteMap activeChunkSet;
	
	static
	{
		clientLimits.lowerLimit = 32;
		clientLimits.higherLimit = Integer.MAX_VALUE;
		clientLimits.updateRadius = 7;
		infinityLimits.lowerLimit = Integer.MAX_VALUE;
		infinityLimits.higherLimit = Integer.MAX_VALUE;
		infinityLimits.updateRadius = 99;
		infinityLimits.updateByChunkLoader = true;
	}
	
	public ServerLoadBalancer(World world)
	{
		this.world = world;
		this.activeChunkSet = world.getActiveChunkSet();
	}
	
	public boolean canUpdateEntity(Entity ent)
	{
		if(ent.isEntityPlayerMP() || isClient && world.isRemote && ent.isEntityPlayer())
			return true;
		int cx = MathHelper.floor_double(ent.posX) >> 4;
		int cz = MathHelper.floor_double(ent.posZ) >> 4;

		Chunk chunk = world.getChunkIfExists(cx, cz);
		if(chunk == null)
		{
			if(!ent.forceSpawn)
			{
				world.getEventProxy().startEntity(ent);
				ent.setDead();
			}
			return false;
		}
		
		int count = chunk.getEntityCountOfSameType(ent);
		PerChunkEntityLimits limits = getLimits(ent);
		if(count > limits.higherLimit)
		{
			world.getEventProxy().startEntity(ent);
			ent.setDead();
			return false;
		}

		int prior = activeChunkSet.get(ChunkHash.chunkToKey(cx, cz));
		if(prior == Byte.MAX_VALUE)
		{
			ent.despawnInactive();
			return false;
		}

		if(!ent.addedToChunk)
			return true;

		int lowerLimit = limits.lowerLimit;

		if(prior == WorldConstants.CL_CHUNK_PRIOR)
		{
			if(!limits.updateByChunkLoader)
				lowerLimit = 1;
		}
		else if(prior > limits.updateRadius)
		{
			lowerLimit = 1;
		}

		if(count > lowerLimit)
		{
			float rate = (float)lowerLimit / (float)count;
			if(rng.nextFloat() < rate)
				return true;
			return false;
		}

		return true;
	}
	
	private PerChunkEntityLimits getLimits(Entity e)
	{
		if(isClient)
			return clientLimits;
		Limits limits = ((WorldServer)e.worldObj).getConfig().loadBalancer.limits;
		
		if(e.isEntityMonster())			return limits.monsters;
		else if(e.isEntityAnimal())		return limits.animals;
		else if(e.isEntityAmbient())	return limits.ambient;
		else if(e.isEntityWater())		return limits.water;
		else if(e.isEntityItem())		return limits.items;
		else if(e.isEntityXPOrb())		return limits.xpOrbs;
		
		return infinityLimits;
	}
}
