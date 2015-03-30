package org.ultramine.economy;

import java.util.HashMap;
import java.util.Map;

public final class CurrencyRegistry
{
	private static final Map<String, Currency> currencies = new HashMap<String, Currency>();
	
	public static final Currency GSC = registerCurrency("GSC", "$", "dollar");
	
	public static Currency registerCurrency(String code, String sign, String dispName)
	{
		code = code.toUpperCase();
		if(currencies.containsKey(code))
			throw new IllegalStateException("Currency with code "+code+" is already registered");
		Currency cur = new Currency(code, sign, dispName);
		currencies.put(code, cur);
		return cur;
	}
	
	public static Currency getCurrency(String code)
	{
		return currencies.get(code.toUpperCase());
	}
}
