package org.ultramine.permission;

import org.ultramine.permission.internal.CheckResult;

public interface IPermissionContainer
{
	String getName();
	
	CheckResult check(String worldname, String permissionKey);
	
	String getMeta(String worldname, String key);
}
