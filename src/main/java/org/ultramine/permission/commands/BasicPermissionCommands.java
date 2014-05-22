package org.ultramine.permission.commands;

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
			completion = "<list save reload>"
	)
	public static void pcofnig(CommandContext context)
	{
		String action = context.getString(0);

		if (action.equals("save"))
		{
			PermissionHandler.getInstance().save();
			context.notifyAdmins("command.pconfig.success.save");
		}
		else if (action.equals("reload"))
		{
			PermissionHandler.getInstance().reload();
			context.notifyAdmins("command.pconfig.success.reload");
		}
		else
			context.throwBadUsage();
	}

	@Command(
			name = "pworld",
			group = "permissions",
			permissions = {"permissions.admin"},
			completion = "<list add remove> <permission>...",
			isUsableFromServer = false
	)
	public static void pworld(CommandContext context)
	{
		String action = context.getString(0);
		String[] permissions = context.getLast(1);
		String world = context.getSenderAsPlayer().getEntityWorld().getWorldInfo().getWorldName();

		doPworld(context, world, action, permissions);
	}

	@Command(
			name = "pworld.super",
			group = "permissions",
			permissions = {"permissions.superadmin"},
			completion = "<world> <list add remove> <permission>..."
	)
	public static void pworldSuper(CommandContext context)
	{
		String world = context.getString(0);
		String action = context.getString(1);
		String[] permissions = context.getLast(2);

		doPworld(context, world, action, permissions);
	}

	private static void doPworld(CommandContext context, String world, String action, String[] permissions)
	{
		if (permissions.length == 0)
			context.throwBadUsage();

		if (action.equals("add"))
		{
			for (String permission : permissions)
			{
				PermissionHandler.getInstance().addToWorld(world, permission);
				context.notifyAdmins("command.pworld.success.add", permission, world);
			}
		}
		else if (action.equals("remove"))
		{
			for (String permission : permissions)
			{
				PermissionHandler.getInstance().removeFromWorld(world, permission);
				context.notifyAdmins("command.pworld.success.remove", permission, world);
			}
		}
		else
			context.throwBadUsage();
	}

	@Command(
			name = "puser",
			group = "permissions",
			permissions = {"permissions.admin"},
			completion = "<player> <list add remove> <permission>...",
			isUsableFromServer = false
	)
	public static void puser(CommandContext context)
	{
		String player = context.getString(0);
		String action = context.getString(1);
		String[] permissions = context.getLast(2);
		String world = context.getSenderAsPlayer().getEntityWorld().getWorldInfo().getWorldName();

		doPuser(context, world, player, action, permissions);
	}

	@Command(
			name = "puser.super",
			group = "permissions",
			permissions = {"permissions.superadmin"},
			completion = "<world> <player> <list add remove> <permission>..."
	)
	public static void puserSuper(CommandContext context)
	{
		String world = context.getString(0);
		String player = context.getString(1);
		String action = context.getString(2);
		String[] permissions = context.getLast(3);

		doPuser(context, world, player, action, permissions);
	}

	private static void doPuser(CommandContext context, String world, String player, String action, String[] permissions)
	{
		if (permissions.length == 0)
			context.throwBadUsage();

		if (action.equals("add"))
		{
			for (String permission : permissions)
			{
				PermissionHandler.getInstance().add(world, player, permission);
				context.notifyAdmins("command.puser.success.add", permission, player, world);
			}
		}
		else if (action.equals("remove"))
		{
			for (String permission : permissions)
			{
				PermissionHandler.getInstance().remove(world, player, permission);
				context.notifyAdmins("command.puser.success.remove", permission, player, world);
			}
		}
		else
			context.throwBadUsage();
	}

	@Command(
			name = "pgroup",
			group = "permissions",
			permissions = {"permissions.superadmin"},
			completion = "<group> <list add remove> <permission>..."
	)
	public static void pgroup(CommandContext context)
	{
		String group = context.getString(0);
		String action = context.getString(1);
		String[] permissions = context.getLast(2);

		if (permissions.length == 0)
			context.throwBadUsage();

		if (action.equals("add"))
		{
			for (String permission : permissions)
			{
				PermissionHandler.getInstance().addToGroup(group, permission);
				context.notifyAdmins("command.pgroup.success.add", permission, group);
			}
		}
		else if (action.equals("remove"))
		{
			for (String permission : permissions)
			{
				PermissionHandler.getInstance().removeFromGroup(group, permission);
				context.notifyAdmins("command.pgroup.success.remove", permission, group);
			}
		}
		else
			context.throwBadUsage();
	}
}
