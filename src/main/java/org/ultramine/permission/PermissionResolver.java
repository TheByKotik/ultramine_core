package org.ultramine.permission;

import java.util.Map;

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

	public CheckResult check(String key)
	{
		if (key == null)
			return CheckResult.UNRESOLVED;

		key = key.toLowerCase();
		if (values.containsKey(key))
			return toCheckResult(values.get(key));

		int index = key.lastIndexOf('.');
		while (index >= 0)
		{
			key = key.substring(0, index);
			String wildcard = key + ".*";
			if (values.containsKey(wildcard))
				return toCheckResult(values.get(wildcard));

			index = key.lastIndexOf('.');
		}
		if (values.containsKey("*"))
			return toCheckResult(values.get("*"));

		return CheckResult.UNRESOLVED;
	}

	public enum CheckResult { TRUE, FALSE, UNRESOLVED }
	private CheckResult toCheckResult(boolean bool)
	{
		return bool ? CheckResult.TRUE : CheckResult.FALSE;
	}
}
