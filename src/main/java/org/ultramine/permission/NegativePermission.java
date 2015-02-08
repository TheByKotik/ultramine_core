package org.ultramine.permission;

import org.ultramine.permission.internal.AbstractResolver;
import org.ultramine.permission.internal.CheckResult;
import org.ultramine.permission.internal.MetaResolver;

class NegativePermission extends PermissionRepository.ProxyPermission
{
	private String key;

	public NegativePermission(String key, IPermission permission)
	{
		super(permission);
		this.key = key;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public CheckResult check(String key)
	{
		return super.check(key).invert();
	}

	@Override
	public String getMeta(String key)
	{
		return "";
	}

	@Override
	public void mergePermissionsTo(final AbstractResolver<Boolean> resolver)
	{
		super.mergePermissionsTo(new AbstractResolver<Boolean>()
		{
			@Override
			public boolean merge(String key, Boolean value, int priority)
			{
				return resolver.merge(key, !value, priority);
			}
		});
	}

	@Override
	public void mergeMetaTo(MetaResolver resolver)
	{
	}
}
