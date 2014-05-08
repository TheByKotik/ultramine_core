package org.ultramine.permission;

import java.util.Map;

/**
 * Created by Евгений on 07.05.2014.
 */
public class PermissionResolver extends Resolver<Boolean>
{
	public static PermissionResolver createInverted(PermissionResolver anotherResolver)
	{
		PermissionResolver resolver = new PermissionResolver();
		for (Map.Entry<String, Boolean> entry : anotherResolver.values.entrySet())
		{
			resolver.values.put(entry.getKey(), !entry.getValue());
			resolver.priorities.put(entry.getKey(), anotherResolver.priorities.get(entry.getKey()));
		}
		return resolver;
	}

	public static PermissionResolver createForKey(String key, int priority)
	{
		PermissionResolver resolver = new PermissionResolver();
		resolver.values.put(key, true);
		resolver.priorities.put(key, priority);
		return resolver;
	}

	public boolean has(String key)
	{
		if (key == null)
			return false;

		key = key.toLowerCase();
		if (values.containsKey(key))
			return values.get(key);

		int index = key.lastIndexOf('.');
		while (index >= 0)
		{
			key = key.substring(0, index);
			String wildcard = key + ".*";
			if (values.containsKey(wildcard))
				return values.get(wildcard);

			index = key.lastIndexOf('.');
		}
		if (values.containsKey("*"))
			return values.get("*");

		return false;
	}
}
