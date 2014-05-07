package org.ultramine.permission;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Евгений on 07.05.2014.
 */
public class PermissionResolver
{
	private Map<String, Boolean> permissions;
	private Map<String, Integer> priorities;

	public static PermissionResolver createInverted(PermissionResolver anotherResolver)
	{
		PermissionResolver resolver = new PermissionResolver();
		for (Map.Entry<String, Boolean> entry : anotherResolver.permissions.entrySet())
		{
			resolver.permissions.put(entry.getKey(), !entry.getValue());
			resolver.priorities.put(entry.getKey(), anotherResolver.priorities.get(entry.getKey()));
		}
		return resolver;
	}

	public static PermissionResolver createForKey(String key, int priority)
	{
		PermissionResolver resolver = new PermissionResolver();
		resolver.permissions.put(key, true);
		resolver.priorities.put(key, priority);
		return resolver;
	}

	public PermissionResolver()
	{
		permissions = new HashMap<String, Boolean>();
		priorities = new HashMap<String, Integer>();
	}

	public boolean has(String key)
	{
		if (key == null)
			return false;

		key = key.toLowerCase();
		if (permissions.containsKey(key))
			return permissions.get(key);

		int index = key.lastIndexOf('.');
		while (index >= 0)
		{
			key = key.substring(0, index);
			String wildcard = key + ".*";
			if (permissions.containsKey(wildcard))
				return permissions.get(wildcard);

			index = key.lastIndexOf('.');
		}
		if (permissions.containsKey("*"))
			return permissions.get("*");

		return false;
	}

	public void clear()
	{
		permissions.clear();
	}

	public void merge(PermissionResolver anotherResolver, int priority)
	{
		for (String key : anotherResolver.permissions.keySet())
			if (!priorities.containsKey(key) || priorities.get(key) < priority)
			{
				permissions.put(key, anotherResolver.permissions.get(key));
				priorities.put(key, priority);
			}
	}
}
