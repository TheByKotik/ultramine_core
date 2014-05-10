package org.ultramine.permission;

import java.util.HashMap;
import java.util.Map;

import static org.ultramine.permission.PermissionResolver.CheckResult;

public class UserContainer<T extends User>
{
	protected Map<String, T> users;
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

	public final boolean checkUserPermission(String userName, String permissionKey)
	{
		switch (check(userName, permissionKey))
		{
			case TRUE:
				return true;
			default:
				return false;
		}
	}

	protected CheckResult check(String userName, String permissionKey)
	{
		userName = userName.toLowerCase();
		CheckResult result = CheckResult.UNRESOLVED;

		if (parentContainer != null)
			result = parentContainer.check(userName, permissionKey);

		if (result == CheckResult.UNRESOLVED && contains(userName))
			result = get(userName).getPermissions().check(permissionKey);

		return result;
	}

	public T get(String name)
	{
		return users.get(name.toLowerCase());
	}

	public void add(T user)
	{
		if (users.containsKey(user.getName()))
			return;

		users.put(user.getName(), user);
	}

	public void remove(String name)
	{
		users.remove(name.toLowerCase());
	}

	public void remove(User user)
	{
		remove(user.getName());
	}

	public void clear()
	{
		users.clear();
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
