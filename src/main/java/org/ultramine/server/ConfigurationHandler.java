package org.ultramine.server;

import java.io.File;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

public class ConfigurationHandler
{
	private static File settingsDir = new File(FMLLaunchHandler.getMinecraftHome(), "settings");
	private static File worldsDir = new File(FMLLaunchHandler.getMinecraftHome(), "worlds");
	
	static
	{
		if(!settingsDir.exists()) settingsDir.mkdir();
		if(!worldsDir.exists()) worldsDir.mkdir();
	}
	
	public static File getSettingDir()
	{
		return settingsDir;
	}
	
	public static File getWorldsDir()
	{
		return worldsDir;
	}
}
