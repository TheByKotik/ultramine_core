package org.ultramine.server;

import org.ultramine.server.data.player.io.PlayerDataIOExecutor;
import org.ultramine.server.util.BasicTypeParser;
import org.ultramine.server.util.WarpLocation;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

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
	public void onServerTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			PlayerDataIOExecutor.tick();
			Teleporter.tick();
		}
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone e)
	{
		if(e.entityPlayer.isEntityPlayerMP())
		{
			((EntityPlayerMP)e.entityPlayer).setData(((EntityPlayerMP)e.original).getData());
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
}
