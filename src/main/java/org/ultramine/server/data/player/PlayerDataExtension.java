package org.ultramine.server.data.player;

import net.minecraft.nbt.NBTTagCompound;

public abstract class PlayerDataExtension
{
	public abstract void writeToNBT(NBTTagCompound nbt);
	
	public abstract void readFromNBT(NBTTagCompound nbt);
}
