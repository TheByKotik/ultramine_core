package org.ultramine.permission;

import java.util.HashMap;
import java.util.Map;

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

	public void setMeta(String key, Object value)
	{
		innerMeta.put(key, value);
	}

	public void removeMeta(String key)
	{
		innerMeta.remove(key);
	}

	public void clearMeta()
	{
		innerMeta.clear();
	}

	public int getPriority()
	{
		return getMeta().getInt("priority");
	}

	public Map<String, Object> getInnerMeta()
	{
		return new HashMap<String, Object>(innerMeta);
	}

	public abstract MetaResolver getMeta();
}
