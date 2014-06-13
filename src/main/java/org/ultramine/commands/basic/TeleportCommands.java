package org.ultramine.commands.basic;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.Teleporter;

public class TeleportCommands
{
	@Command(
			name = "tp",
			group = "basic",
			aliases = {"tppos", "tpposp", "tpto"},
			permissions = {"basic.tp"},
			syntax = {
					"<player%dst>",
					"<player%target> <player%dst>",
					"<%x> <%y> <%z>",
					"<world> <%x> <%y> <%z>",
					"<player%target> <%x> <%y> <%z>",
					"<player%target> <world> <%x> <%y> <%z>"
			}
	)
	public static void tp(CommandContext context)
	{
		EntityPlayerMP target = context.contains("target") ? context.get("target").asPlayer() : context.getSenderAsPlayer();
		if(context.contains("dst"))
		{
			EntityPlayerMP dst = context.get("dst").asPlayer();
			Teleporter.tpNow(target, context.get("dst").asPlayer());
			context.notifyAdmins("command.tp.success.player", target.getCommandSenderName(), dst.getCommandSenderName());
		}
		else if(context.contains("x") && context.contains("y") && context.contains("z"))
		{
			WorldServer world = context.contains("world") ? context.get("world").asWorld() : target.getServerForPlayer();
			double x = context.get("x").asCoordinate(target.posX);
			double y = context.get("y").asCoordinate(target.posY);
			double z = context.get("z").asCoordinate(target.posZ);
			Teleporter.tpNow(target, world.provider.dimensionId, x, y, z);
			context.notifyAdmins("command.tp.success.coordinate",
					target.getCommandSenderName(), world.getWorldInfo().getWorldName(), x, y, z);
		}
	}
}
