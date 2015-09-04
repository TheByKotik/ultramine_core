package org.ultramine.permission.internal;

import org.ultramine.permission.IDirtyListener;
import org.ultramine.permission.IPermission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PermissionHolder extends MetaHolder implements IDirtyListener
{
	private boolean dirty;

	private Map<String, IPermission> permissions = new LinkedHashMap<String, IPermission>();
	private PermissionResolver permissionResolver = new PermissionResolver();
	private MetaResolver metaResolver = new MetaResolver();

	public PermissionHolder()
	{
		super();
		this.dirty = false;
	}

	public PermissionHolder(Map<String, String> meta)
	{
		super(meta);
		this.dirty = true;
	}

	public CheckResult check(String key)
	{
		return getPermissionResolver().check(key);
	}

	public void addPermission(IPermission permission)
	{
		if (permissions.containsKey(permission.getKey()))
			return;

		permissions.put(permission.getKey(), permission);
		permission.subscribe(this);

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

		IPermission permission = permissions.remove(key);
		permission.unsubscribe(this);

		makeDirty();
	}

	public void clearPermissions()
	{
		for (IPermission permission : permissions.values())
			permission.unsubscribe(this);

		permissions.clear();
		makeDirty();
	}

	public List<String> getInnerPermissions()
	{
		return new ArrayList<String>(permissions.keySet());
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
	public void setMeta(String key, String value)
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
	
	public void setInnerMeta(Map<String, String> meta)
	{
		super.setInnerMeta(meta);
		makeDirty();
	}

	@Override
	public void clearMeta()
	{
		super.clearMeta();
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
			permission.mergePermissionsTo(permissionResolver);
			permission.mergeMetaTo(metaResolver);
		}

		mergeInnerMeta();
	}

	protected PermissionResolver getPermissionResolver()
	{
		if (isDirty())
			calculate();

		return permissionResolver;
	}

	@Override
	protected MetaResolver getMetaResolver()
	{
		if (isDirty())
			calculate();

		return metaResolver;
	}
}