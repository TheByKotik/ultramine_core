package org.ultramine.commands.basic;

import java.util.Map;

import static net.minecraft.util.EnumChatFormatting.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.storage.WorldInfo;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.Teleporter;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.util.BasicTypeFormatter;
import org.ultramine.server.util.InventoryUtil;
import org.ultramine.server.util.WarpLocation;

public class BasicCommands
{
	@Command(
			name = "home",
			group = "basic",
			aliases = {"дом", "хата"},
			permissions = {"command.basic.home", "command.basic.home.multi", "command.basic.home.other"},
			syntax = {
					"",
					"<%name>",
					"<player%dst> <%name>",
					"<player%target> <player%dst> <%name>"
			}
	)
	public static void home(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("name", "command.basic.home.multi", "command.basic.home.multi.fail");
		ctx.checkPermissionIfArg("dst", "command.basic.home.other", "command.basic.home.other.fail");
		EntityPlayerMP target = ctx.contains("target") ? ctx.get("target").asPlayer() : ctx.getSenderAsPlayer();
		PlayerData data = ctx.contains("dst") ? ctx.get("dst").asPlayerData() : ctx.getSenderAsPlayer().getData();
		WarpLocation home = data.core().getHome(ctx.contains("name") ? ctx.get("name").asString() : "home");
		ctx.check(home != null, "command.home.fail.notset");
		if(home.dimension != target.dimension && !ConfigurationHandler.getServerConfig().settings.teleportation.interWorldHome)
			ctx.checkSenderPermission("ability.admin.ignoreInterworldHome", "command.home.fail.interworld");
		Teleporter.tpLaterOrNow(target, home, ctx.contains("target"));
	}
	
	@Command(
			name = "sethome",
			group = "basic",
			aliases = {"здесьдом"},
			permissions = {"command.basic.sethome", "command.basic.sethome.multi", "command.basic.sethome.other"},
			syntax = {
					"",
					"<%name>",
					"<player%target> <%name>",
					"<warp%dst> <player%target> <%name>"
			}
	)
	public static void sethome(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("name", "command.basic.sethome.multi", "command.home.multi.fail");
		ctx.checkPermissionIfArg("target", "command.basic.sethome.other", "command.home.other.fail");
		PlayerData data = ctx.contains("target") ? ctx.get("target").asPlayerData() : ctx.getSenderAsPlayer().getData();
		WarpLocation dst = ctx.contains("dst") ? ctx.getServerData().getWarp(ctx.get("dst").asString()) : WarpLocation.getFromPlayer(ctx.getSenderAsPlayer());
		ctx.check(dst != null, "command.warp.fail");
		data.core().setHome(ctx.contains("name") ? ctx.get("name").asString() : "home", dst);
		ctx.sendMessage("command.sethome.success");
	}
	
	@Command(
			name = "removehome",
			group = "basic",
			aliases = {"rmhome"},
			permissions = {"command.basic.removehome"},
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
			group = "basic",
			permissions = {"command.basic.homelist"},
			isUsableFromServer = false
	)
	public static void homelist(CommandContext ctx)
	{
		ctx.sendMessage("command.homelist.head");
		for(Map.Entry<String, WarpLocation> ent : ctx.getSenderAsPlayer().getData().core().getHomes().entrySet())
			ctx.sendMessage(GOLD, "    - %s [%s](%s, %s, %s)", ent.getKey(), ent.getValue().dimension, (int)ent.getValue().x, (int)ent.getValue().y, (int)ent.getValue().z);
	}
	
