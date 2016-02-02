package org.ultramine.server.chunk;

import java.util.ArrayList;
import java.util.List;

import org.ultramine.server.util.UnsafeUtil;

import cpw.mods.fml.common.FMLCommonHandler;
import sun.misc.Unsafe;

public class OffHeapChunkStorage
{
	private static final boolean IS_CLIENT = FMLCommonHandler.instance().getSide().isClient();
	static final Unsafe U = UnsafeUtil.getUnsafe();
	private static final int SLOT_SIZE = 4096*3;
	private static final int SLOTS_PER_CHUNK = IS_CLIENT ? 8*512 : 8*2048;
	private static final int SLOT_LIMIT = ((int)Math.round(Integer.parseInt(System.getProperty("org.ultramine.server.offheapchunk.memlimit", "6"))*5.3333333333d))*(8*2048);
	
	private int counter = -1;
	private final List<MemSlot> freeSlots = new ArrayList<MemSlot>();
	
	private MemChunk chunk;
	private int slots;
	
	private static final OffHeapChunkStorage INSTANCE = new OffHeapChunkStorage();
	public static OffHeapChunkStorage instance()
	{
		return INSTANCE;
	}
	
	private OffHeapChunkStorage()
	{
		slots = SLOTS_PER_CHUNK;
		chunk = new MemChunk();
	}
	
	public synchronized MemSlot allocateSlot()
	{
		if(freeSlots.size() != 0)
		{
			return freeSlots.remove(freeSlots.size()-1);
		}
		else
		{
			if(++counter == slots)
			{
				if(counter >= SLOT_LIMIT)
					throw new OutOfMemoryError("Off-heap chunk storage");
				slots += SLOTS_PER_CHUNK;
				chunk = new MemChunk();
			}
			
			return new MemSlot(chunk.nextSlot());
		}
	}
	
	public long getTotalMemory()
	{
		return (long)slots*SLOT_SIZE;
	}
	
	public long getUsedMemory()
	{
		return (counter+1-freeSlots.size())*SLOT_SIZE;
	}
	
	private static class MemChunk
	{
		private int counter;
		private long pointer;
		
		public MemChunk()
		{
			pointer = U.allocateMemory(SLOT_SIZE * SLOTS_PER_CHUNK);
		}
		
		public long nextSlot()
		{
			return pointer + (counter++)*SLOT_SIZE;
		}
	}
	
	//LSB
	//MSB#META
	//BLOCK#SKY
	public static class MemSlot
	{
		private static final long BYTE_ARRAY_OFFSET = U.arrayBaseOffset(byte[].class);
		private static final int OFFSET_LSB		= 0;
		private static final int OFFSET_MSB		= 4096;
		private static final int OFFSET_META	= 4096+2048;
		private static final int OFFSET_BLOCK	= 4096+2048+2048;
		private static final int OFFSET_SKY		= 4096+2048+2048+2048;
		
		private final long pointer;
		
		private MemSlot(long pointer)
		{
			this.pointer = pointer;
		}
		
		private void setByte(int ind, byte data)
		{
			U.putByte(pointer + ind, data);
		}
		
		private byte getByte(int ind)
		{
			return U.getByte(pointer + ind);
		}
		
		//raw set
		
		public void setLSB(byte[] arr)
		{
			if(arr == null || arr.length != 4096) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer, 4096);
		}
		
