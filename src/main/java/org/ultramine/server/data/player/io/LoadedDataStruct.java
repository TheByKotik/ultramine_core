package org.ultramine.server.data.player.io;

import org.ultramine.server.data.player.PlayerData;

import net.minecraft.nbt.NBTTagCompound;

public class LoadedDataStruct
{
	private final NBTTagCompound nbt;
	private final PlayerData data;

	public LoadedDataStruct(NBTTagCompound nbt, PlayerData data)
	{
		this.nbt = nbt;
		this.data = data;
	}

	public NBTTagCompound getNBT()
	{
		return nbt;
	}

	public PlayerData getPlayerData()
	{
		return data;
	}
}
