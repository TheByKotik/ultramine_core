package org.ultramine.economy;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class Currency
{
	private static final NumberFormat dformat = new DecimalFormat("#0.##");
	
	private final String code;
	private final String sign;
	private final String dispName;
	
	Currency(String code, String sign, String dispName)
	{
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
}
