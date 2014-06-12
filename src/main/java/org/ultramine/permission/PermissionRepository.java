package org.ultramine.permission;

import org.ultramine.permission.internal.CheckResult;
import org.ultramine.permission.internal.MetaResolver;
import org.ultramine.permission.internal.PermissionResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionRepository
{
	private Map<String, ProxyPermission> proxyPermissions;

	public PermissionRepository()
	{
		proxyPermissions = new HashMap<String, ProxyPermission>();
	}

	public PermissionRepository(PermissionRepository anotherRepository)
	{
		proxyPermissions = new HashMap<String, ProxyPermission>(anotherRepository.proxyPermissions);
	}

	public ProxyPermission getPermission(String key)
	{
		key = key.toLowerCase();

		ProxyPermission permission = proxyPermissions.get(key);
		if (permission == null)
		{
			if (key.startsWith("^"))
				permission = new NegativePermission(key, getPermission(key.substring(1)));

			else if (key.endsWith(".*") || key.equals("*"))
				permission = new ProxyPermission(new DummyPermission(key));

			else
				permission = new ProxyPermission(key);

			proxyPermissions.put(key, permission);
		}

		return permission;
	}

	public ProxyPermission registerPermission(IPermission permission)
	{
		ProxyPermission proxy = getPermission(permission.getKey());
		if (!proxy.isDummy())
			throw new IllegalArgumentException("Permission already registered");

		if (permission instanceof ProxyPermission)
		{
			proxyPermissions.put(permission.getKey(), (ProxyPermission)permission);
			return (ProxyPermission)permission;
		}
		else
		{
			proxy.link(permission);
			return proxy;
		}
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

		public boolean isDummy()
		{
			return isDummy;
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
		public void mergePermissionsTo(PermissionResolver resolver)
		{
			wrappedPermission.mergePermissionsTo(resolver);
		}

		@Override
		public void mergeMetaTo(MetaResolver resolver)
		{
			wrappedPermission.mergeMetaTo(resolver);
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
}