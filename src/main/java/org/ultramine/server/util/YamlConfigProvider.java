package org.ultramine.server.util;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class YamlConfigProvider
{
	private static final Yaml YAML;
	
	static
	{
		PropertyUtils prorutils = new PropertyUtils();
		prorutils.setSkipMissingProperties(true);
		
		Constructor constructor = new Constructor();
		constructor.setPropertyUtils(prorutils);
		
		DumperOptions opts = new DumperOptions();
		opts.setIndent(4);
		
		YAML = new Yaml(constructor, new Representer(), opts);
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
			return readConfig(configFile, clazz);
		}

		return ret;
	}
	
	public static <T> T readConfig(File configFile, Class<T> clazz)
	{
		Reader reader = null;
		try
		{
			reader = new InputStreamReader(new FileInputStream(configFile), Charsets.UTF_8);
			return YAML.loadAs(reader, clazz);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to read config: " + configFile.getPath(), e);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}
	}
	
	public static void saveConfig(File configFile, Object o)
	{
		writeFile(configFile, YAML.dumpAsMap(o));
	}
	
	public static void writeFile(File configFile, String text)
	{
		try
		{
			FileUtils.write(configFile, text, Charsets.UTF_8);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to save config: " + configFile.getPath(), e);
		}
	}
}
