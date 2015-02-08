package org.ultramine.permission.internal;

import java.util.*;

public class PermissionResolver extends Resolver<Boolean>
{
	private final SortedMap<String, Boolean> wildcards = new TreeMap<String, Boolean>(Collections.reverseOrder());

	public static PermissionResolver createInverted(PermissionResolver anotherResolver)
	{
		PermissionResolver resolver = new PermissionResolver();
		for (Map.Entry<String, Boolean> entry : anotherResolver.values.entrySet())
			resolver.merge(entry.getKey(), !entry.getValue(), anotherResolver.priorities.get(entry.getKey()));

		return resolver;
	}

	public static PermissionResolver createForKey(String key, int priority)
	{
		PermissionResolver resolver = new PermissionResolver();
		resolver.merge(key, true, priority);
		return resolver;
	}

	public CheckResult check(String key)
	{
		if (key == null)
			return CheckResult.UNRESOLVED;

		Boolean val = values.get(key);
		if (val != null)
			return CheckResult.fromBoolean(val);

		if (wildcards.size() > 0)
		{
			for (Map.Entry<String, Boolean> entry : wildcards.entrySet())
			{
				if (key.startsWith(entry.getKey()))
					return CheckResult.fromBoolean(entry.getValue());
			}
		}

		return CheckResult.UNRESOLVED;
	}

	@Override
	public void clear()
	{
		super.clear();
		wildcards.clear();
	}

	@Override
	public boolean merge(String key, Boolean value, int priority)
	{
		boolean result = super.merge(key, value, priority);
		if (result && (key.endsWith(".*") || key.equals("*")))
			wildcards.put(key.substring(0, key.length() - 1), value);

		return result;
	}
}