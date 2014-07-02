package org.ultramine.server.data.player;

import java.util.HashMap;
import java.util.Map;

import org.ultramine.server.util.WarpLocation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PlayerCoreData extends PlayerDataExtension
{
	private Map<String, WarpLocation> homes = new HashMap<String, WarpLocation>();
	
	public WarpLocation getHome(String name)
	{
		return homes.get(name);
	}
	
	public void setHome(String name, WarpLocation home)
	{
		homes.put(name, home);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList homeList = new NBTTagList();
		for(Map.Entry<String, WarpLocation> ent : homes.entrySet())
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			nbt1.setString("k", ent.getKey());
			nbt1.setTag("v", ent.getValue().toNBT());
			homeList.appendTag(nbt1);
		}
		nbt.setTag("homes", homeList);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagList homeList = nbt.getTagList("homes", 10);
		for(int i = 0, s = homeList.tagCount(); i < s; i++)
		{
			NBTTagCompound nbt1 = homeList.getCompoundTagAt(i);
			homes.put(nbt1.getString("k"), WarpLocation.getFromNBT(nbt1.getCompoundTag("v")));
		}
	}
}
