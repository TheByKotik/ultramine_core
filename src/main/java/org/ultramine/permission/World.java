package org.ultramine.permission;

import org.ultramine.permission.internal.CheckResult;
import org.ultramine.permission.internal.PermissionHolder;
import org.ultramine.permission.internal.UserContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World extends UserContainer<User>
{
	private GroupPermission defaultGroup;

	public World()
	{
		this.defaultGroup = new GroupPermission("");
	}

	public void load(PermissionRepository repository, WorldData data)
	{
		if (data == null)
			return;

		defaultGroup = new GroupPermission("");
		if (data.default_group != null)
		{
			if (data.default_group.meta != null)
				defaultGroup.setInnerMeta(data.default_group.meta);

			if (data.default_group.permissions != null)
			{
				for (String permission : data.default_group.permissions)
					defaultGroup.addPermission(repository.getPermission(permission));
			}
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

		data.default_group = new HolderData(defaultGroup);
		data.users = new HashMap<String, HolderData>(users.size());

		for (User user : users.values())
		{
			HolderData holderData = new HolderData(user);
			if (!holderData.isEmpty())
				data.users.put(user.getName(), new HolderData(user));
		}

		return data;
	}

	public GroupPermission getDefaultGroup()
	{
		return defaultGroup;
	}

	public void setParentContainer(UserContainer container)
	{
		parentContainer = container;
	}

	@Override
	public String getUserMeta(String userName, String metaKey)
	{
		String result = super.getUserMeta(userName, metaKey);
		return result.isEmpty() ? defaultGroup.getMeta(metaKey) : result;
	}

	@Override
	protected CheckResult check(String userName, String permissionKey)
	{
		CheckResult result = super.check(userName, permissionKey);
		return result == CheckResult.UNRESOLVED ? defaultGroup.check(permissionKey) : result;
	}

	public static class WorldData
	{
		public HolderData default_group = new HolderData();
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

		public boolean isEmpty()
		{
			return permissions.isEmpty() && meta.isEmpty();
		}
	}
}
