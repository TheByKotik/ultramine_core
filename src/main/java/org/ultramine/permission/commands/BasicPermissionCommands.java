package org.ultramine.permission.commands;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.permission.IPermissionManager;
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
	public static void pcofnig(CommandContext ctx)
	{
		if (ctx.actionIs("save"))
		{
			PermissionHandler.getInstance().save();
			ctx.notifyAdmins("command.pconfig.success.save");
		}
		else
		{
			PermissionHandler.getInstance().reload();
			ctx.notifyAdmins("command.pconfig.success.reload");
		}
	}

	@Command(
			name = "puser",
			group = "permissions",
			permissions = {"permissions.admin.user"},
			syntax = {
					"<player> [group setgroup] <group>",
					"<player> [add remove] <permission>...",
					"<player> [meta] <pmeta> <%value>",
					"<world> <player> [add remove] <permission>...",
					"<world> <player> [meta] <pmeta> <%value>"
			}
	)
	public static void puser(CommandContext ctx)
	{
		String player = ctx.get("player").asString();
		String world = ctx.contains("world")
				? ctx.get("world").asWorld().getWorldInfo().getWorldName()
				: IPermissionManager.GLOBAL_WORLD;

		ctx.checkSenderPermissionInWorld(world, "permissions.admin.world");

		if (ctx.actionIs("group") || ctx.actionIs("setgroup"))
		{
			PermissionHandler.getInstance().setUserGroup(player, ctx.get("group").asString());
			ctx.sendMessage("command.puser.success.group", player, ctx.get("group").asString());
		}
		else if (ctx.actionIs("add"))
		{
			for (CommandContext.Argument arg : ctx.get("permission").asArray())
			{
				PermissionHandler.getInstance().add(world, player, arg.asString());
				ctx.sendMessage("command.puser.success.add", arg.asString(), player, world);
			}
		}
		else if (ctx.actionIs("remove"))
		{
			for (CommandContext.Argument arg : ctx.get("permission").asArray())
			{
				PermissionHandler.getInstance().remove(world, player, arg.asString());
				ctx.sendMessage("command.puser.success.remove", arg.asString(), player, world);
			}
		}
		else
		{
			String key = ctx.get("pmeta").asString();
			String value = ctx.get("value").asString();

			PermissionHandler.getInstance().setMeta(world, player, key, value);
			ctx.sendMessage("command.puser.success.meta", key, value, player, world);
		}

		PermissionHandler.getInstance().save();
	}


	@Command(
			name = "pmixin",
			group = "permissions",
			permissions = {"permissions.admin.mixin"},
			syntax = {
					"<mixin> [add remove] <permission>...",
					"<mixin> [meta] <pmeta> <%value>"
			}
	)
	public static void pmixin(CommandContext ctx)
	{
		String mixin = ctx.get("mixin").asString();

		if (ctx.actionIs("add"))
		{
			for (CommandContext.Argument arg : ctx.get("permission").asArray())
			{
				PermissionHandler.getInstance().addToMixin(mixin, arg.asString());
				ctx.sendMessage("command.pmixin.success.add", arg.asString(), mixin);
			}
		}
		else if (ctx.actionIs("remove"))
		{
			for (CommandContext.Argument arg : ctx.get("permission").asArray())
			{
				PermissionHandler.getInstance().removeFromMixin(mixin, arg.asString());
				ctx.sendMessage("command.pmixin.success.remove", arg.asString(), mixin);
			}
		}
		else
		{
			String key = ctx.get("key").asString();
			String value = ctx.get("value").asString();
			PermissionHandler.getInstance().setMixinMeta(mixin, key, value);
			ctx.sendMessage("command.pmixin.success.meta", key, value, mixin);
		}

		PermissionHandler.getInstance().save();
	}
	
	@Command(
			name = "pgroup",
			group = "permissions",
			permissions = {"permissions.admin.group"},
			syntax = {
					"<group> [parent setparent] <group%parent>",
					"<group> [add remove] <permission>...",
					"<group> [meta] <pmeta> <%value>",
					"<group> <world> [add remove] <permission>...",
					"<group> <world> [meta] <pmeta> <%value>"
			}
	)
	public static void pgroup(CommandContext ctx)
	{
		String group = ctx.get("group").asString();
		String world = ctx.contains("world")
				? ctx.getServer().getMultiWorld().getNameByID(ctx.get("world").asWorld().provider.dimensionId)
				: IPermissionManager.GLOBAL_WORLD;

		if (ctx.actionIs("parent") || ctx.actionIs("setparent"))
		{
			PermissionHandler.getInstance().setUserGroup(group, ctx.get("parent").asString());
			ctx.sendMessage("command.pgroup.success.parent", ctx.get("parent").asString(), group);
		}
		if (ctx.actionIs("add"))
		{
			for (CommandContext.Argument arg : ctx.get("permission").asArray())
			{
				PermissionHandler.getInstance().addToGroup(group, world, arg.asString());
				ctx.sendMessage("command.pgroup.success.add", arg.asString(), group);
			}
		}
		else if (ctx.actionIs("remove"))
		{
			for (CommandContext.Argument arg : ctx.get("permission").asArray())
			{
				PermissionHandler.getInstance().removeFromGroup(group, world, arg.asString());
				ctx.sendMessage("command.pgroup.success.remove", arg.asString(), group);
			}
		}
		else
		{
			String key = ctx.get("key").asString();
			String value = ctx.get("value").asString();
			PermissionHandler.getInstance().setGroupMeta(group, world, key, value);
			ctx.sendMessage("command.pgroup.success.meta", key, value, group);
		}

		PermissionHandler.getInstance().save();
	}
}