	@Command(
			name = "warp",
			aliases = {"go", "пойти", "на", "в"},
			group = "basic",
			permissions = {"command.basic.warp", "command.basic.warp.other"},
			syntax = {
					"<warp%name>",
					"<player%target> <warp%name>"
			}
	)
	public static void warp(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("target", "command.basic.warp.other", "command.warp.noperm.other");
		EntityPlayerMP target = ctx.contains("target") ? ctx.get("target").asPlayer() : ctx.getSenderAsPlayer();
		WarpLocation warp = ctx.getServerData().getWarp(ctx.get("name").asString());
		ctx.check(warp != null, "command.warp.fail");
		if(warp.dimension != target.dimension && !ConfigurationHandler.getServerConfig().settings.teleportation.interWorldWarp)
			ctx.checkSenderPermission("ability.admin.ignoreInterworldHome", "command.warp.fail.interworld");
		Teleporter.tpLaterOrNow(target, warp, ctx.contains("target"));
	}
	
	@Command(
			name = "setwarp",
			group = "basic",
			permissions = {"command.basic.setwarp"},
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
			group = "basic",
			permissions = {"command.basic.resetwarp"},
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
			group = "basic",
			aliases = {"rmwarp"},
			permissions = {"command.basic.removewarp"},
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
			group = "basic",
			permissions = {"command.basic.warplist"}
	)
	public static void warplist(CommandContext ctx)
	{
		ctx.sendMessage("command.warplist.head");
		for(Map.Entry<String, WarpLocation> ent : ctx.getServerData().getWarps().entrySet())
			ctx.sendMessage(GOLD, "    - %s [%s](%s, %s, %s)", ent.getKey(), ent.getValue().dimension, (int)ent.getValue().x, (int)ent.getValue().y, (int)ent.getValue().z);
	}
	
	@Command(
			name = "back",
			group = "basic",
			permissions = {"command.basic.back"}
	)
	public static void back(CommandContext ctx)
	{
		WarpLocation loc = ctx.getSenderAsPlayer().getData().core().getLastLocation();
		ctx.check(loc != null, "command.back.fail");
		Teleporter.tpLater(ctx.getSenderAsPlayer(), loc);
	}
	
	@Command(
			name = "fastwarp",
			group = "basic",
			permissions = {"command.basic.fastwarp"},
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
			name = "tphere",
			group = "basic",
			permissions = {"command.basic.tphere"},
			syntax = {"<player%target>"}
	)
	public static void tphere(CommandContext ctx)
	{
		Teleporter.tpNow(ctx.get("target").asPlayer(), ctx.getSenderAsPlayer());
		ctx.sendMessage("command.tp.success.player", ctx.get("target").asPlayer().func_145748_c_(), ctx.getSenderAsPlayer().func_145748_c_());
	}
	
	@Command(
			name = "setspawn",
			group = "basic",
			permissions = {"command.basic.setspawn"},
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
			group = "basic",
			permissions = {"command.basic.setlocalspawn"}
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
			group = "basic",
			permissions = {"command.basic.localspawn"}
	)
	public static void localspawn(CommandContext ctx)
	{
		EntityPlayerMP player = ctx.getSenderAsPlayer();
		WorldInfo wi = player.worldObj.getWorldInfo();
		Teleporter.tpNow(player, wi.getSpawnX(), wi.getSpawnY(), wi.getSpawnZ());
	}
	
	@Command(
			name = "heal",
			group = "basic",
			permissions = {"command.basic.heal", "command.basic.heal.other"},
			syntax = {"", "<player>"}
	)
	public static void heal(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("player", "command.admin.heal.other", "command.heal.noperm.other");
		EntityPlayerMP player = ctx.contains("player") ? ctx.get("player").asPlayer() : ctx.getSenderAsPlayer();
		player.setHealth(player.getMaxHealth());
		player.getFoodStats().addStats(20, 0.5F);
		player.addChatComponentMessage(new ChatComponentTranslation("command.heal.success").setChatStyle(new ChatStyle().setColor(GOLD)));
		if(ctx.contains("player"))
			ctx.sendMessage("command.heal.success.other", player.getCommandSenderName());
	}
	
	@Command(
			name = "dropall",
			group = "basic",
			permissions = {"command.basic.dropall"},
			syntax = {"", "<player>"}
	)
	public static void dropall(CommandContext ctx)
	{
		ctx.checkPermissionIfArg("player", "command.admin.dropall.other", "command.dropall.noperm.other");
		EntityPlayerMP player = ctx.contains("player") ? ctx.get("player").asPlayer() : ctx.getSenderAsPlayer();
		player.inventory.dropAllItems();
	}
	
