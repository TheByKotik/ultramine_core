package org.ultramine.commands.basic;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.Teleporter;
import org.ultramine.server.util.BasicTypeParser;

public class VanillaCommands
{
	@Command(
			name = "tp",
			group = "vanilla",
			aliases = {"tppos", "tpposp", "tpto"},
			permissions = {"command.vanilla.tp"},
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
			Teleporter.tpNow(target, dst);
			context.sendMessage(EnumChatFormatting.GOLD, "command.tp.success.player", target.getCommandSenderName(), dst.getCommandSenderName());
		}
		else if(context.contains("x") && context.contains("y") && context.contains("z"))
		{
			WorldServer world = context.contains("world") ? context.get("world").asWorld() : target.getServerForPlayer();
			double x = context.get("x").asCoordinate(target.posX);
			double y = context.get("y").asCoordinate(target.posY);
			double z = context.get("z").asCoordinate(target.posZ);
			Teleporter.tpNow(target, world.provider.dimensionId, x, y, z);
			context.sendMessage(EnumChatFormatting.GOLD, "command.tp.success.coordinate",
					target.getCommandSenderName(), world.getWorldInfo().getWorldName(), x, y, z);
		}
	}
	
	@Command(
			name = "difficulty",
			group = "vanilla",
			permissions = {"command.vanilla.difficulty"},
			syntax = {
					"<list peaceful easy normal hard % difficulty>",
					"<world> <list peaceful easy normal hard % difficulty>"
			}
	)
	public static void difficulty(CommandContext ctx)
	{
		WorldServer world = ctx.contains("world") ? ctx.get("world").asWorld() : ctx.getSenderAsPlayer().getServerForPlayer();
		EnumDifficulty difficulty = BasicTypeParser.parseDifficulty(ctx.get("difficulty").asString());
		if(difficulty == null) ctx.throwBadUsage();
		ctx.notifyAdmins("command.difficulty.success", world.getWorldInfo().getWorldName(),
				new ChatComponentTranslation(world.difficultySetting.getDifficultyResourceKey()),
				new ChatComponentTranslation(difficulty.getDifficultyResourceKey()));
		MinecraftServer server = MinecraftServer.getServer();
		if(server.isSinglePlayer())
			server.func_147139_a(difficulty);
		else
			world.difficultySetting = difficulty;
	}
}
