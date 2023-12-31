package org.ultramine.server.chunk;

import java.util.Collection;

import net.minecraft.world.chunk.Chunk;
import net.openhft.koloboke.collect.map.IntObjCursor;
import net.openhft.koloboke.collect.map.IntObjMap;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;

import org.ultramine.server.chunk.ChunkHash;

public class ChunkMap
{
	private static final int FLAT_LOOKUP_SIZE = 512;
	private static final int FLAT_MAP_SIZE = FLAT_LOOKUP_SIZE*2;
	
	private final Chunk[] flatMap = new Chunk[FLAT_MAP_SIZE*FLAT_MAP_SIZE];
	
	private final IntObjMap<Chunk> map = HashIntObjMaps.newMutableMap();
	
	public void put(int x, int z, Chunk chunk)
	{
		put(x, z, ChunkHash.chunkToKey(x, z), chunk);
	}
	
	public void put(int hash, Chunk chunk)
	{
		put(ChunkHash.keyToX(hash), ChunkHash.keyToZ(hash), hash, chunk);
	}
	
	public Chunk get(int x, int z)
	{
		if(isFlatMapable(x, z))
		{
			return getFlat(x, z);
		}
		
		return map.get(ChunkHash.chunkToKey(x, z));
	}
	
	public Chunk get(int hash)
	{
		int x = ChunkHash.keyToX(hash);
		int z = ChunkHash.keyToZ(hash);
		
		if(isFlatMapable(x, z))
		{
			return getFlat(x, z);
		}
		
		return map.get(hash);
	}
	
	public Chunk remove(int x, int z)
	{
		if(isFlatMapable(x, z))
		{
			removeFlat(x, z);
		}
		
		return map.remove(ChunkHash.chunkToKey(x, z));
	}
	
	public Chunk remove(int hash)
	{
		int x = ChunkHash.keyToX(hash);
		int z = ChunkHash.keyToZ(hash);
		
		if(isFlatMapable(x, z))
		{
			removeFlat(x, z);
		}
		
		return map.remove(hash);
	}
	
	public boolean contains(int x, int z)
	{
		if(isFlatMapable(x, z))
		{
			return containsFlat(x, z);
		}
		
		return map.containsKey(ChunkHash.chunkToKey(x, z));
	}
	
	public boolean contains(int hash)
	{
		return map.containsKey(hash);
	}
	
	public IntObjCursor<Chunk> iterator()
	{
		return map.cursor();
	}
	
	public Collection<Chunk> valueCollection()
	{
		return map.values();
	}
	
	public int size()
	{
		return map.size();
	}
	
	public void clear()
	{
		for(Chunk chunk : valueCollection())
			if(isFlatMapable(chunk.xPosition, chunk.zPosition))
				removeFlat(chunk.xPosition, chunk.zPosition);
		
		map.clear();
	}
	
	
	
	private void put(int x, int z, int hash, Chunk chunk)
	{
		if(isFlatMapable(x, z))
		{
			putFlat(x, z, chunk);
		}
		
		map.put(hash, chunk);
	}
	
	private boolean isFlatMapable(int x, int z)
	{
		return Math.abs(x) < FLAT_LOOKUP_SIZE && Math.abs(z) < FLAT_LOOKUP_SIZE;
	}
	
	private int getFlatIndex(int x, int z)
	{
		return (x + FLAT_LOOKUP_SIZE)*FLAT_MAP_SIZE + (z + FLAT_LOOKUP_SIZE);
	}
	
	private void putFlat(int x, int z, Chunk chunk)
	{
		flatMap[getFlatIndex(x, z)] = chunk;
	}
	
	private Chunk getFlat(int x, int z)
	{
		return flatMap[getFlatIndex(x, z)];
	}
	
	private void removeFlat(int x, int z)
	{
		flatMap[getFlatIndex(x, z)] = null;
	}
	
	private boolean containsFlat(int x, int z)
	{
		return flatMap[getFlatIndex(x, z)] != null;
	}
}
