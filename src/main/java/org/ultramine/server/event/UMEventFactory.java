package org.ultramine.server.event;

import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

public class UMEventFactory
{
	public static void fireInventoryClose(EntityPlayerMP player)
	{
		MinecraftForge.EVENT_BUS.post(new InventoryCloseEvent(player));
	}
	
	public static boolean fireHangingBreak(EntityHanging entity, DamageSource source)
	{
		return MinecraftForge.EVENT_BUS.post(new HangingEvent.HangingBreakEvent(entity, source));
	}
}
