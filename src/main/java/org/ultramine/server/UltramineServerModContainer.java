package org.ultramine.server;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

import org.ultramine.commands.basic.VanillaCommands;
import org.ultramine.commands.syntax.DefaultCompleters;
import org.ultramine.permission.commands.BasicPermissionCommands;
import org.ultramine.permission.internal.OpPermissionProxySet;
import org.ultramine.server.data.player.PlayerCoreData;

public class UltramineServerModContainer extends DummyModContainer
{
	public UltramineServerModContainer()
	{
		super(new ModMetadata());
	    ModMetadata meta = getMetadata();
		meta.modId		= "UltramineServer";
		meta.name		= "Ultramine Server";
		meta.version	= "1.0";
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}
	
	@Subscribe
	public void modConstruction(FMLConstructionEvent evt)
	{
		NetworkRegistry.INSTANCE.register(this, this.getClass(), null, evt.getASMHarvestedData());
	}

	@Subscribe
	public void preInit(FMLPreInitializationEvent e)
	{
		if(e.getSide().isServer())
			ConfigurationHandler.load();
	}
	
	@Subscribe
	public void init(FMLInitializationEvent e)
	{
		UMEventHandler handler = new UMEventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
	}

	@Subscribe
	public void postInit(FMLPostInitializationEvent e)
	{
		if(e.getSide().isServer())
			ConfigurationHandler.saveServerConfig();
	}
	
	@Subscribe
	public void serverAboutToStart(FMLServerAboutToStartEvent e)
	{
		e.getServer().getMultiWorld().register();
	}
	
	@Subscribe
	public void serverStarting(FMLServerStartingEvent e)
	{
		e.getServer().getConfigurationManager().getDataLoader().registerPlayerDataExt(PlayerCoreData.class, "core");
		e.registerArgumentHandlers(DefaultCompleters.class);
		e.registerCommands(BasicPermissionCommands.class);
		e.registerCommands(VanillaCommands.class);

		e.getPermissionHandler().createGroup(OpPermissionProxySet.OP_GROUP, "*");
	}
	
	@Subscribe
	public void serverStarted(FMLServerStartedEvent e)
	{
		MinecraftServer.getServer().getConfigurationManager().getDataLoader().loadCache();
	}
	
	@Subscribe
	public void serverStopped(FMLServerStoppedEvent e)
	{
		MinecraftServer.getServer().getMultiWorld().unregister();
	}
	
	@NetworkCheckHandler
	public boolean networkCheck(Map<String,String> map, Side side)
	{
		return true;
	}

	@Override
	public File getSource()
	{
		return UltraminePlugin.location;
	}

	@Override
	public List<String> getOwnedPackages()
	{
		return ImmutableList.of(
			"org.ultramine.server"
		);
	}

	@Override
	public Object getMod()
	{
		return this;
	}
}
