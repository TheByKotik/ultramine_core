package org.ultramine.permission;

public interface IPermission
{
	public String getKey();
	public String getName();
	public String getDescription();
	public int getPriority();

	public PermissionResolver getPermissions();
	public MetaResolver getMeta();
}