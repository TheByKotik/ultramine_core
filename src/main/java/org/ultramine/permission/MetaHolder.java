package org.ultramine.permission;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Евгений on 08.05.2014.
 */
public abstract class MetaHolder
{
	protected Map<String, Object> innerMeta;

	public MetaHolder()
	{
		innerMeta = new HashMap<String, Object>();
	}

	public MetaHolder(Map<String, Object> meta)
	{
		innerMeta = meta;
	}

	public void setValue(String key, Object value)
	{
		innerMeta.put(key, value);
	}

	public void removeValue(String key)
	{
		innerMeta.remove(key);
	}

	public String getString(String key)
	{
		Map<String, Object> meta = getEffectiveMeta();
		if (meta.containsKey(key))
			return (String)meta.get(key);
		else
			return "";
	}

	public int getInt(String key)
	{
		Map<String, Object> meta = getEffectiveMeta();
		if (meta.containsKey(key))
			return (Integer)meta.get(key);
		else
			return 0;
	}

	public int getPriority()
	{
		return getInt("priority");
	}

	public abstract Map<String, Object> getEffectiveMeta();
}
