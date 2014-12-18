package org.ultramine.server;

import org.ultramine.server.UltramineServerConfig.SettingsConf.MessagesConf.AutoBroacastConf;
import org.ultramine.server.chunk.ChunkProfiler;
import org.ultramine.server.util.BasicTypeParser;
import org.ultramine.server.util.WarpLocation;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.functions.GenericIterableFactory;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.WorldServer;
import static net.minecraft.util.EnumChatFormatting.*;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

public class UMEventHandler
{
	@SubscribeEvent
	public void onServerChat(ServerChatEvent e)
	{
		String prefix = PermissionHandler.getInstance().getMeta(e.player, "prefix").replace('&', '\u00A7');
		String postfix = PermissionHandler.getInstance().getMeta(e.player, "postfix").replace('&', '\u00A7');
		
		ChatComponentStyle username = (ChatComponentStyle) e.player.func_145748_c_();
		ChatComponentStyle msg = new ChatComponentText(e.message);
		
		username.getChatStyle().setColor(BasicTypeParser.parseColor(PermissionHandler.getInstance().getMeta(e.player, "color")));
		EnumChatFormatting color = BasicTypeParser.parseColor(PermissionHandler.getInstance().getMeta(e.player, "textcolor"));
		msg.getChatStyle().setColor(color != null ? color : EnumChatFormatting.WHITE);
		
		e.component = new ChatComponentTranslation("%s%s%s\u00A77: %s", prefix, username, postfix, msg);
	}
	
