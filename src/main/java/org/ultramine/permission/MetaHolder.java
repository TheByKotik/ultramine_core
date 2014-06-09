package org.ultramine.permission;

import java.util.HashMap;
import java.util.Map;

public abstract class MetaHolder
{
	protected Map<String, String> innerMeta;

	public MetaHolder()
	{
		innerMeta = new HashMap<String, String>();
	}

	public MetaHolder(Map<String, String> meta)
	{
		setInnerMeta(meta);
	}

	public void setMeta(String key, String value)
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
		return getMetaResolver().getInt("priority");
	}

	public Map<String, String> getInnerMeta()
	{
		return new HashMap<String, String>(innerMeta);
	}

	public void setInnerMeta(Map<String, String> meta)
	{
		innerMeta = new HashMap<String, String>(meta);
	}

	public abstract MetaResolver getMetaResolver();
}
