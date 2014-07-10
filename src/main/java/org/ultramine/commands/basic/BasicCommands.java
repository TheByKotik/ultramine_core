package org.ultramine.commands.basic;

import java.util.Map;

import static net.minecraft.util.EnumChatFormatting.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.world.storage.WorldInfo;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.Teleporter;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.util.WarpLocation;

public class BasicCommands
{
	@Command(
			name = "home",
			group = "player",
			aliases = {"дом", "хата"},
			permissions = {"command.home", "command.home.multi", "command.home.other"},
			syntax = {
					"",
					"<%name>",
					"<player%dst> <%name>",
					"<player%target> <player%dst> <%name>"
			}
	)
	public static void home(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("name", "command.home.multi", "command.home.multi.fail");
		ctx.checkPermissionIfArg("dst", "command.home.other", "command.home.other.fail");
		EntityPlayerMP target = ctx.contains("target") ? ctx.get("target").asPlayer() : ctx.getSenderAsPlayer();
		PlayerData data = ctx.contains("dst") ? ctx.get("dst").asPlayerData() : ctx.getSenderAsPlayer().getData();
		WarpLocation home = data.core().getHome(ctx.contains("name") ? ctx.get("name").asString() : "home");
		ctx.check(home != null, "command.home.fail.notset");
		Teleporter.tpLater(target, home);
	}
	
	@Command(
			name = "sethome",
			group = "player",
			aliases = {"здесьдом"},
			permissions = {"command.home", "command.home.multi", "command.home.other"},
			syntax = {
					"",
					"<%name>",
					"<player%target> <%name>",
					"<warp%dst> <player%target> <%name>"
			}
	)
	public static void sethome(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("name", "command.home.multi", "command.home.multi.fail");
		ctx.checkPermissionIfArg("target", "command.home.other", "command.home.other.fail");
		PlayerData data = ctx.contains("target") ? ctx.get("target").asPlayerData() : ctx.getSenderAsPlayer().getData();
		WarpLocation dst = ctx.contains("dst") ? ctx.getServerData().getWarp(ctx.get("dst").asString()) : WarpLocation.getFromPlayer(ctx.getSenderAsPlayer());
		ctx.check(dst != null, "command.warp.fail");
		data.core().setHome(ctx.contains("name") ? ctx.get("name").asString() : "home", dst);
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
			permissions = {"command.warp", "command.warp.other"},
			syntax = {
					"<warp%name>",
					"<player> <warp%name>"
			}
	)
	public static void warp(CommandContext ctx)
	{
		if(ctx.contains("player"))
			ctx.checkSenderPermission("command.warp.other", "command.warp.noperm.other");
		EntityPlayerMP target = ctx.contains("player") ? ctx.get("player").asPlayer() : ctx.getSenderAsPlayer();
		WarpLocation warp = ctx.getServerData().getWarp(ctx.get("name").asString());
		ctx.check(warp != null, "command.warp.fail");
		Teleporter.tpLater(target, warp);
	}
	
	@Command(
			name = "setwarp",
			group = "player",
			permissions = {"command.setwarp"},
			syntax = {"<%name>", "<%name> <%radius>"}
	)
	public static void setwarp(CommandContext ctx)
	{
		ctx.check(ctx.getServerData().getWarp(ctx.get("name").asString()) == null, "command.setwarp.fail");
		WarpLocation warp = WarpLocation.getFromPlayer(ctx.getSenderAsPlayer());
		if(ctx.contains("radius"))
			warp.randomRadius = ctx.get("radius").asDouble();
		ctx.getServerData().setWarp(ctx.get("name").asString(), warp);
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
		ctx.getServerData().setWarp(ctx.get("name").asString(), warp);
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
		ctx.check(!name.equals("spawn"), "command.removewarp.fail.spawn");
		ctx.check(ctx.getServerData().removeWarp(name), "command.removewarp.fail.nowarp");
		ctx.getServerData().removeFastWarp(name);
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
		for(Map.Entry<String, WarpLocation> ent : ctx.getServerData().getWarps().entrySet())
			ctx.sendMessage(GOLD, "    - %s [%s](%s, %s, %s)", ent.getKey(), ent.getValue().dimension, (int)ent.getValue().x, (int)ent.getValue().y, (int)ent.getValue().z);
	}
	
	@Command(
			name = "back",
			group = "player",
			permissions = {"command.back"}
	)
	public static void back(CommandContext ctx)
	{
		WarpLocation loc = ctx.getSenderAsPlayer().getData().core().getLastLocation();
		ctx.check(loc != null, "command.back.fail");
		Teleporter.tpLater(ctx.getSenderAsPlayer(), loc);
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
			ctx.check(ctx.getServerData().getWarp(name) != null, "command.fastwarp.fail.nowarp");
			ctx.check(!ctx.getServerData().getFastWarps().contains(name), "command.fastwarp.fail.already");
			ctx.getServerData().addFastWarp(name);
		}
		else if(ctx.getAction().equals("remove"))
		{
			ctx.check(ctx.getServerData().removeFastWarp(name), "command.fastwarp.fail.nofastwarp");
		}
		ctx.sendMessage("command.fastwarp.success."+ctx.getAction());
	}
	
	@Command(
			name = "setspawn",
			group = "admin",
			permissions = {"command.setspawn"},
			syntax = {"", "<%radius>"}
	)
	public static void setspawn(CommandContext ctx)
	{
		WarpLocation warp = WarpLocation.getFromPlayer(ctx.getSenderAsPlayer());
		if(ctx.contains("radius"))
			warp.randomRadius = ctx.get("radius").asDouble();
		ctx.getServerData().setWarp("spawn", warp);
		ctx.sendMessage("command.setspawn.success");
	}
	
	@Command(
			name = "setlocalspawn",
			group = "admin",
			permissions = {"command.setlocalspawn"}
	)
	public static void setlocalspawn(CommandContext ctx)
	{
		EntityPlayerMP player = ctx.getSenderAsPlayer();
		WorldInfo wi = player.worldObj.getWorldInfo();
		wi.setSpawnPosition(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
		ctx.sendMessage("command.setlocalspawn.success");
	}
	
	@Command(
			name = "localspawn",
			group = "admin",
			permissions = {"command.localspawn"}
	)
	public static void localspawn(CommandContext ctx)
	{
		EntityPlayerMP player = ctx.getSenderAsPlayer();
		WorldInfo wi = player.worldObj.getWorldInfo();
		Teleporter.tpNow(player, wi.getSpawnX(), wi.getSpawnY(), wi.getSpawnZ());
	}
}
