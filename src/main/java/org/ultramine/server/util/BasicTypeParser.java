package org.ultramine.server.util;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.EnumDifficulty;

public class BasicTypeParser
{
	public static boolean isInt(String val)
	{
		int len = val.length();
		if(len > 11 || len == 0) return false;
		if(len > 9)
		{
			try
			{
				Integer.parseInt(val);
				return true;
			} catch(NumberFormatException e){return false;}
		}
		
		int i = 0;
		if(val.charAt(0) == '-')
		{
			i = 1;
			if(len == 1) return false;
		}
		
		for(; i < len; i++)
		{
			char c = val.charAt(i);
			if(c < '0' || c > '9') return false;
		}
		
		return true;
	}
	
	public static boolean isUnsignedInt(String val)
	{
		int len = val.length();
		if(len > 10 || len == 0) return false;
		if(len == 10)
		{
			try
			{
				if(Integer.parseInt(val) >= 0) return true;
			} catch(NumberFormatException e){return false;}
		}
		
		for(int i = 0; i < len; i++)
		{
			char c = val.charAt(i);
			if(c < '0' || c > '9') return false;
		}
		
		return true;
	}
	
	public static EnumDifficulty parseDifficulty(String str)
	{
		if(isUnsignedInt(str))
		{
			return EnumDifficulty.getDifficultyEnum(Math.min(Integer.parseInt(str), 3));
		}
		
		str = str.toLowerCase();
		
		if(str.equals("p") || str.equals("peaceful"))
		{
			return EnumDifficulty.PEACEFUL;
		}
		else if(str.equals("e") || str.equals("easy"))
		{
			return EnumDifficulty.EASY;
		}
		else if(str.equals("n") || str.equals("normal"))
		{
			return EnumDifficulty.NORMAL;
		}
		else if(str.equals("h") || str.equals("hard"))
		{
			return EnumDifficulty.HARD;
		}
		
		return null;
	}
	
	public static EnumChatFormatting parseColor(String str)
	{
		if(!str.isEmpty())
		{
			char c = str.charAt(0);
			return EnumChatFormatting.getByColorCode(str.length() == 1 ? c : c == '&' ? str.charAt(1) : c);
		}
		
		return null;
	}
}
