package org.ultramine.permission;

public interface IChangeablePermission extends IPermission
{
	public void subscribe(IDirtyListener listener);
	public void unsubscribe(IDirtyListener listener);
}
