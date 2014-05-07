package org.ultramine.permission;

import java.util.Map;

/**
 * Created by Евгений on 07.05.2014.
 */
public interface IChangeablePermission extends IPermission
{
	public boolean isDirty();
	public void subscribe(IDirtyListener listener);
	public void unsubscribe(IDirtyListener listener);
}
