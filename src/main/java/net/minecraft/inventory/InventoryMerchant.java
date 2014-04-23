package net.minecraft.inventory;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class InventoryMerchant implements IInventory
{
	private final IMerchant theMerchant;
	private ItemStack[] theInventory = new ItemStack[3];
	private final EntityPlayer thePlayer;
	private MerchantRecipe currentRecipe;
	private int currentRecipeIndex;
	private static final String __OBFID = "CL_00001756";

	public InventoryMerchant(EntityPlayer par1EntityPlayer, IMerchant par2IMerchant)
	{
		this.thePlayer = par1EntityPlayer;
		this.theMerchant = par2IMerchant;
	}

	public int getSizeInventory()
	{
		return this.theInventory.length;
	}

	public ItemStack getStackInSlot(int par1)
	{
		return this.theInventory[par1];
	}

	public ItemStack decrStackSize(int par1, int par2)
	{
		if (this.theInventory[par1] != null)
		{
			ItemStack itemstack;

			if (par1 == 2)
			{
				itemstack = this.theInventory[par1];
				this.theInventory[par1] = null;
				return itemstack;
			}
			else if (this.theInventory[par1].stackSize <= par2)
			{
				itemstack = this.theInventory[par1];
				this.theInventory[par1] = null;

				if (this.inventoryResetNeededOnSlotChange(par1))
				{
					this.resetRecipeAndSlots();
				}

				return itemstack;
			}
			else
			{
				itemstack = this.theInventory[par1].splitStack(par2);

				if (this.theInventory[par1].stackSize == 0)
				{
					this.theInventory[par1] = null;
				}

				if (this.inventoryResetNeededOnSlotChange(par1))
				{
					this.resetRecipeAndSlots();
				}

				return itemstack;
			}
		}
		else
		{
			return null;
		}
	}

	private boolean inventoryResetNeededOnSlotChange(int par1)
	{
		return par1 == 0 || par1 == 1;
	}

	public ItemStack getStackInSlotOnClosing(int par1)
	{
		if (this.theInventory[par1] != null)
		{
			ItemStack itemstack = this.theInventory[par1];
			this.theInventory[par1] = null;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
	{
		this.theInventory[par1] = par2ItemStack;

		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
		{
			par2ItemStack.stackSize = this.getInventoryStackLimit();
		}

		if (this.inventoryResetNeededOnSlotChange(par1))
		{
			this.resetRecipeAndSlots();
		}
	}

	public String getInventoryName()
	{
		return "mob.villager";
	}

	public boolean hasCustomInventoryName()
	{
		return false;
	}

	public int getInventoryStackLimit()
	{
		return 64;
	}

	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
	{
		return this.theMerchant.getCustomer() == par1EntityPlayer;
	}

	public void openInventory() {}

	public void closeInventory() {}

	public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack)
	{
		return true;
	}

	public void markDirty()
	{
		this.resetRecipeAndSlots();
	}

	public void resetRecipeAndSlots()
	{
		this.currentRecipe = null;
		ItemStack itemstack = this.theInventory[0];
		ItemStack itemstack1 = this.theInventory[1];

		if (itemstack == null)
		{
			itemstack = itemstack1;
			itemstack1 = null;
		}

		if (itemstack == null)
		{
			this.setInventorySlotContents(2, (ItemStack)null);
		}
		else
		{
			MerchantRecipeList merchantrecipelist = this.theMerchant.getRecipes(this.thePlayer);

			if (merchantrecipelist != null)
			{
				MerchantRecipe merchantrecipe = merchantrecipelist.canRecipeBeUsed(itemstack, itemstack1, this.currentRecipeIndex);

				if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled())
				{
					this.currentRecipe = merchantrecipe;
					this.setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
				}
				else if (itemstack1 != null)
				{
					merchantrecipe = merchantrecipelist.canRecipeBeUsed(itemstack1, itemstack, this.currentRecipeIndex);

					if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled())
					{
						this.currentRecipe = merchantrecipe;
						this.setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
					}
					else
					{
						this.setInventorySlotContents(2, (ItemStack)null);
					}
				}
				else
				{
					this.setInventorySlotContents(2, (ItemStack)null);
				}
			}
		}

		this.theMerchant.func_110297_a_(this.getStackInSlot(2));
	}

	public MerchantRecipe getCurrentRecipe()
	{
		return this.currentRecipe;
	}

	public void setCurrentRecipeIndex(int par1)
	{
		this.currentRecipeIndex = par1;
		this.resetRecipeAndSlots();
	}
}