package org.ultramine.permission;

public class NegativePermission extends PermissionRepository.ProxyPermission implements IDirtyListener
{
	private PermissionResolver resolver;
	private boolean dirty;

	public NegativePermission(IPermission permission)
	{
		super(permission);
		super.subscribe(this);
		this.dirty = true;
	}

	@Override
	public String getKey()
	{
		return "^" + super.getKey();
	}

	@Override
	public String getName()
	{
		return "NOT: " + super.getName();
	}

	@Override
	public String getDescription()
	{
		if (!super.getDescription().isEmpty())
			return "NOT: " + super.getDescription();
		else
			return "";
	}

	@Override
	public PermissionResolver getPermissions()
	{
		if (dirty)
		{
			resolver = PermissionResolver.createInverted(super.getPermissions());
			dirty = false;
		}
		return resolver;
	}

	@Override
	public boolean isDirty()
	{
		return (getType() == ProxyType.CHANGEABLE) && dirty;
	}

	@Override
	public void makeDirty()
	{
		dirty = true;
	}
}
