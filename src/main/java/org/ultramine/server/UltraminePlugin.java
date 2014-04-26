package org.ultramine.server;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.io.File;
import java.util.Map;

public class UltraminePlugin implements IFMLLoadingPlugin
{
	public static File location;

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[0];
	}

	@Override
	public String getModContainerClass()
	{
		return "org.ultramine.server.UltramineServerModContainer";
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		location = (File)data.get("coremodLocation");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
