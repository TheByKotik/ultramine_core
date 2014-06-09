package org.ultramine.permission;

public class MetaResolver extends Resolver<String>
{
	public static final MetaResolver BLANK_RESOLVER = new MetaResolver();

	public String getString(String key)
	{
		if (values.containsKey(key))
			return values.get(key);
		else
			return "";
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
