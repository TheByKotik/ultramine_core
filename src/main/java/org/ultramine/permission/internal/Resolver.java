package org.ultramine.permission.internal;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.HashMap;
import java.util.Map;

public class Resolver<T>
{
	protected Map<String, T> values;
	protected TObjectIntHashMap<String> priorities;

	public Resolver()
	{
		values = new HashMap<String, T>();
		priorities = new TObjectIntHashMap<String>();
	}

	public void clear()
	{
		values.clear();
		priorities.clear();
	}

	public final void merge(Resolver<T> anotherResolver, int priority)
	{
		if (anotherResolver != null)
			merge(anotherResolver.values, priority);
	}

	public final void merge(Map<String, T> newValues, int priority)
	{
		for (Map.Entry<String, T> entry : newValues.entrySet())
			merge(entry.getKey(), entry.getValue(), priority);

	}

	public boolean merge(String key, T value, int priority)
	{
		if (!priorities.containsKey(key) || priorities.get(key) < priority)
		{
			values.put(key, value);
			priorities.put(key, priority);
			return true;
		}
		return false;
	}
}
