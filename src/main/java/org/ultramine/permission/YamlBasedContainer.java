package org.ultramine.permission;

import org.apache.commons.io.FilenameUtils;
import org.ultramine.server.util.YamlConfigProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlBasedContainer extends UserContainer<User>
{
	private static final String DP_PREFIX = "yaml-dp.";

	private File config;
	private PermissionRepository repository;
	private GroupPermission defaultPermissions;
	private User defaultUser;

	public YamlBasedContainer(PermissionRepository permissionRepository, File config)
	{
		this.config = config;
		this.repository = permissionRepository;

		String name = FilenameUtils.getBaseName(config.getName()).toLowerCase();
		defaultPermissions = new GroupPermission(DP_PREFIX + name);
		defaultPermissions.setMeta("description", "Default permissions for " + name);

		defaultUser = new User(DP_PREFIX + name);
		defaultUser.addPermission(defaultPermissions);

		reload();
	}

	public void reload()
	{
		WorldData data = YamlConfigProvider.getOrCreateConfig(config, WorldData.class);

		defaultPermissions.clearPermissions();
		for (String permission : data.default_permissions)
			defaultPermissions.addPermission(repository.getPermission(permission));
		repository.registerPermission(defaultPermissions);

		clear();
		for (Map.Entry<String, WorldData.UserData> userData : data.users.entrySet())
		{
			User user = new User(userData.getKey(), userData.getValue().meta);
			for (String permission : userData.getValue().permissions)
				user.addPermission(repository.getPermission(permission));
			add(user);
		}
	}

	public void save()
	{
		WorldData data = new WorldData();

		data.default_permissions = defaultPermissions.getInnerPermissions();
		data.users = new HashMap<String, WorldData.UserData>(users.size());

		for (User user : users.values())
		{
			WorldData.UserData userData = new WorldData.UserData();
			userData.permissions = user.getInnerPermissions();
			userData.meta = user.getInnerMeta();
		}

		YamlConfigProvider.saveConfig(config, data);
	}

	public GroupPermission getDefaultPermissions()
	{
		return defaultPermissions;
	}

	public void setParentContainer(UserContainer container)
	{
		parentContainer = container;
	}

	@Override
	protected PermissionResolver.CheckResult check(String userName, String permissionKey)
	{
		PermissionResolver.CheckResult result = super.check(userName, permissionKey);

		if (result == PermissionResolver.CheckResult.UNRESOLVED)
			result = defaultUser.getPermissions().check(permissionKey);

		return result;
	}

	public static class WorldData
	{
		public List<String> default_permissions = new ArrayList<String>();
		public Map<String, UserData> users = new HashMap<String, UserData>();

		public static class UserData {
			public List<String> permissions = new ArrayList<String>();
			public Map<String, Object> meta = new HashMap<String, Object>();
		}
	}
}
