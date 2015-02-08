package org.ultramine.permission;

import org.ultramine.permission.internal.AbstractResolver;
import org.ultramine.permission.internal.CheckResult;
import org.ultramine.permission.internal.MetaResolver;

public interface IPermission
{
	public String getKey();
	public CheckResult check(String key);
	public String getMeta(String key);

	public void mergePermissionsTo(AbstractResolver<Boolean> resolver);
	public void mergeMetaTo(MetaResolver resolver);

	public void subscribe(IDirtyListener listener);
	public void unsubscribe(IDirtyListener listener);
}