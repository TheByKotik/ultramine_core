package org.ultramine.permission.internal;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.HashMap;
import java.util.Map;

public class Resolver<T> extends AbstractResolver<T>
{
	protected final Map<String, T> values = new HashMap<String, T>();
	protected final TObjectIntHashMap<String> priorities = new TObjectIntHashMap<String>();

	public Resolver()
	{
	}

	public void clear()
	{
		values.clear();
		priorities.clear();
	}

	protected Map<String, T> getValues()
	{
		return values;
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
