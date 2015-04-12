package org.ultramine.server.tools;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.PermissionHandler;
import org.ultramine.server.UltramineServerConfig.ToolsConf.WarpProtectionEntry;
import org.ultramine.server.util.WarpLocation;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class WarpProtection
{
	private boolean isInside(WarpLocation warp, int dim, int x, int z, int radius)
	{
		return warp.dimension == dim && Math.abs(warp.x - x) < radius && Math.abs(warp.z - z) < radius;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBreakEvent(BlockEvent.BreakEvent e)
	{
		if(!e.getPlayer().isEntityPlayerMP() || ((EntityPlayerMP)e.getPlayer()).playerNetServerHandler == null)
			return;
		if(!PermissionHandler.getInstance().has(e.getPlayer(), "ability.admin.breakprivate"))
		{
			for(WarpProtectionEntry warpConf : ConfigurationHandler.getServerConfig().tools.warpProtection)
			{
				if(warpConf.changeBlocks)
				{
					WarpLocation warp = MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarp(warpConf.name);
					if(warp != null && isInside(warp, e.getPlayer().dimension, e.x, e.z, warpConf.radius))
						e.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEvent(PlayerInteractEvent e)
	{
		if(!PermissionHandler.getInstance().has(e.entityPlayer, "ability.admin.breakprivate"))
		{
			for(WarpProtectionEntry warpConf : ConfigurationHandler.getServerConfig().tools.warpProtection)
			{
				if(warpConf.useItems || warpConf.userBlocks)
				{
					WarpLocation warp = MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarp(warpConf.name);
					if(warp != null)
					{
						if(e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
						{
							if(warpConf.useItems && isInside(warp, e.entityPlayer.dimension, MathHelper.floor_double(e.entity.posX),
									MathHelper.floor_double(e.entity.posZ), warpConf.radius))
								e.useItem = Event.Result.DENY;
						}
						else if(isInside(warp, e.entityPlayer.dimension, e.x, e.z, warpConf.radius))
						{
							if(warpConf.useItems)
								e.useItem = Event.Result.DENY;
							if(warpConf.userBlocks)
								e.useBlock = Event.Result.DENY;
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingAttackEvent(LivingAttackEvent e)
	{
		Entity attacker = e.source.getEntity();
		if(attacker != null)
		{
			boolean isPvP = attacker.isEntityPlayerMP() && e.entityLiving.isEntityPlayerMP();
			for(WarpProtectionEntry warpConf : ConfigurationHandler.getServerConfig().tools.warpProtection)
			{
				if(isPvP && warpConf.pvp || !isPvP && e.entityLiving.isEntityPlayerMP() && warpConf.mobDamage)
				{
					WarpLocation warp = MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarp(warpConf.name);
					if(warp != null && isInside(warp, e.entityLiving.dimension, MathHelper.floor_double(e.entityLiving.posX),
							MathHelper.floor_double(e.entityLiving.posZ), warpConf.radius))
						e.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMobSpawn(LivingSpawnEvent.CheckSpawn e)
	{
		for(WarpProtectionEntry warpConf : ConfigurationHandler.getServerConfig().tools.warpProtection)
		{
			if(warpConf.mobSpawn)
			{
				WarpLocation warp = MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarp(warpConf.name);
				if(warp != null && isInside(warp, e.world.provider.dimensionId, MathHelper.floor_double(e.x), MathHelper.floor_double(e.z), warpConf.radius))
					e.setResult(Result.DENY);
			}
		}
	}
}
