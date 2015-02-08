package org.ultramine.permission.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ultramine.permission.IPermission;
import org.ultramine.permission.IPermissionContainer;
import org.ultramine.permission.IPermissionManager;
import org.ultramine.permission.MixinPermission;

public class PermUser implements IPermissionContainer
{
	private final String name;
	private final MixinPermission global = new MixinPermission("global_world_mixin", -1);
	private final Map<String, PermissionHolder> worlds = new LinkedHashMap<String, PermissionHolder>();
	private IPermissionContainer parentContainer;
	
	public PermUser(String name)
	{
		this.name = name;
		worlds.put(IPermissionManager.GLOBAL_WORLD, global);
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	public void setParent(IPermissionContainer parentContainer)
	{
		this.parentContainer = parentContainer;
	}
	
	public String getParentName()
	{
		return parentContainer != null ? parentContainer.getName() : "";
	}
	
	public boolean checkUserPermission(String worldname, String permissionKey)
	{
		return check(worldname, permissionKey).asBoolean();
	}
	
	public CheckResult check(String worldname, String permissionKey)
	{
		CheckResult result = CheckResult.UNRESOLVED;

		PermissionHolder world = worlds.get(worldname);
		if (world == null)
			world = global;

		result = world.check(permissionKey);

		if (result == CheckResult.UNRESOLVED && parentContainer != null)
			result = parentContainer.check(worldname, permissionKey);

		return result;
	}
	
	@Override
	public String getMeta(String worldname, String key)
	{
		String result = null;

		PermissionHolder world = worlds.get(worldname);
		if (world == null)
			world = global;

		result = world.getMeta(key);

		if (result == null && parentContainer != null)
			result = parentContainer.getMeta(worldname, key);

		return result;
	}
	
	public PermissionHolder getGlobalHolder()
	{
		return global;
	}
	
	public PermissionHolder getOrCreateWorldHolder(String worldname)
	{
		PermissionHolder holder = worlds.get(worldname);
		if(holder == null)
		{
			holder = new PermissionHolder();
			holder.addPermission(global);
			worlds.put(worldname, holder);
		}
		return holder;
	}
	
	public void addPermission(String worldname, IPermission permission)
	{
		getOrCreateWorldHolder(worldname).addPermission(permission);
	}

	public void removePermission(String worldname, String key)
	{
		PermissionHolder holder = worlds.get(worldname);
		if(holder != null)
			holder.removePermission(key);
	}
	
	public void setMeta(String worldname, String key, String value)
	{
		getOrCreateWorldHolder(worldname).setMeta(key, value);
	}

	public void clearAll()
	{
		global.clearPermissions();
		global.clearMeta();
		for(PermissionHolder holder : worlds.values())
		{
			holder.clearPermissions();
			holder.clearMeta();
		}
		worlds.clear();
		worlds.put(IPermissionManager.GLOBAL_WORLD, global);
	}
	
	public Map<String, PermissionHolder> getInnerWorlds()
	{
		Map<String, PermissionHolder> map = new LinkedHashMap<String, PermissionHolder>(worlds);
		map.remove(IPermissionManager.GLOBAL_WORLD);
		return map;
	}
}
