package org.ultramine.permission;

import org.ultramine.permission.internal.CheckResult;
import org.ultramine.permission.internal.MetaResolver;
import org.ultramine.permission.internal.PermissionResolver;

public interface IPermission
{
	public String getKey();
	public CheckResult check(String key);
	public String getMeta(String key);

	public void mergePermissionsTo(PermissionResolver resolver);
	public void mergeMetaTo(MetaResolver resolver);

	public void subscribe(IDirtyListener listener);
	public void unsubscribe(IDirtyListener listener);
}