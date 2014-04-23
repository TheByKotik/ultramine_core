package net.minecraft.entity.monster;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityMagmaCube extends EntitySlime
{
	private static final String __OBFID = "CL_00001691";

	public EntityMagmaCube(World par1World)
	{
		super(par1World);
		this.isImmuneToFire = true;
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.20000000298023224D);
	}

	public boolean getCanSpawnHere()
	{
		return this.worldObj.difficultySetting != EnumDifficulty.PEACEFUL && this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty() && !this.worldObj.isAnyLiquid(this.boundingBox);
	}

	public int getTotalArmorValue()
	{
		return this.getSlimeSize() * 3;
	}

	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float par1)
	{
		return 15728880;
	}

	public float getBrightness(float par1)
	{
		return 1.0F;
	}

	protected String getSlimeParticle()
	{
		return "flame";
	}

	protected EntitySlime createInstance()
	{
		return new EntityMagmaCube(this.worldObj);
	}

	protected Item getDropItem()
	{
		return Items.magma_cream;
	}

	protected void dropFewItems(boolean par1, int par2)
	{
		Item item = this.getDropItem();

		if (item != null && this.getSlimeSize() > 1)
		{
			int j = this.rand.nextInt(4) - 2;

			if (par2 > 0)
			{
				j += this.rand.nextInt(par2 + 1);
			}

			for (int k = 0; k < j; ++k)
			{
				this.dropItem(item, 1);
			}
		}
	}

	public boolean isBurning()
	{
		return false;
	}

	protected int getJumpDelay()
	{
		return super.getJumpDelay() * 4;
	}

	protected void alterSquishAmount()
	{
		this.squishAmount *= 0.9F;
	}

	protected void jump()
	{
		this.motionY = (double)(0.42F + (float)this.getSlimeSize() * 0.1F);
		this.isAirBorne = true;
	}

	protected void fall(float par1) {}

	protected boolean canDamagePlayer()
	{
		return true;
	}

	protected int getAttackStrength()
	{
		return super.getAttackStrength() + 2;
	}

	protected String getJumpSound()
	{
		return this.getSlimeSize() > 1 ? "mob.magmacube.big" : "mob.magmacube.small";
	}

	public boolean handleLavaMovement()
	{
		return false;
	}

	protected boolean makesSoundOnLand()
	{
		return true;
	}
}