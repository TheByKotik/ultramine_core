package org.ultramine.permission;

import java.util.HashMap;
import java.util.Map;

public class PermissionHolder extends MetaHolder implements IDirtyListener
{
	private boolean dirty;

	private Map<String, IPermission> permissions = new HashMap<String, IPermission>();
	private PermissionResolver permissionResolver = new PermissionResolver();
	private MetaResolver metaResolver = new MetaResolver();

	public PermissionHolder()
	{
		super();
		this.dirty = false;
	}

	public PermissionHolder(Map<String, Object> meta)
	{
		super(meta);
		this.dirty = true;
	}

	public PermissionResolver getPermissions()
	{
		if (isDirty())
			calculate();

		return permissionResolver;
	}

	@Override
	public MetaResolver getMeta()
	{
		if (isDirty())
			calculate();

		return metaResolver;
	}

	public void addPermission(IPermission permission)
	{
		if (permissions.containsKey(permission.getKey()))
			return;

		permissions.put(permission.getKey(), permission);
		if (permission instanceof IChangeablePermission)
			((IChangeablePermission) permission).subscribe(this);

		makeDirty();
	}

	public void removePermission(IPermission permission)
	{
		removePermission(permission.getKey());
	}

	public void removePermission(String key)
	{
		if (!permissions.containsKey(key))
			return;

		IPermission perm = permissions.remove(key);
		if (perm instanceof IChangeablePermission)
			((IChangeablePermission) perm).unsubscribe(this);

		makeDirty();
	}

	public boolean isDirty()
	{
		return dirty;
	}

	@Override
	public void makeDirty()
	{
		dirty = true;
	}

	@Override
	public void setMeta(String key, Object value)
	{
		super.setMeta(key, value);
		makeDirty();
	}

	@Override
	public void removeMeta(String key)
	{
		super.removeMeta(key);
		makeDirty();
	}

	public void calculate()
	{
		if (!isDirty())
			return;
		dirty = false;

		permissionResolver.clear();
		metaResolver.clear();

		for (IPermission permission : permissions.values())
		{
			permissionResolver.merge(permission.getPermissions(), permission.getPriority());
			metaResolver.merge(permission.getMeta(), permission.getPriority());
		}

		metaResolver.merge(innerMeta, Integer.MAX_VALUE);
	}
}
