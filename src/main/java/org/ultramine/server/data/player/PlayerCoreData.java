package org.ultramine.server.data.player;

import java.util.HashMap;
import java.util.Map;

import org.ultramine.economy.Account;
import org.ultramine.economy.PlayerAccount;
import org.ultramine.server.Teleporter;
import org.ultramine.server.util.WarpLocation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PlayerCoreData extends PlayerDataExtension
{
	private final Map<String, WarpLocation> homes = new HashMap<String, WarpLocation>();
	private final PlayerAccount account;
	
	//undatabased
	private Teleporter teleporter;
	private long nextTeleportationTime;
	private WarpLocation lastLocation;
	private String lastMessagedPlayer;
	
	public PlayerCoreData(PlayerData data)
	{
		super(data);
		this.account = new PlayerAccount(data);
	}
	
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
	
	public Account getAccount()
	{
		return account;
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
	
	public String getLastMessagedPlayer()
	{
		return lastMessagedPlayer;
	}
	
	public void setLastMessagedPlayer(String lastMessagedPlayer)
	{
		this.lastMessagedPlayer = lastMessagedPlayer;
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
		
		NBTTagCompound accnbt = new NBTTagCompound();
		account.writeToNBT(accnbt);
		nbt.setTag("acc", accnbt);
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
		
		account.readFromNBT(nbt.getCompoundTag("acc"));
	}
}
