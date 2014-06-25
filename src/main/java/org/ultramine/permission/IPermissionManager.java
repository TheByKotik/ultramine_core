package org.ultramine.permission;

import org.ultramine.permission.internal.UserContainer;

public interface IPermissionManager
{
	public boolean has(String world, String player, String permission);

	public void add(String world, String player, String permission);

	public void addToWorld(String world, String permission);

	public void addToGroup(String group, String permission);

	public void remove(String world, String player, String permission);

	public void removeFromWorld(String world, String permission);

	public void removeFromGroup(String group, String permission);

	public String getMeta(String world, String player, String key);

	public void setMeta(String world, String player, String key, String value);

	public void setWorldMeta(String world, String key, String value);

	public void setGroupMeta(String group, String key, String value);

	public void save();

	public void reload();

	public PermissionRepository getRepository();

	public UserContainer getWorldContainer(String world);
}