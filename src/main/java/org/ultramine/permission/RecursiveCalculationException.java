package org.ultramine.permission;

public class RecursiveCalculationException extends RuntimeException
{
	public RecursiveCalculationException(String location)
	{
		super("Recursive calculation detected in " + location);
	}

	public RecursiveCalculationException(GroupPermission groupPermission)
	{
		this(groupPermission.getKey());
	}
}
