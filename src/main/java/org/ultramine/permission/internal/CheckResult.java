package org.ultramine.permission.internal;

public enum CheckResult
{
	TRUE, FALSE, UNRESOLVED;

	public static CheckResult fromBoolean(boolean value)
	{
		return value ? TRUE : FALSE;
	}

	public boolean asBoolean()
	{
		return this == TRUE;
	}

	public CheckResult invert()
	{
		switch (this)
		{
			case TRUE:
				return FALSE;
			case FALSE:
				return TRUE;
			default:
				return UNRESOLVED;
		}
	}
}