package org.ultramine.server;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.io.File;
import java.util.Map;

import net.minecraft.launchwrapper.LaunchClassLoader;

public class UltraminePlugin implements IFMLLoadingPlugin
{
	public static File location;

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{
				"org.ultramine.server.asm.transformers.Compat172BlockSend"
		};
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
		((LaunchClassLoader)this.getClass().getClassLoader()).addTransformerExclusion("org.ultramine.server.asm.");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
