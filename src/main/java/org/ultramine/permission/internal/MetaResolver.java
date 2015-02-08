package org.ultramine.permission.internal;

public class MetaResolver extends Resolver<String>
{
	public static final MetaResolver BLANK_RESOLVER = new MetaResolver();

	public MetaResolver()
	{
		super();
	}

	public String getString(String key)
	{
		return values.get(key);
	}

	public int getInt(String key)
	{
		if (values.containsKey(key))
		{
			try
			{
				return Integer.parseInt(values.get(key));
			}
			catch (Exception ignored)
			{
			}
		}

		return 0;
	}
}
