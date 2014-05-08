package org.ultramine.permission;

/**
 * Created by uguuseha on 08.05.14.
 */
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
