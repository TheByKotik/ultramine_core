package org.ultramine.permission.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.ultramine.permission.MixinPermission;
import org.ultramine.permission.IPermissionManager;
import org.ultramine.permission.PermissionRepository;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientPermissionManager implements IPermissionManager
{
	private PermissionRepository permissionRepository;
	private Map<String, MixinPermission> groups;
	private String owner;

	public ClientPermissionManager(String owner, PermissionRepository permissionRepository)
	{
		this.permissionRepository = permissionRepository;
		this.groups = new HashMap<String, MixinPermission>();
		this.owner = owner;
	}

	@Override
	public boolean has(String world, String player, String permission)
	{
		return player.equalsIgnoreCase(owner);
	}

	@Override
	public void add(String world, String player, String permission)
	{
		
	}

	@Override
	public void addToMixin(String group, String permission)
	{
		
	}

	@Override
	public void addToGroup(String group, String world, String permission)
	{
		
	}

	@Override
	public void remove(String world, String player, String permission)
	{
		
	}

	@Override
	public void removeFromMixin(String group, String permission)
	{
		
	}

	@Override
	public void removeFromGroup(String group, String world, String permission)
	{
		
	}

	@Override
	public String getMeta(String world, String player, String key)
	{
		return "";
	}

	@Override
	public void setMeta(String world, String player, String key, String value)
	{
		
	}

	@Override
	public void setMixinMeta(String group, String key, String value)
	{
		
	}

	@Override
	public void setGroupMeta(String group, String world, String key, String value)
	{
		
	}

	@Override
	public void setUserGroup(String user, String group)
	{
		
	}

	@Override
	public void setGroupInherits(String group, String parent)
	{
		
	}

	@Override
	public void save()
	{
	}

	@Override
	public void reload()
	{
	}

	@Override
	public PermissionRepository getRepository()
	{
		return permissionRepository;
	}
}
