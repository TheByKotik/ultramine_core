package org.ultramine.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.ultramine.permission.GroupPermission;
import org.ultramine.permission.IPermissionManager;
import org.ultramine.permission.PermissionRepository;
import org.ultramine.permission.internal.ServerPermissionManager;
import org.ultramine.permission.internal.UserContainer;

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
	public void addToWorld(String world, String permission)
	{
		getHandler().addToWorld(world, permission);
	}

	@Override
	public void addToGroup(String group, String permission)
	{
		getHandler().addToGroup(group, permission);
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
	public void removeFromWorld(String world, String permission)
	{
		getHandler().removeFromWorld(world, permission);
	}

	@Override
	public void removeFromGroup(String group, String permission)
	{
		getHandler().removeFromGroup(group, permission);
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
	public void setGroupMeta(String group, String key, String value)
	{
		getHandler().setGroupMeta(group, key, value);
	}

	public void setMeta(ICommandSender player, String key, String value)
	{
		setMeta(worldName(player), player.getCommandSenderName(), key, value);
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

	@Override
	public UserContainer getWorldContainer(String world)
	{
		return getHandler().getWorldContainer(world);
	}

	public GroupPermission createGroup(String name, String... permissions)
	{
		GroupPermission group = new GroupPermission(name);
		for (String permission : permissions)
			group.addPermission(getRepository().getPermission(permission));
		getRepository().registerPermission(group);
		return group;
	}

	private String worldName(ICommandSender player)
	{
		return player.getEntityWorld().getWorldInfo().getWorldName();
	}
}