	@Command(
			name = "item",
			group = "basic",
			aliases = {"i"},
			permissions = {"command.basic.item"},
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
			group = "basic",
			permissions = {"command.basic.dupe"},
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
			group = "basic",
			permissions = {"command.basic.gm"},
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
	
	@Command(
			name = "custmsg",
			group = "basic",
			permissions = {"command.basic.custmsg"},
			syntax = {
					"[all] <msg>...",
					"<player> <msg>..."
			}
	)
	public static void custmsg(CommandContext ctx)
	{
		String msg = ctx.get("msg").asString().replace('&', '\u00a7');
		if(ctx.getAction().equals("all"))
			ctx.broadcast(new ChatComponentText(msg));
		else
			ctx.get("player").asPlayer().addChatMessage(new ChatComponentText(msg));
	}
	
	@Command(
			name = "mute",
			group = "basic",
			permissions = {"command.basic.mute"},
			syntax = {
					"<player>",
					"<player> <time>"
			}
	)
	public static void mute(CommandContext ctx)
	{
		long time = ctx.contains("time") ? ctx.get("time").asTimeMills() : Long.MAX_VALUE;
		PlayerData data = ctx.get("player").asPlayerData();
		data.core().mute(time);
		ctx.get("player").asOfflinePlayer().sendMessage(RED, RED, "command.mute.notify");
		if(time != Long.MAX_VALUE)
			ctx.broadcast("command.mute.broadcast", data.getProfile().getName(), BasicTypeFormatter.formatTime(time, true));
		else
			ctx.broadcast("command.mute.broadcast.forever", data.getProfile().getName());
	}
	
	@Command(
			name = "commandmute",
			aliases= {"cmdmute", "hardmute"},
			group = "basic",
			permissions = {"command.basic.commandmute"},
			syntax = {
					"<player>",
					"<player> <time>"
			}
	)
	public static void commandmute(CommandContext ctx)
	{
		long time = ctx.contains("time") ? ctx.get("time").asTimeMills() : Long.MAX_VALUE;
		PlayerData data = ctx.get("player").asPlayerData();
		data.core().mute(time, true);
		ctx.get("player").asOfflinePlayer().sendMessage(RED, RED, "command.commandmute.notify");
		if(time != Long.MAX_VALUE)
			ctx.broadcast("command.commandmute.broadcast", data.getProfile().getName(), BasicTypeFormatter.formatTime(time, true));
		else
			ctx.broadcast("command.commandmute.broadcast.forever", data.getProfile().getName());
	}
	
	@Command(
			name = "unmute",
			group = "basic",
			permissions = {"command.basic.unmute"},
			syntax = {"<player>"}
	)
	public static void unmute(CommandContext ctx)
	{
		PlayerData data = ctx.get("player").asPlayerData();
		ctx.check(data.core().isMuted(), "command.unmute.fail");
		data.core().unmute();
		ctx.get("player").asOfflinePlayer().sendMessage(GREEN, GREEN, "command.unmute.notify");
		ctx.sendMessage("command.unmute.success", data.getProfile().getName());
	}
	
	@Command(
			name = "vanish",
			group = "basic",
			aliases = {"hide"},
			permissions = {"command.basic.vanish"},
			syntax = {""}
	)
	public static void vanish(CommandContext ctx)
	{
		EntityPlayerMP player = ctx.getSenderAsPlayer();
		if(player.isHidden())
		{
			player.show();
			ctx.sendMessage("command.vanish.success.show");
			ctx.notifyOtherAdmins("command.vanish.notify.show");
		}
		else
		{
			player.hide();
			ctx.sendMessage("command.vanish.success.hide");
			ctx.notifyOtherAdmins("command.vanish.notify.hide");
		}
	}
}
