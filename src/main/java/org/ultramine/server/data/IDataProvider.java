package org.ultramine.server.data;

import java.util.List;

import org.ultramine.server.data.player.PlayerData;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.NBTTagCompound;

public interface IDataProvider
{
	NBTTagCompound loadPlayer(GameProfile player);
	
	void savePlayer(GameProfile player, NBTTagCompound nbt);
	
	PlayerData loadPlayerData(GameProfile player);
	
	List<PlayerData> loadAllPlayerData();
	
	void savePlayerData(PlayerData data);
}
