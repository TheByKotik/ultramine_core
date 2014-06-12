package net.minecraft.server;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.StartupQuery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Bootstrap;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.permission.IPermissionManager;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.MultiWorld;
import org.ultramine.server.WatchdogThread;
import org.ultramine.server.chunk.ChunkIOExecutor;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

public abstract class MinecraftServer implements ICommandSender, Runnable, IPlayerUsage
{
	private static final Logger logger = LogManager.getLogger();
	private static MinecraftServer mcServer;
	private final ISaveFormat anvilConverterForAnvilFile;
	private final PlayerUsageSnooper usageSnooper = new PlayerUsageSnooper("server", this, getSystemTimeMillis());
	private final File anvilFile;
	private final List tickables = new ArrayList();
	private final ICommandManager commandManager;
	public final Profiler theProfiler = new Profiler();
	private final NetworkSystem field_147144_o;
	private final ServerStatusResponse field_147147_p = new ServerStatusResponse();
	private final Random field_147146_q = new Random();
	@SideOnly(Side.SERVER)
	private String hostname;
	private int serverPort = -1;
	public WorldServer[] worldServers = new WorldServer[0];
	private ServerConfigurationManager serverConfigManager;
	private boolean serverRunning = true;
	private boolean serverStopped;
	private int tickCounter;
	protected final Proxy serverProxy;
	public String currentTask;
	public int percentDone;
	private boolean onlineMode;
	private boolean canSpawnAnimals;
	private boolean canSpawnNPCs;
	private boolean pvpEnabled;
	private boolean allowFlight;
	private String motd;
	private int buildLimit;
	private int field_143008_E = 0;
	public final long[] tickTimeArray = new long[100];
	//public long[][] timeOfLastDimensionTick;
	public Hashtable<Integer, long[]> worldTickTimes = new Hashtable<Integer, long[]>();
	private KeyPair serverKeyPair;
	private String serverOwner;
	private String folderName;
	@SideOnly(Side.CLIENT)
	private String worldName;
	private boolean isDemo;
	private boolean enableBonusChest;
	private boolean worldIsBeingDeleted;
	private String field_147141_M = "";
	private boolean serverIsRunning;
	private long timeOfLastWarning;
	private String userMessage;
	private boolean startProfiling;
	private boolean isGamemodeForced;
	private final MinecraftSessionService field_147143_S;
	private long field_147142_T = 0L;
	private static final String __OBFID = "CL_00001462";
	
	private static final int TPS = 20;
	private static final int TICK_TIME = 1000000000 / TPS;
	public static double currentTPS = 20;
	private static long catchupTime = 0;
	private IPermissionManager permissionManager;

	public MinecraftServer(File p_i45281_1_, Proxy p_i45281_2_)
	{
		mcServer = this;
		this.serverProxy = p_i45281_2_;
		this.anvilFile = p_i45281_1_;
		this.field_147144_o = new NetworkSystem(this);
		this.commandManager = new ServerCommandManager();
		this.anvilConverterForAnvilFile = new AnvilSaveConverter(p_i45281_1_);
		this.field_147143_S = (new YggdrasilAuthenticationService(p_i45281_2_, UUID.randomUUID().toString())).createMinecraftSessionService();
	}

	protected abstract boolean startServer() throws IOException;

	protected void convertMapIfNeeded(String par1Str)
	{
		if (this.getActiveAnvilConverter().isOldMapFormat(par1Str))
		{
			logger.info("Converting map!");
			this.setUserMessage("menu.convertingLevel");
			this.getActiveAnvilConverter().convertMapFormat(par1Str, new IProgressUpdate()
			{
				private long field_96245_b = System.currentTimeMillis();
				private static final String __OBFID = "CL_00001417";
				public void displayProgressMessage(String par1Str) {}
				public void setLoadingProgress(int par1)
				{
					if (System.currentTimeMillis() - this.field_96245_b >= 1000L)
					{
						this.field_96245_b = System.currentTimeMillis();
						MinecraftServer.logger.info("Converting... " + par1 + "%");
					}
				}
				@SideOnly(Side.CLIENT)
				public void resetProgressAndMessage(String par1Str) {}
				@SideOnly(Side.CLIENT)
				public void func_146586_a() {}
				public void resetProgresAndWorkingMessage(String par1Str) {}
			});
		}
	}

	protected synchronized void setUserMessage(String par1Str)
	{
		this.userMessage = par1Str;
	}

	@SideOnly(Side.CLIENT)

	public synchronized String getUserMessage()
	{
		return this.userMessage;
	}

