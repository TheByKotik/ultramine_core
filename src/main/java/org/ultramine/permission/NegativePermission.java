package org.ultramine.permission;

public class NegativePermission implements IChangeablePermission
{
	private IPermission permission;
	private boolean isChangeable;
	private PermissionResolver resolver;

	public NegativePermission(IPermission permission)
	{
		this.permission = permission;
		this.resolver = PermissionResolver.createInverted(permission.getPermissions());
		this.isChangeable = permission instanceof IChangeablePermission;
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
	public PermissionResolver getPermissions()
	{
		if (isDirty())
			resolver = PermissionResolver.createInverted(permission.getPermissions());
		return resolver;
	}

	@Override
	public MetaResolver getMeta()
	{
		return permission.getMeta();
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
