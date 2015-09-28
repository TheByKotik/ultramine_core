package org.ultramine.economy;

import net.minecraft.nbt.NBTTagCompound;

public interface IHoldings
{
	Account getAccount();

	public Currency getCurrency();

	double getBalance();
	
	void setBalanceSilently(double balance);

	void setBalance(double balance);

	void add(double amount);

	void subtract(double amount);
	
	void subtractChecked(double amount);

	void divide(double amount);

	void multiply(double amount);

	boolean isNegative();

	boolean hasEnough(double amount);

	boolean hasOver(double amount);
	
	void transact(IHoldings to, double amount);
	
	void transactChecked(IHoldings to, double amount);
	
	void writeToNBT(NBTTagCompound nbt);
	
	void readFromNBT(NBTTagCompound nbt);
}
