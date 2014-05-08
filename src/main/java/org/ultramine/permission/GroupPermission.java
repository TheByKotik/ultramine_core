package org.ultramine.permission;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroupPermission extends MetaHolder implements IChangeablePermission, IDirtyListener
{
	private static Logger logger = LogManager.getLogger(GroupPermission.class);

	private String key;
	private boolean dirty;

	private PermissionResolver permissionResolver = new PermissionResolver();
	private List<IDirtyListener> listeners = new ArrayList<IDirtyListener>();
	private Map<String, IPermission> permissions = new HashMap<String, IPermission>();
	private MetaResolver metaResolver = new MetaResolver();

	public GroupPermission(String key)
	{
		super();
		this.key = key.toLowerCase();
		this.dirty = false;
	}

	public GroupPermission(String key, Map<String, Object> meta)
	{
		super(meta);
		this.key = key.toLowerCase();
		this.dirty = true;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public String getName()
	{
		if (innerMeta.containsKey("name"))
			return (String) innerMeta.get("name");
		else
			return key;
	}

	@Override
	public String getDescription()
	{
		if (innerMeta.containsKey("description"))
			return (String) innerMeta.get("description");
		else
			return "";
	}

	@Override
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

	@Override
	public boolean isDirty()
	{
		return dirty;
	}

	@Override
	public void subscribe(IDirtyListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void unsubscribe(IDirtyListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public void makeDirty()
	{
		logger.error(getName());
		if (isDirty())
			return;

		dirty = true;
		for (IDirtyListener listener : listeners)
			listener.makeDirty();
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

	@Override
	public int hashCode()
	{
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof IPermission)
			return getKey().equals(((IPermission) obj).getKey());

		return super.equals(obj);
	}
}
