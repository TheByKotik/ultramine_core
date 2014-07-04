package org.ultramine.commands.basic;

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
}
