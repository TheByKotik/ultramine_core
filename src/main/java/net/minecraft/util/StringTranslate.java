package net.minecraft.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class StringTranslate
{
	private static final Pattern numericVariablePattern = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
	private static final Splitter equalSignSplitter = Splitter.on('=').limit(2);
	private final Map languageList;
	private static StringTranslate instance = new StringTranslate();
	private long lastUpdateTimeInMilliseconds;
	private static final String __OBFID = "CL_00001212";

	public StringTranslate()
	{
		InputStream inputstream = StringTranslate.class.getResourceAsStream("/assets/minecraft/lang/en_US.lang");
		languageList = Maps.newHashMap();
		inject(this, inputstream);
	}

	public static void inject(InputStream inputstream)
	{
		inject(instance, inputstream);
	}

	private static void inject(StringTranslate inst, InputStream inputstream)
	{
		HashMap<String, String> map = parseLangFile(inputstream);
		inst.languageList.putAll(map);
		inst.lastUpdateTimeInMilliseconds = System.currentTimeMillis();
	}

	public static HashMap<String,String> parseLangFile(InputStream inputstream)
	{
		HashMap<String,String> table = Maps.newHashMap();
		try
		{
			Iterator iterator = IOUtils.readLines(inputstream, Charsets.UTF_8).iterator();

			while (iterator.hasNext())
			{
				String s = (String)iterator.next();

				if (!s.isEmpty() && s.charAt(0) != 35)
				{
					String[] astring = (String[])Iterables.toArray(equalSignSplitter.split(s), String.class);

					if (astring != null && astring.length == 2)
					{
						String s1 = astring[0];
						String s2 = numericVariablePattern.matcher(astring[1]).replaceAll("%$1s");
						table.put(s1, s2);
					}
				}
			}

		}
		catch (Exception ioexception)
		{
			;
		}
		return table;
	}

	static StringTranslate getInstance()
	{
		return instance;
	}

	@SideOnly(Side.CLIENT)

	public static synchronized void replaceWith(Map par0Map)
	{
		instance.languageList.clear();
		instance.languageList.putAll(par0Map);
		instance.lastUpdateTimeInMilliseconds = System.currentTimeMillis();
	}

	public synchronized String translateKey(String par1Str)
	{
		return this.tryTranslateKey(par1Str);
	}

	public synchronized String translateKeyFormat(String par1Str, Object ... par2ArrayOfObj)
	{
		String s1 = this.tryTranslateKey(par1Str);

		try
		{
			return String.format(s1, par2ArrayOfObj);
		}
		catch (IllegalFormatException illegalformatexception)
		{
			return "Format error: " + s1;
		}
	}

	private String tryTranslateKey(String par1Str)
	{
		String s1 = (String)this.languageList.get(par1Str);
		return s1 == null ? par1Str : s1;
	}

	public synchronized boolean containsTranslateKey(String par1Str)
	{
		return this.languageList.containsKey(par1Str);
	}

	public long getLastUpdateTimeInMilliseconds()
	{
		return this.lastUpdateTimeInMilliseconds;
	}
}