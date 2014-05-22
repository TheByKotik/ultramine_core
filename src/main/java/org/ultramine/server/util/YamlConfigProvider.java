package org.ultramine.server.util;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class YamlConfigProvider
{
	private static final Yaml YAML;
	
	static
	{
		PropertyUtils prorutils = new PropertyUtils();
		prorutils.setSkipMissingProperties(true);
		
		Constructor constructor = new Constructor();
		constructor.setPropertyUtils(prorutils);
		
		YAML = new Yaml(constructor);
	}

	public static <T> T getOrCreateConfig(File configFile, Class<T> clazz)
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
					if (reader != null)
					{
						reader.close();
					}
				} catch (IOException ignored) {}
			}
		}

		return ret;
	}

	public static void saveConfig(File configFile, Object o)
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
				if (writer != null)
				{
					writer.close();
				}
			} catch (IOException ignored) {}
		}
	}
}
