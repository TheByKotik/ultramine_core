package org.ultramine.server.service;

import net.minecraftforge.common.MinecraftForge;
import org.ultramine.core.service.Service;
import org.ultramine.core.service.ServiceDelegate;
import org.ultramine.core.service.ServiceManager;
import org.ultramine.core.service.ServiceProviderLoader;
import org.ultramine.core.service.ServiceStateHandler;
import org.ultramine.core.service.ServiceSwitchEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UMServiceManager implements ServiceManager
{
	private final Map<Class<?>, ServiceWrapper> services = new HashMap<>();

	private @Nonnull <T> ServiceWrapper<T> getOrCreateService(Class<T> serviceClass)
	{
		@SuppressWarnings("unchecked")
		ServiceWrapper<T> service = services.get(serviceClass);
		if(service == null)
		{
			Service desc = serviceClass.getAnnotation(Service.class);
			if(desc == null)
				throw new IllegalArgumentException("Given class is not a service class: "+serviceClass.getName());
			ServiceDelegate<T> delegate;
			try {
				delegate = ServiceDelegateGenerator.makeServiceDelegate(getClass(), serviceClass.getSimpleName() + "_delegate", serviceClass).newInstance();
			} catch(InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			service = new ServiceWrapper<>(serviceClass, delegate, desc);
			services.put(serviceClass, service);
		}
		return service;
	}

	@Override
	public <T> void register(@Nonnull Class<T> serviceClass, @Nonnull T provider, int priority)
	{
		serviceClass.getClass(); // NPE
		provider.getClass(); // NPE
		register(serviceClass, new SimpleServiceProviderLoader<>(provider), priority);
	}

	@Override
	public <T> void register(@Nonnull Class<T> serviceClass, @Nonnull ServiceProviderLoader<T> providerLoader, int priority)
	{
		serviceClass.getClass(); // NPE
		providerLoader.getClass(); // NPE
		ServiceWrapper<T> service = getOrCreateService(serviceClass);
		service.addProvider(new ServiceProviderRegistration<>(providerLoader, priority));
	}

	@Nonnull
	@Override
	public <T> T provide(@Nonnull Class<T> service)
	{
		return getOrCreateService(service).provide();
	}

	private static class ServiceWrapper<T>
	{
		private final Class<T> serviceClass;
		private final ServiceDelegate<T> delegate;
		private final Service desc;
		private final List<ServiceProviderRegistration<T>> providers = new ArrayList<>();
		private ServiceProviderRegistration<T> currentProvider;

		public ServiceWrapper(Class<T> serviceClass, ServiceDelegate<T> delegate, Service desc)
		{
			this.serviceClass = serviceClass;
			this.delegate = delegate;
			this.desc = desc;
		}

		public Service getDesc()
		{
			return desc;
		}

		private void switchTo(ServiceProviderRegistration<T> newProvider)
		{
			if(providers.isEmpty())
				throw new IllegalStateException("Service provider is not registered");
			ServiceProviderRegistration<T> oldProvider = currentProvider;
			MinecraftForge.EVENT_BUS.post(new ServiceSwitchEvent.Pre(serviceClass, delegate, oldProvider == null ? null : oldProvider.providerLoader, newProvider.providerLoader));
			if(oldProvider != null)
			{
				if(delegate.getProvider() instanceof ServiceStateHandler)
					((ServiceStateHandler)delegate.getProvider()).onDisabled();
				oldProvider.providerLoader.unload();
			}
			newProvider.providerLoader.load(delegate);
			if(delegate.getProvider() instanceof ServiceStateHandler)
				((ServiceStateHandler)delegate.getProvider()).onEnabled();
			currentProvider = newProvider;
			MinecraftForge.EVENT_BUS.post(new ServiceSwitchEvent.Post(serviceClass, delegate, oldProvider == null ? null : oldProvider.providerLoader, newProvider.providerLoader));
		}

		public void addProvider(ServiceProviderRegistration<T> provider)
		{
			if(desc.singleProvider() && providers.size() != 0)
				throw new IllegalStateException("Tried to register second provider for single-impl service'"+serviceClass.getName() +
						"'. First provider: " + providers.get(0).providerLoader + ", second provider: " + provider);
			providers.add(provider);
			if(currentProvider == null || provider.priority >= currentProvider.priority)
				switchTo(provider);
		}

		public T provide()
		{
			return delegate.asService();
		}
	}

	private static class ServiceProviderRegistration<T> implements Comparable<ServiceProviderRegistration>
	{
		public final ServiceProviderLoader<T> providerLoader;
		public final int priority;

		private ServiceProviderRegistration(ServiceProviderLoader<T> providerLoader, int priority)
		{
			this.providerLoader = providerLoader;
			this.priority = priority;
		}

		public int compareTo(ServiceProviderRegistration o)
		{
			return Integer.compare(priority, o.priority);
		}
	}

	private static class SimpleServiceProviderLoader<T> implements ServiceProviderLoader<T>
	{
		public final T provider;

		private SimpleServiceProviderLoader(T provider)
		{
			this.provider = provider;
		}

		@Override
		public void load(ServiceDelegate<T> service)
		{
			service.setProvider(provider);
		}

		@Override
		public void unload()
		{

		}

		@Override
		public String toString()
		{
			return "SimpleServiceProviderLoader{" +
					"provider=" + provider +
					'}';
		}
	}
}
