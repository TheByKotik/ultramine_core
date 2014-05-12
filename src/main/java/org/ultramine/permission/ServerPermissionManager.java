package org.ultramine.permission;

import org.ultramine.server.util.YamlConfigProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerPermissionManager implements IPermissionHandler
{
	private final static String GLOBAL_WORLD = "global";
	private final static String GROUPS_CONFIG = "groups.yml";

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
	public boolean has(String world, String player, String permission)
	{
		if (!worlds.containsKey(world))
			return getGlobal().checkUserPermission(player, permission);

		return getWorld(world).checkUserPermission(player, permission);
	}

	@Override
	public void add(String world, String player, String permission)
	{
		if (!worlds.containsKey(world))
			reloadWorld(world);

		World worldContainer = getWorld(world);
		if (!worldContainer.contains(player))
			worldContainer.add(new User(player));

		worldContainer.get(player).addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void add(String world, String permission)
	{
		if (!worlds.containsKey(world))
			reloadWorld(world);

		getWorld(world).getDefaultPermissions().addPermission(permissionRepository.getPermission(permission));
	}

	@Override
	public void remove(String world, String player, String permission)
	{
		if (!worlds.containsKey(world))
			return;

		World worldContainer = getWorld(world);
		if (!worldContainer.contains(player))
			return;

		worldContainer.get(player).removePermission(permission);
	}

	@Override
	public void remove(String world, String permission)
	{
		if (!worlds.containsKey(world))
			return;

		getWorld(world).getDefaultPermissions().removePermission(permission);
	}

	@Override
	public MetaResolver getMeta(String world, String player)
	{
		if (!worlds.containsKey(world))
			return MetaResolver.BLANK_RESOLVER;

		World worldContainer = getWorld(world);
		if (!worldContainer.contains(player))
			return MetaResolver.BLANK_RESOLVER;

		return worldContainer.get(player).getMeta();
	}

	@Override
	public void setMeta(String world, String player, String key, Object value)
	{
		if (!worlds.containsKey(world))
			reloadWorld(world);

		World worldContainer = getWorld(world);
		if (!worldContainer.contains(player))
			worldContainer.add(new User(player));

		worldContainer.get(player).setMeta(key, value);
	}

	@Override
	public Set<String> findUsersWithPermission(String world, String permission)
	{
		if (!worlds.containsKey(world))
			return getGlobal().getAllWithPermission(permission);

		return getWorld(world).getAllWithPermission(permission);
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

	@Override
	public PermissionRepository getRepository()
	{
		return permissionRepository;
	}

	public void reloadWorld(String name)
	{
		World.WorldData data = YamlConfigProvider.getOrCreateConfig(worldFile(name), World.WorldData.class);
		if (!worlds.containsKey(name))
			worlds.put(name, new World(permissionRepository));

		worlds.get(name).load(data);

		if (!name.equals(GLOBAL_WORLD))
			worlds.get(name).setParentContainer(getGlobal());

	}

	public void saveWorld(String name)
	{
		if (!worlds.containsKey(name))
			return;

		YamlConfigProvider.saveConfig(worldFile(name), worlds.get(name).save());
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
			GroupPermission group;
			if (!groups.containsKey(groupData.getKey()))
			{
				group = new GroupPermission(groupData.getKey(), groupData.getValue().meta);
				permissionRepository.registerPermission(group);
				groups.put(groupData.getKey(), group);
			}
			else
			{
				group = groups.get(groupData.getKey());
				group.setInnerMeta(groupData.getValue().meta);
			}

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

	public World getWorld(String name)
	{
		return worlds.get(name);
	}

	public World getGlobal()
	{
		return getWorld(GLOBAL_WORLD);
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
