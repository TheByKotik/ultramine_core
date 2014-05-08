package org.ultramine.permission;

public interface IChangeablePermission extends IPermission
{
	public boolean isDirty();
	public void subscribe(IDirtyListener listener);
	public void unsubscribe(IDirtyListener listener);
}
