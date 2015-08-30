package org.ultramine.server.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class MinecraftUtil
{
	//from ItemBoat
	public static MovingObjectPosition getMovingObjectPosition(EntityPlayer player)
	{
		float var4 = 1.0F;
        float var5 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * var4;
        float var6 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * var4;
        double var7 = player.prevPosX + (player.posX - player.prevPosX) * (double)var4;
        double var9 = player.prevPosY + (player.posY - player.prevPosY) * (double)var4 + 1.62D - (double)player.yOffset;
        double var11 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)var4;
        Vec3 var13 = Vec3.createVectorHelper(var7, var9, var11);
        float var14 = MathHelper.cos(-var6 * 0.017453292F - (float)Math.PI);
        float var15 = MathHelper.sin(-var6 * 0.017453292F - (float)Math.PI);
        float var16 = -MathHelper.cos(-var5 * 0.017453292F);
        float var17 = MathHelper.sin(-var5 * 0.017453292F);
        float var18 = var15 * var16;
        float var20 = var14 * var16;
        double var21 = 5.0D;
        Vec3 var23 = var13.addVector((double)var18 * var21, (double)var17 * var21, (double)var20 * var21);
        return player.worldObj.rayTraceBlocks(var13, var23, true);
	}
	
	public static int countXPCostForLevel(int level)
	{
		if(level < 17)
		{
			return 17*level;
		}
		else if(level < 30)
		{
			int cost = 17*level;
			for(int i = 0; i < level - 15; i++)
				cost += i*3;
			return cost + level/18;
		}
		else
		{
			int cost = 826;
			for(int i = 0; i < level - 30; i++)
				cost += 62 + i*7;
			return cost;
		}
	}
}
