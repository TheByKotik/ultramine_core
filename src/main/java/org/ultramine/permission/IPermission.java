package org.ultramine.permission;

/**
 * Created by Евгений on 02.05.2014.
 */
public interface IPermission
{
	public String getKey();
	public String getName();
	public String getDescription();
	public int getPriority();

	public PermissionResolver getPermissions();
	public MetaResolver getMeta();
}