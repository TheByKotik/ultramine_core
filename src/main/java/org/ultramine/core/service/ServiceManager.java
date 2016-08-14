package org.ultramine.core.service;

import javax.annotation.Nonnull;

@Service
public interface ServiceManager
{
	<T> void register(@Nonnull Class<T> serviceClass, @Nonnull T provider, int priority);

	<T> void register(@Nonnull Class<T> serviceClass, @Nonnull ServiceProviderLoader<T> providerLoader, int priority);

	@Nonnull <T> T provide(@Nonnull Class<T> service);
}
