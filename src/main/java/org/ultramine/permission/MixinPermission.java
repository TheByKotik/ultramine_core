package org.ultramine.permission;

import org.ultramine.permission.internal.AbstractResolver;
import org.ultramine.permission.internal.MetaResolver;
import org.ultramine.permission.internal.PermissionHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MixinPermission extends PermissionHolder implements IPermission
{
	private final String key;
	private final List<IDirtyListener> listeners = new ArrayList<IDirtyListener>();
	private int priority = Integer.MIN_VALUE;

	public MixinPermission(String key)
	{
		super();
		this.key = key.toLowerCase();
	}
	
	public MixinPermission(String key, int priority)
	{
		this(key);
		this.priority = priority;
	}

	public MixinPermission(String key, Map<String, String> meta)
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
	public void mergePermissionsTo(AbstractResolver<Boolean> resolver)
	{
		resolver.merge(getPermissionResolver(), getPriority());
	}

	@Override
	public void mergeMetaTo(MetaResolver resolver)
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
	public void calculate()
	{
		super.calculate();
		getPermissionResolver().merge(key, true, Integer.MAX_VALUE);
	}

	private int getPriority()
	{
		return priority != Integer.MIN_VALUE ? priority : getMetaResolver().getInt("priority");
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
