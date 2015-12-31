package org.ultramine.server;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandHandler;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import org.ultramine.commands.CommandRegistry;
import org.ultramine.commands.basic.BasicCommands;
import org.ultramine.commands.basic.FastWarpCommand;
import org.ultramine.commands.basic.OpenInvCommands;
import org.ultramine.commands.basic.TechCommands;
import org.ultramine.commands.basic.VanillaCommands;
import org.ultramine.commands.syntax.DefaultCompleters;
import org.ultramine.economy.EconomyCommands;
import org.ultramine.permission.IPermissionManager;
import org.ultramine.permission.MinecraftPermissions;
import org.ultramine.permission.commands.BasicPermissionCommands;
import org.ultramine.server.chunk.ChunkProfiler;
import org.ultramine.server.data.Databases;
import org.ultramine.server.data.ServerDataLoader;
import org.ultramine.server.data.player.PlayerCoreData;
import org.ultramine.server.event.ForgeModIdMappingEvent;
import org.ultramine.server.tools.ButtonCommand;
import org.ultramine.server.tools.ItemBlocker;
import org.ultramine.server.tools.WarpProtection;
import org.ultramine.server.util.GlobalExecutors;
import org.ultramine.server.util.SyncServerExecutor;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLModIdMappingEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class UltramineServerModContainer extends DummyModContainer
{
	private static UltramineServerModContainer instance;
	
	private LoadController controller;
	@SideOnly(Side.SERVER)
	private ButtonCommand buttonCommand;
	private ItemBlocker itemBlocker;
	private final RecipeCache recipeCache = new RecipeCache();
	
	public UltramineServerModContainer()
	{
		super(new ModMetadata());
		instance = this;
	    ModMetadata meta = getMetadata();
		meta.modId		= "UltramineServer";
		meta.name		= "Ultramine Server";
		meta.version	= "@version@";
	}
	
	public static UltramineServerModContainer getInstance()
	{
		return instance;
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		this.controller = controller;
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
		try
		{
			if(e.getSide().isServer())
			{
				ConfigurationHandler.load();
				Databases.init();
				MinecraftServer.getServer().getMultiWorld().preloadConfigs();
			}
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}
	
	@Subscribe
	public void init(FMLInitializationEvent e)
	{
		try
		{
			UMEventHandler handler = new UMEventHandler();
			MinecraftForge.EVENT_BUS.register(handler);
			FMLCommonHandler.instance().bus().register(handler);
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}

	@Subscribe
	public void postInit(FMLPostInitializationEvent e)
	{
		try
		{
			if(e.getSide().isServer())
				ConfigurationHandler.saveServerConfig();
			((SyncServerExecutor)GlobalExecutors.nextTick()).register();
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}
	
	@Subscribe
	public void serverAboutToStart(FMLServerAboutToStartEvent e)
	{
		try
		{
			e.getServer().getMultiWorld().register();
			if(e.getSide().isServer())
			{
				buttonCommand = new ButtonCommand(e.getServer());
				itemBlocker = new ItemBlocker();
			}
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}
	
	@Subscribe
	public void serverStarting(FMLServerStartingEvent e)
	{
		try
		{
			e.getServer().getConfigurationManager().getDataLoader().registerPlayerDataExt(PlayerCoreData.class, "core");
			e.registerArgumentHandlers(DefaultCompleters.class);
			e.registerCommands(BasicPermissionCommands.class);
			e.registerCommands(VanillaCommands.class);
			e.registerCommands(BasicCommands.class);
			e.registerCommands(TechCommands.class);
			e.registerCommands(EconomyCommands.class);
			e.registerCommands(OpenInvCommands.class);
			
			for(String perm : new String[]{
					"command.vanilla.help",
					"command.vanilla.msg",
					"command.vanilla.me",
					"command.vanilla.kill",
					"command.vanilla.list",
					"ability.player.useblock",
					"ability.player.useitem",
					"ability.player.blockplace",
					"ability.player.blockbreak",
					"ability.player.attack",
					"ability.player.chat",
					"command.fastwarp.spawn",
					})
			{
				e.getPermissionHandler().addToGroup(IPermissionManager.DEFAULT_GROUP_NAME, IPermissionManager.GLOBAL_WORLD, perm);
			}
			e.getPermissionHandler().setGroupMeta(IPermissionManager.DEFAULT_GROUP_NAME, IPermissionManager.GLOBAL_WORLD, "color", "7");
			e.getPermissionHandler().addToGroup("admin", IPermissionManager.GLOBAL_WORLD, "*");
			e.getPermissionHandler().addToGroup("admin", IPermissionManager.GLOBAL_WORLD, "^"+MinecraftPermissions.HIDE_JOIN_MESSAGE);
			e.getPermissionHandler().setGroupMeta("admin", IPermissionManager.GLOBAL_WORLD, "color", "c");
			e.getPermissionHandler().setGroupMeta("admin", IPermissionManager.GLOBAL_WORLD, "tablistcolor", "c");
			e.getPermissionHandler().setGroupMeta("admin", IPermissionManager.GLOBAL_WORLD, "prefix", "&4[admin] ");
			
			if(e.getSide().isServer())
			{
				buttonCommand.load(e);
				itemBlocker.load();
				MinecraftForge.EVENT_BUS.register(new WarpProtection());
				e.getServer().getScheduler().start();
			}
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}
	
	@Subscribe
	public void serverStarted(FMLServerStartedEvent e)
	{
		try
		{
			PermissionHandler.getInstance().reload();
			ServerDataLoader loader = MinecraftServer.getServer().getConfigurationManager().getDataLoader();
			CommandRegistry reg = ((CommandHandler)MinecraftServer.getServer().getCommandManager()).getRegistry();
			loader.loadCache();
			loader.addDefaultWarps();
			for(String name : loader.getFastWarps())
				reg.registerCommand(new FastWarpCommand(name));
			getRecipeCache().clearCache();
			if(e.getSide().isServer())
				getRecipeCache().setEnabled(ConfigurationHandler.getServerConfig().settings.other.recipeCacheEnabled);
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}
	
	@Subscribe
	public void serverStopped(FMLServerStoppedEvent e)
	{
		try
		{
			MinecraftServer.getServer().getMultiWorld().unregister();
			ChunkProfiler.instance().setEnabled(false);
			
			if(e.getSide().isServer())
			{
				buttonCommand.unload();
				MinecraftServer.getServer().getScheduler().stop();
			}
		}
		catch (Throwable t)
		{
			controller.errorOccurred(this, t);
		}
	}
	
	@Subscribe
	public void remap(FMLModIdMappingEvent e)
	{
		MinecraftForge.EVENT_BUS.post(new ForgeModIdMappingEvent(e));
		FurnaceRecipes.smelting().remap();
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
			"org.ultramine.server",
			"org.ultramine.commands",
			"org.ultramine.server.util"
		);
	}

	@Override
	public Object getMod()
	{
		return this;
	}
	
	public RecipeCache getRecipeCache()
	{
		return recipeCache;
	}
	
	public void reloadToolsCfg()
	{
		getRecipeCache().setEnabled(ConfigurationHandler.getServerConfig().settings.other.recipeCacheEnabled);
		itemBlocker.reload();
	}
}
