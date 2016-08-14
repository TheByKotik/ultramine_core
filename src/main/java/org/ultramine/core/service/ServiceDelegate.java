package org.ultramine.core.service;

import javax.annotation.Nullable;

public interface ServiceDelegate<T>
{
	void setProvider(T obj);

	@Nullable T getProvider();

	T asService();
}
