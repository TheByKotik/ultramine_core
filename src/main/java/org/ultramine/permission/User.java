package org.ultramine.permission;

import java.util.Map;

public class User extends PermissionHolder
{
	private final String name;

	public User(String name)
	{
		super();
		this.name = name.toLowerCase();
	}

	public User(String name, Map<String, Object> meta)
	{
		super(meta);
		this.name = name.toLowerCase();
	}

	public String getName()
	{
		return name;
	}

	@Override
	public int getPriority()
	{
		return getMeta().getInt("priority");
	}
}