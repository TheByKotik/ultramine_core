package org.ultramine.permission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by uguuseha on 08.05.14.
 */
public class Resolver<T>
{
	protected Map<String, T> values = new HashMap<String, T>();
	protected Map<String, Integer> priorities = new HashMap<String, Integer>();

	public void clear()
	{
		values.clear();
		priorities.clear();
	}

	public void merge(Resolver<T> anotherResolver, int priority)
	{
		merge(anotherResolver.values, priority);
	}

	public void merge(Map<String, T> newValues, int priority)
	{
		for (String key : newValues.keySet())
			if (!priorities.containsKey(key) || priorities.get(key) < priority)
			{
				values.put(key, newValues.get(key));
				priorities.put(key, priority);
			}
	}
}
