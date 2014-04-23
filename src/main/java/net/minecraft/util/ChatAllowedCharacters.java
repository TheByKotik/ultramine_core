package net.minecraft.util;

public class ChatAllowedCharacters
{
	public static final char[] allowedCharacters = new char[] {'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
	private static final String __OBFID = "CL_00001606";

	public static boolean isAllowedCharacter(char par0)
	{
		return par0 != 167 && par0 >= 32 && par0 != 127;
	}

	public static String filerAllowedCharacters(String par0Str)
	{
		StringBuilder stringbuilder = new StringBuilder();
		char[] achar = par0Str.toCharArray();
		int i = achar.length;

		for (int j = 0; j < i; ++j)
		{
			char c0 = achar[j];

			if (isAllowedCharacter(c0))
			{
				stringbuilder.append(c0);
			}
		}

		return stringbuilder.toString();
	}
}