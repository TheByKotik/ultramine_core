package org.ultramine.permission.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.ultramine.permission.MixinPermission;
import org.apache.commons.lang3.StringUtils;
import org.ultramine.permission.IPermissionManager;
import org.ultramine.permission.PermissionRepository;
import org.ultramine.server.util.YamlConfigProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.SERVER)
public class ServerPermissionManager implements IPermissionManager
{
	private static final String GROUPS_CONFIG = "groups.yml";
	private static final String MIXINS_CONFIG = "mixins.yml";
	private static final String MIXIN_PREFIX = "mixin.";

	private final File configDir;
	private final PermissionRepository permissionRepository;
	private final Map<String, MixinPermission> mixins = new LinkedHashMap<String, MixinPermission>();
	private final Map<String, PermUser> groups = new LinkedHashMap<String, PermUser>();
	private final Map<String, PermUser> users = new LinkedHashMap<String, PermUser>();
	private final PermUser defaultGroup;

	public ServerPermissionManager(File configDir, PermissionRepository permissionRepository)
	{
		this.configDir = new File(configDir, "permissions");
		if (!this.configDir.exists())
			this.configDir.mkdir();

		this.permissionRepository = permissionRepository;
		defaultGroup = getOrCreateGroup(DEFAULT_GROUP_NAME);
	}

	@Override
	public PermissionRepository getRepository()
	{
		return permissionRepository;
	}

	@Override
	public boolean has(String world, String player, String permission)
	{
		PermUser user = users.get(player.toLowerCase());
		if(user == null)
			user = defaultGroup;
		return user.checkUserPermission(world, permission);
	}

	@Override
	public void add(String world, String player, String permission)
	{
		getOrCreateUser(player).addPermission(world, permissionRepository.getPermission(permission));
	}

	@Override
	public void addToMixin(String group, String permission)
	{
		getOrCreateMixin(group).addPermission(permissionRepository.getPermission(permission));
	}
	
	@Override
	public void addToGroup(String group, String world, String permission)
	{
		getOrCreateGroup(group).addPermission(world, permissionRepository.getPermission(permission));
	}

	@Override
	public void remove(String world, String player, String permission)
	{
		PermUser user = users.get(player.toLowerCase());
		if (user == null)
			return;

		user.removePermission(world, permission);
	}

	@Override
	public void removeFromMixin(String mixin, String permission)
	{
		MixinPermission groupObj = mixins.get(fixMixinKey(mixin));
		if (groupObj == null)
			return;

		groupObj.removePermission(permission);
	}
	
	@Override
	public void removeFromGroup(String group, String world, String permission)
	{
		getOrCreateGroup(group).removePermission(world, permission);
	}

	@Override
	public String getMeta(String world, String player, String key)
	{
		PermUser user  = users.get(player.toLowerCase());
		if (user == null)
			user = defaultGroup;

		String meta = user.getMeta(world, key);
		return meta != null ? meta : "";
	}

	@Override
	public void setMixinMeta(String group, String key, String value)
	{
		getOrCreateMixin(group).setMeta(key, value);
	}
	
	@Override
	public void setGroupMeta(String group, String world, String key, String value)
	{
		getOrCreateGroup(group).setMeta(world, key, value);
	}
	
	@Override
	public void setMeta(String world, String player, String key, String value)
	{
		getOrCreateUser(player).setMeta(world, key, value);
	}

	@Override
	public void setUserGroup(String user, String group)
	{
		getOrCreateUser(user).setParent(permissionRepository.getContainer(group));
	}

	@Override
	public void setGroupInherits(String group, String parent)
	{
		getOrCreateGroup(group).setParent(permissionRepository.getContainer(parent));
	}

	@Override
	public void save()
	{
		saveMixins();
		saveGroups();
		saveUsers();
	}

	@Override
	public void reload()
	{
		reloadMixins();
		reloadGroups();
		reloadUsers();
	}

	public void reloadMixins()
	{
		File file = mixinsFile();
		if(file.exists())
		{
			for (MixinPermission mixin : mixins.values())
			{
				mixin.clearPermissions();
				mixin.clearMeta();
			}

			MixinData data = YamlConfigProvider.getOrCreateConfig(file, MixinData.class);
			if (data == null || data.mixins == null)
				return;

			for (Map.Entry<String, HolderData> groupData : data.mixins.entrySet())
			{
				MixinPermission mixin = getOrCreateMixin(groupData.getKey());
				mixin.setInnerMeta(groupData.getValue().meta);

				for (String pKey : groupData.getValue().permissions)
					mixin.addPermission(permissionRepository.getPermission(pKey));
			}	
		}
		else
		{
			saveMixins();
		}
	}

	public void saveMixins()
	{
		MixinData data = new MixinData();

		for (Map.Entry<String, MixinPermission> group : mixins.entrySet())
			data.mixins.put(group.getKey(), new HolderData(group.getValue()));

		YamlConfigProvider.saveConfig(mixinsFile(), data);
	}

