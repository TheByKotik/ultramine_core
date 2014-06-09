package org.ultramine.permission;

public interface IPermission
{
	public String getKey();
	public int getPriority();

	public CheckResult check(String key);
	public String getMeta(String key);

	public void mergeTo(PermissionResolver resolver);
	public void mergeTo(MetaResolver resolver);

	public void subscribe(IDirtyListener listener);
	public void unsubscribe(IDirtyListener listener);
}