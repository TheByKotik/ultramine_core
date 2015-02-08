package org.ultramine.permission;

public interface IPermissionManager
{
	public static final String GLOBAL_WORLD = "global";
	public static final String DEFAULT_GROUP_NAME = "default";

	public boolean has(String world, String player, String permission);

	public void add(String world, String player, String permission);

	public void addToMixin(String mixin, String permission);

	public void addToGroup(String group, String world, String permission);

	public void remove(String world, String player, String permission);

	public void removeFromMixin(String mixin, String permission);

	public void removeFromGroup(String group, String world, String permission);

	public String getMeta(String world, String player, String key);

	public void setMeta(String world, String player, String key, String value);

	public void setMixinMeta(String mixin, String key, String value);

	public void setGroupMeta(String group, String world, String key, String value);

	public void setUserGroup(String user, String group);

	public void setGroupInherits(String group, String parent);

	public void save();

	public void reload();

	public PermissionRepository getRepository();
}