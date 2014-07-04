package org.ultramine.server.data.player;

import java.util.HashMap;
import java.util.Map;

import org.ultramine.server.Teleporter;
import org.ultramine.server.util.WarpLocation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PlayerCoreData extends PlayerDataExtension
{
	private Map<String, WarpLocation> homes = new HashMap<String, WarpLocation>();
	
	//undatabased
	private Teleporter teleporter;
	private long nextTeleportationTime;
	private WarpLocation lastLocation;
	
	public WarpLocation getHome(String name)
	{
		return homes.get(name);
	}
	
	public void setHome(String name, WarpLocation home)
	{
		homes.put(name, home);
	}
	
	public Map<String, WarpLocation> getHomes()
	{
		return homes;
	}
	
	public Teleporter getTeleporter()
	{
		return teleporter;
	}
	
	public void setTeleporter(Teleporter teleporter)
	{
		this.teleporter = teleporter;
	}
	
	public long getNextTeleportationTime()
	{
		return nextTeleportationTime;
	}

	public void setNextTeleportationTime(long nextTeleportationTime)
	{
		this.nextTeleportationTime = nextTeleportationTime;
	}
	
	public WarpLocation getLastLocation()
	{
		return lastLocation;
	}

	public void setLastLocation(WarpLocation lastLocation)
	{
		this.lastLocation = lastLocation;
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
