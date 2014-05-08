package org.ultramine.permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionRepository
{
	private Map<String, IPermission> permissions = new HashMap<String, IPermission>();
	private Map<String, ProxyPermission> proxyPermissions = new HashMap<String, ProxyPermission>();

	public ProxyPermission getPermission(String key)
	{
		key = key.toLowerCase();
		if (!proxyPermissions.containsKey(key))
			proxyPermissions.put(key, new ProxyPermission(key));

		return proxyPermissions.get(key);
	}

	public void registerPermission(IPermission permission)
	{
		if (permissions.containsKey(permission.getKey()))
			throw new IllegalArgumentException("Permission already registered");

		ProxyPermission proxy = getPermission(permission.getKey());
		proxy.linkPermission(permission);
		permissions.put(permission.getKey(), permission);
	}

	public class ProxyPermission implements IChangeablePermission
	{
		private String key;
		private IPermission wrappedPermission;
		private boolean changeable;
		private List<IDirtyListener> listeners = new ArrayList<IDirtyListener>();

		private ProxyPermission(String key)
		{
			this.key = key;
			this.wrappedPermission = new Permission(key);
			this.changeable = false;
		}

		@Override
		public String getKey()
		{
			return key;
		}

		public IPermission getWrappedPermission()
		{
			return wrappedPermission;
		}

		public boolean isChangeable()
		{
			return changeable;
		}

		@Override
		public String getName()
		{
			return wrappedPermission.getName();
		}

		@Override
		public String getDescription()
		{
			return wrappedPermission.getDescription();
		}

		@Override
		public int getPriority()
		{
			return wrappedPermission.getPriority();
		}

		@Override
		public PermissionResolver getPermissions()
		{
			return wrappedPermission.getPermissions();
		}

		@Override
		public MetaResolver getMeta()
		{
			return wrappedPermission.getMeta();
		}

		@Override
		public boolean isDirty()
		{
			return changeable && ((IChangeablePermission)wrappedPermission).isDirty();
		}

		@Override
		public void subscribe(IDirtyListener listener)
		{
			if (changeable)
				((IChangeablePermission)wrappedPermission).subscribe(listener);
			else
				listeners.add(listener);
		}

		@Override
		public void unsubscribe(IDirtyListener listener)
		{
			if (changeable)
				((IChangeablePermission)wrappedPermission).unsubscribe(listener);
			else
				listeners.remove(listener);
		}

		private void linkPermission(IPermission permission)
		{
			wrappedPermission = permission;
			for (IDirtyListener listener : listeners)
				listener.makeDirty();
			listeners.clear();
		}

		private void linkPermission(IChangeablePermission permission)
		{
			wrappedPermission = permission;
			for (IDirtyListener listener : listeners)
			{
				permission.subscribe(listener);
				listener.makeDirty();
			}
			listeners.clear();
			this.changeable = true;
		}

		@Override
		public int hashCode()
		{
			return wrappedPermission.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			return wrappedPermission.equals(obj);
		}

		@Override
		public String toString()
		{
			return wrappedPermission.toString();
		}
	}
}
