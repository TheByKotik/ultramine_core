package org.ultramine.server;

import org.ultramine.server.util.WarpLocation;

import net.minecraft.entity.player.EntityPlayerMP;

public class Teleporter
{
	public static void tpNow(EntityPlayerMP target, EntityPlayerMP dst)
	{
		if(target == null || dst == null) return;
		doTeleportation(target, dst.worldObj.provider.dimensionId, dst.posX, dst.posY, dst.posZ, dst.rotationYaw, dst.rotationPitch);
	}
	
	public static void tpNow(EntityPlayerMP target, double x, double y, double z)
	{
		if(target == null) return;
		doTeleportation(target, target.worldObj.provider.dimensionId, x, y, z, target.rotationYaw, target.rotationPitch);
	}
	
	public static void tpNow(EntityPlayerMP target, int dimension, double x, double y, double z)
	{
		if(target == null) return;
		doTeleportation(target, dimension, x, y, z, target.rotationYaw, target.rotationPitch);
	}
	
	public static void tpNow(EntityPlayerMP target, WarpLocation dst)
	{
		if(target == null || dst == null) return;
		doTeleportation(target, dst.dimension, dst.x, dst.y, dst.z, dst.yaw, dst.pitch);
	}
	
	private static void doTeleportation(EntityPlayerMP player, int dimension, double x, double y, double z, float yaw, float pitch)
	{
		//player.getPlayerData().lastLocation = WarpLocation.getFromPlayer(player);
		
		player.playerNetServerHandler.setPlayerLocation(x, y, z, yaw, pitch);
		
		if(player.dimension != dimension)
		{
			player.transferToDimension(dimension);
		}
		
		//player.getPlayerData().onTeleport();
		//player.getPlayerData().teleport = null;
	}
}
