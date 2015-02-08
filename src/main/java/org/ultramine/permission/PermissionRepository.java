package org.ultramine.permission;

import org.ultramine.permission.internal.AbstractResolver;
import org.ultramine.permission.internal.CheckResult;
import org.ultramine.permission.internal.MetaResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionRepository
{
	private final Map<String, ProxyPermission> proxyPermissions;
	private final Map<String, IPermissionContainer> proxyContainers;

	public PermissionRepository()
	{
		proxyPermissions = new HashMap<String, ProxyPermission>();
		proxyContainers = new HashMap<String, IPermissionContainer>();
	}

	public PermissionRepository(PermissionRepository anotherRepository)
	{
		proxyPermissions = new HashMap<String, ProxyPermission>(anotherRepository.proxyPermissions);
		proxyContainers = new HashMap<String, IPermissionContainer>(anotherRepository.proxyContainers);
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

	public void lockPermissions(String... permissions)
	{
		for (String permission : permissions)
			getPermission(permission).lock();
	}
	
	public IPermissionContainer getContainer(String name)
	{
		IPermissionContainer container = proxyContainers.get(name);
		if(container == null)
			proxyContainers.put(name, container = new ProxyContainer(name));
		return container;
	}
	
	public void registerContainer(String name, IPermissionContainer container)
	{
		IPermissionContainer old = proxyContainers.get(name);
		if(old == null)
			proxyContainers.put(name, container);
		else if(old instanceof ProxyContainer)
			((ProxyContainer)old).link(container);
		else
			throw new IllegalArgumentException("Container already registered");
			
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
		public void mergePermissionsTo(AbstractResolver<Boolean> resolver)
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

		private void lock()
		{
			listeners = null;
			isDummy = false;
		}

		private void link(IPermission permission)
		{
			wrappedPermission = permission;
			for (IDirtyListener listener : listeners)
			{
				permission.subscribe(listener);
				listener.makeDirty();
			}
			lock();
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

	private static class ProxyContainer implements IPermissionContainer
	{
		private final String name;
		private IPermissionContainer wrappedContainer;
		
		private ProxyContainer(String name)
		{
			this.name = name;
		}
	
		@Override
		public String getName()
		{
			return name;
		}
		
		@Override
		public CheckResult check(String worldname, String permissionKey)
		{
			return wrappedContainer != null ? wrappedContainer.check(worldname, permissionKey) : CheckResult.UNRESOLVED;
		}
		
		@Override
		public String getMeta(String worldname, String key)
		{
			return wrappedContainer != null ? wrappedContainer.getMeta(worldname, key) : null;
		}
	
		private void link(IPermissionContainer container)
		{
			this.wrappedContainer = container;
		}
	}
}