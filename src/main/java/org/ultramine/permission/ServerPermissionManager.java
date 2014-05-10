package org.ultramine.permission;

import org.ultramine.server.util.YamlConfigProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ServerPermissionManager
{
	private final static String GLOBAL_WORLD = "global";
	private final static String GROUPS_CONFIG = "groups.yml";

	private File configDir;
	private Map<String, World> worlds;
	private PermissionRepository permissionRepository;
	private Map<String, GroupPermission> groups;

	public ServerPermissionManager(File configDir)
	{
		this.configDir = new File(configDir, "permissions");
		if (!this.configDir.exists())
			this.configDir.mkdir();

		this.permissionRepository = new PermissionRepository();
		this.worlds = new HashMap<String, World>();
		this.groups = new HashMap<String, GroupPermission>();
		reloadWorld(GLOBAL_WORLD);
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
		Map<String, World.HolderData> groups = new HashMap<String, World.HolderData>();
	}
}
