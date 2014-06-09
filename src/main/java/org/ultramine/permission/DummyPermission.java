package org.ultramine.permission;

public class DummyPermission implements IPermission
{
	private String key;

	public DummyPermission(String key)
	{
		this.key = key.toLowerCase();
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public int getPriority()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public CheckResult check(String key)
	{
		if (key == null)
			return CheckResult.UNRESOLVED;

		key = key.toLowerCase();
		if (key.equals(getKey()) || "*".equals(getKey()))
			return CheckResult.TRUE;

		if (getKey().endsWith(".*"))
		{
			String base = getKey().substring(0, getKey().length() - 3);
			if (key.startsWith(base))
				return CheckResult.TRUE;
		}

		return CheckResult.UNRESOLVED;
	}

	@Override
	public String getMeta(String key)
	{
		return "";
	}

	@Override
	public void mergeTo(PermissionResolver resolver)
	{
		resolver.merge(getKey(), true, getPriority());
	}

	@Override
	public void mergeTo(MetaResolver resolver)
	{
	}

	@Override
	public void subscribe(IDirtyListener listener)
	{
	}

	@Override
	public void unsubscribe(IDirtyListener listener)
	{
	}

	@Override
	public int hashCode()
	{
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof IPermission)
			return getKey().equals(((IPermission)obj).getKey());

		return super.equals(obj);
	}
}
