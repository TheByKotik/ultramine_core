package org.ultramine.permission;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClientPermissionManager implements IPermissionHandler
{
	private World global;
	private PermissionRepository permissionRepository;
	private Map<String, GroupPermission> groups;

	public ClientPermissionManager(PermissionRepository permissionRepository)
	{
		this.permissionRepository = permissionRepository;
		this.global = new World(permissionRepository);
		this.groups = new HashMap<String, GroupPermission>();
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
	public void addToWorld(String world, String permission)
	{
		global.getDefaultPermissions().addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void addToGroup(String group, String permission)
	{
		if (!group.startsWith("group."))
			group = "group." + group;

		if (!groups.containsKey(group))
			groups.put(group, new GroupPermission(group));

		groups.get(group).addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void remove(String world, String player, String permission)
	{
		if (!global.contains(player))
			return;

		global.get(player).removePermission(permission);
	}

	@Override
	public void removeFromWorld(String world, String permission)
	{
		global.getDefaultPermissions().removePermission(permission);
	}

	@Override
	public void removeFromGroup(String group, String permission)
	{
		if (!group.startsWith("group."))
			group = "group." + group;

		if (!groups.containsKey(group))
			return;

		groups.get(group).removePermission(permission);
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
