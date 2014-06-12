package org.ultramine.permission.commands;

import net.minecraft.command.WrongUsageException;
import org.ultramine.commands.Action;
import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.PermissionHandler;

public class BasicPermissionCommands
{
	@Command(
			name = "pconfig",
			group = "permissions",
			aliases = {"permissions", "pcfg"},
			permissions = {"permissions.admin"},
			syntax = {"[save reload]"}
	)
	public static void pcofnig(CommandContext context)
	{
		if (context.getAction().equals("save"))
		{
			PermissionHandler.getInstance().save();
			context.notifyAdmins("command.pconfig.success.save");
		}
		else if (context.getAction().equals("reload"))
		{
			PermissionHandler.getInstance().reload();
			context.notifyAdmins("command.pconfig.success.reload");
		}
	}



	@Command(
			name = "pworld",
			group = "permissions",
			permissions = {"permissions.admin.world"},
			syntax = {
					"[add remove] <permission>...",
					"<world> [add remove] <permission>..."
			}
	)
	public static void pworld(CommandContext context)
	{
		if (!context.contains("world"))
		{
			if (context.senderIsServer())
				throw new WrongUsageException("command.permissions.serverworld");

			context.set("world", context.getSenderAsPlayer().getEntityWorld().getWorldInfo().getWorldName());
		}

		context.checkSenderPermissionInWorld(context.get("world").asString(), "permissions.admin.world");
		context.doAction();
	}

	@Action(command = "pworld", name = "add")
	public static void pworld_add(CommandContext context)
	{
		String world = context.get("world").asString();

		for (CommandContext.Argument arg : context.get("permission").asArray())
		{
			PermissionHandler.getInstance().addToWorld(world, arg.asString());
			context.notifyAdmins("command.pworld.success.add", arg.asString(), world);
		}
	}

	@Action(command = "pworld", name = "remove")
	public static void pworld_remove(CommandContext context)
	{
		String world = context.get("world").asString();

		for (CommandContext.Argument arg : context.get("permission").asArray())
		{
			PermissionHandler.getInstance().removeFromWorld(world, arg.asString());
			context.notifyAdmins("command.pworld.success.remove", arg.asString(), world);
		}
	}



	@Command(
			name = "puser",
			group = "permissions",
			permissions = {"permissions.admin.user"},
			syntax = {
					"<player> [add remove] <permission>...",
					"<player> [meta] <pmeta> <%value>",
					"<world> <player> [add remove] <permission>...",
					"<world> <player> [meta] <pmeta> <%value>"
			}
	)
	public static void puser(CommandContext context)
	{
		if (!context.contains("world"))
		{
			if (context.senderIsServer())
				throw new WrongUsageException("command.permissions.serverworld");

			context.set("world", context.getSenderAsPlayer().getEntityWorld().getWorldInfo().getWorldName());
		}

		context.checkSenderPermissionInWorld(context.get("world").asString(), "permissions.admin.user");
		context.doAction();
	}

	@Action(command = "puser", name = "add")
	public static void puser_add(CommandContext context)
	{
		String world = context.get("world").asString();
		String player = context.get("player").asString();

		for (CommandContext.Argument arg : context.get("permission").asArray())
		{
			PermissionHandler.getInstance().add(world, player, arg.asString());
			context.notifyAdmins("command.puser.success.add", arg.asString(), player, world);
		}
	}

	@Action(command = "puser", name = "remove")
	public static void puser_remove(CommandContext context)
	{
		String world = context.get("world").asString();
		String player = context.get("player").asString();

		for (CommandContext.Argument arg : context.get("permission").asArray())
		{
			PermissionHandler.getInstance().remove(world, player, arg.asString());
			context.notifyAdmins("command.puser.success.remove", arg.asString(), player, world);
		}
	}

	@Action(command = "puser", name = "meta")
	public static void puser_meta(CommandContext context)
	{
		String world = context.get("world").asString();
		String player = context.get("player").asString();
		String key = context.get("pmeta").asString();
		String value = context.get("value").asString();

		PermissionHandler.getInstance().setMeta(world, player, key, value);
		context.notifyAdmins("command.puser.success.meta", key, value, player, world);
	}



	@Command(
			name = "pgroup",
			group = "permissions",
			permissions = {"permissions.admin.group"},
			syntax = {
					"<group> [add remove] <permission>...",
					"<group> [meta] <pmeta> <%value>"
			}
	)
	public static void pgroup(CommandContext context)
	{
		String group = context.get("group").asString();

		if (context.getAction().equals("add"))
		{
			for (CommandContext.Argument arg : context.get("permission").asArray())
			{
				PermissionHandler.getInstance().addToGroup(group, arg.asString());
				context.notifyAdmins("command.pgroup.success.add", arg.asString(), group);
			}
		}

		else if (context.getAction().equals("remove"))
		{
			for (CommandContext.Argument arg : context.get("permission").asArray())
			{
				PermissionHandler.getInstance().removeFromGroup(group, arg.asString());
				context.notifyAdmins("command.pgroup.success.remove", arg.asString(), group);
			}
		}

		else if (context.getAction().equals("meta"))
		{
			String key = context.get("key").asString();
			String value = context.get("value").asString();
			PermissionHandler.getInstance().setGroupMeta(group, key, value);
			context.notifyAdmins("command.pgroup.success.meta", key, value, group);
		}
	}

}
