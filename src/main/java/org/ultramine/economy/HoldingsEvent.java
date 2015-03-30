package org.ultramine.economy;

import cpw.mods.fml.common.eventhandler.Event;

public class HoldingsEvent extends Event
{
	public final Holdings holdings;

	protected HoldingsEvent(Holdings holdings)
	{
		this.holdings = holdings;
	}
}