	@SubscribeEvent
	public void onServerTickCommon(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			MinecraftServer server = MinecraftServer.getServer();
			
			Teleporter.tick();
			ChunkProfiler.instance().tick(server.getTickCounter());
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.SERVER)
	public void onServerTickServer(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			MinecraftServer server = MinecraftServer.getServer();
			server.getBackupManager().tick();
			
			AutoBroacastConf cfg = ConfigurationHandler.getServerConfig().settings.messages.autobroadcast;
			if(cfg.enabled && server.getTickCounter() % (cfg.intervalSeconds*20) == 0)
			{
				if(cfg.showDebugInfo)
				{
					double tps = Math.round(server.currentTPS*10)/10d;
					double downtime = server.currentWait/1000/1000d;
					double pickdowntime = server.pickWait/1000/1000d;
					int load = (int)Math.round((50-downtime)/50*100);
					int pickload = (int)Math.round((50-pickdowntime)/50*100);
					ChatComponentText loadcomp = new ChatComponentText(Integer.toString(load).concat("%"));
					ChatComponentText pickloadcomp = new ChatComponentText(Integer.toString(pickload).concat("%"));
					ChatComponentText tpscomp = new ChatComponentText(Double.toString(tps));
					loadcomp.getChatStyle().setColor(load > 100 ? RED : DARK_GREEN);
					pickloadcomp.getChatStyle().setColor(pickload >= 200 ? RED : DARK_GREEN);
					tpscomp.getChatStyle().setColor(tps < 15 ? RED : DARK_GREEN);
					
					int mobcount = 0;
					int itemcount = 0;
					
					for(WorldServer world : server.getMultiWorld().getLoadedWorlds())
					{
						for(Entity ent : GenericIterableFactory.newCastingIterable(world.loadedEntityList, Entity.class))
						{
							if(ent.isEntityLiving() && !ent.isEntityPlayer())
								mobcount++;
							else if(ent instanceof EntityItem)
								itemcount++;
						}
					}
					
					ChatComponentTranslation full = new ChatComponentTranslation("ultramine.autobroadcast.debugmsg", loadcomp, pickloadcomp, tpscomp,
							Integer.toString(mobcount), Integer.toString(itemcount));
					full.getChatStyle().setColor(YELLOW);
					
					server.getConfigurationManager().sendChatMsg(full);
				}
				
				if(cfg.messages.length != 0)
				{
					if(cfg.showAllMessages)
					{
						for(String msg : cfg.messages)
							broadcastMessage(msg);
					}
					else
					{
						broadcastMessage(cfg.messages[server.getTickCounter() % (cfg.intervalSeconds*20*cfg.messages.length) / (cfg.intervalSeconds*20)]);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.END && e.side.isServer())
		{
			EntityPlayerMP player = (EntityPlayerMP)e.player;
			int x = MathHelper.floor_double(player.posX);
			int z = MathHelper.floor_double(player.posZ);
			if(!player.getServerForPlayer().getBorder().isInsideBorder(x, z))
			{
				ChunkPosition pos = player.getServerForPlayer().getBorder().correctPosition(x, z);
				player.playerNetServerHandler.setPlayerLocation(pos.chunkPosX, player.lastTickPosY, pos.chunkPosZ, player.rotationYaw, player.rotationPitch);
			}
		}
	}
	
	private static void broadcastMessage(String msg)
	{
		ChatComponentText msgcomp = new ChatComponentText(msg);
		msgcomp.getChatStyle().setColor(DARK_GREEN);
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(msgcomp);
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone e)
	{
		if(e.entityPlayer.isEntityPlayerMP())
		{
			((EntityPlayerMP)e.entityPlayer).setData(((EntityPlayerMP)e.original).getData());
			((EntityPlayerMP)e.entityPlayer).setStatisticsFile(MinecraftServer.getServer().getConfigurationManager().func_152602_a(e.entityPlayer));
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent e)
	{
		if(e.entityLiving.isEntityPlayerMP())
		{
			EntityPlayerMP player = (EntityPlayerMP)e.entityLiving;
			Teleporter tp = player.getData().core().getTeleporter();
			if(tp != null)
				tp.cancel();
			player.getData().core().setLastLocation(WarpLocation.getFromPlayer(player));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent e)
	{
		MinecraftServer.getServer().getConfigurationManager().getDataLoader().handlePlayerDimensionChange((EntityPlayerMP)e.player, e.fromDim, e.toDim);
	}
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBreakEvent(BlockEvent.BreakEvent e)
	{
		if(!e.getPlayer().isEntityPlayerMP() || ((EntityPlayerMP)e.getPlayer()).playerNetServerHandler == null)
			return;
		if(!PermissionHandler.getInstance().has(e.getPlayer(), "ability.player.blockbreak"))
		{
			e.setCanceled(true);
			e.getPlayer().addChatMessage(new ChatComponentTranslation("ultramine.ability.blockbreak").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}
	}
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlaceEvent(BlockEvent.PlaceEvent e)
	{
		if(!e.player.isEntityPlayerMP() || ((EntityPlayerMP)e.player).playerNetServerHandler == null)
			return;
		if(!PermissionHandler.getInstance().has(e.player, "ability.player.blockplace"))
		{
			e.setCanceled(true);
			e.player.addChatMessage(new ChatComponentTranslation("ultramine.ability.blockplace").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}
	}
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEvent(PlayerInteractEvent e)
	{
		if(!PermissionHandler.getInstance().has(e.entityPlayer, "ability.player.useitem"))
		{
			e.useItem = Event.Result.DENY;
			if(e.entityPlayer.inventory.getCurrentItem() != null)
				e.entityPlayer.addChatMessage(new ChatComponentTranslation("ultramine.ability.useitem").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}
		if(!PermissionHandler.getInstance().has(e.entityPlayer, "ability.player.useblock"))
		{
			e.useBlock = Event.Result.DENY;
			e.entityPlayer.addChatMessage(new ChatComponentTranslation("ultramine.ability.useblock").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		}
		
		if(e.useItem == Event.Result.DENY && e.useBlock == Event.Result.DENY)
			e.setCanceled(true);
	}
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingAttackEvent(LivingAttackEvent e)
	{
		Entity attacker = e.source.getEntity();
		if(attacker != null && attacker.isEntityPlayerMP())
		{
			EntityPlayerMP player = (EntityPlayerMP)attacker;
			if(!PermissionHandler.getInstance().has(player, "ability.player.attack"))
			{
				e.setCanceled(true);
				player.addChatMessage(new ChatComponentTranslation("ultramine.ability.attack").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			}
		}
	}
}
