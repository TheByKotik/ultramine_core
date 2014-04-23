package net.minecraft.world.chunk.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.NibbleArray;

public class ExtendedBlockStorage
{
	private int yBase;
	private int blockRefCount;
	private int tickRefCount;
	private byte[] blockLSBArray;
	private NibbleArray blockMSBArray;
	private NibbleArray blockMetadataArray;
	private NibbleArray blocklightArray;
	private NibbleArray skylightArray;
	private static final String __OBFID = "CL_00000375";

	public ExtendedBlockStorage(int par1, boolean par2)
	{
		this.yBase = par1;
		this.blockLSBArray = new byte[4096];
		this.blockMetadataArray = new NibbleArray(this.blockLSBArray.length, 4);
		this.blocklightArray = new NibbleArray(this.blockLSBArray.length, 4);

		if (par2)
		{
			this.skylightArray = new NibbleArray(this.blockLSBArray.length, 4);
		}
	}

	public Block getBlockByExtId(int p_150819_1_, int p_150819_2_, int p_150819_3_)
	{
		int l = this.blockLSBArray[p_150819_2_ << 8 | p_150819_3_ << 4 | p_150819_1_] & 255;

		if (this.blockMSBArray != null)
		{
			l |= this.blockMSBArray.get(p_150819_1_, p_150819_2_, p_150819_3_) << 8;
		}

		return Block.getBlockById(l);
	}

	public void func_150818_a(int p_150818_1_, int p_150818_2_, int p_150818_3_, Block p_150818_4_)
	{
		int l = this.blockLSBArray[p_150818_2_ << 8 | p_150818_3_ << 4 | p_150818_1_] & 255;

		if (this.blockMSBArray != null)
		{
			l |= this.blockMSBArray.get(p_150818_1_, p_150818_2_, p_150818_3_) << 8;
		}

		Block block1 = Block.getBlockById(l);

		if (block1 != Blocks.air)
		{
			--this.blockRefCount;

			if (block1.getTickRandomly())
			{
				--this.tickRefCount;
			}
		}

		if (p_150818_4_ != Blocks.air)
		{
			++this.blockRefCount;

			if (p_150818_4_.getTickRandomly())
			{
				++this.tickRefCount;
			}
		}

		int i1 = Block.getIdFromBlock(p_150818_4_);
		this.blockLSBArray[p_150818_2_ << 8 | p_150818_3_ << 4 | p_150818_1_] = (byte)(i1 & 255);

		if (i1 > 255)
		{
			if (this.blockMSBArray == null)
			{
				this.blockMSBArray = new NibbleArray(this.blockLSBArray.length, 4);
			}

			this.blockMSBArray.set(p_150818_1_, p_150818_2_, p_150818_3_, (i1 & 3840) >> 8);
		}
		else if (this.blockMSBArray != null)
		{
			this.blockMSBArray.set(p_150818_1_, p_150818_2_, p_150818_3_, 0);
		}
	}

	public int getExtBlockMetadata(int par1, int par2, int par3)
	{
		return this.blockMetadataArray.get(par1, par2, par3);
	}

	public void setExtBlockMetadata(int par1, int par2, int par3, int par4)
	{
		this.blockMetadataArray.set(par1, par2, par3, par4);
	}

	public boolean isEmpty()
	{
		return this.blockRefCount == 0;
	}

	public boolean getNeedsRandomTick()
	{
		return this.tickRefCount > 0;
	}

	public int getYLocation()
	{
		return this.yBase;
	}

	public void setExtSkylightValue(int par1, int par2, int par3, int par4)
	{
		this.skylightArray.set(par1, par2, par3, par4);
	}

	public int getExtSkylightValue(int par1, int par2, int par3)
	{
		return this.skylightArray.get(par1, par2, par3);
	}

	public void setExtBlocklightValue(int par1, int par2, int par3, int par4)
	{
		this.blocklightArray.set(par1, par2, par3, par4);
	}

	public int getExtBlocklightValue(int par1, int par2, int par3)
	{
		return this.blocklightArray.get(par1, par2, par3);
	}

	public void removeInvalidBlocks()
	{
		this.blockRefCount = 0;
		this.tickRefCount = 0;

		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				for (int k = 0; k < 16; ++k)
				{
					Block block = this.getBlockByExtId(i, j, k);

					if (block != Blocks.air)
					{
						++this.blockRefCount;

						if (block.getTickRandomly())
						{
							++this.tickRefCount;
						}
					}
				}
			}
		}
	}

	public byte[] getBlockLSBArray()
	{
		return this.blockLSBArray;
	}

	@SideOnly(Side.CLIENT)
	public void clearMSBArray()
	{
		this.blockMSBArray = null;
	}

	public NibbleArray getBlockMSBArray()
	{
		return this.blockMSBArray;
	}

	public NibbleArray getMetadataArray()
	{
		return this.blockMetadataArray;
	}

	public NibbleArray getBlocklightArray()
	{
		return this.blocklightArray;
	}

	public NibbleArray getSkylightArray()
	{
		return this.skylightArray;
	}

	public void setBlockLSBArray(byte[] par1ArrayOfByte)
	{
		this.blockLSBArray = par1ArrayOfByte;
	}

	public void setBlockMSBArray(NibbleArray par1NibbleArray)
	{
		this.blockMSBArray = par1NibbleArray;
	}

	public void setBlockMetadataArray(NibbleArray par1NibbleArray)
	{
		this.blockMetadataArray = par1NibbleArray;
	}

	public void setBlocklightArray(NibbleArray par1NibbleArray)
	{
		this.blocklightArray = par1NibbleArray;
	}

	public void setSkylightArray(NibbleArray par1NibbleArray)
	{
		this.skylightArray = par1NibbleArray;
	}

	@SideOnly(Side.CLIENT)
	public NibbleArray createBlockMSBArray()
	{
		this.blockMSBArray = new NibbleArray(this.blockLSBArray.length, 4);
		return this.blockMSBArray;
	}
}