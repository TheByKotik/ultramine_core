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
		if (permission instanceof IChangeablePermission)
			proxy.linkChangeable((IChangeablePermission)permission);
		else
			proxy.linkSimple(permission);

		permissions.put(permission.getKey(), permission);
	}

	public static class ProxyPermission implements IChangeablePermission
	{
		private String key;
		private IPermission wrappedPermission;
		private ProxyType proxyType;
		private List<IDirtyListener> listeners = new ArrayList<IDirtyListener>();

		private ProxyPermission(String key)
		{
			this.key = key;
			this.wrappedPermission = new Permission(key);
			this.proxyType = ProxyType.DUMMY;
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

		public ProxyType getType()
		{
			return proxyType;
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
			return (proxyType == ProxyType.CHANGEABLE) && ((IChangeablePermission)wrappedPermission).isDirty();
		}

		@Override
		public void subscribe(IDirtyListener listener)
		{
			switch (proxyType)
			{
				case CHANGEABLE:
					((IChangeablePermission)wrappedPermission).subscribe(listener);
					break;
				case DUMMY:
					listeners.add(listener);
					break;
			}
		}

		@Override
		public void unsubscribe(IDirtyListener listener)
		{
			switch (proxyType)
			{
				case CHANGEABLE:
					((IChangeablePermission)wrappedPermission).unsubscribe(listener);
					break;
				case DUMMY:
					listeners.remove(listener);
					break;
			}
		}

		private void linkSimple(IPermission permission)
		{
			wrappedPermission = permission;
			for (IDirtyListener listener : listeners)
				listener.makeDirty();
			listeners.clear();
			proxyType = ProxyType.SIMPLE;
		}

		private void linkChangeable(IChangeablePermission permission)
		{
			wrappedPermission = permission;
			for (IDirtyListener listener : listeners)
			{
				permission.subscribe(listener);
				listener.makeDirty();
			}
			listeners.clear();
			proxyType = ProxyType.CHANGEABLE;
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

		public static enum ProxyType { DUMMY, SIMPLE, CHANGEABLE }
	}
}
