package net.minecraft.entity.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public abstract class EntityMinecartContainer extends EntityMinecart implements IInventory
{
	private ItemStack[] minecartContainerItems = new ItemStack[36];
	private boolean dropContentsWhenDead = true;
	private static final String __OBFID = "CL_00001674";

	public EntityMinecartContainer(World par1World)
	{
		super(par1World);
	}

	public EntityMinecartContainer(World par1World, double par2, double par4, double par6)
	{
		super(par1World, par2, par4, par6);
	}

	public void killMinecart(DamageSource par1DamageSource)
	{
		super.killMinecart(par1DamageSource);

		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			ItemStack itemstack = this.getStackInSlot(i);

			if (itemstack != null)
			{
				float f = this.rand.nextFloat() * 0.8F + 0.1F;
				float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
				float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

				while (itemstack.stackSize > 0)
				{
					int j = this.rand.nextInt(21) + 10;

					if (j > itemstack.stackSize)
					{
						j = itemstack.stackSize;
					}

					itemstack.stackSize -= j;
					EntityItem entityitem = new EntityItem(this.worldObj, this.posX + (double)f, this.posY + (double)f1, this.posZ + (double)f2, new ItemStack(itemstack.getItem(), j, itemstack.getItemDamage()));
					float f3 = 0.05F;
					entityitem.motionX = (double)((float)this.rand.nextGaussian() * f3);
					entityitem.motionY = (double)((float)this.rand.nextGaussian() * f3 + 0.2F);
					entityitem.motionZ = (double)((float)this.rand.nextGaussian() * f3);
					this.worldObj.spawnEntityInWorld(entityitem);
				}
			}
		}
	}

	public ItemStack getStackInSlot(int par1)
	{
		return this.minecartContainerItems[par1];
	}

	public ItemStack decrStackSize(int par1, int par2)
	{
		if (this.minecartContainerItems[par1] != null)
		{
			ItemStack itemstack;

			if (this.minecartContainerItems[par1].stackSize <= par2)
			{
				itemstack = this.minecartContainerItems[par1];
				this.minecartContainerItems[par1] = null;
				return itemstack;
			}
			else
			{
				itemstack = this.minecartContainerItems[par1].splitStack(par2);

				if (this.minecartContainerItems[par1].stackSize == 0)
				{
					this.minecartContainerItems[par1] = null;
				}

				return itemstack;
			}
		}
		else
		{
			return null;
		}
	}

	public ItemStack getStackInSlotOnClosing(int par1)
	{
		if (this.minecartContainerItems[par1] != null)
		{
			ItemStack itemstack = this.minecartContainerItems[par1];
			this.minecartContainerItems[par1] = null;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
	{
		this.minecartContainerItems[par1] = par2ItemStack;

		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
		{
			par2ItemStack.stackSize = this.getInventoryStackLimit();
		}
	}

	public void markDirty() {}

	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
	{
		return this.isDead ? false : par1EntityPlayer.getDistanceSqToEntity(this) <= 64.0D;
	}

	public void openInventory() {}

	public void closeInventory() {}

	public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack)
	{
		return true;
	}

	public String getInventoryName()
	{
		return this.hasCustomInventoryName() ? this.func_95999_t() : "container.minecart";
	}

	public int getInventoryStackLimit()
	{
		return 64;
	}

	public void travelToDimension(int par1)
	{
		this.dropContentsWhenDead = false;
		super.travelToDimension(par1);
	}

	public void setDead()
	{
		if (this.dropContentsWhenDead)
		{
			for (int i = 0; i < this.getSizeInventory(); ++i)
			{
				ItemStack itemstack = this.getStackInSlot(i);

				if (itemstack != null)
				{
					float f = this.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

					while (itemstack.stackSize > 0)
					{
						int j = this.rand.nextInt(21) + 10;

						if (j > itemstack.stackSize)
						{
							j = itemstack.stackSize;
						}

						itemstack.stackSize -= j;
						EntityItem entityitem = new EntityItem(this.worldObj, this.posX + (double)f, this.posY + (double)f1, this.posZ + (double)f2, new ItemStack(itemstack.getItem(), j, itemstack.getItemDamage()));

						if (itemstack.hasTagCompound())
						{
							entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
						}

						float f3 = 0.05F;
						entityitem.motionX = (double)((float)this.rand.nextGaussian() * f3);
						entityitem.motionY = (double)((float)this.rand.nextGaussian() * f3 + 0.2F);
						entityitem.motionZ = (double)((float)this.rand.nextGaussian() * f3);
						this.worldObj.spawnEntityInWorld(entityitem);
					}
				}
			}
		}

		super.setDead();
	}

	protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeEntityToNBT(par1NBTTagCompound);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.minecartContainerItems.length; ++i)
		{
			if (this.minecartContainerItems[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte)i);
				this.minecartContainerItems[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		par1NBTTagCompound.setTag("Items", nbttaglist);
	}

	protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readEntityFromNBT(par1NBTTagCompound);
		NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items", 10);
		this.minecartContainerItems = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;

			if (j >= 0 && j < this.minecartContainerItems.length)
			{
				this.minecartContainerItems[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
	}

	public boolean interactFirst(EntityPlayer par1EntityPlayer)
	{
		if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.minecart.MinecartInteractEvent(this, par1EntityPlayer))) return true;
		if (!this.worldObj.isRemote)
		{
			par1EntityPlayer.displayGUIChest(this);
		}

		return true;
	}

	protected void applyDrag()
	{
		int i = 15 - Container.calcRedstoneFromInventory(this);
		float f = 0.98F + (float)i * 0.001F;
		this.motionX *= (double)f;
		this.motionY *= 0.0D;
		this.motionZ *= (double)f;
	}
}