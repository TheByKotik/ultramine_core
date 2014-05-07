package org.ultramine.permission;

import java.util.Map;

/**
 * Created by Евгений on 02.05.2014.
 */
public interface IPermission
{
	public String getKey();
	public String getName();
	public String getDescription();
	public int getPriority();

	public PermissionResolver getResolver();
	public Map<String, Object> getEffectiveMeta();
}