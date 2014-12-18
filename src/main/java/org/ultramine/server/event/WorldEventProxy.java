package org.ultramine.server.event;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class WorldEventProxy
{
	public void pushState(WorldUpdateObjectType state)
	{
	}

	public void popState()
	{
	}

	public void startEntity(Entity entity)
	{
	}

	public void startTileEntity(TileEntity tile)
	{
	}

	public void startBlock(Block block, int x, int y, int z)
	{
	}

	public void startNeighbor(int x, int y, int z)
	{
	}

	public void endNeighbor()
	{
	}

	public void startInteract(ItemStack stack, Block block, int x, int y, int z)
	{
	}

	public void endInteract()
	{
	}

	public String getObjectOwner()
	{
		return null;
	}

	public boolean canChangeBlock(int x, int y, int z, Block block, int meta, int flags)
	{
		return true;
	}
}
