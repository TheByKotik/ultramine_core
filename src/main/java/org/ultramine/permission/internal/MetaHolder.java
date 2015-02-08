package org.ultramine.permission.internal;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class MetaHolder
{
	private Map<String, String> innerMeta;

	public MetaHolder()
	{
		innerMeta = new LinkedHashMap<String, String>();
	}

	public MetaHolder(Map<String, String> meta)
	{
		setInnerMeta(meta);
	}

	public String getMeta(String key)
	{
		return getMetaResolver().getString(key);
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

	public Map<String, String> getInnerMeta()
	{
		return new LinkedHashMap<String, String>(innerMeta);
	}

	public void setInnerMeta(Map<String, String> meta)
	{
		innerMeta = new LinkedHashMap<String, String>(meta);
	}

	protected abstract MetaResolver getMetaResolver();

	protected void mergeInnerMeta()
	{
		getMetaResolver().merge(innerMeta, Integer.MAX_VALUE);
	}
}
