package org.ultramine.economy;

import net.minecraft.command.CommandException;
import net.minecraft.util.MathHelper;

public class Holdings
{
	private final Account acc;
	private final Currency cur;
	
	private long balance;

	Holdings(Account acc, Currency cur)
	{
		this.acc = acc;
		this.cur = cur;
	}
	
	Holdings(Account acc, Currency cur, double balance)
	{
		this(acc, cur);
		this.balance = (int)(balance*100);
	}
	
	Holdings(Account acc, Currency cur, long balanceInternal)
	{
		this(acc, cur);
		this.balance = balanceInternal;
	}
	
	long getBalanceInternal()
	{
		return this.balance;
	}

	public Account getAccount()
	{
		return acc;
	}

	public Currency getCurrency()
	{
		return cur;
	}

	public double getBalance()
	{
		return balance / 100.0d;
	}
	
	void setBalanceSilently(double balance)
	{
		this.balance = (int)(balance*100);
	}

	public void setBalance(double balance)
	{
		this.balance = (int)(balance*100);
		acc.onHoldingsChange(this);
	}

	public void add(double amount)
	{
		if(amount <= 0.0d)
			throw new CommandException("economy.fail.negativeamount");
		this.balance += MathHelper.floor_double(amount*100);
		acc.onHoldingsChange(this);
	}

	public void subtract(double amount)
	{
		if(amount <= 0.0d)
			throw new CommandException("economy.fail.negativeamount");
		this.balance -= MathHelper.ceiling_double_int(amount*100);
		acc.onHoldingsChange(this);
	}
	
	public void subtractChecked(double amount)
	{
		if((balance - MathHelper.ceiling_double_int(amount*100)) < 0L)
			throw new CommandException("economy.fail.notenough");
		subtract(amount);
	}

	public void divide(double amount)
	{
		this.balance /= amount;
		acc.onHoldingsChange(this);
	}

	public void multiply(double amount)
	{
		this.balance *= amount;
		acc.onHoldingsChange(this);
	}

	public boolean isNegative()
	{
		return this.balance < 0L;
	}

	public boolean hasEnough(double amount)
	{
		return MathHelper.ceiling_double_int(amount*100) <= this.balance;
	}

	public boolean hasOver(double amount)
	{
		return MathHelper.ceiling_double_int(amount*100) < this.balance;
	}
	
	public void transact(Holdings to, double amount)
	{
		this.subtract(amount);
		to.add(amount);
	}
	
	public void transactChecked(Holdings to, double amount)
	{
		this.subtractChecked(amount);
		to.add(amount);
	}
}
