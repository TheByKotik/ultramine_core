package org.ultramine.permission.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.ultramine.permission.GroupPermission;
import org.ultramine.permission.IPermissionManager;
import org.ultramine.permission.PermissionRepository;
import org.ultramine.permission.User;
import org.ultramine.permission.World;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientPermissionManager implements IPermissionManager
{
	private World global;
	private PermissionRepository permissionRepository;
	private Map<String, GroupPermission> groups;
	private String owner;

	public ClientPermissionManager(String owner, PermissionRepository permissionRepository)
	{
		this.permissionRepository = permissionRepository;
		this.global = new World();
		this.groups = new HashMap<String, GroupPermission>();
		this.owner = owner;
	}

	@Override
	public boolean has(String world, String player, String permission)
	{
		return player.equalsIgnoreCase(owner) || global.checkUserPermission(player, permission);
	}

	@Override
	public void add(String world, String player, String permission)
	{
		getOrCreateUser(player).addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void addToWorld(String world, String permission)
	{
		global.getDefaultPermissions().addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void addToGroup(String group, String permission)
	{
		getOrCreateGroup(group).addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void remove(String world, String player, String permission)
	{
		User user = global.get(player);
		if (user == null)
			return;

		user.removePermission(permission);
	}

	@Override
	public void removeFromWorld(String world, String permission)
	{
		global.getDefaultPermissions().removePermission(permission);
	}

	@Override
	public void removeFromGroup(String group, String permission)
	{
		GroupPermission groupObj = groups.get(ServerPermissionManager.fixGroupKey(group));
		groupObj.removePermission(permission);
	}

	@Override
	public String getMeta(String world, String player, String key)
	{
		User user = global.get(player);
		if (user == null)
			return "";
		else
			return user.getMeta(key);
	}

	@Override
	public void setMeta(String world, String player, String key, String value)
	{
		getOrCreateUser(player).setMeta(key, value);
	}

	@Override
	public void setGroupMeta(String group, String key, String value)
	{
		getOrCreateGroup(group).setMeta(key, value);
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

	@Override
	public UserContainer getWorldContainer(String world)
	{
		return global;
	}

	private User getOrCreateUser(String name)
	{
		User user = global.get(name);
		if (user == null)
		{
			user = new User(name);
			global.add(user);
		}

		return user;
	}

	private GroupPermission getOrCreateGroup(String name)
	{
		String groupKey = ServerPermissionManager.fixGroupKey(name);
		GroupPermission group = groups.get(groupKey);
		if (group == null)
		{
			group = new GroupPermission(groupKey);
			permissionRepository.registerPermission(group);
			groups.put(groupKey, group);
		}

		return group;
	}
}
