package org.ultramine.server;

import java.util.Map;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

@Mod
(
		modid =		"UltramineServer",
		name =		"Ultramine Server",
		version =	"1.0"
)
public class UltramineServerMod
{
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		ConfigurationHandler.load();
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		ConfigurationHandler.saveServerConfig();
	}
	
	@NetworkCheckHandler
	public boolean networkCheck(Map<String,String> map, Side side)
	{
		return true;
	}
}
