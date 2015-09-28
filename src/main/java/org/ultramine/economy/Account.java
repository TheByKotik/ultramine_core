package org.ultramine.economy;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public abstract class Account
{
	protected final Map<Currency, IHoldings> holdings = new HashMap<Currency, IHoldings>();
	
	public abstract String getName();
	
	public abstract void onHoldingsChange(IHoldings holdings);
	
	public abstract void onHoldingsCreate(IHoldings holdings);
	
	public IHoldings getHoldingsOf(Currency cur)
	{
		IHoldings hlds = holdings.get(cur);
		if(hlds == null)
		{
			hlds = cur.createHoldings(this);
			holdings.put(cur, hlds);
			onHoldingsCreate(hlds);
		}
		return hlds;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		for(Map.Entry<Currency, IHoldings> ent : holdings.entrySet())
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			ent.getValue().writeToNBT(nbt1);
			nbt.setTag(ent.getKey().getCode(), nbt1);
		}
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		for(Object code : nbt.func_150296_c())
		{
			Currency cur = CurrencyRegistry.getCurrency((String)code);
			if(cur != null)
			{
				IHoldings hlds = cur.createHoldings(this);
				NBTBase b = nbt.getTag((String)code);
				if(b instanceof NBTTagCompound)
					hlds.readFromNBT((NBTTagCompound)b);
				else
					hlds.setBalanceSilently(nbt.getDouble((String)code));//TODO remove backward compatibility
				holdings.put(cur, hlds);
			}
		}
	}
}
