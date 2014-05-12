package org.ultramine.permission;

import java.util.Set;

public class ClientPermissionManager implements IPermissionHandler
{
	private World global;
	private PermissionRepository permissionRepository;

	public ClientPermissionManager(PermissionRepository permissionRepository)
	{
		this.permissionRepository = permissionRepository;
		this.global = new World(permissionRepository);
	}

	@Override
	public boolean has(String world, String player, String permission)
	{
		return global.checkUserPermission(player, permission);
	}

	@Override
	public void add(String world, String player, String permission)
	{
		if (!global.contains(player))
			global.add(new User(player));

		global.get(player).addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void add(String world, String permission)
	{
		global.getDefaultPermissions().addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void remove(String world, String player, String permission)
	{
		if (!global.contains(player))
			return;

		global.get(player).removePermission(permission);
	}

	@Override
	public void remove(String world, String permission)
	{
		global.getDefaultPermissions().removePermission(permission);
	}

	@Override
	public MetaResolver getMeta(String world, String player)
	{
		if (!global.contains(player))
			return MetaResolver.BLANK_RESOLVER;

		return global.get(player).getMeta();
	}

	@Override
	public void setMeta(String world, String player, String key, Object value)
	{
		if (!global.contains(player))
			global.add(new User(player));

		global.get(player).setMeta(key, value);
	}

	@Override
	public Set<String> findUsersWithPermission(String world, String permission)
	{
		return global.getAllWithPermission(permission);
	}

	@Override
	public void save()
	{
	}

	@Override
	public void reload()
	{
	}

	@Override
	public PermissionRepository getRepository()
	{
		return permissionRepository;
	}
}
