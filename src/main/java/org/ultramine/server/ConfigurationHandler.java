package org.ultramine.server;

import java.io.File;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.util.Resources;
import org.ultramine.server.util.YamlConfigProvider;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

public class ConfigurationHandler
{
	public static Logger log = LogManager.getLogger();
	
	private static File settingsDir = new File(FMLLaunchHandler.getMinecraftHome(), "settings");
	private static File worldsDir = new File(FMLLaunchHandler.getMinecraftHome(), "worlds");
	
	private static File serverConfigFile = new File(getSettingDir(), "server.yml");
	private static File worldsConfigFile = new File(getSettingDir(), "worlds.yml");
	
	private static UltramineServerConfig serverConfig;
	private static WorldsConfig worldsConfig;
	
	static
	{
		if(!settingsDir.exists()) settingsDir.mkdir();
		if(!worldsDir.exists()) worldsDir.mkdir();
	}
	
	public static void load()
	{
		serverConfig = YamlConfigProvider.getOrCreateConfig(serverConfigFile, UltramineServerConfig.class);
		
		if(!worldsConfigFile.exists())
			YamlConfigProvider.writeFile(worldsConfigFile, Resources.getAsString("/org/ultramine/defaults/defaultworlds.yml")
					.replace("{seed}", Long.toString(Math.abs(new Random().nextLong()))));
		worldsConfig = YamlConfigProvider.readConfig(worldsConfigFile, WorldsConfig.class);
	}
	
	public static File getSettingDir()
	{
		return settingsDir;
	}
	
	public static File getWorldsDir()
	{
		return worldsDir;
	}
	
	public static UltramineServerConfig getServerConfig()
	{
		return serverConfig;
	}
	
	public static WorldsConfig getWorldsConfig()
	{
		return worldsConfig;
	}
	
	public static void saveServerConfig()
	{
		YamlConfigProvider.saveConfig(serverConfigFile, serverConfig);
	}
}