		public void setLSB(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 4096) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer, 4096);
		}
		
		public void setMSB(byte[] arr)
		{
			if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer + OFFSET_MSB, 2048);
		}
		
		public void setMSB(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_MSB, 2048);
		}
		
		public void setBlockMetadata(byte[] arr)
		{
			if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer + OFFSET_META, 2048);
		}
		
		public void setBlockMetadata(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_META, 2048);
		}
		
		public void setBlocklight(byte[] arr)
		{
			if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer + OFFSET_BLOCK, 2048);
		}
		
		public void setBlocklight(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_BLOCK, 2048);
		}
		
		public void setSkylight(byte[] arr)
		{
			if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET, null, pointer + OFFSET_SKY, 2048);
		}
		
		public void setSkylight(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
			U.copyMemory(arr, BYTE_ARRAY_OFFSET + start, null, pointer + OFFSET_SKY, 2048);
		}
		
		//raw copy
		
		public void copyLSB(byte[] arr)
		{
			if(arr == null || arr.length != 4096) throw new IllegalArgumentException();
			U.copyMemory(null, pointer, arr, BYTE_ARRAY_OFFSET, 4096);
		}
		
		public void copyLSB(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 4096) throw new IllegalArgumentException();
			U.copyMemory(null, pointer, arr, BYTE_ARRAY_OFFSET + start, 4096);
		}
		
		public void copyMSB(byte[] arr)
		{
			if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
			U.copyMemory(null, pointer + OFFSET_MSB, arr, BYTE_ARRAY_OFFSET, 2048);
		}
		
		public void copyMSB(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
			U.copyMemory(null, pointer + OFFSET_MSB, arr, BYTE_ARRAY_OFFSET + start, 2048);
		}
		
		public void copyBlockMetadata(byte[] arr)
		{
			if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
			U.copyMemory(null, pointer + OFFSET_META, arr, BYTE_ARRAY_OFFSET, 2048);
		}
		
		public void copyBlockMetadata(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
			U.copyMemory(null, pointer + OFFSET_META, arr, BYTE_ARRAY_OFFSET + start, 2048);
		}
		
		public void copyBlocklight(byte[] arr)
		{
			if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
			U.copyMemory(null, pointer + OFFSET_BLOCK, arr, BYTE_ARRAY_OFFSET, 2048);
		}
		
		public void copyBlocklight(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
			U.copyMemory(null, pointer + OFFSET_BLOCK, arr, BYTE_ARRAY_OFFSET + start, 2048);
		}
		
		public void copySkylight(byte[] arr)
		{
			if(arr == null || arr.length != 2048) throw new IllegalArgumentException();
			U.copyMemory(null, pointer + OFFSET_SKY, arr, BYTE_ARRAY_OFFSET, 2048);
		}
		
		public void copySkylight(byte[] arr, int start)
		{
			if(arr == null || arr.length - start < 2048) throw new IllegalArgumentException();
			U.copyMemory(null, pointer + OFFSET_SKY, arr, BYTE_ARRAY_OFFSET + start, 2048);
		}
		
		//array copy
		
		public byte[] copyLSB()
		{
			byte[] arr = new byte[4096];
			copyLSB(arr);
			return arr;
		}
		
		public byte[] copyMSB()
		{
			byte[] arr = new byte[2048];
			copyMSB(arr);
			return arr;
		}
		
		public byte[] copyBlockMetadata()
		{
			byte[] arr = new byte[2048];
			copyBlockMetadata(arr);
			return arr;
		}
		
		public byte[] copyBlocklight()
		{
			byte[] arr = new byte[2048];
			copyBlocklight(arr);
			return arr;
		}
		
		public byte[] copySkylight()
		{
			byte[] arr = new byte[2048];
			copySkylight(arr);
			return arr;
		}
		
		//clear
		
		public void clearMSB()
		{
			U.setMemory(pointer + OFFSET_MSB, 2048, (byte)0);
		}
		
		public void clearSkylight()
		{
			U.setMemory(pointer + OFFSET_SKY, 2048, (byte)0);
		}
		
		public void clearAll()
		{
			U.setMemory(pointer, SLOT_SIZE, (byte)0);
		}
		
		//
		
		private int get4bits(int start, int x, int y, int z)
		{
			int l = y << 8 | z << 4 | x;
			int i1 = l >> 1;
			int j1 = l & 1;
			return j1 == 0 ? getByte(start+i1) & 15 : getByte(start+i1) >> 4 & 15;
		}
		
		private void set4bits(int start, int x, int y, int z, int data)
		{
			int i1 = y << 8 | z << 4 | x;
			int j1 = i1 >> 1;
			int k1 = i1 & 1;

			int off = start+j1;
			if (k1 == 0)
			{
				setByte(off, (byte)(getByte(off) & 240 | data & 15));
			}
			else
			{
				setByte(off, (byte)(getByte(off) & 15 | (data & 15) << 4));
			}
		}
		
		public int getBlockID(int x, int y, int z)
		{
			return (getByte(y << 8 | z << 4 | x) & 255) | (get4bits(OFFSET_MSB, x, y, z) << 8);
		}
		
		public void setBlockID(int x, int y, int z, int id)
		{
			setByte(y << 8 | z << 4 | x, (byte)(id & 0xFF));
			set4bits(OFFSET_MSB, x, y, z, (id & 3840) >> 8);
		}
		
		public int getMeta(int x, int y, int z)
		{
			return get4bits(OFFSET_META, x, y, z);
		}
		
		public void setMeta(int x, int y, int z, int meta)
		{
			set4bits(OFFSET_META, x, y, z, meta);
		}
		
		public int getBlocklight(int x, int y, int z)
		{
			return get4bits(OFFSET_BLOCK, x, y, z);
		}
		
		public void setBlocklight(int x, int y, int z, int val)
		{
			set4bits(OFFSET_BLOCK, x, y, z, val);
		}
		
		public int getSkylight(int x, int y, int z)
		{
			return get4bits(OFFSET_SKY, x, y, z);
		}
		
		public void setSkylight(int x, int y, int z, int val)
		{
			set4bits(OFFSET_SKY, x, y, z, val);
		}
		
		public void free()
		{
			OffHeapChunkStorage inst = OffHeapChunkStorage.instance();
			synchronized(inst)
			{
				inst.freeSlots.add(this);
			}
		}
	}
}