	protected void loadAllWorlds(String par1Str, String par2Str, long par3, WorldType par5WorldType, String par6Str)
	{
		this.convertMapIfNeeded(par1Str);
		this.setUserMessage("menu.loadingLevel");
		ISaveHandler isavehandler = this.anvilConverterForAnvilFile.getSaveLoader(par1Str, true);
		WorldInfo worldinfo = isavehandler.loadWorldInfo();
		WorldSettings worldsettings;

		if (worldinfo == null)
		{
			worldsettings = new WorldSettings(par3, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), par5WorldType);
			worldsettings.func_82750_a(par6Str);
		}
		else
		{
			worldsettings = new WorldSettings(worldinfo);
		}

		if (this.enableBonusChest)
		{
			worldsettings.enableBonusChest();
		}

		WorldServer overWorld = (isDemo() ? new DemoWorldServer(this, isavehandler, par2Str, 0, theProfiler) : new WorldServer(this, isavehandler, par2Str, 0, worldsettings, theProfiler));
		for (int dim : DimensionManager.getStaticDimensionIDs())
		{
			WorldServer world = (dim == 0 ? overWorld : new WorldServerMulti(this, isavehandler, par2Str, dim, worldsettings, overWorld, theProfiler));
			world.addWorldAccess(new WorldManager(this, world));

			if (!this.isSinglePlayer())
			{
				world.getWorldInfo().setGameType(this.getGameType());
			}

			MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
		}

