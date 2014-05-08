package org.ultramine.permission;

import java.util.HashMap;
import java.util.Map;

public class UserContainer<T extends User>
{
	private Map<String, T> users;
	protected UserContainer parentContainer;

	public UserContainer()
	{
		users = new HashMap<String, T>();
	}

	public UserContainer(UserContainer parentContainer)
	{
		this();
		this.parentContainer = parentContainer;
	}

	public boolean checkUserPermission(String userName, String permissionKey)
	{
		if (parentContainer != null && parentContainer.contains(userName))
			switch (parentContainer.get(userName).getPermissions().check(permissionKey))
			{
				case TRUE: return true;
				case FALSE: return false;
			}

		if (contains(userName))
			switch (get(userName).getPermissions().check(permissionKey))
			{
				case TRUE: return true;
				case FALSE: return false;
			}

		return false;
	}

	public T get(String name)
	{
		return users.get(name);
	}

	public void add(T user)
	{
		if (users.containsKey(user.getName()))
			return;

		users.put(user.getName(), user);
	}

	public void remove(String name)
	{
		users.remove(name);
	}

	public void remove(User user)
	{
		remove(user.getName());
	}

	public boolean contains(String name)
	{
		return users.containsKey(name);
	}

	public boolean contains(User user)
	{
		return contains(user.getName());
	}
}
