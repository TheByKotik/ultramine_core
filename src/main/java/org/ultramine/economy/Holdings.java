package org.ultramine.economy;

import net.minecraft.command.CommandException;

public class Holdings
{
	private final Account acc;
	private final Currency cur;
	
	private double balance;

	Holdings(Account acc, Currency cur)
	{
		this.acc = acc;
		this.cur = cur;
	}
	
	Holdings(Account acc, Currency cur, double balance)
	{
		this(acc, cur);
		this.balance = balance;
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
		return balance;
	}
	
	void setBalanceSilently(double balance)
	{
		this.balance = balance;
	}

	public void setBalance(double balance)
	{
		this.balance = balance;
		acc.onHoldingsChange(this);
	}

	public void add(double amount)
	{
		if(amount <= 0.0d)
			throw new CommandException("economy.fail.negativeamount");
		this.balance += amount;
		acc.onHoldingsChange(this);
	}

	public void subtract(double amount)
	{
		if(amount <= 0.0d)
			throw new CommandException("economy.fail.negativeamount");
		this.balance -= amount;
		acc.onHoldingsChange(this);
	}
	
	public void subtractChecked(double amount)
	{
		if((balance - amount) < 0.0d)
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
		return this.balance < 0.0;
	}

	public boolean hasEnough(double amount)
	{
		return amount <= this.balance;
	}

	public boolean hasOver(double amount)
	{
		return amount < this.balance;
	}

	public boolean hasUnder(double amount)
	{
		return amount > this.balance;
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
