package org.ultramine.permission;

import java.util.Set;

public interface IPermissionHandler
{
	public boolean has(String world, String player, String permission);

	public void add(String world, String player, String permission);

	public void add(String world, String permission);

	public void remove(String world, String player, String permission);

	public void remove(String world, String permission);

	public MetaResolver getMeta(String world, String player);

	public void setMeta(String world, String player, String key, Object value);

	public Set<String> findUsersWithPermission(String world, String permission);

	public void save();

	public void reload();

	public PermissionRepository getRepository();
}