package org.ultramine.server.data.player;

import net.minecraft.nbt.NBTTagCompound;

public class PlayerDataExtensionInfo
{
	private Class<? extends PlayerDataExtension> clazz;
	private String nbtTagName;
	
	public PlayerDataExtensionInfo(Class<? extends PlayerDataExtension> clazz, String nbtTagName)
	{
		this.clazz = clazz;
		this.nbtTagName = nbtTagName;
	}
	
	public Class<? extends PlayerDataExtension> getExtClass()
	{
		return clazz;
	}
	
	public String getTagName()
	{
		return nbtTagName;
	}
	
	private PlayerDataExtension makeNew()
	{
		try
		{
			return clazz.newInstance();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public PlayerDataExtension createFromNBT(NBTTagCompound nbt)
	{
		PlayerDataExtension data = makeNew();
		if(nbt != null)
			data.readFromNBT(nbt.getCompoundTag(nbtTagName));
		return data;
	}
}
