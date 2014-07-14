package org.ultramine.server;

import java.util.Iterator;
import java.util.LinkedList;

import org.ultramine.server.util.WarpLocation;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class Teleporter
{
	private static final boolean isServer = FMLCommonHandler.instance().getSide().isServer();
	private static final LinkedList<Teleporter> teleporters = new LinkedList<Teleporter>();
	
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
		doTeleportation(target, dst);
	}
	
	public static void tpLater(EntityPlayerMP target, WarpLocation dst)
	{
		long timeto = target.getData().core().getNextTeleportationTime() - System.currentTimeMillis();
		if(timeto > 0 && !target.hasPermission("admin.abilities.skipteleportcooldown"))
		{
			target.addChatMessage(new ChatComponentTranslation("teleporter.fail.cooldownd", timeto/1000).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			return;
		}
		
		if(!isServer || target.hasPermission("admin.abilities.skipteleportdelay"))
		{
			tpNow(target, dst);
		}
		else
		{
			Teleporter tel = target.getData().core().getTeleporter();
			if(tel != null)
			{
				if(tel.dst.equals(dst))
					return;
				else
					tel.cancel();
			}
			
			tel = new Teleporter(target, dst);
			teleporters.add(tel);
			target.getData().core().setTeleporter(tel);
		}
	}
	
	private static void doTeleportation(EntityPlayerMP target, WarpLocation dst)
	{
		dst = dst.randomize();
		doTeleportation(target, dst.dimension, dst.x, dst.y, dst.z, dst.yaw, dst.pitch);
	}
	
	private static void doTeleportation(EntityPlayerMP player, int dimension, double x, double y, double z, float yaw, float pitch)
	{
		player.getData().core().setLastLocation(WarpLocation.getFromPlayer(player));
		
		if(player.dimension != dimension)
		{
			player.setPositionAndRotation(x, y, z, yaw, pitch);
			player.transferToDimension(dimension);
		}
		else
		{
			player.playerNetServerHandler.setPlayerLocation(x, y, z, yaw, pitch);
		}
		
		if(isServer)
		{
			player.getData().core().setNextTeleportationTime(System.currentTimeMillis() + ConfigurationHandler.getServerConfig().settings.teleportation.cooldown*1000);
			player.getData().core().setTeleporter(null);
		}
	}
	
	static void tick()
	{
		for(Iterator<Teleporter> it = teleporters.iterator();it.hasNext();)
		{
			if(it.next().update())
				it.remove();
		}
	}
	
	private final EntityPlayerMP target;
	private final WarpLocation dst;
	private final long timeEnd;
	
	private Teleporter(EntityPlayerMP target, WarpLocation dst)
	{
		this.target = target;
		this.dst = dst;
		int delay = ConfigurationHandler.getServerConfig().settings.teleportation.delay;
		timeEnd = System.currentTimeMillis() + delay*1000;
		target.addChatMessage(new ChatComponentTranslation("teleporter.delay", delay).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)));
		
	}
	
	private boolean update()
	{
		if(timeEnd - System.currentTimeMillis() <= 0)
		{
			doTeleportation(target, dst);
			return true;
		}
		
		return false;
	}
	
	public void cancel()
	{
		teleporters.remove(this);
		target.getData().core().setTeleporter(null);
		target.addChatMessage(new ChatComponentTranslation("teleporter.canceled").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		
	}
}
