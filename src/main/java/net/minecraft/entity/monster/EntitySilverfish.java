package net.minecraft.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class EntitySilverfish extends EntityMob
{
	private int allySummonCooldown;
	private static final String __OBFID = "CL_00001696";

	public EntitySilverfish(World par1World)
	{
		super(par1World);
		this.setSize(0.3F, 0.7F);
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.6000000238418579D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
	}

	protected boolean canTriggerWalking()
	{
		return false;
	}

	protected Entity findPlayerToAttack()
	{
		double d0 = 8.0D;
		return this.worldObj.getClosestVulnerablePlayerToEntity(this, d0);
	}

	protected String getLivingSound()
	{
		return "mob.silverfish.say";
	}

	protected String getHurtSound()
	{
		return "mob.silverfish.hit";
	}

	protected String getDeathSound()
	{
		return "mob.silverfish.kill";
	}

	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
	{
		if (this.isEntityInvulnerable())
		{
			return false;
		}
		else
		{
			if (this.allySummonCooldown <= 0 && (par1DamageSource instanceof EntityDamageSource || par1DamageSource == DamageSource.magic))
			{
				this.allySummonCooldown = 20;
			}

			return super.attackEntityFrom(par1DamageSource, par2);
		}
	}

	protected void attackEntity(Entity par1Entity, float par2)
	{
		if (this.attackTime <= 0 && par2 < 1.2F && par1Entity.boundingBox.maxY > this.boundingBox.minY && par1Entity.boundingBox.minY < this.boundingBox.maxY)
		{
			this.attackTime = 20;
			this.attackEntityAsMob(par1Entity);
		}
	}

	protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
	{
		this.playSound("mob.silverfish.step", 0.15F, 1.0F);
	}

	protected Item getDropItem()
	{
		return Item.getItemById(0);
	}

	public void onUpdate()
	{
		this.renderYawOffset = this.rotationYaw;
		super.onUpdate();
	}

	protected void updateEntityActionState()
	{
		super.updateEntityActionState();

		if (!this.worldObj.isRemote)
		{
			int i;
			int j;
			int k;
			int i1;

			if (this.allySummonCooldown > 0)
			{
				--this.allySummonCooldown;

				if (this.allySummonCooldown == 0)
				{
					i = MathHelper.floor_double(this.posX);
					j = MathHelper.floor_double(this.posY);
					k = MathHelper.floor_double(this.posZ);
					boolean flag = false;

					for (int l = 0; !flag && l <= 5 && l >= -5; l = l <= 0 ? 1 - l : 0 - l)
					{
						for (i1 = 0; !flag && i1 <= 10 && i1 >= -10; i1 = i1 <= 0 ? 1 - i1 : 0 - i1)
						{
							for (int j1 = 0; !flag && j1 <= 10 && j1 >= -10; j1 = j1 <= 0 ? 1 - j1 : 0 - j1)
							{
								if (this.worldObj.getBlock(i + i1, j + l, k + j1) == Blocks.monster_egg)
								{
									if (!this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))
									{
										int k1 = this.worldObj.getBlockMetadata(i + i1, j + l, k + j1);
										ImmutablePair immutablepair = BlockSilverfish.func_150197_b(k1);
										this.worldObj.setBlock(i + i1, j + l, k + j1, (Block)immutablepair.getLeft(), ((Integer)immutablepair.getRight()).intValue(), 3);
									}
									else
									{
										this.worldObj.func_147480_a(i + i1, j + l, k + j1, false);
									}

									Blocks.monster_egg.onBlockDestroyedByPlayer(this.worldObj, i + i1, j + l, k + j1, 0);

									if (this.rand.nextBoolean())
									{
										flag = true;
										break;
									}
								}
							}
						}
					}
				}
			}

			if (this.entityToAttack == null && !this.hasPath())
			{
				i = MathHelper.floor_double(this.posX);
				j = MathHelper.floor_double(this.posY + 0.5D);
				k = MathHelper.floor_double(this.posZ);
				int l1 = this.rand.nextInt(6);
				Block block = this.worldObj.getBlock(i + Facing.offsetsXForSide[l1], j + Facing.offsetsYForSide[l1], k + Facing.offsetsZForSide[l1]);
				i1 = this.worldObj.getBlockMetadata(i + Facing.offsetsXForSide[l1], j + Facing.offsetsYForSide[l1], k + Facing.offsetsZForSide[l1]);

				if (BlockSilverfish.func_150196_a(block))
				{
					this.worldObj.setBlock(i + Facing.offsetsXForSide[l1], j + Facing.offsetsYForSide[l1], k + Facing.offsetsZForSide[l1], Blocks.monster_egg, BlockSilverfish.func_150195_a(block, i1), 3);
					this.spawnExplosionParticle();
					this.setDead();
				}
				else
				{
					this.updateWanderPath();
				}
			}
			else if (this.entityToAttack != null && !this.hasPath())
			{
				this.entityToAttack = null;
			}
		}
	}

	public float getBlockPathWeight(int par1, int par2, int par3)
	{
		return this.worldObj.getBlock(par1, par2 - 1, par3) == Blocks.stone ? 10.0F : super.getBlockPathWeight(par1, par2, par3);
	}

	protected boolean isValidLightLevel()
	{
		return true;
	}

	public boolean getCanSpawnHere()
	{
		if (super.getCanSpawnHere())
		{
			EntityPlayer entityplayer = this.worldObj.getClosestPlayerToEntity(this, 5.0D);
			return entityplayer == null;
		}
		else
		{
			return false;
		}
	}

	public EnumCreatureAttribute getCreatureAttribute()
	{
		return EnumCreatureAttribute.ARTHROPOD;
	}
}