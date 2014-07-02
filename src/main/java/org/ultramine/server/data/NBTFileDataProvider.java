package org.ultramine.server.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.data.player.PlayerDataExtension;
import org.ultramine.server.data.player.PlayerDataExtensionInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.storage.SaveHandler;

public class NBTFileDataProvider implements IDataProvider
{
	private static final Logger log = LogManager.getLogger();

	private final ServerConfigurationManager mgr;
	private File umPlayerDir;

	public NBTFileDataProvider(ServerConfigurationManager mgr)
	{
		this.mgr = mgr;
	}

	@Override
	public NBTTagCompound loadPlayer(GameProfile player)
	{
		File dir = ((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir();
		File file = new File(dir, player.getId().toString() + ".dat");
		if(file.exists())
		{
			try
			{
				return CompressedStreamTools.readCompressed(new FileInputStream(file));
			}
			catch(IOException e)
			{
				log.warn("Failed to load player data for " + player.getName(), e);
			}
		}
		
		return null;
	}
	
	@Override
	public void savePlayer(GameProfile player, NBTTagCompound nbt)
	{
		safeWriteNBT(new File(((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir(), player.getId().toString() + ".dat"), nbt);
	}

	@Override
	public PlayerData loadPlayerData(GameProfile player)
	{
		checkPlayerDir();
		
		return readPlayerData(getPlayerDataNBT(player.getId().toString()));
	}
	
	public List<PlayerData> loadAllPlayerData()
	{
		checkPlayerDir();
		
		List<PlayerData> list = new ArrayList<PlayerData>();
		for(File file : umPlayerDir.listFiles())
		{
			if(file.getName().endsWith(".dat"))
			{
				try
				{
					list.add(readPlayerData(CompressedStreamTools.readCompressed(new FileInputStream(file))));
				}
				catch(IOException e)
				{
					log.warn("Failed to load ultramine player data from " + file.getName(), e);
				}
			}
		}
		
		return list;
	}
	
	@Override
	public void savePlayerData(PlayerData data)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("id", data.getProfile().getId().toString());
		nbt.setString("name", data.getProfile().getName());
		for(PlayerDataExtensionInfo info : mgr.getDataLoader().getDataExtProviders())
		{
			NBTTagCompound extnbt = new NBTTagCompound();
			data.get(info.getExtClass()).writeToNBT(extnbt);
			nbt.setTag(info.getTagName(), extnbt);
		}
		
		safeWriteNBT(new File(umPlayerDir, data.getProfile().getId().toString() + ".dat"), nbt);
	}
	
	private void checkPlayerDir()
	{
		if(umPlayerDir == null)
		{
			umPlayerDir = new File(((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir(), "ultramine");
			umPlayerDir.mkdir();
		}
	}

	private NBTTagCompound getPlayerDataNBT(String username)
	{
		try
		{
			File file = new File(umPlayerDir, username + ".dat");

			if (file.exists())
			{
				return CompressedStreamTools.readCompressed(new FileInputStream(file));
			}
		}
		catch (IOException e)
		{
			log.warn("Failed to load ultramine player data for " + username, e);
		}

		return null;
	}
	
	private PlayerData readPlayerData(NBTTagCompound nbt)
	{
		List<PlayerDataExtensionInfo> infos = mgr.getDataLoader().getDataExtProviders();
		List<PlayerDataExtension> data = new ArrayList<PlayerDataExtension>(infos.size());
		
		for(PlayerDataExtensionInfo info : infos)
		{
			data.add(info.createFromNBT(nbt));
		}
		
		PlayerData pdata = new PlayerData(data);
		if(nbt != null && nbt.hasKey("id") && nbt.hasKey("name"))
			pdata.setProfile(new GameProfile(UUID.fromString(nbt.getString("id")), nbt.getString("name")));
		return pdata;
	}
	
	private void safeWriteNBT(File file, NBTTagCompound nbt)
	{
		try
		{
			File file1 = new File(file.getParentFile(), file.getName()+".tmp");
			CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(file1));

			if (file.exists())
				file.delete();
			file1.renameTo(file);
		}
		catch(IOException e)
		{
			log.warn("Failed to write file: "+file.getAbsolutePath(), e);
		}
	}
}
