package org.ultramine.permission;

import org.ultramine.permission.internal.PermissionHolder;

import java.util.Map;

public class User extends PermissionHolder
{
	private final String name;

	public User(String name)
	{
		super();
		this.name = name.toLowerCase();
	}

	public User(String name, Map<String, String> meta)
	{
		super(meta);
		this.name = name.toLowerCase();
	}

	public String getName()
	{
		return name;
	}
}