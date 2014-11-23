package org.ultramine.permission.commands;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.permission.internal.ServerPermissionManager;
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
		if (context.actionIs("save"))
		{
			PermissionHandler.getInstance().save();
			context.notifyAdmins("command.pconfig.success.save");
		}
		else
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
					"[meta] <pmeta> <%value>",
					"<world> [add remove] <permission>...",
					"<world> [meta] <pmeta> <%value>",
			}
	)
	public static void pworld(CommandContext context)
	{
		String world = context.contains("world")
				? context.get("world").asWorld().getWorldInfo().getWorldName()
				: ServerPermissionManager.GLOBAL_WORLD;

		context.checkSenderPermissionInWorld(world, "permissions.admin.world");

		if (context.actionIs("add"))
		{
			for (CommandContext.Argument arg : context.get("permission").asArray())
			{
				PermissionHandler.getInstance().addToWorld(world, arg.asString());
				context.notifyAdmins("command.pworld.success.add", arg.asString(), world);
			}
		}
		else if (context.actionIs("remove"))
		{
			for (CommandContext.Argument arg : context.get("permission").asArray())
			{
				PermissionHandler.getInstance().removeFromWorld(world, arg.asString());
				context.notifyAdmins("command.pworld.success.remove", arg.asString(), world);
			}
		}
		else
		{
			String key = context.get("pmeta").asString();
			String value = context.get("value").asString();

			PermissionHandler.getInstance().setWorldMeta(world, key, value);
			context.notifyAdmins("command.pworld.success.meta", key, value, world);
		}

		PermissionHandler.getInstance().save();
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
		String player = context.get("player").asString();
		String world = context.contains("world")
				? context.get("world").asWorld().getWorldInfo().getWorldName()
				: ServerPermissionManager.GLOBAL_WORLD;

		context.checkSenderPermissionInWorld(world, "permissions.admin.world");

		if (context.actionIs("add"))
		{
			for (CommandContext.Argument arg : context.get("permission").asArray())
			{
				PermissionHandler.getInstance().add(world, player, arg.asString());
				context.notifyAdmins("command.puser.success.add", arg.asString(), player, world);
			}
		}
		else if (context.actionIs("remove"))
		{
			for (CommandContext.Argument arg : context.get("permission").asArray())
			{
				PermissionHandler.getInstance().remove(world, player, arg.asString());
				context.notifyAdmins("command.puser.success.remove", arg.asString(), player, world);
			}
		}
		else
		{
			String key = context.get("pmeta").asString();
			String value = context.get("value").asString();

			PermissionHandler.getInstance().setMeta(world, player, key, value);
			context.notifyAdmins("command.puser.success.meta", key, value, player, world);
		}

		PermissionHandler.getInstance().save();
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

		if (context.actionIs("add"))
		{
			for (CommandContext.Argument arg : context.get("permission").asArray())
			{
				PermissionHandler.getInstance().addToGroup(group, arg.asString());
				context.notifyAdmins("command.pgroup.success.add", arg.asString(), group);
			}
		}
		else if (context.actionIs("remove"))
		{
			for (CommandContext.Argument arg : context.get("permission").asArray())
			{
				PermissionHandler.getInstance().removeFromGroup(group, arg.asString());
				context.notifyAdmins("command.pgroup.success.remove", arg.asString(), group);
			}
		}
		else
		{
			String key = context.get("key").asString();
			String value = context.get("value").asString();
			PermissionHandler.getInstance().setGroupMeta(group, key, value);
			context.notifyAdmins("command.pgroup.success.meta", key, value, group);
		}

		PermissionHandler.getInstance().save();
	}
}
