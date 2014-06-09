package org.ultramine.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupPermission extends PermissionHolder implements IPermission
{
	private final String key;
	private final List<IDirtyListener> listeners = new ArrayList<IDirtyListener>();

	public GroupPermission(String key)
	{
		super();
		this.key = key.toLowerCase();
	}

	public GroupPermission(String key, Map<String, String> meta)
	{
		super(meta);
		this.key = key.toLowerCase();
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public CheckResult check(String key)
	{
		return getPermissionResolver().check(key);
	}

	@Override
	public String getMeta(String key)
	{
		return getMetaResolver().getString(key);
	}

	@Override
	public void mergeTo(PermissionResolver resolver)
	{
		resolver.merge(getPermissionResolver(), getPriority());
	}

	@Override
	public void mergeTo(MetaResolver resolver)
	{
		resolver.merge(getMetaResolver(), getPriority());
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
		if (isDirty())
			return;

		super.makeDirty();
		for (IDirtyListener listener : listeners)
			listener.makeDirty();
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
