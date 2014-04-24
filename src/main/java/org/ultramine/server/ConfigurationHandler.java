package org.ultramine.server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

public class ConfigurationHandler
{
	public static Logger log = LogManager.getLogger();

	private static final Yaml YAML = new Yaml();
	
	private static File settingsDir = new File(FMLLaunchHandler.getMinecraftHome(), "settings");
	private static File worldsDir = new File(FMLLaunchHandler.getMinecraftHome(), "worlds");
	private static File serverConfigFile = new File(getSettingDir(), "server.yml");
	
	private static UltramineServerConfig serverConfig;
	
	static
	{
		if(!settingsDir.exists()) settingsDir.mkdir();
		if(!worldsDir.exists()) worldsDir.mkdir();
	}
	
	public static void load()
	{
		serverConfig = getOrCreateConfig(serverConfigFile, UltramineServerConfig.class);
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
	
	public static void saveServerConfig()
	{
		saveConfig(serverConfigFile, serverConfig);
	}

	private static <T> T getOrCreateConfig(File configFile, Class<T> clazz)
	{
		T ret;
		
		if(!configFile.exists())
		{
			try
			{
				ret = clazz.newInstance();
			}
			catch (Exception e)
			{
				throw new RuntimeException("impossible exception", e);
			}

			saveConfig(configFile, ret);
		}
		else
		{
			FileReader reader = null;
			try
			{
				reader = new FileReader(configFile);
				ret = YAML.loadAs(reader, clazz);
			}
			catch (IOException e)
			{
				throw new RuntimeException("Failed to read config: " + configFile.getPath(), e);
			}
			finally
			{
				try
				{
					reader.close();
				} catch (IOException ignored) {}
			}
		}
		
		return ret;
	}
	
	private static void saveConfig(File configFile, Object o)
	{
		FileWriter writer = null;
		try
		{
			configFile.createNewFile();
			writer = new FileWriter(configFile);
			writer.write(YAML.dumpAsMap(o));
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to save default config: " + configFile.getPath(), e);
		}
		finally
		{
			try
			{
				writer.close();
			} catch (IOException ignored) {}
		}
	}
}
