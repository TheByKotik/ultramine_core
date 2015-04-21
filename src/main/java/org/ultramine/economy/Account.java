package org.ultramine.economy;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

public abstract class Account
{
	protected final Map<Currency, Holdings> holdings = new HashMap<Currency, Holdings>();
	
	public abstract String getName();
	
	protected abstract void onHoldingsChange(Holdings holdings);
	
	protected abstract void onHoldingsCreate(Holdings holdings);
	
	public Holdings getHoldingsOf(Currency cur)
	{
		Holdings hlds = holdings.get(cur);
		if(hlds == null)
		{
			hlds = new Holdings(this, cur);
			holdings.put(cur, hlds);
			onHoldingsCreate(hlds);
		}
		return hlds;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		for(Map.Entry<Currency, Holdings> ent : holdings.entrySet())
		{
			nbt.setDouble(ent.getKey().getCode(), ent.getValue().getBalance());
		}
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		for(Object code : nbt.func_150296_c())
		{
			Currency cur = CurrencyRegistry.getCurrency((String)code);
			if(cur != null)
				holdings.put(cur, new Holdings(this, cur, nbt.getDouble((String)code)));
		}
	}
}
