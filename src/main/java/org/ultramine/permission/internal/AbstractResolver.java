package org.ultramine.permission.internal;

import java.util.Map;

public abstract class AbstractResolver<T>
{
	protected Map<String, T> getValues()
	{
		return null;
	}
	
	public final void merge(AbstractResolver<T> anotherResolver, int priority)
	{
		if (anotherResolver != null)
			merge(anotherResolver.getValues(), priority);
	}

	public final void merge(Map<String, T> newValues, int priority)
	{
		for (Map.Entry<String, T> entry : newValues.entrySet())
			merge(entry.getKey(), entry.getValue(), priority);

	}

	public abstract boolean merge(String key, T value, int priority);
	
}
