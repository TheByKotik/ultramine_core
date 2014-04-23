package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class InventoryCraftResult implements IInventory
{
	private ItemStack[] stackResult = new ItemStack[1];
	private static final String __OBFID = "CL_00001760";

	public int getSizeInventory()
	{
		return 1;
	}

	public ItemStack getStackInSlot(int par1)
	{
		return this.stackResult[0];
	}

	public String getInventoryName()
	{
		return "Result";
	}

	public boolean hasCustomInventoryName()
	{
		return false;
	}

	public ItemStack decrStackSize(int par1, int par2)
	{
		if (this.stackResult[0] != null)
		{
			ItemStack itemstack = this.stackResult[0];
			this.stackResult[0] = null;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	public ItemStack getStackInSlotOnClosing(int par1)
	{
		if (this.stackResult[0] != null)
		{
			ItemStack itemstack = this.stackResult[0];
			this.stackResult[0] = null;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
	{
		this.stackResult[0] = par2ItemStack;
	}

	public int getInventoryStackLimit()
	{
		return 64;
	}

	public void markDirty() {}

	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
	{
		return true;
	}

	public void openInventory() {}

	public void closeInventory() {}

	public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack)
	{
		return true;
	}
}