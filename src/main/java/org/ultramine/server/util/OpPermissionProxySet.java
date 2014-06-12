package org.ultramine.server.util;

import org.ultramine.permission.internal.ServerPermissionManager;
import org.ultramine.server.PermissionHandler;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Never use it! For backward сompatibility only.
 */
@Deprecated
public class OpPermissionProxySet implements Set<String>
{
	public static final String OP_PERMISSION = "minecraft.op";

	@Override
	public int size()
	{
		return ops().size();
	}

	@Override
	public boolean isEmpty()
	{
		return ops().isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return ops().contains(o);
	}

	@Override
	public Iterator<String> iterator()
	{
		return ops().iterator();
	}

	@Override
	public Object[] toArray()
	{
		return ops().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return ops().toArray(a);
	}

	@Override
	public boolean add(String s)
	{
		PermissionHandler.getInstance().add("global", s, OP_PERMISSION);
		return true;
	}

	@Override
	public boolean remove(Object o)
	{
		PermissionHandler.getInstance().remove("global", o.toString(), OP_PERMISSION);
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return ops().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends String> c)
	{
		for (String user : c)
			add(user);
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		Set<String> ops = ops();
		for (String user : ops)
		{
			if (!c.contains(user))
				remove(user);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		for (Object user : c)
			remove(user);
		return true;
	}

	@Override
	public void clear()
	{
		Set<String> ops = ops();
		for (String user : ops)
			remove(user);
	}

	private Set<String> ops()
	{
		return PermissionHandler.getInstance()
				.getWorldContainer(ServerPermissionManager.GLOBAL_WORLD).getAllWithPermission(OP_PERMISSION);
	}
}
