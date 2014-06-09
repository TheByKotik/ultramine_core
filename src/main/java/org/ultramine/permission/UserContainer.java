package org.ultramine.permission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		return check(userName, permissionKey) == CheckResult.TRUE;
	}

	protected CheckResult check(String userName, String permissionKey)
	{
		userName = userName.toLowerCase();
		CheckResult result = CheckResult.UNRESOLVED;

		if (parentContainer != null)
			result = parentContainer.check(userName, permissionKey);

		if (result == CheckResult.UNRESOLVED && contains(userName))
			result = get(userName).getPermissionResolver().check(permissionKey);

		return result;
	}

	public T get(String name)
	{
		return users.get(name.toLowerCase());
	}

	public Set<String> getAllWithPermission(String permission)
	{
		Set<String> result;
		if (parentContainer != null)
			result = parentContainer.getAllWithPermission(permission);
		else
			result = new HashSet<String>();

		for (User user : users.values())
		{
			if (user.getPermissionResolver().check(permission) == CheckResult.TRUE)
				result.add(user.getName());
		}

		return result;
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
