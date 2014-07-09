package org.ultramine.commands.basic;

import java.util.Map;

import net.minecraft.server.MinecraftServer;
import static net.minecraft.util.EnumChatFormatting.*;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.Teleporter;
import org.ultramine.server.util.WarpLocation;

public class BasicCommands
{
	@Command(
			name = "home",
			group = "player",
			aliases = {"дом", "хата"},
			permissions = {"command.home", "command.home.multi"},
			syntax = {"", "<%name>"}
	)
	public static void home(CommandContext ctx)
	{
		WarpLocation home;
		if(ctx.contains("name"))
		{
			ctx.checkSenderPermission("command.home.multi", "command.home.multi.fail");
			home = ctx.getSenderAsPlayer().getData().core().getHome(ctx.get("name").asString());
		}
		else
		{
			home = ctx.getSenderAsPlayer().getData().core().getHome("home");
		}
		ctx.check(home != null, "command.home.fail.notset");
		Teleporter.tpLater(ctx.getSenderAsPlayer(), home);
	}
	
	@Command(
			name = "sethome",
			group = "player",
			aliases = {"здесьдом"},
			permissions = {"command.home", "command.home.multi"},
			syntax = {"", "<%name>"}
	)
	public static void sethome(CommandContext ctx)
	{
		if(ctx.contains("name"))
		{
			ctx.checkSenderPermission("command.home.multi", "command.sethome.multi.fail");
			ctx.getSenderAsPlayer().getData().core().setHome(ctx.get("name").asString(), WarpLocation.getFromPlayer(ctx.getSenderAsPlayer()));
		}
		else
		{
			ctx.getSenderAsPlayer().getData().core().setHome("home", WarpLocation.getFromPlayer(ctx.getSenderAsPlayer()));
		}
		ctx.sendMessage("command.sethome.success");
	}
	
	@Command(
			name = "removehome",
			group = "player",
			aliases = {"rmhome"},
			permissions = {"command.home.multi"},
			syntax = {"<%name>"}
	)
	public static void removehome(CommandContext ctx)
	{
		String name = ctx.get("name").asString();
		WarpLocation home = ctx.getSenderAsPlayer().getData().core().getHome(name);
		ctx.check(home != null, "command.removehome.fail");
		ctx.getSenderAsPlayer().getData().core().getHomes().remove(name);
		ctx.sendMessage("command.removehome.success");
	}
	
	@Command(
			name = "homelist",
			group = "player",
			permissions = {"command.home.multi"}
	)
	public static void homelist(CommandContext ctx)
	{
		ctx.getSenderAsPlayer();
		ctx.sendMessage("command.homelist.head");
		for(Map.Entry<String, WarpLocation> ent : ctx.getSenderAsPlayer().getData().core().getHomes().entrySet())
			ctx.sendMessage(GOLD, "    - %s [%s](%s, %s, %s)", ent.getKey(), ent.getValue().dimension, (int)ent.getValue().x, (int)ent.getValue().y, (int)ent.getValue().z);
	}
	
	@Command(
			name = "warp",
			group = "player",
			permissions = {"command.warp"},
			syntax = {"<%name>"}
	)
	public static void warp(CommandContext ctx)
	{
		WarpLocation warp = MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarp(ctx.get("name").asString());
		ctx.check(warp != null, "command.warp.fail");
		Teleporter.tpLater(ctx.getSenderAsPlayer(), warp);
	}
	
	@Command(
			name = "setwarp",
			group = "player",
			permissions = {"command.setwarp"},
			syntax = {"<%name>", "<%name> <%radius>"}
	)
	public static void setwarp(CommandContext ctx)
	{
		ctx.check(MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarp(ctx.get("name").asString()) == null, "command.setwarp.fail");
		WarpLocation warp = WarpLocation.getFromPlayer(ctx.getSenderAsPlayer());
		if(ctx.contains("radius"))
			warp.randomRadius = ctx.get("radius").asDouble();
		MinecraftServer.getServer().getConfigurationManager().getDataLoader().setWarp(ctx.get("name").asString(), warp);
		ctx.sendMessage("command.setwarp.success");
	}
	
	@Command(
			name = "resetwarp",
			group = "player",
			permissions = {"command.resetwarp"},
			syntax = {"<%name>", "<%name> <%radius>"}
	)
	public static void resetwarp(CommandContext ctx)
	{
		WarpLocation warp = WarpLocation.getFromPlayer(ctx.getSenderAsPlayer());
		if(ctx.contains("radius"))
			warp.randomRadius = ctx.get("radius").asDouble();
		MinecraftServer.getServer().getConfigurationManager().getDataLoader().setWarp(ctx.get("name").asString(), warp);
		ctx.sendMessage("command.resetwarp.success");
	}
	
	@Command(
			name = "removewarp",
			group = "player",
			aliases = {"rmwarp"},
			permissions = {"command.removewarp"},
			syntax = {"<%name>"}
	)
	public static void removewarp(CommandContext ctx)
	{
		String name = ctx.get("name").asString();
		ctx.check(MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarp(name) != null, "command.removewarp.fail");
		MinecraftServer.getServer().getConfigurationManager().getDataLoader().setWarp(name, null);
		ctx.sendMessage("command.removewarp.success");
	}
	
	@Command(
			name = "warplist",
			group = "player",
			permissions = {"command.warplist"}
	)
	public static void warplist(CommandContext ctx)
	{
		ctx.getSenderAsPlayer();
		ctx.sendMessage("command.warplist.head");
		for(Map.Entry<String, WarpLocation> ent : MinecraftServer.getServer().getConfigurationManager().getDataLoader().getWarps().entrySet())
			ctx.sendMessage(GOLD, "    - %s [%s](%s, %s, %s)", ent.getKey(), ent.getValue().dimension, (int)ent.getValue().x, (int)ent.getValue().y, (int)ent.getValue().z);
	}
	
	@Command(
			name = "fastwarp",
			group = "admin",
			permissions = {"command.fastwarp"},
			syntax = {"[add remove] <%name>"}
	)
	public static void fastwarp(CommandContext ctx)
	{
		String name = ctx.get("name").asString();
		ctx.check(!name.equals("spawn"), "command.fastwarp.fail.spawn");
		if(ctx.getAction().equals("add"))
		{
			MinecraftServer.getServer().getConfigurationManager().getDataLoader().addFastWarp(name);
		}
		else if(ctx.getAction().equals("remove"))
		{
			MinecraftServer.getServer().getConfigurationManager().getDataLoader().removeFastWarp(name);
		}
		ctx.sendMessage("command.fastwarp.success."+ctx.getAction());
	}
}
