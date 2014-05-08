package org.ultramine.permission;

public class MetaResolver extends Resolver<Object>
{
	public static final MetaResolver BLANK_RESOLVER = new MetaResolver();

	public String getString(String key)
	{
		if (values.containsKey(key))
			return (String)values.get(key);
		else
			return "";
	}

	public int getInt(String key)
	{
		if (values.containsKey(key))
			return (Integer)values.get(key);
		else
			return 0;
	}


}
