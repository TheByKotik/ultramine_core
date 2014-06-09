package org.ultramine.permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermissionRepository
{
	private Set<String> registeredPermissions;
	private Map<String, ProxyPermission> proxyPermissions;

	public PermissionRepository()
	{
		registeredPermissions = new HashSet<String>();
		proxyPermissions = new HashMap<String, ProxyPermission>();
	}

	public PermissionRepository(PermissionRepository anotherRepository)
	{
		registeredPermissions = new HashSet<String>(anotherRepository.registeredPermissions);
		proxyPermissions = new HashMap<String, ProxyPermission>(anotherRepository.proxyPermissions);
	}

	public ProxyPermission getPermission(String key)
	{
		key = key.toLowerCase();

		if (!proxyPermissions.containsKey(key))
		{
			if (key.startsWith("^"))
			{
				proxyPermissions.put(key, new NegativePermission(key));
				registeredPermissions.add(key);
			}
			else
				proxyPermissions.put(key, new ProxyPermission(key));
		}

		return proxyPermissions.get(key);
	}

	public ProxyPermission registerPermission(IPermission permission)
	{
		if (registeredPermissions.contains(permission.getKey()))
			throw new IllegalArgumentException("Permission already registered");

		if (permission.getKey().startsWith("^"))
			throw new IllegalArgumentException("^* names are reserved");

		if (permission instanceof ProxyPermission)
		{
			proxyPermissions.put(permission.getKey(), (ProxyPermission)permission);
			return (ProxyPermission)permission;
		}

		ProxyPermission proxy = getPermission(permission.getKey());
		proxy.link(permission);

		registeredPermissions.add(permission.getKey());
		return proxy;
	}

	public static class ProxyPermission implements IPermission
	{
		private IPermission wrappedPermission;
		private boolean isDummy;
		private List<IDirtyListener> listeners;

		public ProxyPermission(String key)
		{
			this.wrappedPermission = new DummyPermission(key);
			this.isDummy = true;
			listeners = new ArrayList<IDirtyListener>();
		}

		public ProxyPermission(IPermission permission)
		{
			this.wrappedPermission = permission;
			this.isDummy = false;
			listeners = new ArrayList<IDirtyListener>();
		}

		@Override
		public String getKey()
		{
			return wrappedPermission.getKey();
		}

		public IPermission getWrappedPermission()
		{
			return wrappedPermission;
		}

		@Override
		public int getPriority()
		{
			return wrappedPermission.getPriority();
		}

		@Override
		public CheckResult check(String key)
		{
			return wrappedPermission.check(key);
		}

		@Override
		public String getMeta(String key)
		{
			return wrappedPermission.getMeta(key);
		}

		@Override
		public void mergeTo(PermissionResolver resolver)
		{
			wrappedPermission.mergeTo(resolver);
		}

		@Override
		public void mergeTo(MetaResolver resolver)
		{
			wrappedPermission.mergeTo(resolver);
		}

		@Override
		public void subscribe(IDirtyListener listener)
		{
			if (isDummy)
				listeners.add(listener);
			else
				wrappedPermission.subscribe(listener);
		}

		@Override
		public void unsubscribe(IDirtyListener listener)
		{
			if (isDummy)
				listeners.remove(listener);
			else
				wrappedPermission.unsubscribe(listener);
		}

		private void link(IPermission permission)
		{
			wrappedPermission = permission;
			for (IDirtyListener listener : listeners)
			{
				permission.subscribe(listener);
				listener.makeDirty();
			}
			listeners = null;
			isDummy = false;
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

	private class NegativePermission extends ProxyPermission
	{
		private String key;

		public NegativePermission(String key)
		{
			super(getPermission(key.substring(1)));
			this.key = key;
		}

		@Override
		public String getKey()
		{
			return key;
		}

		@Override
		public CheckResult check(String key)
		{
			return super.check(key).invert();
		}

		@Override
		public String getMeta(String key)
		{
			return "";
		}
	}
}