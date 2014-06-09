package org.ultramine.permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World extends UserContainer<User>
{
	private PermissionRepository repository;
	private GroupPermission defaultPermissions;

	public World(PermissionRepository permissionRepository)
	{
		this.repository = permissionRepository;
		this.defaultPermissions = new GroupPermission("");
	}

	public void load(WorldData data)
	{
		if (data == null)
			return;

		defaultPermissions.clearPermissions();
		if (data.default_permissions != null)
		{
			for (String permission : data.default_permissions)
				defaultPermissions.addPermission(repository.getPermission(permission));
		}

		clear();
		if (data.users == null)
			return;

		for (Map.Entry<String, HolderData> userData : data.users.entrySet())
		{
			User user = new User(userData.getKey(), userData.getValue().meta);
			for (String permission : userData.getValue().permissions)
				user.addPermission(repository.getPermission(permission));
			add(user);
		}
	}

	public WorldData save()
	{
		WorldData data = new WorldData();

		data.default_permissions = defaultPermissions.getInnerPermissions();
		data.users = new HashMap<String, HolderData>(users.size());

		for (User user : users.values())
			data.users.put(user.getName(), new HolderData(user));

		return data;
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
	protected CheckResult check(String userName, String permissionKey)
	{
		CheckResult result = super.check(userName, permissionKey);

		if (result == CheckResult.UNRESOLVED)
			result = defaultPermissions.getPermissionResolver().check(permissionKey);

		return result;
	}

	public static class WorldData
	{
		public List<String> default_permissions = new ArrayList<String>();
		public Map<String, HolderData> users = new HashMap<String, HolderData>();
	}

	public static class HolderData
	{
		public List<String> permissions;
		public Map<String, String> meta;

		public HolderData()
		{
			permissions = new ArrayList<String>();
			meta = new HashMap<String, String>();
		}

		public HolderData(PermissionHolder holder)
		{
			permissions = holder.getInnerPermissions();
			meta = holder.getInnerMeta();
		}
	}
}
