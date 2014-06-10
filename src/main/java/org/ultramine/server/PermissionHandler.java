package org.ultramine.server;

import net.minecraft.command.ICommandSender;
import org.ultramine.permission.DummyPermission;
import org.ultramine.permission.IPermission;
import org.ultramine.permission.IPermissionHandler;
import org.ultramine.permission.PermissionRepository;
import org.ultramine.permission.internal.ClientPermissionManager;
import org.ultramine.permission.internal.MetaResolver;
import org.ultramine.permission.internal.ServerPermissionManager;

import java.util.Set;

public class PermissionHandler implements IPermissionHandler
{
	public static final String OP_PERMISSION = "minecraft.op";

	private static PermissionHandler instance;
	private static PermissionRepository mainRepository = new PermissionRepository();

	// TODO: Удалить
	public static void registerPermission(String key, String name, String description)
	{
		if (instance != null)
			getInstance().getRepository().registerPermission(new DummyPermission(key));
		else
			mainRepository.registerPermission(new DummyPermission(key));
	}

	public static void registerPermission(IPermission permission)
	{
		if (instance != null)
			getInstance().getRepository().registerPermission(permission);
		else
			mainRepository.registerPermission(permission);
	}

	public static PermissionRepository.ProxyPermission getPermission(String key)
	{
		if (instance != null)
			return getInstance().getRepository().getPermission(key);

		return mainRepository.getPermission(key);
	}

	public static void initServer()
	{
		if (instance != null)
			throw new IllegalStateException("Handler is already initialized");
		instance = new PermissionHandler(new ServerPermissionManager(ConfigurationHandler.getSettingDir(), new PermissionRepository(mainRepository)));
	}

	public static void initClient()
	{
		if (instance != null)
			throw new IllegalStateException("Handler is already initialized");
		instance = new PermissionHandler(new ClientPermissionManager(new PermissionRepository(mainRepository)));
	}

	public static void reset()
	{
		instance = null;
	}

	public static PermissionHandler getInstance()
	{
		if (instance == null)
			throw new IllegalStateException("Handler is not initialized");
		return instance;
	}

	private IPermissionHandler handler;

	private PermissionHandler(IPermissionHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public boolean has(String world, String player, String permission)
	{
		return handler.has(world, player, permission);
	}

	public boolean has(ICommandSender player, String permission)
	{
		return has(worldName(player), player.getCommandSenderName(), permission);
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
		handler.add(world, player, permission);
	}

	public void add(ICommandSender player, String permission)
	{
		add(worldName(player), player.getCommandSenderName(), permission);
	}

	@Override
	public void addToWorld(String world, String permission)
	{
		handler.addToWorld(world, permission);
	}

	@Override
	public void addToGroup(String group, String permission)
	{
		handler.addToGroup(group, permission);
	}

	@Override
	public void remove(String world, String player, String permission)
	{
		handler.remove(world, player, permission);
	}

	public void remove(ICommandSender player, String permission)
	{
		remove(worldName(player), player.getCommandSenderName(), permission);
	}

	@Override
	public void removeFromWorld(String world, String permission)
	{
		handler.removeFromWorld(world, permission);
	}

	@Override
	public void removeFromGroup(String group, String permission)
	{
		handler.removeFromGroup(group, permission);
	}

	@Override
	public MetaResolver getMeta(String world, String player)
	{
		return handler.getMeta(world, player);
	}

	public MetaResolver getMeta(ICommandSender player)
	{
		return getMeta(worldName(player), player.getCommandSenderName());
	}

	@Override
	public void setMeta(String world, String player, String key, String value)
	{
		handler.setMeta(world, player, key, value);
	}

	public void setMeta(ICommandSender player, String key, String value)
	{
		setMeta(worldName(player), player.getCommandSenderName(), key, value);
	}

	@Override
	public Set<String> findUsersWithPermission(String world, String permission)
	{
		return handler.findUsersWithPermission(world, permission);
	}

	@Override
	public void save()
	{
		handler.save();
	}

	@Override
	public void reload()
	{
		handler.reload();
	}

	@Override
	public PermissionRepository getRepository()
	{
		return handler.getRepository();
	}

	private String worldName(ICommandSender player)
	{
		return player.getEntityWorld().getWorldInfo().getWorldName();
	}
}
