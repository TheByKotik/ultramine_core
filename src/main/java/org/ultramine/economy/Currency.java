package org.ultramine.economy;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class Currency
{
	private static final NumberFormat dformat = new DecimalFormat("#0.##");
	
	private final Constructor<? extends IHoldings> cls;
	private final String code;
	private final String sign;
	private final String dispName;
	
	Currency(Class<? extends IHoldings> cls, String code, String sign, String dispName)
	{
		try
		{
			this.cls = cls.getDeclaredConstructor(Account.class, Currency.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
		this.code = code;
		this.sign = sign;
		this.dispName = dispName;
	}

	public String getCode()
	{
		return code;
	}

	public String getSign()
	{
		return sign;
	}
	
	public String getDisplayName()
	{
		return dispName;
	}
	
	public String format(double amount)
	{
		return dformat.format(amount) + sign;
	}
	
	IHoldings createHoldings(Account acc)
	{
		try
		{
			return cls.newInstance(acc, this);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
