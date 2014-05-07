package org.ultramine.permission;

import java.util.Map;

/**
 * Created by Евгений on 07.05.2014.
 */
public class NegativePermission implements IChangeablePermission
{
	private IPermission permission;
	private boolean isChangeable;
	private PermissionResolver resolver;

	public NegativePermission(IPermission permission)
	{
		this.permission = permission;
		this.resolver = PermissionResolver.createInverted(permission.getResolver());
		this.isChangeable = false;
	}

	public NegativePermission(IChangeablePermission permission)
	{
		this.permission = permission;
		this.resolver = PermissionResolver.createInverted(permission.getResolver());
		this.isChangeable = true;
	}

	@Override
	public String getKey()
	{
		return "^" + permission.getKey();
	}

	@Override
	public String getName()
	{
		return "NOT: " + permission.getName();
	}

	@Override
	public String getDescription()
	{
		if (!permission.getDescription().isEmpty())
			return "NOT: " + permission.getDescription();
		else
			return "";
	}

	@Override
	public int getPriority()
	{
		return permission.getPriority();
	}

	@Override
	public PermissionResolver getResolver()
	{
		if (isDirty())
			resolver = PermissionResolver.createInverted(permission.getResolver());
		return resolver;
	}

	@Override
	public Map<String, Object> getEffectiveMeta()
	{
		return permission.getEffectiveMeta();
	}

	@Override
	public boolean isDirty()
	{
		return isChangeable && ((IChangeablePermission) permission).isDirty();
	}

	@Override
	public void subscribe(IDirtyListener listener)
	{
		if (isChangeable)
			((IChangeablePermission) permission).subscribe(listener);
	}

	@Override
	public void unsubscribe(IDirtyListener listener)
	{
		if (isChangeable)
			((IChangeablePermission) permission).unsubscribe(listener);
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
