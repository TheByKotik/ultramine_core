package org.ultramine.server;

import org.ultramine.server.util.BasicTypeFormatter;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.functions.GenericIterableFactory;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;

@SideOnly(Side.SERVER)
public class Restarter
{
	private static Restarter currentRestarter;
	private ServerConfigurationManager mgr;
	private int toRestart;
	private int ticks;
	
	private Restarter(int seconds)
	{
		toRestart = seconds;
		mgr = MinecraftServer.getServer().getConfigurationManager();
	}
	
	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START)
		{
			if(++ticks % 20 == 0)
				onSecond();
		}
	}
	
	public void onSecond()
	{
		if(toRestart <= 0)
		{
			initiateRestart();
		}
		else
		{
			if(toRestart == 3600 || toRestart == 1800 || toRestart == 900 || toRestart == 600 || toRestart == 300 || toRestart == 120 || toRestart == 60 ||
					toRestart == 40 || toRestart == 20 || toRestart == 15 || toRestart < 11)
			{
				mgr.sendChatMsg(new ChatComponentText("\u00a75Рестарт сервера через " + BasicTypeFormatter.formatTime(toRestart*1000, true)));
				if(toRestart == 1)
				{
					mgr.sendChatMsg(new ChatComponentText("\u00a75Рестарт сервера!"));
					mgr.sendChatMsg(new ChatComponentText("\u00a75Вы будете автоматически отключены от сервера"));
				}
			}
		}
		
		toRestart--;
	}
	
	private void initiateRestart()
	{
		mgr.saveAllPlayerData();
		for(EntityPlayerMP player : GenericIterableFactory.newCastingIterable(mgr.playerEntityList, EntityPlayerMP.class))
			player.playerNetServerHandler.kickPlayerFromServer("\u00a75Сервер был перезапущен\n\u00a7dВы сможете войти через несколько минут");
		
		mgr.getServerInstance().initiateShutdown();
	}
	
	public static void restart(int seconds)
	{
		abort();
		currentRestarter = new Restarter(seconds);
		FMLCommonHandler.instance().bus().register(currentRestarter);
	}
	
	public static boolean abort()
	{
		if(currentRestarter != null)
		{
			FMLCommonHandler.instance().bus().unregister(currentRestarter);
			currentRestarter = null;
			return true;
		}
		
		return false;
	}
}
