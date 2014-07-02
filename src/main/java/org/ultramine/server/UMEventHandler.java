package org.ultramine.server;

import org.ultramine.server.data.player.io.PlayerDataIOExecutor;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.ServerChatEvent;

public class UMEventHandler
{
	@SubscribeEvent
	public void onServerChat(ServerChatEvent e)
	{
		String prefix = PermissionHandler.getInstance().getMeta(e.player, "prefix").replace('&', '\u00A7');
		String postfix = PermissionHandler.getInstance().getMeta(e.player, "postfix").replace('&', '\u00A7');
		
		String namecolor = PermissionHandler.getInstance().getMeta(e.player, "color");
		String msgcolor = PermissionHandler.getInstance().getMeta(e.player, "textcolor");
		
		ChatComponentStyle username = (ChatComponentStyle) e.player.func_145748_c_();
		ChatComponentStyle msg = new ChatComponentText(e.message);
		
		if(!namecolor.isEmpty())
		{
			EnumChatFormatting color = EnumChatFormatting.getByColorCode(namecolor.charAt(0));
			if(color != null)
				username.getChatStyle().setColor(color);
		}
		
		if(!msgcolor.isEmpty())
		{
			EnumChatFormatting color = EnumChatFormatting.getByColorCode(msgcolor.charAt(0));
			if(color != null)
				msg.getChatStyle().setColor(color);
		}
		else
		{
			msg.getChatStyle().setColor(EnumChatFormatting.WHITE);
		}
		
		e.component = new ChatComponentTranslation("%s%s%s\u00A77: %s", prefix, username, postfix, msg);
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			PlayerDataIOExecutor.tick();
		}
	}
}
