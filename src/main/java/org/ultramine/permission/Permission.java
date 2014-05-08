package org.ultramine.permission;

/**
 * Created by Евгений on 07.05.2014.
 */
public class Permission implements IPermission
{
	private String key;
	private String name;
	private String description;
	private PermissionResolver resolver;

	public Permission(String key, String name, String description)
	{
		this.key = key.toLowerCase();
		this.name = name;
		this.description = description;
		this.resolver = PermissionResolver.createForKey(key, getPriority());
	}

	public Permission(String key)
	{
		this(key, key, "");
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public int getPriority()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public PermissionResolver getPermissions()
	{
		return resolver;
	}

	@Override
	public MetaResolver getMeta()
	{
		return MetaResolver.BLANK_RESOLVER;
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
