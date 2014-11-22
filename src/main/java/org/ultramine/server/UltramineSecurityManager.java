package org.ultramine.server;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.security.Permission;

import cpw.mods.fml.relauncher.FMLSecurityManager;

public class UltramineSecurityManager extends FMLSecurityManager
{
	private static final boolean PREVENT_FILESYSTEM_ACCESS = !Boolean.parseBoolean(System.getProperty("org.ultramine.security.allow_out_of_dir_access"));
	private static final String JAVA_HOME = System.getProperty("java.home");
	private final String dir;
	
	public UltramineSecurityManager()
	{
		super(true);
		try
		{
			dir = new File(".").getCanonicalFile().getParentFile().getPath();
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void checkPermission(Permission perm)
	{
		super.checkPermission(perm);
		if(PREVENT_FILESYSTEM_ACCESS && perm instanceof FilePermission)
		{
			String name = perm.getName();
			try
			{
				String path = new File(name).getCanonicalFile().getParentFile().getPath();
				if(!path.startsWith(dir) && !path.startsWith("/dev") && !path.startsWith(JAVA_HOME))
					throw new SecurityException("Illegal out-of-dir filesystem access: "+path);
			}
			catch(IOException e)
			{
				throw new RuntimeException("Failed to resolve canonical path for: "+name, e);
			}
		}
	}
}
