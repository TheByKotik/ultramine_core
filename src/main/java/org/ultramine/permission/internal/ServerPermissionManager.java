package org.ultramine.permission.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.ultramine.permission.GroupPermission;
import org.ultramine.permission.IPermissionManager;
import org.ultramine.permission.PermissionRepository;
import org.ultramine.permission.User;
import org.ultramine.permission.World;
import org.ultramine.server.util.YamlConfigProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.SERVER)
public class ServerPermissionManager implements IPermissionManager
{
	public final static String GLOBAL_WORLD = "global";
	private final static String GROUPS_CONFIG = "groups.yml";
	private static final String GROUP_PREFIX = "group.";

	private File configDir;
	private Map<String, World> worlds;
	private PermissionRepository permissionRepository;
	private Map<String, GroupPermission> groups;

	public ServerPermissionManager(File configDir, PermissionRepository permissionRepository)
	{
		this.configDir = new File(configDir, "permissions");
		if (!this.configDir.exists())
			this.configDir.mkdir();

		this.permissionRepository = permissionRepository;
		this.worlds = new HashMap<String, World>();
		this.groups = new HashMap<String, GroupPermission>();
		reloadGroups();
		reloadWorld(GLOBAL_WORLD);
	}

	@Override
	public PermissionRepository getRepository()
	{
		return permissionRepository;
	}

	@Override
	public UserContainer getWorldContainer(String world)
	{
		return worlds.get(world);
	}

	@Override
	public boolean has(String world, String player, String permission)
	{
		World worldObj  = worlds.get(world);
		if (worldObj == null)
			worldObj = worlds.get(GLOBAL_WORLD);

		return worldObj.checkUserPermission(player, permission);
	}

	@Override
	public void add(String world, String player, String permission)
	{
		getOrCreateUser(world, player).addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void addToWorld(String world, String permission)
	{
		getOrCreateWorld(world).getDefaultGroup()
				.addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void addToGroup(String group, String permission)
	{
		getOrCreateGroup(group).addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void remove(String world, String player, String permission)
	{
		User user = getUser(world, player);
		if (user == null)
			return;

		user.removePermission(permission);
	}

	@Override
	public void removeFromWorld(String world, String permission)
	{
		World worldObj = worlds.get(world);
		if (worldObj == null)
			return;

		worldObj.getDefaultGroup().removePermission(permission);
	}

	@Override
	public void removeFromGroup(String group, String permission)
	{
		GroupPermission groupObj = groups.get(fixGroupKey(group));
		if (groupObj == null)
			return;

		groupObj.removePermission(permission);
	}

	@Override
	public String getMeta(String world, String player, String key)
	{
		World worldObj  = worlds.get(world);
		if (worldObj == null)
			worldObj = worlds.get(GLOBAL_WORLD);

		return worldObj.getUserMeta(player, key);
	}

	@Override
	public void setMeta(String world, String player, String key, String value)
	{
		getOrCreateUser(world, player).setMeta(key, value);
	}

	@Override
	public void setWorldMeta(String world, String key, String value)
	{
		getOrCreateWorld(world).getDefaultGroup().setMeta(key, value);
	}

	@Override
	public void setGroupMeta(String group, String key, String value)
	{
		getOrCreateGroup(group).setMeta(key, value);
	}

	@Override
	public void save()
	{
		saveGroups();
		for (String world : worlds.keySet())
			saveWorld(world);
	}

	@Override
	public void reload()
	{
		reloadGroups();
		for (String world : worlds.keySet())
			reloadWorld(world);
	}

	public World reloadWorld(String name)
	{
		World.WorldData data = YamlConfigProvider.getOrCreateConfig(worldFile(name), World.WorldData.class);
		World world = worlds.get(name);
		if (world == null)
		{
			world = new World();
			worlds.put(name, world);
		}

		world.load(permissionRepository, data);

		if (!name.equals(GLOBAL_WORLD))
			world.setParentContainer(worlds.get(GLOBAL_WORLD));

		return world;
	}

	public void saveWorld(String name)
	{
		World world = worlds.get(name);
		if (world == null)
			return;

		YamlConfigProvider.saveConfig(worldFile(name), world.save());
	}

	public void reloadGroups()
	{
		for (GroupPermission group : groups.values())
		{
			group.clearPermissions();
			group.clearMeta();
		}

		GroupData data = YamlConfigProvider.getOrCreateConfig(groupsFile(), GroupData.class);
		if (data == null || data.groups == null)
			return;

		for (Map.Entry<String, World.HolderData> groupData : data.groups.entrySet())
		{
			GroupPermission group = getOrCreateGroup(groupData.getKey());
			group.setInnerMeta(groupData.getValue().meta);

			for (String pKey : groupData.getValue().permissions)
				group.addPermission(permissionRepository.getPermission(pKey));
		}
	}

	public void saveGroups()
	{
		GroupData data = new GroupData();

		for (Map.Entry<String, GroupPermission> group : groups.entrySet())
			data.groups.put(group.getKey(), new World.HolderData(group.getValue()));

		YamlConfigProvider.saveConfig(groupsFile(), data);
	}

	private World getOrCreateWorld(String name)
	{
		World world = worlds.get(name);
		if (world == null)
			world = reloadWorld(name);

		return world;
	}

	private User getUser(String worldName, String userName)
	{
		World world  = worlds.get(worldName);
		if (world == null)
			return null;

		return world.get(userName);
	}

	private User getOrCreateUser(String worldName, String userName)
	{
		World world  = getOrCreateWorld(worldName);

		User user = world.get(userName);
		if (user == null)
		{
			user = new User(userName);
			world.add(user);
		}

		return user;
	}

	public static String fixGroupKey(String key)
	{
		if (key.startsWith(GROUP_PREFIX))
			return key;
		else
			return GROUP_PREFIX + key;
	}

	private GroupPermission getOrCreateGroup(String name)
	{
		String groupKey = fixGroupKey(name);
		GroupPermission group = groups.get(groupKey);
		if (group == null)
		{
			group = new GroupPermission(groupKey);
			permissionRepository.registerPermission(group);
			groups.put(groupKey, group);
		}

		return group;
	}

	private File worldFile(String name)
	{
		return new File(configDir, "users-" + name + ".yml");
	}

	private File groupsFile()
	{
		return new File(configDir, GROUPS_CONFIG);
	}

	public static class GroupData
	{
		public Map<String, World.HolderData> groups = new HashMap<String, World.HolderData>();
	}
}
