package org.ultramine.server.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class InventoryUtil
{
	public static boolean isStacksEquals(ItemStack is1, ItemStack is2)
	{
		return
				is1 == null && is2 == null || is1 != null && is1.isItemEqual(is2) &&
				(is1.stackTagCompound == null && is2.stackTagCompound != null ? false : is1.stackTagCompound == null || is1.stackTagCompound.equals(is2.stackTagCompound));
	}
	
	public static boolean contains(IInventory inv, ItemStack item)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack is = inv.getStackInSlot(i);
			if(isStacksEquals(is, item)) return true;
		}

		return false;
	}

	public static int first(IInventory inv, ItemStack item)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack is = inv.getStackInSlot(i);
			if(isStacksEquals(is, item)) return i;
		}

		return -1;
	}

	public static int firstEmpty(IInventory inv)
	{
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			if(inv.getStackInSlot(i) == null) return i;
		}

		return -1;
	}

	private static int firstPartial(IInventory inv, ItemStack filteredItem)
	{
		if(filteredItem == null)
		{
			return -1;
		}
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack cItem = inv.getStackInSlot(i);
			if(cItem != null && cItem.stackSize < cItem.getMaxStackSize() && isStacksEquals(cItem, filteredItem))
			{
				return i;
			}
		}
		return -1;
	}

	public static List<ItemStack> removeItem(IInventory inv, ItemStack... items)
	{
		List<ItemStack> ret = new LinkedList<ItemStack>();
		for(ItemStack item : items)
		{
			ret.addAll(removeItem(inv, item));
		}
		return ret;
	}
	
	public static List<ItemStack> removeItem(IInventory inv, ItemStack item)
	{
		List<ItemStack> leftover = null;

		// TODO: optimization

		int toDelete = item.stackSize;

		while(true)
		{
			int first = first(inv, item);

			// Drat! we don't have this type in the inventory
			if(first == -1)
			{
				item.stackSize = toDelete;
				if(leftover == null)
					leftover = new LinkedList<ItemStack>();
				leftover.add(item);
				break;
			}
			else
			{
				ItemStack itemStack = inv.getStackInSlot(first);
				int amount = itemStack.stackSize;

				if(amount <= toDelete)
				{
					toDelete -= amount;
					// clear the slot, all used up
					inv.setInventorySlotContents(first, null);
				}
				else
				{
					// split the stack and store
					itemStack.stackSize = amount - toDelete;
					inv.setInventorySlotContents(first, itemStack);
					toDelete = 0;
				}
			}

			// Bail when done
			if(toDelete <= 0)
			{
				break;
			}
		}
		
		if(leftover == null)
			leftover = Collections.emptyList();
		return leftover;
	}
	
	public static List<ItemStack> addItem(IInventory inv, ItemStack... items)
	{
		List<ItemStack> ret = new LinkedList<ItemStack>();
		for(ItemStack item : items)
		{
			ret.addAll(addItem(inv, item));
		}
		return ret;
	}

	public static List<ItemStack> addItem(IInventory inv, ItemStack item)
	{
		List<ItemStack> leftover = null;

		int maxSize = Math.min(inv.getInventoryStackLimit(), item.getMaxStackSize());

		while(true)
		{
			// Do we already have a stack of it?
			int firstPartial = firstPartial(inv, item);

			// Drat! no partial stack
			if(firstPartial == -1)
			{
				// Find a free spot!
				int firstFree = firstEmpty(inv);

				if(firstFree == -1)
				{
					// No space at all!
					if(leftover == null)
						leftover = new LinkedList<ItemStack>();
					leftover.add(item);
					break;
				}
				else
				{
					// More than a single stack!
					if(item.stackSize > maxSize)
					{
						ItemStack stack = item.splitStack(maxSize);
						inv.setInventorySlotContents(firstFree, stack);
					}
					else
					{
						// Just store it
						inv.setInventorySlotContents(firstFree, item);
						break;
					}
				}
			}
			else
			{
				// So, apparently it might only partially fit, well lets do just that
				ItemStack partialItem = inv.getStackInSlot(firstPartial);

				int amount = item.stackSize;
				int partialAmount = partialItem.stackSize;
				int maxAmount = partialItem.getMaxStackSize();

				// Check if it fully fits
				if(amount + partialAmount <= maxAmount)
				{
					partialItem.stackSize = (amount + partialAmount);
					break;
				}

				// It fits partially
				partialItem.stackSize = maxAmount;
				item.stackSize = (amount + partialAmount - maxAmount);
			}
		}
		
		if(leftover == null)
			leftover = Collections.emptyList();
		return leftover;
	}

	public static void dropItem(World world, double x, double y, double z, ItemStack item)
	{
		if(item == null)
			return;
		double var7 = world.rand.nextDouble() * 0.8D + 0.1D;
		double var9 = world.rand.nextDouble() * 0.8D + 0.1D;
		double var11 = world.rand.nextDouble() * 0.8D + 0.1D;
		EntityItem var14 = new EntityItem(world, x + var7, y + var9, z + var11, item);
		double var15 = 0.05D;
		var14.motionX = world.rand.nextGaussian() * var15;
		var14.motionY = world.rand.nextGaussian() * var15 + 0.2D;
		var14.motionZ = world.rand.nextGaussian() * var15;
		world.spawnEntityInWorld(var14);
	}

	public static void dropItemFixed(World world, double x, double y, double z, ItemStack item)
	{
		if(item == null)
			return;
		double var7 = world.rand.nextDouble() * 0.8D + 0.1D;
		double var9 = world.rand.nextDouble() * 0.8D + 0.1D;
		double var11 = world.rand.nextDouble() * 0.8D + 0.1D;
		EntityItem var14 = new EntityItem(world, x + var7, y + var9, z + var11, item);
		world.spawnEntityInWorld(var14);
	}
}