		this.serverConfigManager.setPlayerManager(new WorldServer[]{ overWorld });
		this.func_147139_a(this.func_147135_j());
		this.initialWorldChunkLoad();
	}

	protected void initialWorldChunkLoad()
	{
		boolean flag = true;
		boolean flag1 = true;
		boolean flag2 = true;
		boolean flag3 = true;
		int i = 0;
		this.setUserMessage("menu.generatingTerrain");
		byte b0 = 0;
		logger.info("Preparing start region for level " + b0);
		WorldServer worldserver = this.worldServers[b0];
		ChunkCoordinates chunkcoordinates = worldserver.getSpawnPoint();
		long j = getSystemTimeMillis();

		for (int k = -192; k <= 192 && this.isServerRunning(); k += 16)
		{
			for (int l = -192; l <= 192 && this.isServerRunning(); l += 16)
			{
				long i1 = getSystemTimeMillis();

				if (i1 - j > 1000L)
				{
					this.outputPercentRemaining("Preparing spawn area", i * 100 / 625);
					j = i1;
				}

				++i;
				worldserver.theChunkProviderServer.loadChunk(chunkcoordinates.posX + k >> 4, chunkcoordinates.posZ + l >> 4);
			}
		}

		this.clearCurrentTask();
	}

	public abstract boolean canStructuresSpawn();

	public abstract WorldSettings.GameType getGameType();

	public abstract EnumDifficulty func_147135_j();

	public abstract boolean isHardcore();

	public abstract int getOpPermissionLevel();

	protected void outputPercentRemaining(String par1Str, int par2)
	{
		this.currentTask = par1Str;
		this.percentDone = par2;
		logger.info(par1Str + ": " + par2 + "%");
	}

	protected void clearCurrentTask()
	{
		this.currentTask = null;
		this.percentDone = 0;
	}

	protected void saveAllWorlds(boolean par1)
	{
		if (!this.worldIsBeingDeleted)
		{
			WorldServer[] aworldserver = this.worldServers;
			if (aworldserver == null) return; //Forge: Just in case, NPE protection as it has been encountered.
			int i = aworldserver.length;

			for (int j = 0; j < i; ++j)
			{
				WorldServer worldserver = aworldserver[j];

				if (worldserver != null)
				{
					if (!par1)
					{
						logger.info("Saving chunks for level \'" + worldserver.getWorldInfo().getWorldName() + "\'/" + worldserver.provider.getDimensionName());
					}

					try
					{
						worldserver.saveAllChunks(true, (IProgressUpdate)null);
					}
					catch (MinecraftException minecraftexception)
					{
						logger.warn(minecraftexception.getMessage());
					}
				}
			}
		}
	}

	public void stopServer()
	{
		if (!this.worldIsBeingDeleted && Loader.instance().hasReachedState(LoaderState.SERVER_STARTED) && !serverStopped) // make sure the save is valid and we don't save twice
		{
			logger.info("Stopping server");

			if (this.func_147137_ag() != null)
			{
				this.func_147137_ag().terminateEndpoints();
			}

			if (this.serverConfigManager != null)
			{
				logger.info("Saving players");
				this.serverConfigManager.saveAllPlayerData();
				this.serverConfigManager.removeAllPlayers();
			}

			logger.info("Saving worlds");
			this.saveAllWorlds(false);

			for (int i = 0; i < this.worldServers.length; ++i)
			{
				WorldServer worldserver = this.worldServers[i];
				MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(worldserver));
				worldserver.flush();
			}

			WorldServer[] tmp = worldServers;
			for (WorldServer world : tmp)
			{
				DimensionManager.setWorld(world.provider.dimensionId, null);
			}

			if (this.usageSnooper.isSnooperRunning())
			{
				this.usageSnooper.stopSnooper();
			}
		}
	}

	public boolean isServerRunning()
	{
		return this.serverRunning;
	}

	public void initiateShutdown()
	{
		this.serverRunning = false;
	}

	public void run()
	{
		try
		{
			if (this.startServer())
			{
				FMLCommonHandler.instance().handleServerStarted();
				long i = getSystemTimeMillis();
				long l = 0L;
				this.field_147147_p.func_151315_a(new ChatComponentText(this.motd));
				this.field_147147_p.func_151321_a(new ServerStatusResponse.MinecraftProtocolVersionIdentifier("1.7.2", 4));
				this.func_147138_a(this.field_147147_p);
				
				WatchdogThread.doStart();
				
				for (long lastTick = 0L; this.serverRunning; this.serverIsRunning = true)
				{
					long curTime = System.nanoTime();
					long wait = TICK_TIME - (curTime - lastTick) - catchupTime;

					if (wait > 0)
					{
						Thread.sleep(wait / 1000000);
						catchupTime = 0;
						continue;
					}
					else
					{
						catchupTime = Math.min(TICK_TIME * TPS, Math.abs(wait));
					}

					currentTPS = (currentTPS * 0.95) + (1E9 / (curTime - lastTick) * 0.05);
					
					lastTick = curTime;
					this.tick();
					WatchdogThread.tick();
				}
				
				FMLCommonHandler.instance().handleServerStopping();
				FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
			}
			else
			{
				FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
				//this.finalTick((CrashReport)null);
			}
		}
		catch (StartupQuery.AbortedException e)
		{
			// ignore silently
			FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
		}
		catch (Throwable throwable1)
		{
			logger.error("Encountered an unexpected exception", throwable1);
			CrashReport crashreport = null;

			if (throwable1 instanceof ReportedException)
			{
				crashreport = this.addServerInfoToCrashReport(((ReportedException)throwable1).getCrashReport());
			}
			else
			{
				crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
			}

			File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

			if (crashreport.saveToFile(file1))
			{
				logger.error("This crash report has been saved to: " + file1.getAbsolutePath());
			}
			else
			{
				logger.error("We were unable to save this crash report to disk.");
			}

			FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
			//this.finalTick(crashreport);
		}
		finally
		{
			try
			{
				this.stopServer();
				this.serverStopped = true;
			}
			catch (Throwable throwable)
			{
				logger.error("Exception stopping the server", throwable);
			}
			finally
			{
				try
				{
					FMLCommonHandler.instance().handleServerStopped();
				}
				finally
				{
					this.serverStopped = true;
					this.systemExitNow();
				}
			}
		}
	}

	private void func_147138_a(ServerStatusResponse p_147138_1_)
	{
		File file1 = this.getFile("server-icon.png");

		if (file1.isFile())
		{
			ByteBuf bytebuf = Unpooled.buffer();

			try
			{
				BufferedImage bufferedimage = ImageIO.read(file1);
				Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
				Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
				ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
				ByteBuf bytebuf1 = Base64.encode(bytebuf);
				p_147138_1_.func_151320_a("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
			}
			catch (Exception exception)
			{
				logger.error("Couldn\'t load server icon", exception);
			}
		}
	}

	protected File getDataDirectory()
	{
		return ConfigurationHandler.getSettingDir();
	}

	protected void finalTick(CrashReport par1CrashReport) {}

	protected void systemExitNow() {}

	public void tick()
	{
		long i = System.nanoTime();
		AxisAlignedBB.getAABBPool().cleanPool();
		FMLCommonHandler.instance().onPreServerTick();
		++this.tickCounter;

		if (this.startProfiling)
		{
			this.startProfiling = false;
			this.theProfiler.profilingEnabled = true;
			this.theProfiler.clearProfiling();
		}

		this.theProfiler.startSection("root");
		this.updateTimeLightAndEntities();

		if (i - this.field_147142_T >= 5000000000L)
		{
			this.field_147142_T = i;
			this.field_147147_p.func_151319_a(new ServerStatusResponse.PlayerCountData(this.getMaxPlayers(), this.getCurrentPlayerCount()));
			GameProfile[] agameprofile = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
			int j = MathHelper.getRandomIntegerInRange(this.field_147146_q, 0, this.getCurrentPlayerCount() - agameprofile.length);

			for (int k = 0; k < agameprofile.length; ++k)
			{
				agameprofile[k] = ((EntityPlayerMP)this.serverConfigManager.playerEntityList.get(j + k)).getGameProfile();
			}

			Collections.shuffle(Arrays.asList(agameprofile));
			this.field_147147_p.func_151318_b().func_151330_a(agameprofile);
		}

		if (this.tickCounter % 900 == 0)
		{
			this.theProfiler.startSection("save");
			this.serverConfigManager.saveAllPlayerData();
			this.saveAllWorlds(true);
			this.theProfiler.endSection();
		}

		this.theProfiler.startSection("tallying");
		this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - i;
		this.theProfiler.endSection();
		this.theProfiler.startSection("snooper");

		if (!this.usageSnooper.isSnooperRunning() && this.tickCounter > 100)
		{
			this.usageSnooper.startSnooper();
		}

		if (this.tickCounter % 6000 == 0)
		{
			this.usageSnooper.addMemoryStatsToSnooper();
		}

		this.theProfiler.endSection();
		this.theProfiler.endSection();
		FMLCommonHandler.instance().onPostServerTick();
	}

	public void updateTimeLightAndEntities()
	{
		theProfiler.startSection("ChunkIOExecutor");
		ChunkIOExecutor.tick();
		theProfiler.endSection();
		
		this.theProfiler.startSection("levels");
		int i;

		Integer[] ids = DimensionManager.getIDs(this.tickCounter % 200 == 0);
		for (int x = 0; x < ids.length; x++)
		{
			int id = ids[x];
			long j = System.nanoTime();

			if (id == 0 || this.getAllowNether())
			{
				WorldServer worldserver = DimensionManager.getWorld(id);
				this.theProfiler.startSection(worldserver.getWorldInfo().getWorldName());
				this.theProfiler.startSection("pools");
				worldserver.getWorldVec3Pool().clear();
				this.theProfiler.endSection();

				if (this.tickCounter % 20 == 0)
				{
					this.theProfiler.startSection("timeSync");
					this.serverConfigManager.sendPacketToAllPlayersInDimension(new S03PacketTimeUpdate(worldserver.getTotalWorldTime(), worldserver.getWorldTime(), worldserver.getGameRules().getGameRuleBooleanValue("doDaylightCycle")), worldserver.provider.dimensionId);
					this.theProfiler.endSection();
				}

				this.theProfiler.startSection("tick");
				FMLCommonHandler.instance().onPreWorldTick(worldserver);
				CrashReport crashreport;

				try
				{
					worldserver.tick();
				}
				catch (Throwable throwable1)
				{
					crashreport = CrashReport.makeCrashReport(throwable1, "Exception ticking world");
					worldserver.addWorldInfoToCrashReport(crashreport);
					throw new ReportedException(crashreport);
				}

				try
				{
					worldserver.updateEntities();
				}
				catch (Throwable throwable)
				{
					crashreport = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
					worldserver.addWorldInfoToCrashReport(crashreport);
					throw new ReportedException(crashreport);
				}

				FMLCommonHandler.instance().onPostWorldTick(worldserver);
				this.theProfiler.endSection();
				this.theProfiler.startSection("tracker");
				worldserver.getEntityTracker().updateTrackedEntities();
				this.theProfiler.endSection();
				this.theProfiler.endSection();
			}

			worldTickTimes.get(id)[this.tickCounter % 100] = System.nanoTime() - j;
		}

		this.theProfiler.endStartSection("dim_unloading");
		DimensionManager.unloadWorlds(worldTickTimes);
		this.theProfiler.endStartSection("connection");
		this.func_147137_ag().networkTick();
		this.theProfiler.endStartSection("players");
		this.serverConfigManager.sendPlayerInfoToAllPlayers();
		this.theProfiler.endStartSection("tickables");

		for (i = 0; i < this.tickables.size(); ++i)
		{
			((IUpdatePlayerListBox)this.tickables.get(i)).update();
		}

		this.theProfiler.endSection();
	}

	public boolean getAllowNether()
	{
		return true;
	}

	public void startServerThread()
	{
		StartupQuery.reset();
		(new Thread("Server thread")
		{
			private static final String __OBFID = "CL_00001418";
			public void run()
			{
				MinecraftServer.this.run();
			}
		}).start();
	}

	public File getFile(String par1Str)
	{
		return new File(this.getDataDirectory(), par1Str);
	}

	public void logWarning(String par1Str)
	{
		logger.warn(par1Str);
	}

	public WorldServer worldServerForDimension(int par1)
	{
		WorldServer ret = DimensionManager.getWorld(par1);
		if (ret == null)
		{
			DimensionManager.initDimension(par1);
			ret = DimensionManager.getWorld(par1);
		}
		return ret;
	}

	public String getMinecraftVersion()
	{
		return "1.7.2";
	}

	public int getCurrentPlayerCount()
	{
		return this.serverConfigManager.getCurrentPlayerCount();
	}

	public int getMaxPlayers()
	{
		return this.serverConfigManager.getMaxPlayers();
	}

	public String[] getAllUsernames()
	{
		return this.serverConfigManager.getAllUsernames();
	}

	public String getServerModName()
	{
		return FMLCommonHandler.instance().getModName();
	}

	public CrashReport addServerInfoToCrashReport(CrashReport par1CrashReport)
	{
		par1CrashReport.getCategory().addCrashSectionCallable("Profiler Position", new Callable()
		{
			private static final String __OBFID = "CL_00001419";
			public String call()
			{
				return MinecraftServer.this.theProfiler.profilingEnabled ? MinecraftServer.this.theProfiler.getNameOfLastSection() : "N/A (disabled)";
			}
		});

		if (this.worldServers != null && this.worldServers.length > 0 && this.worldServers[0] != null)
		{
			par1CrashReport.getCategory().addCrashSectionCallable("Vec3 Pool Size", new Callable()
			{
				private static final String __OBFID = "CL_00001420";
				public String call()
				{
					int i = MinecraftServer.this.worldServers[0].getWorldVec3Pool().getPoolSize();
					int j = 56 * i;
					int k = j / 1024 / 1024;
					int l = MinecraftServer.this.worldServers[0].getWorldVec3Pool().getNextFreeSpace();
					int i1 = 56 * l;
					int j1 = i1 / 1024 / 1024;
					return i + " (" + j + " bytes; " + k + " MB) allocated, " + l + " (" + i1 + " bytes; " + j1 + " MB) used";
				}
			});
		}

		if (this.serverConfigManager != null)
		{
			par1CrashReport.getCategory().addCrashSectionCallable("Player Count", new Callable()
			{
				private static final String __OBFID = "CL_00001780";
				public String call()
				{
					return MinecraftServer.this.serverConfigManager.getCurrentPlayerCount() + " / " + MinecraftServer.this.serverConfigManager.getMaxPlayers() + "; " + MinecraftServer.this.serverConfigManager.playerEntityList;
				}
			});
		}

		return par1CrashReport;
	}

	public List getPossibleCompletions(ICommandSender par1ICommandSender, String par2Str)
	{
		ArrayList arraylist = new ArrayList();

		if (par2Str.startsWith("/"))
		{
			par2Str = par2Str.substring(1);
			boolean flag = !par2Str.contains(" ");
			List list = this.commandManager.getPossibleCommands(par1ICommandSender, par2Str);

			if (list != null)
			{
				Iterator iterator = list.iterator();

				while (iterator.hasNext())
				{
					String s3 = (String)iterator.next();

					if (flag)
					{
						arraylist.add("/" + s3);
					}
					else
					{
						arraylist.add(s3);
					}
				}
			}

			return arraylist;
		}
		else
		{
			String[] astring = par2Str.split(" ", -1);
			String s1 = astring[astring.length - 1];
			String[] astring1 = this.serverConfigManager.getAllUsernames();
			int i = astring1.length;

			for (int j = 0; j < i; ++j)
			{
				String s2 = astring1[j];

				if (CommandBase.doesStringStartWith(s1, s2))
				{
					arraylist.add(s2);
				}
			}

			return arraylist;
		}
	}

	public static MinecraftServer getServer()
	{
		return mcServer;
	}

	public String getCommandSenderName()
	{
		return "Server";
	}

	public void addChatMessage(IChatComponent p_145747_1_)
	{
		logger.info(p_145747_1_.getUnformattedText());
	}

	public boolean canCommandSenderUseCommand(int par1, String par2Str)
	{
		return true;
	}

	public ICommandManager getCommandManager()
	{
		return this.commandManager;
	}

	public KeyPair getKeyPair()
	{
		return this.serverKeyPair;
	}

	public String getServerOwner()
	{
		return this.serverOwner;
	}

	public void setServerOwner(String par1Str)
	{
		this.serverOwner = par1Str;
	}

	public boolean isSinglePlayer()
	{
		return this.serverOwner != null;
	}

	public String getFolderName()
	{
		return this.folderName;
	}

	public void setFolderName(String par1Str)
	{
		this.folderName = par1Str;
	}

	@SideOnly(Side.CLIENT)
	public void setWorldName(String par1Str)
	{
		this.worldName = par1Str;
	}

	@SideOnly(Side.CLIENT)
	public String getWorldName()
	{
		return this.worldName;
	}

	public void setKeyPair(KeyPair par1KeyPair)
	{
		this.serverKeyPair = par1KeyPair;
	}

	public void func_147139_a(EnumDifficulty p_147139_1_)
	{
		for (int i = 0; i < this.worldServers.length; ++i)
		{
			WorldServer worldserver = this.worldServers[i];

			if (worldserver != null)
			{
				if (worldserver.getWorldInfo().isHardcoreModeEnabled())
				{
					worldserver.difficultySetting = EnumDifficulty.HARD;
					worldserver.setAllowedSpawnTypes(true, true);
				}
				else if (this.isSinglePlayer())
				{
					worldserver.difficultySetting = p_147139_1_;
					worldserver.setAllowedSpawnTypes(worldserver.difficultySetting != EnumDifficulty.PEACEFUL, true);
				}
				else
				{
					worldserver.difficultySetting = p_147139_1_;
					worldserver.setAllowedSpawnTypes(this.allowSpawnMonsters(), this.canSpawnAnimals);
				}
			}
		}
	}

	protected boolean allowSpawnMonsters()
	{
		return true;
	}

	public boolean isDemo()
	{
		return this.isDemo;
	}

	public void setDemo(boolean par1)
	{
		this.isDemo = par1;
	}

	public void canCreateBonusChest(boolean par1)
	{
		this.enableBonusChest = par1;
	}

	public ISaveFormat getActiveAnvilConverter()
	{
		return this.anvilConverterForAnvilFile;
	}

	public void deleteWorldAndStopServer()
	{
		this.worldIsBeingDeleted = true;
		this.getActiveAnvilConverter().flushCache();

		for (int i = 0; i < this.worldServers.length; ++i)
		{
			WorldServer worldserver = this.worldServers[i];

			if (worldserver != null)
			{
				MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(worldserver));
				worldserver.flush();
			}
		}

		this.getActiveAnvilConverter().deleteWorldDirectory(this.worldServers[0].getSaveHandler().getWorldDirectoryName());
		this.initiateShutdown();
	}

	public String getTexturePack()
	{
		return this.field_147141_M;
	}

	public void addServerStatsToSnooper(PlayerUsageSnooper par1PlayerUsageSnooper)
	{
		par1PlayerUsageSnooper.addData("whitelist_enabled", Boolean.valueOf(false));
		par1PlayerUsageSnooper.addData("whitelist_count", Integer.valueOf(0));
		par1PlayerUsageSnooper.addData("players_current", Integer.valueOf(this.getCurrentPlayerCount()));
		par1PlayerUsageSnooper.addData("players_max", Integer.valueOf(this.getMaxPlayers()));
		par1PlayerUsageSnooper.addData("players_seen", Integer.valueOf(this.serverConfigManager.getAvailablePlayerDat().length));
		par1PlayerUsageSnooper.addData("uses_auth", Boolean.valueOf(this.onlineMode));
		par1PlayerUsageSnooper.addData("gui_state", this.getGuiEnabled() ? "enabled" : "disabled");
		par1PlayerUsageSnooper.addData("run_time", Long.valueOf((getSystemTimeMillis() - par1PlayerUsageSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L));
		par1PlayerUsageSnooper.addData("avg_tick_ms", Integer.valueOf((int)(MathHelper.average(this.tickTimeArray) * 1.0E-6D)));
		int i = 0;

		for (int j = 0; j < this.worldServers.length; ++j)
		{
			if (this.worldServers[j] != null)
			{
				WorldServer worldserver = this.worldServers[j];
				WorldInfo worldinfo = worldserver.getWorldInfo();
				par1PlayerUsageSnooper.addData("world[" + i + "][dimension]", Integer.valueOf(worldserver.provider.dimensionId));
				par1PlayerUsageSnooper.addData("world[" + i + "][mode]", worldinfo.getGameType());
				par1PlayerUsageSnooper.addData("world[" + i + "][difficulty]", worldserver.difficultySetting);
				par1PlayerUsageSnooper.addData("world[" + i + "][hardcore]", Boolean.valueOf(worldinfo.isHardcoreModeEnabled()));
				par1PlayerUsageSnooper.addData("world[" + i + "][generator_name]", worldinfo.getTerrainType().getWorldTypeName());
				par1PlayerUsageSnooper.addData("world[" + i + "][generator_version]", Integer.valueOf(worldinfo.getTerrainType().getGeneratorVersion()));
				par1PlayerUsageSnooper.addData("world[" + i + "][height]", Integer.valueOf(this.buildLimit));
				par1PlayerUsageSnooper.addData("world[" + i + "][chunks_loaded]", Integer.valueOf(worldserver.getChunkProvider().getLoadedChunkCount()));
				++i;
			}
		}

		par1PlayerUsageSnooper.addData("worlds", Integer.valueOf(i));
	}

	public void addServerTypeToSnooper(PlayerUsageSnooper par1PlayerUsageSnooper)
	{
		par1PlayerUsageSnooper.addData("singleplayer", Boolean.valueOf(this.isSinglePlayer()));
		par1PlayerUsageSnooper.addData("server_brand", this.getServerModName());
		par1PlayerUsageSnooper.addData("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
		par1PlayerUsageSnooper.addData("dedicated", Boolean.valueOf(this.isDedicatedServer()));
	}

	public boolean isSnooperEnabled()
	{
		return true;
	}

	public abstract boolean isDedicatedServer();

	public boolean isServerInOnlineMode()
	{
		return this.onlineMode;
	}

	public void setOnlineMode(boolean par1)
	{
		this.onlineMode = par1;
	}

	public boolean getCanSpawnAnimals()
	{
		return this.canSpawnAnimals;
	}

	public void setCanSpawnAnimals(boolean par1)
	{
		this.canSpawnAnimals = par1;
	}

	public boolean getCanSpawnNPCs()
	{
		return this.canSpawnNPCs;
	}

	public void setCanSpawnNPCs(boolean par1)
	{
		this.canSpawnNPCs = par1;
	}

	public boolean isPVPEnabled()
	{
		return this.pvpEnabled;
	}

	public void setAllowPvp(boolean par1)
	{
		this.pvpEnabled = par1;
	}

	public boolean isFlightAllowed()
	{
		return this.allowFlight;
	}

	public void setAllowFlight(boolean par1)
	{
		this.allowFlight = par1;
	}

	public abstract boolean isCommandBlockEnabled();

	public String getMOTD()
	{
		return this.motd;
	}

	public void setMOTD(String par1Str)
	{
		this.motd = par1Str;
	}

	public int getBuildLimit()
	{
		return this.buildLimit;
	}

	public void setBuildLimit(int par1)
	{
		this.buildLimit = par1;
	}

	public ServerConfigurationManager getConfigurationManager()
	{
		return this.serverConfigManager;
	}

	public void setConfigurationManager(ServerConfigurationManager par1ServerConfigurationManager)
	{
		this.serverConfigManager = par1ServerConfigurationManager;
	}

	public void setGameType(WorldSettings.GameType par1EnumGameType)
	{
		for (int i = 0; i < this.worldServers.length; ++i)
		{
			getServer().worldServers[i].getWorldInfo().setGameType(par1EnumGameType);
		}
	}

	public NetworkSystem func_147137_ag()
	{
		return this.field_147144_o;
	}

	@SideOnly(Side.CLIENT)
	public boolean serverIsInRunLoop()
	{
		return this.serverIsRunning;
	}

	public boolean getGuiEnabled()
	{
		return false;
	}

	public abstract String shareToLAN(WorldSettings.GameType var1, boolean var2);

	public int getTickCounter()
	{
		return this.tickCounter;
	}

	public void enableProfiling()
	{
		this.startProfiling = true;
	}

	@SideOnly(Side.CLIENT)
	public PlayerUsageSnooper getPlayerUsageSnooper()
	{
		return this.usageSnooper;
	}

	public ChunkCoordinates getPlayerCoordinates()
	{
		return new ChunkCoordinates(0, 0, 0);
	}

	public World getEntityWorld()
	{
		return this.worldServers[0];
	}

	public int getSpawnProtectionSize()
	{
		return 16;
	}

	public boolean isBlockProtected(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer)
	{
		return false;
	}

	public boolean getForceGamemode()
	{
		return this.isGamemodeForced;
	}

	public Proxy getServerProxy()
	{
		return this.serverProxy;
	}

	public static long getSystemTimeMillis()
	{
		return System.currentTimeMillis();
	}

	public int func_143007_ar()
	{
		return this.field_143008_E;
	}

	public void func_143006_e(int par1)
	{
		this.field_143008_E = par1;
	}

	public IChatComponent func_145748_c_()
	{
		return new ChatComponentText(this.getCommandSenderName());
	}

	public boolean func_147136_ar()
	{
		return true;
	}

	public MinecraftSessionService func_147130_as()
	{
		return this.field_147143_S;
	}

	public ServerStatusResponse func_147134_at()
	{
		return this.field_147147_p;
	}

	public void func_147132_au()
	{
		this.field_147142_T = 0L;
	}

	@SideOnly(Side.SERVER)
	public String getServerHostname()
	{
		return this.hostname;
	}

	@SideOnly(Side.SERVER)
	public void setHostname(String par1Str)
	{
		this.hostname = par1Str;
	}

	@SideOnly(Side.SERVER)
	public void func_82010_a(IUpdatePlayerListBox par1IUpdatePlayerListBox)
	{
		this.tickables.add(par1IUpdatePlayerListBox);
	}

	@SideOnly(Side.SERVER)
	public static void main(String[] par0ArrayOfStr)
	{
		Bootstrap.func_151354_b();

		try
		{
			boolean flag = !GraphicsEnvironment.isHeadless();
			String s = null;
			String s1 = ".";
			String s2 = null;
			boolean flag1 = false;
			boolean flag2 = false;
			int i = -1;

			for (int j = 0; j < par0ArrayOfStr.length; ++j)
			{
				String s3 = par0ArrayOfStr[j];
				String s4 = j == par0ArrayOfStr.length - 1 ? null : par0ArrayOfStr[j + 1];
				boolean flag3 = false;

				if (!s3.equals("nogui") && !s3.equals("--nogui"))
				{
					if (s3.equals("--port") && s4 != null)
					{
						flag3 = true;

						try
						{
							i = Integer.parseInt(s4);
						}
						catch (NumberFormatException numberformatexception)
						{
							;
						}
					}
					else if (s3.equals("--singleplayer") && s4 != null)
					{
						flag3 = true;
						s = s4;
					}
					else if (s3.equals("--universe") && s4 != null)
					{
						flag3 = true;
						s1 = s4;
					}
					else if (s3.equals("--world") && s4 != null)
					{
						flag3 = true;
						s2 = s4;
					}
					else if (s3.equals("--demo"))
					{
						flag1 = true;
					}
					else if (s3.equals("--bonusChest"))
					{
						flag2 = true;
					}
				}
				else
				{
					flag = false;
				}

				if (flag3)
				{
					++j;
				}
			}

			final DedicatedServer dedicatedserver = new DedicatedServer(ConfigurationHandler.getWorldsDir());

			if (s != null)
			{
				dedicatedserver.setServerOwner(s);
			}

			if (s2 != null)
			{
				dedicatedserver.setFolderName(s2);
			}

			if (i >= 0)
			{
				dedicatedserver.setServerPort(i);
			}

			if (flag1)
			{
				dedicatedserver.setDemo(true);
			}

			if (flag2)
			{
				dedicatedserver.canCreateBonusChest(true);
			}

			dedicatedserver.startServerThread();
			Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread")
			{
				private static final String __OBFID = "CL_00001806";
				public void run()
				{
					dedicatedserver.stopServer();
				}
			});
		}
		catch (Exception exception)
		{
			logger.fatal("Failed to start the minecraft server", exception);
		}
	}

	@SideOnly(Side.SERVER)
	public void logInfo(String par1Str)
	{
		logger.info(par1Str);
	}

	@SideOnly(Side.SERVER)
	public String getHostname()
	{
		return this.hostname;
	}

	@SideOnly(Side.SERVER)
	public int getPort()
	{
		return this.serverPort;
	}

	@SideOnly(Side.SERVER)
	public String getMotd()
	{
		return this.motd;
	}

	@SideOnly(Side.SERVER)
	public String getPlugins()
	{
		return "";
	}

	@SideOnly(Side.SERVER)
	public String handleRConCommand(String par1Str)
	{
		RConConsoleSource.instance.resetLog();
		this.commandManager.executeCommand(RConConsoleSource.instance, par1Str);
		return RConConsoleSource.instance.getLogContents();
	}

	@SideOnly(Side.SERVER)
	public boolean isDebuggingEnabled()
	{
		return false;
	}

	@SideOnly(Side.SERVER)
	public void logSevere(String par1Str)
	{
		logger.error(par1Str);
	}

	@SideOnly(Side.SERVER)
	public void logDebug(String par1Str)
	{
		if (this.isDebuggingEnabled())
		{
			logger.info(par1Str);
		}
	}

	@SideOnly(Side.SERVER)
	public int getServerPort()
	{
		return this.serverPort;
	}

	@SideOnly(Side.SERVER)
	public void setServerPort(int par1)
	{
		this.serverPort = par1;
	}

	@SideOnly(Side.SERVER)
	public void func_155759_m(String p_155759_1_)
	{
		this.field_147141_M = p_155759_1_;
	}

	public boolean isServerStopped()
	{
		return this.serverStopped;
	}

	@SideOnly(Side.SERVER)
	public void setForceGamemode(boolean par1)
	{
		this.isGamemodeForced = par1;
	}

	/* ========================================= ULTRAMINE START ======================================*/

	private final MultiWorld multiworld = new MultiWorld(this);
	
	public MultiWorld getMultiWorld()
	{
		return multiworld;
	}

	public IPermissionManager getPermissionManager()
	{
		return permissionManager;
	}

	protected void setPermissionManager(IPermissionManager permissionManager)
	{
		this.permissionManager = permissionManager;
	}
}