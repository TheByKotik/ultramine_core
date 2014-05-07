package org.ultramine.permission;

import java.util.*;

/**
 * Created by Евгений on 08.05.2014.
 */
public class GroupPermission extends MetaHolder implements IChangeablePermission, IDirtyListener
{
	private String key;
	private boolean dirty;

	private PermissionResolver resolver = new PermissionResolver();
	private List<IDirtyListener> listeners = new ArrayList<IDirtyListener>();
	private Set<IPermission> permissions = new HashSet<IPermission>();
	private Map<String, Object> effectiveMeta = new HashMap<String, Object>();

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
	public PermissionResolver getResolver()
	{
		if (isDirty())
			calculate();

		return resolver;
	}

	@Override
	public Map<String, Object> getEffectiveMeta()
	{
		if (isDirty())
			calculate();

		return effectiveMeta;
	}

	public void addPermission(IPermission permission)
	{
		permissions.add(permission);
		makeDirty();
	}

	public void removePermission(IPermission permission)
	{
		permissions.remove(permission);
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
		if (isDirty())
			return;

		dirty = true;
		for (IDirtyListener listener : listeners)
			listener.makeDirty();
	}

	@Override
	public void setValue(String key, Object value)
	{
		super.setValue(key, value);
		makeDirty();
	}

	@Override
	public void removeValue(String key)
	{
		super.removeValue(key);
		makeDirty();
	}

	public void calculate()
	{
		if (!isDirty())
			return;

		resolver.clear();
		for (IPermission permission : permissions)
			resolver.merge(permission.getResolver(), permission.getPriority());

		effectiveMeta.clear();
		Map<String, Integer> priorities = new HashMap<String, Integer>();

		for (IPermission permission : permissions)
		{
			Map<String, Object> meta = permission.getEffectiveMeta();

			for (String key : meta.keySet())
				if (!priorities.containsKey(key) || permission.getPriority() > priorities.get(key))
				{
					effectiveMeta.put(key, meta.get(key));
					priorities.put(key, permission.getPriority());
				}
		}

		for (String key : innerMeta.keySet())
			effectiveMeta.put(key, innerMeta.get(key));

		this.dirty = false;
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
			return getKey().equals(((IPermission)obj).getKey());

		return super.equals(obj);
	}
}
