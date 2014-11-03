package org.ultramine.commands.basic;

import java.util.List;
import java.util.Map;

import static net.minecraft.util.EnumChatFormatting.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.storage.WorldInfo;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.commands.IExtendedCommand;
import org.ultramine.server.Teleporter;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.util.InventoryUtil;
import org.ultramine.server.util.WarpLocation;

public class BasicCommands
{
	@Command(
			name = "help",
			group = "player",
			aliases = {"?"},
			permissions = {"command.help"},
			syntax = {
					"",
					"<page>"
			}
	)
	public static void help(CommandContext ctx)
	{
		@SuppressWarnings("unchecked")
		List<IExtendedCommand> cmds = (List<IExtendedCommand>)ctx.getServer().getCommandManager().getPossibleCommands(ctx.getSender());
		int pages = cmds.size()/10 + (cmds.size()%10 == 0 ? 0 : 1);
		int page = ctx.contains("page") ? ctx.get("page").asString().equals("all") ? -1 : ctx.get("page").asInt(1, pages) -1 : 0;
		
		int start = page == -1 ? 0 : page*10;
		int limit = page == -1 ? cmds.size() : Math.min(cmds.size(), start + 10);
		
		ctx.sendMessage("commands.help.header", page+1, pages);
		
		String group = "";
		for(int i = start; i < limit; i++)
		{
			IExtendedCommand cmd = cmds.get(i);

			if(!group.equals(cmd.getGroup()))
			{
				group = cmd.getGroup();
				ctx.sendMessage("%s:", group);
			}
			String usageS = cmd.getCommandUsage(ctx.getSender());
			ChatComponentTranslation usage = new ChatComponentTranslation(usageS != null ? usageS : '/' + cmd.getCommandName());
			ChatComponentTranslation desc = new ChatComponentTranslation(cmd.getDescription());
			usage.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + cmd.getCommandName() + " "));
			usage.getChatStyle().setColor(YELLOW);
			desc.getChatStyle().setColor(DARK_AQUA);
			ctx.sendMessage(DARK_GRAY, YELLOW, "  - %s <- %s", usage, desc);
		}
	}
	
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
			aliases = {"go", "пойти", "на"},
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
	
	@Command(
			name = "heal",
			group = "admin",
			permissions = {"command.heal", "command.heal.other"},
			syntax = {"", "<player>"}
	)
	public static void heal(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("player", "command.heal.other", "command.heal.noperm.other");
		EntityPlayerMP player = ctx.contains("player") ? ctx.get("player").asPlayer() : ctx.getSenderAsPlayer();
		player.setHealth(player.getMaxHealth());
		player.getFoodStats().addStats(20, 0.5F);
		player.addChatComponentMessage(new ChatComponentTranslation("command.heal.success").setChatStyle(new ChatStyle().setColor(GOLD)));
		if(ctx.contains("player"))
			ctx.sendMessage("command.heal.success.other", player.getCommandSenderName());
	}
	
	@Command(
			name = "dropall",
			group = "admin",
			permissions = {"command.dropall"},
			syntax = {"", "<player>"}
	)
	public static void dropall(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("player", "command.dropall.other", "command.dropall.noperm.other");
		EntityPlayerMP player = ctx.contains("player") ? ctx.get("player").asPlayer() : ctx.getSenderAsPlayer();
		player.inventory.dropAllItems();
	}
	
	@Command(
			name = "item",
			group = "admin",
			aliases = {"i"},
			permissions = {"command.item"},
			syntax = {
					"<item>",
					"<item> <int%size>",
					"<player> <item>..."
			}
	)
	public static void item(CommandContext ctx)
	{
		ItemStack is = ctx.get("item").asItemStack();
		EntityPlayerMP player = ctx.contains("player") ? ctx.get("player").asPlayer() : ctx.getSenderAsPlayer();
		if(ctx.contains("size"))
			is.stackSize = ctx.get("size").asInt();
		InventoryUtil.addItem(player.inventory, is);
	}
	
	@Command(
			name = "dupe",
			group = "admin",
			permissions = {"command.dupe"},
			syntax = {"", "<%count>"}
	)
	public static void dupe(CommandContext ctx)
	{
		ItemStack is = ctx.getSenderAsPlayer().inventory.getCurrentItem();
		ctx.check(is != null, "command.dupe.fail");
		is = is.copy();
		if(ctx.contains("count"))
			is.stackSize *= ctx.get("count").asInt();
		InventoryUtil.addItem(ctx.getSenderAsPlayer().inventory, is);
	}
	
	@Command(
			name = "gm",
			group = "admin",
			permissions = {"command.gm"},
			syntax = {""}
	)
	public static void gm(CommandContext ctx)
	{
		EntityPlayerMP player = ctx.getSenderAsPlayer();
		
		GameType type = player.theItemInWorldManager.getGameType();
		GameType newtype = GameType.SURVIVAL;
		if(type == GameType.SURVIVAL)
			newtype = GameType.CREATIVE;
		
		player.setGameType(newtype);
	}
}
