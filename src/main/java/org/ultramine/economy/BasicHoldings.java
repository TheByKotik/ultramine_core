package org.ultramine.economy;

import net.minecraft.command.CommandException;
import net.minecraft.nbt.NBTTagCompound;

public class BasicHoldings implements IHoldings
{
	private final Account acc;
	private final Currency cur;
	
	private long balance;

	public BasicHoldings(Account acc, Currency cur)
	{
		this.acc = acc;
		this.cur = cur;
	}
	
	public BasicHoldings(Account acc, Currency cur, double balance)
	{
		this(acc, cur);
		this.balance = (int)(balance*100);
	}
	
	public BasicHoldings(Account acc, Currency cur, long balanceInternal)
	{
		this(acc, cur);
		this.balance = balanceInternal;
	}
	
	long getBalanceInternal()
	{
		return this.balance;
	}

	@Override
	public Account getAccount()
	{
		return acc;
	}

	@Override
	public Currency getCurrency()
	{
		return cur;
	}

	@Override
	public double getBalance()
	{
		return balance / 100.0d;
	}

	@Override
	public void setBalanceSilently(double balance)
	{
		this.balance = (int)(balance*100);
	}

	@Override
	public void setBalance(double balance)
	{
		this.balance = (int)(balance*100);
		acc.onHoldingsChange(this);
	}

	@Override
	public void add(double amount)
	{
		if(amount <= 0.0d)
			throw new CommandException("economy.fail.negativeamount");
		this.balance = Math.addExact(this.balance, floor(amount*100));
		acc.onHoldingsChange(this);
	}

	@Override
	public void subtract(double amount)
	{
		if(amount <= 0.0d)
			throw new CommandException("economy.fail.negativeamount");
		this.balance = Math.subtractExact(this.balance, ceiling(amount*100));
		acc.onHoldingsChange(this);
	}

	@Override
	public void subtractChecked(double amount)
	{
		if(Math.subtractExact(balance, ceiling(amount*100)) < 0L)
			throw new CommandException("economy.fail.notenough");
		subtract(amount);
	}

	@Override
	public void divide(double amount)
	{
		this.balance /= amount;
		acc.onHoldingsChange(this);
	}

	@Override
	public void multiply(double amount)
	{
		this.balance *= amount;
		acc.onHoldingsChange(this);
	}

	@Override
	public boolean isNegative()
	{
		return this.balance < 0L;
	}

	@Override
	public boolean hasEnough(double amount)
	{
		return ceiling(amount*100) <= this.balance;
	}

	@Override
	public boolean hasOver(double amount)
	{
		return ceiling(amount*100) < this.balance;
	}

	@Override
	public void transact(IHoldings to, double amount)
	{
		this.subtract(amount);
		to.add(amount);
	}

	@Override
	public void transactChecked(IHoldings to, double amount)
	{
		this.subtractChecked(amount);
		to.add(amount);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setLong("b", this.balance);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		this.balance = nbt.getLong("b");
	}

	private static long ceiling(double arg)
	{
		long i = (long)arg;
		return arg > (double)i ? Math.addExact(i, 1) : i;
	}

	private static long floor(double arg)
	{
		long i = (long)arg;
		return arg < (double)i ? Math.subtractExact(i, 1) : i;
	}
}