	public void reloadGroups()
	{
		File file = groupsFile();
		if(file.exists())
		{
			for(PermUser group : groups.values())
				group.clearAll();

			GroupsData data = YamlConfigProvider.getOrCreateConfig(file, GroupsData.class);
			for(Map.Entry<String, UserData> groupData : data.groups.entrySet())
				fillContainer(getOrCreateGroup(groupData.getKey()), groupData.getValue());
		}
		else
		{
			saveGroups();
		}
	}
	
	public void saveGroups()
	{
		GroupsData data = new GroupsData();

		for (PermUser group : groups.values())
			data.groups.put(group.getName(), serializeContainer(group));

		YamlConfigProvider.saveConfig(groupsFile(), data);
	}
	
	public void saveUsers()
	{
		UsersData data = new UsersData();

		for (PermUser user : users.values())
			data.users.put(user.getName(), serializeContainer(user));

		YamlConfigProvider.saveConfig(usersFile(), data);
	}

	public void reloadUsers()
	{
		for(PermUser user : users.values())
			user.clearAll();
		users.clear();

		UsersData data = YamlConfigProvider.getOrCreateConfig(usersFile(), UsersData.class);
		for(Map.Entry<String, UserData> userData : data.users.entrySet())
		{
			PermUser container = new PermUser(userData.getKey());
			fillContainer(container, userData.getValue());
			users.put(userData.getKey().toLowerCase(), container);
		}
	}

	private void fillContainer(PermUser container, UserData data)
	{
		if(!StringUtils.isEmpty(data.inherits))
			container.setParent(permissionRepository.getContainer(data.inherits));

		fillHolder(container.getGlobalHolder(), data.global);
		for(Map.Entry<String, HolderData> worldData : data.worlds.entrySet())
			fillHolder(container.getOrCreateWorldHolder(worldData.getKey()), worldData.getValue());
	}

	private void fillHolder(PermissionHolder holder, HolderData data)
	{
		holder.setInnerMeta(data.meta);
		for (String pKey : data.permissions)
			holder.addPermission(permissionRepository.getPermission(pKey));
	}
	
	private UserData serializeContainer(PermUser group)
	{
		UserData user = new UserData();
		user.inherits = group.getParentName();
		user.global = new HolderData(group.getGlobalHolder());
		for(Map.Entry<String, PermissionHolder> world : group.getInnerWorlds().entrySet())
			user.worlds.put(world.getKey(), new HolderData(world.getValue()));
		return user;
	}

	public static String fixMixinKey(String key)
	{
		if (key.startsWith(MIXIN_PREFIX))
			return key;
		else
			return MIXIN_PREFIX + key;
	}

	private MixinPermission getOrCreateMixin(String name)
	{
		String groupKey = fixMixinKey(name);
		MixinPermission mixin = mixins.get(groupKey);
		if (mixin == null)
		{
			mixin = new MixinPermission(groupKey);
			permissionRepository.registerPermission(mixin);
			mixins.put(groupKey, mixin);
		}

		return mixin;
	}

	private PermUser getOrCreateGroup(String name)
	{
		name = name.toLowerCase();
		PermUser container = groups.get(name);
		if(container == null)
		{
			container = new PermUser(name);
			permissionRepository.registerContainer(name, container);
			groups.put(name, container);
		}
		return container;
	}

	private PermUser getOrCreateUser(String name)
	{
		name = name.toLowerCase();
		PermUser container = users.get(name);
		if(container == null)
		{
			container = new PermUser(name);
			container.setParent(defaultGroup);
			users.put(name, container);
		}
		return container;
	}

	private File groupsFile()
	{
		return new File(configDir, GROUPS_CONFIG);
	}

	private File usersFile()
	{
		return new File(configDir, "users.yml");
	}

	private File mixinsFile()
	{
		return new File(configDir, MIXINS_CONFIG);
	}

	public static class HolderData
	{
		public Map<String, String> meta;
		public List<String> permissions;

		public HolderData()
		{
			meta = new HashMap<String, String>();
			permissions = new ArrayList<String>();
		}

		public HolderData(PermissionHolder holder)
		{
			meta = holder.getInnerMeta();
			permissions = holder.getInnerPermissions();
		}

		public boolean isEmpty()
		{
			return permissions.isEmpty() && meta.isEmpty();
		}
	}

	public static class UserData
	{
		public String inherits = "";
		public HolderData global = new HolderData();
		public Map<String, HolderData> worlds = new LinkedHashMap<String, HolderData>();
	}

	public static class GroupsData
	{
		public Map<String, UserData> groups = new LinkedHashMap<String, UserData>();
	}

	public static class UsersData
	{
		public Map<String, UserData> users = new LinkedHashMap<String, UserData>();
	}

	public static class MixinData
	{
		public Map<String, HolderData> mixins = new LinkedHashMap<String, HolderData>();
	}
}
