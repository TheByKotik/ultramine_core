package org.ultramine.economy;

import net.minecraftforge.common.MinecraftForge;

import org.ultramine.server.data.player.PlayerData;

public class PlayerAccount extends Account
{
	private final PlayerData data;

	public PlayerAccount(PlayerData data)
	{
		this.data = data;
	}
	
	@Override
	public String getName()
	{
		return data.getProfile().getName();
	}

	@Override
	public void onHoldingsChange(IHoldings holdings)
	{
		MinecraftForge.EVENT_BUS.post(new PlayerHoldingsEvent.ChangeEvent(holdings, data));
		data.save();
	}

	@Override
	public void onHoldingsCreate(IHoldings holdings)
	{
		MinecraftForge.EVENT_BUS.post(new PlayerHoldingsEvent.CreateEvent(holdings, data));
	}
}
