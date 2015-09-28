package org.ultramine.economy;

import cpw.mods.fml.common.eventhandler.Event;

public class HoldingsEvent extends Event
{
	public final IHoldings holdings;

	protected HoldingsEvent(IHoldings holdings)
	{
		this.holdings = holdings;
	}
}
