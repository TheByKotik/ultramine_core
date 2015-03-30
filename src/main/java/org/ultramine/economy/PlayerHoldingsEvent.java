package org.ultramine.economy;

import org.ultramine.server.data.player.PlayerData;

public class PlayerHoldingsEvent extends HoldingsEvent
{
	public final PlayerData player;
	
	protected PlayerHoldingsEvent(Holdings holdings, PlayerData player)
	{
		super(holdings);
		this.player = player;
	}
	
	public static class CreateEvent extends PlayerHoldingsEvent
	{
		public CreateEvent(Holdings holdings, PlayerData player)
		{
			super(holdings, player);
		}
	}
	
	public static class ChangeEvent extends PlayerHoldingsEvent
	{
		public ChangeEvent(Holdings holdings, PlayerData player)
		{
			super(holdings, player);
		}
	}
}
