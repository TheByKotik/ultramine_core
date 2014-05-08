package org.ultramine.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupPermission extends PermissionHolder implements IChangeablePermission
{
	private final String key;
	private final List<IDirtyListener> listeners = new ArrayList<IDirtyListener>();

	public GroupPermission(String key)
	{
		super();
		this.key = key.toLowerCase();
	}

	public GroupPermission(String key, Map<String, Object> meta)
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
