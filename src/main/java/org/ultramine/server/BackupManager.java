package org.ultramine.server;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.UltramineServerConfig.SettingsConf.AutoBackupConf;
import org.ultramine.server.data.ServerDataLoader;
import org.ultramine.server.util.GlobalExecutors;
import org.ultramine.server.util.ZipUtil;

import com.google.common.base.Function;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.functions.GenericIterableFactory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class BackupManager
{
	private static final Logger log = LogManager.getLogger();
	
	private final MinecraftServer server;
	
	private long lastBackupTime;
	private boolean isBackuping;
	private AtomicBoolean backupCompleted = new AtomicBoolean(false);
	
	public BackupManager(MinecraftServer server)
	{
		this.server = server;
		this.lastBackupTime = server.startTime;
	}
	
	public void tick()
	{
		if(backupCompleted.get())
		{
			backupCompleted.set(false);
			for(WorldServer world : server.getMultiWorld().getLoadedWorlds())
				world.theChunkProviderServer.resumeSaving();

			isBackuping = false;
		}
		
		AutoBackupConf conf = ConfigurationHandler.getServerConfig().settings.autobackup;
		if(conf.enabled && (System.currentTimeMillis() - lastBackupTime >= conf.interval*60*1000) && !isBackuping)
		{
			lastBackupTime = System.currentTimeMillis();
			File dir = server.getBackupDir();
			List<BackupDescriptor> list = getBackupList();
			
			if(conf.maxBackups != -1)
				while(list.size() >= conf.maxBackups)
					FileUtils.deleteQuietly(new File(dir, list.remove(0).getName()));
			while(conf.maxDirSize != -1 && conf.maxDirSize > FileUtils.sizeOfDirectory(dir) && list.size() != 0)
				FileUtils.deleteQuietly(new File(dir, list.remove(0).getName()));
			
			if(conf.notifyPlayers)
				server.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("ultramine.autobackup.start")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)));
			
			if(conf.worlds == null)
				backupAll();
			else
				backup(conf.worlds);
		}
	}
	
	public List<BackupDescriptor> getBackupList()
	{
		List<BackupDescriptor> list = new ArrayList<BackupDescriptor>();
		for(File file : server.getBackupDir().listFiles())
		{
			if(file.getName().endsWith(".zip"))
				list.add(new BackupDescriptor(file));
		}
		Collections.sort(list);
		return list;
	}

	public void backupWorldsSyncUnsafe() throws IOException
	{
		String filename = backupWorldDirs(server.getMultiWorld().getDirsForBackup());
		log.info("World backup created {}", filename);
	}
	
	private String backupWorldDirs(Collection<String> worlds) throws IOException
	{
		String zipname = String.format("%1$tY.%1$tm.%1$td_%1$tH-%1$tM-%1$tS.zip", System.currentTimeMillis());
		File zip = new File(server.getBackupDir(), zipname);
		File worldsDir = FMLCommonHandler.instance().getSavesDirectory();
		ZipUtil.zipAll(zip, worldsDir, worlds);
		return zip.getName();
	}
	
	public void backupAll()
	{
		backup(server.getMultiWorld().getDirsForBackup());
	}
	
	public void backup(final Collection<String> dirs)
	{
		if(isBackuping)
			throw new IllegalStateException("Already backuping");
		isBackuping = true;
		log.info("Starting backup, saving worlds");
		server.getConfigurationManager().saveAllPlayerData();
		for(WorldServer world : server.getMultiWorld().getLoadedWorlds())
		{
			if(dirs.contains(server.getMultiWorld().getSaveDirName(world)))
			{
				world.theChunkProviderServer.saveChunks(true, null);
				MinecraftForge.EVENT_BUS.post(new WorldEvent.Save(world));
			}
			world.theChunkProviderServer.preventSaving();
		}
		
		GlobalExecutors.writingIOExecutor().execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ThreadedFileIOBase.threadedIOInstance.waitForFinish();
				} catch (InterruptedException ignored){}

				RegionFileCache.clearRegionFileReferences();
				
				log.info("Worlds saved, making backup");
				
				try
				{
					String filename = backupWorldDirs(dirs);
					log.info("World backup completed {}", filename);
				}
				catch(IOException e)
				{
					log.error("Failed to make backup", e);
				}
				
				backupCompleted.set(true);
			}
		});
	}
	
	public void applyBackup(String path) throws CommandException
	{
		applyBackup(path, null, null, true, false);
	}
	
	//Адовы костыли с CommandContext и CommandException. Нужна универсальность без привязки к системе команд
	public void applyBackup(String path, CommandContext ctx, List<String> moveOnlyList, boolean movePlayersP, final boolean makeTemp) throws CommandException
	{
		final boolean movePlayers = movePlayersP && server.getConfigurationManager().getDataLoader().getDataProvider().isUsingWorldPlayerDir();
		File zipFile = new File(server.getBackupDir(), path);
		if(!zipFile.exists())
			throw new CommandException("command.backup.apply.fail.nofile", path);
		
		final Set<String> moveOnly;
		try
		{
			Set<String> available = ZipUtil.getRootFiles(zipFile);
			if(moveOnlyList == null)
			{
				moveOnly = new HashSet<String>(available);
			}
			else
			{
				moveOnly = new HashSet<String>(moveOnlyList);
				moveOnly.retainAll(available);
			}
		}
		catch(IOException e)
		{
			log.error("Failed to apply backup (read zip file)", e);
			throw new CommandException("command.backup.apply.fail.zip.read", path);
		}
		
		if(moveOnly.size() == 0)
			throw new RuntimeException("command.backup.apply.fail.nothing");
		else if(ctx != null)
			ctx.sendMessage("command.backup.apply.started", moveOnly);
		
		TIntObjectMap<List<EntityPlayerMP>> dimToPlayerMap = new TIntObjectHashMap<List<EntityPlayerMP>>();
		if(!makeTemp)
		{
			List<WorldServer> worlds = new ArrayList<WorldServer>(server.getMultiWorld().getLoadedWorlds());
			for(WorldServer world : worlds)
			{
				if(!moveOnly.contains(server.getMultiWorld().getSaveDirName(world)))
						continue;
				List<EntityPlayerMP> players = server.getMultiWorld().destroyWorld(world);
				dimToPlayerMap.put(world.provider.dimensionId, players);
			}
			
			try
			{
				ThreadedFileIOBase.threadedIOInstance.waitForFinish();
			} catch (InterruptedException ignored){}

			RegionFileCache.clearRegionFileReferences();
			
			for(WorldServer world : worlds)
			{
				String saveDir = server.getMultiWorld().getSaveDirName(world);
				if(!moveOnly.contains(saveDir))
					continue;
				try
				{
					if(movePlayers)
					{
						FileUtils.deleteDirectory(new File(server.getWorldsDir(), saveDir));
					}
					else
					{
						for(File file : new File(server.getWorldsDir(), saveDir).listFiles())
						{
							if(!file.getName().equals("playerdata"))
								FileUtils.forceDelete(file);
						}
					}
				}
				catch(IOException e)
				{
					log.warn("Failed to delete world directory ("+saveDir+") on backup apply", e);
					if(ctx != null)
						ctx.sendMessage(EnumChatFormatting.RED, "command.backup.apply.fail.rmdir", saveDir);
				}
			}
			
			for(EntityPlayerMP player : GenericIterableFactory.newCastingIterable(server.getConfigurationManager().playerEntityList, EntityPlayerMP.class))
				player.playerNetServerHandler.sendPacket(new S00PacketKeepAlive((int)(System.nanoTime() / 1000000L))); //prevent disconnect
		}
		
		try
		{
			ZipUtil.unzip(zipFile, server.getWorldsDir(), new Function<String, String>()
			{
				@Override
				public String apply(String name)
				{
					for(String s : moveOnly)
						if(!name.startsWith(s))
							return null;
					if(name.endsWith("/session.lock"))
						return null;
					if(!movePlayers && name.contains("/playerdata/"))
						return null;
					if(makeTemp)
						name = "unpack_" + name;
					return name;
				}
			});
		}
		catch(IOException e)
		{
			log.error("Failed to apply backup (unpack or write files)! May lead to major bugs!", e);
			if(ctx != null)
				ctx.sendMessage(EnumChatFormatting.RED, "command.backup.apply.fail.zip.unpack");
		}
		
		for(EntityPlayerMP player : GenericIterableFactory.newCastingIterable(server.getConfigurationManager().playerEntityList, EntityPlayerMP.class))
			player.playerNetServerHandler.sendPacket(new S00PacketKeepAlive((int)(System.nanoTime() / 1000000L))); //prevent disconnect
		
		if(makeTemp)
		{
			if(ctx != null)
				ctx.sendMessage("command.backup.apply.success.temp");
			for(File file : server.getWorldsDir().listFiles())
			{
				String name = file.getName();
				if(name.startsWith("unpack_"))
				{
					int dim = server.getMultiWorld().allocTempDim();
					name = "temp_"+dim+"_"+name.substring(7);
					file.renameTo(new File(server.getWorldsDir(), name));
					server.getMultiWorld().makeTempWorld(name, dim);
					if(ctx != null)
						ctx.sendMessage("    - [%s](%s)", dim, name);
				}
			}
		}
		else
		{
			boolean backOverworld = dimToPlayerMap.containsKey(0);
			if(backOverworld)
				server.getMultiWorld().initDimension(0); //overworld first
			for(TIntObjectIterator<List<EntityPlayerMP>> it = dimToPlayerMap.iterator(); it.hasNext();)
			{
				it.advance();
				int dim = it.key();
				if(dim != 0)
					DimensionManager.initDimension(dim);
			}
			
			ServerDataLoader loader = server.getConfigurationManager().getDataLoader();
			if(movePlayers && backOverworld) //global player data reload
			{
				loader.reloadPlayerCache();
				for(EntityPlayerMP player : GenericIterableFactory.newCastingIterable(server.getConfigurationManager().playerEntityList, EntityPlayerMP.class))
					reloadPlayer(player);
			}
			
			for(TIntObjectIterator<List<EntityPlayerMP>> it = dimToPlayerMap.iterator(); it.hasNext();)
			{
				it.advance();
				int dim = it.key();
				WorldServer world = server.getMultiWorld().getWorldByID(dim);
				for(EntityPlayerMP player : it.value())
				{
					player.setWorld(world);
					if(movePlayers)
					{
						if(server.getMultiWorld().getIsolatedDataDims().contains(dim))
							reloadPlayer(player);
					}
					else
					{
						world.spawnEntityInWorld(player);
						world.getPlayerManager().addPlayer(player);
						player.theItemInWorldManager.setWorld(world);
					}
				}
			}
			
			if(ctx != null)
				ctx.sendMessage("command.backup.apply.success");
		}
	}
	
	private void reloadPlayer(EntityPlayerMP player)
	{
		int curdim = player.dimension;
		WorldServer world = server.getMultiWorld().getWorldByID(curdim);
		player.setWorld(world);
		server.getConfigurationManager().getDataLoader().syncReloadPlayer(player);
		int newdim = player.dimension;
		if(newdim != curdim)
		{
			player.dimension = curdim;
			player.transferToDimension(newdim);
		}
		else
		{
			world.spawnEntityInWorld(player);
			world.getPlayerManager().addPlayer(player);
			player.theItemInWorldManager.setWorld(world);
			player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
			server.getConfigurationManager().updateTimeAndWeatherForPlayer(player, world);
			server.getConfigurationManager().syncPlayerInventory(player);

			for(PotionEffect eff : GenericIterableFactory.newCastingIterable(player.getActivePotionEffects(), PotionEffect.class))
				player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), eff));
		}
	}
	
	public static class BackupDescriptor implements Comparable<BackupDescriptor>
	{
		private final String name;
		private final long time;
		
		private BackupDescriptor(String name, long time)
		{
			this.name = name;
			this.time = time;
		}
		
		private BackupDescriptor(File file)
		{
			this(file.getName(), file.lastModified());
		}

		@Override
		public int compareTo(BackupDescriptor b)
		{
			return Long.compare(time, b.time);
		}
		
		public String getName()
		{
			return name;
		}
	}
}
