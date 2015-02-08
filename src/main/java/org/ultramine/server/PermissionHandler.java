package org.ultramine.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import org.ultramine.permission.MixinPermission;
import org.ultramine.permission.IPermissionManager;
import org.ultramine.permission.PermissionRepository;
import org.ultramine.permission.internal.ServerPermissionManager;

public class PermissionHandler implements IPermissionManager
{
	private static PermissionHandler instance = new PermissionHandler();

	public static PermissionHandler getInstance()
	{
		return instance;
	}

	private IPermissionManager getHandler()
	{
		return MinecraftServer.getServer().getPermissionManager();
	}

	@Override
	public boolean has(String world, String player, String permission)
	{
		return getHandler().has(world, player, permission);
	}

	public boolean has(ICommandSender player, String permission)
	{
		return has(worldName(player), player.getCommandSenderName(), permission);
	}

	public boolean hasGlobally(String player, String permission)
	{
		return has(ServerPermissionManager.GLOBAL_WORLD, player, permission);
	}

	public boolean hasGlobally(ICommandSender player, String permission)
	{
		return has(ServerPermissionManager.GLOBAL_WORLD, player.getCommandSenderName(), permission);
	}

	public boolean has(ICommandSender player, String... permissions)
	{
		if (permissions == null)
			return true;

		for (String permission : permissions)
			if (!has(player, permission)) return false;
		return true;
	}

	public boolean hasAny(ICommandSender player, String... permissions)
	{
		if (permissions == null)
			return true;

		for (String permission : permissions)
			if (has(player, permission)) return true;
		return false;
	}

	@Override
	public void add(String world, String player, String permission)
	{
		getHandler().add(world, player, permission);
	}

	public void add(ICommandSender player, String permission)
	{
		add(worldName(player), player.getCommandSenderName(), permission);
	}

	@Override
	public void addToMixin(String group, String permission)
	{
		getHandler().addToMixin(group, permission);
	}
	
	@Override
	public void addToGroup(String group, String world, String permission)
	{
		getHandler().addToGroup(group, world, permission);
	}

	@Override
	public void remove(String world, String player, String permission)
	{
		getHandler().remove(world, player, permission);
	}

	public void remove(ICommandSender player, String permission)
	{
		remove(worldName(player), player.getCommandSenderName(), permission);
	}

	@Override
	public void removeFromMixin(String group, String permission)
	{
		getHandler().removeFromMixin(group, permission);
	}
	
	@Override
	public void removeFromGroup(String group, String world, String permission)
	{
		getHandler().removeFromGroup(group, world, permission);
	}

	@Override
	public String getMeta(String world, String player, String key)
	{
		return getHandler().getMeta(world, player, key);
	}

	public String getMeta(ICommandSender player, String key)
	{
		return getMeta(worldName(player), player.getCommandSenderName(), key);
	}

	@Override
	public void setMeta(String world, String player, String key, String value)
	{
		getHandler().setMeta(world, player, key, value);
	}

	@Override
	public void setMixinMeta(String group, String key, String value)
	{
		getHandler().setMixinMeta(group, key, value);
	}
	
	@Override
	public void setGroupMeta(String group, String world, String key, String value)
	{
		getHandler().setGroupMeta(group, world, key, value);
	}

	public void setMeta(ICommandSender player, String key, String value)
	{
		setMeta(worldName(player), player.getCommandSenderName(), key, value);
	}
	
	@Override
	public void setUserGroup(String user, String group)
	{
		getHandler().setUserGroup(user, group);
	}

	@Override
	public void setGroupInherits(String group, String parent)
	{
		getHandler().setGroupInherits(group, parent);
	}

	@Override
	public void save()
	{
		getHandler().save();
	}

	@Override
	public void reload()
	{
		getHandler().reload();
	}

	@Override
	public PermissionRepository getRepository()
	{
		return getHandler().getRepository();
	}

	public MixinPermission createGroup(String name, String... permissions)
	{
		MixinPermission group = new MixinPermission(name);
		for (String permission : permissions)
			group.addPermission(getRepository().getPermission(permission));
		getRepository().registerPermission(group);
		return group;
	}

	private String worldName(ICommandSender player)
	{
		return MinecraftServer.getServer().getMultiWorld().getNameByID(player.getEntityWorld().provider.dimensionId);
	}
}
