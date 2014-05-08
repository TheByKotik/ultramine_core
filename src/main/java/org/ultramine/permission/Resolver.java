package org.ultramine.permission;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.HashMap;
import java.util.Map;

public class Resolver<T>
{
	protected Map<String, T> values = new HashMap<String, T>();
	protected TObjectIntHashMap<String> priorities = new TObjectIntHashMap<String>();

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
